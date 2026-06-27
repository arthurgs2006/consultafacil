package br.edu.ifsp.gru.agendamento_consultas.dto;

/**
 * DTO com os dados públicos de um profissional, usado pelo paciente para escolher com quem agendar.
 *
 * @param id            identificador único do profissional
 * @param nome          nome de exibição do profissional
 * @param especialidade especialidade do profissional (pode ser {@code null})
 */
public record ProfissionalResponse(Long id, String nome, String especialidade, String registro, boolean ativo) {
    public ProfissionalResponse(Long id, String nome, String especialidade) {
        this(id, nome, especialidade, null, true);
    }
}
