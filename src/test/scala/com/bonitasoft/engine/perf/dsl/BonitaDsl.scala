package com.bonitasoft.engine.perf.dsl

import java.io.Serializable
import java.util

import com.bonitasoft.engine.perf.action._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder

trait BonitaDsl {
  def bonita() = BonitaProtocolBuilder()

  def login() = Login()

  def logout() = new LogoutActionBuilder()

  def startProcess(processName: String, processVersion: String) = new StartProcessActionBuilder(processName, processVersion)

  def startProcess(processName: String, processVersion: String, contract: util.Map[String,Serializable]) = new StartProcessContractActionBuilder(processName, processVersion, contract)

  def waitForProcessCompletion() = new WaitForProcessCompletionActionBuilder()

  def executeTaskWhenReady(taskName: String, contract: util.Map[String,Serializable], username: String) = new ExecuteTaskActionBuilder(taskName, contract, username)

  def executeFirstOpenAction(contract: util.Map[String,Serializable]) = new ExecuteFirstOpenActionBuilder(contract)
}
