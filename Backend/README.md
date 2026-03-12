# Finex AI

A production-grade RAG application that lets users upload bank statements (PDF/CSV) and ask natural language questions about their financial data — grounded in retrieved document context, not hallucinated answers.

Built with **Java 21, Spring Boot 3, Spring AI, Ollama, and PostgreSQL + pgvector**.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         React Frontend                          │
│         Upload UI  ·  Chat Interface  ·  Source Panel           │
└─────────────────────────┬───────────────────────────────────────┘
                          │ REST
┌─────────────────────────▼───────────────────────────────────────┐
│                    Spring Boot API (Java 21)                     │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  /documents  │  │   /chat/ask  │  │  GlobalExceptionHndlr│   │
│  │   upload     │  │  RAG query   │  │  + Actuator metrics  │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────────┘   │
│         │                 │                                      │
│  ┌──────▼───────┐  ┌──────▼───────┐                             │
│  │IngestionSvc  │  │  RagService  │                             │
│  │ Parse→Chunk  │  │Search→Prompt │                             │
│  │ →Embed→Store │  │ →Answer+Src  │                             │
│  └──────┬───────┘  └──────┬───────┘                             │
└─────────┼─────────────────┼───────────────────────────────────--┘
          │                 │
┌─────────▼─────┐   ┌───────▼──────────┐
│  PostgreSQL   │   │     Ollama        │
│  + pgvector   │   │  llama3.2 (chat)  │
│  documents    │   │  nomic-embed-text │
│  chat_history │   │  (embeddings)     │
│  vector_store │   └──────────────────┘
└───────────────┘
```

### RAG Pipeline (step by step)

1. User uploads a PDF or CSV bank statement
2. `DocumentParserService` extracts text (PDFBox for PDF, OpenCSV for CSV)
3. Text is split into overlapping chunks (configurable size + overlap)
4. `Spring AI` embeds each chunk using `nomic-embed-text` via Ollama
5. Vectors + metadata stored in PostgreSQL via `pgvector`
6. User asks a question → question is embedded → `pgvector` similarity search retrieves top-K chunks
7. Retrieved chunks are injected into a system prompt
8. `llama3.2` generates a grounded answer via Ollama
9. Response includes answer + source citations + latency metrics
10. All interactions logged to `chat_history` for audit

---

## Tech Stack

| Layer       | Technology                        | Why                                      |
|-------------|-----------------------------------|------------------------------------------|
| Language    | Java 21                           | LTS, virtual threads, records            |
| Framework   | Spring Boot 3.3                   | Enterprise-grade, production-ready       |
| AI Layer    | Spring AI 1.0.0-M3                | Official Spring RAG + vector store APIs  |
| LLM         | Ollama (llama3.2)                 | Local inference, no API cost             |
| Embeddings  | Ollama (nomic-embed-text, 768d)   | High quality, runs locally               |
| Vector DB   | PostgreSQL + pgvector (HNSW)      | No extra infra, cosine similarity search |
| ORM         | Spring Data JPA                   | Familiar, type-safe queries              |
| PDF parsing | Apache PDFBox 3                   | Apache-licensed, reliable                |
| CSV parsing | OpenCSV 5                         | Handles edge cases well                  |
| Testing     | JUnit 5 + Testcontainers          | Real DB in CI, not mocks                 |
| CI/CD       | GitHub Actions                    | Build → Test → Docker on every push      |
| Infra       | Docker Compose                    | Single-command local stack               |

---

## Quick Start

### Prerequisites

- Docker + Docker Compose
- Java 21
- Maven 3.9+

### 1. Start the infrastructure

```bash
docker-compose up -d postgres ollama
```

Wait ~30 seconds for Ollama to pull the models (first run only).

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

API available at: `http://localhost:8080`

### 3. Try it with curl

**Upload a statement:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@sample-data/sample-transactions.csv" \
  -F "accountType=Checking"
```

**Ask a question:**
```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How much did I spend on subscriptions this month?", "sessionId": "demo-1"}'
```

**Check health:**
```bash
curl http://localhost:8080/actuator/health
```

---

## Sample Questions

These work well with the included sample CSV:

- `"Summarize my spending for January"`
- `"What were my top 5 expenses by amount?"`
- `"How much did I spend on subscriptions?"`
- `"Were there any unusual or large transactions?"`
- `"What is my total spending on dining?"`
- `"How much was spent on groceries vs entertainment?"`

---

## Running Tests

```bash
cd backend
mvn test
```

Tests use **Testcontainers** to spin up a real PostgreSQL instance — no mocking the database. This ensures the schema, JPA mappings, and API contracts are all verified against real infrastructure.

---

## Key Engineering Decisions

**Why pgvector instead of a dedicated vector DB (Pinecone, Weaviate)?**
For an enterprise banking context, minimizing infrastructure footprint matters. pgvector runs inside the existing PostgreSQL instance, keeps vectors and metadata co-located, supports ACID transactions, and is production-proven at scale.

**Why Spring AI instead of LangChain4j?**
Spring AI is the official Spring ecosystem library. At a company like Zions that standardizes on Spring Boot, Spring AI integrates naturally with existing bean lifecycle, configuration, and observability tooling.

**Why Ollama for local inference?**
For a banking demo, all data stays local — no financial data sent to external APIs. Ollama also makes the stack reproducible without API keys.

**Why overlapping chunks?**
Financial documents often have context that spans multiple lines (e.g., a transaction amount on one line, the merchant category on the next). Overlap ensures this context isn't lost at chunk boundaries.

---

## Observability

Spring Actuator exposes AI-aware metrics at `/actuator/metrics`, including:
- `spring.ai.chat.client.observations` — chat model latency/token usage
- `spring.ai.embedding.model.observations` — embedding model metrics
- `spring.ai.vectorstore.observations` — vector store query metrics

These can be scraped by Prometheus and visualized in Grafana.

---

## Future Improvements (v2)

- [ ] Role-based access (Spring Security)
- [ ] Suspicious transaction detection (rules engine)
- [ ] Monthly spending dashboard with charts
- [ ] Statement comparison ("compare January vs February")
- [ ] GCP Cloud Run deployment + Cloud SQL
- [ ] Streaming responses via SSE
- [ ] Python FastAPI microservice for advanced PDF table extraction
