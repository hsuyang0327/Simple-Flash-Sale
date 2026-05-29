'use client';

import Header from '@/components/client/Header';
import Footer from '@/components/client/Footer';
import { useAuthHealthCheck } from '@/hooks/useAuthHealthCheck';

export default function ClientLayout({ children }: { children: React.ReactNode }) {
  useAuthHealthCheck();

  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1 container mx-auto px-4 py-8">{children}</main>
      <Footer />
    </div>
  );
}
