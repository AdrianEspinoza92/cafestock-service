package org.ups.cafestock.catalog.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;

import java.util.Locale;
import java.util.UUID;

@Service
public class EditarInsumoUseCase {

    private final InsumoRepositoryPort insumoRepositoryPort;

    public EditarInsumoUseCase(InsumoRepositoryPort insumoRepositoryPort) {
        this.insumoRepositoryPort = insumoRepositoryPort;
    }

    public Insumo ejecutar(UUID id, String nombre, UnidadMedida unidadMedida) {
        Insumo insumo = insumoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "No existe un insumo con el id '" + id + "'"));

        String nuevoNombre = nombre != null ? nombre.trim() : insumo.getNombre();
        UnidadMedida nuevaUnidadMedida = unidadMedida != null ? unidadMedida : insumo.getUnidadMedida();

        if (nuevoNombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del insumo no puede estar vacío");
        }

        String nombreNormalizado = nuevoNombre.toLowerCase(Locale.ROOT);
        boolean nombreCambio = !nombreNormalizado.equals(insumo.getNombre().toLowerCase(Locale.ROOT));
        if (nombreCambio
                && insumo.getEstado() == EstadoCatalogo.ACTIVO
                && insumoRepositoryPort.existsByNombreNormalizadoYEstado(nombreNormalizado, EstadoCatalogo.ACTIVO)) {
            throw new NombreDuplicadoException(
                    "Ya existe un insumo activo con el nombre '" + nuevoNombre + "'");
        }

        insumo.editar(nuevoNombre, nuevaUnidadMedida);
        return insumoRepositoryPort.guardar(insumo);
    }
}
