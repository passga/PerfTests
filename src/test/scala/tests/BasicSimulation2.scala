package tests

import com.bonitasoft.engine.perf.Predef._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

class BasicSimulation2 extends Simulation {

  val bonitaProtocol: BonitaProtocolBuilder = bonita()
    .remote()
    .setupTest(client => { })

  val scn: ScenarioBuilder = scenario("Login logout") // A scenario is a chain of requests and pauses
    .exec(login()
    .withUser("walter.bates", "bpm"))
    .exec(startProcess("Test2", "1.0"))
    .exec(executeTaskWhenReady("Step3",null,"walter.bates"))
    .exec(executeTaskWhenReady("Step4",null,"walter.bates"))
    .exec(executeTaskWhenReady("Step5",null,"walter.bates"))
    .exec(executeTaskWhenReady("Step6",null,"walter.bates"))
    .exec(waitForProcessCompletion())
    .exec(logout())

  //setUp(scn.inject(atOnceUsers(2)).protocols(bonitaProtocol))
    setUp(scn.inject(rampUsers(500).during(20 seconds)).protocols(bonitaProtocol))
}
