package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.enums.StatusConsulta;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.security.JwtAuthFilter;
import br.edu.ifsp.gru.agendamento_consultas.service.ConsultaService;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de fatia web (slice) para {@link ConsultaController}, usando {@code @WebMvcTest}
 * com {@link ConsultaService} mockado via Mockito.
 */
@WebMvcTest(
        controllers = ConsultaController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConsultaService consultaService;

    private Usuario paciente() {
        return Usuario.builder().id(1L).nome("joao").email("joao@teste.com")
                .senha("hash").papel(Papel.PACIENTE).build();
    }

    @Test
    void agendar_comSucesso_retorna201() throws Exception {
        Usuario paciente = paciente();
        ConsultaRequest request = new ConsultaRequest(10L);
        ConsultaResponse response = new ConsultaResponse(
                100L, "joao", "dra-ana", "Cardiologia",
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0),
                StatusConsulta.AGENDADA, LocalDateTime.now());
        when(consultaService.agendar(any(ConsultaRequest.class), any(Usuario.class))).thenReturn(response);

        mockMvc.perform(post("/consultas")
                        .with(user(paciente))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.nomePaciente").value("joao"))
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    void agendar_quandoNaoEhPaciente_retorna403() throws Exception {
        Usuario paciente = paciente();
        ConsultaRequest request = new ConsultaRequest(10L);
        when(consultaService.agendar(any(ConsultaRequest.class), any(Usuario.class)))
                .thenThrow(new AppException("Apenas pacientes podem agendar consultas", HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/consultas")
                        .with(user(paciente))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Apenas pacientes podem agendar consultas"));
    }
}
