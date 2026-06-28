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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço com as regras de negócio de consultas agendadas.
 *
 * <p>Regras aplicadas:
 * <ul>
 *   <li>Apenas pacientes podem agendar consultas, reservando um horário livre.</li>
 *   <li>Um horário já reservado não pode ser agendado novamente (evita sobreposição).</li>
 *   <li>Apenas o próprio paciente ou um administrador podem cancelar, com no mínimo
 *       24h de antecedência em relação ao horário da consulta.</li>
 *   <li>Ao cancelar, o horário volta a ficar disponível para outros pacientes.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ConsultaService {

    private static final long MIN_CANCELLATION_HOURS = 24;

    private final ConsultaRepository consultaRepository;
    private final HorarioRepository horarioRepository;

    /**
     * Agenda uma consulta reservando um horário livre.
     *
     * @param request dados do agendamento (identificador do horário)
     * @param paciente usuário autenticado (deve ser {@code PACIENTE})
     * @return dados da consulta agendada
     * @throws AppException {@code 403} se o usuário não for paciente, {@code 404} se o horário
     *                       não existir, {@code 409} se o horário já estiver reservado
     */
    @Transactional
    public ConsultaResponse agendar(ConsultaRequest request, Usuario paciente) {
        if (paciente.getPapel() != Papel.PACIENTE) {
            throw new AppException("Apenas pacientes podem agendar consultas", HttpStatus.FORBIDDEN);
        }

        Horario horario = horarioRepository.findById(request.horarioId())
                .orElseThrow(() -> new AppException("Horário não encontrado", HttpStatus.NOT_FOUND));

        if (horario.isReservado()) {
            throw new AppException("Horário já está ocupado", HttpStatus.CONFLICT);
        }

        horario.setReservado(true);
        horarioRepository.save(horario);

        Consulta consulta = Consulta.builder()
                .paciente(paciente)
                .profissional(horario.getProfissional())
                .horario(horario)
                .status(StatusConsulta.AGENDADA)
                .build();

        return paraResponse(consultaRepository.save(consulta));
    }

    /**
     * Cancela uma consulta agendada, liberando o horário para novo agendamento.
     *
     * @param id           identificador da consulta
     * @param usuarioAtual usuário autenticado (deve ser o paciente dono ou um administrador)
     * @throws AppException {@code 404} se a consulta não existir, {@code 403} se o usuário não
     *                       tiver permissão, {@code 409} se a consulta não estiver {@code AGENDADA}
     *                       ou se faltarem menos de 24h para o horário agendado
     */
    @Transactional
    public void cancelar(Long id, Usuario usuarioAtual) {
        Consulta consulta = buscarOuFalhar(id);
        validarPermissaoCancelamento(consulta, usuarioAtual);

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new AppException("Consulta não está mais agendada", HttpStatus.CONFLICT);
        }

        Horario horario = consulta.getHorario();
        LocalDateTime appointmentStart = LocalDateTime.of(horario.getData(), horario.getHoraInicio());
        if (LocalDateTime.now().plusHours(MIN_CANCELLATION_HOURS).isAfter(appointmentStart)) {
            throw new AppException(
                    "Cancelamento deve ser feito com pelo menos 24h de antecedência", HttpStatus.CONFLICT);
        }

        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);

        horario.setReservado(false);
        horarioRepository.save(horario);
    }

    @Transactional
    public ConsultaResponse concluir(Long id, Usuario usuarioAtual) {
        Consulta consulta = buscarOuFalhar(id);
        if (usuarioAtual.getPapel() != Papel.PROFISSIONAL
                || !consulta.getProfissional().getId().equals(usuarioAtual.getId())) {
            throw new AppException("Apenas o profissional responsável pode concluir a consulta", HttpStatus.FORBIDDEN);
        }
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new AppException("Consulta não está mais agendada", HttpStatus.CONFLICT);
        }
        consulta.setStatus(StatusConsulta.CONCLUIDA);
        return paraResponse(consultaRepository.save(consulta));
    }

    /**
     * Retorna o histórico de consultas do paciente autenticado.
     *
     * @param paciente usuário autenticado
     * @return lista de consultas do paciente
     */
    @Transactional(readOnly = true)
    public List<ConsultaResponse> buscarHistorico(Usuario paciente) {
        return consultaRepository.findByPaciente(paciente)
                .stream().map(this::paraResponse).toList();
    }

    /**
     * Retorna a agenda do profissional autenticado, com filtros opcionais por dia e paciente.
     *
     * @param profissional usuário autenticado (deve ser {@code PROFISSIONAL})
     * @param data         filtra consultas nesta data (opcional)
     * @param pacienteId   filtra consultas deste paciente (opcional)
     * @return lista de consultas que atendem aos critérios
     * @throws AppException {@code 403} se o usuário não for profissional
     */
    @Transactional(readOnly = true)
    public List<ConsultaResponse> buscarAgenda(Usuario profissional, LocalDate data, Long pacienteId) {
        if (profissional.getPapel() != Papel.PROFISSIONAL) {
            throw new AppException("Apenas profissionais possuem agenda", HttpStatus.FORBIDDEN);
        }
        return consultaRepository.findByProfissionalComFiltros(profissional, data, pacienteId)
                .stream().map(this::paraResponse).toList();
    }

    /**
     * Lista todas as consultas do sistema com filtros opcionais (uso administrativo).
     *
     * @param data           filtra consultas nesta data (opcional)
     * @param profissionalId filtra consultas deste profissional (opcional)
     * @return lista de consultas que atendem aos critérios
     */
    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarTodasAdmin(LocalDate data, Long profissionalId) {
        return consultaRepository.buscarTodasComFiltros(data, profissionalId)
                .stream().map(this::paraResponse).toList();
    }

    private void validarPermissaoCancelamento(Consulta consulta, Usuario usuario) {
        if (usuario.getPapel() == Papel.ADMIN) return;
        if (!consulta.getPaciente().getId().equals(usuario.getId())) {
            throw new AppException("Acesso negado", HttpStatus.FORBIDDEN);
        }
    }

    private Consulta buscarOuFalhar(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new AppException("Consulta não encontrada", HttpStatus.NOT_FOUND));
    }

    private ConsultaResponse paraResponse(Consulta consulta) {
        Horario horario = consulta.getHorario();
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPaciente().getNome(),
                consulta.getProfissional().getNome(),
                consulta.getProfissional().getEspecialidade(),
                horario.getData(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                consulta.getStatus(),
                consulta.getCriadoEm()
        );
    }
}
