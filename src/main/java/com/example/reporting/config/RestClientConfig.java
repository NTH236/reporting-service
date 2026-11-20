package com.example.reporting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${services.auth.base-url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.auth.connect-timeout-ms:5000}")
    private int authConnectTimeout;

    @Value("${services.auth.read-timeout-ms:5000}")
    private int authReadTimeout;

    @Value("${services.product.base-url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${services.product.connect-timeout-ms:5000}")
    private int productConnectTimeout;

    @Value("${services.product.read-timeout-ms:5000}")
    private int productReadTimeout;

    @Value("${services.order.base-url:http://localhost:8083}")
    private String orderServiceUrl;

    @Value("${services.order.connect-timeout-ms:5000}")
    private int orderConnectTimeout;

    @Value("${services.order.read-timeout-ms:5000}")
    private int orderReadTimeout;

    private SimpleClientHttpRequestFactory createRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return factory;
    }

    @Bean
    public RestClient authRestClient() {
        return RestClient.builder()
                .baseUrl(authServiceUrl)
                .requestFactory(createRequestFactory(authConnectTimeout, authReadTimeout))
                .build();
    }

    @Bean
    public RestClient productRestClient() {
        return RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(createRequestFactory(productConnectTimeout, productReadTimeout))
                .build();
    }

    @Bean
    public RestClient orderRestClient() {
        return RestClient.builder()
                .baseUrl(orderServiceUrl)
                .requestFactory(createRequestFactory(orderConnectTimeout, orderReadTimeout))
                .build();
    }
}

