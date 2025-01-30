package com.projects.cafe_winchester_backend.controller.admin;

import com.projects.cafe_winchester_backend.dto.AddMenuItemDto;
import com.projects.cafe_winchester_backend.dto.MenuItemDto;
import com.projects.cafe_winchester_backend.entity.MenuCategory;
import com.projects.cafe_winchester_backend.entity.MenuItem;
import com.projects.cafe_winchester_backend.entity.Orders;
import com.projects.cafe_winchester_backend.service.S3Service;
import com.projects.cafe_winchester_backend.service.ShopService;
import com.projects.cafe_winchester_backend.service.UserService;
import com.projects.cafe_winchester_backend.util.tokenUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/manage")
public class AdminController {

    private final S3Service s3Service;
    private final UserService userService;
    private final ShopService shopService;
    private final tokenUtil jwtTokenUtil;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public AdminController(S3Service s3Service, UserService userService, ShopService shopService, tokenUtil jwtTokenUtil, SimpMessagingTemplate simpMessagingTemplate) {
        this.s3Service = s3Service;
        this.userService = userService;
        this.shopService = shopService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PutMapping("/data/menuitem/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable("id") Long itemId, @Valid @ModelAttribute MenuItemDto menuItemDto) {
        MenuItem updatedMenuItem = shopService.updateMenuItem(itemId, menuItemDto);

        return ResponseEntity
                .ok()
                .body(updatedMenuItem);
    }

    @GetMapping("/data/currentorders")
    public ResponseEntity<List<Map<String, Object>>> getCurrentOrders() {
        List<Orders> orders = shopService.getAllCurrentOrders();
        List<Map<String, Object>> simplifiedCurrentOrders = orders.stream()
                .map(order -> {
                    List<Map<String, Object>> orderItems = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> simplifiedOrderItem = new HashMap<>();
                                simplifiedOrderItem.put("orderItemId", orderItem.getOrderItemId());
                                simplifiedOrderItem.put("price", orderItem.getPrice());
                                simplifiedOrderItem.put("quantity", orderItem.getQuantity());
                                simplifiedOrderItem.put("menuItem", Map.of(
                                        "id", orderItem.getItem().getId(),
                                        "name", orderItem.getItem().getName(),
                                        "imageUrl", s3Service.generateSignedUrl(orderItem.getItem().getImageUrl()),
                                        "description", orderItem.getItem().getDescription(),
                                        "price", orderItem.getItem().getPrice(),
                                        "category", Map.of(
                                                "id", orderItem.getItem().getCategory().getId(),
                                                "name", orderItem.getItem().getCategory().getName()
                                        )
                                ));

                                return simplifiedOrderItem;
                            })
                            .toList();

                    Map<String, Object> simplifiedOrder = new HashMap<>();
                    simplifiedOrder.put("orderId", order.getOrderId());
                    simplifiedOrder.put("user", Map.of(
                            "userId", order.getUser().getUserId(),
                            "email", order.getUser().getEmail(),
                            "phoneNumber", order.getUser().getPhoneNumber(),
                            "address", Map.of(
                                    "lat", order.getUser().getAddress().getLatitude(),
                                    "lon", order.getUser().getAddress().getLongitude()
                            )

                    ));
                    simplifiedOrder.put("status", order.getStatus());
                    simplifiedOrder.put("orderDate", order.getOrderDate());
                    simplifiedOrder.put("orderItems", orderItems);

                    return simplifiedOrder;
                })
                .toList();

