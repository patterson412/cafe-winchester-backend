package com.projects.cafe_winchester_backend.controller.customer;

import com.projects.cafe_winchester_backend.dto.OrderDto;
import com.projects.cafe_winchester_backend.dto.OrderItemDto;
import com.projects.cafe_winchester_backend.entity.*;
import com.projects.cafe_winchester_backend.service.ShopService;
import com.projects.cafe_winchester_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class CustomerController {

    private UserService userService;

    private SimpMessagingTemplate simpMessagingTemplate;

    private ShopService shopService;

    public CustomerController(UserService userService, SimpMessagingTemplate simpMessagingTemplate, ShopService shopService) {
        this.userService = userService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.shopService = shopService;
    }

    /*
    Sample input for the below orderDto should look like this

    {
        "orderItemDtos": [
            {
                "menuItemId": 1,
                "quantity": 2,
                "price": 12.99
            },
            {
                "menuItemId": 3,
                "quantity": 1,
                "price": 8.50
            }
        ]
    }
     */


    @PostMapping("/orders/neworder")
    public ResponseEntity<Map<String, Object>> addOrder(@Valid @RequestBody OrderDto orderDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName(); // here essentially the getUsername() method is called

        User user = userService.getUserByEmail(currentPrincipalName);

        Orders newOrder = new Orders();
        List<OrderItems> orderItemsList = new ArrayList<>();
        for (OrderItemDto orderItemDto : orderDto.getOrderItemDtos()) {
            OrderItems newOrderItems = new OrderItems();
            newOrderItems.setItem(shopService.getMenuItemById(orderItemDto.getMenuItemId()));
            newOrderItems.setPrice(orderItemDto.getPrice().multiply(BigDecimal.valueOf(orderItemDto.getQuantity())));
            newOrderItems.setQuantity(orderItemDto.getQuantity());
            newOrderItems.setOrder(newOrder);
            orderItemsList.add(newOrderItems);
        }
        newOrder.setOrderItems(orderItemsList);
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setOrderDate(LocalDateTime.now());

        User updatedUser = userService.addOrder(user.getUserId(), newOrder);

        Orders savedOrder = shopService.getLatestOrderOfUser(updatedUser.getUserId());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order created successfully");
        responseBody.put("orderId", savedOrder.getOrderId());
        responseBody.put("status", savedOrder.getStatus());
        responseBody.put("orderDate", savedOrder.getOrderDate());

        simpMessagingTemplate.convertAndSend("/topic/orders", savedOrder);  // Sends to Admin Subscribers

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);

    }


    @GetMapping("/user/favourites")
    public ResponseEntity<Map<String, Object>> getFavourites() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User user = userService.getUserByEmail(currentPrincipalName);

        List<Favourites> userFavourites = user.getFavourites();

        List<Map<String, Object>> simplifiedMenuItems = new ArrayList<>();

        for (Favourites fav : userFavourites) {
            Map<String, Object> simplifiedItem = new HashMap<>();

            // Add MenuItem fields
            simplifiedItem.put("id", fav.getMenuItem().getId());
            simplifiedItem.put("name", fav.getMenuItem().getName());
            simplifiedItem.put("description", fav.getMenuItem().getDescription());
            simplifiedItem.put("price", fav.getMenuItem().getPrice());
            simplifiedItem.put("imageUrl", fav.getMenuItem().getImageUrl());

            // Add simplified category with only id and name
            Map<String, Object> simplifiedCategory = new HashMap<>();
            simplifiedCategory.put("id", fav.getMenuItem().getCategory().getId());
            simplifiedCategory.put("name", fav.getMenuItem().getCategory().getName());

            simplifiedItem.put("category", simplifiedCategory);

            simplifiedMenuItems.add(simplifiedItem);
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("favourites", simplifiedMenuItems);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);

    }

    @PostMapping("/user/favourites/{id}")
    public ResponseEntity<?> addFavourite(@PathVariable("id") Long menuItemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User user = userService.getUserByEmail(currentPrincipalName);

        Favourites newFavourite = new Favourites();
        newFavourite.setMenuItem(shopService.getMenuItemById(menuItemId));

        userService.addToFavourites(user.getUserId(), newFavourite);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("message", "Added Item to Favourites successfully");

        return ResponseEntity
                .ok()
                .body(responseBody);

    }

}
