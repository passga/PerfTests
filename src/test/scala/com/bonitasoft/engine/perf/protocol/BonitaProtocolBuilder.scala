package com.bonitasoft.engine.perf.protocol

import java.util

import org.bonitasoft.engine.api.APIClient

object BonitaProtocolBuilder {

  implicit def toBonitaProtocol(builder: BonitaProtocolBuilder): BonitaProtocol = builder.build

  def apply(): BonitaProtocolBuilder =
    BonitaProtocolBuilder(BonitaProtocol())
}

/**
  * Builder for HttpProtocol used in DSL
  *
  * @param protocol the protocol being built
  */
case class BonitaProtocolBuilder(protocol: BonitaProtocol) {

  def local(): BonitaProtocolBuilder = {
    this.protocol.local = true
    this
  }
  def remote(): BonitaProtocolBuilder = {
    this.protocol.local = false
    val settings = new util.HashMap[String,String]()
    settings.put("server.url", System.getProperty("bonitaUrl"))
    settings.put("application.name", System.getProperty("bonitaContext"))
    this.protocol.settings = settings;

    this
  }
  //"http://localhost:8080","bonita"
  def remote(serverUrl: String, applicationName: String): BonitaProtocolBuilder = {
    this.protocol.local = false
    val settings = new util.HashMap[String,String]()
    settings.put("server.url", serverUrl)
    settings.put("application.name", applicationName)
    this.protocol.settings = settings;

    this
  }

  def setupTest(setup: APIClient => Unit): BonitaProtocolBuilder = {
    this.protocol.setupTest = setup
    this
  }

  def build: BonitaProtocol = protocol
}