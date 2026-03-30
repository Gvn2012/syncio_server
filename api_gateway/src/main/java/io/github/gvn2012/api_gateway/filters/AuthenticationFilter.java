package io.github.gvn2012.api_gateway.filters;

import io.github.gvn2012.api_gateway.dtos.APIResource;
import io.github.gvn2012.api_gateway.dtos.PermissionRequest;
import io.github.gvn2012.api_gateway.dtos.ValidateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final RouteValidator routeValidator;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public AuthenticationFilter(RouteValidator routeValidator, WebClient.Builder webClientBuilder) {
        this.routeValidator = routeValidator;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getURI().getPath();
        String httpMethod = exchange.getRequest().getMethod().name();

        log.debug("Incoming API Gateway request: [{}] {}", httpMethod, requestPath);

        if (!routeValidator.isSecured.test(exchange)) {
            log.debug("Route is public, bypassing authentication filter for: {}", requestPath);
            return chain.filter(exchange);
        }

        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Blocked request to secured route: [{}] {} - Missing Authorization header", httpMethod,
                    requestPath);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.debug("Authorization header found, verifying token via auth-service for: {}", requestPath);

        return webClientBuilder.build()
                .get()
                .uri("lb://auth-service/api/v1/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<APIResource<ValidateResponse>>() {
                })
                .doOnNext(apiResource -> log.debug("Token successfully validated. UserID: {}, Role: {}",
                        apiResource.getData().getUserId(), apiResource.getData().getUserRole()))
                .flatMap(apiResource -> {

                    var authData = apiResource.getData();
                    var permissionRequest = new PermissionRequest(
                            authData.getUserId(),
                            authData.getUserRole(),
                            requestPath,
                            httpMethod);

                    log.debug("Checking permissions via permission-service for UserID: {} to access [{}] {}",
                            authData.getUserId(), httpMethod, requestPath);

                    return webClientBuilder.build()
                            .post()
                            .uri("lb://permission-service/api/v1/permissions/authorize")
                            .bodyValue(permissionRequest)
                            .retrieve()
                            .toBodilessEntity()
                            .doOnSuccess(response -> log.info("Access GRANTED: User {} authorized for [{}] {}",
                                    authData.getUserId(), httpMethod, requestPath))
                            .flatMap(permissionResponse -> {
                                log.debug("Mutating request: Injecting X-User-Id header ({}) into downstream request",
                                        authData.getUserId());
                                var modifiedExchange = exchange.mutate()
                                        .request(r -> r.header("X-User-Id", authData.getUserId()))
                                        .build();
                                return chain.filter(modifiedExchange);
                            })
                            .onErrorResume(error -> {
                                log.warn("Access DENIED: Permission check failed for User {}. Reason: {}",
                                        authData.getUserId(), error.getMessage());
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            });
                })
                .onErrorResume(error -> {
                    log.error("Authentication check failed for [{}] {}. Reason: {}", httpMethod, requestPath,
                            error.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}