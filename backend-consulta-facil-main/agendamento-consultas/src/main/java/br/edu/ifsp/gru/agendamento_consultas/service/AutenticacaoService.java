package br.edu.ifsp.gru.agendamento_consultas.service;

import br.edu.ifsp.gru.agendamento_consultas.dto.AutenticacaoResponse;
import br.edu.ifsp.gru.agendamento_consultas.dto.AdminProfissionalRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.CadastroRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.LoginRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.ProfissionalResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.repository.UsuarioRepository;
import br.edu.ifsp.gru.agendamento_consultas.security.JwtService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável pelo cadastro e autenticação de usuários.
 *
 * <p>Implementa {@link UserDetailsService} para que o Spring Security possa
 * carregar os dados do usuário pelo e-mail durante a autenticação via JWT.</p>
 */
@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Lazy AuthenticationManager authenticationManager
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Carrega o usuário pelo e-mail para uso interno do Spring Security.
     *
     * @param email e-mail do usuário
     * @return {@link UserDetails} correspondente ao e-mail
     * @throws UsernameNotFoundException se nenhum usuário com esse e-mail existir
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    /**
     * Registra um novo usuário como paciente ou profissional e retorna um token JWT.
     * Cadastro público como {@code ADMIN} não é permitido; contas administrativas
     * devem ser criadas diretamente no banco de dados.
     *
     * @param request dados de cadastro (nome de usuário, e-mail, senha, perfil e especialidade)
     * @return token JWT e informações básicas do usuário criado
     * @throws AppException {@code 409} se o e-mail ou o nome de usuário já estiverem em uso,
     *                       {@code 400} se o perfil informado for {@code ADMIN} ou se um
     *                       paciente informar especialidade
     */
    public AutenticacaoResponse registrar(CadastroRequest request) {
        if (request.papel() == Papel.ADMIN) {
            throw new AppException("Cadastro público como administrador não é permitido", HttpStatus.BAD_REQUEST);
        }
        if (request.papel() == Papel.PACIENTE && request.especialidade() != null && !request.especialidade().isBlank()) {
            throw new AppException("Paciente não pode informar especialidade", HttpStatus.BAD_REQUEST);
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new AppException("E-mail já cadastrado", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByNome(request.nomeUsuario())) {
            throw new AppException("Nome de usuário já cadastrado", HttpStatus.CONFLICT);
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nomeUsuario())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .papel(request.papel())
                .especialidade(request.papel() == Papel.PROFISSIONAL ? request.especialidade() : null)
                .build();

        usuarioRepository.save(usuario);
        return new AutenticacaoResponse(jwtService.generate(usuario), usuario.getNome(), usuario.getPapel().name());
    }

    /**
     * Lista todos os profissionais cadastrados, para que pacientes escolham com quem agendar.
     *
     * @return lista de profissionais
     */
    public List<ProfissionalResponse> listarProfissionais() {
        return usuarioRepository.findByPapel(Papel.PROFISSIONAL).stream()
                .map(this::paraProfissionalResponse)
                .toList();
    }

    public List<ProfissionalResponse> listarProfissionaisAtivos() {
        return usuarioRepository.findByPapel(Papel.PROFISSIONAL).stream()
                .filter(Usuario::isAtivo)
                .map(this::paraProfissionalResponse)
                .toList();
    }

    public ProfissionalResponse cadastrarProfissional(AdminProfissionalRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new AppException("E-mail já cadastrado", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByNome(request.nome())) {
            throw new AppException("Nome de usuário já cadastrado", HttpStatus.CONFLICT);
        }
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .papel(Papel.PROFISSIONAL)
                .especialidade(request.especialidade())
                .registroProfissional(request.registro())
                .ativo(true)
                .build();
        return paraProfissionalResponse(usuarioRepository.save(usuario));
    }

    public void desativarProfissional(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getPapel() == Papel.PROFISSIONAL)
                .orElseThrow(() -> new AppException("Profissional não encontrado", HttpStatus.NOT_FOUND));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    private ProfissionalResponse paraProfissionalResponse(Usuario usuario) {
        return new ProfissionalResponse(usuario.getId(), usuario.getNome(), usuario.getEspecialidade(),
                usuario.getRegistroProfissional(), usuario.isAtivo());
    }

    /**
     * Autentica um usuário com e-mail e senha e retorna um token JWT.
     *
     * @param request credenciais de login (e-mail e senha)
     * @return token JWT e informações básicas do usuário autenticado
     * @throws org.springframework.security.core.AuthenticationException se as credenciais forem inválidas
     */
    public AutenticacaoResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        return new AutenticacaoResponse(jwtService.generate(usuario), usuario.getNome(), usuario.getPapel().name());
    }
}
