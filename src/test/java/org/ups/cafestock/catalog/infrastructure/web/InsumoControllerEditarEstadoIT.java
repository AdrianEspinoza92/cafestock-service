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
class InsumoControllerEditarEstadoIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String crearInsumoYObtenerId(String nombre) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/insumos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"" + nombre + "\",\"unidadMedida\":\"UNIDAD\",\"stockInicial\":1}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void debeEditarNombreYUnidadDeMedida() throws Exception {
        // given
        String id = crearInsumoYObtenerId("Insumo Editar IT");

        // when / then
        mockMvc.perform(patch("/insumos/" + id)
                        .contentType("application/json")
                        .content("{\"unidadMedida\":\"KILOGRAMO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadMedida").value("KILOGRAMO"));
    }

    @Test
    void debeDesactivarYExcluirDeListadoActivoLuegoReactivarYReaparecer() throws Exception {
        // given
        String id = crearInsumoYObtenerId("Insumo Estado IT");

        // when
        mockMvc.perform(post("/insumos/" + id + "/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        // then: desaparece de GET ?estado=ACTIVO
        MvcResult listadoTrasDesactivar = mockMvc.perform(get("/insumos?estado=ACTIVO"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(listadoTrasDesactivar.getResponse().getContentAsString()).doesNotContain(id);

        // when: reactivar
        mockMvc.perform(post("/insumos/" + id + "/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        // then: reaparece en GET ?estado=ACTIVO
        MvcResult listadoTrasActivar = mockMvc.perform(get("/insumos?estado=ACTIVO"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(listadoTrasActivar.getResponse().getContentAsString()).contains(id);
    }

    @Test
    void debeRetornar404AlEditarInsumoInexistente() throws Exception {
        // given / when / then
        mockMvc.perform(patch("/insumos/00000000-0000-0000-0000-000000000000")
                        .contentType("application/json")
                        .content("{\"unidadMedida\":\"LITRO\"}"))
                .andExpect(status().isNotFound());
    }
}
