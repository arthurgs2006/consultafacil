Tema 6 — Aplicativo de Agendamento de Consultas
Enunciado do Projeto

Desenvolva um aplicativo mobile para agendamento de consultas entre pacientes e profissionais de saúde (médicos, psicólogos, fisioterapeutas, etc.). A aplicação deve permitir o cadastro de usuários com papéis distintos (paciente, profissional e administrador), o gerenciamento de horários disponíveis pelos profissionais e o agendamento de consultas pelos pacientes. O app deve impedir sobreposição de agendamentos, registrar o histórico de consultas e permitir cancelamentos com regras de antecedência mínima. A interface deve variar conforme o papel do usuário autenticado.

1. Histórias de Usuário

🔐 Autenticação e Autorização

Como visitante, quero me cadastrar como paciente, profissional ou administrador para poder utilizar a plataforma de agendamento
Como usuário autenticado, quero fazer login e ter meu token JWT persistido no dispositivo para acessar os recursos da API com segurança
Como administrador, quero que a interface exiba telas e ações condizentes com o papel de cada usuário (PATIENT, PROFESSIONAL, ADMIN)
👩‍⚕️ Profissionais e Horários

Como profissional, quero cadastrar e editar meus horários disponíveis em uma tela de agenda para que pacientes possam me agendar
Como paciente, quero visualizar a lista de profissionais e seus horários disponíveis em um formato de grade ou lista para escolher o melhor horário para minha consulta
Como administrador, quero gerenciar o cadastro de profissionais em uma tela administrativa para manter a base de dados atualizada
📅 Agendamentos

Como paciente, quero agendar uma consulta com um profissional em um horário livre com seletor visual de data e hora para garantir meu atendimento
Como paciente, quero cancelar uma consulta com pelo menos 24h de antecedência, com confirmação via modal e validação no app
Como sistema, quero exibir erro visual ao tentar agendar em um horário já ocupado para evitar conflitos de agenda
Como profissional, quero visualizar minha agenda de consultas futuras e passadas em uma tela dedicada para me organizar com meus atendimentos
Como administrador, quero visualizar todos os agendamentos da plataforma em uma listagem filtrada para supervisionar a operação
📊 Consultas e Relatórios

Como paciente, quero consultar meu histórico de consultas para acompanhar minha jornada de atendimento
Como profissional, quero filtrar minha agenda por dia ou paciente para facilitar meu planejamento diário
Como administrador, quero filtrar consultas por data ou profissional para gerar relatórios e estatísticas
2. Requisitos de Testes

Testes unitários de hooks de agendamento e funções de validação de conflito de horário com Jest
3. Extras/Opcionais

Notificação local de lembrete de consulta (expo-notifications) com antecedência configurável
Tela de calendário visual para profissionais (react-native-calendars)
Regras adicionais: limite de cancelamentos por mês, bloqueio de agendamento de última hora

---

# Backend — agendamento-consultas

API REST em Spring Boot que implementa o sistema descrito acima: autenticação via JWT, cadastro de horários disponíveis por profissionais e agendamento de consultas por pacientes, sem sobreposição, com cancelamento com antecedência mínima e visão administrativa global.

> Guia voltado para quem vai consumir a API pelo front-end (mobile ou web): todos os exemplos de JSON abaixo usam exatamente os nomes de campo retornados/esperados pela API.

## Stack

- **Java 25**, **Spring Boot 4.1.0**
- **Spring Data JPA** + **Hibernate** — persistência
- **Spring Security 7** — autenticação stateless via JWT (sem sessão de servidor)
- **MySQL** (via `mysql-connector-j`) + **Flyway** (`flyway-core`, `flyway-mysql`) — versionamento de schema
- **jjwt** (0.12.6) — geração e validação de tokens JWT
- **Bean Validation** (`spring-boot-starter-validation`) — validação de DTOs de entrada
- **Lombok** — redução de boilerplate (getters/setters/construtores)

## Estrutura do projeto

```
src/main/java/br/edu/ifsp/gru/agendamento_consultas/
├── config/          SecurityConfig — cadeia de filtros, AuthenticationProvider/Manager, PasswordEncoder
├── controller/      AutenticacaoController, HorarioController, ConsultaController, AdminController
├── dto/             records de request/response
├── enums/           Papel (PACIENTE, PROFISSIONAL, ADMIN), StatusConsulta (AGENDADA, CANCELADA, CONCLUIDA)
├── exception/       AppException + GlobalExceptionHandler (@RestControllerAdvice)
├── model/           Usuario, Horario, Consulta (entidades JPA)
├── repository/      interfaces Spring Data JPA
├── security/        JwtService (geração/validação de token), JwtAuthFilter (filtro por requisição)
└── service/         AutenticacaoService, HorarioService, ConsultaService (regras de negócio)
```

