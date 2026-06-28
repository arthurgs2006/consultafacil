package br.edu.ifsp.gru.agendamento_consultas.controller;

import br.edu.ifsp.gru.agendamento_consultas.dto.AutenticacaoResponse;
import br.edu.ifsp.gru.agendamento_consultas.dto.CadastroRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.LoginRequest;
import br.edu.ifsp.gru.agendamento_consultas.service.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para os endpoints públicos de autenticação.
 *
 * <p>Rotas disponíveis sem autenticação:
 * <ul>
 *   <li>{@code POST /autenticacao/registrar} – cadastro de novo usuário</li>
 *   <li>{@code POST /autenticacao/login} – autenticação e obtenção do token JWT</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/autenticacao")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    /**
     * Registra um novo usuário e retorna um token JWT.
     *
     * @param request dados de cadastro (nome de usuário, e-mail e senha)
     * @return {@code 201 Created} com o token e informações do usuário
     */
    @PostMapping("/registrar")
    public ResponseEntity<AutenticacaoResponse> registrar(@Valid @RequestBody CadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autenticacaoService.registrar(request));
    }

    /**
     * Autentica um usuário existente e retorna um token JWT.
     *
     * @param request credenciais de login (e-mail e senha)
     * @return {@code 200 OK} com o token e informações do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<AutenticacaoResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacaoService.login(request));
    }
}
