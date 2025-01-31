package com.projects.cafe_winchester_backend.controller.customer;

import com.projects.cafe_winchester_backend.dto.OrderDto;
import com.projects.cafe_winchester_backend.dto.OrderItemDto;
import com.projects.cafe_winchester_backend.entity.*;
import com.projects.cafe_winchester_backend.service.S3Service;
import com.projects.cafe_winchester_backend.service.ShopService;
import com.projects.cafe_winchester_backend.service.UserService;
import com.projects.cafe_winchester_backend.util.tokenUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    private tokenUtil jwtTokenUtil;
    private S3Service s3Service;

    public CustomerController(UserService userService, SimpMessagingTemplate simpMessagingTemplate, ShopService shopService, tokenUtil jwtTokenUtil, S3Service s3Service) {
        this.userService = userService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.shopService = shopService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.s3Service = s3Service;
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
    @Transactional
    public ResponseEntity<Map<String, Object>> addOrder(@Valid @RequestBody OrderDto orderDto) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userService.getUserById(currentUser.getUsername());

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
                .body(responseBody);

    }


    @GetMapping("/user/favourites")
    public ResponseEntity<Map<String, Object>> getFavourites() {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userService.getUserById(currentUser.getUsername());

        List<Favourites> userFavourites = user.getFavourites();

        List<Map<String, Object>> simplifiedMenuItems = new ArrayList<>();

        for (Favourites fav : userFavourites) {
            Map<String, Object> simplifiedItem = new HashMap<>();

            // Add MenuItem fields
            simplifiedItem.put("id", fav.getMenuItem().getId());
            simplifiedItem.put("name", fav.getMenuItem().getName());
            simplifiedItem.put("description", fav.getMenuItem().getDescription());
            simplifiedItem.put("price", fav.getMenuItem().getPrice());
            simplifiedItem.put("imageUrl", s3Service.generateSignedUrl(fav.getMenuItem().getImageUrl()));

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
                .body(responseBody);

    }

    @PostMapping("/user/favourites/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> addFavourite(@PathVariable("id") Long menuItemId) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userService.getUserById(currentUser.getUsername());

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
