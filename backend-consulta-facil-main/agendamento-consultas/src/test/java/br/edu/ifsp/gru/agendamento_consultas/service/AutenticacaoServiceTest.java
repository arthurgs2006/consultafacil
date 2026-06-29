package br.edu.ifsp.gru.agendamento_consultas.service;

import br.edu.ifsp.gru.agendamento_consultas.dto.AutenticacaoResponse;
import br.edu.ifsp.gru.agendamento_consultas.dto.CadastroRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.LoginRequest;
import br.edu.ifsp.gru.agendamento_consultas.dto.ProfissionalResponse;
import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.exception.AppException;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.repository.UsuarioRepository;
import br.edu.ifsp.gru.agendamento_consultas.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários de {@link AutenticacaoService}, com dependências mockadas via Mockito
 * (sem subir contexto Spring nem banco de dados).
 */
@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AutenticacaoService autenticacaoService;

    @BeforeEach
    void setUp() {
        autenticacaoService = new AutenticacaoService(usuarioRepository, passwordEncoder, jwtService, authenticationManager);
    }

    private Usuario criarUsuario(Long id, String nome, String email, Papel papel, String especialidade) {
        return Usuario.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .senha("hash")
                .papel(papel)
                .especialidade(especialidade)
                .build();
    }

    @Test
    void registrar_pacienteComSucesso() {
        CadastroRequest request = new CadastroRequest("joao123", "joao@teste.com", "senha1234", Papel.PACIENTE, null);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.existsByNome(request.nomeUsuario())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-hash");
        when(jwtService.generate(any(UserDetails.class))).thenReturn("token-jwt");

        AutenticacaoResponse response = autenticacaoService.registrar(request);

        assertThat(response.token()).isEqualTo("token-jwt");
        assertThat(response.nomeUsuario()).isEqualTo("joao123");
        assertThat(response.papel()).isEqualTo("PACIENTE");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getNome()).isEqualTo("joao123");
        assertThat(salvo.getEmail()).isEqualTo("joao@teste.com");
        assertThat(salvo.getSenha()).isEqualTo("senha-hash");
        assertThat(salvo.getPapel()).isEqualTo(Papel.PACIENTE);
        assertThat(salvo.getEspecialidade()).isNull();
    }

    @Test
    void registrar_profissionalComSucesso() {
        CadastroRequest request = new CadastroRequest("dra-ana", "ana@teste.com", "senha1234", Papel.PROFISSIONAL, "Cardiologia");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.existsByNome(request.nomeUsuario())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-hash");
        when(jwtService.generate(any(UserDetails.class))).thenReturn("token-jwt");

        AutenticacaoResponse response = autenticacaoService.registrar(request);

        assertThat(response.papel()).isEqualTo("PROFISSIONAL");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getEspecialidade()).isEqualTo("Cardiologia");
    }

    @Test
    void registrar_rejeitaCadastroComoAdmin() {
        CadastroRequest request = new CadastroRequest("admin", "admin@teste.com", "senha1234", Papel.ADMIN, null);

        assertThatThrownBy(() -> autenticacaoService.registrar(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void registrar_rejeitaEspecialidadeParaPaciente() {
        CadastroRequest request = new CadastroRequest("joao123", "joao@teste.com", "senha1234", Papel.PACIENTE, "Cardiologia");

        assertThatThrownBy(() -> autenticacaoService.registrar(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void registrar_rejeitaEmailDuplicado() {
        CadastroRequest request = new CadastroRequest("joao123", "joao@teste.com", "senha1234", Papel.PACIENTE, null);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> autenticacaoService.registrar(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_rejeitaNomeDeUsuarioDuplicado() {
        CadastroRequest request = new CadastroRequest("joao123", "joao@teste.com", "senha1234", Papel.PACIENTE, null);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.existsByNome(request.nomeUsuario())).thenReturn(true);

        assertThatThrownBy(() -> autenticacaoService.registrar(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_comSucesso() {
        LoginRequest request = new LoginRequest("joao@teste.com", "senha1234");
        Usuario usuario = criarUsuario(1L, "joao123", "joao@teste.com", Papel.PACIENTE, null);
        when(usuarioRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generate(usuario)).thenReturn("token-jwt");

        AutenticacaoResponse response = autenticacaoService.login(request);

        assertThat(response.token()).isEqualTo("token-jwt");
        assertThat(response.nomeUsuario()).isEqualTo("joao123");
        assertThat(response.papel()).isEqualTo("PACIENTE");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_usuarioNaoEncontradoAposAutenticar() {
        LoginRequest request = new LoginRequest("fantasma@teste.com", "senha1234");
        when(usuarioRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacaoService.login(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void listarProfissionais_retornaListaMapeadaCorretamente() {
        Usuario profissional = criarUsuario(2L, "dra-ana", "ana@teste.com", Papel.PROFISSIONAL, "Cardiologia");
        when(usuarioRepository.findByPapel(Papel.PROFISSIONAL)).thenReturn(List.of(profissional));

        List<ProfissionalResponse> resultado = autenticacaoService.listarProfissionais();

        assertThat(resultado).hasSize(1);
        ProfissionalResponse dto = resultado.get(0);
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.nome()).isEqualTo("dra-ana");
        assertThat(dto.especialidade()).isEqualTo("Cardiologia");
    }

    @Test
    void loadUserByUsername_comSucesso() {
        Usuario usuario = criarUsuario(1L, "joao123", "joao@teste.com", Papel.PACIENTE, null);
        when(usuarioRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(usuario));

        UserDetails resultado = autenticacaoService.loadUserByUsername("joao@teste.com");

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    void loadUserByUsername_naoEncontrado() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacaoService.loadUserByUsername("fantasma@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
