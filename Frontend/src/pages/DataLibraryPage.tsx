import { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, FileText, Loader2, CheckCircle, XCircle } from 'lucide-react';
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
  const { data: documents = [], isLoading, error } = useDocuments();
  const { mutate: upload, isPending }       = useUploadDocument();

  const documentList = Array.isArray(documents) ? documents : [];

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
            ) : error ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-muted-foreground text-xs">
                Unable to load documents. Make sure the backend is running.
              </td></tr>
            ) : documentList.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-muted-foreground text-xs">
                No documents yet. Upload a PDF or CSV to get started.
              </td></tr>
            ) : documentList.map((doc: DocumentRecord) => (
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
