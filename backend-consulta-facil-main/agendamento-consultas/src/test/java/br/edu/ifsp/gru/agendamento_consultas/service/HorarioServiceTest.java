package br.edu.ifsp.gru.agendamento_consultas.service;

import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Horario;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.repository.HorarioRepository;
import br.edu.ifsp.gru.agendamento_consultas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Testes unitários de {@link HorarioService}, com dependências mockadas via Mockito
 * (sem subir contexto Spring nem banco de dados).
 */
@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private HorarioService horarioService;

    @BeforeEach
    void setUp() {
        horarioService = new HorarioService(horarioRepository, usuarioRepository);
    }

    private Usuario profissional(Long id) {
        return Usuario.builder().id(id).nome("dra-ana").email("ana@teste.com")
                .senha("hash").papel(Papel.PROFISSIONAL).especialidade("Cardiologia").build();
    }

    private Usuario paciente(Long id) {
        return Usuario.builder().id(id).nome("joao").email("joao@teste.com")
                .senha("hash").papel(Papel.PACIENTE).build();
    }

    private Horario horario(Long id, Usuario profissional, boolean reservado) {
        return Horario.builder()
                .id(id)
                .profissional(profissional)
                .data(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(10, 0))
                .reservado(reservado)
                .build();
    }

    @Test
    void criar_comSucesso() {
        Usuario prof = profissional(1L);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(horarioRepository.existeSobreposicao(eq(prof), any(), any(), any(), isNull())).thenReturn(false);
        when(horarioRepository.save(any(Horario.class))).thenAnswer(inv -> {
            Horario h = inv.getArgument(0);
            h.setId(10L);
            return h;
        });

        HorarioResponse response = horarioService.criar(request, prof);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.profissionalId()).isEqualTo(1L);
        assertThat(response.reservado()).isFalse();
    }

    @Test
    void criar_rejeitaNaoProfissional() {
        Usuario paciente = paciente(2L);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThatThrownBy(() -> horarioService.criar(request, paciente))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(horarioRepository);
    }

    @Test
    void criar_rejeitaDataNoPassado() {
        Usuario prof = profissional(1L);
        HorarioRequest request = new HorarioRequest(LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThatThrownBy(() -> horarioService.criar(request, prof))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void criar_rejeitaHoraFimMenorOuIgualHoraInicio() {
        Usuario prof = profissional(1L);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 0));

        assertThatThrownBy(() -> horarioService.criar(request, prof))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void criar_rejeitaSobreposicao() {
        Usuario prof = profissional(1L);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(horarioRepository.existeSobreposicao(eq(prof), any(), any(), any(), isNull())).thenReturn(true);

        assertThatThrownBy(() -> horarioService.criar(request, prof))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(horarioRepository, never()).save(any());
    }

    @Test
    void atualizar_comSucesso() {
        Usuario prof = profissional(1L);
        Horario existente = horario(5L, prof, false);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(2), LocalTime.of(11, 0), LocalTime.of(12, 0));
        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(horarioRepository.existeSobreposicao(eq(prof), any(), any(), any(), eq(5L))).thenReturn(false);
        when(horarioRepository.save(any(Horario.class))).thenAnswer(inv -> inv.getArgument(0));

        HorarioResponse response = horarioService.atualizar(5L, request, prof);

        assertThat(response.horaInicio()).isEqualTo(LocalTime.of(11, 0));
        assertThat(response.horaFim()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void atualizar_rejeitaQuandoNaoEhDono() {
        Usuario dono = profissional(1L);
        Usuario outroProfissional = profissional(2L);
        Horario existente = horario(5L, dono, false);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(2), LocalTime.of(11, 0), LocalTime.of(12, 0));
        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> horarioService.atualizar(5L, request, outroProfissional))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void atualizar_rejeitaQuandoJaReservado() {
        Usuario prof = profissional(1L);
        Horario existente = horario(5L, prof, true);
        HorarioRequest request = new HorarioRequest(LocalDate.now().plusDays(2), LocalTime.of(11, 0), LocalTime.of(12, 0));
        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> horarioService.atualizar(5L, request, prof))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void excluir_comSucesso() {
        Usuario prof = profissional(1L);
        Horario existente = horario(5L, prof, false);
        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        horarioService.excluir(5L, prof);

        verify(horarioRepository).delete(existente);
    }

    @Test
    void excluir_rejeitaQuandoNaoEhDono() {
        Usuario dono = profissional(1L);
        Usuario outroProfissional = profissional(2L);
        Horario existente = horario(5L, dono, false);
        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> horarioService.excluir(5L, outroProfissional))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(horarioRepository, never()).delete(any());
    }

    @Test
    void excluir_rejeitaQuandoReservado() {
        Usuario prof = profissional(1L);
        Horario existente = horario(5L, prof, true);
        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> horarioService.excluir(5L, prof))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(horarioRepository, never()).delete(any());
    }

    @Test
    void buscarMinhas_retornaListaDoProfissional() {
        Usuario prof = profissional(1L);
        Horario h = horario(5L, prof, false);
        when(horarioRepository.findByProfissional(prof)).thenReturn(List.of(h));

        List<HorarioResponse> resultado = horarioService.buscarMinhas(prof);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(5L);
    }

    @Test
    void buscarLivresPorProfissional_comSucesso() {
        Usuario prof = profissional(1L);
        Horario h = horario(5L, prof, false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(prof));
        when(horarioRepository.findByProfissionalAndReservadoFalse(prof)).thenReturn(List.of(h));

        List<HorarioResponse> resultado = horarioService.buscarLivresPorProfissional(1L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void buscarLivresPorProfissional_profissionalNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.buscarLivresPorProfissional(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
