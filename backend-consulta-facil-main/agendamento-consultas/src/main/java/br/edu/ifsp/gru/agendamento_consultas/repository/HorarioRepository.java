package br.edu.ifsp.gru.agendamento_consultas.repository;

import br.edu.ifsp.gru.agendamento_consultas.model.Horario;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repositório JPA para operações de persistência da entidade {@link Horario}.
 */
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    /**
     * Lista todos os horários (livres ou reservados) cadastrados pelo profissional.
     *
     * @param profissional profissional dono dos horários
     * @return lista de horários do profissional
     */
    List<Horario> findByProfissional(Usuario profissional);

    /**
     * Lista os horários livres de um profissional, para escolha do paciente.
     *
     * @param profissional profissional consultado
     * @return lista de horários ainda não reservados
     */
    List<Horario> findByProfissionalAndReservadoFalse(Usuario profissional);

    /**
     * Verifica se já existe um horário do profissional que se sobrepõe ao intervalo informado
     * na mesma data. Usado para impedir o cadastro de horários conflitantes.
     *
     * @param profissional profissional dono do horário
     * @param data         data do horário
     * @param horaInicio   início do intervalo a validar
     * @param horaFim      término do intervalo a validar
     * @param excludeId    identificador a ignorar na checagem (usado em edições); pode ser {@code null}
     * @return {@code true} se houver sobreposição com outro horário já cadastrado
     */
    @Query("""
            SELECT COUNT(a) > 0 FROM Horario a
            WHERE a.profissional = :profissional
              AND a.data = :data
              AND a.horaInicio < :horaFim
              AND a.horaFim > :horaInicio
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    boolean existeSobreposicao(
            @Param("profissional") Usuario profissional,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim,
            @Param("excludeId") Long excludeId
    );
}
