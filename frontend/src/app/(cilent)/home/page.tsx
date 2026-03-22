'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { FlashSaleService } from '@/services/flashSaleService';
import { EventProductDTO } from '@/types/event';

function formatTime(iso: string): string {
  if (!iso) return '-';
  const d = new Date(iso);
  return d.toLocaleString('zh-TW', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

export default function ClientHome() {
  const [products, setProducts] = useState<EventProductDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    FlashSaleService.list(0, 20)
      .then(res => setProducts(res.content))
      .catch(() => {/* error handled by http.ts toast */})
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-400 text-sm">載入中...</p>
      </div>
    );
  }

  if (products.length === 0) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-400 text-sm">目前沒有進行中的閃購活動</p>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold tracking-tight text-gray-900 mb-2">閃購活動</h1>
      <p className="text-xs text-gray-400 uppercase tracking-widest italic font-mono mb-8">Flash Sale Events</p>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map(product => {
          const soldOut = parseInt(product.stock) <= 0;
          return (
            <div
              key={product.productId}
              className="bg-white border border-gray-100 rounded-2xl shadow-sm p-6 flex flex-col gap-3"
            >
              <div>
                <h2 className="text-lg font-bold text-gray-900 truncate">{product.productName}</h2>
                <p className="text-xs text-gray-400 mt-1 line-clamp-2">{product.description}</p>
              </div>

              <div className="flex items-center justify-between text-sm">
                <span className="font-bold text-blue-700 text-base">${parseFloat(product.price).toLocaleString()}</span>
                <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${soldOut ? 'bg-gray-100 text-gray-400' : 'bg-green-50 text-green-600'}`}>
                  {soldOut ? '售完' : `剩 ${product.stock} 件`}
                </span>
              </div>

              <p className="text-xs text-gray-400">截止：{formatTime(product.endTime)}</p>

              {soldOut ? (
                <button
                  disabled
                  className="w-full py-2 rounded-xl bg-gray-100 text-gray-400 font-semibold text-sm cursor-not-allowed"
                >
                  本場次已售完
                </button>
              ) : (
                <Link
                  href={`/events/${product.productId}`}
                  className="w-full py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm text-center transition-all"
                >
                  立即搶購
                </Link>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
