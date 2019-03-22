package com.bonitasoft.engine.perf.action

import io.gatling.core.session.Session
import org.bonitasoft.engine.api.APIClient

trait LoggedInAction {

  def client(session:Session): APIClient = session.attributes("client").asInstanceOf[APIClient]

}
