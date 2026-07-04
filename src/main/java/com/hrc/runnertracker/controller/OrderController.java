package com.hrc.runnertracker.controller;

import com.hrc.runnertracker.dto.request.CreateOrderRequest;
import com.hrc.runnertracker.dto.response.ApiResponse;
import com.hrc.runnertracker.dto.response.OrderResponse;
import com.hrc.runnertracker.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders — Tạo đơn hàng mới.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = orderService.createOrder(
                userDetails.getUsername(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đơn hàng thành công", response));
    }

    /**
     * GET /api/v1/orders — Lấy danh sách đơn hàng của user đang đăng nhập.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<OrderResponse> orders = orderService.getOrders(
                userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách đơn hàng thành công", orders));
    }

    /**
     * GET /api/v1/orders/{id} — Lấy chi tiết đơn hàng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        OrderResponse response = orderService.getOrderById(
                userDetails.getUsername(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy chi tiết đơn hàng thành công", response));
    }
}
