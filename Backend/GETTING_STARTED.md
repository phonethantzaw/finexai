# Finex AI - Getting Started

## Prerequisites

- Docker & Docker Compose
- Java 21
- Maven 3.9+
- Ollama (for local embedding model)
- OpenRouter API key (for chat model)
- Clerk account (for JWT authentication)

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

Wait for PostgreSQL to start. Check status:
```bash
docker-compose ps
docker logs banking-ai-postgres
```

### 2. Set Environment Variables

Create a `.env` file in the project root (never committed to git):
```bash
# OpenRouter — chat model
OPENROUTER_API_KEY=your_openrouter_api_key_here

# Clerk JWT verification
CLERK_JWKS_URI=https://<your-clerk-frontend-api>/.well-known/jwks.json

# Cloudflare R2 storage
CF_ACCOUNT_ID=your_cloudflare_account_id
CF_R2_ACCESS_KEY_ID=your_r2_access_key_id
CF_R2_SECRET_ACCESS_KEY=your_r2_secret_access_key
CF_R2_BUCKET_NAME=your_bucket_name
CF_R2_PUBLIC_URL=https://pub-xxxx.r2.dev
```

#### Cloudflare R2 Setup Checklist

1. Go to **Cloudflare Dashboard → R2** and create a bucket.
2. Enable **Public Access** on the bucket (so uploaded files get a public URL).
3. Create an **R2 API Token** with *Object Read & Write* permission scoped to your bucket.
4. Copy your **Account ID**, **Access Key ID**, **Secret Access Key**, and the public bucket URL (e.g. `https://pub-xxxx.r2.dev`).
5. Set all five `CF_*` variables in your `.env`.

Find your `CLERK_JWKS_URI`:
> Clerk Dashboard → **API Keys** → **Clerk Frontend API**  
> e.g. `https://your-app.clerk.accounts.dev/.well-known/jwks.json`

Export the variables before running:
```bash
export OPENROUTER_API_KEY=...
export CLERK_JWKS_URI=https://your-app.clerk.accounts.dev/.well-known/jwks.json
export CF_ACCOUNT_ID=...
export CF_R2_ACCESS_KEY_ID=...
export CF_R2_SECRET_ACCESS_KEY=...
export CF_R2_BUCKET_NAME=...
export CF_R2_PUBLIC_URL=https://pub-xxxx.r2.dev
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

Application will start on `http://localhost:8080`

### 4. Obtain a Clerk JWT for Testing

All API endpoints (except `GET /actuator/health`) require a **Bearer token** from Clerk.

**From your React frontend (Clerk SDK):**
```typescript
import { useAuth } from "@clerk/react";
const { getToken } = useAuth();
const token = await getToken();
// Use token in Authorization header: Bearer <token>
```

**For Postman / curl testing:** use Clerk's test tokens from your dashboard or sign in via your frontend and copy the token from `localStorage` / DevTools network tab.

### 5. Test the API

**Upload a document:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer <your-clerk-jwt>" \
  -F "file=@sample-data/sample-transactions.csv" \
  -F "accountType=Checking"
```

**Ask a question:**
```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-clerk-jwt>" \
  -d '{
    "question": "How much did I spend on subscriptions this month?",
    "sessionId": "demo-session-1"
  }'
```

**Get chat history:**
```bash
curl http://localhost:8080/api/chat/history/demo-session-1 \
  -H "Authorization: Bearer <your-clerk-jwt>"
