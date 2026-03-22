'use client';

import { useEffect, useRef, useState } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { OrderClientService } from '@/services/orderClientService';

const POLL_INTERVAL_MS = 2000;
const POLL_TIMEOUT_MS = 30000;

export default function WaitingPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const productId = params.productId as string;
  const eventId = searchParams.get('eventId') ?? '';

  const [timedOut, setTimedOut] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!eventId) return;

    // Start polling
    intervalRef.current = setInterval(async () => {
      try {
        const res = await OrderClientService.pollStatus(eventId);
        if (res.status === 'SUCCESS' && res.order) {
          clearInterval(intervalRef.current!);
          clearTimeout(timeoutRef.current!);
          router.push(`/events/${productId}/payment?orderId=${res.order.orderId}`);
        }
      } catch {
        // error handled by http.ts toast
      }
    }, POLL_INTERVAL_MS);

    // Timeout guard
    timeoutRef.current = setTimeout(() => {
      clearInterval(intervalRef.current!);
      setTimedOut(true);
    }, POLL_TIMEOUT_MS);

    return () => {
      clearInterval(intervalRef.current!);
      clearTimeout(timeoutRef.current!);
    };
  }, [eventId, productId, router]);

  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] gap-6 px-4">
      {!timedOut ? (
        <>
          <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
          <div className="text-center space-y-1">
            <p className="text-gray-800 font-semibold text-lg">搶購成功！</p>
            <p className="text-gray-400 text-sm">訂單處理中，請稍候...</p>
            <p className="text-gray-300 text-xs">每 2 秒自動確認一次</p>
          </div>
        </>
      ) : (
        <div className="text-center space-y-4">
          <p className="text-gray-700 font-semibold">訂單確認時間較長</p>
          <p className="text-gray-400 text-sm">系統仍在處理中，請稍後至「我的訂單」查詢</p>
          <button
            onClick={() => router.push('/member/orders')}
            className="px-6 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm transition-all"
          >
            前往我的訂單
          </button>
        </div>
      )}
    </div>
  );
}
