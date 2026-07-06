package org.ups.cafestock.catalog.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerCrearIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeCrearProductoYRetornar201() throws Exception {
        // given / when / then
        mockMvc.perform(post("/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Producto IT Crear\",\"precio\":2.10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Producto IT Crear"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void debeRechazarPrecioInvalidoCon400() throws Exception {
        // given / when / then
        mockMvc.perform(post("/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Producto IT Precio Invalido\",\"precio\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRechazarNombreDuplicadoCon409() throws Exception {
        // given
        mockMvc.perform(post("/productos")
                .contentType("application/json")
                .content("{\"nombre\":\"Producto IT Duplicado\",\"precio\":1.00}"));

        // when / then
        mockMvc.perform(post("/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Producto IT Duplicado\",\"precio\":1.50}"))
                .andExpect(status().isConflict());
    }
}
