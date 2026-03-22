'use client';

import { useState } from 'react';
// import Swal from 'sweetalert2';
import { EventResponse, EventRequest } from '@/types/event';
import { EventService } from '@/services/eventService';

interface EventFormProps {
  productId: string;
  initialEvent?: EventResponse | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export default function EventForm({ productId, initialEvent, onSuccess, onCancel }: EventFormProps) {
  const isEditMode = !!initialEvent;

  const [formData, setFormData] = useState<EventRequest>({
    productId,
    price: initialEvent?.price ?? 0,
    stock: initialEvent?.stock ?? 0,
    startTime: initialEvent ? new Date(initialEvent.startTime).toISOString().slice(0, 16) : '',
    endTime: initialEvent ? new Date(initialEvent.endTime).toISOString().slice(0, 16) : '',
    status: initialEvent?.status ?? 0,
  });

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: (name === 'price' || name === 'stock' || name === 'status')
        ? parseFloat(value) || 0
        : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const newErrors: { [key: string]: string } = {};
    if (!formData.productId.trim()) {
      newErrors.productId = '商品 ID 遺失';
    }
    if (
      formData.price === null ||
      formData.price === undefined ||
      isNaN(formData.price) ||
      formData.price < 0
    ) {
      newErrors.price = '價格為必填且不可為負數';
    }
    if (
      formData.stock === null ||
      formData.stock === undefined ||
      isNaN(formData.stock) ||
      formData.stock < 0
    ) {
      newErrors.stock = '庫存為必填且不可為負數';
    }
    if (!formData.startTime) {
      newErrors.startTime = '開始時間為必填';
    }
    if (!formData.endTime) {
      newErrors.endTime = '結束時間為必填';
    }
    if (formData.startTime && formData.endTime && new Date(formData.startTime) >= new Date(formData.endTime)) {
      newErrors.endTime = '結束時間必須晚於開始時間';
    }
    setErrors(newErrors);
    if (Object.keys(newErrors).length > 0) {
      setLoading(false);
      return;
    }
    try {
      if (isEditMode && initialEvent) {
        await EventService.update(initialEvent.eventId, formData);
      } else {
        await EventService.create(formData);
      }
      if (onSuccess) onSuccess();
    } catch (error) {
      console.error("Form submission failed:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (onCancel) onCancel();
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* 價格 */}
      <div>
        <label className="block text-sm font-bold text-slate-900 mb-2">
          價格 <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          name="price"
          value={formData.price}
          onChange={handleChange}
          placeholder="輸入價格"
          disabled={loading}
          className={`w-full px-4 py-2.5 border ${errors.price ? 'border-red-500' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50`}
        />
        {errors.price && <p className="mt-1 text-xs text-red-600 font-bold">{errors.price}</p>}
      </div>

      {/* 庫存 */}
      <div>
        <label className="block text-sm font-bold text-slate-900 mb-2">
          庫存 <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          name="stock"
          value={formData.stock}
          onChange={handleChange}
          placeholder="輸入庫存數量"
          disabled={loading}
          className={`w-full px-4 py-2.5 border ${errors.stock ? 'border-red-500' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50`}
        />
        {errors.stock && <p className="mt-1 text-xs text-red-600 font-bold">{errors.stock}</p>}
      </div>

      {/* 開始時間 */}
      <div>
        <label className="block text-sm font-bold text-slate-900 mb-2">
          開始時間 <span className="text-red-500">*</span>
        </label>
        <input
          type="datetime-local"
          name="startTime"
          value={formData.startTime}
          onChange={handleChange}
          disabled={loading}
          className={`w-full px-4 py-2.5 border ${errors.startTime ? 'border-red-500' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50`}
        />
        {errors.startTime && <p className="mt-1 text-xs text-red-600 font-bold">{errors.startTime}</p>}
      </div>

      {/* 結束時間 */}
      <div>
        <label className="block text-sm font-bold text-slate-900 mb-2">
          結束時間 <span className="text-red-500">*</span>
        </label>
        <input
          type="datetime-local"
          name="endTime"
          value={formData.endTime}
          onChange={handleChange}
          disabled={loading}
          className={`w-full px-4 py-2.5 border ${errors.endTime ? 'border-red-500' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50`}
        />
        {errors.endTime && <p className="mt-1 text-xs text-red-600 font-bold">{errors.endTime}</p>}
      </div>

      {/* 狀態 */}
      <div>
        <label className="block text-sm font-bold text-slate-900 mb-2">活動狀態</label>
        <select
          name="status"
          value={formData.status}
          onChange={handleChange}
          disabled={loading}
          className="w-full px-4 py-2.5 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50"
        >
          <option value={0}>即將開始</option>
          <option value={1}>進行中</option>
          <option value={2}>已結束</option>
        </select>
      </div>

      {/* 按鈕 */}
      <div className="flex gap-3 pt-4 border-t border-slate-100">
        <button
          type="button"
          onClick={handleCancel}
          disabled={loading}
          className="flex-1 px-4 py-2.5 bg-slate-100 text-slate-600 font-bold rounded-xl shadow-sm transition-all hover:bg-slate-200 disabled:opacity-50"
        >
          取消
        </button>
        <button
          type="submit"
          disabled={loading}
          className="flex-1 px-4 py-2.5 bg-blue-600 text-white font-bold rounded-xl shadow-sm transition-all hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? '提交中...' : isEditMode ? '更新' : '新增'}
        </button>
      </div>
    </form>
  );
}
