package org.ups.cafestock.catalog.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;

import java.time.ZoneOffset;

@Component
public class ProductoMapper {

    public org.ups.cafestock.catalog.infrastructure.web.dto.Producto aDto(Producto producto) {
        return new org.ups.cafestock.catalog.infrastructure.web.dto.Producto(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                aDtoEstado(producto.getEstado())
        )
                .creadoEn(producto.getCreadoEn().atOffset(ZoneOffset.UTC))
                .actualizadoEn(producto.getActualizadoEn().atOffset(ZoneOffset.UTC));
    }

    private org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo aDtoEstado(EstadoCatalogo estado) {
        return org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo.valueOf(estado.name());
    }
}
