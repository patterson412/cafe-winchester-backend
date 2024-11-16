package com.projects.cafe_winchester_backend.controller;

import com.projects.cafe_winchester_backend.service.S3Service;
import com.projects.cafe_winchester_backend.service.ShopService;
import com.projects.cafe_winchester_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ShopController {

    private ShopService shopService;
    private UserService userService;
    private S3Service s3Service;

    public ShopController(ShopService shopService, UserService userService, S3Service s3Service) {
        this.shopService = shopService;
        this.userService = userService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        return ResponseEntity.ok(shopService.getAllProducts());
    }

    /* Sample JSON output for getAllProducts
    {
      "Coffee": [
        {
          "id": 1,
          "name": "Espresso",
          "imageUrl": "https://bucket.s3.region.amazonaws.com/menu-items/espresso.jpg",
          "description": "Strong Italian coffee",
          "price": 3.50,
          "categoryId": 1,
          "categoryName": "Coffee"
        },
        {
          "id": 2,
          "name": "Latte",
          "imageUrl": "https://bucket.s3.region.amazonaws.com/menu-items/latte.jpg",
          "description": "Espresso with steamed milk",
          "price": 4.50,
          "categoryId": 1,
          "categoryName": "Coffee"
        }
      ],
      "Pastries": [
        {
          "id": 3,
          "name": "Croissant",
          "imageUrl": "https://bucket.s3.region.amazonaws.com/menu-items/croissant.jpg",
          "description": "Buttery French pastry",
          "price": 3.00,
          "categoryId": 2,
          "categoryName": "Pastries"
        },
        {
          "id": 4,
          "name": "Muffin",
          "imageUrl": "https://bucket.s3.region.amazonaws.com/menu-items/muffin.jpg",
          "description": "Blueberry muffin",
          "price": 2.50,
          "categoryId": 2,
          "categoryName": "Pastries"
        }
      ]
    }
     */


}
