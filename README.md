# hls-study

Projeto de estudo focado em **Kafka, Kafka Streams e processamento assíncrono**, simulando um pipeline de upload e transcodificação de vídeo estilo YouTube (upload → transcodificação em múltiplas resoluções → geração de manifesto HLS).

> Não é um produto, apenas material de estudo e ficar como portfólio.

## O que esse projeto faz

1. Recebe upload de um vídeo
2. Transcodifica em paralelo pra 480p e 1080p (HLS)
3. Espera as duas resoluções terminarem (join de streams)
4. Gera o manifesto `master.m3u8` que aponta pras duas
5. Disponibiliza a URL final pra reprodução via player HLS (video.js/hls.js)

## Arquitetura

_**Caixas com borda** = serviços rodando. **Texto entre parênteses** = tópicos do Kafka._

```
                     ┌─────────────────┐
                     │       API       │  POST /upload
                     └────────┬────────┘
                              │
                      (videos.uploaded)
                              │
                  ┌───────────┴───────────┐
                  ▼                       ▼
         ┌──────────────────┐   ┌──────────────────┐
         │   worker-480p    │   │  worker-1080p    │
         └────────┬─────────┘   └─────────┬────────┘
                  │                       │
       (videos.480p-uploaded)  (videos.1080p-uploaded)
                  │                       │
                  └───────────┬───────────┘
                              ▼
                ┌───────────────────────────┐
                │       orchestrator        │
                │  Kafka Streams · join 2/2 │
                └─────────────┬─────────────┘
                              │
                  (videos.generate-master)
                              │
                              ▼
                     ┌─────────────────┐
                     │  worker-master  │
                     └────────┬────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │       API       │  GET /videos/{id}
                     └─────────────────┘
```

- **API** recebe o upload, salva o raw no MinIO, registra metadados no Postgres e publica no Kafka. Também expõe o endpoint de consulta que devolve a URL do vídeo pronto.
- **workers 480p/1080p** consomem o evento de upload, rodam FFmpeg, geram os segmentos HLS daquela resolução, sobem pro MinIO e salvam o pointer no Postgres.
- **orchestrator** usa Kafka Streams pra fazer o join dos eventos de 480p e 1080p pela `video_id`. Quando os dois chegam, dispara o evento de geração do manifesto final. Não escreve em storage nem em banco — só orquestra.
- **worker-master** monta o `master.m3u8` a partir dos pointers salvos no Postgres, sobe pro MinIO e marca o vídeo como pronto.

## Stack

| Componente                    | Linguagem/Tech        |
| ----------------------------- | --------------------- |
| API                           | Java + Spring Boot    |
| Orchestrator                  | Java + Kafka Streams  |
| Workers (480p, 1080p, master) | Go                    |
| Mensageria                    | Kafka (multi-broker)  |
| Storage de vídeo              | MinIO (S3-compatible) |
| Banco de metadados            | PostgreSQL            |
| Infra                         | Docker Compose        |

## Estrutura do repositório

```
.
├── infra/          # docker-compose, configs de Kafka/MinIO/Postgres, scripts de setup
├── api/             # Java + Spring Boot — upload e retrieval
├── orchestrator/     # Java + Kafka Streams — join dos eventos de transcodificação
├── worker-480p/       # Go — transcodificação 480p via FFmpeg
├── worker-1080p/      # Go — transcodificação 1080p via FFmpeg
├── worker-master/     # Go — geração do manifesto master.m3u8
└── README.md
```

## Como rodar

```bash
cd infra
docker compose up -d
```

Isso sobe Kafka (multi-broker), MinIO e Postgres. Cada serviço (API, orchestrator, workers) tem instruções próprias de build/run no README da respectiva pasta.

## Status

🚧 Em construção — projeto de estudo em progresso.

- [ ] API: upload + registro no Postgres + publish no Kafka
- [ ] worker-480p
- [ ] worker-1080p
- [ ] orchestrator: join 480p + 1080p
- [ ] worker-master: geração do manifesto
- [ ] API: endpoint de retrieval
- [ ] Testar failover: matar instância do orchestrator no meio do processamento e validar recovery via changelog
- [ ] (stretch) worker-thumbnail, rodando independente, sem passar pelo join

## Motivação

Projeto criado pra aprender Kafka e Kafka Streams na prática — particionamento, consumer groups, joins entre streams, state stores, recovery via changelog topic — além de praticar Go do zero nos workers.
