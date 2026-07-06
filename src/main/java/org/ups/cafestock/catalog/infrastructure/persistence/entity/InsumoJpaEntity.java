package org.ups.cafestock.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "insumo")
public class InsumoJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "nombre_normalizado", nullable = false)
    private String nombreNormalizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "stock_inicial", nullable = false)
    private BigDecimal stockInicial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCatalogo estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected InsumoJpaEntity() {
        // requerido por JPA
    }

    public InsumoJpaEntity(UUID id, String nombre, String nombreNormalizado, UnidadMedida unidadMedida,
                            BigDecimal stockInicial, EstadoCatalogo estado, Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.nombreNormalizado = nombreNormalizado;
        this.unidadMedida = unidadMedida;
        this.stockInicial = stockInicial;
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

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public BigDecimal getStockInicial() {
        return stockInicial;
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
