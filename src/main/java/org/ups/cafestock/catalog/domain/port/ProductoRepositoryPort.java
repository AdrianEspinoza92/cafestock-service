package org.ups.cafestock.catalog.domain.port;

import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepositoryPort {

    boolean existsByNombreNormalizadoYEstado(String nombreNormalizado, EstadoCatalogo estado);

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(UUID id);

    List<Producto> listar(EstadoCatalogo estado);
}
