package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.AutenticacaoResponse;
import br.edu.ifsp.gru.agendamento_consultas.dto.CadastroRequest;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.security.JwtAuthFilter;
import br.edu.ifsp.gru.agendamento_consultas.service.AutenticacaoService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de fatia web (slice) para {@link AutenticacaoController}, usando {@code @WebMvcTest}
 * com {@link AutenticacaoService} mockado via Mockito.
 */
@WebMvcTest(
        controllers = AutenticacaoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @Test
    void registrar_comSucesso_retorna201() throws Exception {
        CadastroRequest request = new CadastroRequest("joao123", "joao@teste.com", "senha1234", Papel.PACIENTE, null);
        AutenticacaoResponse response = new AutenticacaoResponse("token-jwt", "joao123", "PACIENTE");
        when(autenticacaoService.registrar(any(CadastroRequest.class))).thenReturn(response);

        mockMvc.perform(post("/autenticacao/registrar")
                        .with(user("anonimo").roles("PACIENTE"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.nomeUsuario").value("joao123"))
                .andExpect(jsonPath("$.papel").value("PACIENTE"));
    }

    @Test
    void registrar_comDadosInvalidos_retorna400() throws Exception {
        String corpoInvalido = """
                {"nomeUsuario":"","email":"nao-e-email","senha":"123","papel":null}
                """;

        mockMvc.perform(post("/autenticacao/registrar")
                        .with(user("anonimo").roles("PACIENTE"))
                        .contentType("application/json")
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest());
    }
}
