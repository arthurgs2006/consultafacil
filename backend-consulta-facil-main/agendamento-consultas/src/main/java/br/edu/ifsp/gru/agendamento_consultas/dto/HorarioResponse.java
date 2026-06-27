package br.edu.ifsp.gru.agendamento_consultas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO com os dados de resposta de um horário disponível.
 *
 * @param id               identificador único do horário
 * @param profissionalId   identificador do profissional dono do horário
 * @param nomeProfissional nome de exibição do profissional
 * @param data             data do horário
 * @param horaInicio       horário de início
 * @param horaFim          horário de término
 * @param reservado        indica se o horário já foi reservado
 */
public record HorarioResponse(
        Long id,
        Long profissionalId,
        String nomeProfissional,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean reservado
) {}
