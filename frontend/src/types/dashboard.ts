export interface DashboardStockResponse {
  productId: string;
  productName: string;
  eventId: string | null;
  redisStock: number;
  dbStock: number;
}
