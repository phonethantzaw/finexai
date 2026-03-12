import { useState, useRef, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { nanoid } from 'nanoid';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Loader2, Bot, User } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useAskQuestion } from '@/hooks/useChat';
import { SourcePanel } from '@/components/chat/SourcePanel';
import type { Message, SourceChunk } from '@/types';

const SUGGESTED = [
  'Summarize my spending this month',
  'What were my top 5 expenses?',
  'How much did I spend on subscriptions?',
  'Were there any unusual transactions?',
];

export default function NewChatPage() {
  const { sessionId: routeSession } = useParams<{ sessionId?: string }>();
  const sessionId   = useRef(routeSession ?? nanoid()).current;
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput]       = useState('');
  const [sources, setSources]   = useState<SourceChunk[]>([]);
  const bottomRef               = useRef<HTMLDivElement>(null);
  const { mutate: ask, isPending } = useAskQuestion();

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = (text: string) => {
    if (!text.trim() || isPending) return;

    const userMsg: Message = {
      id: nanoid(), role: 'user', content: text, timestamp: new Date(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');

    ask(
      { question: text, sessionId },
      {
        onSuccess: (data) => {
          const assistantMsg: Message = {
            id:        nanoid(),
            role:      'assistant',
            content:   data.answer,
            sources:   data.sources,
            latencyMs: data.latencyMs,
            timestamp: new Date(),
          };
          setMessages((prev) => [...prev, assistantMsg]);
          setSources(data.sources);
        },
        onError: () => {
          setMessages((prev) => [
            ...prev,
            { id: nanoid(), role: 'assistant',
              content: 'Something went wrong. Please try again.',
              timestamp: new Date() },
          ]);
        },
      }
    );
  };

  const isEmpty = messages.length === 0;

  return (
    <div className="flex h-[calc(100vh-4rem)] gap-4">
      <div className="flex flex-1 flex-col">
        <ScrollArea className="flex-1 pr-2">
          {isEmpty ? (
            <div className="flex flex-col items-center justify-center h-full pt-24 gap-6">
              <Bot className="size-12 text-muted-foreground" />
              <p className="text-lg font-medium">Ask about your bank statements</p>
              <div className="grid grid-cols-2 gap-2 max-w-lg w-full">
                {SUGGESTED.map((s) => (
                  <Button key={s} variant="outline" size="sm"
                    className="h-auto py-2 text-left justify-start text-xs"
                    onClick={() => sendMessage(s)}>
                    {s}
                  </Button>
                ))}
              </div>
            </div>
          ) : (
            <div className="space-y-4 py-4">
              <AnimatePresence initial={false}>
                {messages.map((msg) => (
                  <motion.div key={msg.id}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    className={`flex gap-3 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}>
                    <div className={`size-7 rounded-full flex items-center justify-center shrink-0
                      ${msg.role === 'user' ? 'bg-primary' : 'bg-muted'}`}>
                      {msg.role === 'user'
                        ? <User className="size-3.5 text-primary-foreground" />
                        : <Bot  className="size-3.5 text-muted-foreground" />}
                    </div>
                    <div className={`max-w-[75%] rounded-xl px-4 py-2.5 text-sm
                      ${msg.role === 'user'
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-muted text-foreground'}`}>
                      <p className="whitespace-pre-wrap">{msg.content}</p>
                      {msg.latencyMs && (
                        <p className="mt-1 text-[10px] opacity-50">{msg.latencyMs}ms</p>
                      )}
                    </div>
                  </motion.div>
                ))}
              </AnimatePresence>

              {isPending && (
                <div className="flex gap-3">
                  <div className="size-7 rounded-full bg-muted flex items-center justify-center">
                    <Loader2 className="size-3.5 animate-spin text-muted-foreground" />
                  </div>
                  <div className="bg-muted rounded-xl px-4 py-2.5">
                    <span className="text-sm text-muted-foreground">Thinking…</span>
                  </div>
                </div>
              )}
              <div ref={bottomRef} />
            </div>
          )}
        </ScrollArea>

        <div className="border-t pt-4 mt-2">
          <div className="flex gap-2 items-end">
            <Textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  sendMessage(input);
                }
              }}
              placeholder="Ask about your financial documents…"
              className="resize-none min-h-[44px] max-h-[160px]"
              rows={1}
            />
            <Button size="icon" onClick={() => sendMessage(input)}
              disabled={!input.trim() || isPending}>
              <Send className="size-4" />
            </Button>
          </div>
          <p className="text-[11px] text-muted-foreground mt-1.5">
            Enter to send · Shift+Enter for new line
          </p>
        </div>
      </div>

      {sources.length > 0 && <SourcePanel sources={sources} />}
    </div>
  );
}
