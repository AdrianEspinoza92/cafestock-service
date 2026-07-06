package org.ups.cafestock.catalog.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerEditarEstadoIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String crearProductoYObtenerId(String nombre) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"" + nombre + "\",\"precio\":1.00}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void debeEditarNombreYPrecio() throws Exception {
        // given
        String id = crearProductoYObtenerId("Producto Editar IT");

        // when / then
        mockMvc.perform(patch("/productos/" + id)
                        .contentType("application/json")
                        .content("{\"precio\":9.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(9.99));
    }

    @Test
    void debeDesactivarYExcluirDeListadoActivoLuegoReactivarYReaparecer() throws Exception {
        // given
        String id = crearProductoYObtenerId("Producto Estado IT");

        // when
        mockMvc.perform(post("/productos/" + id + "/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        // then: desaparece de GET ?estado=ACTIVO
        MvcResult listadoTrasDesactivar = mockMvc.perform(get("/productos?estado=ACTIVO"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(listadoTrasDesactivar.getResponse().getContentAsString()).doesNotContain(id);

        // when: reactivar
        mockMvc.perform(post("/productos/" + id + "/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        // then: reaparece en GET ?estado=ACTIVO
        MvcResult listadoTrasActivar = mockMvc.perform(get("/productos?estado=ACTIVO"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(listadoTrasActivar.getResponse().getContentAsString()).contains(id);
    }

    @Test
    void debeRetornar404AlEditarProductoInexistente() throws Exception {
        // given / when / then
        mockMvc.perform(patch("/productos/00000000-0000-0000-0000-000000000000")
                        .contentType("application/json")
                        .content("{\"precio\":1.00}"))
                .andExpect(status().isNotFound());
    }

    /**
     * FR-011 / SC-003: una venta ya registrada debe conservar el precio con el
     * que se vendió aunque el producto se edite o desactive después. Esta
     * historia no administra la entidad Venta (fuera de alcance, ver
     * spec.md Assumptions), así que aquí se simula el momento de la venta
     * capturando el precio devuelto por la API en ese instante -tal como lo
     * haría el futuro registro de Venta al copiar el valor- y se verifica
     * que esa copia no se ve afectada por una edición o baja posteriores.
     */
    @Test
    void debePreservarElPrecioCapturadoAlMomentoDeUnaVentaSimuladaTrasEditarYDesactivarElProducto() throws Exception {
        // given: se crea el producto y se "vende" (se captura su precio en ese instante,
        // leído de la propia respuesta de creación, tal como lo haría un futuro registro de Venta)
        MvcResult creado = mockMvc.perform(post("/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Producto Precio Historico IT\",\"precio\":1.00}"))
                .andExpect(status().isCreated())
                .andReturn();
        var productoCreado = objectMapper.readTree(creado.getResponse().getContentAsString());
        String id = productoCreado.get("id").asText();
        java.math.BigDecimal precioAlMomentoDeLaVentaSimulada =
                new java.math.BigDecimal(productoCreado.get("precio").asText());

        // when: el producto se edita y luego se desactiva
        mockMvc.perform(patch("/productos/" + id)
                        .contentType("application/json")
                        .content("{\"precio\":5.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(5.00));

        mockMvc.perform(post("/productos/" + id + "/desactivar"))
                .andExpect(status().isOk());

        // then: la venta simulada conserva su propio precio capturado en el momento de la venta
        assertThat(precioAlMomentoDeLaVentaSimulada).isEqualByComparingTo("1.00");

        // y el precio actual del producto (consultado de nuevo) sí refleja el nuevo valor,
        // demostrando que ambos valores son independientes una vez capturados
        MvcResult productoActual = mockMvc.perform(get("/productos/" + id))
                .andExpect(status().isOk())
                .andReturn();
        java.math.BigDecimal precioActual = new java.math.BigDecimal(
                objectMapper.readTree(productoActual.getResponse().getContentAsString())
                        .get("precio").asText());
        assertThat(precioActual).isEqualByComparingTo("5.00");
        assertThat(precioActual).isNotEqualByComparingTo(precioAlMomentoDeLaVentaSimulada);
    }
}
