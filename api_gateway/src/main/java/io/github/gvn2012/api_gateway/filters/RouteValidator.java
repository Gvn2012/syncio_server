package io.github.gvn2012.api_gateway.filters;


import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    private static final List<String> OPEN_ROUTES = List.of(
            "/eureka",
            "/eureka/",
            "/eureka/web",
            "/"
    );


    public Predicate<ServerWebExchange> isSecured =
            exchange -> OPEN_ROUTES.stream()
                    .noneMatch(uri -> exchange.getRequest().getURI().getPath().startsWith(uri));
}
