package org.ups.cafestock.catalog.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.cafestock.catalog.application.usecase.CambiarEstadoProductoUseCase;
import org.ups.cafestock.catalog.application.usecase.CrearProductoUseCase;
import org.ups.cafestock.catalog.application.usecase.EditarProductoUseCase;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;
import org.ups.cafestock.catalog.infrastructure.web.api.ProductosApi;
import org.ups.cafestock.catalog.infrastructure.web.dto.CrearProductoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.EditarProductoRequest;
import org.ups.cafestock.catalog.infrastructure.web.mapper.ProductoMapper;

import java.util.List;
import java.util.UUID;

@RestController
public class ProductoController implements ProductosApi {

    private final CrearProductoUseCase crearProductoUseCase;
    private final EditarProductoUseCase editarProductoUseCase;
    private final CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final ProductoMapper productoMapper;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                               EditarProductoUseCase editarProductoUseCase,
                               CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase,
                               ProductoRepositoryPort productoRepositoryPort,
                               ProductoMapper productoMapper) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.editarProductoUseCase = editarProductoUseCase;
        this.cambiarEstadoProductoUseCase = cambiarEstadoProductoUseCase;
        this.productoRepositoryPort = productoRepositoryPort;
        this.productoMapper = productoMapper;
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Producto> crearProducto(
            CrearProductoRequest crearProductoRequest) {
        Producto producto = crearProductoUseCase.ejecutar(
                crearProductoRequest.getNombre(), crearProductoRequest.getPrecio());
        return ResponseEntity.status(HttpStatus.CREATED).body(productoMapper.aDto(producto));
    }

    @Override
    public ResponseEntity<List<org.ups.cafestock.catalog.infrastructure.web.dto.Producto>> listarProductos(
            org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo estado) {
        EstadoCatalogo estadoDominio = estado == null ? null : EstadoCatalogo.valueOf(estado.name());
        List<org.ups.cafestock.catalog.infrastructure.web.dto.Producto> productos = productoRepositoryPort
                .listar(estadoDominio).stream()
                .map(productoMapper::aDto)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Producto> obtenerProducto(UUID id) {
        Producto producto = productoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNoEncontradoException("No existe un producto con el id '" + id + "'"));
        return ResponseEntity.ok(productoMapper.aDto(producto));
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Producto> editarProducto(
            UUID id, EditarProductoRequest editarProductoRequest) {
        Producto producto = editarProductoUseCase.ejecutar(
                id, editarProductoRequest.getNombre(), editarProductoRequest.getPrecio());
        return ResponseEntity.ok(productoMapper.aDto(producto));
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Producto> activarProducto(UUID id) {
        Producto producto = cambiarEstadoProductoUseCase.activar(id);
        return ResponseEntity.ok(productoMapper.aDto(producto));
    }

    @Override
    public ResponseEntity<org.ups.cafestock.catalog.infrastructure.web.dto.Producto> desactivarProducto(UUID id) {
        Producto producto = cambiarEstadoProductoUseCase.desactivar(id);
        return ResponseEntity.ok(productoMapper.aDto(producto));
    }
}
