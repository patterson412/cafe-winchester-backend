package com.projects.cafe_winchester_backend.service;

import com.projects.cafe_winchester_backend.dto.AddMenuItemDto;
import com.projects.cafe_winchester_backend.dto.MenuItemDto;
import com.projects.cafe_winchester_backend.entity.MenuCategory;
import com.projects.cafe_winchester_backend.entity.MenuItem;
import com.projects.cafe_winchester_backend.entity.OrderStatus;
import com.projects.cafe_winchester_backend.entity.Orders;
import com.projects.cafe_winchester_backend.repository.MenuCategoryDao;
import com.projects.cafe_winchester_backend.repository.MenuItemDao;
import com.projects.cafe_winchester_backend.repository.OrderDao;
import com.projects.cafe_winchester_backend.repository.PaymentDao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final MenuItemDao menuItemDao;
    private final OrderDao orderDao;
    private final PaymentDao paymentDao;
    private final S3Service s3Service;
    private final MenuCategoryDao menuCategoryDao;
    private final DistributedLockService lockService;

    public ShopService(MenuItemDao menuItemDao, OrderDao orderDao, PaymentDao paymentDao,
                       S3Service s3Service, MenuCategoryDao menuCategoryDao,
                       DistributedLockService lockService) {
        this.menuItemDao = menuItemDao;
        this.orderDao = orderDao;
        this.paymentDao = paymentDao;
        this.s3Service = s3Service;
        this.menuCategoryDao = menuCategoryDao;
        this.lockService = lockService;
    }

    public MenuItem getMenuItemById(Long menuItemId) {
        return menuItemDao.findById(menuItemId).orElseThrow(() ->
                new NoSuchElementException("Menu Item not found with id: " + menuItemId));
    }

    public MenuItem getMenuItemByName(String name) {
        return menuItemDao.findByNameContainingIgnoreCase(name).orElseThrow(() ->
                new NoSuchElementException("Menu Item not found with name: " + name));
    }

    public Orders getLatestOrderOfUser(Long userId) {
        return orderDao.findFirstByUserUserIdOrderByOrderDateDesc(userId).orElseThrow(() ->
                new NoSuchElementException("Order not found under User ID: " + userId));
    }

    @Transactional
    public MenuItem updateMenuItem(Long itemId, MenuItemDto menuItemDto) {
        String lockKey = "menu-item-lock:" + itemId;

        try {
            if (!lockService.acquireLock(lockKey)) {
                throw new RuntimeException("Unable to acquire lock for menu item update");
            }

            MenuItem menuItem = menuItemDao.findById(itemId)
                    .orElseThrow(() -> new NoSuchElementException("Menu Item not found with id: " + itemId));

            if (menuItemDto.getName() == null || menuItemDto.getName().trim().isEmpty() ||
                    menuItemDto.getDescription() == null || menuItemDto.getDescription().trim().isEmpty() ||
                    menuItemDto.getPrice() == null ||
                    menuItemDto.getMenuCategoryId() == null) {
                throw new RuntimeException("Required fields for updating Menu Item is missing");
            }

            menuItem.setName(menuItemDto.getName());
            menuItem.setDescription(menuItemDto.getDescription());
            menuItem.setPrice(menuItemDto.getPrice());

            menuItem.setCategory(menuCategoryDao.findById(menuItemDto.getMenuCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Menu Category not found with id: " + menuItemDto.getMenuCategoryId())));

            if (menuItemDto.getNewImage() != null && !menuItemDto.getNewImage().isEmpty()) {
                try {
                    String newImageUrl = s3Service.uploadFile(menuItemDto.getNewImage(), S3Service.ImageType.MENU_ITEM);
                    menuItem.setImageUrl(newImageUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                }
            }

            return menuItemDao.save(menuItem);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    public Map<String, Object> getAllProducts() {
        return menuCategoryDao.findAll().stream()
                .collect(Collectors.toMap(
                        MenuCategory::getName,
                        category -> category.getMenuItems().stream()
                                .map(item -> {
                                    Map<String, Object> itemDetails = new HashMap<>();
                                    itemDetails.put("id", item.getId());
                                    itemDetails.put("name", item.getName());
                                    itemDetails.put("imageUrl", item.getImageUrl());
                                    itemDetails.put("description", item.getDescription());
                                    itemDetails.put("price", item.getPrice());
                                    itemDetails.put("categoryId", item.getCategory().getId());
                                    itemDetails.put("categoryName", item.getCategory().getName());
                                    return itemDetails;
                                })
                                .toList()
                ));
    }

    public List<Map<String, Object>> getAllCategoriesNamesAndIdOnly() {
        return menuCategoryDao.findAll().stream()
                .map(category -> {
                    Map<String, Object> simplifiedCategory = new HashMap<>();
                    simplifiedCategory.put("id", category.getId());
                    simplifiedCategory.put("name", category.getName());
                    return simplifiedCategory;
                })
                .toList();
    }

    @Transactional
    public MenuItem addMenuItem(AddMenuItemDto addMenuItemDto) {
        String lockKey = "menu-items-lock";

        try {
            if (!lockService.acquireLock(lockKey)) {
                throw new RuntimeException("Unable to acquire lock for adding menu item");
            }

            if (addMenuItemDto.getName() == null || addMenuItemDto.getName().trim().isEmpty() ||
                    addMenuItemDto.getDescription() == null || addMenuItemDto.getDescription().trim().isEmpty() ||
                    addMenuItemDto.getPrice() == null ||
                    addMenuItemDto.getMenuCategoryId() == null) {
                throw new RuntimeException("Required fields for adding new Menu Item is missing");
            }

            MenuItem menuItem = new MenuItem();
            menuItem.setName(addMenuItemDto.getName());
            menuItem.setPrice(addMenuItemDto.getPrice());
            menuItem.setCategory(menuCategoryDao.findById(addMenuItemDto.getMenuCategoryId()).orElseThrow(
                    () -> new NoSuchElementException("Menu category not found with ID: " + addMenuItemDto.getMenuCategoryId())));
            if (addMenuItemDto.getNewImage() != null && !addMenuItemDto.getNewImage().isEmpty()) {
                menuItem.setImageUrl(s3Service.uploadFile(addMenuItemDto.getNewImage(), S3Service.ImageType.MENU_ITEM));
            }
            menuItem.setDescription(addMenuItemDto.getDescription());
            return menuItemDao.save(menuItem);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    @Transactional
    public MenuCategory addMenuCategory(String name) {
        String lockKey = "menu-categories-lock";

        try {
            if (!lockService.acquireLock(lockKey)) {
                throw new RuntimeException("Unable to acquire lock for adding menu category");
            }

            MenuCategory menuCategory = new MenuCategory();
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("'name' cannot be empty when adding a new Menu Category");
            }
            menuCategory.setName(name);
            return menuCategoryDao.save(menuCategory);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    public Orders getOrder(Long id) {
        return orderDao.findById(id).orElseThrow(() -> new NoSuchElementException("Cannot find Order with ID: " + id));
    }

    @Transactional
    public Orders acceptOrder(Long id) {
        String lockKey = "order-lock:" + id;

        try {
            if (!lockService.acquireLock(lockKey)) {
                throw new RuntimeException("Unable to acquire lock for accepting order");
            }

            Orders order = orderDao.findById(id).orElseThrow(() -> new NoSuchElementException("Cannot find Order with ID: " + id));
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("Order must be in PENDING state to be accepted");
            }
            order.setStatus(OrderStatus.PROCESSING);
            return orderDao.save(order);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    @Transactional
    public Orders declineOrder(Long id) {
        String lockKey = "order-lock:" + id;

        try {
            if (!lockService.acquireLock(lockKey)) {
                throw new RuntimeException("Unable to acquire lock for declining order");
            }

            Orders order = orderDao.findById(id).orElseThrow(() -> new NoSuchElementException("Cannot find Order with ID: " + id));
            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
                throw new IllegalStateException("Order must be in PENDING or PROCESSING state to be declined");
            }
            order.setStatus(OrderStatus.CANCELLED);
            return orderDao.save(order);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    @Transactional
    public Orders completeOrder(Long id) {
        String lockKey = "order-lock:" + id;

        try {
            if (!lockService.acquireLock(lockKey)) {
                throw new RuntimeException("Unable to acquire lock for completing order");
            }

            Orders order = orderDao.findById(id).orElseThrow(() -> new NoSuchElementException("Cannot find Order with ID: " + id));
            if (order.getStatus() != OrderStatus.PROCESSING) {
                throw new IllegalStateException("Order must be in PROCESSING state to be completed");
            }
            order.setStatus(OrderStatus.COMPLETED);
            return orderDao.save(order);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    public List<Orders> getAllCurrentOrders() {
        return orderDao.findByStatusNotInOrderByOrderDateDesc(
                Arrays.asList(OrderStatus.COMPLETED, OrderStatus.CANCELLED)
        );
    }

    public List<Orders> getAllOrders() {
        return orderDao.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
    }
}