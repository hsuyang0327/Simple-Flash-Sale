'use client';

import { Edit3, Trash2, Plus } from 'lucide-react';
import { useState } from 'react';
import Swal from 'sweetalert2';
import { EventResponse, EventPageResponse } from '@/types/event';
import { EventService } from '@/services/eventService';
import EventForm from './EventForm';

interface EventTableProps {
  productId: string;
  initialEvents?: EventPageResponse | null;
}

export default function EventTable({ productId, initialEvents }: EventTableProps) {
  const [events, setEvents] = useState<EventResponse[]>(initialEvents?.content || []);
  const [currentPage, setCurrentPage] = useState(initialEvents?.page?.number || 0);
  const [totalPages, setTotalPages] = useState(initialEvents?.page?.totalPages || 0);
  const [totalElements, setTotalElements] = useState(initialEvents?.page?.totalElements || 0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(false);

  // 新增/編輯 Modal 狀態
  const [showForm, setShowForm] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<EventResponse | null>(null);

  // 統一的 SweetAlert2 成功彈窗
  const showSuccess = (title: string, text: string) => {
    Swal.fire({
      title,
      text,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false,
      timerProgressBar: true,
      customClass: { popup: 'rounded-3xl' }
    });
  };

  const loadPage = async (page: number) => {
    setLoading(true);
    try {
      const data = await EventService.listByProductId(productId, page, pageSize);
      setEvents(data.content);
      setCurrentPage(data.page.number);
      setTotalPages(data.page.totalPages);
      setTotalElements(data.page.totalElements);
    } catch (error) {
      console.error("Failed to load event list:", error);
    } finally {
      setLoading(false);
    }
  };

  const refresh = async () => {
    await loadPage(currentPage);
  };

  const handleAddEvent = () => {
    setSelectedEvent(null);
    setShowForm(true);
  };

  const handleEditEvent = (event: EventResponse) => {
    setSelectedEvent(event);
    setShowForm(true);
  };

  const handleFormSuccess = async () => {
    showSuccess(
      selectedEvent ? '更新成功' : '新增成功',
      selectedEvent ? '活動已更新' : '活動已新增'
    );
    setShowForm(false);
    await refresh();
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setSelectedEvent(null);
  };

  const handleDelete = async (event: EventResponse) => {
    const result = await Swal.fire({
      title: '確認刪除',
      text: `確定要刪除此活動嗎？`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: '刪除',
      cancelButtonText: '取消',
      customClass: { popup: 'rounded-3xl' }
    });

    if (result.isConfirmed) {
      try {
        await EventService.delete(event.eventId);
        showSuccess('刪除成功', '活動已刪除');
        await refresh();
      } catch (error) {
        console.error("Delete event failed:", error);
      }
    }
  };

  const getStatusBadge = (status: number) => {
    switch (status) {
      case 0:
        return <span className="inline-flex px-3 py-1 bg-amber-100 text-amber-700 text-xs font-bold rounded-full border border-amber-200">即將開始</span>;
      case 1:
        return <span className="inline-flex px-3 py-1 bg-emerald-100 text-emerald-700 text-xs font-bold rounded-full border border-emerald-200">進行中</span>;
      case 2:
        return <span className="inline-flex px-3 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-full border border-red-200">已結束</span>;
      default:
        return <span className="inline-flex px-3 py-1 bg-gray-100 text-gray-700 text-xs font-bold rounded-full border border-gray-200">未知</span>;
    }
  };

  const formatDateTime = (dateTimeString: string) => {
    return new Date(dateTimeString).toLocaleString('zh-TW');
  };

  return (
    <div className="w-full space-y-8">
      {/* 新增按鈕 */}
      <div className="flex justify-end">
        <button
          onClick={handleAddEvent}
          className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white text-sm font-bold rounded-xl shadow-sm transition-all active:scale-90 hover:bg-blue-700"
        >
          <Plus size={16} />
          新增活動
        </button>
      </div>

      {/* 活動表格 */}
      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-separate border-spacing-0">
            <thead>
              <tr className="bg-slate-50/80">
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">價格</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">庫存</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">開始時間</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">結束時間</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">狀態</th>
                <th className="px-6 py-6 text-xs font-black text-slate-500 border-b border-slate-100 text-center uppercase tracking-widest">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {events.length === 0 ? (
                <tr className="bg-white hover:bg-slate-50">
                  <td colSpan={6} className="px-8 py-12 text-center text-sm text-slate-400">
                    沒有活動資料
                  </td>
                </tr>
              ) : (
                events.map((event, index) => (
                  <tr key={event.eventId} className={`transition-all ${index % 2 === 0 ? 'bg-white' : 'bg-slate-50/30'} hover:bg-blue-50/40`}>
                    <td className="px-8 py-7">
                      <span className="text-sm font-bold text-slate-900">${event.price}</span>
                    </td>
                    <td className="px-8 py-7">
                      <span className="text-sm text-slate-600">{event.stock} 件</span>
                    </td>
                    <td className="px-8 py-7">
                      <span className="text-sm text-slate-600 whitespace-nowrap">{formatDateTime(event.startTime)}</span>
                    </td>
                    <td className="px-8 py-7">
                      <span className="text-sm text-slate-600 whitespace-nowrap">{formatDateTime(event.endTime)}</span>
                    </td>
                    <td className="px-8 py-7">
                      {getStatusBadge(event.status)}
                    </td>
                    <td className="px-6 py-7 text-center">
                      <div className="flex justify-center gap-2.5">
                        <button
                          onClick={() => handleEditEvent(event)}
                          className="p-2.5 bg-blue-50 text-blue-600 border border-blue-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-blue-100 disabled:opacity-50 disabled:cursor-not-allowed"
                          disabled={loading || event.status === 1}
                          title={event.status === 1 ? "活動進行中不可編輯" : undefined}
                        >
                          <Edit3 size={15} />
                        </button>
                        <button
                          onClick={() => handleDelete(event)}
                          className="p-2.5 bg-red-50 text-red-600 border border-red-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-red-100 disabled:opacity-50 disabled:cursor-not-allowed"
                          disabled={loading || event.status === 1}
                          title={event.status === 1 ? "活動進行中不可刪除" : undefined}
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* 分頁控制 */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-6">
          <div className="text-sm text-slate-600">
            顯示第 {currentPage * pageSize + 1} 到 {Math.min((currentPage + 1) * pageSize, totalElements)} 項，共 {totalElements} 項
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => loadPage(currentPage - 1)}
              disabled={currentPage === 0 || loading}
              className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              上一頁
            </button>
            <span className="px-4 py-2 text-sm font-bold text-slate-900">
              第 {currentPage + 1} 頁，共 {totalPages} 頁
            </span>
            <button
              onClick={() => loadPage(currentPage + 1)}
              disabled={currentPage >= totalPages - 1 || loading}
              className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              下一頁
            </button>
          </div>
        </div>
      )}

      {/* 活動表單 Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-[2.5rem] shadow-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto p-8">
            <div>
              <h2 className="text-2xl font-bold text-slate-900 mb-2">
                {selectedEvent ? '編輯活動' : '新增活動'}
              </h2>
              <div className="h-1 w-16 bg-blue-200 rounded-full mb-6"></div>
            </div>
            <EventForm
              productId={productId}
              initialEvent={selectedEvent}
              onSuccess={handleFormSuccess}
              onCancel={handleFormCancel}
            />
          </div>
        </div>
      )}
    </div>
  );
}
