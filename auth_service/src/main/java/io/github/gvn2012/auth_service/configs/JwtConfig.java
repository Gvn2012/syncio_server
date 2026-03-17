package io.github.gvn2012.auth_service.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Getter
    @Value( "${jwt.secret_key}")
    private String secretKey;

    @Getter
    @Value( "${jwt.access_token_duration}")
    private String expirationTime;

    @Getter
    @Value( "${jwt.refresh_token_duration}")
    private String refreshExpirationTime;

}
