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
        return crearInsumoYObtenerId(nombre, "1");
    }

    private String crearInsumoYObtenerId(String nombre, String stockInicial) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/insumos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"" + nombre + "\",\"unidadMedida\":\"UNIDAD\",\"stockInicial\":" + stockInicial + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void debeEditarNombreYUnidadDeMedidaSinAjustarElStockInicial() throws Exception {
        // given: stock inicial distintivo para poder verificar que la edición no lo toca
        String id = crearInsumoYObtenerId("Insumo Editar IT", "7.500");

        // when
        mockMvc.perform(patch("/insumos/" + id)
                        .contentType("application/json")
                        .content("{\"unidadMedida\":\"KILOGRAMO\",\"nombre\":\"Insumo Editar IT Renombrado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadMedida").value("KILOGRAMO"))
                .andExpect(jsonPath("$.nombre").value("Insumo Editar IT Renombrado"));

        // then: FR-007 / edge case "la edición no ajusta cantidades de stock en curso" —
        // el use case de edición no acepta ni modifica stockInicial; se verifica explícitamente
        // que el valor persistido sigue siendo el original tras el PATCH
        MvcResult insumoActual = mockMvc.perform(get("/insumos/" + id))
                .andExpect(status().isOk())
                .andReturn();
        java.math.BigDecimal stockTrasEditar = new java.math.BigDecimal(
                objectMapper.readTree(insumoActual.getResponse().getContentAsString())
                        .get("stockInicial").asText());
        assertThat(stockTrasEditar).isEqualByComparingTo("7.500");
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
