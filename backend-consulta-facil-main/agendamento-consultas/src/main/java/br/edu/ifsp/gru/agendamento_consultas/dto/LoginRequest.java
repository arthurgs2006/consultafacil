package br.edu.ifsp.gru.agendamento_consultas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO com as credenciais para autenticação de um usuário existente.
 *
 * @param email e-mail cadastrado
 * @param senha senha do usuário
 */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String senha
) {}