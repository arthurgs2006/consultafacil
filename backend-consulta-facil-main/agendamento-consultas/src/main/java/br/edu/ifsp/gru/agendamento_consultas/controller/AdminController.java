package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.ConsultaResponse;
import br.edu.ifsp.gru.agendamento_consultas.dto.AdminProfissionalRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.ProfissionalResponse;
import br.edu.ifsp.gru.agendamento_consultas.service.ConsultaService;
import br.edu.ifsp.gru.agendamento_consultas.service.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST exclusivo para administradores ({@code ROLE_ADMIN}).
 *
 * <p>Fornece visão global do sistema: profissionais cadastrados e todas as consultas
 * agendadas, com filtros. O acesso é restrito pela configuração do Spring Security
 * em {@code SecurityConfig}, que exige a role {@code ADMIN} para todas as rotas {@code /admin/**}.</p>
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AutenticacaoService autenticacaoService;
    private final ConsultaService consultaService;

    /**
     * Lista todos os profissionais cadastrados no sistema.
     *
     * @return {@code 200 OK} com a lista de profissionais
     */
    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalResponse>> listarProfissionais() {
        return ResponseEntity.ok(autenticacaoService.listarProfissionais());
    }

    @PostMapping("/profissionais")
    public ResponseEntity<ProfissionalResponse> cadastrarProfissional(
            @Valid @RequestBody AdminProfissionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autenticacaoService.cadastrarProfissional(request));
    }

    @DeleteMapping("/profissionais/{id}")
    public ResponseEntity<Void> desativarProfissional(@PathVariable Long id) {
        autenticacaoService.desativarProfissional(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todas as consultas do sistema com filtros opcionais.
     *
     * @param data           filtra consultas nesta data (opcional, formato {@code yyyy-MM-dd})
     * @param profissionalId filtra consultas deste profissional (opcional)
     * @return {@code 200 OK} com a lista de consultas
     */
    @GetMapping("/consultas")
    public ResponseEntity<List<ConsultaResponse>> listarConsultas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long profissionalId
    ) {
        return ResponseEntity.ok(consultaService.listarTodasAdmin(data, profissionalId));
    }
}
