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
