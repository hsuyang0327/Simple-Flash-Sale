import http from '@/lib/http';
import { ProductAdminResponse, ProductAdminPageResponse, ProductRequest, ProductSearchParams } from '@/types/product';

const BASE_PATH = '/admin/products';

export const ProductService = {
  /**
   * 取得商品列表（分頁）或搜尋
   */
  list: (params: ProductSearchParams = {}): Promise<ProductAdminPageResponse> => {
    const query = new URLSearchParams();
    query.append('productName', params.productName ?? '');
    query.append('page', (params.page ?? 0).toString());
    query.append('size', (params.size ?? 10).toString());
    query.append('sort', 'createdAt');
    query.append('direction', 'DESC');
    return http.get(`${BASE_PATH}/search?${query.toString()}`);
  },

  /**
   * 取得單一商品（Admin）
   */
  get: (id: string): Promise<ProductAdminResponse> =>
    http.get(`${BASE_PATH}/${id}`),

  /**
   * 建立商品
   */
  create: (payload: ProductRequest): Promise<ProductAdminResponse> =>
    http.post(BASE_PATH, payload),

  /**
   * 更新商品
   */
  update: (id: string, payload: ProductRequest): Promise<ProductAdminResponse> =>
    http.put(`${BASE_PATH}/${id}`, payload),

  /**
   * 刪除商品
   */
  delete: (id: string): Promise<void> =>
    http.delete(`${BASE_PATH}/${id}`),
};
