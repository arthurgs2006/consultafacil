package br.edu.ifsp.gru.agendamento_consultas.model;

import br.edu.ifsp.gru.agendamento_consultas.enums.StatusConsulta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma consulta agendada por um paciente com um profissional.
 *
 * <p>Toda consulta reserva um {@link Horario} específico. Data e horário da consulta
 * são obtidos a partir do horário reservado, evitando duplicar essa informação aqui.</p>
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    /** Identificador único gerado pelo banco de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Paciente que agendou a consulta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Usuario paciente;

    /** Profissional que atenderá a consulta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Usuario profissional;

    /** Horário reservado para esta consulta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false)
    private Horario horario;

    /** Estado atual da consulta. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    /** Data e hora de criação, preenchida automaticamente pelo Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
