package io.github.gvn2012.auth_service.services.impls;


import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import io.github.gvn2012.auth_service.configs.JwtConfig;
import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.services.interfaces.AuthServiceInterface;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService implements AuthServiceInterface {

    private final JwtConfig jwtConfig;
    private final Key key;

    public AuthService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
    }

    public String generateAccessToken(String username, UUID userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
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

    public Boolean isTokenExpired(String token){
        return getExpirationDateFromToken(token).before(new Date());
    }

    public Boolean validateToken(@NotNull String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        String raw = token.trim();
        if (raw.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            raw = raw.substring("Bearer ".length()).trim();
        }
        if (raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(raw)
                    .getBody();

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
            }

            return true;
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired", e);
        } catch (SecurityException e) { // bad signature / key
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token signature", e);
        } catch (MalformedJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed token", e);
        } catch (UnsupportedJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unsupported token", e);
        } catch (IllegalArgumentException e) { // parser edge cases
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e);
        } catch (JwtException e) { // any other JJWT validation failure
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e);
        }
    }

    public APIResource<GenerateLoginTokenResponse> generateLoginToken(GenerateLoginTokenRequest request) {
      try{
          String accessToken = generateAccessToken(request.getUsername(), UUID.fromString(request.getUserId()), "USER");
          String refreshToken = generateRefreshToken(request.getUsername(), UUID.fromString(request.getUserId()));
          return APIResource.ok("Tokens are generated successfully", new GenerateLoginTokenResponse(accessToken, refreshToken));
      } catch (Exception e) {
          return APIResource.error("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST, e.getMessage());
      }
    }
}
