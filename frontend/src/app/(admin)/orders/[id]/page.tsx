import { OrderAdminService } from '@/services/orderAdminService';
import { OrderAdminResponse } from '@/types/order';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';

interface OrderDetailPageProps {
  params: {
    id: string;
  };
}

export default async function OrderDetailPage({ params }: OrderDetailPageProps) {
  let order: OrderAdminResponse | null = null;

  try {
    order = await OrderAdminService.get(params.id);
  } catch (error) {
    console.error("Failed to fetch order:", error);
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING': return '待處理';
      case 'PAID': return '已付款';
      case 'CANCELLED': return '已取消';
      case 'COMPLETED': return '已完成';
      default: return status;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-500';
      case 'PAID': return 'bg-blue-500';
      case 'CANCELLED': return 'bg-red-500';
      case 'COMPLETED': return 'bg-green-500';
      default: return 'bg-gray-500';
    }
  };

  if (!order) {
    return (
      <div className="p-8">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900">訂單未找到</h1>
          <Link href="/admin/orders" className="text-blue-600 hover:text-blue-800 mt-4 inline-block">
            返回訂單列表
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 p-8">
      <div className="flex items-center gap-4">
        <Link
          href="/admin/orders"
          className="p-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
        >
          <ArrowLeft size={20} />
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tighter text-gray-900">訂單詳細</h1>
          <p className="text-sm text-gray-600">訂單 ID: {order.orderId}</p>
        </div>
      </div>

      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">訂單資訊</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">訂單 ID:</span>
                  <span className="text-sm text-gray-900">{order.orderId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">商品名稱:</span>
                  <span className="text-sm text-gray-900">{order.productName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">商品 ID:</span>
                  <span className="text-sm text-gray-900">{order.productId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">數量:</span>
                  <span className="text-sm text-gray-900">{order.quantity}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">總價:</span>
                  <span className="text-sm text-gray-900 font-semibold">NT$ {order.totalPrice}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">會員資訊</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">會員名稱:</span>
                  <span className="text-sm text-gray-900">{order.memberName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">會員 ID:</span>
                  <span className="text-sm text-gray-900">{order.memberId}</span>
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">狀態資訊</h3>
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium text-gray-600">狀態:</span>
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold text-white ${getStatusColor(order.status)}`}>
                    {getStatusText(order.status)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">創建時間:</span>
                  <span className="text-sm text-gray-900">{new Date(order.createdAt).toLocaleString('zh-TW')}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm font-medium text-gray-600">更新時間:</span>
                  <span className="text-sm text-gray-900">{new Date(order.updatedAt).toLocaleString('zh-TW')}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}