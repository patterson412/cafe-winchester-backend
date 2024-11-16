package com.projects.cafe_winchester_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.projects.cafe_winchester_backend.entity.MenuCategory;
import com.projects.cafe_winchester_backend.entity.MenuItem;
import com.projects.cafe_winchester_backend.dto.AddMenuItemDto;
import com.projects.cafe_winchester_backend.service.ShopService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.mock.web.MockMultipartFile;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CafeWinchesterBackendApplication {

	@Autowired
	private ShopService shopService;

	public static void main(String[] args) {
		SpringApplication.run(CafeWinchesterBackendApplication.class, args);
	}

	@PostConstruct
	public void insertData() {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Long> categoryIds = new HashMap<>();
		Map<String, String> categoryFolders = new HashMap<>();

		String categoriesJson = """
            [
                {"name": "Pancakes", "folder": "pancakes"},
                {"name": "Waffles", "folder": "waffles"},
                {"name": "Soup & Salads", "folder": "soup-salads"},
                {"name": "Short Bites", "folder": "short-bites"},
                {"name": "Sandwiches & Wraps", "folder": "sandwiches"},
                {"name": "Quesadilla", "folder": "quesadilla"},
                {"name": "Burgers & Fries", "folder": "burgers"},
                {"name": "Pol Rotti & Burgers", "folder": "pol-rotti"},
                {"name": "Rice, Pasta & Noodles", "folder": "rice-pasta"},
                {"name": "Cakes & Desserts", "folder": "desserts"},
                {"name": "Hot Chocolate", "folder": "hot-chocolate"},
                {"name": "Hot Coffee", "folder": "hot-coffee"},
                {"name": "Cold Coffee", "folder": "cold-coffee"},
                {"name": "Shakes", "folder": "shakes"},
                {"name": "Mojito", "folder": "mojito"},
                {"name": "Smoothies", "folder": "smoothies"},
                {"name": "Juice", "folder": "juice"},
                {"name": "Iced Tea", "folder": "iced-tea"},
                {"name": "Crushers", "folder": "crushers"},
                {"name": "Water Bottles", "folder": "water"}
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
                },
                {
                    "name": "French Fries",
                    "description": "Crispy golden fries with special seasoning",
                    "price": "5.99",
                    "category": "Short Bites",
                    "imageName": "french-fries.jpg"
                },
                {
                    "name": "Chicken Wings",
                    "description": "Spicy chicken wings with blue cheese dip",
                    "price": "13.99",
                    "category": "Short Bites",
                    "imageName": "chicken-wings.jpg"
                },
                {
                    "name": "Mozzarella Sticks",
                    "description": "Crispy fried mozzarella with marinara sauce",
                    "price": "9.99",
                    "category": "Short Bites",
                    "imageName": "mozzarella-sticks.jpg"
                },
                {
                    "name": "Onion Rings",
                    "description": "Crispy battered onion rings with dip",
                    "price": "7.99",
                    "category": "Short Bites",
                    "imageName": "onion-rings.jpg"
                },
                {
                    "name": "Nachos",
                    "description": "Tortilla chips with cheese, salsa, and guacamole",
                    "price": "11.99",
                    "category": "Short Bites",
                    "imageName": "nachos.jpg"
                },
                {
                    "name": "Club Sandwich",
                    "description": "Triple-decker with chicken, bacon, lettuce, and tomato",
                    "price": "14.99",
                    "category": "Sandwiches & Wraps",
                    "imageName": "club-sandwich.jpg"
                },
                {
                    "name": "Chicken Caesar Wrap",
                    "description": "Grilled chicken with romaine and Caesar dressing",
                    "price": "12.99",
                    "category": "Sandwiches & Wraps",
                    "imageName": "caesar-wrap.jpg"
                },
                {
                    "name": "Veggie Wrap",
                    "description": "Fresh vegetables with hummus and feta",
                    "price": "11.99",
                    "category": "Sandwiches & Wraps",
                    "imageName": "veggie-wrap.jpg"
                },
                {
                    "name": "BLT Sandwich",
                    "description": "Classic bacon, lettuce, and tomato on toasted bread",
                    "price": "10.99",
                    "category": "Sandwiches & Wraps",
                    "imageName": "blt-sandwich.jpg"
                },
                {
                    "name": "Tuna Sandwich",
                    "description": "Tuna salad with lettuce and cucumber",
                    "price": "11.99",
                    "category": "Sandwiches & Wraps",
                    "imageName": "tuna-sandwich.jpg"
                },
                {
                    "name": "Chicken Quesadilla",
                    "description": "Grilled chicken and cheese in crispy tortilla",
                    "price": "13.99",
                    "category": "Quesadilla",
                    "imageName": "chicken-quesadilla.jpg"
                },
                {
                    "name": "Cheese Quesadilla",
                    "description": "Blend of melted cheeses with peppers",
                    "price": "11.99",
                    "category": "Quesadilla",
                    "imageName": "cheese-quesadilla.jpg"
                },
                {
                    "name": "Beef Quesadilla",
                    "description": "Seasoned beef with cheese and peppers",
                    "price": "14.99",
                    "category": "Quesadilla",
                    "imageName": "beef-quesadilla.jpg"
                },
                {
                    "name": "Veggie Quesadilla",
                    "description": "Grilled vegetables with cheese blend",
                    "price": "12.99",
                    "category": "Quesadilla",
                    "imageName": "veggie-quesadilla.jpg"
                },
                {
                    "name": "Mushroom Quesadilla",
                    "description": "Sautéed mushrooms with herbs and cheese",
                    "price": "12.99",
                    "category": "Quesadilla",
                    "imageName": "mushroom-quesadilla.jpg"
                },
                {
                    "name": "Classic Beef Burger",
                    "description": "Angus beef patty with cheese and fries",
                    "price": "16.99",
                    "category": "Burgers & Fries",
                    "imageName": "beef-burger.jpg"
                },
                {
                    "name": "Chicken Burger",
                    "description": "Grilled chicken breast with special sauce",
                    "price": "14.99",
                    "category": "Burgers & Fries",
                    "imageName": "chicken-burger.jpg"
                },
                {
                    "name": "Mushroom Swiss Burger",
                    "description": "Beef patty with mushrooms and Swiss cheese",
                    "price": "17.99",
                    "category": "Burgers & Fries",
                    "imageName": "mushroom-burger.jpg"
                },
                {
                    "name": "BBQ Bacon Burger",
                    "description": "Beef patty with bacon and BBQ sauce",
                    "price": "18.99",
                    "category": "Burgers & Fries",
                    "imageName": "bbq-burger.jpg"
                },
                {
                    "name": "Veggie Burger",
                    "description": "Plant-based patty with fresh toppings",
                    "price": "15.99",
                    "category": "Burgers & Fries",
                    "imageName": "veggie-burger.jpg"
                },
                {
                    "name": "Chicken Pol Rotti Burger",
                    "description": "Spicy chicken with pol rotti bun and curry sauce",
                    "price": "14.99",
                    "category": "Pol Rotti & Burgers",
                    "imageName": "chicken-pol-rotti.jpg"
                },
                {
                    "name": "Fish Pol Rotti Burger",
                    "description": "Crispy fish fillet with pol rotti and spicy sauce",
                    "price": "15.99",
                    "category": "Pol Rotti & Burgers",
                    "imageName": "fish-pol-rotti.jpg"
                },
                {
                    "name": "Egg Pol Rotti Burger",
                    "description": "Fried egg with pol rotti and curry sauce",
                    "price": "13.99",
                    "category": "Pol Rotti & Burgers",
                    "imageName": "egg-pol-rotti.jpg"
                },
                {
                    "name": "Beef Pol Rotti Burger",
                    "description": "Spicy beef with pol rotti and special sauce",
                    "price": "16.99",
                    "category": "Pol Rotti & Burgers",
                    "imageName": "beef-pol-rotti.jpg"
                },
                {
                    "name": "Vegetable Pol Rotti Burger",
                    "description": "Mixed vegetables with pol rotti and curry sauce",
                    "price": "13.99",
                    "category": "Pol Rotti & Burgers",
                    "imageName": "veg-pol-rotti.jpg"
                },
                {
                    "name": "Chicken Fried Rice",
                    "description": "Wok-fried rice with chicken and vegetables",
                    "price": "15.99",
                    "category": "Rice, Pasta & Noodles",
                    "imageName": "chicken-fried-rice.jpg"
                },
                {
                    "name": "Fettuccine Alfredo",
                    "description": "Creamy pasta with parmesan and garlic",
                    "price": "16.99",
                    "category": "Rice, Pasta & Noodles",
                    "imageName": "fettuccine.jpg"
                },
                {
                    "name": "Pad Thai",
                    "description": "Thai style noodles with shrimp and peanuts",
                    "price": "17.99",
                    "category": "Rice, Pasta & Noodles",
                    "imageName": "pad-thai.jpg"
                },
                {
                    "name": "Vegetable Noodles",
                    "description": "Stir-fried noodles with mixed vegetables",
                    "price": "14.99",
                    "category": "Rice, Pasta & Noodles",
                    "imageName": "veg-noodles.jpg"
                },
                {
                    "name": "Seafood Pasta",
                    "description": "Mixed seafood in creamy tomato sauce",
                    "price": "18.99",
                    "category": "Rice, Pasta & Noodles",
                    "imageName": "seafood-pasta.jpg"
                },
                {
                    "name": "Chocolate Lava Cake",
                    "description": "Warm chocolate cake with molten center",
                    "price": "8.99",
                    "category": "Cakes & Desserts",
                    "imageName": "lava-cake.jpg"
                },
                {
                    "name": "New York Cheesecake",
                    "description": "Classic cheesecake with berry compote",
                    "price": "7.99",
                    "category": "Cakes & Desserts",
                    "imageName": "cheesecake.jpg"
                },
                {
                    "name": "Tiramisu",
                    "description": "Italian coffee-flavored dessert",
                    "price": "8.99",
                    "category": "Cakes & Desserts",
                    "imageName": "tiramisu.jpg"
                },
                {
                    "name": "Apple Pie",
                    "description": "Warm apple pie with vanilla ice cream",
                    "price": "7.99",
                    "category": "Cakes & Desserts",
                    "imageName": "apple-pie.jpg"
                },
                {
                    "name": "Brownie Sundae",
                    "description": "Warm brownie with ice cream and sauce",
                    "price": "9.99",
                    "category": "Cakes & Desserts",
                    "imageName": "brownie-sundae.jpg"
                },
                {
                    "name": "Classic Hot Chocolate",
                    "description": "Rich hot chocolate with whipped cream",
                    "price": "4.99",
                    "category": "Hot Chocolate",
                    "imageName": "classic-hot-chocolate.jpg"
                },
                {
                    "name": "Mint Hot Chocolate",
                    "description": "Hot chocolate with mint flavor and cream",
                    "price": "5.49",
                    "category": "Hot Chocolate",
                    "imageName": "mint-hot-chocolate.jpg"
                },
                {
                    "name": "Dark Hot Chocolate",
                    "description": "Dark chocolate blend with whipped cream",
                    "price": "5.49",
                    "category": "Hot Chocolate",
                    "imageName": "dark-hot-chocolate.jpg"
                },
                {
                    "name": "Hazelnut Hot Chocolate",
                    "description": "Hot chocolate with hazelnut flavor",
                    "price": "5.99",
                    "category": "Hot Chocolate",
                    "imageName": "hazelnut-hot-chocolate.jpg"
                },
                {
                    "name": "White Hot Chocolate",
                    "description": "Creamy white chocolate drink",
                    "price": "5.99",
                    "category": "Hot Chocolate",
                    "imageName": "white-hot-chocolate.jpg"
                },
                {
                    "name": "Espresso",
                    "description": "Single shot of espresso",
                    "price": "2.99",
                    "category": "Hot Coffee",
                    "imageName": "espresso.jpg"
                },
                {
                    "name": "Cappuccino",
                    "description": "Espresso with steamed milk and foam",
                    "price": "4.99",
                    "category": "Hot Coffee",
                    "imageName": "cappuccino.jpg"
                },
                {
                    "name": "Cafe Latte",
                    "description": "Espresso with steamed milk",
                    "price": "4.99",
                    "category": "Hot Coffee",
                    "imageName": "latte.jpg"
                },
                {
                    "name": "Americano",
                    "description": "Espresso with hot water",
                    "price": "3.99",
                    "category": "Hot Coffee",
                    "imageName": "americano.jpg"
                },
                {
                    "name": "Mocha",
                    "description": "Espresso with chocolate and steamed milk",
                    "price": "5.49",
                    "category": "Hot Coffee",
                    "imageName": "mocha.jpg"
                },
                {
                    "name": "Iced Latte",
                    "description": "Chilled espresso with cold milk",
                    "price": "5.49",
                    "category": "Cold Coffee",
                    "imageName": "iced-latte.jpg"
                },
                {
                    "name": "Iced Mocha",
                    "description": "Chilled mocha with whipped cream",
                    "price": "5.99",
                    "category": "Cold Coffee",
                    "imageName": "iced-mocha.jpg"
                },
                {
                    "name": "Cold Brew",
                    "description": "Smooth cold brewed coffee",
                    "price": "4.99",
                    "category": "Cold Coffee",
                    "imageName": "cold-brew.jpg"
                },
                {
                    "name": "Frappuccino",
                    "description": "Blended coffee with cream",
                    "price": "6.49",
                    "category": "Cold Coffee",
                    "imageName": "frappuccino.jpg"
                },
                {
                    "name": "Iced Americano",
                    "description": "Chilled espresso with water",
                    "price": "4.49",
                    "category": "Cold Coffee",
                    "imageName": "iced-americano.jpg"
                },
                {
                    "name": "Vanilla Milkshake",
                    "description": "Classic vanilla shake with cream",
                    "price": "6.99",
                    "category": "Shakes",
                    "imageName": "vanilla-shake.jpg"
                },
                {
                    "name": "Chocolate Milkshake",
                    "description": "Rich chocolate shake with cream",
                    "price": "6.99",
                    "category": "Shakes",
                    "imageName": "chocolate-shake.jpg"
                },
                {
                    "name": "Strawberry Milkshake",
                    "description": "Fresh strawberry shake",
                    "price": "7.49",
                    "category": "Shakes",
                    "imageName": "strawberry-shake.jpg"
                },
                {
                    "name": "Oreo Milkshake",
                    "description": "Cookies and cream shake",
                    "price": "7.99",
                    "category": "Shakes",
                    "imageName": "oreo-shake.jpg"
                },
                {
                    "name": "Banana Milkshake",
                    "description": "Fresh banana shake with cream",
                    "price": "7.49",
                    "category": "Shakes",
                    "imageName": "banana-shake.jpg"
                },
                {
                    "name": "Classic Mojito",
                    "description": "Mint, lime and soda refresher",
                    "price": "6.99",
                    "category": "Mojito",
                    "imageName": "classic-mojito.jpg"
                },
                {
                    "name": "Strawberry Mojito",
                    "description": "Strawberry and mint refresher",
                    "price": "7.49",
                    "category": "Mojito",
                    "imageName": "strawberry-mojito.jpg"
                },
                {
                    "name": "Blueberry Mojito",
                    "description": "Blueberry and mint refresher",
                    "price": "7.49",
                    "category": "Mojito",
                    "imageName": "blueberry-mojito.jpg"
                },
                {
                    "name": "Passion Fruit Mojito",
                    "description": "Tropical passion fruit mojito",
                    "price": "7.99",
                    "category": "Mojito",
                    "imageName": "passion-mojito.jpg"
                },
                {
                    "name": "Watermelon Mojito",
                    "description": "Fresh watermelon and mint",
                    "price": "7.49",
                    "category": "Mojito",
                    "imageName": "watermelon-mojito.jpg"
                },
                {
                    "name": "Berry Blast Smoothie",
                    "description": "Mixed berries with yogurt",
                    "price": "7.99",
                    "category": "Smoothies",
                    "imageName": "berry-smoothie.jpg"
                },
                {
                    "name": "Mango Smoothie",
                    "description": "Fresh mango with yogurt",
                    "price": "7.49",
                    "category": "Smoothies",
                    "imageName": "mango-smoothie.jpg"
                },
                {
                    "name": "Green Smoothie",
                    "description": "Spinach, apple and banana blend",
                    "price": "7.99",
                    "category": "Smoothies",
                    "imageName": "green-smoothie.jpg"
                },
                {
                    "name": "Banana Oat Smoothie",
                    "description": "Banana and oats with honey",
                    "price": "7.49",
                    "category": "Smoothies",
                    "imageName": "banana-smoothie.jpg"
                },
                {
                    "name": "Tropical Smoothie",
                    "description": "Mixed tropical fruits blend",
                    "price": "7.99",
                    "category": "Smoothies",
                    "imageName": "tropical-smoothie.jpg"
                },
                {
                    "name": "Fresh Orange Juice",
                    "description": "Freshly squeezed orange juice",
                    "price": "4.99",
                    "category": "Juice",
                    "imageName": "orange-juice.jpg"
                },
                {
                    "name": "Watermelon Juice",
                    "description": "Fresh watermelon juice",
                    "price": "4.99",
                    "category": "Juice",
                    "imageName": "watermelon-juice.jpg"
                },
                {
                    "name": "Apple Juice",
                    "description": "Fresh apple juice",
                    "price": "4.99",
                    "category": "Juice",
                    "imageName": "apple-juice.jpg"
                },
                {
                    "name": "Pineapple Juice",
                    "description": "Fresh pineapple juice",
                    "price": "5.49",
                    "category": "Juice",
                    "imageName": "pineapple-juice.jpg"
                },
                {
                    "name": "Mixed Fruit Juice",
                    "description": "Blend of seasonal fruits",
                    "price": "5.99",
                    "category": "Juice",
                    "imageName": "mixed-juice.jpg"
                },
                {
                    "name": "Classic Iced Tea",
                    "description": "Traditional black iced tea",
                    "price": "3.99",
                    "category": "Iced Tea",
                    "imageName": "classic-tea.jpg"
                },
                {
                    "name": "Peach Iced Tea",
                    "description": "Iced tea with peach flavor",
                    "price": "4.49",
                    "category": "Iced Tea",
                    "imageName": "peach-tea.jpg"
                },
                {
                    "name": "Lemon Iced Tea",
                    "description": "Iced tea with fresh lemon",
                    "price": "4.49",
                    "category": "Iced Tea",
                    "imageName": "lemon-tea.jpg"
                },
                {
                    "name": "Green Iced Tea",
                    "description": "Chilled green tea",
                    "price": "4.49",
                    "category": "Iced Tea",
                    "imageName": "green-tea.jpg"
                },
                {
                    "name": "Passion Fruit Iced Tea",
                    "description": "Iced tea with passion fruit",
                    "price": "4.99",
                    "category": "Iced Tea",
                    "imageName": "passion-tea.jpg"
                },
                {
                    "name": "Oreo Crusher",
                    "description": "Crushed Oreos with ice cream",
                    "price": "7.99",
                    "category": "Crushers",
                    "imageName": "oreo-crusher.jpg"
                },
                {
                    "name": "Chocolate Chip Crusher",
                    "description": "Chocolate and cookie blend",
                    "price": "7.99",
                    "category": "Crushers",
                    "imageName": "choc-chip-crusher.jpg"
                },
                {
                    "name": "Caramel Crusher",
                    "description": "Caramel coffee crusher",
                    "price": "7.99",
                    "category": "Crushers",
                    "imageName": "caramel-crusher.jpg"
                },
                {
                    "name": "Mocha Crusher",
                    "description": "Coffee and chocolate crusher",
                    "price": "7.99",
                    "category": "Crushers",
                    "imageName": "mocha-crusher.jpg"
                },
                {
                    "name": "Vanilla Crusher",
                    "description": "Vanilla bean crusher",
                    "price": "7.49",
                    "category": "Crushers",
                    "imageName": "vanilla-crusher.jpg"
                },
                {
                    "name": "Mineral Water 500ml",
                    "description": "Natural mineral water",
                    "price": "1.99",
                    "category": "Water Bottles",
                    "imageName": "small-water.jpg"
                },
                {
                    "name": "Mineral Water 1L",
                    "description": "Natural mineral water",
                    "price": "2.99",
                    "category": "Water Bottles",
                    "imageName": "large-water.jpg"
                },
                {
                    "name": "Sparkling Water 500ml",
                    "description": "Carbonated mineral water",
                    "price": "2.49",
                    "category": "Water Bottles",
                    "imageName": "small-sparkling.jpg"
                },
                {
                    "name": "Sparkling Water 1L",
                    "description": "Carbonated mineral water",
                    "price": "3.49",
                    "category": "Water Bottles",
                    "imageName": "large-sparkling.jpg"
                },
                {
                    "name": "Flavored Water 500ml",
                    "description": "Lightly flavored mineral water",
                    "price": "2.99",
                    "category": "Water Bottles",
                    "imageName": "flavored-water.jpg"
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
	}
}