package com.projects.cafe_winchester_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.projects.cafe_winchester_backend.entity.Address;
import com.projects.cafe_winchester_backend.entity.MenuCategory;
import com.projects.cafe_winchester_backend.entity.MenuItem;
import com.projects.cafe_winchester_backend.dto.AddMenuItemDto;
import com.projects.cafe_winchester_backend.entity.User;
import com.projects.cafe_winchester_backend.service.S3Service;
import com.projects.cafe_winchester_backend.service.ShopService;
import com.projects.cafe_winchester_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;

@SpringBootApplication
public class CafeWinchesterBackendApplication {

	@Autowired
	private ShopService shopService;


	@Autowired
	private UserService userService;


	public static void main(String[] args) {
		SpringApplication.run(CafeWinchesterBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase() {
		return args -> {

			// Setting up the Users and admins for testing purposes

			userService.createUser("patterson.leon1960@gmail.com", "user123", new String[]{"USER"});	// For Spring Security, User 1

			User newUser = userService.getUserById("patterson.leon1960@gmail.com");

			newUser.setEmail("patterson.leon1960@gmail.com");
			newUser.setPhoneNumber("0766411334");

			Address address = new Address();
			address.setLatitude(6.964069135177284);
			address.setLongitude(79.86877552240256);

			address.setUser(newUser);
			newUser.setAddress(address);

			userService.saveUser(newUser);


			userService.createUser("manager@example.com", "admin123", new String[]{"ADMIN"}); // For Spring Security, Admin 1

			User newUser2 = userService.getUserById("manager@example.com");

			newUser2.setEmail("manager@example.com");
			newUser2.setPhoneNumber("0721513314");

			Address address2 = new Address();
			address2.setLatitude(6.964069135177284);
			address2.setLongitude(79.86877552240256);

			address2.setUser(newUser2);
			newUser2.setAddress(address2);

			userService.saveUser(newUser2);


			// Adding sample items

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Long> categoryIds = new HashMap<>();
			Map<String, String> categoryFolders = new HashMap<>();

			String categoriesJson = """
                [
                    {"name": "Pancakes", "folder": "pancakes"},
                    {"name": "Waffles", "folder": "waffles"},
                    {"name": "Soup & Salads", "folder": "soup-salads"}
                ]
                """;

			String menuItemsJson = """
                [
                    {
                        "name": "Classic Buttermilk Pancakes",
                        "description": "Fluffy pancakes served with maple syrup and butter",
                        "price": "8.99",
                        "category": "Pancakes",
                        "imageName": "buttermilk.jpg"
                    },
                    {
                        "name": "Chocolate Chip Pancakes",
                        "description": "Pancakes loaded with chocolate chips and whipped cream",
                        "price": "10.99",
                        "category": "Pancakes",
                        "imageName": "chocolate-chip.jpg"
                    },
                    {
                        "name": "Blueberry Pancakes",
                        "description": "Fresh blueberry pancakes with berry compote",
                        "price": "11.99",
                        "category": "Pancakes",
                        "imageName": "blueberry.jpg"
                    },
                    {
                        "name": "Banana Nutella Pancakes",
                        "description": "Pancakes topped with banana slices and Nutella",
                        "price": "12.99",
                        "category": "Pancakes",
                        "imageName": "banana-nutella.jpg"
                    },
                    {
                        "name": "Mixed Berry Pancakes",
                        "description": "Pancakes with mixed berry compote and cream",
                        "price": "12.99",
                        "category": "Pancakes",
                        "imageName": "mixed-berry.jpg"
                    },
                    {
                        "name": "Classic Belgian Waffle",
                        "description": "Crispy Belgian waffle with maple syrup and butter",
                        "price": "9.99",
                        "category": "Waffles",
                        "imageName": "belgian.jpg"
                    },
                    {
                        "name": "Chicken & Waffle",
                        "description": "Crispy fried chicken with Belgian waffle and honey butter",
                        "price": "15.99",
                        "category": "Waffles",
                        "imageName": "chicken-waffle.jpg"
                    },
                    {
                        "name": "Chocolate Waffle",
                        "description": "Chocolate waffle with chocolate sauce and ice cream",
                        "price": "12.99",
                        "category": "Waffles",
                        "imageName": "chocolate-waffle.jpg"
                    },
                    {
                        "name": "Fruit Waffle",
                        "description": "Belgian waffle topped with fresh seasonal fruits",
                        "price": "13.99",
                        "category": "Waffles",
                        "imageName": "fruit-waffle.jpg"
                    },
                    {
                        "name": "Nutella Waffle",
                        "description": "Waffle topped with Nutella, banana and whipped cream",
                        "price": "13.99",
                        "category": "Waffles",
                        "imageName": "nutella-waffle.jpg"
                    },
                    {
                        "name": "Caesar Salad",
                        "description": "Crisp romaine, croutons, parmesan with Caesar dressing",
                        "price": "12.99",
                        "category": "Soup & Salads",
                        "imageName": "caesar-salad.jpg"
                    },
                    {
                        "name": "Cream of Mushroom Soup",
                        "description": "Rich and creamy mushroom soup with herbs",
                        "price": "8.99",
                        "category": "Soup & Salads",
                        "imageName": "mushroom-soup.jpg"
                    },
                    {
                        "name": "Greek Salad",
                        "description": "Mixed greens, feta, olives with balsamic dressing",
                        "price": "13.99",
                        "category": "Soup & Salads",
                        "imageName": "greek-salad.jpg"
                    },
                    {
                        "name": "Tomato Soup",
                        "description": "Classic tomato basil soup with croutons",
                        "price": "7.99",
                        "category": "Soup & Salads",
                        "imageName": "tomato-soup.jpg"
                    },
                    {
                        "name": "Chicken Salad",
                        "description": "Grilled chicken with mixed greens and honey mustard",
                        "price": "14.99",
                        "category": "Soup & Salads",
                        "imageName": "chicken-salad.jpg"
                    }
                ]
                """;

			try {
				// Create categories first
				JsonNode categoriesNode = mapper.readTree(categoriesJson);
				for (JsonNode categoryNode : categoriesNode) {
					String categoryName = categoryNode.get("name").asText();
					String folderName = categoryNode.get("folder").asText();

					MenuCategory category = shopService.addMenuCategory(categoryName);
					categoryIds.put(categoryName, category.getId());
					categoryFolders.put(categoryName, folderName);

					System.out.println("Created category: " + categoryName + " with folder: " + folderName);
				}

				// Create menu items with images
				JsonNode menuItemsNode = mapper.readTree(menuItemsJson);
				int totalItems = 0;
				int successfulItems = 0;
				int failedItems = 0;

				for (JsonNode itemNode : menuItemsNode) {
					totalItems++;
					try {
						String itemName = itemNode.get("name").asText();
						String category = itemNode.get("category").asText();
						String imageName = itemNode.get("imageName").asText();
						String categoryFolder = categoryFolders.get(category);

						String imagePath = String.format("menu-images/%s/%s", categoryFolder, imageName);

						// Load image
						Resource resource = new ClassPathResource("static/" + imagePath);
						if (!resource.exists()) {
							System.out.println("Warning: Image not found for " + itemName + " at path: " + imagePath);
							failedItems++;
							continue;
						}

						String mimeType = Files.probeContentType(resource.getFile().toPath());
						if (mimeType == null) {
							System.out.println("Warning: Image mimetype not found for " + itemName + " at path: " + imagePath);
							failedItems++;
							continue;
						}

						// Convert the file to MultipartFile
						MultipartFile imageFile = new MockMultipartFile(
								"newImage",                          // name of the parameter
								resource.getFilename(),          // original filename
								mimeType,                    // content type
								resource.getInputStream()        // file content
						);

						AddMenuItemDto menuItemDto = new AddMenuItemDto();
						menuItemDto.setName(itemName);
						menuItemDto.setDescription(itemNode.get("description").asText());
						menuItemDto.setPrice(new BigDecimal(itemNode.get("price").asText()));
						menuItemDto.setMenuCategoryId(categoryIds.get(category));
						menuItemDto.setNewImage(imageFile);

						MenuItem savedItem = shopService.addMenuItem(menuItemDto);
						successfulItems++;
						System.out.println("Successfully created menu item: " + savedItem.getName());

					} catch (IOException e) {
						failedItems++;
						System.out.println("Error processing menu item: " + itemNode.get("name").asText());
						System.out.println("Error details: " + e.getMessage());
					}
				}

				// Print summary
				System.out.println("\n=== Menu Creation Summary ===");
				System.out.println("Total items processed: " + totalItems);
				System.out.println("Successfully created: " + successfulItems);
				System.out.println("Failed to create: " + failedItems);
				System.out.println("========================");

			} catch (JsonProcessingException e) {
				System.out.println("Error parsing JSON data: " + e.getMessage());
				System.out.println("Please check the JSON format and try again.");
			} catch (Exception e) {
				System.out.println("Unexpected error occurred: " + e.getMessage());
			}
		};
	}
}