package org.ups.cafestock.catalog.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.cafestock.catalog.domain.model.HistorialPrecioProducto;
import org.ups.cafestock.catalog.domain.port.HistorialPrecioProductoRepositoryPort;
import org.ups.cafestock.catalog.infrastructure.persistence.entity.HistorialPrecioProductoJpaEntity;
import org.ups.cafestock.catalog.infrastructure.persistence.repository.HistorialPrecioProductoJpaRepository;

import java.util.List;
import java.util.UUID;

@Component
public class HistorialPrecioProductoRepositoryAdapter implements HistorialPrecioProductoRepositoryPort {

    private final HistorialPrecioProductoJpaRepository historialPrecioProductoJpaRepository;

    public HistorialPrecioProductoRepositoryAdapter(
            HistorialPrecioProductoJpaRepository historialPrecioProductoJpaRepository) {
        this.historialPrecioProductoJpaRepository = historialPrecioProductoJpaRepository;
    }

    @Override
    public HistorialPrecioProducto guardar(HistorialPrecioProducto historial) {
        HistorialPrecioProductoJpaEntity entity = new HistorialPrecioProductoJpaEntity(
                historial.getId(),
                historial.getProductoId(),
                historial.getPrecio(),
                historial.getRegistradoEn()
        );
        HistorialPrecioProductoJpaEntity guardado = historialPrecioProductoJpaRepository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public List<HistorialPrecioProducto> listarPorProducto(UUID productoId) {
        return historialPrecioProductoJpaRepository.findByProductoIdOrderByRegistradoEnAsc(productoId).stream()
                .map(this::aDominio)
                .toList();
    }

    private HistorialPrecioProducto aDominio(HistorialPrecioProductoJpaEntity entity) {
        return HistorialPrecioProducto.reconstruir(
                entity.getId(), entity.getProductoId(), entity.getPrecio(), entity.getRegistradoEn());
    }
}
