package br.edu.ifsp.gru.agendamento_consultas.repository;

import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para operações de persistência da entidade {@link Usuario}.
 *
 * <p>Oferece buscas por e-mail e verificações de unicidade usadas durante
 * o cadastro e a autenticação.</p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo e-mail.
     *
     * @param email e-mail de autenticação
     * @return {@link Optional} contendo o usuário, se encontrado
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se já existe um usuário com o e-mail informado.
     *
     * @param email e-mail a verificar
     * @return {@code true} se o e-mail já estiver cadastrado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se já existe um usuário com o nome de exibição informado.
     *
     * @param nome nome de exibição a verificar
     * @return {@code true} se o nome já estiver em uso
     */
    boolean existsByNome(String nome);

    /**
     * Lista todos os usuários com o perfil informado.
     *
     * @param papel perfil desejado (ex: {@link Papel#PROFISSIONAL})
     * @return lista de usuários com o perfil
     */
    List<Usuario> findByPapel(Papel papel);
    List<Usuario> findByPapelAndAtivoTrue(Papel papel);
}
