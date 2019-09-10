package tests

import java.util

import com.bonitasoft.engine.perf.Predef._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder
import com.bonitasoft.engine.perf.utils.ContractUtils
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

class SimpleProcessSimulation extends Simulation {
  def contract1(): util.HashMap[String, java.io.Serializable] = {
    val stepContract: util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()
    stepContract.put("input1", ContractUtils.serialize("this is my data"))
    return stepContract
  }

  val bonitaProtocol: BonitaProtocolBuilder = bonita()
    .remote()
    .setupTest(client => { })

  val starProcess: ScenarioBuilder = scenario("starProcess") // A scenario is a chain of requests and pauses
    .exec(login()
    .withUser("walter.bates", "bpm"))
    .exec(startProcess("simpleProcessTest", "1.0"))
    .exec(startProcess("simpleProcessTest", "1.0"))
    .exec(startProcess("simpleProcessTest", "1.0"))
    .exec(startProcess("simpleProcessTest", "1.0"))
    .exec(executeFirstOpenAction(contract1()))
    .exec(executeFirstOpenAction(contract1()))
    .exec(executeFirstOpenAction(contract1()))
    .exec(executeFirstOpenAction(contract1()))
    .exec(logout())

  //setUp(scn.inject(atOnceUsers(2)).protocols(bonitaProtocol))
  setUp(starProcess.inject(rampUsers(10000).during(30 minute)).protocols(bonitaProtocol))
}
