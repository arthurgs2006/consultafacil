package br.edu.ifsp.gru.agendamento_consultas.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para criação ou atualização de um horário disponível por um profissional.
 *
 * @param data       data do horário (obrigatório, não pode ser no passado)
 * @param horaInicio horário de início (obrigatório)
 * @param horaFim    horário de término (obrigatório, deve ser após o início)
 */
public record   HorarioRequest(
        @NotNull LocalDate data,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFim
) {}
