# Front-end da Barbearia Três Cadeiras

Interface web do sistema de reservas, feita com React, Vite e TypeScript. Por ela, o cliente cria conta, entra, marca horário, confirma presença, muda o horário, altera os serviços, cancela e avalia o atendimento.

## Requisitos

- Node.js 20 ou mais recente
- O back-end deste repositório rodando (Java 21)

## Como executar

1. Na raiz do repositório, suba o back-end. Ele atende em `http://localhost:8080`:

   ```bash
   ./mvnw spring-boot:run
   ```

   Se a máquina só tiver Java 17, acrescente `-Djava.version=17` ao comando.

2. Em outro terminal, instale as dependências e suba o front-end:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. Abra `http://localhost:5173` e crie uma conta na aba **Criar conta**.

O Vite encaminha as chamadas de `/api` para o back-end. Se ele estiver em outra porta, informe o endereço ao subir o front-end:

```bash
API_PROXY_TARGET=http://localhost:8099 npm run dev
```

## Comandos

| Comando | O que faz |
|---|---|
| `npm run dev` | Sobe o servidor de desenvolvimento |
| `npm run build` | Verifica os tipos e gera a versão de produção em `dist/` |
| `npm run preview` | Serve localmente o conteúdo de `dist/` |

## Variáveis de ambiente

| Variável | Quando usar | Valor padrão |
|---|---|---|
| `VITE_API_URL` | Na versão publicada, com o endereço público do back-end | Vazio, usa a mesma origem |
| `API_PROXY_TARGET` | Em desenvolvimento, com o back-end fora da porta 8080 | `http://localhost:8080` |

## Como publicar na Vercel

O front-end está pronto para a Vercel, e o `vercel.json` já cuida das rotas. Crie o projeto com `frontend` como diretório raiz e defina `VITE_API_URL`.

O back-end precisa estar antes em um endereço público e liberar a origem do front-end em CORS, o que ele ainda não faz. Sem isso, a versão publicada abre, mas não consegue falar com a API.

## O que saber antes de usar

- **Serviços e barbeiros:** a API não tem rota para listá-los. Os serviços em `src/catalogo.ts` espelham o `schema.sql` e precisam mudar junto com ele. Os barbeiros existem só nesse arquivo, porque o back-end não tem cadastro de barbeiro.
- **Contas somem ao reiniciar o back-end:** com `spring.jpa.hibernate.ddl-auto=create-drop`, a tabela de usuários é recriada a cada início. Nesse caso, a interface pede para entrar de novo e é preciso criar outra conta.
- **Regras de negócio:** `src/regras.ts` repete algumas regras só para decidir quais ações mostrar. Quem valida é a API, e as mensagens de erro dela aparecem na tela.
