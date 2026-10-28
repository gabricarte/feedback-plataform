# Feedback Platform — Tech Challenge Fase 4

Plataforma serverless na AWS onde estudantes avaliam aulas e administradores recebem alertas de
avaliações críticas e um relatório semanal.

## Arquitetura

```
                 POST /avaliacao
Estudante ──► API Gateway ──► ReceiveFeedbackFunction ──► DynamoDB (FeedbackTable)
                                     │ nota 0–3 (HIGH)
                                     ▼
                              SNS (UrgencyTopic) ──► NotifyUrgencyFunction ──► SES ──► Admin
                                                           │ falhou 3x
                                                           ▼
                                                     SQS (dead-letter)

EventBridge (segunda 12:00 UTC) ──► GenerateWeeklyReportFunction ──► DynamoDB (scan 7 dias)
                                                                  └─► SES ──► Admin

CloudWatch: logs (30 dias), X-Ray, alarmes de erro ──► SNS (AlarmTopic) ──► e-mail do Admin
```

Modelo de nuvem: **serverless (FaaS + serviços gerenciados)** na AWS. Sem servidores para
administrar, cobrança por uso, escala automática.

| Função | Gatilho | Responsabilidade única |
|---|---|---|
| `ReceiveFeedbackFunction` | API Gateway `POST /avaliacao` | Validar, gravar e sinalizar feedback crítico |
| `NotifyUrgencyFunction` | SNS `UrgencyTopic` | Enviar e-mail de urgência |
| `GenerateWeeklyReportFunction` | EventBridge (semanal) | Montar e enviar o relatório semanal |

### Regra de urgência

| Nota | Urgência | Alerta imediato |
|---|---|---|
| 0–3 | `HIGH` (ALTA) | Sim |
| 4–6 | `MEDIUM` (MÉDIA) | Não |
| 7–10 | `LOW` (BAIXA) | Não |

## Estrutura do projeto

```
feedback-platform/
├── pom.xml                      # parent: versões e plugins
├── template.yaml                # infraestrutura (AWS SAM)
├── .github/workflows/deploy.yml # CI/CD
├── feedback-core/               # domínio + adaptadores compartilhados
│   └── techchallenge.feedback.core
│       ├── domain/              # Feedback, UrgencyLevel, InvalidFeedbackException
│       ├── repository/          # FeedbackRepository + implementação DynamoDB
│       ├── notification/        # EmailSender + implementação SES
│       ├── config/  json/       # variáveis de ambiente, logs, ObjectMapper
├── receive-feedback-function/   # Lambda 1 (+ publicador SNS)
├── notify-urgency-function/     # Lambda 2 (+ conteúdo do e-mail de alerta)
├── weekly-report-function/      # Lambda 3 (+ WeeklyReport e e-mail do relatório)
└── test-support/                # fakes em memória usados só nos testes
```

Cada função gera seu próprio JAR e só carrega o que usa.

## API

```http
POST /avaliacao
Content-Type: application/json

{ "descricao": "A aula foi ótima", "nota": 9 }
```

| Status | Quando |
|---|---|
| `201` | `{"id": "...", "message": "Feedback registrado com sucesso."}` |
| `400` | Corpo vazio, JSON inválido, `descricao` em branco, `nota` ausente, fora de 0–10 ou não inteira |
| `500` | Falha de infraestrutura (detalhes só no log) |

## Deploy

Pré-requisitos: Java 17, Maven 3.9+, AWS SAM CLI, credenciais AWS.

1. Verifique no SES os e-mails do remetente e do administrador (no sandbox do SES os dois precisam estar verificados).
2. Gere os JARs e rode os testes:
   ```bash
   mvn package
   ```
3. Publique:
   ```bash
   sam deploy --guided --stack-name feedback-platform --capabilities CAPABILITY_IAM
   ```
4. Confirme a inscrição no e-mail "AWS Notification - Subscription Confirmation" (alarmes).

Deploy automático: o workflow `.github/workflows/deploy.yml` roda build e testes em todo push/PR
e faz `sam deploy` a cada push na `main`. Ele usa OIDC (sem chaves fixas no GitHub). Configure:
`secrets.AWS_DEPLOY_ROLE_ARN`, `vars.AWS_REGION`, `vars.ADMIN_EMAIL`, `vars.SENDER_EMAIL`.

Teste rápido:
```bash
curl -X POST "$ENDPOINT" -H 'Content-Type: application/json' -d '{"descricao":"Sem áudio","nota":1}'
```

## Segurança e governança

- IAM com menor privilégio: cada função só tem as ações que usa (`PutItem`, `Scan`, `sns:Publish`, `ses:SendEmail`).
- Dados criptografados em repouso: DynamoDB (SSE), SNS (KMS), SQS (SSE). Em trânsito: HTTPS.
- Backup: Point-in-time recovery na tabela; `DeletionPolicy: Retain`.
- Throttling na API (20 req/s, pico 40).
- Nada sensível nos logs nem nas respostas de erro 500.

## Monitoramento

- CloudWatch Logs de cada função (retenção 30 dias) e tracing X-Ray.
- Alarmes por e-mail: erros em cada Lambda, 5xx na API e mensagens na dead-letter queue de alertas.

## Testes

```bash
mvn test
```
Os testes usam fakes em memória (`test-support`), sem acessar a AWS.
