package io.github.gvn2012.api_gateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean(name = "zipkinWebClientBuilder")
    public WebClient.Builder zipkinWebClientBuilder() {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(
                HttpClient.create(ConnectionProvider.builder("zipkin-connection-provider")
                        .maxIdleTime(Duration.ofSeconds(30))
                        .build())
        ));
    }
}
