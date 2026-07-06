package org.ups.cafestock.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "producto_precio_historico")
public class HistorialPrecioProductoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "registrado_en", nullable = false)
    private Instant registradoEn;

    protected HistorialPrecioProductoJpaEntity() {
        // requerido por JPA
    }

    public HistorialPrecioProductoJpaEntity(UUID id, UUID productoId, BigDecimal precio, Instant registradoEn) {
        this.id = id;
        this.productoId = productoId;
        this.precio = precio;
        this.registradoEn = registradoEn;
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
