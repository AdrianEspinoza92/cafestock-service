package org.ups.cafestock.catalog.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;

import java.util.Locale;
import java.util.UUID;

@Service
public class CambiarEstadoInsumoUseCase {

    private final InsumoRepositoryPort insumoRepositoryPort;

    public CambiarEstadoInsumoUseCase(InsumoRepositoryPort insumoRepositoryPort) {
        this.insumoRepositoryPort = insumoRepositoryPort;
    }

    public Insumo activar(UUID id) {
        Insumo insumo = buscarOFallar(id);
        String nombreNormalizado = insumo.getNombre().toLowerCase(Locale.ROOT);
        if (insumoRepositoryPort.existsByNombreNormalizadoYEstado(nombreNormalizado, EstadoCatalogo.ACTIVO)) {
            throw new NombreDuplicadoException(
                    "Ya existe un insumo activo con el nombre '" + insumo.getNombre() + "'");
        }
        insumo.activar();
        return insumoRepositoryPort.guardar(insumo);
    }

    public Insumo desactivar(UUID id) {
        Insumo insumo = buscarOFallar(id);
        insumo.desactivar();
        return insumoRepositoryPort.guardar(insumo);
    }

    private Insumo buscarOFallar(UUID id) {
        return insumoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "No existe un insumo con el id '" + id + "'"));
    }
}
