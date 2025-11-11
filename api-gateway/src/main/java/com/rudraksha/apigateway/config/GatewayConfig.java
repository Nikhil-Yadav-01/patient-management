package com.rudraksha.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    // Local: "http://localhost:4000"
    // Docker: "http://patient-service:4000" (use container name)
    private static final String PATIENT_SERVICE_URI = "http://patient-service:4000";

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ---------------- Patients API ----------------------
                // API Gateway -> http://localhost:4004/api/patients
                // Patient Service -> http://localhost:4000/patients
                .route("patient-service-route", r -> r
                        .path("/api/patients/**")
                        .filters(f -> f.stripPrefix(1)) // removes /api prefix
                        .uri(PATIENT_SERVICE_URI))

                // --------------- Swagger/OpenAPI docs ----------------
                // API Gateway -> http://localhost:4004/api-docs/patients
                // Patient Service -> http://localhost:4000/v3/api-docs
                .route("api-docs-patient-route", r -> r
                        .path("/api-docs/patients")
                        .filters(f -> f.rewritePath(
                                "/api-docs/patients", // incoming
                                "/v3/api-docs"        // actual path in patient service
                        ))
                        .uri(PATIENT_SERVICE_URI))

                .build();
    }
}
