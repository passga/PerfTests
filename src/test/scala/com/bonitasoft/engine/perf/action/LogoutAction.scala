package com.bonitasoft.engine.perf.action

import com.bonitasoft.engine.perf.protocol.BonitaComponents
import io.gatling.commons.stats.OK
import io.gatling.commons.util.Clock
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen


class LogoutAction(coreComponents: CoreComponents,
                   bonitaComponents: BonitaComponents,
                   val statsEngine: StatsEngine,
                   val next: Action) extends ExitableAction with NameGen with LoggedInAction {
  override def name: String = "logout"

  override def execute(session: Session): Unit = {
    val start = System.currentTimeMillis
    client(session).logout()
    statsEngine.logResponse(session, "logout", start, System.currentTimeMillis(), OK, None, None)
    next ! session.remove("client")
  }

  override def clock: Clock = coreComponents.clock
}
