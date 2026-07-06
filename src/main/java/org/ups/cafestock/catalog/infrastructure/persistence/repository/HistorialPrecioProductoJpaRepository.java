package org.ups.cafestock.catalog.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.cafestock.catalog.infrastructure.persistence.entity.HistorialPrecioProductoJpaEntity;

import java.util.List;
import java.util.UUID;

public interface HistorialPrecioProductoJpaRepository extends JpaRepository<HistorialPrecioProductoJpaEntity, UUID> {

    List<HistorialPrecioProductoJpaEntity> findByProductoIdOrderByRegistradoEnAsc(UUID productoId);
}
