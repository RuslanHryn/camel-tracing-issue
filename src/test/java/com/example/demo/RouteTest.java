package com.example.demo;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.apache.camel.opentelemetry.OpenTelemetryTracingStrategy;
import org.apache.camel.opentelemetry.starter.OpenTelemetryConfigurationProperties;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@CamelSpringBootTest
@TestPropertySource(properties = "camel.opentelemetry.enabled=true")
class RouteTest {

    @Autowired
    ProducerTemplate producerTemplate;

    @Test
    void testRoute() throws InterruptedException {
        producerTemplate.sendBody("seda:secondRoute", "test");

        Thread.sleep(100);

        producerTemplate.sendBody("direct:firstRoute", "secondRoute");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        OpenTelemetryTracer openTelemetryEventNotifier(CamelContext camelContext, OpenTelemetryConfigurationProperties config) {
            OpenTelemetryTracer ottracer = new OpenTelemetryTracer();
            OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                    .buildAndRegisterGlobal();
            Tracer tracer = openTelemetry.getTracer("opentelemetry-instrumentation-java");

            ottracer.setTracer(tracer);
            ottracer.setTracingStrategy(new OpenTelemetryTracingStrategy(ottracer));

            ottracer.init(camelContext);
            return ottracer;
        }
    }
}
