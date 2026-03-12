# Finex AI — Frontend

A modern, production-grade React 19 frontend for Finex AI. Built with TypeScript, Vite, shadcn/ui, TanStack Query, and Zustand. Designed to mirror the clean density of tools like ElevenLabs and Linear.

---

## Tech Stack

| Category        | Library / Tool              | Purpose                                 |
|-----------------|-----------------------------|-----------------------------------------|
| Framework       | React 19 + TypeScript       | UI + type safety                        |
| Build Tool      | Vite 6                      | Fast dev server + optimized builds      |
| Styling         | Tailwind CSS v4             | Utility-first styling                   |
| Components      | shadcn/ui                   | Accessible, customizable UI primitives  |
| Routing         | React Router v7             | Client-side routing                     |
| Server State    | TanStack Query v5           | API fetching, caching, mutations        |
| Client State    | Zustand v5                  | Auth session + UI preferences           |
| HTTP Client     | Axios                       | API calls with interceptors             |
| Animations      | Framer Motion v12           | Page transitions + micro-interactions   |
| Icons           | Lucide React                | Consistent icon set                     |
| Notifications   | sonner                      | Toast notifications                     |

---

## Setup

### Prerequisites

- Node.js 20+
- npm or pnpm

### Install

```bash
npm install
```

### Install shadcn/ui

```bash
npx shadcn@latest init
```

When prompted:
- Style: **Default**
- Base color: **Zinc**
- CSS variables: **Yes**

### Add shadcn components used in this project

```bash
npx shadcn@latest add button card input label sidebar dialog
npx shadcn@latest add dropdown-menu table badge avatar command
npx shadcn@latest add sheet scroll-area separator progress
npx shadcn@latest add toast alert skeleton
```

### Install additional dependencies

```bash
npm install axios @tanstack/react-query zustand framer-motion
npm install react-router-dom lucide-react sonner
npm install @tanstack/react-query-devtools
```

### Environment Variables

Create `.env.local`:

```env
VITE_API_BASE_URL=http://localhost:3000
VITE_APP_NAME=Finex AI
```

### Run Development Server

```bash
npm run dev
```

---

## Folder Structure

```
src/
├── api/                        # Axios instance + all API services
│   ├── client.ts               # Axios base instance with interceptors
│   ├── documents.ts            # Document upload & list APIs
│   ├── chat.ts                 # Chat ask + history APIs
│   └── auth.ts                 # Login / signup (mock or real)
│
├── components/
│   ├── layout/
│   │   ├── AppLayout.tsx       # Root layout: Sidebar + Topbar + Outlet
│   │   ├── Sidebar.tsx         # Navigation sidebar
│   │   └── Topbar.tsx          # Top navigation bar
│   ├── chat/
│   │   ├── ChatWindow.tsx      # Full chat interface
│   │   ├── MessageBubble.tsx   # Individual message rendering
│   │   ├── MessageInput.tsx    # Input + send button
│   │   └── SourcePanel.tsx     # RAG source citations drawer
│   ├── documents/
│   │   ├── UploadDropzone.tsx  # Drag-and-drop file upload
│   │   └── DocumentTable.tsx   # Uploaded documents list
│   ├── home/
│   │   ├── StatsCards.tsx      # Quick stats overview
│   │   └── RecentChats.tsx     # Recent session list
│   └── ui/                     # shadcn generated components (auto)
│
├── pages/
│   ├── LoginPage.tsx
│   ├── SignupPage.tsx
│   ├── HomePage.tsx
│   ├── NewChatPage.tsx
│   ├── RecentsPage.tsx
│   ├── DataLibraryPage.tsx
│   ├── SettingsPage.tsx
│   └── ProfilePage.tsx
│
├── store/
│   ├── authStore.ts            # Auth state (user, token, logout)
│   └── settingsStore.ts        # Theme + preferences
│
├── hooks/
│   ├── useDocuments.ts         # TanStack Query hooks for documents
│   └── useChat.ts              # TanStack Query hooks for chat
│
├── lib/
│   ├── utils.ts                # cn() helper (shadcn default)
│   └── constants.ts            # App-wide constants
│
├── types/
│   └── index.ts                # Shared TypeScript types
│
├── router/
│   └── index.tsx               # Route definitions + guards
│
├── App.tsx
└── main.tsx
```