        return ResponseEntity
                .ok()
                .body(simplifiedCurrentOrders);
    }

    @GetMapping("/data/allorders")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Orders> orders = shopService.getAllOrders();
        List<Map<String, Object>> simplifiedAllOrders = orders.stream()
                .map(order -> {
                    List<Map<String, Object>> orderItems = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> simplifiedOrderItem = new HashMap<>();
                                simplifiedOrderItem.put("orderItemId", orderItem.getOrderItemId());
                                simplifiedOrderItem.put("price", orderItem.getPrice());
                                simplifiedOrderItem.put("quantity", orderItem.getQuantity());
                                simplifiedOrderItem.put("menuItem", Map.of(
                                        "id", orderItem.getItem().getId(),
                                        "name", orderItem.getItem().getName(),
                                        "imageUrl", s3Service.generateSignedUrl(orderItem.getItem().getImageUrl()),
                                        "description", orderItem.getItem().getDescription(),
                                        "price", orderItem.getItem().getPrice(),
                                        "category", Map.of(
                                                "id", orderItem.getItem().getCategory().getId(),
                                                "name", orderItem.getItem().getCategory().getName()
                                        )
                                ));

                                return simplifiedOrderItem;
                            })
                            .toList();

                    Map<String, Object> simplifiedOrder = new HashMap<>();
                    simplifiedOrder.put("orderId", order.getOrderId());
                    simplifiedOrder.put("user", Map.of(
                            "userId", order.getUser().getUserId(),
                            "email", order.getUser().getEmail(),
                            "phoneNumber", order.getUser().getPhoneNumber(),
                            "address", Map.of(
                                    "lat", order.getUser().getAddress().getLatitude(),
                                    "lon", order.getUser().getAddress().getLongitude()
                            )

                    ));
                    simplifiedOrder.put("status", order.getStatus());
                    simplifiedOrder.put("orderDate", order.getOrderDate());
                    simplifiedOrder.put("orderItems", orderItems);

                    return simplifiedOrder;
                })
                .toList();

        return ResponseEntity
                .ok()
                .body(simplifiedAllOrders);
    }


    // Method to get all categories returning only the category name and ID
    @GetMapping("/data/menucategories")
    public ResponseEntity<?> getAllCategoriesOnly() {
        List<Map<String, Object>> categories = shopService.getAllCategoriesNamesAndIdOnly();

        return ResponseEntity
                .ok()
                .body(categories);
    }
    /* Sample JSON output for above getAllCategoriesOnly
    [
      {
        "id": 1,
        "name": "Appetizers"
      },
      {
        "id": 2,
        "name": "Main Course"
      },
      {
        "id": 3,
        "name": "Desserts"
      }
    ]
     */

    // Method to Insert new MenuItem
    @PostMapping("/data/menuitems")
    public ResponseEntity<MenuItem> addMenuItem(@Valid @ModelAttribute AddMenuItemDto addMenuItemDto) {    // @ModelAttribute is used if request data is coming in formData
        MenuItem newMenuItem = shopService.addMenuItem(addMenuItemDto);

        return ResponseEntity
                .ok()
                .body(newMenuItem);
    }

    // Method to create empty new Category
    @PostMapping("/data/menucategories")
    public ResponseEntity<Map<String, Object>> addMenuCategory(@RequestBody Map<String, String> payload) {
        MenuCategory newMenuCategory = shopService.addMenuCategory(payload.get("name"));
        Map<String, Object> simplifiedCategory = new HashMap<>();
        simplifiedCategory.put("id", newMenuCategory.getId());
        simplifiedCategory.put("name", newMenuCategory.getName());

        return ResponseEntity
                .ok()
                .body(simplifiedCategory);
    }

    // Method to accept order
    @PostMapping("/data/orders/accept/{id}")
    public ResponseEntity<Map<String, Object>> acceptOrder(@PathVariable("id") Long orderId) {
        Orders order = shopService.acceptOrder(orderId);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order Accepted & Processing");
        responseBody.put("order", order);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(order.getUser().getUserId()), "/queue/order-notifications", responseBody);

        return ResponseEntity
                .ok()
                .body(responseBody);
    }

    // Method to decline order
    @PostMapping("/data/orders/decline/{id}")
    public ResponseEntity<Map<String, Object>> declineOrder(@PathVariable("id") Long orderId) {
        Orders order = shopService.declineOrder(orderId);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order Declined");
        responseBody.put("order", order);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(order.getUser().getUserId()), "/queue/order-notifications", responseBody);

        return ResponseEntity
                .ok()
                .body(responseBody);
    }

    // Method to mark order complete
    @PostMapping("/data/orders/complete/{id}")
    public ResponseEntity<Map<String, Object>> completeOrder(@PathVariable("id") Long orderId) {
        Orders order = shopService.completeOrder(orderId);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order Completed");
        responseBody.put("order", order);

        simpMessagingTemplate.convertAndSendToUser(String.valueOf(order.getUser().getUserId()), "/queue/order-notifications", responseBody);

        return ResponseEntity
                .ok()
                .body(responseBody);
    }

    /* Sample JSON data format sent to user for order-notifications
    {
        "message": "Order Accepted & Processing",
        "order": {
            "id": 123,
            "status": "PROCESSING",
            // ... other order details
        }
    }
     */

}