```

**Health check (no auth required):**
```bash
curl http://localhost:8080/actuator/health
```

### 6. Run Tests

```bash
./mvnw test
```

Tests use Testcontainers to spin up a real PostgreSQL instance with pgvector extension.

## Project Structure

```
src/main/java/com/phonethantzaw/aibankingassistant/
├── config/
│   ├── SecurityConfig.java          # Clerk JWT / OAuth2 Resource Server config
│   ├── VectorStoreConfig.java       # pgvector configuration
│   └── CorsConfig.java              # CORS settings (CorsConfigurationSource)
├── controller/
│   ├── DocumentController.java      # Document upload API (auth-scoped)
│   └── ChatController.java          # Chat/RAG API (auth-scoped)
├── service/
│   ├── DocumentParserService.java   # PDF & CSV parsing
│   ├── IngestionService.java        # Chunking & embedding interface
│   ├── IngestionServiceImpl.java    # Chunking & embedding implementation
│   ├── RagService.java              # RAG query interface
│   └── RagServiceImpl.java          # RAG query implementation
├── model/
│   ├── DocumentRecord.java          # Document metadata entity (documents table)
│   └── ChatHistory.java             # Chat history entity
├── repository/
│   ├── DocumentRepository.java
│   └── ChatHistoryRepository.java
├── dto/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   └── UploadResponse.java
└── exception/
    └── GlobalExceptionHandler.java
```

## Authentication

All endpoints except `GET /actuator/health` are protected by **Clerk JWT verification**.

### How it works
1. The frontend (React + Clerk) obtains a JWT via `getToken()`.
2. The frontend sends it as `Authorization: Bearer <token>`.
3. Spring Boot validates the JWT automatically against Clerk's JWKS endpoint (`CLERK_JWKS_URI`).
4. The `userId` is extracted from the JWT `sub` claim — **no manual decoding needed**.
5. All data (documents, chat history, vector search) is scoped to that `userId`.

### Frontend setup (React + Axios)
```typescript
import { useAuth } from "@clerk/react";

