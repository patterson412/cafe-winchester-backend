package com.projects.cafe_winchester_backend.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

public class OrderItemDto {
    @NotNull
    private Long menuItemId;

    @NotNull
    @Min(1)  // Ensures quantity is at least 1
    private int quantity;

    @NotNull
    @Digits(integer=8, fraction=2)
    @DecimalMin("0.00") // Ensures no negative price is passed
    private BigDecimal price;

    public OrderItemDto() {
    }

    public OrderItemDto(Long menuItemId, int quantity, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderItemDto{" +
                "menuItemId=" + menuItemId +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
