# Testes manuais

Data base: 04/07/2026 09:00

## Paciente

1. Entrar na plataforma e logar com maria@email.com / 12345678
2. Buscar "Ana" → só aparece Dra. Ana Souza
3. Filtrar por Neurologia → aparece Dr. Carlos Lima
4. Clicar em Agendar, escolher 06/07 e um horário livre
5. Confirmar → mensagem "Consulta agendada!"
6. Abrir Histórico e ver a consulta nova
7. Tentar cancelar → modal mostra regra de 24h e limite de 3/mês
8. Confirmar cancelamento → status muda pra Cancelada

## Profissional

1. Logar com ana@consultafacil.com
2. Navegar pelo calendário e selecionar um dia
3. Ativar/desativar horários na grade
4. Filtrar consultas por data, nome do paciente e status

## Admin

1. Logar com admin@consultafacil.com
2. Conferir os números no dashboard (total, agendadas, canceladas)
3. Cadastrar um profissional novo → aparece na listagem
4. Desativar o profissional → some dos filtros
