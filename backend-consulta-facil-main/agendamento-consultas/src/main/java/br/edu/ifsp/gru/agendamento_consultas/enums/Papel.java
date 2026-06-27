package br.edu.ifsp.gru.agendamento_consultas.enums;

/**
 * Perfis de acesso disponíveis no sistema.
 *
 * <ul>
 *   <li>{@link #PACIENTE} – paciente: agenda consultas e consulta seu histórico.</li>
 *   <li>{@link #PROFISSIONAL} – profissional de saúde: cadastra horários disponíveis e gerencia sua agenda.</li>
 *   <li>{@link #ADMIN} – administrador: possui visão global de profissionais e consultas.</li>
 * </ul>
 */
public enum Papel {
    PACIENTE,
    PROFISSIONAL,
    ADMIN
}
