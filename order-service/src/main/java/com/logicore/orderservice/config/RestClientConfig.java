package com.logicore.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Composition-root configuration for outbound HTTP calls (Product Service).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(@Value("${logicore.product-service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}