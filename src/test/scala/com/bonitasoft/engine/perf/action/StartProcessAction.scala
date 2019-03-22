package com.bonitasoft.engine.perf.action

import com.bonitasoft.engine.perf.protocol.BonitaComponents
import io.gatling.commons.stats.OK
import io.gatling.commons.util.Clock
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen


class StartProcessAction(coreComponents: CoreComponents,
                         bonitaComponents: BonitaComponents,
                         val statsEngine: StatsEngine,
                         val next: Action,
                         val processName: String,
                         val processVersion: String) extends ExitableAction with NameGen with LoggedInAction {
  override def name: String = s"start-$processName-$processVersion"

  override def execute(session: Session): Unit = {
    val start = System.currentTimeMillis
    val apiClient = client(session)
    val processDefinitionId = apiClient.getProcessAPI.getProcessDefinitionId(processName, processVersion)
    val instance = apiClient.getProcessAPI.startProcess(processDefinitionId)
    statsEngine.logResponse(session, name, start, System.currentTimeMillis(), OK, None, None)
    next ! session.set("processInstanceId", instance.getId)
  }

  override def clock: Clock = coreComponents.clock
}
