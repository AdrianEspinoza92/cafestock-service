package org.ups.cafestock.catalog.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.infrastructure.persistence.adapter.ProductoRepositoryAdapter;
import org.ups.cafestock.catalog.infrastructure.persistence.repository.ProductoJpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ProductoRepositoryAdapter.class)
class ProductoRepositoryAdapterIT {

    @Autowired
    private ProductoRepositoryAdapter productoRepositoryAdapter;

    @Autowired
    private ProductoJpaRepository productoJpaRepository;

    @Test
    void debePersistirYRecuperarUnProducto() {
        // given
        Producto producto = Producto.crear("Test Producto Persistencia", new BigDecimal("3.25"));

        // when
        Producto guardado = productoRepositoryAdapter.guardar(producto);
        Optional<Producto> recuperado = productoRepositoryAdapter.buscarPorId(guardado.getId());

        // then
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getNombre()).isEqualTo("Test Producto Persistencia");
        assertThat(recuperado.get().getPrecio()).isEqualByComparingTo("3.25");
        assertThat(recuperado.get().getEstado()).isEqualTo(EstadoCatalogo.ACTIVO);
    }

    @Test
    void debeDetectarExistenciaPorNombreNormalizadoYEstado() {
        // given
        productoRepositoryAdapter.guardar(Producto.crear("Test Producto Duplicado", new BigDecimal("1.00")));

        // when
        boolean existe = productoRepositoryAdapter.existsByNombreNormalizadoYEstado(
                "test producto duplicado", EstadoCatalogo.ACTIVO);

        // then
        assertThat(existe).isTrue();
    }

    @Test
    void debeRechazarPersistenciaDeNombreDuplicadoEnMismoEstado() {
        // given
        productoRepositoryAdapter.guardar(Producto.crear("Test Producto Constraint", new BigDecimal("1.00")));
        productoJpaRepository.flush();

        // when
        Producto duplicado = Producto.crear("Test Producto Constraint", new BigDecimal("2.00"));

        // then
        assertThatThrownBy(() -> {
            productoRepositoryAdapter.guardar(duplicado);
            productoJpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
