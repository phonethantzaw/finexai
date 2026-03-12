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
