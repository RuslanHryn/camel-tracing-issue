package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:firstRoute")
                .log("firstRoute received message")
                .pollEnrich("seda:secondRoute", 0)
                .process(exchange -> {
                    //
                });

        from("seda:secondRoute")
                .process(exchange -> {
                    //
                });
    }
}
