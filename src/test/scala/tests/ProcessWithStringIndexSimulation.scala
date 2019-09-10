package tests

import java.util

import com.bonitasoft.engine.perf.Predef._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder
import com.bonitasoft.engine.perf.utils.ContractUtils
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

class ProcessWithStringIndexSimulation extends Simulation {

  def contract1() : util.HashMap[String, java.io.Serializable] = {
    val stepContract :util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()
    stepContract.put("comment", ContractUtils.serialize("this is my data"))

    return stepContract
  }

  val bonitaProtocol: BonitaProtocolBuilder = bonita()
    .remote()
    .setupTest(client => { })

  val scn: ScenarioBuilder = scenario("Login logout") // A scenario is a chain of requests and pauses
    .exec(login()
      .withUser("walter.bates", "bpm"))
    .exec(startProcess("processWithStringIndex", "2.0"))
    .exec(logout())

  //setUp(scn.inject(atOnceUsers(2)).protocols(bonitaProtocol))
  setUp(scn.inject(rampUsers(100000).during(12000 seconds)).protocols(bonitaProtocol))

}
