package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.response.DashboardStockResponse;
import com.flashsale.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description DashboardController — admin dashboard data endpoints
 * @author Yang-Hsu
 * @date 2026/4/2
 */
@Tag(name = "Admin Dashboard", description = "Admin dashboard monitoring endpoints.")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Redis Stock Overview", description = "Returns all preheated products with Redis stock vs DB stock for admin monitoring.")
    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<List<DashboardStockResponse>>> getStocks() {
        List<DashboardStockResponse> stocks = dashboardService.getStocks();
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, stocks));
    }
}
