package org.ups.cafestock.catalog.domain.exception;

public class NombreDuplicadoException extends RuntimeException {

    public NombreDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
