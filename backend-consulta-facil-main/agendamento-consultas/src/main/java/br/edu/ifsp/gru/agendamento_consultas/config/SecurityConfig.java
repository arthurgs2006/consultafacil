package br.edu.ifsp.gru.agendamento_consultas.config;

import br.edu.ifsp.gru.agendamento_consultas.security.JwtAuthFilter;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * Configuração principal do Spring Security.
 *
 * <p>Define:
 * <ul>
 *   <li>Política de sessão {@code STATELESS} (sem estado no servidor, baseada em JWT).</li>
 *   <li>Rotas públicas: {@code /auth/**}.</li>
 *   <li>Rotas restritas a administradores: {@code /admin/**}.</li>
 *   <li>Todas as demais rotas exigem autenticação.</li>
 *   <li>Filtro {@link JwtAuthFilter} inserido antes do filtro padrão de autenticação.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configura a cadeia de filtros de segurança HTTP.
     *
     * @param http objeto de configuração do Spring Security
     * @param jwtAuthFilter filtro JWT inserido antes da autenticação padrão
     * @param authenticationProvider provedor de autenticação usado na cadeia
     * @return {@link SecurityFilterChain} configurada
     * @throws Exception se a configuração falhar
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            AuthenticationProvider authenticationProvider,
            ObjectMapper objectMapper,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/autenticacao/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(response, objectMapper, 401, "Não autenticado"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(response, objectMapper, 403, "Acesso negado"))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String origins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(origins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Escreve uma resposta JSON padronizada {@code {"error": "<mensagem>"}} para rejeições
     * feitas diretamente pelo filtro de segurança (token ausente/inválido ou role insuficiente),
     * que ocorrem antes da requisição chegar aos controllers e por isso não passam pelo
     * {@link br.edu.ifsp.gru.agendamento_consultas.exception.GlobalExceptionHandler}.
     */
    private static void writeJsonError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }

    /**
     * Configura o provedor de autenticação que valida credenciais contra o banco de dados.
     * No Spring Security 7, o {@link UserDetailsService} é passado diretamente no construtor.
     *
     * @param userDetailsService serviço que carrega os dados do usuário pelo e-mail
     * @return {@link DaoAuthenticationProvider} com o {@link UserDetailsService} e o encoder configurados
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expõe o {@link AuthenticationManager} como bean para ser injetado no {@code AutenticacaoService}.
     *
     * @param config configuração de autenticação do Spring
     * @return gerenciador de autenticação
     * @throws Exception se a criação falhar
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define o encoder de senhas utilizado para hash BCrypt.
     *
     * @return instância de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
