package br.edu.ifsp.gru.agendamento_consultas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminProfissionalRequest(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String senha,
        @NotBlank String especialidade,
        @NotBlank String registro
) {}
