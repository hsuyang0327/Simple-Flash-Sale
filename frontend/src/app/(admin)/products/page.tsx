export const dynamic = 'force-dynamic';

import ProductTable from '@/components/admin/products/ProductTable';
import { ProductService } from '@/services/productService';
import { Package } from 'lucide-react';
import { ProductAdminPageResponse } from '@/types/product';

export default async function ProductsPage() {
  let initialProducts: ProductAdminPageResponse | null = null;

  try {
    initialProducts = await ProductService.list({ page: 0, size: 10 });
  } catch (error) {
    console.error("Server fetch failed", error);
    initialProducts = {
      content: [],
      page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
    };
  }

  return (
    <div className="space-y-10">
      <div className="flex justify-between items-end border-b border-gray-100 pb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tighter text-gray-900">商品管理</h1>
          <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Product Management Unit</p>
        </div>
        <div className="h-10 w-10 bg-gray-900 rounded-xl flex items-center justify-center text-white shadow-lg">
          <Package size={18} fill="currentColor" />
        </div>
      </div>

      <ProductTable initialProducts={initialProducts} />
    </div>
  );
}
