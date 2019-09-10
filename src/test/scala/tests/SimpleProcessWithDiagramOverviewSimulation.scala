package tests

import java.util

import com.bonitasoft.engine.perf.Predef._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder
import com.bonitasoft.engine.perf.utils.ContractUtils
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

class SimpleProcessWithDiagramOverviewSimulation extends Simulation {
  def contract1(): util.HashMap[String, java.io.Serializable] = {
    val stepContract: util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()
    stepContract.put("input1", ContractUtils.serialize("this is my data"))
    return stepContract
  }

  val bonitaProtocol: BonitaProtocolBuilder = bonita()
    .remote()
    .setupTest(client => { })

  val scn: ScenarioBuilder = scenario("Login logout") // A scenario is a chain of requests and pauses
    .exec(login()
    .withUser("walter.bates", "bpm"))
    .exec(startProcess("SimpleProcessWithDiagramOverview", "1.0"))
    .exec(executeTaskWhenReady("Task1", contract1(), "walter.bates"))
    .exec(executeTaskWhenReady("Task2", null, "walter.bates"))
    .exec(executeTaskWhenReady("Task3", null, "walter.bates"))
    .exec(logout())



  //setUp(scn.inject(atOnceUsers(2)).protocols(bonitaProtocol))
  setUp(scn.inject(rampUsers(100000).during(12000 seconds)).protocols(bonitaProtocol))
}
