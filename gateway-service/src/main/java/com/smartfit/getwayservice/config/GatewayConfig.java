package com.smartfit.getwayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("conference-service", r -> r.path("/conference/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://CONFERENCE-SERVICE"))
                .route("keynote-service", r -> r.path("/keynote/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://KEYNOTE-SERVICE"))
                .route("notification-service", r -> r.path("/notification/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://NOTIFICATION-SERVICE"))
                .build();
    }
}
