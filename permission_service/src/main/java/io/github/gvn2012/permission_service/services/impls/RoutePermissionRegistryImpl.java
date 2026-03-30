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
        // ================= USER SERVICE ENDPOINTS =================

        // --- Profile ---
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users", "user:profile:read", "id"));
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}", "user:profile:read", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}", "user:profile:update", "userId"));

        // --- Addresses ---
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}/addresses", "user:address:read",
                "userId"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{userId}/addresses", "user:address:create",
                "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/addresses/{addressId}",
                "user:address:update", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{userId}/addresses/{addressId}",
                "user:address:delete", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/addresses/{addressId}/set-primary",
                "user:address:set_primary", "userId"));

        // --- Phones ---
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}/phones", "user:phone:read", "userId"));
        rules.add(
                new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{userId}/phones", "user:phone:create", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/phones/{phoneId}", "user:phone:update",
                "userId"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{userId}/phones/{phoneId}",
                "user:phone:delete", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/phones/{phoneId}/set-primary",
                "user:phone:set_primary", "userId"));

        // --- Emails ---
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}/emails", "user:email:read", "userId"));
        rules.add(
                new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{userId}/emails", "user:email:create", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/emails/{emailId}", "user:email:update",
                "userId"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{userId}/emails/{emailId}",
                "user:email:delete", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/emails/{emailId}/set-primary",
                "user:email:set_primary", "userId"));

        // --- Emergency Contacts ---
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}/emergency-contacts", "user:contact:read",
                "userId"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{userId}/emergency-contacts",
                "user:contact:create", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH, "/api/v1/users/{userId}/emergency-contacts/{contactId}",
                "user:contact:update", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/users/{userId}/emergency-contacts/{contactId}",
                "user:contact:delete", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PATCH,
                "/api/v1/users/{userId}/emergency-contacts/{contactId}/set-primary", "user:contact:set_primary",
                "userId"));

        // ================= OTHER SERVICES (Examples) =================
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/posts", "post:create", null));
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/posts/{postId}", "post:read", "postId"));
        rules.add(new RouteMappingRule(HttpMethod.PUT, "/api/v1/posts/{postId}", "post:update", "postId"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/posts/{postId}", "post:delete", "postId"));

        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/groups/{groupId}/members", "group:manage_members",
                "groupId"));
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
                    // 1. Try path variables
                    Map<String, String> variables = pathMatcher.extractUriTemplateVariables(rule.pathPattern(), path);
                    targetId = variables.get(rule.targetIdVariable());

                    // 2. If not found in path, try query string
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