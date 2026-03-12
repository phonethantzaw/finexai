import { createBrowserRouter, Navigate } from 'react-router-dom';
import { useAuth } from '@clerk/react';
import AppLayout from '@/components/layout/AppLayout';
import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import HomePage from '@/pages/HomePage';
import NewChatPage from '@/pages/NewChatPage';
import DataLibraryPage from '@/pages/DataLibraryPage';
import LandingPage from '@/pages/LandingPage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isSignedIn, isLoaded } = useAuth();
  if (!isLoaded) return null;
  if (!isSignedIn) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export const router = createBrowserRouter([
  { path: '/',       element: <LandingPage /> },
  { path: '/login',  element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
  {
    path: '/app',
    element: <ProtectedRoute><AppLayout /></ProtectedRoute>,
    children: [
      { index: true,           element: <Navigate to="/app/home" replace /> },
      { path: 'home',          element: <HomePage /> },
      { path: 'chat',          element: <NewChatPage /> },
      { path: 'chat/:sessionId', element: <NewChatPage /> },
      { path: 'library',       element: <DataLibraryPage /> },
    ],
  },
]);
