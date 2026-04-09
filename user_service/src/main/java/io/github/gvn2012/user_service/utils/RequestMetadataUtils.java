package io.github.gvn2012.user_service.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

public class RequestMetadataUtils {

    public static Map<String, Object> extractMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        HttpServletRequest request = getCurrentRequest();
        
        if (request != null) {
            metadata.put("ip_address", getClientIp(request));
            metadata.put("user_agent", request.getHeader("User-Agent"));
            metadata.put("referer", request.getHeader("Referer"));
            metadata.put("origin", request.getHeader("Origin"));
            metadata.put("platform", request.getHeader("X-Platform"));
        }
        
        return metadata;
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
