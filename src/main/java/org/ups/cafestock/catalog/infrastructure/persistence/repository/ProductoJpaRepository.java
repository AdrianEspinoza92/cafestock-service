package org.ups.cafestock.catalog.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.infrastructure.persistence.entity.ProductoJpaEntity;

import java.util.List;
import java.util.UUID;

public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, UUID> {

    boolean existsByNombreNormalizadoAndEstado(String nombreNormalizado, EstadoCatalogo estado);

    List<ProductoJpaEntity> findByEstado(EstadoCatalogo estado);
}
