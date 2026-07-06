package org.ups.cafestock.catalog.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.infrastructure.web.dto.Error;

@RestControllerAdvice
public class CatalogExceptionHandler {

    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<Error> manejarValorInvalido(ValorInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error("VALOR_INVALIDO", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> manejarValidacionBean(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Datos de entrada inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error("VALOR_INVALIDO", mensaje));
    }

    @ExceptionHandler(NombreDuplicadoException.class)
    public ResponseEntity<Error> manejarNombreDuplicado(NombreDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Error("NOMBRE_DUPLICADO", ex.getMessage()));
    }

    @ExceptionHandler(RegistroNoEncontradoException.class)
    public ResponseEntity<Error> manejarRegistroNoEncontrado(RegistroNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Error("NO_ENCONTRADO", ex.getMessage()));
    }
}
