package com.projects.cafe_winchester_backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private Orders order;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // DECIMAL(10, 2) in the database // Stores the price for a MenuItem, including in multiple quantities

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id", nullable = false)
    private MenuItem item;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public OrderItems() {
    }

    public OrderItems(Long orderItemId, Orders order, BigDecimal price, MenuItem item, int quantity) {
        this.orderItemId = orderItemId;
        this.order = order;
        this.price = price;
        this.item = item;
        this.quantity = quantity;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public MenuItem getItem() {
        return item;
    }

    public void setItem(MenuItem item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderItems{" +
                "orderItemId=" + orderItemId +
                ", order=" + order +
                ", price=" + price +
                ", item=" + item +
                ", quantity=" + quantity +
                '}';
    }
}
