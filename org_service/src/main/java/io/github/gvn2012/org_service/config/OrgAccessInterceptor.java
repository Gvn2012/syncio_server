package io.github.gvn2012.org_service.config;

import io.github.gvn2012.org_service.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class OrgAccessInterceptor implements HandlerInterceptor {

    @Override
    @SuppressWarnings("null")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String xUserId = request.getHeader("X-User-Id");

        // If the API Gateway didn't attach the header, assume it's an internal call
        // bypassing the gateway
        if (xUserId == null || xUserId.trim().isEmpty()) {
            return true;
        }

        // Allow admins even if X-User-Id doesn't match
        String xUserRoles = request.getHeader("X-User-Roles");
        if (xUserRoles != null && xUserRoles.contains("ADMIN")) {
            return true;
        }

        // We will need deeper permission checks per org later (e.g., querying
        // org_members or org owners).
        // For now, allow requests through to the service layer which must implement
        // ownership/membership checks.
        return true;
    }
}
