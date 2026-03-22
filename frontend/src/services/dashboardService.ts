import http from '@/lib/http';
import { DashboardStockResponse } from '@/types/dashboard';

export const DashboardService = {
  getStocks: (): Promise<DashboardStockResponse[]> =>
    http.get('/admin/dashboard/stocks'),
};
