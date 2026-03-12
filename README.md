# Finex AI

An AI-powered financial assistant that lets users upload bank statements (PDF/CSV) and ask natural language questions about their finances using RAG (Retrieval-Augmented Generation).

## Tech Stack

### Backend
- **Java 21 + Spring Boot 4**
- **Spring AI** — RAG pipeline with pgvector, chat via Gemini 2.5 Flash (OpenRouter)
- **Embeddings** — `nomic-embed-text-v2-moe` via Ollama (768 dimensions, HNSW index)
- **Auth** — Clerk JWT via Spring Security OAuth2 Resource Server
- **Storage** — Cloudflare R2 (S3-compatible)
- **Document parsing** — Apache PDFBox (PDF), OpenCSV (CSV)
- **Database** — PostgreSQL + pgvector

### Frontend
- **React 19 + TypeScript + Vite 7**
- **Auth** — `@clerk/react`
- **Routing** — `react-router-dom v7`
- **State** — TanStack Query (server) + Zustand (client)
- **UI** — Tailwind CSS v4 + shadcn/ui + Framer Motion

## Project Structure

```
Finex-AI/
├── Backend/    # Spring Boot API
└── Frontend/   # React SPA
```

## Pages

| Route | Page |
|---|---|
| `/` | Landing |
| `/login` | Sign in (Clerk) |
| `/signup` | Sign up (Clerk) |
| `/app/home` | Dashboard |
| `/app/chat` | New chat |
| `/app/chat/:sessionId` | Existing chat |
| `/app/library` | Document library |

## Getting Started

### Prerequisites
- Java 21
- Node.js 20+
- Docker (for PostgreSQL + pgvector + Ollama)

### Backend

```bash
cd Backend
docker compose up -d        # starts PostgreSQL+pgvector and Ollama
./mvnw spring-boot:run
```

Configure `Backend/src/main/resources/application.properties` with your:
- OpenRouter API key (Gemini 2.5 Flash)
- Clerk JWKS URI
- Cloudflare R2 credentials
- PostgreSQL connection

### Frontend

```bash
cd Frontend
npm install
npm run dev
```

Configure `Frontend/.env.local` with your Clerk publishable key and API base URL.

## RAG Configuration

| Parameter | Value |
|---|---|
| Embedding model | `nomic-embed-text-v2-moe` (Ollama) |
| Chat model | `google/gemini-2.5-flash` (OpenRouter) |
| Chunk size | 800 tokens |
| Chunk overlap | 200 tokens |
| Vector index | HNSW (768 dimensions) |
