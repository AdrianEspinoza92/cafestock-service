package org.ups.cafestock.catalog.domain.port;

import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsumoRepositoryPort {

    boolean existsByNombreNormalizadoYEstado(String nombreNormalizado, EstadoCatalogo estado);

    Insumo guardar(Insumo insumo);

    Optional<Insumo> buscarPorId(UUID id);

    List<Insumo> listar(EstadoCatalogo estado);
}
