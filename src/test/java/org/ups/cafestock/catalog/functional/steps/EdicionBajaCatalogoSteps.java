package org.ups.cafestock.catalog.functional.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.ups.cafestock.catalog.infrastructure.web.dto.CrearInsumoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.CrearProductoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.EditarProductoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo;
import org.ups.cafestock.catalog.infrastructure.web.dto.Insumo;
import org.ups.cafestock.catalog.infrastructure.web.dto.Producto;
import org.ups.cafestock.catalog.infrastructure.web.dto.UnidadMedida;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EdicionBajaCatalogoSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final Map<String, Producto> productosPorNombre = new HashMap<>();
    private final Map<String, Insumo> insumosPorNombre = new HashMap<>();

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Before
    public void limpiarEstado() {
        productosPorNombre.clear();
        insumosPorNombre.clear();
    }

    @Given("que existe un producto activo llamado {string} con precio {double}")
    public void queExisteUnProductoActivoLlamadoConPrecio(String nombre, double precio) {
        CrearProductoRequest request = new CrearProductoRequest().nombre(nombre).precio(BigDecimal.valueOf(precio));
        ResponseEntity<Producto> respuesta = restTemplate.postForEntity(baseUrl() + "/productos", request, Producto.class);
        productosPorNombre.put(nombre, respuesta.getBody());
    }

    @Given("que existe un producto activo llamado {string}")
    public void queExisteUnProductoActivoLlamado(String nombre) {
        queExisteUnProductoActivoLlamadoConPrecio(nombre, 1.00);
    }

    @Given("que existe un insumo activo llamado {string}")
    public void queExisteUnInsumoActivoLlamado(String nombre) {
        CrearInsumoRequest request = new CrearInsumoRequest()
                .nombre(nombre).unidadMedida(UnidadMedida.UNIDAD).stockInicial(BigDecimal.TEN);
        ResponseEntity<Insumo> respuesta = restTemplate.postForEntity(baseUrl() + "/insumos", request, Insumo.class);
        insumosPorNombre.put(nombre, respuesta.getBody());
    }

    @When("edito el precio del producto {string} a {double}")
    public void editoElPrecioDelProductoA(String nombre, double nuevoPrecio) {
        Producto producto = productosPorNombre.get(nombre);
        EditarProductoRequest request = new EditarProductoRequest().precio(BigDecimal.valueOf(nuevoPrecio));
        RequestEntity<EditarProductoRequest> requestEntity = RequestEntity
                .method(HttpMethod.PATCH, URI.create(baseUrl() + "/productos/" + producto.getId()))
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request);
        ResponseEntity<Producto> respuesta = restTemplate.exchange(requestEntity, Producto.class);
        productosPorNombre.put(nombre, respuesta.getBody());
    }

    @When("desactivo el producto {string}")
    public void desactivoElProducto(String nombre) {
        Producto producto = productosPorNombre.get(nombre);
        restTemplate.postForEntity(baseUrl() + "/productos/" + producto.getId() + "/desactivar", null, Producto.class);
    }

    @When("activo nuevamente el producto {string}")
    public void activoNuevamenteElProducto(String nombre) {
        Producto producto = productosPorNombre.get(nombre);
        restTemplate.postForEntity(baseUrl() + "/productos/" + producto.getId() + "/activar", null, Producto.class);
    }

    @When("desactivo el insumo {string}")
    public void desactivoElInsumo(String nombre) {
        Insumo insumo = insumosPorNombre.get(nombre);
        restTemplate.postForEntity(baseUrl() + "/insumos/" + insumo.getId() + "/desactivar", null, Insumo.class);
    }

    @When("activo nuevamente el insumo {string}")
    public void activoNuevamenteElInsumo(String nombre) {
        Insumo insumo = insumosPorNombre.get(nombre);
        restTemplate.postForEntity(baseUrl() + "/insumos/" + insumo.getId() + "/activar", null, Insumo.class);
    }

    @Then("el cambio se refleja en las pantallas que lo usan")
    public void elCambioSeReflejaEnLasPantallasQueLoUsan() {
        Producto actualizado = productosPorNombre.get("Mocha Editable");
        ResponseEntity<Producto> respuesta = restTemplate.getForEntity(
                baseUrl() + "/productos/" + actualizado.getId(), Producto.class);
        assertThat(respuesta.getBody().getPrecio()).isEqualByComparingTo("2.50");
    }

    @Then("el producto desaparece de la lista de productos activos")
    public void elProductoDesapareceDeLaListaDeProductosActivos() {
        Producto producto = productosPorNombre.get("Producto Desactivable");
        ResponseEntity<Producto[]> activos = restTemplate.getForEntity(
                baseUrl() + "/productos?estado=" + EstadoCatalogo.ACTIVO, Producto[].class);
        assertThat(List.of(activos.getBody())).noneMatch(p -> p.getId().equals(producto.getId()));
    }

    @Then("el producto vuelve a aparecer en la lista de productos activos")
    public void elProductoVuelveAAparecerEnLaListaDeProductosActivos() {
        Producto producto = productosPorNombre.get("Producto Desactivable");
        ResponseEntity<Producto[]> activos = restTemplate.getForEntity(
                baseUrl() + "/productos?estado=" + EstadoCatalogo.ACTIVO, Producto[].class);
        assertThat(List.of(activos.getBody())).anyMatch(p -> p.getId().equals(producto.getId()));
    }

    @Then("el insumo desaparece de la lista de insumos activos")
    public void elInsumoDesapareceDeLaListaDeInsumosActivos() {
        Insumo insumo = insumosPorNombre.get("Insumo Desactivable");
        ResponseEntity<Insumo[]> activos = restTemplate.getForEntity(
                baseUrl() + "/insumos?estado=" + EstadoCatalogo.ACTIVO, Insumo[].class);
        assertThat(List.of(activos.getBody())).noneMatch(i -> i.getId().equals(insumo.getId()));
    }

    @Then("el insumo vuelve a aparecer en la lista de insumos activos")
    public void elInsumoVuelveAAparecerEnLaListaDeInsumosActivos() {
        Insumo insumo = insumosPorNombre.get("Insumo Desactivable");
        ResponseEntity<Insumo[]> activos = restTemplate.getForEntity(
                baseUrl() + "/insumos?estado=" + EstadoCatalogo.ACTIVO, Insumo[].class);
        assertThat(List.of(activos.getBody())).anyMatch(i -> i.getId().equals(insumo.getId()));
    }
}