---

## TypeScript Types

```typescript
// src/types/index.ts

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
```

---

## Routing

```tsx
// src/router/index.tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import AppLayout from '@/components/layout/AppLayout';
import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import HomePage from '@/pages/HomePage';
import NewChatPage from '@/pages/NewChatPage';
import RecentsPage from '@/pages/RecentsPage';
import DataLibraryPage from '@/pages/DataLibraryPage';
import SettingsPage from '@/pages/SettingsPage';
import ProfilePage from '@/pages/ProfilePage';

// Protected route wrapper
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token);
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export const router = createBrowserRouter([
  { path: '/login',  element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
  {
    path: '/',
    element: <ProtectedRoute><AppLayout /></ProtectedRoute>,
    children: [
      { index: true,           element: <HomePage /> },
      { path: 'chat',          element: <NewChatPage /> },
      { path: 'chat/:sessionId', element: <NewChatPage /> },
      { path: 'recents',       element: <RecentsPage /> },
      { path: 'library',       element: <DataLibraryPage /> },
      { path: 'settings',      element: <SettingsPage /> },
      { path: 'profile',       element: <ProfilePage /> },
    ],
  },
]);
```

---

## API Layer

### Axios Client

```typescript
// src/api/client.ts
import axios from 'axios';
import { useAuthStore } from '@/store/authStore';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach token to every request
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Redirect to login on 401
apiClient.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### Documents API

```typescript
// src/api/documents.ts
import { apiClient } from './client';
import type { DocumentRecord, UploadResponse } from '@/types';

export const documentsApi = {
  upload: async (file: File, accountType?: string): Promise<UploadResponse> => {
    const form = new FormData();
    form.append('file', file);
    if (accountType) form.append('accountType', accountType);
    const { data } = await apiClient.post('/api/documents/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  },

  list: async (): Promise<DocumentRecord[]> => {
    const { data } = await apiClient.get('/api/documents/all');
    return data;
  },
};
```

### Chat API

```typescript
// src/api/chat.ts
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
```

### Auth API (mock — swap with your real backend)

```typescript
// src/api/auth.ts
// NOTE: The Spring Boot backend in this project does not include auth.
// Replace this mock with real calls once you add Spring Security.

import type { User, AuthTokens } from '@/types';

export const authApi = {
  login: async (email: string, _password: string): Promise<{ user: User; tokens: AuthTokens }> => {
    // MOCK — replace with: const { data } = await apiClient.post('/auth/login', { email, password })
    await new Promise((r) => setTimeout(r, 600));
    return {
      user:   { id: '1', email, name: email.split('@')[0] },
      tokens: { accessToken: 'mock-token-' + Date.now() },
    };
  },

  signup: async (email: string, _password: string, name: string): Promise<{ user: User; tokens: AuthTokens }> => {
    await new Promise((r) => setTimeout(r, 600));
    return {
      user:   { id: '2', email, name },
      tokens: { accessToken: 'mock-token-' + Date.now() },
    };
  },
};
```

---

## State Management (Zustand)

### Auth Store

```typescript
// src/store/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/types';

interface AuthState {
  user:    User | null;
  token:   string | null;
  setAuth: (user: User, token: string) => void;
  logout:  () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user:    null,
      token:   null,
      setAuth: (user, token) => set({ user, token }),
      logout:  () => set({ user: null, token: null }),
    }),
    { name: 'auth-storage' }
  )
);
```

### Settings Store

```typescript
// src/store/settingsStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type Theme = 'light' | 'dark' | 'system';

