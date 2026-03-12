CREATE EXTENSION IF NOT EXISTS vector;

-- Vector store table (managed by Spring AI PgVectorStore)
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(768)
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops);

-- Documents metadata table
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    account_type VARCHAR(100),
    upload_time TIMESTAMP NOT NULL DEFAULT NOW(),
    chunk_count INTEGER,
    file_size BIGINT,
    file_url TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);

-- Chat history table
CREATE TABLE IF NOT EXISTS chat_history (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    question TEXT NOT NULL,
    answer TEXT,
    sources TEXT,
    latency_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
