'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Swal from 'sweetalert2';
import { ProductAdminResponse, ProductRequest } from '@/types/product';
import { ProductService } from '@/services/productService';

interface ProductFormProps {
  initialProduct?: ProductAdminResponse | null;
}

export default function ProductForm({ initialProduct }: ProductFormProps) {
  const router = useRouter();
  const isEditMode = !!initialProduct;

  const [formData, setFormData] = useState<ProductRequest>({
    productName: initialProduct?.productName || '',
    description: initialProduct?.description || '',
    status: initialProduct?.status ?? 0,
  });

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  // 統一的 SweetAlert2 成功彈窗
  const showSuccess = (title: string, text: string) => {
    Swal.fire({
      title,
      text,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false,
      timerProgressBar: true,
      customClass: { popup: 'rounded-3xl' },
    });
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'status' ? parseInt(value) : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const newErrors: { [key: string]: string } = {};
    if (!formData.productName.trim()) {
      newErrors.productName = '商品名稱為必填';
    }
    setErrors(newErrors);
    if (Object.keys(newErrors).length > 0) {
      setLoading(false);
      return;
    }
    try {
      if (isEditMode && initialProduct) {
        await ProductService.update(initialProduct.productId, formData);
        showSuccess('更新成功', `商品 ${formData.productName} 已更新`);
      } else {
        await ProductService.create(formData);
        showSuccess('新增成功', `商品 ${formData.productName} 已新增`);
      }
      router.push('/products');
      router.refresh();
    } catch (error) {
      console.error("Form submission failed:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.back();
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-10">
      <div className="max-w-2xl space-y-8">
        {/* 商品名稱 */}
        <div>
          <label className="block text-sm font-bold text-slate-900 mb-3">
            商品名稱 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="productName"
            value={formData.productName}
            onChange={handleChange}
            placeholder="輸入商品名稱"
            disabled={loading}
            className={`w-full px-4 py-3 border ${errors.productName ? 'border-red-500' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50`}
          />
          {errors.productName && <p className="mt-1 text-xs text-red-600 font-bold">{errors.productName}</p>}
        </div>

        {/* 描述 */}
        <div>
          <label className="block text-sm font-bold text-slate-900 mb-3">描述</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="輸入商品描述（選填）"
            disabled={loading}
            rows={4}
            className="w-full px-4 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50"
          />
        </div>

        {/* 狀態（僅編輯模式顯示） */}
        {isEditMode && (
          <>
            <div>
              <label className="block text-sm font-bold text-slate-900 mb-3">狀態</label>
              <select
                name="status"
                value={formData.status}
                onChange={handleChange}
                disabled={loading}
                className="w-full px-4 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-50"
              >
                <option value={0}>待上架</option>
                <option value={1}>已上架</option>
                <option value={2}>已下架</option>
              </select>
            </div>
            {/* 提示：上架需要活動 */}
            {formData.status === 1 && (
              <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
                <p className="text-sm text-blue-700 font-medium">
                  💡 提示：商品上架必須至少有一個活動（Event）。若沒有活動，後端會拒絕此操作。
                </p>
              </div>
            )}
          </>
        )}

        {/* 按鈕 */}
        <div className="flex gap-4 pt-6 border-t border-slate-100">
          <button
            type="button"
            onClick={handleCancel}
            disabled={loading}
            className="flex-1 px-6 py-3 bg-slate-100 text-slate-600 font-bold rounded-xl shadow-sm transition-all hover:bg-slate-200 disabled:opacity-50"
          >
            取消
          </button>
          <button
            type="submit"
            disabled={loading}
            className="flex-1 px-6 py-3 bg-blue-600 text-white font-bold rounded-xl shadow-sm transition-all hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? '提交中...' : isEditMode ? '更新' : '新增'}
          </button>
        </div>
      </div>
    </form>
  );
}
