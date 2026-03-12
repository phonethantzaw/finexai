import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { useAuth } from '@clerk/react';
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar';
import { setClerkTokenGetter } from '@/api/client';
import { AppSidebar } from './Sidebar';
import { Topbar } from './Topbar';

export default function AppLayout() {
  const { getToken } = useAuth();

  useEffect(() => {
    setClerkTokenGetter(getToken);
  }, [getToken]);

  return (
    <SidebarProvider
      style={
        {
          '--sidebar-width': '16rem',
          '--sidebar-width-icon': '3rem',
        } as React.CSSProperties
      }
    >
      <AppSidebar />
      <SidebarInset>
        <Topbar />
        <main className="flex flex-1 flex-col gap-4 p-6">
          <Outlet />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}
