# MS Oportunidades — Mini CRM

> Parte do sistema **Mini CRM (PJBL)**. Arquitetura (C4, Canvas, ARC42), frontends, infra e links de todos os repositórios: [Le3Dev/minicrm-arquitetura](https://github.com/Le3Dev/minicrm-arquitetura).

Microsserviço do **funil de vendas** (Prospecção → Contato → Proposta → Fechado), gravado no **Azure SQL**. Quando uma oportunidade é fechada como **ganha**, ele publica o evento `OportunidadeGanha` no **Azure Service Bus**; a Azure Function consome e o MS Clientes passa o cliente para Ativo (RF30).

> **Situação:** as 6 slices do funil estão prontas (criar, mover, listar, buscar, atualizar e excluir), com adaptadores de Azure SQL e Service Bus, migração Flyway e testes (unitários, ArchUnit e API com H2).

| | URL |
| --- | --- |
| Swagger | `https://ms-oportunidades.kindbay-40f0b2c9.centralus.azurecontainerapps.io/docs` |
| Imagem Docker | `docker.io/leandrocanha/minicrm-ms-oportunidades:latest` |

## Arquitetura do código

**Clean Architecture** em quatro camadas, com a camada de aplicação em **vertical slices** (um pacote por caso de uso):

```
br.pjbl.minicrm.oportunidades
├── domain/            Java puro: Oportunidade, Etapa, Resultado, MotivoPerda, portas (Repository, EventoPublisher), exceções
├── application/       uma slice por caso de uso: criaroportunidade, moveroportunidade, listarfunil,
│                      buscaroportunidade, atualizaroportunidade, excluiroportunidade
├── infrastructure/    jpa (Azure SQL), eventos (Azure Service Bus), memoria (testes/local)
└── api/               controllers REST, DTOs e tratamento de erros (Swagger via springdoc)
```

Dependências: `api → application → domain` e `infrastructure → domain`; o `ArquiteturaTest` (ArchUnit) garante isso.

## Tecnologias

Java 21, Spring Boot 3.5, Spring Data JPA, Azure SQL Database (SQL Server), Flyway, Azure Service Bus, springdoc-openapi (Swagger UI), Bean Validation, JUnit 5, ArchUnit, H2 (testes), Docker, Azure Container Apps, GitHub Actions.

## Implementado

| Item | Onde |
| --- | --- |
| Regras do funil da SPEC 5.2: fechar exige resultado; perdido exige motivo da lista fixa; sair do Fechado exige `reabrir`; retrocesso livre | `domain/Oportunidade.java` + `OportunidadeTest` |
| Slice **CriarOportunidade** (`POST /oportunidades`) | `application/criaroportunidade` |
| Slice **MoverOportunidade** (`PATCH /oportunidades/{id}`), que publica `OportunidadeGanha` ao fechar como ganho | `application/moveroportunidade` |
| Azure SQL com JPA, `@Version` (concorrência otimista) e migração Flyway `V1__oportunidades.sql` | `infrastructure/jpa`, `resources/db/migration` |
| Publicação no Service Bus no formato exato do `asyncapi.yaml` (sem `SERVICEBUS_CONNECTION`, vai só para o log) | `infrastructure/eventos` |
| ArchUnit: camadas, Java puro em domain/application, slices independentes | `ArquiteturaTest` |
| CI com `mvn verify` e publicação da imagem em tag `v*` (ex.: `v1.0.0`) | `.github/workflows/ci.yml` |

## Demais slices

| Slice | Rota | Regra |
| --- | --- | --- |
| `listarfunil` | `GET /oportunidades?responsavelId=u2,u5&clienteId=` | As 4 etapas com itens, quantidade e soma; `valorEmNegociacao` soma as abertas (RF08, RF19) |
| `buscaroportunidade` | `GET /oportunidades/{id}` | 404 se não existir |
| `atualizaroportunidade` | `PUT /oportunidades/{id}` | Título, valor, data prevista e responsável; só se estiver aberta |
| `excluiroportunidade` | `DELETE /oportunidades/{id}` | 204; proibido se estiver fechada como ganha |

- [ ] (Opcional) Padrão **Outbox**: gravar o evento numa tabela na mesma transação e publicar depois, para não perder evento se o Service Bus cair.

## Rodar localmente

```bash
mvn verify                                    # testes com H2 em modo SQL Server
AZURE_SQL_URL=... AZURE_SQL_USUARIO=... AZURE_SQL_SENHA=... mvn spring-boot:run
```

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
