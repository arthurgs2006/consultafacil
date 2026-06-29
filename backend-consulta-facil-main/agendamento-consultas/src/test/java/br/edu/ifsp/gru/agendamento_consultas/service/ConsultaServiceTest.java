package br.edu.ifsp.gru.agendamento_consultas.service;

import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.enums.StatusConsulta;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Consulta;
import br.edu.ifsp.gru.agendamento_consultas.model.Horario;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.repository.ConsultaRepository;
import br.edu.ifsp.gru.agendamento_consultas.repository.HorarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários de {@link ConsultaService}, com dependências mockadas via Mockito
 * (sem subir contexto Spring nem banco de dados).
 */
@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private HorarioRepository horarioRepository;

    private ConsultaService consultaService;

    @BeforeEach
    void setUp() {
        consultaService = new ConsultaService(consultaRepository, horarioRepository);
    }

    private Usuario profissional(Long id) {
        return Usuario.builder().id(id).nome("dra-ana").email("ana@teste.com")
                .senha("hash").papel(Papel.PROFISSIONAL).especialidade("Cardiologia").build();
    }

    private Usuario paciente(Long id) {
        return Usuario.builder().id(id).nome("joao").email("joao@teste.com")
                .senha("hash").papel(Papel.PACIENTE).build();
    }

    private Horario horario(Long id, Usuario profissional, boolean reservado, LocalDate data, LocalTime horaInicio) {
        return Horario.builder()
                .id(id)
                .profissional(profissional)
                .data(data)
                .horaInicio(horaInicio)
                .horaFim(horaInicio.plusHours(1))
                .reservado(reservado)
                .build();
    }

    private Consulta consulta(Long id, Usuario paciente, Usuario profissional, Horario horario, StatusConsulta status) {
        return Consulta.builder()
                .id(id)
                .paciente(paciente)
                .profissional(profissional)
                .horario(horario)
                .status(status)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    void agendar_comSucesso_marcaHorarioComoReservado() {
        Usuario paciente = paciente(1L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, false, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        ConsultaRequest request = new ConsultaRequest(10L);

        when(horarioRepository.findById(10L)).thenReturn(Optional.of(horario));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> {
            Consulta c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        ConsultaResponse response = consultaService.agendar(request, paciente);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.nomePaciente()).isEqualTo("joao");
        assertThat(response.nomeProfissional()).isEqualTo("dra-ana");
        assertThat(response.status()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(horario.isReservado()).isTrue();
        verify(horarioRepository).save(horario);
    }

    @Test
    void agendar_rejeitaNaoPaciente() {
        Usuario prof = profissional(2L);
        ConsultaRequest request = new ConsultaRequest(10L);

        assertThatThrownBy(() -> consultaService.agendar(request, prof))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(horarioRepository);
    }

    @Test
    void agendar_rejeitaHorarioInexistente() {
        Usuario paciente = paciente(1L);
        ConsultaRequest request = new ConsultaRequest(999L);
        when(horarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultaService.agendar(request, paciente))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void agendar_rejeitaHorarioJaReservado() {
        Usuario paciente = paciente(1L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        ConsultaRequest request = new ConsultaRequest(10L);
        when(horarioRepository.findById(10L)).thenReturn(Optional.of(horario));

        assertThatThrownBy(() -> consultaService.agendar(request, paciente))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void cancelar_comSucesso_liberaHorario() {
        Usuario paciente = paciente(1L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.AGENDADA);
        when(consultaRepository.findById(100L)).thenReturn(Optional.of(consulta));

        consultaService.cancelar(100L, paciente);

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        assertThat(horario.isReservado()).isFalse();
        verify(consultaRepository).save(consulta);
        verify(horarioRepository).save(horario);
    }

    @Test
    void cancelar_permiteQuandoEhAdmin() {
        Usuario paciente = paciente(1L);
        Usuario admin = Usuario.builder().id(99L).nome("admin").email("admin@teste.com").senha("hash").papel(Papel.ADMIN).build();
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.AGENDADA);
        when(consultaRepository.findById(100L)).thenReturn(Optional.of(consulta));

        consultaService.cancelar(100L, admin);

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
    }

    @Test
    void cancelar_rejeitaQuandoNaoEhDonoNemAdmin() {
        Usuario dono = paciente(1L);
        Usuario outroPaciente = paciente(3L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, dono, prof, horario, StatusConsulta.AGENDADA);
        when(consultaRepository.findById(100L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelar(100L, outroPaciente))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void cancelar_rejeitaQuandoConsultaNaoEstaAgendada() {
        Usuario paciente = paciente(1L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, false, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.CANCELADA);
        when(consultaRepository.findById(100L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelar(100L, paciente))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void cancelar_rejeitaQuandoFaltamMenosDe24h() {
        Usuario paciente = paciente(1L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, true, LocalDate.now(), LocalTime.now().plusHours(2));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.AGENDADA);
        when(consultaRepository.findById(100L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelar(100L, paciente))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void buscarHistorico_retornaListaDoPaciente() {
        Usuario paciente = paciente(1L);
        Usuario prof = profissional(2L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.AGENDADA);
        when(consultaRepository.findByPaciente(paciente)).thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado = consultaService.buscarHistorico(paciente);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(100L);
    }

    @Test
    void buscarAgenda_rejeitaNaoProfissional() {
        Usuario paciente = paciente(1L);

        assertThatThrownBy(() -> consultaService.buscarAgenda(paciente, null, null))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void buscarAgenda_filtraCorretamente() {
        Usuario prof = profissional(2L);
        Usuario paciente = paciente(1L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.AGENDADA);
        LocalDate filtroData = horario.getData();
        when(consultaRepository.findByProfissionalComFiltros(prof, filtroData, 1L)).thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado = consultaService.buscarAgenda(prof, filtroData, 1L);

        assertThat(resultado).hasSize(1);
        verify(consultaRepository).findByProfissionalComFiltros(prof, filtroData, 1L);
    }

    @Test
    void listarTodasAdmin_retornaListaComFiltros() {
        Usuario prof = profissional(2L);
        Usuario paciente = paciente(1L);
        Horario horario = horario(10L, prof, true, LocalDate.now().plusDays(5), LocalTime.of(9, 0));
        Consulta consulta = consulta(100L, paciente, prof, horario, StatusConsulta.AGENDADA);
        when(consultaRepository.buscarTodasComFiltros(null, 2L)).thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado = consultaService.listarTodasAdmin(null, 2L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nomeProfissional()).isEqualTo("dra-ana");
    }
}
