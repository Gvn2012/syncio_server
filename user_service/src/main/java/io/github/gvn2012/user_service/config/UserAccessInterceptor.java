package io.github.gvn2012.user_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import io.github.gvn2012.user_service.exceptions.ForbiddenException;

import java.util.Map;

@Component
public class UserAccessInterceptor implements HandlerInterceptor {

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String xUserId = request.getHeader("X-User-Id");
        
        // If the API Gateway didn't attach the header, assume it's an internal call bypassing the gateway
        if (xUserId == null || xUserId.trim().isEmpty()) {
            return true;
        }

        // Extract the {userId} path variable
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null && pathVariables.containsKey("userId")) {
            String pathUserId = pathVariables.get("userId");
            
            if (!xUserId.equals(pathUserId)) {
                
                // Allow admins even if X-User-Id doesn't match
                String xUserRoles = request.getHeader("X-User-Roles");
                if (xUserRoles != null && xUserRoles.contains("ADMIN")) {
                    return true;
                }
                
                throw new ForbiddenException("You do not have permission to modify or access this user's resource.");
            }
        }

        return true;
    }
}
