package org.ups.cafestock.catalog.domain.port;

import org.ups.cafestock.catalog.domain.model.HistorialPrecioProducto;

import java.util.List;
import java.util.UUID;

public interface HistorialPrecioProductoRepositoryPort {

    HistorialPrecioProducto guardar(HistorialPrecioProducto historial);

    List<HistorialPrecioProducto> listarPorProducto(UUID productoId);
}
