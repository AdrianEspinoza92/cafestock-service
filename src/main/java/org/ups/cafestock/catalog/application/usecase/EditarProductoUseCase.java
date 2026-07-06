package org.ups.cafestock.catalog.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Service
public class EditarProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public EditarProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    public Producto ejecutar(UUID id, String nombre, BigDecimal precio) {
        Producto producto = productoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "No existe un producto con el id '" + id + "'"));

        String nuevoNombre = nombre != null ? nombre.trim() : producto.getNombre();
        BigDecimal nuevoPrecio = precio != null ? precio : producto.getPrecio();

        if (nuevoNombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del producto no puede estar vacío");
        }
        if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("El precio del producto debe ser mayor que cero");
        }

        String nombreNormalizado = nuevoNombre.toLowerCase(Locale.ROOT);
        boolean nombreCambio = !nombreNormalizado.equals(producto.getNombre().toLowerCase(Locale.ROOT));
        if (nombreCambio
                && producto.getEstado() == EstadoCatalogo.ACTIVO
                && productoRepositoryPort.existsByNombreNormalizadoYEstado(nombreNormalizado, EstadoCatalogo.ACTIVO)) {
            throw new NombreDuplicadoException(
                    "Ya existe un producto activo con el nombre '" + nuevoNombre + "'");
        }

        producto.editar(nuevoNombre, nuevoPrecio);
        return productoRepositoryPort.guardar(producto);
    }
}
