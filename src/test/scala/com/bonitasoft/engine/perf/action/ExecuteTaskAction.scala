package com.bonitasoft.engine.perf.action

import java.io.Serializable
import java.util

import com.bonitasoft.engine.perf.protocol.BonitaComponents
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import org.apache.commons.lang3.exception.ExceptionUtils
import org.bonitasoft.engine.bpm.flownode.{ArchivedHumanTaskInstance, ArchivedHumanTaskInstanceSearchDescriptor, HumanTaskInstance, HumanTaskInstanceSearchDescriptor}
import org.bonitasoft.engine.bpm.process.{ProcessInstance, ProcessInstanceNotFoundException}
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.search.{SearchOptionsBuilder, SearchResult}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}


class ExecuteTaskAction(coreComponents: CoreComponents,
                        bonitaComponents: BonitaComponents,
                        val statsEngine: StatsEngine,
                        val next: Action,
                        val taskName: String,
                        val contract: util.Map[String,Serializable],
                        val username: String) extends ExitableAction with NameGen with LoggedInAction {
  override def name: String = s"executeTask"


  override def execute(session: Session): Unit = {
    val apiClient = client(session)

    implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(bonitaComponents.executor)
    val processAPI = apiClient.getProcessAPI;
    if(session.attributes("error").asInstanceOf[Boolean]){
      statsEngine.logResponse(session, s"$name",
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        KO,
        None,
        None
      )
      next ! session
    }
    val userId = apiClient.getIdentityAPI.getUserByUserName(username).getId
    val processInstanceId = session.attributes("processInstanceId").asInstanceOf[Long]
    val taskReady = Future[HumanTaskInstance] {
      var humanTaskInstance: HumanTaskInstance = null
      val processInstance: ProcessInstance = processAPI.getProcessInstance(processInstanceId)
      while (humanTaskInstance == null) {
        val sob : SearchOptionsBuilder = new SearchOptionsBuilder(0, 1)
        sob.filter(HumanTaskInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstance.getId)
        sob.and.filter(HumanTaskInstanceSearchDescriptor.NAME, taskName)
        val searchResult : SearchResult[HumanTaskInstance] = processAPI.searchHumanTaskInstances(sob.done)
        if(searchResult.getCount>0) {
          humanTaskInstance = searchResult.getResult.get(0)
          processAPI.assignUserTask(humanTaskInstance.getId, userId)
          try {
            processAPI.executeUserTask(userId, humanTaskInstance.getId, contract)
          } catch {
            case e: BonitaException =>
              System.out.println(ExceptionUtils.getStackTrace(e))
              statsEngine.logCrash(session, s"$name-${taskName}",
                ExceptionUtils.getStackTrace(e)
              )
              next ! session.set("error",true)
          }
        }else
          Thread.sleep(100)
       //What if there is no task
      }
      humanTaskInstance
    }
    taskReady.onComplete(tr => {
      val taskCompleted = Future[ArchivedHumanTaskInstance] {
        val humanTaskInstance = tr.get
        var archivedHumanTaskInstance: ArchivedHumanTaskInstance = null
        while (archivedHumanTaskInstance == null) {
          val sob : SearchOptionsBuilder = new SearchOptionsBuilder(0, 1)
          sob.filter(ArchivedHumanTaskInstanceSearchDescriptor.ORIGINAL_HUMAN_TASK_ID, humanTaskInstance.getId)
          val searchResult : SearchResult[ArchivedHumanTaskInstance] = processAPI.searchArchivedHumanTasks(sob.done())
          if(searchResult.getCount>0) {
            archivedHumanTaskInstance = searchResult.getResult.get(0)
          }else
            Thread.sleep(100)
        }
        archivedHumanTaskInstance
      }
      taskCompleted.onComplete(tc => {
        val archivedHumanTaskInstance = tc.get
        val start = tr.get.getLastUpdateDate.getTime
        val end = archivedHumanTaskInstance.getArchiveDate.getTime
        //      println(s"process $processInstanceId took ${end - start} ms")
        statsEngine.logResponse(session, s"$name-${taskName}",
          start,
          end,
          OK,
          None,
          None
        )
        next ! session
      })
    })


  }
  override def clock: Clock = coreComponents.clock
}
