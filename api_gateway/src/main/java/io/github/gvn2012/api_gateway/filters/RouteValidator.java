package io.github.gvn2012.api_gateway.filters;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    private static final List<String> OPEN_ROUTES = List.of(
            "/eureka/**",
            "/api/v1/users/login",
            "/api/v1/users/register",
            "/api/v1/users/emails/verify",
            "/**" // Bypass all routes for testing and dev env
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public final Predicate<ServerWebExchange> isSecured = exchange -> {
        String path = exchange.getRequest().getURI().getPath();
        return path != null && OPEN_ROUTES.stream()
                .noneMatch(uri -> pathMatcher.match(Objects.requireNonNull(uri), Objects.requireNonNull(path)));
    };
}
