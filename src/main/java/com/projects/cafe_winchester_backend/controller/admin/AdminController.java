package com.projects.cafe_winchester_backend.controller.admin;

import com.projects.cafe_winchester_backend.dto.AddMenuItemDto;
import com.projects.cafe_winchester_backend.dto.MenuItemDto;
import com.projects.cafe_winchester_backend.entity.MenuCategory;
import com.projects.cafe_winchester_backend.entity.MenuItem;
import com.projects.cafe_winchester_backend.entity.Orders;
import com.projects.cafe_winchester_backend.repository.MenuItemDao;
import com.projects.cafe_winchester_backend.service.S3Service;
import com.projects.cafe_winchester_backend.service.ShopService;
import com.projects.cafe_winchester_backend.service.UserManagementService;
import com.projects.cafe_winchester_backend.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.util.*;

@RestController
@RequestMapping("/api/manage")
public class AdminController {

    private S3Service s3Service;
    private UserService userService;
    private ShopService shopService;
    private UserManagementService userManagementService;

    private SimpMessagingTemplate simpMessagingTemplate;

    public AdminController(S3Service s3Service, UserService userService, ShopService shopService, UserManagementService userManagementService, SimpMessagingTemplate simpMessagingTemplate) {
        this.s3Service = s3Service;
        this.userService = userService;
        this.shopService = shopService;
        this.userManagementService = userManagementService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PutMapping("/data/menuitem/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable("id") Long itemId, @Valid @ModelAttribute MenuItemDto menuItemDto) {
        MenuItem updatedMenuItem = shopService.updateMenuItem(itemId, menuItemDto);
        return ResponseEntity.ok(updatedMenuItem);
    }

    @GetMapping("/data/currentorders")
    public ResponseEntity<List<Orders>> getCurrentOrders() {
        List<Orders> orders = shopService.getAllCurrentOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/data/allorders")
    public ResponseEntity<List<Orders>> getAllOrders() {
        List<Orders> orders = shopService.getAllOrders();
        return ResponseEntity.ok(orders);
    }


    // Method to get all categories returning only the category name and ID
    @GetMapping("/data/menucategories")
    public ResponseEntity<?> getAllCategoriesOnly() {
        List<Map<String, Object>> categories = shopService.getAllCategoriesNamesAndIdOnly();
        return ResponseEntity.ok(categories);
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
        return ResponseEntity.ok(newMenuItem);
    }

    // Method to create empty new Category
    @PostMapping("/data/menucategories")
    public ResponseEntity<Map<String, Object>> addMenuCategory(@RequestBody Map<String, String> payload) {
        MenuCategory newMenuCategory = shopService.addMenuCategory(payload.get("name"));
        Map<String, Object> simplifiedCategory = new HashMap<>();
        simplifiedCategory.put("id", newMenuCategory.getId());
        simplifiedCategory.put("name", newMenuCategory.getName());
        return ResponseEntity.ok(simplifiedCategory);
    }

    // Method to accept order
    @PostMapping("/data/orders/accept/{id}")
    public synchronized ResponseEntity<Orders> acceptOrder(@PathVariable("id") Long orderId) {
        Orders order = shopService.acceptOrder(orderId);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order Accepted & Processing");
        responseBody.put("order", order);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(order.getUser().getUserId()), "/queue/order-notifications", responseBody);
        return ResponseEntity.ok(order);
    }

    // Method to decline order
    @PostMapping("/data/orders/decline/{id}")
    public ResponseEntity<Orders> declineOrder(@PathVariable("id") Long orderId) {
        Orders order = shopService.declineOrder(orderId);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order Declined");
        responseBody.put("order", order);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(order.getUser().getUserId()), "/queue/order-notifications", responseBody);
        return ResponseEntity.ok(order);
    }

    // Method to mark order complete
    @PostMapping("/data/orders/complete/{id}")
    public ResponseEntity<Orders> completeOrder(@PathVariable("id") Long orderId) {
        Orders order = shopService.completeOrder(orderId);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Order Completed");
        responseBody.put("order", order);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(order.getUser().getUserId()), "/queue/order-notifications", responseBody);
        return ResponseEntity.ok(order);
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
