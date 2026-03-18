package io.github.gvn2012.user_service.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.user_service.utils.JsonHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

    @Bean
    public JsonHelper jsonHelper(ObjectMapper objectMapper) {
        return new JsonHelper(objectMapper);
    }
}