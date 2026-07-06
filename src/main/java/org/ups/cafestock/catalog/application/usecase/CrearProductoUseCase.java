package org.ups.cafestock.catalog.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class CrearProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public CrearProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    public Producto ejecutar(String nombre, BigDecimal precio) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del producto no puede estar vacío");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("El precio del producto debe ser mayor que cero");
        }

        String nombreNormalizado = nombre.trim().toLowerCase(Locale.ROOT);
        if (productoRepositoryPort.existsByNombreNormalizadoYEstado(nombreNormalizado, EstadoCatalogo.ACTIVO)) {
            throw new NombreDuplicadoException(
                    "Ya existe un producto activo con el nombre '" + nombre.trim() + "'");
        }

        Producto producto = Producto.crear(nombre.trim(), precio);
        return productoRepositoryPort.guardar(producto);
    }
}
