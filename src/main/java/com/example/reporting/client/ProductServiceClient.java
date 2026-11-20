package com.example.reporting.client;

import com.example.reporting.dto.ExternalProductDto;
import com.example.reporting.exception.ProductNotFoundException;
import com.example.reporting.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final RestClient productRestClient;

    public ExternalProductDto getProductById(Long productId, String bearerToken) {
        log.debug("Fetching product ID: {} from Product Service", productId);
        try {
            var request = productRestClient.get()
                    .uri("/products/{productId}", productId);

            if (bearerToken != null && !bearerToken.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, bearerToken);
            }

            return request.retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        log.warn("Product ID: {} not found in Product Service", productId);
                        throw new ProductNotFoundException(productId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Product Service returned 5xx server error: {}", res.getStatusCode());
                        throw new ServiceUnavailableException("Product Service", "Internal server error: " + res.getStatusCode());
                    })
                    .body(ExternalProductDto.class);
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Product Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Product Service", "Service is offline or request timed out");
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Product Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Product Service", e.getMessage());
        }
    }

    public List<ExternalProductDto> getAllProducts(String bearerToken) {
        log.debug("Fetching all products from Product Service");
        try {
            var request = productRestClient.get()
                    .uri("/products");

            if (bearerToken != null && !bearerToken.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, bearerToken);
            }

            List<ExternalProductDto> products = request.retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return products != null ? products : Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Product Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Product Service", "Service is offline or request timed out");
        } catch (Exception e) {
            log.error("Error fetching all products from Product Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

