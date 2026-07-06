package org.ups.cafestock.catalog.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;
import org.ups.cafestock.catalog.infrastructure.persistence.entity.InsumoJpaEntity;
import org.ups.cafestock.catalog.infrastructure.persistence.repository.InsumoJpaRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class InsumoRepositoryAdapter implements InsumoRepositoryPort {

    private final InsumoJpaRepository insumoJpaRepository;

    public InsumoRepositoryAdapter(InsumoJpaRepository insumoJpaRepository) {
        this.insumoJpaRepository = insumoJpaRepository;
    }

    @Override
    public boolean existsByNombreNormalizadoYEstado(String nombreNormalizado, EstadoCatalogo estado) {
        return insumoJpaRepository.existsByNombreNormalizadoAndEstado(nombreNormalizado, estado);
    }

    @Override
    public Insumo guardar(Insumo insumo) {
        InsumoJpaEntity entity = new InsumoJpaEntity(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getNombre().trim().toLowerCase(Locale.ROOT),
                insumo.getUnidadMedida(),
                insumo.getStockInicial(),
                insumo.getEstado(),
                insumo.getCreadoEn(),
                insumo.getActualizadoEn()
        );
        InsumoJpaEntity guardado = insumoJpaRepository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public Optional<Insumo> buscarPorId(UUID id) {
        return insumoJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Insumo> listar(EstadoCatalogo estado) {
        List<InsumoJpaEntity> entidades = estado == null
                ? insumoJpaRepository.findAll()
                : insumoJpaRepository.findByEstado(estado);
        return entidades.stream().map(this::aDominio).toList();
    }

    private Insumo aDominio(InsumoJpaEntity entity) {
        return Insumo.reconstruir(
                entity.getId(),
                entity.getNombre(),
                entity.getUnidadMedida(),
                entity.getStockInicial(),
                entity.getEstado(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
