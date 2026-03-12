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
