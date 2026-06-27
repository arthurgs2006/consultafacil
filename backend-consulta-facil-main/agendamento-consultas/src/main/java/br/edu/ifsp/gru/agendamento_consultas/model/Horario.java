package br.edu.ifsp.gru.agendamento_consultas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidade que representa um horário disponível cadastrado por um profissional.
 *
 * <p>Um paciente agenda uma consulta reservando um horário livre ({@code reservado = false}).
 * Quando reservado, o horário não pode mais ser editado, excluído ou agendado por outro
 * paciente até que a consulta correspondente seja cancelada.</p>
 */
@Entity
@Table(name = "availabilities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Horario {

    /** Identificador único gerado pelo banco de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Profissional dono deste horário. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Usuario profissional;

    /** Data do horário disponível. */
    @Column(nullable = false)
    private LocalDate data;

    /** Horário de início. */
    @Column(name = "start_time", nullable = false)
    private LocalTime horaInicio;

    /** Horário de término. */
    @Column(name = "end_time", nullable = false)
    private LocalTime horaFim;

    /** Indica se o horário já foi reservado por um paciente. */
    @Column(nullable = false)
    @Builder.Default
    private boolean reservado = false;

    /** Data e hora de criação, preenchida automaticamente pelo Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
