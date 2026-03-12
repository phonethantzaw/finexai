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