As rotas HTTP também estão em português (`/autenticacao`, `/horarios`, `/consultas`, `/profissionais`, `/admin`), como detalhado na seção de endpoints.

Migrações Flyway em `src/main/resources/db/migration`:
`V1__create_users_table.sql`, `V2__create_availabilities_table.sql`, `V3__create_appointments_table.sql`.

## Configuração e execução local

Propriedades relevantes em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/consulta_facil?serverTimezone=America/Sao_Paulo&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=none
spring.flyway.locations=classpath:db/migration

app.jwt.secret=${JWT_SECRET:dev-secret-key-change-in-production-min-32-chars!!}
app.jwt.expiration=86400000
```

- Requer um MySQL acessível em `localhost:3307` com o schema `consulta_facil` criado; o Flyway aplica as migrações automaticamente na subida.
- `spring.jpa.hibernate.ddl-auto=none` — o schema é gerenciado exclusivamente pelo Flyway, não pelo Hibernate.
- `app.jwt.secret` pode ser sobrescrito pela variável de ambiente `JWT_SECRET`; o valor padrão serve apenas para desenvolvimento.
- `app.jwt.expiration` é o tempo de vida do token em milissegundos (86400000 = 24h).

Para rodar:

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` — todas as rotas abaixo são relativas a essa base (ex: `http://localhost:8080/autenticacao/login`).

## Autenticação (fluxo para o front)

1. Chame `POST /autenticacao/registrar` (novo usuário) ou `POST /autenticacao/login` (usuário existente).
2. Guarde o campo `token` da resposta (ex: `AsyncStorage`/`localStorage`/cookie seguro).
3. Em **toda outra requisição**, envie o header:
   ```
   Authorization: Bearer <token>
   ```
4. O token expira em 24h (`app.jwt.expiration`). Não há endpoint de refresh — expirado, o usuário precisa fazer login de novo (a API responde `401`).

A sessão é `STATELESS`: cada requisição é autenticada individualmente pelo `JwtAuthFilter`, que extrai o e-mail do token, carrega o `Usuario` via `UserDetailsService` (`AutenticacaoService`) e popula o `SecurityContextHolder`.

Regras de acesso (`SecurityConfig`):
- `/autenticacao/**` — público (não precisa de token).
- `/admin/**` — exige o papel `ADMIN`.
- demais rotas — exigem qualquer usuário autenticado; a distinção entre `PACIENTE` e `PROFISSIONAL` é feita nos services (retornam `403` quando o papel não pode executar a ação), não na configuração de rotas.

## Endpoints

### Autenticação (`/autenticacao`) — público

| Método | Rota | Body | Resposta |
|---|---|---|---|
| POST | `/autenticacao/registrar` | `CadastroRequest` | `201` `AutenticacaoResponse` |
| POST | `/autenticacao/login` | `LoginRequest` | `200` `AutenticacaoResponse` |

**`CadastroRequest`** (body de `/autenticacao/registrar`):
```json
{
  "nomeUsuario": "joao123",
  "email": "joao@teste.com",
  "senha": "minhasenha123",
  "papel": "PACIENTE",
  "especialidade": null
}
```
- `nomeUsuario` — obrigatório, nome de exibição único.
- `email` — obrigatório, formato válido, único.
- `senha` — obrigatório, mínimo 8 caracteres.
- `papel` — obrigatório, **`"PACIENTE"` ou `"PROFISSIONAL"`** (cadastro público como `"ADMIN"` é rejeitado com `400`).
- `especialidade` — opcional, só aceito quando `papel = "PROFISSIONAL"` (ex: `"Cardiologia"`). **Se `papel = "PACIENTE"`, não envie esse campo (ou envie `null`/vazio)** — a API responde `400` se vier preenchido para paciente.

**`LoginRequest`** (body de `/autenticacao/login`):
```json
{ "email": "joao@teste.com", "senha": "minhasenha123" }
```

