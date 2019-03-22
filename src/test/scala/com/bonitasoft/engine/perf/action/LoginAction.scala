package com.bonitasoft.engine.perf.action

import com.bonitasoft.engine.perf.protocol.BonitaComponents
import io.gatling.commons.stats._
import io.gatling.commons.util.Clock
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, BlockExit, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import org.bonitasoft.engine.api.APIClient


class LoginAction(coreComponents: CoreComponents,
                  bonitaComponents: BonitaComponents,
                  val statsEngine: StatsEngine,
                  val next: Action,
                  val username:String,
                  val password: String) extends ExitableAction with NameGen {
  override def name: String = "login"

  override def execute(session: Session): Unit = {
    val start = System.currentTimeMillis
    val client = bonitaComponents.client
    try {
      client.login(username, password)
      statsEngine.logResponse(session, name, start, System.currentTimeMillis(), OK, None, None)
    } catch {
      case _: Throwable =>
        statsEngine.logResponse(session, name, start, System.currentTimeMillis(), KO, None, None)

    }
    next ! session.setAll(("error",false),("client", client))
  }

  override def clock: Clock = coreComponents.clock
}
