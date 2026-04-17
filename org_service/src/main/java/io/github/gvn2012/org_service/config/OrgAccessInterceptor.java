package io.github.gvn2012.org_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OrgAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String xUserId = request.getHeader("X-User-Id");

        if (xUserId == null || xUserId.trim().isEmpty()) {
            return true;
        }

        String xUserRoles = request.getHeader("X-User-Roles");
        if (xUserRoles != null && xUserRoles.contains("ADMIN")) {
            return true;
        }
        return true;
    }
}