interface SettingsState {
  theme:       Theme;
  topK:        number;
  model:       string;
  setTheme:    (theme: Theme) => void;
  setTopK:     (k: number) => void;
  setModel:    (model: string) => void;
}

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      theme:    'dark',
      topK:     5,
      model:    'llama3.2',
      setTheme: (theme) => set({ theme }),
      setTopK:  (topK)  => set({ topK }),
      setModel: (model) => set({ model }),
    }),
    { name: 'settings-storage' }
  )
);
```

---

## TanStack Query Setup

```tsx
// src/main.tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { Toaster } from 'sonner';
import { router } from './router';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime:   1000 * 60 * 2,   // 2 minutes
      gcTime:      1000 * 60 * 10,  // 10 minutes
      retry:       1,
      refetchOnWindowFocus: false,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster position="top-right" richColors />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </StrictMode>
);
```

### Custom Query Hooks

```typescript
// src/hooks/useDocuments.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { documentsApi } from '@/api/documents';
import { toast } from 'sonner';

export const DOCUMENTS_KEY = ['documents'] as const;

export function useDocuments() {
  return useQuery({
    queryKey: DOCUMENTS_KEY,
    queryFn:  documentsApi.list,
  });
}

export function useUploadDocument() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, accountType }: { file: File; accountType?: string }) =>
      documentsApi.upload(file, accountType),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: DOCUMENTS_KEY });
      toast.success(`"${data.fileName}" uploaded successfully`);
    },
    onError: () => toast.error('Upload failed. Check file type and try again.'),
  });
}
```

```typescript
// src/hooks/useChat.ts
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
```

---

## Dashboard Layout

```tsx
// src/components/layout/AppLayout.tsx
import { Outlet } from 'react-router-dom';
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar';
import { AppSidebar } from './Sidebar';
import { Topbar } from './Topbar';

export default function AppLayout() {
  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <Topbar />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}
```

### Sidebar

```tsx
// src/components/layout/Sidebar.tsx
import { NavLink } from 'react-router-dom';
import {
  Home, MessageSquarePlus, History, Library, Settings, User
} from 'lucide-react';
import {
  Sidebar, SidebarContent, SidebarHeader, SidebarMenu,
  SidebarMenuItem, SidebarMenuButton, SidebarFooter,
} from '@/components/ui/sidebar';
import { useAuthStore } from '@/store/authStore';

const navItems = [
  { to: '/',         icon: Home,             label: 'Home'         },
  { to: '/chat',     icon: MessageSquarePlus, label: 'New Chat'    },
  { to: '/recents',  icon: History,           label: 'Recents'     },
  { to: '/library',  icon: Library,           label: 'Data Library' },
  { to: '/settings', icon: Settings,          label: 'Settings'    },
  { to: '/profile',  icon: User,              label: 'Profile'     },
];

export function AppSidebar() {
  const user = useAuthStore((s) => s.user);

  return (
    <Sidebar>
      <SidebarHeader className="px-4 py-5">
        <div className="flex items-center gap-2">
          {/* Logo mark */}
          <div className="w-7 h-7 rounded-md bg-primary flex items-center justify-center">
            <span className="text-xs font-bold text-primary-foreground">AI</span>
          </div>
          <span className="font-semibold text-sm tracking-tight">Finex AI</span>
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarMenu>
          {navItems.map(({ to, icon: Icon, label }) => (
            <SidebarMenuItem key={to}>
              <SidebarMenuButton asChild>
                <NavLink
                  to={to}
                  end={to === '/'}
                  className={({ isActive }) =>
                    isActive ? 'bg-accent text-accent-foreground' : ''
                  }
                >
                  <Icon className="size-4" />
                  <span>{label}</span>
                </NavLink>
              </SidebarMenuButton>
            </SidebarMenuItem>
          ))}
        </SidebarMenu>
      </SidebarContent>

      <SidebarFooter className="px-4 pb-4">
        <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
      </SidebarFooter>
    </Sidebar>
  );
}
```

---

## Pages

### Login Page

```tsx
// src/pages/LoginPage.tsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { authApi } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';

