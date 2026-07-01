# Roteiro de validação manual

Ambiente demonstrativo: `DEMO_NOW = 04/07/2026 09:00`.

## Fluxo do paciente

1. Na página inicial, selecionar **Começar agora**.
2. Manter o perfil **Paciente** e entrar com `maria@email.com` / `12345678`.
3. Buscar por `Ana` e confirmar que apenas **Dra. Ana Souza** aparece.
4. Limpar a busca, filtrar por **Neurologia** e confirmar **Dr. Carlos Lima**.
5. Abrir **Agendar**, selecionar `06/07/2026` e um horário habilitado.
6. Confirmar e verificar a mensagem **Consulta agendada!**.
7. Abrir **Histórico** e localizar a nova consulta.
8. Solicitar o cancelamento e verificar o modal com as regras de 24h e limite mensal.
9. Confirmar e verificar a mensagem visual de sucesso e o status **Cancelada**.
10. Para validar erro de antecedência, usar uma consulta a menos de 24h de `DEMO_NOW`; o modal deve permanecer aberto e explicar o bloqueio.

## Fluxo do profissional

1. Entrar com perfil **Profissional**.
2. Navegar entre os meses e selecionar um dia; a data deve ser refletida na grade e no filtro da agenda.
3. Ativar/desativar um horário e verificar `aria-pressed`, cor e mensagem de feedback.
4. Acessar o paciente e confirmar que o horário alterado aparece/desaparece no agendamento.
5. Filtrar consultas por data, trecho do nome do paciente e cada status.

## Fluxo administrativo

1. Entrar com perfil **Admin**.
2. Conferir os indicadores de total, agendadas e canceladas contra a listagem.
3. Cadastrar um profissional com nome, especialidade e registro; verificar mensagem e novo card.
4. Excluir o profissional e confirmar sua remoção da listagem e dos filtros.

## Resultado da revisão

- Login e seleção de perfil: aprovado.
- Busca, filtro, agendamento e histórico: aprovado.
- Confirmação e feedback de cancelamento: aprovado.
- Calendário, disponibilidade e filtros profissionais: aprovado.
- Indicadores, cadastro, listagem e exclusão administrativa: aprovado.
- Layout responsivo e controles com rótulos básicos: aprovado em revisão de código e build.
