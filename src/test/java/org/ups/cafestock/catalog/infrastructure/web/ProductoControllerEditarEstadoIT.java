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
}
