package com.logicore.orderservice.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository over {@link OrderJpaEntity}.
 *
 * <p><b>N+1 problem:</b> loading an order and only lazily initializing its items would
 * trigger one extra SELECT per order to fetch items (N+1). This is prevented using
 * {@link EntityGraph} so that items are fetched eagerly in a single query via a LEFT JOIN,
 * only for the queries that actually need the items.</p>
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    @Query("select o from OrderJpaEntity o where o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(UUID id);

    @EntityGraph(attributePaths = "items")
    @Query("select o from OrderJpaEntity o where o.customerId = :customerId")
    List<OrderJpaEntity> findByCustomerIdWithItems(UUID customerId);

    @EntityGraph(attributePaths = "items")
    @Query("select o from OrderJpaEntity o")
    List<OrderJpaEntity> findAllWithItems();
}