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
import org.ups.cafestock.catalog.infrastructure.web.dto.CrearInsumoRequest;
import org.ups.cafestock.catalog.infrastructure.web.dto.EstadoCatalogo;
import org.ups.cafestock.catalog.infrastructure.web.dto.Insumo;
import org.ups.cafestock.catalog.infrastructure.web.dto.UnidadMedida;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AltaInsumoSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> ultimaRespuestaCruda;
    private String ultimoNombreIntentado;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Before
    public void limpiarEstado() {
        ultimaRespuestaCruda = null;
        ultimoNombreIntentado = null;
    }

    @Given("que ya existe un insumo activo llamado {string}")
    public void queYaExisteUnInsumoActivoLlamado(String nombre) {
        CrearInsumoRequest request = new CrearInsumoRequest()
                .nombre(nombre).unidadMedida(UnidadMedida.LITRO).stockInicial(new BigDecimal("1"));
        restTemplate.postForEntity(baseUrl() + "/insumos", request, Insumo.class);
    }

    @When("doy de alta un insumo con nombre {string} unidad de medida {string} y stock inicial {double}")
    public void doyDeAltaUnInsumoConNombreUnidadDeMedidaYStockInicial(String nombre, String unidad, double stock) {
        crearInsumo(nombre, unidad, stock);
    }

    @When("intento dar de alta un insumo con nombre {string} unidad de medida {string} y stock inicial {double}")
    public void intentoDarDeAltaUnInsumoConNombreUnidadDeMedidaYStockInicial(String nombre, String unidad, double stock) {
        crearInsumo(nombre, unidad, stock);
    }

    @When("intento dar de alta otro insumo con nombre {string} unidad de medida {string} y stock inicial {double}")
    public void intentoDarDeAltaOtroInsumoConNombreUnidadDeMedidaYStockInicial(String nombre, String unidad, double stock) {
        crearInsumo(nombre, unidad, stock);
    }

    private void crearInsumo(String nombre, String unidad, double stock) {
        ultimoNombreIntentado = nombre;
        CrearInsumoRequest request = new CrearInsumoRequest()
                .nombre(nombre).unidadMedida(UnidadMedida.valueOf(unidad)).stockInicial(BigDecimal.valueOf(stock));
        ultimaRespuestaCruda = restTemplate.postForEntity(baseUrl() + "/insumos", request, String.class);
    }

    @Then("el insumo queda disponible para asociarse a recetas y a mínimos")
    public void elInsumoQuedaDisponibleParaAsociarseARecetasYAMinimos() {
        assertThat(ultimaRespuestaCruda.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Insumo[]> activos = restTemplate.getForEntity(
                baseUrl() + "/insumos?estado=" + EstadoCatalogo.ACTIVO, Insumo[].class);
        List<Insumo> insumos = List.of(activos.getBody());
        String nombreEsperado = ultimoNombreIntentado.trim();
        assertThat(insumos)
                .anyMatch(i -> i.getEstado() == EstadoCatalogo.ACTIVO
                        && i.getNombre().equalsIgnoreCase(nombreEsperado));
    }

    @Then("el sistema rechaza la operación y no crea el insumo")
    public void elSistemaRechazaLaOperacionYNoCreaElInsumo() {
        assertThat(ultimaRespuestaCruda.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Then("el sistema rechaza el alta de insumo por duplicado")
    public void elSistemaRechazaElAltaDeInsumoPorDuplicado() {
        assertThat(ultimaRespuestaCruda.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
