'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import Swal from 'sweetalert2';
import { ChevronRight } from 'lucide-react';
import { OrderClientService } from '@/services/orderClientService';
import { OrderClientDetailResponse } from '@/types/order';

const STATUS_LABEL: Record<string, string> = {
  PAID: '已付款',
  FAILED: '已失敗',
  CANCELLED: '已取消',
};

const STATUS_CLASS: Record<string, string> = {
  PAID: 'bg-green-100 text-green-700 border-green-200',
  FAILED: 'bg-red-100 text-red-700 border-red-200',
  CANCELLED: 'bg-gray-100 text-gray-500 border-gray-200',
};

const formatPrice = (price: number) =>
  `NT$ ${price.toLocaleString('zh-TW')}`;

export default function OrderList() {
  const [orders, setOrders] = useState<OrderClientDetailResponse[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const pageSize = 10;

  const loadPage = async (page: number) => {
    setLoading(true);
    try {
      const data = await OrderClientService.list(page, pageSize);
      setOrders(data.content);
      setCurrentPage(data.number);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch {
      // 錯誤由 http.ts toast 處理
    } finally {
      setLoading(false);
      setInitialLoading(false);
    }
  };

  useEffect(() => {
    loadPage(0);
  }, []);

  const handleCancel = async (order: OrderClientDetailResponse) => {
    const result = await Swal.fire({
      title: '確認取消訂單',
      text: `確定要取消「${order.productName}」的訂單嗎？`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6b7280',
      confirmButtonText: '取消訂單',
      cancelButtonText: '返回',
      customClass: { popup: 'rounded-3xl' },
    });

    if (!result.isConfirmed) return;

    try {
      await OrderClientService.cancel(order.orderId);
      Swal.fire({
        title: '已取消',
        text: '訂單已成功取消',
        icon: 'success',
        timer: 1800,
        showConfirmButton: false,
        customClass: { popup: 'rounded-3xl' },
      });
      await loadPage(currentPage);
    } catch {
      // 錯誤由 http.ts toast 處理
    }
  };

  return (
    <div className="flex flex-col h-full gap-6">
      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm overflow-hidden flex-1">
        <div className="px-8 py-6 border-b border-gray-100">
          <h1 className="text-xl font-bold text-gray-900">我的訂單</h1>
          <p className="text-xs text-gray-400 mt-1">共 {totalElements} 筆訂單</p>
        </div>

        {initialLoading ? (
          <div className="px-8 py-16 text-center text-sm text-gray-400">載入中...</div>
        ) : orders.length === 0 ? (
          <div className="px-8 py-16 text-center text-sm text-gray-400">
            目前沒有訂單紀錄
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-separate border-spacing-0">
              <thead>
                <tr className="bg-gray-50/80">
                  <th className="px-8 py-4 text-xs font-black text-gray-500 uppercase tracking-widest border-b border-gray-100">商品名稱</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-widest border-b border-gray-100">數量</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-widest border-b border-gray-100">金額</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-widest border-b border-gray-100">狀態</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-widest border-b border-gray-100">建立時間</th>
                  <th className="px-6 py-4 text-xs font-black text-gray-500 uppercase tracking-widest border-b border-gray-100 text-center">操作</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {orders.map((order, index) => (
                  <tr
                    key={order.orderId}
                    className={`transition-all ${index % 2 === 0 ? 'bg-white' : 'bg-gray-50/30'} hover:bg-blue-50/30`}
                  >
                    <td className="px-8 py-5">
                      <span className="text-sm font-semibold text-gray-900">{order.productName}</span>
                    </td>
                    <td className="px-6 py-5">
                      <span className="text-sm text-gray-700">x{order.quantity}</span>
                    </td>
                    <td className="px-6 py-5">
                      <span className="text-sm text-gray-900">{formatPrice(order.totalPrice)}</span>
                    </td>
                    <td className="px-6 py-5">
                      <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-full border ${STATUS_CLASS[order.status] ?? 'bg-gray-100 text-gray-500'}`}>
                        {STATUS_LABEL[order.status] ?? order.status}
                      </span>
                    </td>
                    <td className="px-6 py-5">
                      <span className="text-sm text-gray-500">
                        {new Date(order.createdAt).toLocaleDateString('zh-TW')}
                      </span>
                    </td>
                    <td className="px-6 py-5">
                      <div className="flex items-center justify-center gap-2">
                        {order.status === 'PAID' && (
                          <button
                            onClick={() => handleCancel(order)}
                            disabled={loading}
                            className="px-3 py-1.5 text-xs font-bold text-red-600 bg-red-50 hover:bg-red-100 border border-red-200 rounded-xl transition-all disabled:opacity-50"
                          >
                            取消
                          </button>
                        )}
                        <Link
                          href={`/member/orders/${order.orderId}`}
                          className="p-1.5 text-blue-600 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-xl transition-all inline-flex items-center"
                        >
                          <ChevronRight size={14} />
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* 分頁 */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between bg-white border border-gray-100 rounded-2xl shadow-sm px-6 py-4">
          <span className="text-sm text-gray-500">
            第 {currentPage + 1} 頁，共 {totalPages} 頁
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => loadPage(currentPage - 1)}
              disabled={currentPage === 0 || loading}
              className="px-4 py-2 text-sm font-semibold bg-gray-50 text-gray-600 border border-gray-200 rounded-xl hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
            >
              上一頁
            </button>
            <button
              onClick={() => loadPage(currentPage + 1)}
              disabled={currentPage >= totalPages - 1 || loading}
              className="px-4 py-2 text-sm font-semibold bg-gray-50 text-gray-600 border border-gray-200 rounded-xl hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
            >
              下一頁
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
