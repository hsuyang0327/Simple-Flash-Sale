'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import { OrderClientService } from '@/services/orderClientService';
import { OrderClientDetailResponse } from '@/types/order';

const STATUS_LABEL: Record<string, string> = {
  PAID: '已付款',
  FAILED: '已失敗',
  CANCELLED: '已取消',
  PENDING: '處理中',
};

const STATUS_CLASS: Record<string, string> = {
  PAID: 'bg-green-100 text-green-700 border-green-200',
  FAILED: 'bg-red-100 text-red-700 border-red-200',
  CANCELLED: 'bg-gray-100 text-gray-500 border-gray-200',
  PENDING: 'bg-yellow-100 text-yellow-700 border-yellow-200',
};

const formatPrice = (price: number) =>
  `NT$ ${price.toLocaleString('zh-TW')}`;

export default function OrderDetail({ orderId }: { orderId: string }) {
  const [order, setOrder] = useState<OrderClientDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    OrderClientService.get(orderId)
      .then(setOrder)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) {
    return (
      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 text-sm text-gray-400">
        載入中...
      </div>
    );
  }

  if (!order) {
    return (
      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 space-y-4">
        <p className="text-sm text-gray-500">找不到此訂單。</p>
        <Link href="/member/orders" className="text-sm text-blue-600 hover:underline">
          ← 返回訂單列表
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <Link
        href="/member/orders"
        className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-blue-600 transition-colors"
      >
        <ArrowLeft size={14} />
        返回訂單列表
      </Link>

      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 space-y-6">
        <div className="border-b border-gray-100 pb-6">
          <h1 className="text-xl font-bold text-gray-900">訂單詳情</h1>
          <p className="text-xs text-gray-400 mt-1 font-mono">{order.orderId}</p>
        </div>

        <div className="space-y-5">
          <Row label="商品名稱" value={order.productName} />
          <Row label="數量" value={`x${order.quantity}`} />
          <Row label="總金額" value={formatPrice(order.totalPrice)} />
          <div className="flex items-center gap-4">
            <span className="w-20 text-sm font-semibold text-gray-500 shrink-0">狀態</span>
            <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-full border ${STATUS_CLASS[order.status] ?? 'bg-gray-100 text-gray-500'}`}>
              {STATUS_LABEL[order.status] ?? order.status}
            </span>
          </div>
          <Row
            label="建立時間"
            value={new Date(order.createdAt).toLocaleString('zh-TW')}
          />
        </div>
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center gap-4">
      <span className="w-20 text-sm font-semibold text-gray-500 shrink-0">{label}</span>
      <span className="text-sm text-gray-900">{value}</span>
    </div>
  );
}