export default function LoginPage() {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading]   = useState(false);
  const { setAuth }             = useAuthStore();
  const navigate                = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { user, tokens } = await authApi.login(email, password);
      setAuth(user, tokens.accessToken);
      navigate('/');
    } catch {
      toast.error('Invalid email or password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-xl">Sign in</CardTitle>
          <CardDescription>Enter your credentials to continue</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" value={email}
                onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="space-y-1">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" value={password}
                onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
          <p className="mt-4 text-sm text-center text-muted-foreground">
            No account? <Link to="/signup" className="underline">Sign up</Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
```

### New Chat Page

This is the most important page. It manages its own local message list, uses the `useAskQuestion` mutation, and generates a `sessionId` per conversation.

```tsx
// src/pages/NewChatPage.tsx
import { useState, useRef, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { nanoid } from 'nanoid'; // npm install nanoid
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
      {/* Chat area */}
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

        {/* Input */}
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

      {/* Source panel (only visible when sources exist) */}
      {sources.length > 0 && <SourcePanel sources={sources} />}
    </div>
  );
}
```

### Source Panel Component

```tsx
// src/components/chat/SourcePanel.tsx
import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { FileText } from 'lucide-react';
import type { SourceChunk } from '@/types';

export function SourcePanel({ sources }: { sources: SourceChunk[] }) {
  return (
    <aside className="w-72 border-l pl-4 shrink-0">
      <h3 className="text-sm font-semibold mb-3">Sources ({sources.length})</h3>
      <ScrollArea className="h-full">
        <div className="space-y-3">
          {sources.map((src, i) => (
            <div key={i} className="rounded-lg border p-3 text-xs space-y-2">
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-1.5 min-w-0">
                  <FileText className="size-3 shrink-0 text-muted-foreground" />
                  <span className="truncate text-muted-foreground">{src.fileName}</span>
                </div>
                <Badge variant="secondary" className="shrink-0">
                  {Math.round(src.score * 100)}%
                </Badge>
              </div>
              <p className="text-muted-foreground leading-relaxed line-clamp-4">
                {src.content}
              </p>
            </div>
          ))}
        </div>
      </ScrollArea>
    </aside>
  );
}
```

### Data Library Page

```tsx
// src/pages/DataLibraryPage.tsx
import { useCallback } from 'react';
import { useDropzone } from 'react-dropzone'; // npm install react-dropzone
import { Upload, FileText, Loader2, CheckCircle, XCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { useDocuments, useUploadDocument } from '@/hooks/useDocuments';
import type { DocumentRecord } from '@/types';

const statusIcon = {
  READY:      <CheckCircle className="size-3.5 text-green-500" />,
  PROCESSING: <Loader2    className="size-3.5 animate-spin text-yellow-500" />,
  FAILED:     <XCircle    className="size-3.5 text-red-500" />,
};

export default function DataLibraryPage() {
  const { data: documents = [], isLoading } = useDocuments();
  const { mutate: upload, isPending }       = useUploadDocument();

  const onDrop = useCallback((files: File[]) => {
    files.forEach((file) => upload({ file }));
  }, [upload]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { 'application/pdf': ['.pdf'], 'text/csv': ['.csv'] },
    multiple: true,
  });

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-2xl font-semibold">Data Library</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Upload bank statements to make them searchable
        </p>
      </div>

      {/* Dropzone */}
      <div {...getRootProps()} className={cn(
        'border-2 border-dashed rounded-xl p-10 text-center cursor-pointer transition-colors',
        isDragActive ? 'border-primary bg-primary/5' : 'border-muted hover:border-muted-foreground/50'
      )}>
        <input {...getInputProps()} />
        <Upload className="size-8 mx-auto mb-3 text-muted-foreground" />
        <p className="text-sm font-medium">
          {isDragActive ? 'Drop files here' : 'Drag & drop PDF or CSV files'}
        </p>
        <p className="text-xs text-muted-foreground mt-1">or click to browse</p>
        {isPending && (
          <div className="mt-3 flex items-center justify-center gap-2 text-xs text-muted-foreground">
            <Loader2 className="size-3 animate-spin" /> Uploading…
          </div>
        )}
      </div>

      {/* Documents table */}
      <div className="rounded-lg border">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-muted/40 text-left">
              <th className="px-4 py-3 font-medium text-muted-foreground">File</th>
              <th className="px-4 py-3 font-medium text-muted-foreground">Type</th>
              <th className="px-4 py-3 font-medium text-muted-foreground">Size</th>
              <th className="px-4 py-3 font-medium text-muted-foreground">Status</th>
              <th className="px-4 py-3 font-medium text-muted-foreground">Uploaded</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-muted-foreground">
                <Loader2 className="size-4 animate-spin mx-auto" />
              </td></tr>
            ) : documents.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-muted-foreground text-xs">
                No documents yet. Upload a PDF or CSV to get started.
              </td></tr>
            ) : documents.map((doc: DocumentRecord) => (
              <tr key={doc.id} className="border-b last:border-0 hover:bg-muted/20">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <FileText className="size-4 text-muted-foreground shrink-0" />
                    <span className="truncate max-w-[200px]">{doc.fileName}</span>
                  </div>
                </td>
                <td className="px-4 py-3">
                  <Badge variant="outline" className="text-xs">{doc.fileType}</Badge>
                </td>
                <td className="px-4 py-3 text-muted-foreground">
                  {(doc.fileSize / 1024).toFixed(1)} KB
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-1.5">
                    {statusIcon[doc.status]}
                    <span className="capitalize text-xs">{doc.status.toLowerCase()}</span>
                  </div>
                </td>
                <td className="px-4 py-3 text-muted-foreground text-xs">
                  {new Date(doc.uploadedAt).toLocaleDateString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

---

## Authentication Flow

```
User visits /                     →  ProtectedRoute checks Zustand (persisted)
  Token missing                   →  Redirect to /login
  Token present                   →  Render AppLayout + Outlet

User submits login form           →  authApi.login()
  Success                         →  setAuth(user, token) → navigate('/')
  Failure                         →  toast.error(...)

Any API call returns 401          →  Axios interceptor → logout() → /login

User clicks logout                →  logout() → navigate('/login')
```

Token is stored via `zustand/middleware/persist` in `localStorage` under the key `auth-storage`.

---

## Theme (Dark/Light)

```typescript
// src/hooks/useTheme.ts
import { useEffect } from 'react';
import { useSettingsStore } from '@/store/settingsStore';

export function useTheme() {
  const { theme, setTheme } = useSettingsStore();

  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove('light', 'dark');
    if (theme === 'system') {
      const mq = window.matchMedia('(prefers-color-scheme: dark)');
      root.classList.add(mq.matches ? 'dark' : 'light');
    } else {
      root.classList.add(theme);
    }
  }, [theme]);

  return { theme, setTheme };
}
```

Call `useTheme()` once in `App.tsx` to apply on mount.

---

## Deployment

### Vite build

```bash
npm run build        # outputs to dist/
npm run preview      # preview the build locally
```

### Nginx (serve dist/)

```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;

  # Required for React Router (HTML5 history mode)
  location / {
    try_files $uri $uri/ /index.html;
  }

  # Proxy API calls to Spring Boot (avoids CORS in production)
  location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
  }
}
```

### Docker

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

Add a `frontend` service to the existing `docker-compose.yml`:

```yaml
frontend:
  build: ./frontend
  ports:
    - "3000:80"
  depends_on:
    - backend
```

---

## Suggested Improvements (v2)

- **Streaming responses** — swap `POST /api/chat/ask` for an SSE endpoint and use `EventSource` to stream tokens into the message bubble in real time, exactly like ChatGPT
- **Command palette** — `cmd+k` using shadcn `<Command>` to jump to any chat or document
- **Session management** — persist `sessionId` → chat messages in `localStorage` so chats survive refresh
- **Document preview** — inline PDF viewer using `react-pdf` so users can see the source document alongside the chat
- **Spending charts** — use `recharts` to visualize category breakdowns on the Home dashboard
- **Spring Security integration** — replace the mock `authApi` with real JWT-based Spring Security endpoints
- **WebSocket** — upgrade chat to WebSocket for true bidirectional streaming
