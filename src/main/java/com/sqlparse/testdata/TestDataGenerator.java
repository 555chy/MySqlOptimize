package com.sqlparse.testdata;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Random;

public class TestDataGenerator {

    private static final int BATCH_SIZE = 10000;
    private static final Random random = new Random(42);

    private static final String[] FIRST_NAMES = {"Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Henry", "Ivy", "Jack", "Olivia", "Noah", "Sophia", "Liam", "Mia", "Ethan", "Ava", "Oliver", "Isabella", "William"};
    private static final String[] LAST_NAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson"};
    private static final String[] CITIES = {"New York", "London", "Tokyo", "Paris", "Berlin", "Sydney", "Beijing", "Moscow", "Dubai", "Singapore", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia"};
    private static final String[] COUNTRIES = {"USA", "UK", "Japan", "France", "Germany", "Australia", "China", "Russia", "UAE", "Singapore", "Canada", "Brazil", "India", "Italy", "Spain"};
    private static final String[] CATEGORY_NAMES = {"Electronics", "Clothing", "Home", "Sports", "Books", "Toys", "Beauty", "Grocery", "Auto", "Garden"};
    private static final String[] BRAND_NAMES = {"Apple", "Samsung", "Nike", "Sony", "Microsoft", "Adidas", "LG", "Coca-Cola", "Toyota", "IKEA", "Amazon", "Google", "Meta", "Tesla", "Netflix"};
    private static final String[] PRODUCT_PREFIXES = {"Premium", "Deluxe", "Super", "Ultra", "Mega", "Pro", "Elite", "Classic", "Modern", "Basic", "Advanced", "Essential", "Ultimate", "Professional", "Compact"};
    private static final String[] PRODUCT_SUFFIXES = {"Widget", "Gadget", "Device", "Kit", "Set", "System", "Package", "Bundle", "Essential", "Collection", "Solution", "Tool", "Appliance", "Accessory", "Component"};
    private static final String[] ORDER_STATUSES = {"pending", "processing", "shipped", "delivered", "cancelled"};
    private static final String[] PAYMENT_METHODS = {"credit_card", "paypal", "bank_transfer", "digital_wallet"};
    private static final String[] PAYMENT_STATUSES = {"pending", "completed", "failed", "refunded"};
    private static final String[] SHIPPING_CARRIERS = {"FedEx", "UPS", "DHL", "USPS", "Amazon Logistics"};
    private static final String[] SHIPPING_STATUSES = {"preparing", "in_transit", "out_for_delivery", "delivered"};
    private static final String[] REVIEW_TEXTS = {"Great product!", "Very satisfied", "Could be better", "Highly recommend", "Not worth the price", "Excellent quality", "Fast shipping", "Worst purchase ever", "Perfect", "Just okay", "Amazing!", "Disappointing", "Will buy again", "Terrible experience", "Above expectations"};
    private static final String[] GENDERS = {"M", "F", "O"};
    private static final String[] MARITAL_STATUS = {"single", "married", "divorced", "widowed"};
    private static final String[] OCCUPATIONS = {"Engineer", "Teacher", "Doctor", "Lawyer", "Artist", "Manager", "Sales", "Accountant", "Designer", "Developer"};
    private static final String[] LOYALTY_TIERS = {"Bronze", "Silver", "Gold", "Platinum"};
    private static final String[] ACCOUNT_TYPES = {"personal", "business"};
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CNY"};
    private static final String[] LANGUAGES = {"en", "es", "fr", "de", "ja", "zh"};
    private static final String[] TIMEZONES = {"UTC", "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney"};

    public static void generateTestData(String dbUrl, int rowCount) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            conn.setAutoCommit(false);
            
            if (checkCurrentRowCount(conn, rowCount)) {
                System.out.println("Table already contains " + rowCount + " rows, skipping generation.");
                return;
            }

            clearAllTables(conn);
            System.out.println("Tables cleared, generating new test data...");
            System.out.println();

            generateCategories(conn);
            generateBrands(conn);
            generateUsers(conn, rowCount);
            generateProducts(conn, rowCount);
            generateInventory(conn, rowCount);
            generateOrders(conn, rowCount);
            generateOrderItems(conn, rowCount);
            generatePayments(conn, rowCount);
            generateShipping(conn, rowCount);
            generateReviews(conn, rowCount);
            
            conn.commit();
            System.out.println("\nTest data generated successfully.");
        }
    }

    private static boolean checkCurrentRowCount(Connection conn, int expectedRowCount) throws SQLException {
        String[] tables = {"users", "categories", "brands", "products", "inventory", 
                          "orders", "order_items", "payments", "shipping", "reviews"};
        
        for (String table : tables) {
            String sql = "SELECT COUNT(*) FROM " + table;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    int currentCount = rs.getInt(1);
                    if (currentCount != expectedRowCount) {
                        System.out.println(table + ": found " + currentCount + " rows, expected " + expectedRowCount);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void clearAllTables(Connection conn) throws SQLException {
        String[] tables = {"reviews", "shipping", "payments", "order_items", "orders", 
                          "inventory", "products", "brands", "categories", "users"};
        
        for (String table : tables) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM " + table);
            }
        }
    }

    private static void printProgress(String tableName, int current, int total) {
        if (current % 100000 == 0 || current == total) {
            int percent = (int) ((double) current / total * 100);
            System.out.println("  " + tableName + ": " + current + "/" + total + " (" + percent + "%)");
        }
    }

    private static void generateUsers(Connection conn, int count) throws SQLException {
        System.out.println("Generating users (" + count + " rows)...");
        String sql = "INSERT INTO users (username, email, password_hash, first_name, last_name, middle_name, " +
                     "phone, mobile_phone, work_phone, address_line1, address_line2, city, state_province, " +
                     "postal_code, country, date_of_birth, gender, marital_status, occupation, annual_income, " +
                     "currency_code, language_preference, timezone, is_verified, loyalty_points, loyalty_tier, " +
                     "referral_code, account_type, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                String username = firstName.toLowerCase() + lastName.toLowerCase() + i;
                pstmt.setString(1, username);
                pstmt.setString(2, username + "@example.com");
                pstmt.setString(3, "hashed_password_" + i);
                pstmt.setString(4, firstName);
                pstmt.setString(5, lastName);
                pstmt.setString(6, random.nextBoolean() ? firstName.substring(0, 1) + lastName : null);
                pstmt.setString(7, "+1-555-" + String.format("%07d", random.nextInt(10000000)));
                pstmt.setString(8, "+1-555-" + String.format("%07d", random.nextInt(10000000)));
                pstmt.setString(9, random.nextBoolean() ? "+1-555-" + String.format("%07d", random.nextInt(10000000)) : null);
                pstmt.setString(10, (random.nextInt(1000) + 1) + " Main St");
                pstmt.setString(11, random.nextBoolean() ? "Apt " + (random.nextInt(100) + 1) : null);
                pstmt.setString(12, CITIES[random.nextInt(CITIES.length)]);
                pstmt.setString(13, "State" + (random.nextInt(50) + 1));
                pstmt.setString(14, String.format("%05d", random.nextInt(100000)));
                pstmt.setString(15, COUNTRIES[random.nextInt(COUNTRIES.length)]);
                pstmt.setString(16, "19" + (random.nextInt(90) + 10) + "-" + String.format("%02d", random.nextInt(12) + 1) + "-" + String.format("%02d", random.nextInt(28) + 1));
                pstmt.setString(17, GENDERS[random.nextInt(GENDERS.length)]);
                pstmt.setString(18, MARITAL_STATUS[random.nextInt(MARITAL_STATUS.length)]);
                pstmt.setString(19, OCCUPATIONS[random.nextInt(OCCUPATIONS.length)]);
                pstmt.setBigDecimal(20, new BigDecimal(random.nextDouble() * 200000 + 30000).setScale(2, BigDecimal.ROUND_HALF_UP));
                pstmt.setString(21, CURRENCIES[random.nextInt(CURRENCIES.length)]);
                pstmt.setString(22, LANGUAGES[random.nextInt(LANGUAGES.length)]);
                pstmt.setString(23, TIMEZONES[random.nextInt(TIMEZONES.length)]);
                pstmt.setBoolean(24, random.nextBoolean());
                pstmt.setInt(25, random.nextInt(10000));
                pstmt.setString(26, LOYALTY_TIERS[random.nextInt(LOYALTY_TIERS.length)]);
                pstmt.setString(27, "REF-" + username.toUpperCase());
                pstmt.setString(28, ACCOUNT_TYPES[random.nextInt(ACCOUNT_TYPES.length)]);
                pstmt.setBoolean(29, true);
                pstmt.addBatch();
                
                if ((i + 1) % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("users", i + 1, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("users", count, count);
        }
    }

    private static void generateCategories(Connection conn) throws SQLException {
        System.out.println("Generating categories (10 rows)...");
        String sql = "INSERT INTO categories (category_name, category_slug, parent_category_id, description, is_visible, is_featured) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < 10; i++) {
                pstmt.setString(1, CATEGORY_NAMES[i]);
                pstmt.setString(2, CATEGORY_NAMES[i].toLowerCase().replace(" ", "-"));
                pstmt.setNull(3, Types.INTEGER);
                pstmt.setString(4, "Category for " + CATEGORY_NAMES[i] + " products");
                pstmt.setBoolean(5, true);
                pstmt.setBoolean(6, random.nextBoolean());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
        System.out.println("  categories: 10/10 (100%)");
    }

    private static void generateBrands(Connection conn) throws SQLException {
        System.out.println("Generating brands (10 rows)...");
        String sql = "INSERT INTO brands (brand_name, brand_slug, logo_url, description, founded_year, is_visible, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < 10; i++) {
                pstmt.setString(1, BRAND_NAMES[i]);
                pstmt.setString(2, BRAND_NAMES[i].toLowerCase().replace(" ", "-"));
                pstmt.setString(3, "https://example.com/logos/" + BRAND_NAMES[i].toLowerCase() + ".png");
                pstmt.setString(4, BRAND_NAMES[i] + " has been a trusted brand for years.");
                pstmt.setInt(5, 1900 + random.nextInt(100));
                pstmt.setBoolean(6, true);
                pstmt.setBoolean(7, random.nextBoolean());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
        System.out.println("  brands: 10/10 (100%)");
    }

    private static void generateProducts(Connection conn, int count) throws SQLException {
        System.out.println("Generating products (" + count + " rows)...");
        String sql = "INSERT INTO products (product_name, product_slug, sku, description, short_description, price, " +
                     "cost_price, sale_price, category_id, brand_id, is_active, is_visible, is_featured, is_new, " +
                     "stock_quantity, stock_status, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                String productName = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)] + " " + 
                                   PRODUCT_SUFFIXES[random.nextInt(PRODUCT_SUFFIXES.length)] + " " + i;
                pstmt.setString(1, productName);
                pstmt.setString(2, productName.toLowerCase().replace(" ", "-"));
                pstmt.setString(3, "SKU-" + String.format("%08d", i));
                pstmt.setString(4, "High quality product with excellent features and superior performance.");
                pstmt.setString(5, "Premium " + PRODUCT_SUFFIXES[random.nextInt(PRODUCT_SUFFIXES.length)] + " for everyday use.");
                BigDecimal price = new BigDecimal(random.nextDouble() * 1000 + 1).setScale(2, BigDecimal.ROUND_HALF_UP);
                pstmt.setBigDecimal(6, price);
                pstmt.setBigDecimal(7, price.multiply(new BigDecimal("0.7")).setScale(2, BigDecimal.ROUND_HALF_UP));
                pstmt.setBigDecimal(8, random.nextBoolean() ? price.multiply(new BigDecimal("0.85")).setScale(2, BigDecimal.ROUND_HALF_UP) : null);
                pstmt.setInt(9, 1 + random.nextInt(10));
                pstmt.setInt(10, 1 + random.nextInt(10));
                pstmt.setBoolean(11, true);
                pstmt.setBoolean(12, true);
                pstmt.setBoolean(13, random.nextBoolean());
                pstmt.setBoolean(14, random.nextBoolean());
                pstmt.setInt(15, random.nextInt(1000) + 1);
                pstmt.setString(16, "in_stock");
                pstmt.setString(17, "https://example.com/images/product" + i + ".jpg");
                pstmt.addBatch();
                
                if ((i + 1) % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("products", i + 1, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("products", count, count);
        }
    }

    private static void generateInventory(Connection conn, int count) throws SQLException {
        System.out.println("Generating inventory (" + count + " rows)...");
        String sql = "INSERT INTO inventory (product_id, warehouse_location, warehouse_zone, warehouse_aisle, " +
                     "quantity, available_quantity, reserved_quantity, reorder_threshold, reorder_quantity, " +
                     "status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "Warehouse " + (char)('A' + random.nextInt(5)));
                pstmt.setString(3, "Zone " + (char)('A' + random.nextInt(3)));
                pstmt.setString(4, String.format("%02d-%02d", random.nextInt(10) + 1, random.nextInt(10) + 1));
                int qty = random.nextInt(1000) + 1;
                pstmt.setInt(5, qty);
                int availableQty = qty - random.nextInt(Math.max(1, qty / 2));
                pstmt.setInt(6, Math.max(0, availableQty));
                pstmt.setInt(7, random.nextInt(Math.max(1, qty / 4)));
                pstmt.setInt(8, 10 + random.nextInt(50));
                pstmt.setInt(9, 50 + random.nextInt(100));
                pstmt.setString(10, "active");
                pstmt.addBatch();
                
                if (i % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("inventory", i, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("inventory", count, count);
        }
    }

    private static void generateOrders(Connection conn, int count) throws SQLException {
        System.out.println("Generating orders (" + count + " rows)...");
        String sql = "INSERT INTO orders (order_number, user_id, customer_email, total_amount, subtotal, " +
                     "tax_amount, shipping_amount, grand_total, base_total_amount, order_status, fulfillment_status, " +
                     "payment_status, shipping_address_line1, shipping_city, shipping_country, " +
                     "billing_address_line1, billing_city, billing_country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                pstmt.setString(1, "ORD-" + String.format("%08d", i));
                pstmt.setInt(2, 1 + random.nextInt(Math.max(1, count / 5)));
                pstmt.setString(3, "customer" + i + "@example.com");
                BigDecimal subtotal = new BigDecimal(random.nextDouble() * 5000 + 1).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal shipping = new BigDecimal(random.nextDouble() * 50 + 5).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal total = subtotal.add(tax).add(shipping);
                pstmt.setBigDecimal(4, total);
                pstmt.setBigDecimal(5, subtotal);
                pstmt.setBigDecimal(6, tax);
                pstmt.setBigDecimal(7, shipping);
                pstmt.setBigDecimal(8, total);
                pstmt.setBigDecimal(9, total);
                pstmt.setString(10, ORDER_STATUSES[random.nextInt(ORDER_STATUSES.length)]);
                pstmt.setString(11, random.nextBoolean() ? "fulfilled" : "unfulfilled");
                pstmt.setString(12, PAYMENT_STATUSES[random.nextInt(PAYMENT_STATUSES.length)]);
                pstmt.setString(13, (random.nextInt(1000) + 1) + " Shipping St");
                pstmt.setString(14, CITIES[random.nextInt(CITIES.length)]);
                pstmt.setString(15, COUNTRIES[random.nextInt(COUNTRIES.length)]);
                pstmt.setString(16, (random.nextInt(1000) + 1) + " Billing Ave");
                pstmt.setString(17, CITIES[random.nextInt(CITIES.length)]);
                pstmt.setString(18, COUNTRIES[random.nextInt(COUNTRIES.length)]);
                pstmt.addBatch();
                
                if ((i + 1) % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("orders", i + 1, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("orders", count, count);
        }
    }

    private static void generateOrderItems(Connection conn, int count) throws SQLException {
        System.out.println("Generating order_items (" + count + " rows)...");
        String sql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, " +
                     "subtotal, total, fulfillment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                int orderId = 1 + random.nextInt(Math.max(1, count / 2));
                int productId = 1 + random.nextInt(Math.max(1, count / 4));
                int quantity = 1 + random.nextInt(5);
                BigDecimal unitPrice = new BigDecimal(random.nextDouble() * 1000 + 1).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, productId);
                pstmt.setString(3, "Product " + productId);
                pstmt.setInt(4, quantity);
                pstmt.setBigDecimal(5, unitPrice);
                pstmt.setBigDecimal(6, subtotal);
                pstmt.setBigDecimal(7, subtotal);
                pstmt.setString(8, random.nextBoolean() ? "fulfilled" : "unfulfilled");
                pstmt.addBatch();
                
                if ((i + 1) % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("order_items", i + 1, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("order_items", count, count);
        }
    }

    private static void generatePayments(Connection conn, int count) throws SQLException {
        System.out.println("Generating payments (" + count + " rows)...");
        String sql = "INSERT INTO payments (order_id, transaction_id, payment_method, payment_processor, " +
                     "amount, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "TXN-" + System.currentTimeMillis() + "-" + i);
                pstmt.setString(3, PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)]);
                pstmt.setString(4, "Stripe");
                pstmt.setBigDecimal(5, new BigDecimal(random.nextDouble() * 5000 + 1).setScale(2, BigDecimal.ROUND_HALF_UP));
                pstmt.setString(6, PAYMENT_STATUSES[random.nextInt(PAYMENT_STATUSES.length)]);
                pstmt.addBatch();
                
                if (i % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("payments", i, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("payments", count, count);
        }
    }

    private static void generateShipping(Connection conn, int count) throws SQLException {
        System.out.println("Generating shipping (" + count + " rows)...");
        String sql = "INSERT INTO shipping (order_id, shipment_number, carrier, service_name, " +
                     "tracking_number, status, shipping_date, estimated_delivery_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "SHIP-" + String.format("%08d", i));
                pstmt.setString(3, SHIPPING_CARRIERS[random.nextInt(SHIPPING_CARRIERS.length)]);
                pstmt.setString(4, "Standard");
                pstmt.setString(5, "TRK-" + Math.abs(random.nextLong()));
                pstmt.setString(6, SHIPPING_STATUSES[random.nextInt(SHIPPING_STATUSES.length)]);
                pstmt.setTimestamp(7, new Timestamp(System.currentTimeMillis() - random.nextInt(86400000 * 7)));
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis() + random.nextInt(86400000 * 14)));
                pstmt.addBatch();
                
                if (i % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("shipping", i, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("shipping", count, count);
        }
    }

    private static void generateReviews(Connection conn, int count) throws SQLException {
        System.out.println("Generating reviews (" + count + " rows)...");
        String sql = "INSERT INTO reviews (product_id, user_id, rating, review_text, is_verified_purchase, " +
                     "is_approved, helpful_count, recommended) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                pstmt.setInt(1, 1 + random.nextInt(Math.max(1, count)));
                pstmt.setInt(2, 1 + random.nextInt(Math.max(1, count / 3)));
                pstmt.setInt(3, 1 + random.nextInt(5));
                pstmt.setString(4, REVIEW_TEXTS[random.nextInt(REVIEW_TEXTS.length)]);
                pstmt.setBoolean(5, random.nextBoolean());
                pstmt.setBoolean(6, random.nextBoolean());
                pstmt.setInt(7, random.nextInt(100));
                pstmt.setBoolean(8, random.nextBoolean());
                pstmt.addBatch();
                
                if ((i + 1) % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    printProgress("reviews", i + 1, count);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            printProgress("reviews", count, count);
        }
    }
}
