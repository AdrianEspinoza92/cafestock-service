package org.ups.cafestock.catalog.domain.exception;

public class RegistroNoEncontradoException extends RuntimeException {

    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
