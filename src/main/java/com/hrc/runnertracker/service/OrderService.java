package com.hrc.runnertracker.service;

import com.hrc.runnertracker.dto.request.CreateOrderRequest;
import com.hrc.runnertracker.dto.response.OrderResponse;
import com.hrc.runnertracker.entity.Order;
import com.hrc.runnertracker.entity.OrderItem;
import com.hrc.runnertracker.entity.User;
import com.hrc.runnertracker.exception.ResourceNotFoundException;
import com.hrc.runnertracker.repository.OrderRepository;
import com.hrc.runnertracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * Tạo đơn hàng mới.
     */
    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        User user = findUserByUsername(username);

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .note(request.getNote())
                .status(Order.OrderStatus.PENDING)
                .build();

        // Tạo order items và tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .build();
            order.getItems().add(item);

            BigDecimal itemTotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    /**
     * Lấy danh sách đơn hàng của user đang đăng nhập.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String username) {
        User user = findUserByUsername(username);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết đơn hàng — chỉ cho phép user xem đơn của mình.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String username, Long orderId) {
        User user = findUserByUsername(username);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toResponse(order);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
