import http from '@/lib/http';
import { EventPageResponse, EventResponse, EventRequest } from '@/types/event';

const BASE_PATH = '/admin/events';

export const EventService = {
  /**
   * 根據商品 ID 取得活動列表（分頁）
   */
  listByProductId: (productId: string, page = 0, size = 10): Promise<EventPageResponse> => {
    const query = new URLSearchParams();
    query.append('productId', productId);
    query.append('page', page.toString());
    query.append('size', size.toString());
    query.append('sort', 'startTime');
    query.append('direction', 'DESC');
    return http.get(`${BASE_PATH}?${query.toString()}`);
  },

  /**
   * 取得單一活動
   */
  get: (id: string): Promise<EventResponse> =>
    http.get(`${BASE_PATH}/${id}`),

  /**
   * 建立活動
   */
  create: (payload: EventRequest): Promise<EventResponse> =>
    http.post(BASE_PATH, payload),

  /**
   * 更新活動
   */
  update: (id: string, payload: EventRequest): Promise<EventResponse> =>
    http.put(`${BASE_PATH}/${id}`, payload),

  /**
   * 刪除活動
   */
  delete: (id: string): Promise<void> =>
    http.delete(`${BASE_PATH}/${id}`),
};
