'use client';

import { useEffect } from 'react';
import http from '@/lib/http';
import { SilentAuthConfig } from '@/lib/http';

const CHECK_INTERVAL_MS = 14 * 60 * 1000; // 14 minutes — just under access token TTL (15 min)

export function useAuthHealthCheck() {
  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | null = null;

    const check = () => {
      // No _silentAuth — let the interceptor handle 4002/4003/4004 popups normally
      http.get('/client/member/me').catch(() => {
        // Interceptor already handles auth errors; ignore all other errors here
      });
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') check();
    };

    // Only start health check if user is already logged in
    http.get<{ memberId?: string }>('/client/member/me', { _silentAuth: true } as SilentAuthConfig)
      .then(data => {
        if (data?.memberId) {
          timer = setInterval(check, CHECK_INTERVAL_MS);
          document.addEventListener('visibilitychange', handleVisibilityChange);
        }
      })
      .catch(() => {
        // Not logged in, don't start timer
      });

    return () => {
      if (timer) clearInterval(timer);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);
}
