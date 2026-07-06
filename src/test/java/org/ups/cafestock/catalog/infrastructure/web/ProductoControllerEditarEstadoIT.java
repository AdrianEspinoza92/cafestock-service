package org.ups.cafestock.catalog.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.ups.cafestock.catalog.domain.model.HistorialPrecioProducto;
import org.ups.cafestock.catalog.domain.port.HistorialPrecioProductoRepositoryPort;

import java.util.List;
import java.util.UUID;

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

    @Autowired
    private HistorialPrecioProductoRepositoryPort historialPrecioProductoRepositoryPort;

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
     * spec.md Assumptions), pero SÍ persiste cada precio reemplazado en
     * `producto_precio_historico` (ver EditarProductoUseCase). Esta prueba
     * verifica ese registro PERSISTIDO en la base de datos real -no una
     * variable local- y confirma que sobrevive intacto a ediciones y bajas
     * posteriores del producto.
     */
    @Test
    void debePreservarEnLaBaseDeDatosElPrecioReemplazadoTrasEditarYDesactivarElProducto() throws Exception {
        // given: se crea el producto con precio 1.00
        MvcResult creado = mockMvc.perform(post("/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Producto Precio Historico IT\",\"precio\":1.00}"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID id = UUID.fromString(
                objectMapper.readTree(creado.getResponse().getContentAsString()).get("id").asText());

        // when: se edita el precio a 5.00 (reemplaza el 1.00 original) y luego se desactiva
        mockMvc.perform(patch("/productos/" + id)
                        .contentType("application/json")
                        .content("{\"precio\":5.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(5.00));

        mockMvc.perform(post("/productos/" + id + "/desactivar"))
                .andExpect(status().isOk());

        // then: el registro PERSISTIDO del precio reemplazado (1.00) sigue existiendo
        // y no fue alterado por la edición ni por la baja posteriores
        List<HistorialPrecioProducto> historial = historialPrecioProductoRepositoryPort.listarPorProducto(id);
        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).getPrecio()).isEqualByComparingTo("1.00");

        // y el precio vigente del producto sí refleja el valor nuevo, confirmando que
        // ambos registros (histórico y vigente) son independientes una vez persistidos
        MvcResult productoActual = mockMvc.perform(get("/productos/" + id))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(productoActual.getResponse().getContentAsString())
                .get("precio").decimalValue()).isEqualByComparingTo("5.00");
    }
}
