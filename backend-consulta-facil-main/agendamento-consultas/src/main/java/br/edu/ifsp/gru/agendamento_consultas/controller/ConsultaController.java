package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaResponse;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para agendamento, cancelamento e consulta de consultas.
 *
 * <p>Todas as rotas exigem autenticação via JWT.</p>
 */
@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    /**
     * Agenda uma consulta reservando um horário livre. O usuário autenticado deve ser paciente.
     *
     * @param request      identificador do horário a reservar
     * @param usuarioAtual usuário autenticado (será o paciente da consulta)
     * @return {@code 201 Created} com os dados da consulta agendada
     */
    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(
            @Valid @RequestBody ConsultaRequest request,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.agendar(request, usuarioAtual));
    }

    /**
     * Cancela uma consulta agendada, com no mínimo 24h de antecedência.
     *
     * @param id           identificador da consulta
     * @param usuarioAtual usuário autenticado (deve ser o paciente dono ou um administrador)
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        consultaService.cancelar(id, usuarioAtual);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/concluir")
    public ResponseEntity<ConsultaResponse> concluir(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        return ResponseEntity.ok(consultaService.concluir(id, usuarioAtual));
    }

    /**
     * Retorna o histórico de consultas do paciente autenticado.
     *
     * @param usuarioAtual usuário autenticado
     * @return {@code 200 OK} com a lista de consultas do paciente
     */
    @GetMapping("/historico")
    public ResponseEntity<List<ConsultaResponse>> historico(@AuthenticationPrincipal Usuario usuarioAtual) {
        return ResponseEntity.ok(consultaService.buscarHistorico(usuarioAtual));
    }

    /**
     * Retorna a agenda do profissional autenticado, com filtros opcionais.
     *
     * @param data         filtra consultas nesta data (opcional, formato {@code yyyy-MM-dd})
     * @param pacienteId   filtra consultas deste paciente (opcional)
     * @param usuarioAtual usuário autenticado (deve ser profissional)
     * @return {@code 200 OK} com a lista de consultas da agenda
     */
    @GetMapping("/agenda")
    public ResponseEntity<List<ConsultaResponse>> agenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long pacienteId,
            @AuthenticationPrincipal Usuario usuarioAtual
    ) {
        return ResponseEntity.ok(consultaService.buscarAgenda(usuarioAtual, data, pacienteId));
    }
}
