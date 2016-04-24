package io.codearte.accurest.stubrunner.messaging.stream

import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.StubConfiguration
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.binding.ChannelBindingService
import org.springframework.context.Lifecycle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.FilterEndpointSpec
import org.springframework.integration.dsl.GenericEndpointSpec
import org.springframework.integration.dsl.IntegrationFlowBuilder
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Service
/**
 * Spring Cloud Stream configuration that iterates over the downloaded Groovy DSLs
 * and registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@EnableBinding
@CompileStatic
class StubRunnerStreamConfiguration {

	@Bean
	FlowRegistrar service(AutowireCapableBeanFactory beanFactory, BatchStubRunner batchStubRunner, ChannelBindingService channelBindingService) {
		Map<StubConfiguration, Collection<GroovyDsl>> accurestContracts = batchStubRunner.accurestContracts
		accurestContracts.each { StubConfiguration key, Collection<GroovyDsl> value ->
			String name = "${key.groupId}_${key.artifactId}"
			value.findAll { it?.input?.messageFrom }.each { GroovyDsl dsl ->
				String flowName = "${name}_${dsl.label}_${dsl.hashCode()}"
				IntegrationFlowBuilder builder = IntegrationFlows.from(dsl.input.messageFrom)
						.filter(new StubRunnerStreamMessageSelector(dsl), { FilterEndpointSpec e -> e.id("${flowName}.filter") } )
						.transform(new StubRunnerStreamTransformer(dsl), { GenericEndpointSpec e -> e.id("${flowName}.transformer") })
				if (dsl.outputMessage) {
					builder = builder.channel(dsl.outputMessage.sentTo)
				} else {
					builder = builder.handle(new DummyMessageHandler(), "handle")
				}
				beanFactory.initializeBean(builder.get(), flowName)
				beanFactory.getBean("${flowName}.filter", Lifecycle.class).start();
				beanFactory.getBean("${flowName}.transformer", Lifecycle.class).start();
				channelBindingService.bindConsumer(beanFactory.getBean(dsl.input.messageFrom, MessageChannel.class), dsl.input.messageFrom)
				if (dsl.outputMessage) {
					channelBindingService.bindProducer(beanFactory.getBean(dsl.outputMessage.sentTo, MessageChannel.class), dsl.outputMessage.sentTo)
				}
			}
		}
		return new FlowRegistrar()
	}

	@CompileStatic
	private static class DummyMessageHandler {
		void handle(Message message) {}
	}

	@Service
	static class FlowRegistrar {}
}