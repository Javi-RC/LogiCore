package com.logicore.inventoryservice;

import com.logicore.inventoryservice.application.command.RegisterInventoryCommand;
import com.logicore.inventoryservice.application.command.ReserveStockCommand;
import com.logicore.inventoryservice.application.port.in.RegisterStockUseCase;
import com.logicore.inventoryservice.application.port.in.ReserveStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class InventoryConcurrencyIT {

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
    private ReserveStockUseCase reserveStockUseCase;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void concurrentReservationsApplyOnlyOnce() throws Exception {
        UUID productId = UUID.randomUUID();
        registerStockUseCase.registerStock(new RegisterInventoryCommand(ProductId.of(productId), 10));

        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Boolean>> tasks = List.of(
                () -> {
                    start.await();
                    reserveStockUseCase.reserve(
                            new ReserveStockCommand("order-a", ProductId.of(productId), 8));
                    return true;
                },
                () -> {
                    start.await();
                    reserveStockUseCase.reserve(
                            new ReserveStockCommand("order-b", ProductId.of(productId), 8));
                    return true;
                }
        );

        ExecutorService pool = Executors.newFixedThreadPool(2);
        int successes = 0;
        int failures = 0;
        start.countDown();
        List<Future<Boolean>> futures = pool.invokeAll(tasks);
        for (Future<Boolean> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
                successes++;
            } catch (ExecutionException ex) {
                failures++;
            }
        }
        pool.shutdownNow();

        assertThat(successes).isEqualTo(1);
        assertThat(failures).isEqualTo(1);

        InventoryItem item = inventoryRepository.findByProductId(ProductId.of(productId))
                .orElseThrow(() -> new AssertionError("item not found"));
        assertThat(item.availableQuantity()).isEqualTo(2);
        assertThat(item.reservedQuantity()).isEqualTo(8);
    }
}