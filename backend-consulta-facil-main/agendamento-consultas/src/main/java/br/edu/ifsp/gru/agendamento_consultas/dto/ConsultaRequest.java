package br.edu.ifsp.gru.agendamento_consultas.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para agendamento de uma consulta.
 *
 * @param horarioId identificador do horário livre a ser reservado (obrigatório)
 */
public record ConsultaRequest(@NotNull Long horarioId) {}
