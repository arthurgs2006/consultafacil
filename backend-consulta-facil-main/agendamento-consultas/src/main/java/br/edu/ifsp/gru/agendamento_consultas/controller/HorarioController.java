package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.HorarioResponse;
import br.edu.ifsp.gru.agendamento_consultas.dto.ProfissionalResponse;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.service.AutenticacaoService;
import br.edu.ifsp.gru.agendamento_consultas.service.HorarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de horários disponíveis e listagem de profissionais.
 *
 * <p>Todas as rotas exigem autenticação via JWT. Cadastro, edição e exclusão de horários
 * são restritos ao profissional dono do horário.</p>
 */
@RestController
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;
    private final AutenticacaoService autenticacaoService;

    /**
     * Lista todos os profissionais cadastrados, para o paciente escolher com quem agendar.
     *
     * @return {@code 200 OK} com a lista de profissionais
     */
    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalResponse>> listarProfissionais() {
        return ResponseEntity.ok(autenticacaoService.listarProfissionaisAtivos());
    }

    /**
     * Cadastra um novo horário disponível. O usuário autenticado deve ser um profissional.
     *
     * @param request      data, início e término do horário
     * @param usuarioAtual usuário autenticado (será o dono do horário)
     * @return {@code 201 Created} com os dados do horário
     */
    @PostMapping("/horarios")
    public ResponseEntity<HorarioResponse> criar(
            @Valid @RequestBody HorarioRequest request,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.criar(request, usuarioAtual));
    }

    /**
     * Atualiza um horário disponível existente. Apenas o profissional dono pode editar,
     * e somente enquanto o horário não estiver reservado.
     *
     * @param id           identificador do horário
     * @param request      novos dados do horário
     * @param usuarioAtual usuário autenticado (deve ser o dono)
     * @return {@code 200 OK} com os dados atualizados
     */
    @PutMapping("/horarios/{id}")
    public ResponseEntity<HorarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody HorarioRequest request,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        return ResponseEntity.ok(horarioService.atualizar(id, request, usuarioAtual));
    }

    /**
     * Exclui um horário disponível. Apenas o profissional dono pode excluir,
     * e somente enquanto o horário não estiver reservado.
     *
     * @param id           identificador do horário
     * @param usuarioAtual usuário autenticado (deve ser o dono)
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/horarios/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        horarioService.excluir(id, usuarioAtual);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos os horários (livres ou reservados) do profissional autenticado.
     *
     * @param usuarioAtual usuário autenticado (deve ser profissional)
     * @return {@code 200 OK} com a lista de horários
     */
    @GetMapping("/horarios/meus")
    public ResponseEntity<List<HorarioResponse>> buscarMinhas(@AuthenticationPrincipal Usuario usuarioAtual) {
        return ResponseEntity.ok(horarioService.buscarMinhas(usuarioAtual));
    }

    /**
     * Lista os horários livres de um profissional, para escolha do paciente.
     *
     * @param profissionalId identificador do profissional
     * @return {@code 200 OK} com a lista de horários livres
     */
    @GetMapping("/horarios/profissional/{profissionalId}")
    public ResponseEntity<List<HorarioResponse>> buscarLivresPorProfissional(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(horarioService.buscarLivresPorProfissional(profissionalId));
    }
}
