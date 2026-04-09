package io.github.gvn2012.auth_service.services.impls;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.github.gvn2012.auth_service.clients.PermissionClient;
import io.github.gvn2012.auth_service.configs.JwtConfig;
import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.auth_service.dtos.responses.ValidateResponse;
import io.github.gvn2012.auth_service.services.interfaces.AuthServiceInterface;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService implements AuthServiceInterface {

    private final JwtConfig jwtConfig;
    private final Key key;

    private final PermissionClient permissionClient;

    public AuthService(JwtConfig jwtConfig, PermissionClient permissionClient) {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
        this.permissionClient = permissionClient;
    }

    public String generateAccessToken(String username, UUID userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("userId", userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(Long.parseLong(jwtConfig.getExpirationTime()))))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username, UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("tokenType", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(Long.parseLong(jwtConfig.getRefreshExpirationTime()))))
                .signWith(key)
                .compact();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getTokenTypeFromToken(String token) {
        return getAllClaimsFromToken(token).get("tokenType", String.class);
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public ValidateResponse validateToken(@NotNull String token) {
        if (token == null || token.isBlank()) {
            return new ValidateResponse(false, "Missing token", null, null);
        }

        String raw = token.split(",")[0].trim();
        raw = raw.replace("\"", "").replace("'", "");

        if (raw.toLowerCase().startsWith("bearer ")) {
            raw = raw.substring(7).trim();
        }

        if (raw.isBlank()) {
            return new ValidateResponse(false, "Missing token", null, null);
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(raw)
                    .getBody();

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                return new ValidateResponse(false, "Invalid token subject", null, null);
            }

            String userId = claims.get("userId", String.class);
            if (userId == null || userId.isBlank()) {
                return new ValidateResponse(false, "Token is missing userId claim", null, null);
            }

            List<String> roles = (List<String>) claims.get("roles");
            if (roles == null || roles.isEmpty()) {
                return new ValidateResponse(false, "Token is missing roles claim", null, null);
            }

            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                return new ValidateResponse(false, "Token expired", null, null);
            }

            log.info("Token valid: subject={}, userId={}, roles={}", subject, userId, roles);

            return new ValidateResponse(true, null, userId, roles);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return new ValidateResponse(false, "Invalid or expired token", null, null);
        }
    }

    public APIResource<GenerateLoginTokenResponse> generateLoginToken(GenerateLoginTokenRequest request) {
        try {
            List<GetUserRoleResponse> userRoleResponse = permissionClient.getUserRole(request.getUserId()).block();

            if (userRoleResponse == null || userRoleResponse.isEmpty()) {
                return APIResource.error("INTERNAL_SERVER_ERROR", "Permission service is not working",
                        HttpStatus.INTERNAL_SERVER_ERROR, null);
            }

            List<String> userRoles = userRoleResponse.stream()
                    .map(GetUserRoleResponse::getRoleName)
                    .toList();

            String accessToken = generateAccessToken(request.getUsername(), UUID.fromString(request.getUserId()),
                    userRoles);
            String refreshToken = generateRefreshToken(request.getUsername(), UUID.fromString(request.getUserId()));
            return APIResource.ok("Tokens are generated successfully",
                    new GenerateLoginTokenResponse(accessToken, refreshToken, userRoles));
        } catch (Exception e) {
            return APIResource.error("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}