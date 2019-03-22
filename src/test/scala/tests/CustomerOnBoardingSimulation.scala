package tests

import java.util

import com.bonitasoft.engine.perf.Predef._
import com.bonitasoft.engine.perf.protocol.BonitaProtocolBuilder
import com.bonitasoft.engine.perf.utils.ContractUtils
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder


class CustomerOnBoardingSimulation extends Simulation {

  def contractInstantiation() : util.HashMap[String, java.io.Serializable] = {
    val stepContract :util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()
    val customer :util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()
    val address :util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()

    customer.put("firstName", "firstName")
    customer.put("lastName", "lastName")
    customer.put("middleName", "middleName")
    customer.put("phoneNumber", "phoneNumber")
    customer.put("eMailAddress", "eMailAddress")

    address.put("street","street")
    address.put("city","city")
    address.put("zipcode","zipcode")
    address.put("country","country")

    customer.put("address", ContractUtils.serialize(address))
    stepContract.put("customerInput", ContractUtils.serialize(customer))
    return stepContract
  }
  def contractFile(filename:String) : util.HashMap[String, java.io.Serializable] = {
    val stepContract :util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()
    stepContract.put(filename, ContractUtils.newFile(filename))
    return stepContract
  }

  val bonitaProtocol: BonitaProtocolBuilder = bonita()
    .remote()
    .setupTest(client => { })

  val scn: ScenarioBuilder = scenario("Login logout") // A scenario is a chain of requests and pauses
    .exec(login()
    .withUser("walter.bates", "bpm"))
    .exec(startProcess("CustomerOnboarding", "1.0",contractInstantiation))
    .exec(executeTaskWhenReady("Tax statement",contractFile("taxStatementInput"),"walter.bates"))
    .exec(executeTaskWhenReady("Provide identification",contractFile("identificationInput"),"walter.bates"))
    .exec(executeTaskWhenReady("Upload proof of domicile",contractFile("proofOfDomicileInput"),"walter.bates"))
    .exec(waitForProcessCompletion())
    .exec(logout())

  setUp(scn.inject(atOnceUsers(2)).protocols(bonitaProtocol))
    //setUp(scn.inject(rampUsers(500).during(20 seconds)).protocols(bonitaProtocol))



}
