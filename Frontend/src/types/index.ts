export interface User {
  id: string;
  email: string;
  name: string;
}

export interface AuthTokens {
  accessToken: string;
}

export interface DocumentRecord {
  id: string;
  fileName: string;
  fileType: 'PDF' | 'CSV';
  fileSize: number;
  accountType?: string;
  status: 'PROCESSING' | 'READY' | 'FAILED';
  uploadedAt: string;
  processedAt?: string;
}

export interface UploadResponse {
  documentId: string;
  fileName: string;
  status: string;
  message: string;
}

export interface SourceChunk {
  content: string;
  fileName: string;
  score: number;
}

export interface ChatResponse {
  question: string;
  answer: string;
  sources: SourceChunk[];
  latencyMs: number;
  hasContext: boolean;
}

export interface ChatHistoryItem {
  id: string;
  sessionId: string;
  question: string;
  answer: string;
  sourceChunks: SourceChunk[];
  modelUsed: string;
  latencyMs: number;
  createdAt: string;
}

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  sources?: SourceChunk[];
  latencyMs?: number;
  timestamp: Date;
}
