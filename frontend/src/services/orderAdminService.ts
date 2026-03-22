import http from '@/lib/http';
import { OrderAdminResponse, OrderAdminPageResponse, OrderSearchParams } from '@/types/order';

// 對應 @RequestMapping("/api/admin/orders")
const BASE_PATH = '/admin/orders';

export const OrderAdminService = {
    /**
     * 搜尋訂單 (分頁) (@GetMapping)
     */
    list: (params: OrderSearchParams = {}): Promise<OrderAdminPageResponse> => {
        const query = new URLSearchParams();
        if (params.productName) query.append('productName', params.productName);
        if (params.memberName) query.append('memberName', params.memberName);
        if (params.page !== undefined) query.append('page', params.page.toString());
        if (params.size !== undefined) query.append('size', params.size.toString());
        query.append('sort', 'createdAt');
        query.append('direction', 'DESC');
        return http.get(`${BASE_PATH}?${query.toString()}`);
    },

    /**
     * 獲取訂單詳細 (@GetMapping("/{id}"))
     */
    get: (id: string): Promise<OrderAdminResponse> =>
        http.get(`${BASE_PATH}/${id}`),
};