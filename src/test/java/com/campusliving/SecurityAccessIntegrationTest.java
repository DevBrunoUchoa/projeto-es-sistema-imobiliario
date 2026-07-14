package com.campusliving;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifica a postura de acesso: o "Visitante" (não autenticado) pode navegar
 * pelo catálogo (RF-15/21/24/25), mas escritas e rotas administrativas
 * continuam bloqueadas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void visitantePodeListarAnuncios() throws Exception {
        mvc.perform(get("/anuncios")).andExpect(status().isOk());
    }

    @Test
    void visitantePodeVerMapa() throws Exception {
        mvc.perform(get("/anuncios/mapa")).andExpect(status().isOk());
    }

    @Test
    void visitanteNaoPodePublicarAnuncio() throws Exception {
        mvc.perform(post("/anuncios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> assertTrue(
                        result.getResponse().getStatus() >= 300,
                        "POST /anuncios anônimo não pode ser 2xx"));
    }

    @Test
    void visitanteNaoAcessaAdmin() throws Exception {
        mvc.perform(get("/admin/usuarios"))
                .andExpect(result -> assertTrue(
                        result.getResponse().getStatus() >= 300,
                        "GET /admin anônimo não pode ser 2xx"));
    }
}
