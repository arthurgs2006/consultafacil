package br.edu.ifsp.gru.agendamento_consultas.model;

import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidade que representa um usuário do sistema.
 *
 * <p>Implementa {@link UserDetails} para integração com o Spring Security.
 * O e-mail é utilizado como identificador de autenticação ({@code getUsername()}),
 * enquanto {@code nome} armazena o nome de exibição escolhido pelo usuário.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    /** Identificador único gerado pelo banco de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome de exibição único do usuário (coluna {@code username} no banco). */
    @Column(name = "username", unique = true, nullable = false)
    private String nome;

    /** E-mail único, utilizado como identificador de autenticação. */
    @Column(unique = true, nullable = false)
    private String email;

    /** Senha armazenada em hash BCrypt. */
    @Column(nullable = false)
    private String senha;

    /** Perfil de acesso do usuário ({@link Papel#PACIENTE}, {@link Papel#PROFISSIONAL} ou {@link Papel#ADMIN}). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    /** Especialidade do profissional (ex: "Cardiologia"). Relevante apenas quando {@code papel == PROFISSIONAL}. */
    private String especialidade;

    @Column(name = "professional_registration")
    private String registroProfissional;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Retorna o e-mail como identificador de autenticação exigido pelo Spring Security.
     *
     * @return e-mail do usuário
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Retorna a senha em hash exigida pelo contrato {@link UserDetails}.
     *
     * @return senha do usuário em hash BCrypt
     */
    @Override
    public String getPassword() {
        return senha;
    }

    /**
     * Retorna as permissões do usuário com base no seu perfil de acesso.
     *
     * @return lista com a authority {@code ROLE_PACIENTE}, {@code ROLE_PROFISSIONAL} ou {@code ROLE_ADMIN}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papel.name()));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return ativo; }
}
