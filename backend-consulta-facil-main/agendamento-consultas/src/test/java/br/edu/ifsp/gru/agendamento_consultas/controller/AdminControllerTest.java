package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.ProfissionalResponse;
import br.edu.ifsp.gru.agendamento_consultas.security.JwtAuthFilter;
import br.edu.ifsp.gru.agendamento_consultas.service.AutenticacaoService;
import br.edu.ifsp.gru.agendamento_consultas.service.ConsultaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de fatia web (slice) para {@link AdminController}, usando {@code @WebMvcTest}
 * com {@link AutenticacaoService} e {@link ConsultaService} mockados via Mockito.
 */
@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @MockitoBean
    private ConsultaService consultaService;

    @Test
    void listarProfissionais_comSucesso_retorna200() throws Exception {
        when(autenticacaoService.listarProfissionais())
                .thenReturn(List.of(new ProfissionalResponse(1L, "dra-ana", "Cardiologia")));

        mockMvc.perform(get("/admin/profissionais").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("dra-ana"))
                .andExpect(jsonPath("$[0].especialidade").value("Cardiologia"));
    }

    @Test
    void listarConsultas_comParametroInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/admin/consultas")
                        .param("profissionalId", "nao-e-um-numero")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
