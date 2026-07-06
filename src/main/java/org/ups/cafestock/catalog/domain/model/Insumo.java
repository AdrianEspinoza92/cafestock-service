package org.ups.cafestock.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Insumo {

    private final UUID id;
    private String nombre;
    private UnidadMedida unidadMedida;
    private final BigDecimal stockInicial;
    private EstadoCatalogo estado;
    private final Instant creadoEn;
    private Instant actualizadoEn;

    private Insumo(UUID id, String nombre, UnidadMedida unidadMedida, BigDecimal stockInicial,
                   EstadoCatalogo estado, Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.stockInicial = stockInicial;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static Insumo crear(String nombre, UnidadMedida unidadMedida, BigDecimal stockInicial) {
        Instant ahora = Instant.now();
        return new Insumo(UUID.randomUUID(), nombre, unidadMedida, stockInicial, EstadoCatalogo.ACTIVO, ahora, ahora);
    }

    public static Insumo reconstruir(UUID id, String nombre, UnidadMedida unidadMedida, BigDecimal stockInicial,
                                      EstadoCatalogo estado, Instant creadoEn, Instant actualizadoEn) {
        return new Insumo(id, nombre, unidadMedida, stockInicial, estado, creadoEn, actualizadoEn);
    }

    public void editar(String nuevoNombre, UnidadMedida nuevaUnidadMedida) {
        this.nombre = nuevoNombre;
        this.unidadMedida = nuevaUnidadMedida;
        this.actualizadoEn = Instant.now();
    }

    public void activar() {
        this.estado = EstadoCatalogo.ACTIVO;
        this.actualizadoEn = Instant.now();
    }

    public void desactivar() {
        this.estado = EstadoCatalogo.INACTIVO;
        this.actualizadoEn = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
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
