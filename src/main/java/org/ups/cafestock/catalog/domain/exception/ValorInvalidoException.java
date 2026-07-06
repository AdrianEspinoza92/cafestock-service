package org.ups.cafestock.catalog.domain.exception;

public class ValorInvalidoException extends RuntimeException {

    public ValorInvalidoException(String mensaje) {
        super(mensaje);
    }
}
