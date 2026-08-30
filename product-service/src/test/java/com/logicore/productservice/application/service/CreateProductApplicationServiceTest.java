package com.logicore.productservice.application.service;

import com.logicore.productservice.application.command.CreateProductCommand;
import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.application.port.out.ProductRepository;
import com.logicore.productservice.domain.exception.SkuAlreadyExistsException;
import com.logicore.productservice.domain.model.Product;
import com.logicore.productservice.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductApplicationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CreateProductApplicationService service;

    @Test
    void createsProductWithUppercasedSku() {
        when(productRepository.findBySku(any())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.create(
                new CreateProductCommand("abc-1", "Widget", "desc", new BigDecimal("5.00")));

        assertThat(response.sku()).isEqualTo("ABC-1");
        assertThat(response.price()).isEqualByComparingTo("5.00");
        assertThat(response.active()).isTrue();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().sku().value()).isEqualTo("ABC-1");
    }

    @Test
    void rejectsDuplicateSku() {
        when(productRepository.findBySku(any())).thenReturn(Optional.of(
                Product.create(ProductId.newId(), com.logicore.productservice.domain.model.Sku.of("ABC-1"),
                        "x", null, com.logicore.productservice.domain.model.Money.of(new BigDecimal("1.00")))));

        assertThatThrownBy(() -> service.create(
                new CreateProductCommand("abc-1", "Widget", null, new BigDecimal("5.00"))))
                .isInstanceOf(SkuAlreadyExistsException.class);
    }
}
