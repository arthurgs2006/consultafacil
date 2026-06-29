package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.security.JwtAuthFilter;
import br.edu.ifsp.gru.agendamento_consultas.service.AutenticacaoService;
import br.edu.ifsp.gru.agendamento_consultas.service.HorarioService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de fatia web (slice) para {@link HorarioController}, usando {@code @WebMvcTest}
 * com {@link HorarioService} e {@link AutenticacaoService} mockados via Mockito.
 */
@WebMvcTest(
        controllers = HorarioController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HorarioService horarioService;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    private Usuario profissional() {
        return Usuario.builder().id(1L).nome("dra-ana").email("ana@teste.com")
                .senha("hash").papel(Papel.PROFISSIONAL).especialidade("Cardiologia").build();
    }

    @Test
    void criar_comSucesso_retorna201() throws Exception {
        Usuario profissional = profissional();
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        HorarioResponse response = new HorarioResponse(10L, 1L, "dra-ana",
                request.data(), request.horaInicio(), request.horaFim(), false);
        when(horarioService.criar(any(HorarioRequest.class), any(Usuario.class))).thenReturn(response);

        mockMvc.perform(post("/horarios")
                        .with(user(profissional))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nomeProfissional").value("dra-ana"))
                .andExpect(jsonPath("$.reservado").value(false));
    }

    @Test
    void criar_quandoNaoEhProfissional_retorna403() throws Exception {
        Usuario profissional = profissional();
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(horarioService.criar(any(HorarioRequest.class), any(Usuario.class)))
                .thenThrow(new AppException("Apenas profissionais podem gerenciar horários", HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/horarios")
                        .with(user(profissional))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Apenas profissionais podem gerenciar horários"));
    }
}
