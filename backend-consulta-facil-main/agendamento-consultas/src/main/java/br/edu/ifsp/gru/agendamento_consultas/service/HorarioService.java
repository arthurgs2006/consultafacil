package br.edu.ifsp.gru.agendamento_consultas.service;

import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Horario;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.repository.HorarioRepository;
import br.edu.ifsp.gru.agendamento_consultas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Serviço com as regras de negócio de horários disponíveis.
 *
 * <p>Apenas profissionais podem cadastrar, editar e excluir seus próprios horários.
 * Um horário já reservado por um paciente não pode mais ser editado ou excluído.
 * Horários que se sobrepõem a outro já cadastrado pelo mesmo profissional na mesma
 * data são rejeitados.</p>
 */
@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Cadastra um novo horário disponível para o profissional autenticado.
     *
     * @param request      dados do horário (data, início e término)
     * @param usuarioAtual usuário autenticado (deve ser {@code PROFISSIONAL})
     * @return dados do horário criado
     * @throws AppException {@code 403} se o usuário não for profissional, {@code 400} se os
     *                       horários forem inválidos ou a data estiver no passado, {@code 409}
     *                       se o horário se sobrepuser a outro já cadastrado
     */
    @Transactional
    public HorarioResponse criar(HorarioRequest request, Usuario usuarioAtual) {
        validarProfissional(usuarioAtual);
        validarIntervaloHorario(request.data(), request.horaInicio(), request.horaFim());
        validarSemSobreposicao(usuarioAtual, request.data(), request.horaInicio(), request.horaFim(), null);

        Horario horario = Horario.builder()
                .profissional(usuarioAtual)
                .data(request.data())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .reservado(false)
                .build();

        return paraResponse(horarioRepository.save(horario));
    }

    /**
     * Atualiza um horário disponível existente. Apenas o profissional dono pode editar,
     * e somente enquanto o horário não estiver reservado.
     *
     * @param id           identificador do horário
     * @param request      novos dados do horário
     * @param usuarioAtual usuário autenticado (deve ser o profissional dono)
     * @return dados atualizados do horário
     * @throws AppException {@code 404} se não existir, {@code 403} se não for o dono,
     *                       {@code 409} se já estiver reservado ou se sobrepuser outro horário
     */
    @Transactional
    public HorarioResponse atualizar(Long id, HorarioRequest request, Usuario usuarioAtual) {
        Horario horario = buscarOuFalhar(id);
        validarDono(horario, usuarioAtual);
        validarNaoReservado(horario);
        validarIntervaloHorario(request.data(), request.horaInicio(), request.horaFim());
        validarSemSobreposicao(usuarioAtual, request.data(), request.horaInicio(), request.horaFim(), id);

        horario.setData(request.data());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFim(request.horaFim());

        return paraResponse(horarioRepository.save(horario));
    }

    /**
     * Exclui um horário disponível. Apenas o profissional dono pode excluir,
     * e somente enquanto o horário não estiver reservado.
     *
     * @param id           identificador do horário
     * @param usuarioAtual usuário autenticado (deve ser o profissional dono)
     * @throws AppException {@code 404} se não existir, {@code 403} se não for o dono,
     *                       {@code 409} se já estiver reservado
     */
    @Transactional
    public void excluir(Long id, Usuario usuarioAtual) {
        Horario horario = buscarOuFalhar(id);
        validarDono(horario, usuarioAtual);
        validarNaoReservado(horario);
        horarioRepository.delete(horario);
    }

    /**
     * Lista todos os horários (livres ou reservados) do profissional autenticado.
     *
     * @param usuarioAtual usuário autenticado (deve ser {@code PROFISSIONAL})
     * @return lista de horários do profissional
     * @throws AppException {@code 403} se o usuário não for profissional
     */
    @Transactional(readOnly = true)
    public List<HorarioResponse> buscarMinhas(Usuario usuarioAtual) {
        validarProfissional(usuarioAtual);
        return horarioRepository.findByProfissional(usuarioAtual)
                .stream().map(this::paraResponse).toList();
    }

    /**
     * Lista os horários livres de um profissional, para escolha do paciente.
     *
     * @param profissionalId identificador do profissional
     * @return lista de horários livres
     * @throws AppException {@code 404} se o usuário não existir ou não for profissional
     */
    @Transactional(readOnly = true)
    public List<HorarioResponse> buscarLivresPorProfissional(Long profissionalId) {
        Usuario profissional = usuarioRepository.findById(profissionalId)
                .filter(u -> u.getPapel() == Papel.PROFISSIONAL)
                .orElseThrow(() -> new AppException("Profissional não encontrado", HttpStatus.NOT_FOUND));
        return horarioRepository.findByProfissionalAndReservadoFalse(profissional)
                .stream().map(this::paraResponse).toList();
    }

    private void validarIntervaloHorario(LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        if (data.isBefore(LocalDate.now())) {
            throw new AppException("A data não pode estar no passado", HttpStatus.BAD_REQUEST);
        }
        if (!horaFim.isAfter(horaInicio)) {
            throw new AppException("O horário de término deve ser após o de início", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarSemSobreposicao(Usuario profissional, LocalDate data, LocalTime horaInicio,
                                         LocalTime horaFim, Long excludeId) {
        if (horarioRepository.existeSobreposicao(profissional, data, horaInicio, horaFim, excludeId)) {
            throw new AppException("Horário sobreposto a outro já cadastrado", HttpStatus.CONFLICT);
        }
    }

    private void validarProfissional(Usuario usuario) {
        if (usuario.getPapel() != Papel.PROFISSIONAL) {
            throw new AppException("Apenas profissionais podem gerenciar horários", HttpStatus.FORBIDDEN);
        }
    }

    private void validarDono(Horario horario, Usuario usuario) {
        if (!horario.getProfissional().getId().equals(usuario.getId())) {
            throw new AppException("Apenas o profissional dono do horário pode realizar esta ação", HttpStatus.FORBIDDEN);
        }
    }

    private void validarNaoReservado(Horario horario) {
        if (horario.isReservado()) {
            throw new AppException("Não é possível alterar um horário já reservado", HttpStatus.CONFLICT);
        }
    }

    private Horario buscarOuFalhar(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new AppException("Horário não encontrado", HttpStatus.NOT_FOUND));
    }

    private HorarioResponse paraResponse(Horario horario) {
        return new HorarioResponse(
                horario.getId(),
                horario.getProfissional().getId(),
                horario.getProfissional().getNome(),
                horario.getData(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.isReservado()
        );
    }
}
