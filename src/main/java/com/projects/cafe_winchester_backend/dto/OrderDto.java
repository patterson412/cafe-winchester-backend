package com.projects.cafe_winchester_backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderDto {
    @NotNull
    private List<OrderItemDto> orderItemDtos;

    public OrderDto() {
    }

    public OrderDto(List<OrderItemDto> OrderItemDtos) {
        this.orderItemDtos = OrderItemDtos;
    }

    public List<OrderItemDto> getOrderItemDtos() {
        return orderItemDtos;
    }

    public void setOrderItemDtos(List<OrderItemDto> OrderItemDtos) {
        this.orderItemDtos = OrderItemDtos;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "orderItemDtos=" + orderItemDtos +
                '}';
    }
}
