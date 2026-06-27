package br.edu.ifsp.gru.agendamento_consultas.dto;

import br.edu.ifsp.gru.agendamento_consultas.enums.StatusConsulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO com os dados de resposta de uma consulta agendada.
 *
 * @param id                        identificador único da consulta
 * @param nomePaciente              nome de exibição do paciente
 * @param nomeProfissional          nome de exibição do profissional
 * @param especialidadeProfissional especialidade do profissional (pode ser {@code null})
 * @param data                      data da consulta
 * @param horaInicio                horário de início
 * @param horaFim                   horário de término
 * @param status                    estado atual da consulta
 * @param criadoEm                  data e hora em que a consulta foi agendada
 */
public record ConsultaResponse(
        Long id,
        String nomePaciente,
        String nomeProfissional,
        String especialidadeProfissional,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        StatusConsulta status,
        LocalDateTime criadoEm
) {}
