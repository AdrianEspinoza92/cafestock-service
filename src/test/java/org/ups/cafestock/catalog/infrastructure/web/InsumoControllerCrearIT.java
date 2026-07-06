package org.ups.cafestock.catalog.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InsumoControllerCrearIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void debeObtenerInsumoPorIdYRetornar200() throws Exception {
        // given
        MvcResult creado = mockMvc.perform(post("/insumos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Insumo IT Obtener\",\"unidadMedida\":\"UNIDAD\",\"stockInicial\":1}"))
                .andExpect(status().isCreated())
                .andReturn();
        String id = objectMapper.readTree(creado.getResponse().getContentAsString()).get("id").asText();

        // when / then
        mockMvc.perform(get("/insumos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Insumo IT Obtener"));
    }

    @Test
    void debeRetornar404AlObtenerInsumoInexistente() throws Exception {
        // given / when / then
        mockMvc.perform(get("/insumos/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void debeCrearInsumoYRetornar201() throws Exception {
        // given / when / then
        mockMvc.perform(post("/insumos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Insumo IT Crear\",\"unidadMedida\":\"KILOGRAMO\",\"stockInicial\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Insumo IT Crear"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void debeRechazarStockInvalidoCon400() throws Exception {
        // given / when / then
        mockMvc.perform(post("/insumos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Insumo IT Stock Invalido\",\"unidadMedida\":\"GRAMO\",\"stockInicial\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRechazarNombreDuplicadoCon409() throws Exception {
        // given
        mockMvc.perform(post("/insumos")
                .contentType("application/json")
                .content("{\"nombre\":\"Insumo IT Duplicado\",\"unidadMedida\":\"LITRO\",\"stockInicial\":1}"));

        // when / then
        mockMvc.perform(post("/insumos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Insumo IT Duplicado\",\"unidadMedida\":\"LITRO\",\"stockInicial\":2}"))
                .andExpect(status().isConflict());
    }
}
