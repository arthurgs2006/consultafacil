package br.edu.ifsp.gru.agendamento_consultas.dto;

import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO com os dados necessários para o cadastro de um novo usuário.
 *
 * @param nomeUsuario nome de exibição único do usuário
 * @param email       e-mail único, utilizado como identificador de autenticação
 * @param senha       senha com no mínimo 8 caracteres
 * @param papel       perfil desejado ({@code PACIENTE} ou {@code PROFISSIONAL}; cadastro público como {@code ADMIN} não é permitido)
 * @param especialidade especialidade do profissional (ex: "Cardiologia"); considerada apenas quando {@code papel == PROFISSIONAL}
 */
public record CadastroRequest(
        @NotBlank String nomeUsuario,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String senha,
        @NotNull Papel papel,
        String especialidade
) {}
