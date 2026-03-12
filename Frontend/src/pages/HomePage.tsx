import { Link } from 'react-router-dom';
import { useUser } from '@clerk/react';
import { MessageSquarePlus, Library, FileText } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function HomePage() {
  const { user } = useUser();
  const name = user?.firstName ?? user?.fullName ?? 'there';

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-3xl font-semibold">Welcome back, {name}!</h1>
        <p className="text-muted-foreground mt-1">
          Ask questions about your financial documents or upload new ones.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <MessageSquarePlus className="size-8 mb-2 text-primary" />
            <CardTitle>Start a Chat</CardTitle>
            <CardDescription>
              Ask questions about your bank statements
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Link to="/app/chat" className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground shadow hover:bg-primary/90 h-9 px-4 py-2 w-full">
              New Chat
            </Link>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <Library className="size-8 mb-2 text-primary" />
            <CardTitle>Data Library</CardTitle>
            <CardDescription>
              Upload and manage your documents
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Link to="/app/library" className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-input bg-background shadow-sm hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2 w-full">
              View Library
            </Link>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <FileText className="size-8 mb-2 text-primary" />
            <CardTitle>Quick Stats</CardTitle>
            <CardDescription>
              Your document overview
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">0</p>
            <p className="text-xs text-muted-foreground">Documents uploaded</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
