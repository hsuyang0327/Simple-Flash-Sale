'use client';

import { Edit3, Trash2, Plus } from 'lucide-react';
import { useState } from 'react';
import Link from 'next/link';
import Swal from 'sweetalert2';
import { ProductAdminResponse, ProductAdminPageResponse } from '@/types/product';
import { ProductService } from '@/services/productService';

interface ProductTableProps {
  initialProducts: ProductAdminPageResponse | null;
}

export default function ProductTable({ initialProducts }: ProductTableProps) {
  const [products, setProducts] = useState<ProductAdminResponse[]>(initialProducts?.content || []);
  const [currentPage, setCurrentPage] = useState(initialProducts?.page?.number || 0);
  const [totalPages, setTotalPages] = useState(initialProducts?.page?.totalPages || 0);
  const [totalElements, setTotalElements] = useState(initialProducts?.page?.totalElements || 0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(false);
  const [searchName, setSearchName] = useState('');

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

  const loadPage = async (page: number, searchTerm = '') => {
    setLoading(true);
    try {
      const data = await ProductService.list({
        productName: searchTerm || undefined,
        page,
        size: pageSize
      });
      setProducts(data.content);
      setCurrentPage(data.page.number);
      setTotalPages(data.page.totalPages);
      setTotalElements(data.page.totalElements);
    } catch (error) {
      console.error("Failed to load product list:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    setCurrentPage(0);
    await loadPage(0, searchName);
  };

  const handleClearSearch = async () => {
    setSearchName('');
    setCurrentPage(0);
    await loadPage(0, '');
  };

  const refresh = async () => {
    await loadPage(currentPage, searchName);
  };

  const handleDelete = async (product: ProductAdminResponse) => {
    const result = await Swal.fire({
      title: '確認刪除',
      text: `確定要刪除商品 ${product.productName} 嗎？`,
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
        await ProductService.delete(product.productId);
        showSuccess('刪除成功', `商品 ${product.productName} 已刪除`);
        await refresh();
      } catch (error) {
        console.error("Delete product failed:", error);
      }
    }
  };

  const getStatusBadge = (status: number) => {
    switch (status) {
      case 0:
        return <span className="inline-flex px-3 py-1 bg-amber-100 text-amber-700 text-xs font-bold rounded-full border border-amber-200">待上架</span>;
      case 1:
        return <span className="inline-flex px-3 py-1 bg-emerald-100 text-emerald-700 text-xs font-bold rounded-full border border-emerald-200">已上架</span>;
      case 2:
        return <span className="inline-flex px-3 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-full border border-red-200">已下架</span>;
      default:
        return <span className="inline-flex px-3 py-1 bg-gray-100 text-gray-700 text-xs font-bold rounded-full border border-gray-200">未知</span>;
    }
  };

  return (
    <div className="w-full space-y-8 p-4">
      
      {/* 搜尋欄 */}
      <div className="flex gap-3 items-end bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-6">
        <div className="flex-1">
          <label className="block text-xs font-black text-slate-500 uppercase tracking-widest mb-2">商品名稱</label>
          <input
            type="text"
            placeholder="輸入商品名稱搜尋"
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="w-full px-4 py-2.5 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          onClick={handleSearch}
          disabled={loading}
          className="px-6 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-xl shadow-sm transition-all active:scale-90 hover:bg-blue-700 disabled:opacity-50"
        >
          搜尋
        </button>
        {searchName && (
          <button
            onClick={handleClearSearch}
            disabled={loading}
            className="px-6 py-2.5 bg-slate-100 text-slate-600 text-sm font-bold rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-200 disabled:opacity-50"
          >
            清除
          </button>
        )}
      </div>

      {/* 新增按鈕 */}
      <div className="flex justify-end">
        <Link
          href="/products/new"
          className="flex items-center gap-2 px-6 py-3 bg-emerald-600 text-white text-sm font-bold rounded-xl shadow-sm transition-all active:scale-90 hover:bg-emerald-700"
        >
          <Plus size={16} />
          新增商品
        </Link>
      </div>

      {/* 商品表格 */}
      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-separate border-spacing-0">
            <thead>
              <tr className="bg-slate-50/80">
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">商品名稱</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">描述</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">狀態</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">創建時間</th>
                <th className="px-6 py-6 text-xs font-black text-slate-500 border-b border-slate-100 text-center uppercase tracking-widest">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {products.length === 0 ? (
                <tr className="bg-white hover:bg-slate-50">
                  <td colSpan={5} className="px-8 py-12 text-center text-sm text-slate-400">
                    沒有商品資料
                  </td>
                </tr>
              ) : (
                products.map((product, index) => (
                  <tr key={product.productId} className={`transition-all ${index % 2 === 0 ? 'bg-white' : 'bg-slate-50/30'} hover:bg-blue-50/40`}>
                    <td className="px-8 py-7">
                      <span className="text-sm font-bold text-slate-900">{product.productName}</span>
                    </td>
                    <td className="px-8 py-7">
                      <span className="text-sm text-slate-600 truncate max-w-xs">{product.description || '-'}</span>
                    </td>
                    <td className="px-8 py-7">
                      {getStatusBadge(product.status)}
                    </td>
                    <td className="px-8 py-7">
                      <span className="text-sm text-slate-600">{new Date(product.createdAt).toLocaleString('zh-TW')}</span>
                    </td>
                    <td className="px-6 py-7 text-center">
                      <div className="flex justify-center gap-2.5">
                        <Link
                          href={`/products/${product.productId}`}
                          className="p-2.5 bg-blue-50 text-blue-600 border border-blue-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-blue-100"
                        >
                          <Edit3 size={15} />
                        </Link>
                        <button
                          onClick={() => handleDelete(product)}
                          className="p-2.5 bg-red-50 text-red-600 border border-red-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-red-100 disabled:opacity-50"
                          disabled={loading}
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
      <div className="flex items-center justify-between bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-6">
        <div className="text-sm text-slate-600">
          顯示第 {currentPage * pageSize + 1} 到 {Math.min((currentPage + 1) * pageSize, totalElements)} 項，共 {totalElements} 項
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => loadPage(currentPage - 1, searchName)}
            disabled={currentPage === 0 || loading}
            className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一頁
          </button>
          <span className="px-4 py-2 text-sm font-bold text-slate-900">
            第 {currentPage + 1} 頁，共 {totalPages} 頁
          </span>
          <button
            onClick={() => loadPage(currentPage + 1, searchName)}
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
