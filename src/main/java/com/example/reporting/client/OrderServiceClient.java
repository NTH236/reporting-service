package com.example.reporting.client;

import com.example.reporting.dto.ExternalOrderDto;
import com.example.reporting.exception.OrderNotFoundException;
import com.example.reporting.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
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
public class OrderServiceClient {

    private final RestClient orderRestClient;

    public ExternalOrderDto getOrderById(Long orderId, String bearerToken) {
        log.debug("Fetching order ID: {} from Order Service", orderId);
        try {
            var request = orderRestClient.get()
                    .uri("/orders/{orderId}", orderId);

            if (bearerToken != null && !bearerToken.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, bearerToken);
            }

            return request.retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        log.warn("Order ID: {} not found in Order Service", orderId);
                        throw new OrderNotFoundException(orderId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Order Service returned 5xx server error: {}", res.getStatusCode());
                        throw new ServiceUnavailableException("Order Service", "Internal server error: " + res.getStatusCode());
                    })
                    .body(ExternalOrderDto.class);
        } catch (OrderNotFoundException e) {
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            throw new OrderNotFoundException(orderId);
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Order Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Order Service", "Service is offline or request timed out");
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Order Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Order Service", e.getMessage());
        }
    }

    public List<ExternalOrderDto> getAllOrders(String bearerToken) {
        log.debug("Fetching all orders from Order Service");
        try {
            var request = orderRestClient.get()
                    .uri("/orders");

            if (bearerToken != null && !bearerToken.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, bearerToken);
            }

            List<ExternalOrderDto> orders = request.retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return orders != null ? orders : Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Order Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Order Service", "Service is offline or request timed out");
        } catch (Exception e) {
            log.error("Error fetching all orders from Order Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

