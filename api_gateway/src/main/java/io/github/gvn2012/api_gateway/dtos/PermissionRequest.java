package io.github.gvn2012.api_gateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
public class PermissionRequest {
    private String userId;
    private String userRole;
    private String requestPath;
    private String httpMethod;
}
