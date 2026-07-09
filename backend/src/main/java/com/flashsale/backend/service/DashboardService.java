package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.DashboardStockResponse;

import java.util.List;

/**
 * @description Dashboard data service interface — stock comparison between Redis and MySQL
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public interface DashboardService {

    List<DashboardStockResponse> getStocks();
}
