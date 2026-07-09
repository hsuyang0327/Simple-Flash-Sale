package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.DashboardStockResponse;

import java.util.List;

public interface DashboardService {

    List<DashboardStockResponse> getStocks();
}
