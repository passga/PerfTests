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
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.search.{SearchOptionsBuilder, SearchResult}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}


class ExecuteFirstOpenAction(coreComponents: CoreComponents,
                             bonitaComponents: BonitaComponents,
                             val statsEngine: StatsEngine,
                             val next: Action,
                             val contract: util.Map[String, Serializable]) extends ExitableAction with NameGen with LoggedInAction {
  override def name: String = s"executeFirstOpenTask"


  override def execute(session: Session): Unit = {

    val start = System.currentTimeMillis
    val apiClient = client(session)
    val processAPI = apiClient.getProcessAPI;
    val userId = apiClient.getSession.getUserId;
    val openTask = processAPI.getPendingHumanTaskInstances(userId, 0, 1, null)

    try {
      if (!openTask.isEmpty) {
        processAPI.assignUserTask(openTask.get(0).getId, userId)
        processAPI.executeUserTask(userId, openTask.get(0).getId, contract)
        statsEngine.logResponse(session, name, start, System.currentTimeMillis(), OK, None, None)
      } else {
        statsEngine.logResponse(session, name, start, System.currentTimeMillis(), KO, None, None)
      }
    } catch {
      case _: Throwable =>
        statsEngine.logResponse(session, name, start, System.currentTimeMillis(), KO, None, None)

    }


    next ! session
  }

  override def clock: Clock = coreComponents.clock
}
