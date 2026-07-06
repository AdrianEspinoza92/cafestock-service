package org.ups.cafestock.catalog.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.cafestock.catalog.application.usecase.CambiarEstadoInsumoUseCase;
import org.ups.cafestock.catalog.application.usecase.CrearInsumoUseCase;
import org.ups.cafestock.catalog.application.usecase.EditarInsumoUseCase;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;
import org.ups.cafestock.catalog.infrastructure.web.api.InsumosApi;
import org.ups.cafestock.catalog.infrastructure.web.dto.CrearInsumoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.EditarInsumoRequest;
import org.ups.cafestock.catalog.infrastructure.web.mapper.InsumoMapper;

import java.util.List;
import java.util.UUID;

@RestController
public class InsumoController implements InsumosApi {

    private final CrearInsumoUseCase crearInsumoUseCase;
    private final EditarInsumoUseCase editarInsumoUseCase;
    private final CambiarEstadoInsumoUseCase cambiarEstadoInsumoUseCase;
    private final InsumoRepositoryPort insumoRepositoryPort;
    private final InsumoMapper insumoMapper;

    public InsumoController(CrearInsumoUseCase crearInsumoUseCase,
                             EditarInsumoUseCase editarInsumoUseCase,
                             CambiarEstadoInsumoUseCase cambiarEstadoInsumoUseCase,
                             InsumoRepositoryPort insumoRepositoryPort,
                             InsumoMapper insumoMapper) {
        this.crearInsumoUseCase = crearInsumoUseCase;
        this.editarInsumoUseCase = editarInsumoUseCase;
        this.cambiarEstadoInsumoUseCase = cambiarEstadoInsumoUseCase;
        this.insumoRepositoryPort = insumoRepositoryPort;
        this.insumoMapper = insumoMapper;
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo> crearInsumo(
            CrearInsumoRequest crearInsumoRequest) {
        UnidadMedida unidadMedida = crearInsumoRequest.getUnidadMedida() == null
                ? null : UnidadMedida.valueOf(crearInsumoRequest.getUnidadMedida().name());
        Insumo insumo = crearInsumoUseCase.ejecutar(
                crearInsumoRequest.getNombre(), unidadMedida, crearInsumoRequest.getStockInicial());
        return ResponseEntity.status(HttpStatus.CREATED).body(insumoMapper.aDto(insumo));
    }

    @Override
    public ResponseEntity<List<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo>> listarInsumos(
            org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo estado) {
        EstadoCatalogo estadoDominio = estado == null ? null : EstadoCatalogo.valueOf(estado.name());
        List<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo> insumos = insumoRepositoryPort
                .listar(estadoDominio).stream()
                .map(insumoMapper::aDto)
                .toList();
        return ResponseEntity.ok(insumos);
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo> obtenerInsumo(UUID id) {
        Insumo insumo = insumoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe un insumo con el id '" + id + "'"));
        return ResponseEntity.ok(insumoMapper.aDto(insumo));
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo> editarInsumo(
            UUID id, EditarInsumoRequest editarInsumoRequest) {
        UnidadMedida unidadMedida = editarInsumoRequest.getUnidadMedida() == null
                ? null : UnidadMedida.valueOf(editarInsumoRequest.getUnidadMedida().name());
        Insumo insumo = editarInsumoUseCase.ejecutar(id, editarInsumoRequest.getNombre(), unidadMedida);
        return ResponseEntity.ok(insumoMapper.aDto(insumo));
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo> activarInsumo(UUID id) {
        Insumo insumo = cambiarEstadoInsumoUseCase.activar(id);
        return ResponseEntity.ok(insumoMapper.aDto(insumo));
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Insumo> desactivarInsumo(UUID id) {
        Insumo insumo = cambiarEstadoInsumoUseCase.desactivar(id);
        return ResponseEntity.ok(insumoMapper.aDto(insumo));
    }
}
