package tests

import com.bonitasoft.engine.perf.Predef._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder


class PeriodicLoadServerlessSimulation extends Simulation {



  val bonitaProtocol: BonitaProtocolBuilder = bonita()
    .remote()
    .setupTest(client => { })

  val scn: ScenarioBuilder = scenario("Login logout") // A scenario is a chain of requests and pauses
    .exec(login()
    .withUser("walter.bates", "bpm"))
    .exec(startProcess("PeriodicLoadServerless", "1.0"))
    .exec(executeTaskWhenReady("Calculate Fibonnaci",null,"walter.bates"))
    .exec(waitForProcessCompletion())
    .exec(logout())


  setUp(scn.inject(atOnceUsers(100)).protocols(bonitaProtocol))
//  setUp(scn.inject(rampUsers(6000).during(600 seconds)).protocols(bonitaProtocol))

//mvn gatling:test -DbonitaUrl=http://localhost:8080 -DbonitaContext=bonita -Dgatling.simulationClass=tests.PeriodicLoadServerlessSimulation


}
