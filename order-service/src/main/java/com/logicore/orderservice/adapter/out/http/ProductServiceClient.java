package com.logicore.orderservice.adapter.out.http;

import com.logicore.orderservice.application.port.out.ProductClient;
import com.logicore.orderservice.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Outbound adapter implementing {@link ProductClient} by calling the Product Service over
 * synchronous REST. Used to validate the product and copy its current price onto the order.
 */
@Component
public class ProductServiceClient implements ProductClient {

    private final RestClient restClient;

    public ProductServiceClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    @Override
    public ProductInfo getProduct(UUID productId) {
        try {
            return restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (request, response) -> {
                        throw new ProductNotFoundException("Product " + productId + " not found");
                    })
                    .body(ProductInfo.class);
        } catch (RestClientException e) {
            throw new ProductNotFoundException("Product " + productId + " could not be retrieved from Product Service");
        }
    }
}