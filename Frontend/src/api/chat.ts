import { apiClient } from './client';
import type { ChatResponse, ChatHistoryItem } from '@/types';

export const chatApi = {
  ask: async (question: string, sessionId: string): Promise<ChatResponse> => {
    const { data } = await apiClient.post('/api/chat/ask', { question, sessionId });
    return data;
  },

  getSessionHistory: async (sessionId: string): Promise<ChatHistoryItem[]> => {
    const { data } = await apiClient.get(`/api/chat/history/${sessionId}`);
    return data;
  },

  getAllHistory: async (): Promise<ChatHistoryItem[]> => {
    const { data } = await apiClient.get('/api/chat/history');
    return data;
  },
};
