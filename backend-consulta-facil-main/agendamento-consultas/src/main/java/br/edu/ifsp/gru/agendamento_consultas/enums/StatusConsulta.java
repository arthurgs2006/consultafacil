package br.edu.ifsp.gru.agendamento_consultas.enums;

/**
 * Estados possíveis para uma consulta agendada.
 *
 * <ul>
 *   <li>{@link #AGENDADA} – consulta agendada e ainda não realizada.</li>
 *   <li>{@link #CANCELADA} – consulta cancelada pelo paciente ou por um administrador.</li>
 *   <li>{@link #CONCLUIDA} – consulta já realizada.</li>
 * </ul>
 */
public enum StatusConsulta {
    AGENDADA,
    CANCELADA,
    CONCLUIDA
}
