import ProductForm from '@/components/admin/products/ProductForm';
import EventTable from '@/components/admin/events/EventTable';
import { ProductService } from '@/services/productService';
import { EventService } from '@/services/eventService';
import { ProductAdminResponse } from '@/types/product';
import { EventPageResponse } from '@/types/event';
import { Package } from 'lucide-react';

export default async function ProductDetailPage({ params }: { params: { id: string } }) {
    const { id: productId } = await params;

    let product: ProductAdminResponse | null = null;
    let events: EventPageResponse | null = null;

    try {
        product = await ProductService.get(productId);
    } catch (error) {
        console.error("Failed to fetch product:", error);
    }

    if (product) {
        try {
            events = await EventService.listByProductId(productId, 0, 10);
        } catch (error) {
            console.error("Failed to fetch events:", error);
            events = {
                content: [],
                page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
            };
        }
    }

    if (!product) {
        return (
            <div className="space-y-6">
                <div className="flex justify-between items-end border-b border-gray-100 pb-8">
                    <div>
                        <h1 className="text-2xl font-bold tracking-tighter text-gray-900">商品不存在</h1>
                        <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Product Not Found</p>
                    </div>
                </div>
                <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-10">
                    <p className="text-slate-600">無法載入商品資料，請返回列表重試。</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-10">
            {/* 標題 */}
            <div className="flex justify-between items-end border-b border-gray-100 pb-8">
                <div>
                    <h1 className="text-2xl font-bold tracking-tighter text-gray-900">商品管理 - {product.productName}</h1>
                    <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Product Details And Events Management</p>
                </div>
                <div className="h-10 w-10 bg-gray-900 rounded-xl flex items-center justify-center text-white shadow-lg">
                    <Package size={18} fill="currentColor" />
                </div>
            </div>

            {/* 商品編輯區 */}
            <div className="space-y-4">
                <h2 className="text-lg font-bold text-slate-900">編輯商品資訊</h2>
                <ProductForm initialProduct={product} />
            </div>

            {/* 活動管理區 */}
            <div className="space-y-4">
                <h2 className="text-lg font-bold text-slate-900">活動管理</h2>
                <EventTable productId={productId} initialEvents={events} />
            </div>
        </div>
    );
}
