package com.bonitasoft.engine.perf.dsl

import com.bonitasoft.engine.perf.action.LoginActionBuilder

case class Login() {

  def withUser(username: String, password: String): LoginActionBuilder = new LoginActionBuilder(username, password)

}
