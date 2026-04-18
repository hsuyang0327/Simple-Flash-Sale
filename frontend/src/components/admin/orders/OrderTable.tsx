'use client';

import { Eye, Search } from 'lucide-react';
import { useState } from 'react';
import { OrderAdminResponse, OrderAdminPageResponse, OrderSearchParams } from '@/types/order';
import { OrderAdminService } from '@/services/orderAdminService';
import Link from 'next/link';

interface OrderTableProps {
  initialOrders: OrderAdminPageResponse | null;
}

export default function OrderTable({ initialOrders }: OrderTableProps) {
  const [orders, setOrders] = useState<OrderAdminResponse[]>(initialOrders?.content || []);
  const [currentPage, setCurrentPage] = useState(initialOrders?.number || 0);
  const [totalPages, setTotalPages] = useState(initialOrders?.totalPages || 0);
  const [totalElements, setTotalElements] = useState(initialOrders?.totalElements || 0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState<OrderSearchParams>({});

  const loadPage = async (page: number, params: OrderSearchParams = {}) => {
    setLoading(true);
    try {
      const data = await OrderAdminService.list({ ...params, page, size: pageSize });
      setOrders(data.content);
      setCurrentPage(data.number);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error) {
      console.error("Failed to load order list:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setSearchParams({ ...searchParams });
    loadPage(0, searchParams);
  };

  return (
    <div className="w-full space-y-8 p-4">
      {/* 搜尋欄 */}
      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">商品名稱</label>
            <input
              type="text"
              placeholder="搜尋商品名稱"
              value={searchParams.productName || ''}
              onChange={(e) => setSearchParams({ ...searchParams, productName: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">會員名稱</label>
            <input
              type="text"
              placeholder="搜尋會員名稱"
              value={searchParams.memberName || ''}
              onChange={(e) => setSearchParams({ ...searchParams, memberName: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div className="flex items-end">
            <button
              onClick={handleSearch}
              className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
              disabled={loading}
            >
              <Search size={16} />
              搜尋
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-separate border-spacing-0">
            <thead>
              <tr className="bg-slate-50/80">
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">商品名稱</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">會員名稱</th>
                <th className="px-6 py-6 text-xs font-black text-slate-500 border-b border-slate-100 text-center uppercase tracking-widest">詳細資料</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {orders.map((order, index) => (
                <tr key={order.orderId} className={`transition-all ${index % 2 === 0 ? 'bg-white' : 'bg-slate-50/30'} hover:bg-blue-50/40`}>
                  <td className="px-8 py-7">
                    <span className="text-sm text-slate-700 whitespace-nowrap">{order.productName}</span>
                  </td>
                  <td className="px-8 py-7">
                    <span className="text-sm text-slate-700 whitespace-nowrap">{order.memberName}</span>
                  </td>
                  <td className="px-6 py-7 text-center">
                    <Link
                      href={`/orders/${order.orderId}`}
                      className="p-2.5 bg-blue-50 text-blue-600 border border-blue-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-blue-100 inline-flex items-center"
                    >
                      <Eye size={15} />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 分頁控制 */}
      <div className="flex items-center justify-between bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-6">
        <div className="text-sm text-slate-600">
          顯示第 {currentPage * pageSize + 1} 到 {Math.min((currentPage + 1) * pageSize, totalElements)} 項，共 {totalElements} 項
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => loadPage(currentPage - 1, searchParams)}
            disabled={currentPage === 0 || loading}
            className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一頁
          </button>
          <span className="px-4 py-2 text-sm font-bold text-slate-900">
            第 {currentPage + 1} 頁，共 {totalPages} 頁
          </span>
          <button
            onClick={() => loadPage(currentPage + 1, searchParams)}
            disabled={currentPage >= totalPages - 1 || loading}
            className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一頁
          </button>
        </div>
      </div>
    </div>
  );
}