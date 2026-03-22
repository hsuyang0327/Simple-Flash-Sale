import http from '@/lib/http';
import { EventProductDTO } from '@/types/event';

const BASE_PATH = '/client/open/redis';

// Raw Redis Hash response — values are all strings
type RedisProductMap = Record<string, string>;

function mapToEventProductDTO(raw: RedisProductMap): EventProductDTO {
  return {
    productId: raw['productId'] ?? '',
    eventId: raw['eventId'] ?? '',
    productName: raw['productName'] ?? '',
    description: raw['description'] ?? '',
    price: raw['price'] ?? '0',
    stock: raw['stock'] ?? '0',
    startTime: raw['startTime'] ?? '',
    endTime: raw['endTime'] ?? '',
  };
}

export const FlashSaleService = {
  /**
   * Get paginated list of preheated flash sale products from Redis
   */
  list: (page = 0, size = 20): Promise<{ content: EventProductDTO[]; totalElements: number; totalPages: number; number: number; first: boolean; last: boolean }> =>
    http.get<{ content: RedisProductMap[]; totalElements: number; totalPages: number; number: number; first: boolean; last: boolean }>(
      `${BASE_PATH}/preheated-products?page=${page}&size=${size}`
    ).then(res => ({
      ...res,
      content: res.content.map(mapToEventProductDTO),
    })),

  /**
   * Get a single preheated product by productId
   */
  getProduct: (productId: string): Promise<EventProductDTO> =>
    http.get<RedisProductMap>(`${BASE_PATH}/product/${productId}`)
      .then(mapToEventProductDTO),
};
