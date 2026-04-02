package io.github.gvn2012.permission_service.services.impls;

import io.github.gvn2012.permission_service.dtos.RouteMappingRule;
import io.github.gvn2012.permission_service.services.interfaces.RoutePermissionRegistryInterface;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoutePermissionRegistryImpl implements RoutePermissionRegistryInterface {

    private final List<RouteMappingRule> rules = new ArrayList<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users", "user:profile:read", "id"));
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{uid}", "user:profile:read", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}", "user:profile:update", "uid"));

        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{uid}/addresses", "user:address:read", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{uid}/addresses", "user:address:create", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/addresses/{aid}", "user:address:update", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{uid}/addresses/{aid}", "user:address:delete", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/addresses/{aid}/set-primary", "user:address:set_primary", "uid"));

        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{uid}/phones", "user:phone:read", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{uid}/phones", "user:phone:create", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/phones/{phid}", "user:phone:update", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{uid}/phones/{phid}", "user:phone:delete", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/phones/{phid}/set-primary", "user:phone:set_primary", "uid"));

        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{uid}/emails", "user:email:read", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{uid}/emails", "user:email:create", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/emails/{eid}", "user:email:update", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{uid}/emails/{eid}", "user:email:delete", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/emails/{eid}/set-primary", "user:email:set_primary", "uid"));

        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{uid}/emergency-contacts", "user:contact:read", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{uid}/emergency-contacts", "user:contact:create", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/emergency-contacts/{cid}", "user:contact:update", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{uid}/emergency-contacts/{cid}", "user:contact:delete", "uid"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{uid}/emergency-contacts/{cid}/set-primary", "user:contact:set_primary", "uid"));

        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/posts", "post:create", null));
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/posts/{pid}", "post:read", "pid"));
        rules.add(new RouteMappingRule(HttpMethod.PUT, "/api/v1/posts/{pid}", "post:update", "pid"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/posts/{pid}", "post:delete", "pid"));

        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/orgs/{oid}/members", "group:manage_members", "oid"));
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/admin/**", "admin:access", null));
    }

    @SuppressWarnings("null")
    @Override
    public Optional<ResolvedRequest> resolve(String method, String pathWithQuery) {
        String path = pathWithQuery;
        String queryString = null;

        if (pathWithQuery.contains("?")) {
            int queryIndex = pathWithQuery.indexOf("?");
            path = pathWithQuery.substring(0, queryIndex);
            queryString = pathWithQuery.substring(queryIndex + 1);
        }

        for (var rule : rules) {
            if (rule.method().name().equalsIgnoreCase(method) && pathMatcher.match(rule.pathPattern(), path)) {
                String targetId = null;

                if (rule.targetIdVariable() != null) {
                    Map<String, String> variables = pathMatcher.extractUriTemplateVariables(rule.pathPattern(), path);
                    targetId = variables.get(rule.targetIdVariable());

                    if (targetId == null && queryString != null) {
                        targetId = extractQueryParam(queryString, rule.targetIdVariable());
                    }
                }

                return Optional.of(new ResolvedRequest(rule.permissionCode(), targetId));
            }
        }
        return Optional.empty();
    }

    private String extractQueryParam(String queryString, String paramName) {
        return java.util.Arrays.stream(queryString.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(parts -> parts.length == 2 && parts[0].equals(paramName))
                .map(parts -> parts[1])
                .findFirst()
                .orElse(null);
    }

    public record ResolvedRequest(String permissionCode, String targetId) {
    }
}