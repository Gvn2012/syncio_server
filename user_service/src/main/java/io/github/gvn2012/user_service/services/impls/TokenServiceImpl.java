package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.services.interfaces.ITokenService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenServiceImpl implements ITokenService {

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    @Override
    public boolean matches(String rawToken, String storedHash) {
        String hashedInput = hashToken(rawToken);
        return MessageDigest.isEqual(
                hashedInput.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}