package org.ups.cafestock.catalog.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;
import org.ups.cafestock.catalog.infrastructure.persistence.entity.ProductoJpaEntity;
import org.ups.cafestock.catalog.infrastructure.persistence.repository.ProductoJpaRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository productoJpaRepository;

    public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository) {
        this.productoJpaRepository = productoJpaRepository;
    }

    @Override
    public boolean existsByNombreNormalizadoYEstado(String nombreNormalizado, EstadoCatalogo estado) {
        return productoJpaRepository.existsByNombreNormalizadoAndEstado(nombreNormalizado, estado);
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoJpaEntity entity = new ProductoJpaEntity(
                producto.getId(),
                producto.getNombre(),
                producto.getNombre().trim().toLowerCase(Locale.ROOT),
                producto.getPrecio(),
                producto.getEstado(),
                producto.getCreadoEn(),
                producto.getActualizadoEn()
        );
        ProductoJpaEntity guardado = productoJpaRepository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public Optional<Producto> buscarPorId(UUID id) {
        return productoJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Producto> listar(EstadoCatalogo estado) {
        List<ProductoJpaEntity> entidades = estado == null
                ? productoJpaRepository.findAll()
                : productoJpaRepository.findByEstado(estado);
        return entidades.stream().map(this::aDominio).toList();
    }

    private Producto aDominio(ProductoJpaEntity entity) {
        return Producto.reconstruir(
                entity.getId(),
                entity.getNombre(),
                entity.getPrecio(),
                entity.getEstado(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
