package io.github.gvn2012.user_service.services.interfaces;

public interface ITokenService {

    String generateToken();

    String hashToken(String rawToken);

    boolean matches(String rawToken, String storedHash);
}
