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
import io.github.gvn2012.auth_service.dtos.requests.LogoutRequest;
import io.github.gvn2012.auth_service.dtos.requests.RefreshTokenRequest;
import io.github.gvn2012.auth_service.entities.UserSession;
import io.github.gvn2012.auth_service.repositories.UserSessionRepository;
import io.github.gvn2012.auth_service.services.interfaces.AuthServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class AuthService implements AuthServiceInterface {

    private final JwtConfig jwtConfig;
    private final Key key;

    private final PermissionClient permissionClient;
    private final UserSessionRepository userSessionRepository;

    public AuthService(JwtConfig jwtConfig, PermissionClient permissionClient,
            UserSessionRepository userSessionRepository) {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
        this.permissionClient = permissionClient;
        this.userSessionRepository = userSessionRepository;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null)
            return null;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private UserSession.UserSessionBuilder populateSessionDetails(UserSession.UserSessionBuilder builder,
            HttpServletRequest httpRequest) {
        if (httpRequest != null) {
            String ipAddress = getClientIp(httpRequest);
            builder.ipAddress(ipAddress != null && ipAddress.length() > 45 ? ipAddress.substring(0, 45) : ipAddress);

            String userAgentString = httpRequest.getHeader("User-Agent");
            if (userAgentString != null) {
                builder.userAgent(userAgentString.length() > 512 ? userAgentString.substring(0, 512) : userAgentString);

                String lowercaseUA = userAgentString.toLowerCase();
                if (lowercaseUA.contains("mobile") || lowercaseUA.contains("android")
                        || lowercaseUA.contains("iphone")) {
                    builder.deviceType("Mobile");
                } else if (lowercaseUA.contains("tablet") || lowercaseUA.contains("ipad")) {
                    builder.deviceType("Tablet");
                } else {
                    builder.deviceType("Desktop");
                }

                builder.deviceName(
                        userAgentString.length() > 128 ? userAgentString.substring(0, 128) : userAgentString);
            }
        }
        return builder;
    }

    public String generateAccessToken(String username, UUID userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
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
                .setId(UUID.randomUUID().toString())
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

    @Transactional
    public APIResource<GenerateLoginTokenResponse> generateLoginToken(GenerateLoginTokenRequest request,
            HttpServletRequest httpRequest) {
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

            UserSession.UserSessionBuilder sessionBuilder = UserSession.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.fromString(request.getUserId()))
                    .sessionTokenHash(hashToken(refreshToken))
                    .lastActiveAt(Instant.now())
                    .expiresAt(Instant.now().plusMillis(Long.parseLong(jwtConfig.getRefreshExpirationTime())))
                    .revoked(false);

            UserSession newSession = populateSessionDetails(sessionBuilder, httpRequest).build();
            userSessionRepository.save(newSession);

            return APIResource.ok("Tokens are generated successfully",
                    new GenerateLoginTokenResponse(accessToken, refreshToken, userRoles));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Database constraint violation during session creation: {}", e.getMessage(), e);
            return APIResource.error("CONFLICT", "Session already exists or constraint violation", HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token generation: {}", e.getMessage(), e);
            return APIResource.error("INTERNAL_SERVER_ERROR", "Failed to generate token: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public APIResource<GenerateLoginTokenResponse> refreshToken(RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = request.getRefreshToken();
            if (token == null || token.isBlank()) {
                return APIResource.error("BAD_REQUEST", "Refresh token is missing", HttpStatus.BAD_REQUEST, null);
            }
            if (isTokenExpired(token)) {
                return APIResource.error("UNAUTHORIZED", "Refresh token is expired", HttpStatus.UNAUTHORIZED, null);
            }

            Claims claims = getAllClaimsFromToken(token);
            String userId = claims.get("userId", String.class);
            String username = claims.getSubject();

            String hashedToken = hashToken(token);
            java.util.Optional<UserSession> sessionOpt = userSessionRepository
                    .findBySessionTokenHashAndRevokedFalse(hashedToken);

            if (sessionOpt.isEmpty()) {
                return APIResource.error("UNAUTHORIZED", "Session not found or revoked", HttpStatus.UNAUTHORIZED, null);
            }

            UserSession session = sessionOpt.get();
            if (session.getExpiresAt().isBefore(Instant.now())) {
                return APIResource.error("UNAUTHORIZED", "Session expired", HttpStatus.UNAUTHORIZED, null);
            }

            session.setRevoked(true);
            session.setRevokedAt(Instant.now());
            session.setRevokedReason("ROTATED");
            userSessionRepository.save(session);

            List<GetUserRoleResponse> userRoleResponse = permissionClient.getUserRole(userId).block();
            List<String> userRoles = userRoleResponse.stream()
                    .map(GetUserRoleResponse::getRoleName).toList();

            String newAccessToken = generateAccessToken(username, UUID.fromString(userId), userRoles);
            String newRefreshToken = generateRefreshToken(username, UUID.fromString(userId));

            UserSession.UserSessionBuilder sessionBuilder = UserSession.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.fromString(userId))
                    .sessionTokenHash(hashToken(newRefreshToken))
                    .lastActiveAt(Instant.now())
                    .expiresAt(Instant.now().plusMillis(Long.parseLong(jwtConfig.getRefreshExpirationTime())))
                    .revoked(false);

            UserSession newSession = populateSessionDetails(sessionBuilder, httpRequest).build();
            userSessionRepository.save(newSession);

            return APIResource.ok("Tokens rotated successfully",
                    new GenerateLoginTokenResponse(newAccessToken, newRefreshToken, userRoles));
        } catch (Exception e) {
            return APIResource.error("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Transactional
    public APIResource<String> logout(LogoutRequest request) {
        try {
            String token = request.getLogoutToken();
            if (token == null || token.isBlank())
                return APIResource.error("BAD_REQUEST", "Token missing", HttpStatus.BAD_REQUEST, null);
            String parsedToken = token;
            if (token.toLowerCase().startsWith("bearer ")) {
                parsedToken = token.substring(7).trim();
            }
            String hashedToken = hashToken(parsedToken);
            java.util.Optional<UserSession> sessionOpt = userSessionRepository
                    .findBySessionTokenHashAndRevokedFalse(hashedToken);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();
                session.setRevoked(true);
                session.setRevokedAt(Instant.now());
                session.setRevokedReason("USER_LOGOUT");
                userSessionRepository.save(session);
            }
            return APIResource.ok("Logged out successfully", "Logged out");
        } catch (Exception e) {
            return APIResource.error("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Transactional
    public APIResource<String> forceLogout(String userId) {
        try {
            List<UserSession> activeSessions = userSessionRepository
                    .findByUserIdAndRevokedFalse(UUID.fromString(userId));
            for (UserSession s : activeSessions) {
                s.setRevoked(true);
                s.setRevokedAt(Instant.now());
                s.setRevokedReason("FORCE_LOGOUT");
            }
            userSessionRepository.saveAll(activeSessions);
            return APIResource.ok("Force logged out successfully", "Force logged out");
        } catch (Exception e) {
            return APIResource.error("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }
}