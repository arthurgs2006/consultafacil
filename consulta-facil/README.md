# ConsultaFácil — Front-end

Front-end web (React + TypeScript + Vite) do ConsultaFácil, aplicativo de agendamento de consultas entre pacientes e profissionais de saúde. Consome a API REST do [backend Spring Boot](../backend-consulta-facil-main/README.md) via JWT.

## Stack

- **React 19** + **TypeScript**
- **Vite** — dev server e build
- **Sass/SCSS** — estilos (sem framework de UI)
- **Vitest** — testes unitários
- **lucide-react** — ícones
- **ESLint** (`typescript-eslint`, `eslint-plugin-react-hooks`)

## Estrutura do projeto

```
src/
├── App.tsx                 Roteamento simples entre telas (home/login/patient/professional/admin)
├── main.tsx                Entry point (ReactDOM + StrictMode)
├── types.ts                Tipos de domínio do front (Doctor, Appointment, Role, ...)
├── components/             Componentes de UI reutilizáveis (Avatar, BottomNav, Toast, StatusBadge...)
├── context/
│   └── AppContext.tsx      Estado global: sessão, profissionais, agendamentos, disponibilidade
├── features/
│   ├── home/               Landing page (marketing/intro do app)
│   ├── auth/                Login e cadastro
│   ├── patient/             Busca de profissionais, agendamento, histórico
│   ├── professional/        Agenda, gestão de horários, configurações
│   └── admin/                Painel administrativo (profissionais, agendamentos, configurações)
├── services/
│   ├── api.ts                Cliente HTTP da API (fetch + JWT), mapeamento DTO ⇄ modelo do front
│   ├── availability.ts       Consulta de horários livres a partir do estado local
│   └── appointmentRules.ts   Regras de validação (antecedência, conflito de horário) — com testes
├── utils/date.ts            Formatação e geração de chaves de data (yyyy-MM-dd)
├── data/mockData.ts          Constantes de UI (lista de horários possíveis `ALL_SLOTS`)
└── styles/                   SCSS por tela/feature, importado em `main.scss`
```

## Papéis e telas

A UI muda conforme o papel do usuário autenticado, devolvido pelo backend no login/cadastro:

- **Paciente** (`PatientPhone`) — busca profissionais por nome/especialidade, agenda consultas em horários livres, acompanha o histórico e cancela agendamentos.
- **Profissional** (`ProfessionalPhone`) — gerencia a própria grade de horários (ativa/desativa), visualiza e filtra a agenda de consultas, conclui atendimentos, e ajusta preferências (modo compacto, ocultar horários bloqueados).
- **Administrador** (`AdminPhone`) — cadastra/desativa profissionais, supervisiona todos os agendamentos da plataforma com filtros, e ajusta preferências de listagem.

Preferências de tela (modo compacto, ocultar itens) são salvas em `localStorage`, por dispositivo.

## Como rodar

Pré-requisito: o [backend](../backend-consulta-facil-main) rodando em `http://localhost:8080` (veja `../INTEGRATION.md` para instruções completas de execução conjunta).

```bash
npm install
npm run dev
```

Acesse `http://localhost:5173`. Em desenvolvimento, o Vite encaminha `/api/*` para `http://localhost:8080` (veja `vite.config.ts`), então não é necessário configurar CORS nem URL da API manualmente.

### Variáveis de ambiente

Veja `.env.example`:

```properties
VITE_API_URL=/api
```

- Em desenvolvimento, deixe vazio ou use `/api` (proxy do Vite).
- Em produção, aponte para a URL pública do backend, sem barra final.

## Scripts disponíveis

| Comando | Descrição |
|---|---|
| `npm run dev` | Sobe o servidor de desenvolvimento (Vite) |
| `npm run build` | Type-check (`tsc -b`) + build de produção |
| `npm run preview` | Serve o build de produção localmente |
| `npm run lint` | ESLint em todo o projeto |
| `npm test` | Roda os testes (Vitest) |

## Integração com o backend

Toda a comunicação HTTP passa por `src/services/api.ts`:

- Login/cadastro em `/autenticacao/*`, token JWT persistido em `localStorage` e enviado como `Authorization: Bearer <token>` em toda requisição autenticada.
- `AppContext` carrega profissionais, horários e agendamentos conforme o papel da sessão (paciente, profissional ou admin) e expõe ações (`schedule`, `cancelAppointment`, `toggleAvailability`, `addDoctor`, ...) que chamam a API e recarregam o estado.
- Erros da API (`{ "error": "..." }` ou `{ "errors": { campo: mensagem } }`) são convertidos em `ApiError` e exibidos ao usuário via toast ou mensagem inline no formulário.

Detalhes de payloads e endpoints: `../backend-consulta-facil-main/README.md`.

### Contas de teste

Veja `../INTEGRATION.md` para a lista completa de contas seedadas no backend (paciente, administrador e profissionais de várias especialidades), todas com senha `12345678`.

## Testes

```bash
npm test
```

Cobre as regras de validação de agendamento/cancelamento em `src/services/appointmentRules.ts` (antecedência mínima, conflito de horário, limite de cancelamentos). Testes manuais end-to-end (roteiro por papel) estão em `MANUAL_TESTS.md`.
