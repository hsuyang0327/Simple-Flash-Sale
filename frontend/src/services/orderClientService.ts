import http from '@/lib/http';
import { OrderClientDetailResponse, OrderClientPageResponse, OrderStatusResponse } from '@/types/order';

const BASE_PATH = '/client/orders';

export const OrderClientService = {
  /**
   * 取得我的訂單列表（分頁，只回 PAID/FAILED/CANCELLED）
   */
  list: (page = 0, size = 10): Promise<OrderClientPageResponse> => {
    const query = new URLSearchParams();
    query.append('page', page.toString());
    query.append('size', size.toString());
    query.append('sort', 'createdAt');
    query.append('direction', 'DESC');
    return http.get(`${BASE_PATH}?${query.toString()}`);
  },

  /**
   * 取得單筆訂單詳情
   */
  get: (id: string): Promise<OrderClientDetailResponse> =>
    http.get(`${BASE_PATH}/${id}`),

  /**
   * 取消訂單（只允許 PAID 狀態）
   */
  cancel: (id: string): Promise<OrderClientDetailResponse> =>
    http.patch(`${BASE_PATH}/${id}/cancel`),

  /**
   * 建立搶購訂單
   */
  createOrder: (eventId: string, quantity: number): Promise<OrderClientDetailResponse> =>
    http.post(BASE_PATH, { eventId, quantity }),

  /**
   * 輪詢訂單狀態（搶購後等待 MQ 持久化）
   */
  pollStatus: (eventId: string): Promise<OrderStatusResponse> =>
    http.get(`${BASE_PATH}/status?eventId=${eventId}`),

  /**
   * 模擬付款
   */
  pay: (orderId: string): Promise<OrderClientDetailResponse> =>
    http.post('/client/payment/pay', { orderId }),
};
