import OrderTable from '@/components/admin/orders/OrderTable';
import { OrderAdminService } from '@/services/orderAdminService';
import { ShoppingCart } from 'lucide-react';
import { OrderAdminPageResponse } from '@/types/order';

export default async function OrdersPage() {
  let initialOrders: OrderAdminPageResponse | null = null;

  try {
    initialOrders = await OrderAdminService.list({ page: 0, size: 10 });
  } catch (error) {
    console.error("Server fetch failed", error);
    initialOrders = {
      content: [],
      page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
    };
  }

  return (
    <div className="space-y-10">
      <div className="flex justify-between items-end border-b border-gray-100 pb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tighter text-gray-900">訂單管理</h1>
          <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Order Management Unit</p>
        </div>
        <div className="h-10 w-10 bg-gray-900 rounded-xl flex items-center justify-center text-white shadow-lg">
          <ShoppingCart size={18} fill="currentColor" />
        </div>
      </div>

      <OrderTable initialOrders={initialOrders} />
    </div>
  );
}