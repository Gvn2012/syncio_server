package io.github.gvn2012.permission_service.dtos;

import org.springframework.http.HttpMethod;

public record RouteMappingRule(
        HttpMethod method,
        String pathPattern,
        String permissionCode,
        String targetIdVariable
) {}