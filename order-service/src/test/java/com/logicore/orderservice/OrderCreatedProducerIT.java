package com.logicore.orderservice;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.orderservice.application.command.CreateOrderCommand;
import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.in.CreateOrderUseCase;
import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.application.port.out.ProductClient;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class OrderCreatedProducerIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("order_db")
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
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private ProductClient productClient;

    @Test
    void createdOrderIsPersistedAndOrderCreatedEventIsPublished() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productClient.getProduct(any(UUID.class)))
                .thenReturn(new ProductClient.ProductInfo(productId, "SKU-WIDGET", "Widget", new BigDecimal("12.50")));

        try (KafkaConsumer<String, DomainEvent<?>> consumer = newConsumer()) {
            consumer.subscribe(List.of(EventTypes.TOPIC_ORDER));

            OrderResponse created = createOrderUseCase.createOrder(new CreateOrderCommand(
                    UUID.randomUUID(),
                    List.of(new CreateOrderCommand.Item(productId, 2))));

            DomainEvent<?> captured = pollForOrderCreated(consumer, 45_000L);
            assertThat(captured).isNotNull();
            assertThat(captured.eventType()).isEqualTo(EventTypes.ORDER_CREATED);
            assertThat(captured.correlationId()).isEqualTo(created.id());

            Order saved = orderRepository.findById(OrderId.of(created.id())).orElseThrow();
            assertThat(saved.items()).hasSize(1);
        }
    }

    private DomainEvent<?> pollForOrderCreated(KafkaConsumer<String, DomainEvent<?>> consumer, long timeoutMillis) {
        Instant deadline = Instant.now().plusMillis(timeoutMillis);
        while (Instant.now().isBefore(deadline)) {
            ConsumerRecords<String, DomainEvent<?>> records = consumer.poll(Duration.ofMillis(500));
            if (!records.isEmpty()) {
                return records.iterator().next().value();
            }
        }
        return null;
    }

    private KafkaConsumer<String, DomainEvent<?>> newConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-it-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put("spring.json.use.type.headers", false);
        props.put("spring.json.value.default.type", DomainEvent.class.getName());
        props.put("spring.json.trusted.packages", "com.logicore.common");
        return new KafkaConsumer<>(props);
    }
}