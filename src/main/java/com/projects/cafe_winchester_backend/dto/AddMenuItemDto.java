package com.projects.cafe_winchester_backend.dto;

import com.projects.cafe_winchester_backend.entity.MenuCategory;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class AddMenuItemDto {
    @NotNull
    private String name;

    private MultipartFile newImage;

    @NotNull
    private String description;

    @NotNull
    private BigDecimal price;
    @NotNull
    private Long menuCategoryId;

    public AddMenuItemDto() {
    }

    public AddMenuItemDto(String name, String imageUrl, MultipartFile newImage, String description, BigDecimal price, Long menuCategoryId) {
        this.name = name;
        this.newImage = newImage;
        this.description = description;
        this.price = price;
        this.menuCategoryId = menuCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MultipartFile getNewImage() {
        return newImage;
    }

    public void setNewImage(MultipartFile newImage) {
        this.newImage = newImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getMenuCategoryId() {
        return menuCategoryId;
    }

    public void setMenuCategoryId(Long menuCategoryId) {
        this.menuCategoryId = menuCategoryId;
    }

    @Override
    public String toString() {
        return "MenuItemDto{" +
                "name='" + name + '\'' +
                ", newImage=" + newImage +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", menuCategoryId=" + menuCategoryId +
                '}';
    }
}
