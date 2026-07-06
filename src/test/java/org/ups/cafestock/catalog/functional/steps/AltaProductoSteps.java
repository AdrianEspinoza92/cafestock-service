package org.ups.cafestock.catalog.functional.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.ups.cafestock.catalog.infrastructure.web.dto.CrearProductoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo;
import org.ups.cafestock.catalog.infrastructure.web.dto.Producto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AltaProductoSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<Producto> ultimaRespuesta;
    private ResponseEntity<String> ultimaRespuestaCruda;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Before
    public void limpiarEstado() {
        ultimaRespuesta = null;
        ultimaRespuestaCruda = null;
    }

    @Given("que soy encargado de compras")
    public void queSoyEncargadoDeCompras() {
        // Rol no modelado en esta historia; paso documental (ver Assumptions en spec.md)
    }

    @Given("que ya existe un producto activo llamado {string}")
    public void queYaExisteUnProductoActivoLlamado(String nombre) {
        CrearProductoRequest request = new CrearProductoRequest().nombre(nombre).precio(new BigDecimal("1.00"));
        restTemplate.postForEntity(baseUrl() + "/productos", request, Producto.class);
    }

    @When("doy de alta un producto con nombre {string} y precio {double}")
    public void doyDeAltaUnProductoConNombreYPrecio(String nombre, double precio) {
        crearProducto(nombre, precio);
    }

    @When("intento dar de alta un producto con nombre {string} y precio {double}")
    public void intentoDarDeAltaUnProductoConNombreYPrecio(String nombre, double precio) {
        crearProducto(nombre, precio);
    }

    @When("intento dar de alta otro producto con nombre {string} y precio {double}")
    public void intentoDarDeAltaOtroProductoConNombreYPrecio(String nombre, double precio) {
        crearProducto(nombre, precio);
    }

    private void crearProducto(String nombre, double precio) {
        CrearProductoRequest request = new CrearProductoRequest().nombre(nombre).precio(BigDecimal.valueOf(precio));
        ultimaRespuestaCruda = restTemplate.postForEntity(baseUrl() + "/productos", request, String.class);
    }

    @Then("el producto queda disponible para marcarse en la pantalla de venta")
    public void elProductoQuedaDisponibleParaMarcarseEnLaPantallaDeVenta() {
        assertThat(ultimaRespuestaCruda.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Producto[]> activos = restTemplate.getForEntity(
                baseUrl() + "/productos?estado=" + EstadoCatalogo.ACTIVO, Producto[].class);
        List<Producto> productos = List.of(activos.getBody());
        assertThat(productos).anyMatch(p -> p.getEstado() == EstadoCatalogo.ACTIVO);
    }

    @Then("el sistema rechaza la operación y no crea el producto")
    public void elSistemaRechazaLaOperacionYNoCreaElProducto() {
        assertThat(ultimaRespuestaCruda.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Then("el sistema rechaza el alta por duplicado")
    public void elSistemaRechazaElAltaPorDuplicado() {
        assertThat(ultimaRespuestaCruda.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
