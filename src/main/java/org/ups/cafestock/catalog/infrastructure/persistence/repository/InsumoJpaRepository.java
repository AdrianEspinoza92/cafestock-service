package org.ups.cafestock.catalog.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.infrastructure.persistence.entity.InsumoJpaEntity;

import java.util.List;
import java.util.UUID;

public interface InsumoJpaRepository extends JpaRepository<InsumoJpaEntity, UUID> {

    boolean existsByNombreNormalizadoAndEstado(String nombreNormalizado, EstadoCatalogo estado);

    List<InsumoJpaEntity> findByEstado(EstadoCatalogo estado);
}