**`AutenticacaoResponse`** (resposta de ambos):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "nomeUsuario": "joao123",
  "papel": "PACIENTE"
}
```

Erros: `409` e-mail ou nome de usuário já cadastrados; `400` se `papel = "ADMIN"` for informado no cadastro, ou se `especialidade` vier preenchida para `papel = "PACIENTE"`; `401` credenciais inválidas no login.

### Profissionais e horários (`/profissionais`, `/horarios`) — autenticado

| Método | Rota | Body/Params | Regra de acesso | Resposta |
|---|---|---|---|---|
| GET | `/profissionais` | – | qualquer autenticado | `200` `List<ProfissionalResponse>` |
| POST | `/horarios` | `HorarioRequest` | apenas `PROFISSIONAL` (vira dono) | `201` `HorarioResponse` |
| PUT | `/horarios/{id}` | `HorarioRequest` | dono do horário, e somente se não estiver reservado | `200` `HorarioResponse` |
| DELETE | `/horarios/{id}` | – | dono do horário, e somente se não estiver reservado | `204` |
| GET | `/horarios/meus` | – | apenas `PROFISSIONAL` (lista os próprios horários) | `200` `List<HorarioResponse>` |
| GET | `/horarios/profissional/{profissionalId}` | – | qualquer autenticado (lista horários livres do profissional) | `200` `List<HorarioResponse>` |

**`ProfissionalResponse`** (item de `GET /profissionais`):
```json
{ "id": 3, "nome": "Dra. Ana Souza", "especialidade": "Cardiologia" }
```

**`HorarioRequest`** (body de `POST`/`PUT` `/horarios`):
```json
{ "data": "2026-08-10", "horaInicio": "09:00:00", "horaFim": "09:30:00" }
```
- `data` — obrigatório, formato `yyyy-MM-dd`, não pode ser no passado.
- `horaInicio` / `horaFim` — obrigatórios, formato `HH:mm:ss` (ou `HH:mm`), `horaFim` deve ser após `horaInicio`.

**`HorarioResponse`** (resposta de `/horarios/**`):
```json
{
  "id": 10,
  "profissionalId": 3,
  "nomeProfissional": "Dra. Ana Souza",
  "data": "2026-08-10",
  "horaInicio": "09:00:00",
  "horaFim": "09:30:00",
  "reservado": false
}
```

Regras de negócio (`HorarioService`):
- Apenas usuários com `papel = PROFISSIONAL` podem criar/editar/excluir horários.
- Horário que se sobrepõe a outro já cadastrado pelo mesmo profissional na mesma data é rejeitado (`409`).
- Horário já reservado (`reservado = true`) não pode ser editado nem excluído (`409`).
- Edição/exclusão só pelo profissional dono (`403` caso contrário); horário inexistente → `404`.

### Consultas (`/consultas`) — autenticado

| Método | Rota | Body/Params | Regra de acesso | Resposta |
|---|---|---|---|---|
| POST | `/consultas` | `ConsultaRequest` | apenas `PACIENTE` (vira o paciente da consulta) | `201` `ConsultaResponse` |
| DELETE | `/consultas/{id}` | – | paciente dono da consulta (ou admin) | `204` |
| GET | `/consultas/historico` | – | qualquer autenticado (histórico do próprio paciente) | `200` `List<ConsultaResponse>` |
| GET | `/consultas/agenda` | query: `data`, `pacienteId` (opcionais) | apenas `PROFISSIONAL` (agenda própria) | `200` `List<ConsultaResponse>` |

**`ConsultaRequest`** (body de `POST /consultas`):
```json
{ "horarioId": 10 }
```
- `horarioId` — obrigatório, id de um `Horario` livre (`reservado = false`) retornado por `/horarios/profissional/{id}`.

**`ConsultaResponse`** (resposta de `/consultas/**`):
```json
{
  "id": 7,
  "nomePaciente": "joao123",
  "nomeProfissional": "Dra. Ana Souza",
  "especialidadeProfissional": "Cardiologia",
  "data": "2026-08-10",
  "horaInicio": "09:00:00",
  "horaFim": "09:30:00",
  "status": "AGENDADA",
  "criadoEm": "2026-07-05T10:15:30"
}
```
- `status` é sempre um destes três valores: `"AGENDADA"`, `"CANCELADA"`, `"CONCLUIDA"`.

Regras de negócio (`ConsultaService`):
- Agendar reserva o `Horario` informado (`reservado = true`); horário inexistente → `404`, já reservado → `409` (evita sobreposição).
- Cancelamento só pelo próprio paciente ou por um `ADMIN` (`403` caso contrário); consulta que não esteja `AGENDADA` → `409`; menos de 24h de antecedência para o horário da consulta → `409`.
- Ao cancelar, o horário volta a ficar disponível (`reservado = false`) para novo agendamento.
- Histórico (`/consultas/historico`) lista todas as consultas do paciente autenticado, independente do status.
- Agenda (`/consultas/agenda`) aceita filtro por `data` (query, `yyyy-MM-dd`) e/ou `pacienteId` (query).

### Administração (`/admin`) — exige papel `ADMIN`

| Método | Rota | Params | Resposta |
|---|---|---|---|
| GET | `/admin/profissionais` | – | `200` `List<ProfissionalResponse>` — todos os profissionais cadastrados |
| GET | `/admin/consultas` | `data` (opcional), `profissionalId` (opcional) | `200` `List<ConsultaResponse>` — todas as consultas do sistema, com filtros |

Não existe cadastro público de `ADMIN`: contas administrativas precisam ser criadas direto no banco (`role = 'ADMIN'` na tabela `users`).

## Tratamento de erros

Nenhuma exceção chega ao cliente (ou ao console) como página padrão do Spring/Tomcat ou stack trace bruto — tudo é convertido em JSON com status HTTP apropriado. O front pode sempre tentar ler `body.error` (string) ou `body.errors` (objeto campo→mensagem) para exibir feedback ao usuário.

`GlobalExceptionHandler` (`@RestControllerAdvice`) cobre as exceções que ocorrem durante o processamento do controller/service:

- `AppException` (lançada pelos services com um `HttpStatus` específico) → `{ "error": "<mensagem>" }` com o status definido (`404`, `403`, `409`, etc.).
- `MethodArgumentNotValidException` (falha de `@Valid` em DTOs) → `400` com `{ "errors": { "campo": "mensagem" } }` — ex: `{ "errors": { "email": "must be a well-formed email address" } }`.
- `AuthenticationException` (ex: credenciais inválidas no `/autenticacao/login`) → `401` `{ "error": "Credenciais inválidas" }`.
- `AccessDeniedException` (ex: `@PreAuthorize` negado) → `403` `{ "error": "Acesso negado" }`.
- `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException`, `HttpRequestMethodNotSupportedException`, `NoHandlerFoundException` (parâmetro ausente/inválido, verbo HTTP errado, rota inexistente) → `400` `{ "error": "Requisição inválida" }`.
- Qualquer outra `Exception` não mapeada → `500` `{ "error": "Erro interno no servidor" }`, com o stack trace completo registrado apenas no log do servidor (nunca exposto ao cliente).

Já as rejeições feitas pelo próprio filtro de segurança — antes da requisição alcançar um controller, como token ausente/inválido em rota protegida ou falta do papel `ADMIN` em `/admin/**` — não passam pelo `@RestControllerAdvice`. Elas são tratadas em `SecurityConfig` via `authenticationEntryPoint`/`accessDeniedHandler`, retornando o mesmo formato: `401` `{ "error": "Não autenticado" }` e `403` `{ "error": "Acesso negado" }`, respectivamente.

## Modelo de dados

- **Usuario**: `id`, `nome` (nome de exibição único), `email` (único), `senha` (hash BCrypt, nunca retornada em nenhum DTO), `papel` (`PACIENTE`/`PROFISSIONAL`/`ADMIN`), `especialidade` (opcional, só relevante para `PROFISSIONAL`). Implementa `UserDetails` — `email` é o identificador de login; a authority é `ROLE_PACIENTE`, `ROLE_PROFISSIONAL` ou `ROLE_ADMIN`.
- **Horario**: `id`, `profissional` (`Usuario`), `data`, `horaInicio`, `horaFim`, `reservado` (boolean), `criadoEm`. Representa um horário que o profissional disponibilizou para agendamento.
- **Consulta**: `id`, `paciente` (`Usuario`), `profissional` (`Usuario`), `horario` (`Horario`, FK única — um horário só vira uma consulta), `status` (`AGENDADA`/`CANCELADA`/`CONCLUIDA`), `criadoEm`. Data e horário da consulta são obtidos via `horario`, sem duplicação de dados.

## Resumo rápido para o front (cola e usa)

| Enum | Valores possíveis (string exata) |
|---|---|
| `papel` / role do usuário | `"PACIENTE"`, `"PROFISSIONAL"`, `"ADMIN"` |
| `status` da consulta | `"AGENDADA"`, `"CANCELADA"`, `"CONCLUIDA"` |

| Formato | Exemplo |
|---|---|
| Datas (`data`) | `"2026-08-10"` (ISO `yyyy-MM-dd`) |
| Horas (`horaInicio`/`horaFim`) | `"09:00:00"` (ISO `HH:mm:ss`) |
| Data e hora (`criadoEm`) | `"2026-07-05T10:15:30"` (ISO `yyyy-MM-ddTHH:mm:ss`) |
