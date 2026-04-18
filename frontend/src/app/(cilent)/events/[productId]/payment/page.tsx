'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Swal from 'sweetalert2';
import { OrderClientService } from '@/services/orderClientService';
import { OrderClientDetailResponse } from '@/types/order';

const PAYMENT_DEADLINE_MS = 10 * 60 * 1000; // 與後端 TTL 同步：10 分鐘

function calcRemaining(createdAt: string): number {
  const deadline = new Date(createdAt).getTime() + PAYMENT_DEADLINE_MS;
  return Math.max(0, deadline - Date.now());
}

function formatCountdown(ms: number): string {
  const totalSec = Math.ceil(ms / 1000);
  const min = Math.floor(totalSec / 60);
  const sec = totalSec % 60;
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
}

export default function PaymentPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const orderId = searchParams.get('orderId') ?? '';

  const [order, setOrder] = useState<OrderClientDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [remaining, setRemaining] = useState<number>(PAYMENT_DEADLINE_MS);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const paidRef = useRef(false);
  const isShowingLeaveDialogRef = useRef(false);

  // 離頁警告（關閉分頁 / 重整）
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (paidRef.current) return;
      e.preventDefault();
      e.returnValue = '離開此頁面將導致訂單失效，確定要離開嗎？';
    };
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, []);

  // 攔截瀏覽器上一頁（SPA popstate）
  useEffect(() => {
    window.history.pushState(null, '', window.location.href);
    const handlePopState = () => {
      if (paidRef.current) return;
      // 永遠先推回假狀態，鎖住頁面
      window.history.pushState(null, '', window.location.href);
      // 已有彈窗開著就不重複開
      if (isShowingLeaveDialogRef.current) return;
      isShowingLeaveDialogRef.current = true;
      Swal.fire({
        title: '確定要離開付款頁面？',
        text: '離開後此訂單將在 10 分鐘內自動失效，庫存將歸還。',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確定離開',
        cancelButtonText: '繼續付款',
        confirmButtonColor: '#d33',
        cancelButtonColor: '#2563eb',
        customClass: { popup: 'rounded-3xl' },
      }).then((result) => {
        isShowingLeaveDialogRef.current = false;
        if (result.isConfirmed) {
          paidRef.current = true;
          router.push('/home');
        }
      });
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, [router]);

  useEffect(() => {
    if (!orderId) return;
    OrderClientService.get(orderId)
      .then((data) => {
        setOrder(data);
        setRemaining(calcRemaining(data.createdAt));
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [orderId]);

  // 倒數計時
  useEffect(() => {
    if (!order) return;

    timerRef.current = setInterval(() => {
      const rem = calcRemaining(order.createdAt);
      setRemaining(rem);
      if (rem === 0) {
        clearInterval(timerRef.current!);
        paidRef.current = true; // 解除離頁警告，倒數到期視同正常結束
        Swal.fire({
          icon: 'warning',
          title: '付款時間已到',
          text: '訂單已自動取消，庫存已歸還。',
          confirmButtonText: '返回首頁',
          confirmButtonColor: '#2563eb',
          allowOutsideClick: false,
        }).then(() => router.push('/home'));
      }
    }, 1000);

    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [order, router]);

  const handlePay = async () => {
    if (!orderId) return;
    setPaying(true);
    try {
      await OrderClientService.pay(orderId);
      paidRef.current = true;
      clearInterval(timerRef.current!);
      await Swal.fire({
        icon: 'success',
        title: '付款成功！',
        text: '感謝您的購買',
        confirmButtonText: '查看訂單',
        confirmButtonColor: '#2563eb',
      });
      router.push('/member/orders');
    } catch {
      // error handled by http.ts toast
    } finally {
      setPaying(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-400 text-sm">載入中...</p>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-400 text-sm">找不到訂單資訊</p>
      </div>
    );
  }

  const isUrgent = remaining < 60 * 1000; // 不到 1 分鐘時變紅

  return (
    <div className="max-w-lg mx-auto px-4 py-10">
      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 space-y-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-bold text-gray-900">訂單確認</h1>
            <p className="text-xs text-gray-400 mt-1 font-mono">#{order.orderId}</p>
          </div>
          <div className={`text-right ${isUrgent ? 'text-red-500' : 'text-orange-500'}`}>
            <p className="text-xs font-semibold">付款倒數</p>
            <p className={`text-2xl font-black tabular-nums ${isUrgent ? 'animate-pulse' : ''}`}>
              {formatCountdown(remaining)}
            </p>
          </div>
        </div>

        <div className="space-y-3 text-sm border-t border-gray-50 pt-4">
          <div className="flex justify-between">
            <span className="text-gray-500">商品</span>
            <span className="font-medium text-gray-900">{order.productName}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">數量</span>
            <span className="font-medium text-gray-900">{order.quantity} 件</span>
          </div>
          <div className="flex justify-between border-t border-gray-50 pt-3">
            <span className="text-gray-700 font-semibold">應付金額</span>
            <span className="text-blue-700 font-bold text-lg">${order.totalPrice.toLocaleString()}</span>
          </div>
        </div>

        <button
          onClick={handlePay}
          disabled={paying || remaining === 0}
          className="w-full py-3 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm transition-all disabled:opacity-50"
        >
          {paying ? '處理中...' : '確認付款'}
        </button>

        <p className="text-center text-xs text-gray-300">此為模擬付款，不會產生實際扣款</p>
        <p className="text-center text-xs text-orange-400 font-medium">⚠ 離開此頁面將導致訂單失效，請盡快完成付款</p>
      </div>
    </div>
  );
}

