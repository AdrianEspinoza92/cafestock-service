package org.ups.cafestock.catalog.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class CrearInsumoUseCase {

    private final InsumoRepositoryPort insumoRepositoryPort;

    public CrearInsumoUseCase(InsumoRepositoryPort insumoRepositoryPort) {
        this.insumoRepositoryPort = insumoRepositoryPort;
    }

    public Insumo ejecutar(String nombre, UnidadMedida unidadMedida, BigDecimal stockInicial) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del insumo no puede estar vacío");
        }
        if (unidadMedida == null) {
            throw new ValorInvalidoException("La unidad de medida del insumo es obligatoria");
        }
        if (stockInicial == null || stockInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException("El stock inicial del insumo debe ser mayor o igual a cero");
        }

        String nombreNormalizado = nombre.trim().toLowerCase(Locale.ROOT);
        if (insumoRepositoryPort.existsByNombreNormalizadoYEstado(nombreNormalizado, EstadoCatalogo.ACTIVO)) {
            throw new NombreDuplicadoException(
                    "Ya existe un insumo activo con el nombre '" + nombre.trim() + "'");
        }

        Insumo insumo = Insumo.crear(nombre.trim(), unidadMedida, stockInicial);
        return insumoRepositoryPort.guardar(insumo);
    }
}
