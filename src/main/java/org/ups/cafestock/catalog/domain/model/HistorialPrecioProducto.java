package org.ups.cafestock.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Registro append-only del precio que tuvo un Producto hasta el instante en
 * que fue reemplazado por una edición. Existe únicamente para que FR-011/
 * SC-003 (una venta ya registrada conserva el precio con el que se vendió)
 * sea verificable dentro de esta historia sin requerir la entidad Venta
 * (fuera de alcance): cualquier precio que quede aquí queda inmutable ante
 * ediciones posteriores del Producto, igual que lo estaría el precio
 * copiado en un futuro registro de Venta.
 */
public class HistorialPrecioProducto {

    private final UUID id;
    private final UUID productoId;
    private final BigDecimal precio;
    private final Instant registradoEn;

    private HistorialPrecioProducto(UUID id, UUID productoId, BigDecimal precio, Instant registradoEn) {
        this.id = id;
        this.productoId = productoId;
        this.precio = precio;
        this.registradoEn = registradoEn;
    }

    public static HistorialPrecioProducto registrar(UUID productoId, BigDecimal precio) {
        return new HistorialPrecioProducto(UUID.randomUUID(), productoId, precio, Instant.now());
    }

    public static HistorialPrecioProducto reconstruir(UUID id, UUID productoId, BigDecimal precio, Instant registradoEn) {
        return new HistorialPrecioProducto(id, productoId, precio, registradoEn);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductoId() {
        return productoId;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Instant getRegistradoEn() {
        return registradoEn;
    }
}
