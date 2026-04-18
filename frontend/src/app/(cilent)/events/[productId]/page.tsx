'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { FlashSaleService } from '@/services/flashSaleService';
import { OrderClientService } from '@/services/orderClientService';
import { EventProductDTO } from '@/types/event';
import Swal from 'sweetalert2';

function formatTime(iso: string): string {
  if (!iso) return '-';
  const d = new Date(iso);
  return d.toLocaleString('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

export default function EventDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = params.productId as string;

  const [product, setProduct] = useState<EventProductDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [submitting, setSubmitting] = useState(false);
  const [soldOut, setSoldOut] = useState(false);

  useEffect(() => {
    FlashSaleService.getProduct(productId)
      .then(data => {
        setProduct(data);
        setSoldOut(parseInt(data.stock) <= 0);
      })
      .catch(() => {/* error handled by http.ts toast */})
      .finally(() => setLoading(false));
  }, [productId]);

  const handlePurchase = async () => {
    if (!product) return;
    setSubmitting(true);
    try {
      await OrderClientService.createOrder(product.eventId, quantity);
      router.push(`/events/${productId}/waiting?eventId=${product.eventId}`);
    } catch (err: unknown) {
      // 401 (auth error) 已由 http.ts 統一處理彈窗，這裡不重複顯示
      const status = (err as { response?: { status?: number } })?.response?.status;
      if (status !== 401) {
        // 業務碼以 HTTP 200 回傳，http.ts reject 時包成 Error，message 即 ResultCode.message
        // stock_invalid (4609) = 數量超過剩餘，但還有庫存
        // stock_sold_out (4620) = 真的 0 庫存
        const msg = (err as Error)?.message;
        if (msg === 'stock_invalid') {
          Swal.fire({
            title: '數量不足',
            text: '您選擇的數量超過剩餘庫存，請重新選擇。',
            icon: 'warning',
            confirmButtonText: '確定',
            customClass: { popup: 'rounded-3xl' },
          });
        } else {
          setSoldOut(true);
          Swal.fire({
            title: '很抱歉',
            text: '商品已售完，感謝您的參與！',
            icon: 'error',
            confirmButtonText: '確定',
            customClass: { popup: 'rounded-3xl' },
          });
        }
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-400 text-sm">載入中...</p>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-400 text-sm">找不到此活動</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <button
        onClick={() => router.push('/home')}
        className="text-sm text-gray-400 hover:text-blue-600 mb-6 flex items-center gap-1"
      >
        ← 返回列表
      </button>

      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{product.productName}</h1>
          <p className="text-gray-500 mt-2 text-sm">{product.description}</p>
        </div>

        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-400 text-xs uppercase tracking-wider mb-1">活動價格</p>
            <p className="text-blue-700 font-bold text-xl">${parseFloat(product.price).toLocaleString()}</p>
          </div>
          <div>
            <p className="text-gray-400 text-xs uppercase tracking-wider mb-1">剩餘庫存</p>
            <p className={`font-semibold ${soldOut ? 'text-gray-400' : 'text-green-600'}`}>
              {soldOut ? '已售完' : `${product.stock} 件`}
            </p>
          </div>
          <div>
            <p className="text-gray-400 text-xs uppercase tracking-wider mb-1">活動截止</p>
            <p className="text-gray-700">{formatTime(product.endTime)}</p>
          </div>
        </div>

        {!soldOut && (
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">購買數量</label>
            <select
              value={quantity}
              onChange={e => setQuantity(parseInt(e.target.value))}
              disabled={submitting}
              className="border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {[1, 2, 3, 4, 5].map(n => (
                <option key={n} value={n}>{n}</option>
              ))}
            </select>
          </div>
        )}

        {soldOut ? (
          <div className="w-full py-3 rounded-xl bg-gray-100 text-gray-400 font-semibold text-center text-sm">
            本場次已售完
          </div>
        ) : (
          <button
            onClick={handlePurchase}
            disabled={submitting}
            className="w-full py-3 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm transition-all disabled:opacity-50"
          >
            {submitting ? '送出中...' : '立即搶購'}
          </button>
        )}
      </div>
    </div>
  );
}
