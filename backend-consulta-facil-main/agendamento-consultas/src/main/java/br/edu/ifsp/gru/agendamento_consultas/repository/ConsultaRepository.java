package br.edu.ifsp.gru.agendamento_consultas.repository;

import br.edu.ifsp.gru.agendamento_consultas.model.Consulta;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositório JPA para operações de persistência da entidade {@link Consulta}.
 */
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /**
     * Retorna o histórico de consultas de um paciente.
     *
     * @param paciente paciente consultado
     * @return lista de consultas do paciente
     */
    List<Consulta> findByPaciente(Usuario paciente);

    /**
     * Retorna a agenda de um profissional com filtros opcionais por data e paciente.
     *
     * @param profissional profissional consultado
     * @param data         filtra consultas nesta data (opcional)
     * @param pacienteId   filtra consultas deste paciente (opcional)
     * @return lista de consultas que atendem aos critérios
     */
    @Query("""
            SELECT a FROM Consulta a
            WHERE a.profissional = :profissional
              AND (:data IS NULL OR a.horario.data = :data)
              AND (:pacienteId IS NULL OR a.paciente.id = :pacienteId)
            """)
    List<Consulta> findByProfissionalComFiltros(
            @Param("profissional") Usuario profissional,
            @Param("data") LocalDate data,
            @Param("pacienteId") Long pacienteId
    );

    /**
     * Retorna todas as consultas do sistema com filtros opcionais por data e profissional
     * (uso administrativo).
     *
     * @param data           filtra consultas nesta data (opcional)
     * @param profissionalId filtra consultas deste profissional (opcional)
     * @return lista de consultas que atendem aos critérios
     */
    @Query("""
            SELECT a FROM Consulta a
            WHERE (:data IS NULL OR a.horario.data = :data)
              AND (:profissionalId IS NULL OR a.profissional.id = :profissionalId)
            """)
    List<Consulta> buscarTodasComFiltros(
            @Param("data") LocalDate data,
            @Param("profissionalId") Long profissionalId
    );
}
