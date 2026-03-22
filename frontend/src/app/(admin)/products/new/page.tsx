import ProductForm from '@/components/admin/products/ProductForm';
import { Package } from 'lucide-react';

export default function NewProductPage() {
  return (
    <div className="space-y-10">
      {/* 標題 */}
      <div className="flex justify-between items-end border-b border-gray-100 pb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tighter text-gray-900">新增商品</h1>
          <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Create New Product</p>
        </div>
        <div className="h-10 w-10 bg-gray-900 rounded-xl flex items-center justify-center text-white shadow-lg">
          <Package size={18} fill="currentColor" />
        </div>
      </div>

      {/* 商品表單 */}
      <div>
        <ProductForm />
      </div>
    </div>
  );
}
