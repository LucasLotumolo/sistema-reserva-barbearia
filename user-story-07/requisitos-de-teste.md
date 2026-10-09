# US-07 — Consultar horários disponíveis: testes funcionais

Requisitos e casos de teste derivados das regras RN-04, RN-05, RN-19 e RN-20 por
particionamento em classes de equivalência (CE) e análise do valor limite (AVL).
A menor unidade nas fronteiras de tempo é 1 minuto.

Requisitos marcados como cobertos já são exercitados pelos testes de TDD dos cenários e não
geram casos novos.

## Requisitos de teste

| ID | Requisito de teste | Critério | Coberto por TDD |
|---|---|---|---|
| US07-R1 | Duração pedida ≤ 0 é rejeitada | CE inválida | — |
| US07-R2 | Duração pedida entre 1 e 600 é aceita | CE válida | 7.1 a 7.7 (30 min) |
| US07-R3 | Duração pedida > 600 devolve lista vazia, sem erro | CE válida | — |
| US07-R4 | Duração nos limites 0, 1, 600 e 601 | AVL | — |
| US07-R5 | Data anterior a hoje é rejeitada | CE inválida | 7.8 |
| US07-R6 | Data de hoje só devolve intervalos a partir do instante atual | CE válida | — |
| US07-R7 | Data futura considera o expediente inteiro | CE válida | 7.2 |
| US07-R8 | Data nos limites ontem, hoje e amanhã | AVL | ontem (7.8), amanhã (7.2) |
| US07-R9 | Intervalo livre menor que a duração é descartado | CE | 7.5 |
| US07-R10 | Intervalo livre maior ou igual à duração é devolvido | CE | 7.1, 7.6 |
| US07-R11 | Intervalo livre nos limites duração − 1, duração e duração + 1 | AVL | duração (7.6) |
| US07-R12 | Agendamentos `AGENDADO` e `CONFIRMADO` bloqueiam o horário | CE | só `AGENDADO` (7.1) |
| US07-R13 | Agendamentos `CANCELADO` e `EXPIRADO` não bloqueiam | CE | 7.7 |
| US07-R14 | Agendamento de outra data na agenda é rejeitado | CE inválida | — |
| US07-R15 | Serviço inexistente no catálogo é rejeitado | CE inválida | — |
| US07-R16 | Lista de serviços vazia é rejeitada | CE inválida | — |
| US07-R17 | Duração da consulta é a soma das durações dos serviços | CE válida | serviço (TDD) |

US07-R1 a US07-R14 se aplicam a `AgendaDoBarbeiro`; US07-R15 a US07-R17, ao
`ConsultarHorariosDisponiveisService`.

## Casos de teste

| ID | Entrada | Esperado | Requisitos |
|---|---|---|---|
| US07-CT1 | Dia vazio, duração 0 | Rejeita | R1, R4 |
| US07-CT2 | Dia vazio, duração −30 | Rejeita | R1 |
| US07-CT3 | Dia vazio, duração 1 | 09:00–19:00 | R2, R4 |
| US07-CT4 | Dia vazio, duração 600 | 09:00–19:00 | R2, R4 |
| US07-CT5 | Dia vazio, duração 601 | Lista vazia | R3, R4 |
| US07-CT6 | Hoje às 15:00, dia vazio | 15:00–19:00 | R6, R8 |
| US07-CT7 | Hoje às 08:00, dia vazio | 09:00–19:00 | R6 |
| US07-CT8 | Hoje às 19:00, dia vazio | Lista vazia | R6 |
| US07-CT9 | Intervalo livre de 29 min, duração 30 | Descartado | R9, R11 |
| US07-CT10 | Intervalo livre de 31 min, duração 30 | Devolvido | R10, R11 |
| US07-CT11 | Agendamento `CONFIRMADO` das 10:00 às 11:00 | 09:00–10:00 e 11:00–19:00 | R12 |
| US07-CT12 | Agenda de amanhã com um agendamento de depois de amanhã | Rejeita na criação da agenda | R14 |
| US07-CT13 | Um `ServicoId` fora do catálogo | Rejeita com erro de serviço não encontrado | R15 |
| US07-CT14 | Nenhum serviço informado | Rejeita | R16 |
| US07-CT15 | Corte (30) + barba (45), intervalo livre de exatamente 75 min | Devolvido | R17 |
