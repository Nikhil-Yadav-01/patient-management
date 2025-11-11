package com.rudraksha.apigateway.controller;

import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GatewayRoutesController {

    private final RouteDefinitionLocator routeDefinitionLocator;

    public GatewayRoutesController(RouteDefinitionLocator routeDefinitionLocator) {
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @GetMapping("/api/routes")
    public Flux<Map<String, Object>> getRoutes() {
        return routeDefinitionLocator.getRouteDefinitions()
                .map(rd -> Map.of(
                        "id", rd.getId(),
                        "uri", rd.getUri().toString(),
                        "predicates", rd.getPredicates().stream()
                                .map(p -> Map.of(
                                        "name", p.getName(),
                                        "args", p.getArgs()
                                ))
                                .collect(Collectors.toList())
                ));
    }
}
