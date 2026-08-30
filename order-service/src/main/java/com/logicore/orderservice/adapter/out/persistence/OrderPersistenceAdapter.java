package com.logicore.orderservice.adapter.out.persistence;

import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound adapter implementing {@link OrderRepository} backed by Spring Data JPA.
 *
 * <p>All read queries that need items use the {@code *WithItems} repository methods to
 * prevent the N+1 problem (see {@link OrderJpaRepository}) and map entities back to domain.</p>
 */
@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(order)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findByIdWithItems(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerIdWithItems(customerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaRepository.findAllWithItems().stream().map(mapper::toDomain).toList();
    }
}