'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import Swal from 'sweetalert2';
import { OrderClientService } from '@/services/orderClientService';
import { OrderClientDetailResponse } from '@/types/order';

export default function PaymentPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const productId = params.productId as string;
  const orderId = searchParams.get('orderId') ?? '';

  const [order, setOrder] = useState<OrderClientDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);

  useEffect(() => {
    if (!orderId) return;
    OrderClientService.get(orderId)
      .then(setOrder)
      .catch(() => {/* error handled by http.ts toast */})
      .finally(() => setLoading(false));
  }, [orderId]);

  const handlePay = async () => {
    if (!orderId) return;
    setPaying(true);
    try {
      await OrderClientService.pay(orderId);
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

  return (
    <div className="max-w-lg mx-auto px-4 py-10">
      <button
        onClick={() => router.push(`/events/${productId}`)}
        className="text-sm text-gray-400 hover:text-blue-600 mb-6 flex items-center gap-1"
      >
        ← 返回商品
      </button>

      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 space-y-6">
        <div>
          <h1 className="text-xl font-bold text-gray-900">訂單確認</h1>
          <p className="text-xs text-gray-400 mt-1 font-mono">#{order.orderId}</p>
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
          disabled={paying}
          className="w-full py-3 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm transition-all disabled:opacity-50"
        >
          {paying ? '處理中...' : '確認付款'}
        </button>

        <p className="text-center text-xs text-gray-300">此為模擬付款，不會產生實際扣款</p>
      </div>
    </div>
  );
}
