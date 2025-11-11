package com.rudraksha.apigateway.config;

import com.rudraksha.apigateway.filter.JwtValidationGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    // Local: "http://localhost:4000"
    // Docker: "http://patient-service:4000" (use container name)
    private static final String PATIENT_SERVICE_URI = "http://patient-service:4000";

    // Local: "http://localhost:4005"
    // Docker: "http://auth-service:4005" (use container name)
    private static final String AUTH_SERVICE_URI = "http://auth-service:4005";

    private final JwtValidationGatewayFilterFactory jwtValidationFilter;

    public GatewayConfig(JwtValidationGatewayFilterFactory jwtValidationFilter) {
        this.jwtValidationFilter = jwtValidationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ---------------- Auth Service ----------------------
                // API Gateway -> http://localhost:4004/auth/**
                // Auth Service -> http://auth-service:4005/**
                // ---------------- Auth Service ----------------------
                .route("auth-service-route", r -> r
                        .path("/auth/**")
                        .filters(f -> f.stripPrefix(1)) // removes /auth prefix
                        .uri(AUTH_SERVICE_URI))

                // ---------------- Patients API ----------------------
                // API Gateway -> http://localhost:4004/api/patients
                // Patient Service -> http://localhost:4000/patients
                .route("patient-service-route", r -> r
                        .path("/api/patients/**")
                        .filters(f -> f
                                .stripPrefix(1) // removes /api prefix
                                .filter(jwtValidationFilter.apply(new Object()))
                        )
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

                // ---------------- SWAGGER DOCS: Auth Service ----------------------
                // API Gateway -> http://localhost:4004/api-docs/auth
                // Auth Service -> http://auth-service:4005/v3/api-docs
                .route("api-docs-auth-route", r -> r
                        .path("/api-docs/auth")
                        .filters(f -> f.rewritePath(
                                "/api-docs/auth",
                                "/v3/api-docs"
                        ))
                        .uri(AUTH_SERVICE_URI))

                .build();
    }
}
