package org.ups.cafestock.catalog.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;

import java.time.ZoneOffset;

@Component
public class InsumoMapper {

    public org.ups.cafestock.catalog.infrastructure.web.dto.Insumo aDto(Insumo insumo) {
        return new org.ups.cafestock.catalog.infrastructure.web.dto.Insumo(
                insumo.getId(),
                insumo.getNombre(),
                aDtoUnidadMedida(insumo.getUnidadMedida()),
                insumo.getStockInicial(),
                aDtoEstado(insumo.getEstado())
        )
                .creadoEn(insumo.getCreadoEn().atOffset(ZoneOffset.UTC))
                .actualizadoEn(insumo.getActualizadoEn().atOffset(ZoneOffset.UTC));
    }

    private org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo aDtoEstado(EstadoCatalogo estado) {
        return org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo.valueOf(estado.name());
    }

    private org.ups.cafestock.catalog.infrastructure.web.dto.UnidadMedida aDtoUnidadMedida(UnidadMedida unidadMedida) {
        return org.ups.cafestock.catalog.infrastructure.web.dto.UnidadMedida.valueOf(unidadMedida.name());
    }
}
