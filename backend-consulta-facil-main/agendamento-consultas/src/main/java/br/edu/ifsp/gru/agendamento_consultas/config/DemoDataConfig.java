package br.edu.ifsp.gru.agendamento_consultas.config;

import br.edu.ifsp.gru.agendamento_consultas.enums.Papel;
import br.edu.ifsp.gru.agendamento_consultas.model.Horario;
import br.edu.ifsp.gru.agendamento_consultas.model.Usuario;
import br.edu.ifsp.gru.agendamento_consultas.repository.HorarioRepository;
import br.edu.ifsp.gru.agendamento_consultas.repository.UsuarioRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class DemoDataConfig {

    @Bean
    @ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
    ApplicationRunner seedDemoData(
            UsuarioRepository usuarios,
            HorarioRepository horarios,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            criarUsuarioSeAusente(usuarios, passwordEncoder, "Administrador", "admin@consultafacil.com",
                    Papel.ADMIN, null, null);
            criarUsuarioSeAusente(usuarios, passwordEncoder, "Maria Silva", "maria@consultafacil.com",
                    Papel.PACIENTE, null, null);
            Usuario profissional = criarUsuarioSeAusente(usuarios, passwordEncoder, "Dra. Ana Souza",
                    "ana@consultafacil.com", Papel.PROFISSIONAL, "Cardiologia", "CRM-SP 184521");

            if (horarios.findByProfissional(profissional).isEmpty()) {
                LocalDate primeiroDia = LocalDate.now().plusDays(1);
                for (int dia = 0; dia < 5; dia++) {
                    LocalDate data = primeiroDia.plusDays(dia);
                    for (int hora : List.of(9, 10, 11, 14, 15, 16)) {
                        horarios.save(Horario.builder()
                                .profissional(profissional)
                                .data(data)
                                .horaInicio(LocalTime.of(hora, 0))
                                .horaFim(LocalTime.of(hora + 1, 0))
                                .build());
                    }
                }
            }
        };
    }

    private Usuario criarUsuarioSeAusente(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            String nome,
            String email,
            Papel papel,
            String especialidade,
            String registro
    ) {
        return usuarios.findByEmail(email).orElseGet(() -> usuarios.save(Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(encoder.encode("12345678"))
                .papel(papel)
                .especialidade(especialidade)
                .registroProfissional(registro)
                .ativo(true)
                .build()));
    }
}
