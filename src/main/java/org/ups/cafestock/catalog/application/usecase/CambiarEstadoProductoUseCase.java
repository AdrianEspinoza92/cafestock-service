package org.ups.cafestock.catalog.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;

import java.util.Locale;
import java.util.UUID;

@Service
public class CambiarEstadoProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public CambiarEstadoProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    public Producto activar(UUID id) {
        Producto producto = buscarOFallar(id);
        String nombreNormalizado = producto.getNombre().toLowerCase(Locale.ROOT);
        if (productoRepositoryPort.existsByNombreNormalizadoYEstado(nombreNormalizado, EstadoCatalogo.ACTIVO)) {
            throw new NombreDuplicadoException(
                    "Ya existe un producto activo con el nombre '" + producto.getNombre() + "'");
        }
        producto.activar();
        return productoRepositoryPort.guardar(producto);
    }

    public Producto desactivar(UUID id) {
        Producto producto = buscarOFallar(id);
        producto.desactivar();
        return productoRepositoryPort.guardar(producto);
    }

    private Producto buscarOFallar(UUID id) {
        return productoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "No existe un producto con el id '" + id + "'"));
    }
}
