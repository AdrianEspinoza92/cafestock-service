package org.ups.cafestock.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Producto {

    private final UUID id;
    private String nombre;
    private BigDecimal precio;
    private EstadoCatalogo estado;
    private final Instant creadoEn;
    private Instant actualizadoEn;

    private Producto(UUID id, String nombre, BigDecimal precio, EstadoCatalogo estado,
                      Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static Producto crear(String nombre, BigDecimal precio) {
        Instant ahora = Instant.now();
        return new Producto(UUID.randomUUID(), nombre, precio, EstadoCatalogo.ACTIVO, ahora, ahora);
    }

    public static Producto reconstruir(UUID id, String nombre, BigDecimal precio, EstadoCatalogo estado,
                                        Instant creadoEn, Instant actualizadoEn) {
        return new Producto(id, nombre, precio, estado, creadoEn, actualizadoEn);
    }

    public void editar(String nuevoNombre, BigDecimal nuevoPrecio) {
        this.nombre = nuevoNombre;
        this.precio = nuevoPrecio;
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
