package com.restaurant.controller.customer;

import com.restaurant.dtos.customer.OrderResponse;
import com.restaurant.dtos.customer.OrderItemResponse;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderSuccessController {

    private final OrderRepository orderRepository;

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam("order") String orderNumber,
                                   @RequestParam("table") String tableToken,
                                   Model model) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setOrderNumber(order.getOrderNumber());
        orderResponse.setTableNumber(order.getTable().getTableNumber());
        orderResponse.setTableName(order.getTable().getTableName());
        orderResponse.setCustomerName(order.getCustomerName());
        orderResponse.setCustomerMobile(order.getCustomerMobile());
        orderResponse.setCustomerEmail(order.getCustomerEmail());
        orderResponse.setSubtotal(order.getSubtotal());
        orderResponse.setTax(order.getTax());
        orderResponse.setDiscount(order.getDiscount());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setOrderStatus(order.getOrderStatus().name());
        orderResponse.setPaymentStatus(order.getPaymentStatus().name());
        orderResponse.setSpecialInstructions(order.getSpecialInstructions());
        orderResponse.setCreatedAt(order.getCreatedAt());
        orderResponse.setConfirmedAt(order.getConfirmedAt());
        orderResponse.setEstimatedReadyTime(order.getEstimatedReadyTime());
        orderResponse.setItems(order.getOrderItems().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList()));

        model.addAttribute("order", orderResponse);
        model.addAttribute("tableToken", tableToken);
        model.addAttribute("tableNumber", order.getTable().getTableNumber());
        model.addAttribute("restaurantName", "Smart Restaurant");
        model.addAttribute("restaurantLogo", "/images/logo.png");
        model.addAttribute("restaurantAddress", "123 Restaurant Street, City");
        model.addAttribute("restaurantPhone", "+91-9876543210");

        return "customer/success";
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setItemName(item.getMenuItem().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        response.setSpecialInstructions(item.getSpecialInstructions());
        return response;
    }
}