package org.ups.cafestock.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "producto")
public class ProductoJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "nombre_normalizado", nullable = false)
    private String nombreNormalizado;

    @Column(nullable = false)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCatalogo estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected ProductoJpaEntity() {
        // requerido por JPA
    }

    public ProductoJpaEntity(UUID id, String nombre, String nombreNormalizado, BigDecimal precio,
                              EstadoCatalogo estado, Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.nombreNormalizado = nombreNormalizado;
        this.precio = precio;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreNormalizado() {
        return nombreNormalizado;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public EstadoCatalogo getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
