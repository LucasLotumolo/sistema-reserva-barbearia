# US-08 — Avaliar atendimento: testes funcionais

Requisitos e casos de teste derivados das regras RN-21 a RN-24 por particionamento em classes
de equivalência (CE) e análise do valor limite (AVL). A menor unidade nas fronteiras de tempo
é 1 minuto.

Requisitos marcados como cobertos já são exercitados pelos testes de TDD dos cenários e não
geram casos novos.

## Requisitos de teste

| ID | Requisito de teste | Critério | Coberto por TDD |
|---|---|---|---|
| US08-R1 | Nota abaixo de 1 é rejeitada | CE inválida | 8.3 (nota 0) |
| US08-R2 | Nota entre 1 e 5 é aceita | CE válida | 8.1 (nota 4), 8.2 (1 e 5) |
| US08-R3 | Nota acima de 5 é rejeitada | CE inválida | 8.4 (nota 6) |
| US08-R4 | Nota nos limites 0, 1, 5 e 6 | AVL | 8.2, 8.3, 8.4 |
| US08-R5 | Avaliação antes do fim do atendimento é rejeitada | CE inválida | 8.5 (durante) |
| US08-R6 | Avaliação a partir do fim do atendimento é aceita | CE válida | 8.1 (30 min depois) |
| US08-R7 | Instante da avaliação nos limites fim − 1 min, fim e fim + 1 min | AVL | — |
| US08-R8 | Avaliação antes do início do atendimento é rejeitada | CE inválida | — |
| US08-R9 | Agendamento `CONFIRMADO` pode ser avaliado | CE válida | 8.1 |
| US08-R10 | Agendamentos `CANCELADO` e `EXPIRADO` não podem ser avaliados | CE inválida | 8.7 |
| US08-R11 | Agendamento `AGENDADO` não pode ser avaliado | CE inválida | — |
| US08-R12 | Segunda avaliação é rejeitada e a original é mantida | CE inválida | 8.6 |
| US08-R13 | Avaliação rejeitada não registra nada no agendamento | CE | 8.3, 8.4, 8.5, 8.7 |
| US08-R14 | O instante registrado é o instante da avaliação | CE válida | 8.1 |
| US08-R15 | Com mais de uma regra violada, prevalece a ordem RN-21 → RN-22 → RN-23 → RN-24 | CE | — |
| US08-R16 | O serviço aplica a avaliação com o instante do relógio e salva o agendamento | CE válida | serviço (TDD) |
| US08-R17 | Agendamento inexistente é rejeitado com erro de domínio | CE inválida | — |
| US08-R18 | Avaliação rejeitada no serviço não é salva | CE inválida | — |

US08-R1 a US08-R15 se aplicam a `Agendamento.avaliar` e `Avaliacao`; US08-R16 a US08-R18, ao
`AvaliarAtendimentoService`.

## Casos de teste

Atendimento de referência: `CONFIRMADO`, das 10:00 às 10:30.

| ID | Entrada | Esperado | Requisitos |
|---|---|---|---|
| US08-CT1 | Nota −3, às 11:00 | Rejeita, sem avaliação registrada | R1, R13 |
| US08-CT2 | Nota 3, às 11:00 | Avaliação registrada com nota 3 | R2 |
| US08-CT3 | Nota 10, às 11:00 | Rejeita, sem avaliação registrada | R3, R13 |
| US08-CT4 | Nota 4, às 10:29 | Rejeita | R5, R7 |
| US08-CT5 | Nota 4, às 10:30 | Avaliação registrada às 10:30 | R6, R7, R14 |
| US08-CT6 | Nota 4, às 10:31 | Avaliação registrada às 10:31 | R6, R7, R14 |
| US08-CT7 | Nota 4, às 09:00 | Rejeita | R8 |
| US08-CT8 | Agendamento `AGENDADO`, nota 4, às 11:00 | Rejeita, sem avaliação registrada | R11, R13 |
| US08-CT9 | Agendamento `CANCELADO`, nota 9, às 11:00 | Rejeita pelo status, não pela nota | R15 |
| US08-CT10 | Nota 0, às 10:15 | Rejeita pelo término, não pela nota | R15 |
| US08-CT11 | Serviço com `AgendamentoId` inexistente | Rejeita com erro de agendamento não encontrado | R17 |
| US08-CT12 | Serviço com nota 0 | Rejeita e não salva o agendamento | R18 |