// Axios interceptor — attach token to every request
apiClient.interceptors.request.use(async (config) => {
  const { getToken } = useAuth();
  const token = await getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

## Database Tables

| Table | Description |
|-------|-------------|
| `documents` | Metadata for uploaded documents (userId, filename, account type, chunk count, file size) |
| `vector_store` | Embeddings managed by Spring AI PgVectorStore (includes userId in metadata for per-user filtering) |
| `chat_history` | Session-based chat history scoped per userId |

## Configuration

Key properties in `application.properties`:

| Property | Description |
|----------|-------------|
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | Clerk JWKS endpoint (`${CLERK_JWKS_URI}`) |
| `spring.ai.openai.base-url` | Local Ollama endpoint for embeddings |
| `spring.ai.openai.embedding.options.model` | Local embedding model (`nomic-embed-text-v2-moe`) |
| `spring.ai.openai.embedding.options.dimensions` | Embedding dimensions (`768`) |
| `spring.ai.openai.chat.base-url` | Chat model API base URL (OpenRouter) |
| `spring.ai.openai.chat.api-key` | OpenRouter API key (`${OPENROUTER_API_KEY}`) |
| `spring.ai.openai.chat.options.model` | Chat model (`google/gemini-2.5-flash`) |
| `spring.ai.vectorstore.pgvector.dimensions` | Vector dimensions — must match embedding model (`768`) |
| `app.ingestion.chunk-size` | Document chunk size (default: `800`) |
| `app.ingestion.chunk-overlap` | Chunk overlap (default: `200`) |
| `app.rag.top-k-results` | Number of similar chunks to retrieve (default: `5`) |

## API Endpoints

### Authentication Required

All endpoints below require:
```
Authorization: Bearer <clerk-jwt>
```

---

### Document Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/documents/upload` | Upload a PDF or CSV bank statement |
| `GET` | `/api/documents/all` | List all documents uploaded by the current user |
| `DELETE` | `/api/documents/{id}` | Delete a document from R2 and the database |

#### POST `/api/documents/upload`

**Postman:**
- Method: `POST`
- URL: `http://localhost:8080/api/documents/upload`
- Headers:
  - `Authorization`: `Bearer <your-clerk-jwt>`
- Body → `form-data`:
  - Key: `file` (type = **File**) → select your PDF or CSV
  - Key: `accountType` (type = **Text**) → e.g. `Checking`

**Response (201 Created):**
```json
{
  "documentId": 1,
  "filename": "sample-transactions.csv",
  "accountType": "Checking",
  "chunkCount": 12,
  "fileSize": 4096,
  "uploadTime": "2026-03-11T10:30:00",
  "fileUrl": "https://pub-xxxx.r2.dev/documents/user_2abc/uuid/sample-transactions.csv",
  "message": "Document uploaded and processed successfully"
}
```

#### GET `/api/documents/all`

**Postman:**
- Method: `GET`
- URL: `http://localhost:8080/api/documents/all`
- Headers: `Authorization: Bearer <your-clerk-jwt>`

Returns only documents belonging to the authenticated user.

#### DELETE `/api/documents/{id}`

**Postman:**
- Method: `DELETE`
- URL: `http://localhost:8080/api/documents/1`
- Headers: `Authorization: Bearer <your-clerk-jwt>`

Deletes the file from **Cloudflare R2** and removes the record from the database.  
Returns `204 No Content` on success, `404` if the document doesn't exist or belongs to a different user.

---

### Chat / RAG Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat/ask` | Ask a question using RAG (scoped to your documents) |
| `GET` | `/api/chat/history/{sessionId}` | Get chat history for a session (current user only) |
| `GET` | `/api/chat/history` | Get all chat history for the current user |

#### POST `/api/chat/ask`

**Postman:**
- Method: `POST`
- URL: `http://localhost:8080/api/chat/ask`
- Headers:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer <your-clerk-jwt>`
- Body → `raw` → `JSON`:

```json
{
  "question": "How much did I spend on subscriptions this month?",
  "sessionId": "my-session-001"
}
```

**Response (200 OK):**
```json
{
  "answer": "Based on your uploaded documents, you spent $45.97 on subscriptions...",
  "sources": [
    {
      "content": "2024-01-15, Netflix, -15.99...",
      "metadata": {
        "filename": "sample-transactions.csv",
        "accountType": "Checking",
        "userId": "user_2abc123",
        "chunkIndex": 2,
        "totalChunks": 12
      }
    }
  ],
  "latencyMs": 1523,
  "sessionId": "my-session-001"
}
```

#### GET `/api/chat/history/{sessionId}`

**Postman:**
- Method: `GET`
- URL: `http://localhost:8080/api/chat/history/my-session-001`
- Headers: `Authorization: Bearer <your-clerk-jwt>`

#### GET `/api/chat/history`

**Postman:**
- Method: `GET`
- URL: `http://localhost:8080/api/chat/history`
- Headers: `Authorization: Bearer <your-clerk-jwt>`

Returns all chat history for the current user across all sessions.

---

### Public Endpoints (no auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/actuator/health` | Health check |
| `GET` | `/actuator/metrics` | Application metrics |

---

## Troubleshooting

**401 Unauthorized:**
- Make sure `CLERK_JWKS_URI` is set and correct.
- Token must come from the same Clerk instance your app uses.
- Tokens expire — re-fetch with `getToken()` before each request.

**403 Forbidden:**
- Ensure the JWT `sub` claim is present (Clerk always sets it).

**Ollama not responding:**
```bash
ollama serve
ollama pull nomic-embed-text-v2-moe
```

**PostgreSQL connection issues:**
```bash
docker logs banking-ai-postgres
docker exec -it banking-ai-postgres psql -U postgres -d banking_ai -c "\dx"
```

**Check pgvector extension:**
```bash
docker exec -it banking-ai-postgres psql -U postgres -d banking_ai -c "SELECT * FROM pg_extension WHERE extname='vector';"
```

## Sample Questions

- "How much did I spend on subscriptions this month?"
- "What were my top 5 expenses?"
- "How much did I spend on groceries vs dining?"
- "Show me all transactions over $100"
- "What is my total spending on transportation?"
- "Were there any unusual transactions?"

## Stopping Services

```bash
docker-compose down
```

To remove volumes (clears all data):
```bash
docker-compose down -v
```
