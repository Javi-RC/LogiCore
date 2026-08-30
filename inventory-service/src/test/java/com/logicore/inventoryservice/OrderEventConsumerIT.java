package com.logicore.inventoryservice;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.inventoryservice.application.command.RegisterInventoryCommand;
import com.logicore.inventoryservice.application.port.in.RegisterStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class OrderEventConsumerIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("inventory_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private RegisterStockUseCase registerStockUseCase;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void consumesOrderCreatedAndReservesStock() throws Exception {
        UUID productId = UUID.randomUUID();
        registerStockUseCase.registerStock(new RegisterInventoryCommand(ProductId.of(productId), 10));

        UUID orderId = UUID.randomUUID();
        OrderEventPayload payload = new OrderEventPayload(
                orderId,
                UUID.randomUUID(),
                "PENDING",
                List.of(new OrderEventPayload.OrderItemPayload(productId, 3, "12.50")));
        kafkaTemplate.send(EventTypes.TOPIC_ORDER,
                DomainEvent.of(EventTypes.ORDER_CREATED, orderId, payload)).get(30, TimeUnit.SECONDS);

        Instant deadline = Instant.now().plus(Duration.ofSeconds(45));
        InventoryItem item = null;
        while (Instant.now().isBefore(deadline)) {
            item = inventoryRepository.findByProductId(ProductId.of(productId)).orElse(null);
            if (item != null && item.availableQuantity() == 7) {
                break;
            }
            Thread.sleep(500);
        }

        assertThat(item).isNotNull();
        assertThat(item.availableQuantity()).isEqualTo(7);
        assertThat(item.reservedQuantity()).isEqualTo(3);
    }
}