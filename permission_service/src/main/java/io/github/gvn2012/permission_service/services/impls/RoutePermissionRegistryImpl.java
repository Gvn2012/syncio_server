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
        // --- USERS ---
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}", "user:read", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.PUT, "/api/v1/users/{userId}", "user:update", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/users/{userId}/phone", "user:phone:read", "userId"));
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/users/{userId}/ban", "user:ban", "userId"));

        // --- POSTS ---
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/posts", "post:create", null)); // No target ID needed for create
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/posts/{postId}", "post:read", "postId"));
        rules.add(new RouteMappingRule(HttpMethod.PUT, "/api/v1/posts/{postId}", "post:update", "postId"));
        rules.add(new RouteMappingRule(HttpMethod.DELETE, "/api/v1/posts/{postId}", "post:delete", "postId"));

        // --- GROUPS ---
        rules.add(new RouteMappingRule(HttpMethod.POST, "/api/v1/groups/{groupId}/members", "group:manage_members", "groupId"));

        // Add a fallback catch-all for admins (Optional)
        rules.add(new RouteMappingRule(HttpMethod.GET, "/api/v1/admin/**", "admin:access", null));
    }

    public Optional<ResolvedRequest> resolve(String method, String path) {
        for (var rule : rules) {
            // Check if HTTP Method matches AND the Path Pattern matches
            if (rule.method().name().equalsIgnoreCase(method) && pathMatcher.match(rule.pathPattern(), path)) {

                String targetId = null;

                // If the rule specifies a variable name (like "userId"), extract it from the path!
                if (rule.targetIdVariable() != null) {
                    Map<String, String> variables = pathMatcher.extractUriTemplateVariables(rule.pathPattern(), path);
                    targetId = variables.get(rule.targetIdVariable());
                }

                return Optional.of(new ResolvedRequest(rule.permissionCode(), targetId));
            }
        }
        return Optional.empty();
    }

    // A small DTO to hold the result
    public record ResolvedRequest(String permissionCode, String targetId) {}
}