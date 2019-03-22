package com.bonitasoft.engine.perf.action

import com.bonitasoft.engine.perf.protocol.BonitaComponents
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import org.bonitasoft.engine.bpm.process.{ArchivedProcessInstance, ProcessInstanceNotFoundException}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}


class WaitForProcessCompletionAction(coreComponents: CoreComponents,
                                     bonitaComponents: BonitaComponents,
                                     val statsEngine: StatsEngine,
                                     val next: Action) extends ExitableAction with NameGen with LoggedInAction {
  override def name: String = "complete-process"

  override def execute(session: Session): Unit = {
    val apiClient = client(session)


    implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(bonitaComponents.executor)

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
    val processInstanceId = session.attributes("processInstanceId").asInstanceOf[Long]
    val completedProcess = Future[ArchivedProcessInstance] {
      var archivedProcessInstance: ArchivedProcessInstance = null
      while (archivedProcessInstance == null) {
        try {
          apiClient.getProcessAPI.getProcessInstance(processInstanceId)
          Thread.sleep(100)
        } catch {
          case _: ProcessInstanceNotFoundException =>
            archivedProcessInstance = apiClient.getProcessAPI.getFinalArchivedProcessInstance(processInstanceId)
        }
      }
      archivedProcessInstance
    }
    completedProcess.onComplete(p => {
      val archivedProcessInstance = p.get

      val start = archivedProcessInstance.getStartDate.getTime
      val end = archivedProcessInstance.getEndDate.getTime
//      println(s"process $processInstanceId took ${end - start} ms")
      statsEngine.logResponse(session, s"$name-${archivedProcessInstance.getName}",
        start,
        end,
        OK,
        None,
        None
      )
      next ! session
    })
  }

  override def clock: Clock = coreComponents.clock
}
