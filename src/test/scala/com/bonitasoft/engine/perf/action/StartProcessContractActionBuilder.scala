package com.bonitasoft.engine.perf.action

import com.bonitasoft.engine.perf.protocol.BonitaProtocol

import java.io.Serializable
import java.util
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.structure.ScenarioContext

class StartProcessContractActionBuilder(processName: String, processVersion: String, contract: util.Map[String,Serializable]) extends ActionBuilder {

  private def components(protocolComponentsRegistry: ProtocolComponentsRegistry) =
    protocolComponentsRegistry.components(BonitaProtocol.BonitaProtocolKey)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._

    val statsEngine = coreComponents.statsEngine
    val bonitaComponents = components(protocolComponentsRegistry)
    new StartProcessContractAction(coreComponents,
      bonitaComponents,
      statsEngine,
      next,
      processName,
      processVersion,
      contract)
  }
}
