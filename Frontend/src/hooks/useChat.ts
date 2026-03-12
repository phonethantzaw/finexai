import { useQuery, useMutation } from '@tanstack/react-query';
import { chatApi } from '@/api/chat';

export function useChatHistory(sessionId: string) {
  return useQuery({
    queryKey: ['chat', 'history', sessionId],
    queryFn:  () => chatApi.getSessionHistory(sessionId),
    enabled:  !!sessionId,
  });
}

export function useAllChatHistory() {
  return useQuery({
    queryKey: ['chat', 'history'],
    queryFn:  chatApi.getAllHistory,
  });
}

export function useAskQuestion() {
  return useMutation({
    mutationFn: ({ question, sessionId }: { question: string; sessionId: string }) =>
      chatApi.ask(question, sessionId),
  });
}
