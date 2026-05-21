import sqlite3
import random
import os

DB_PATH = 'test.db'
SCHEMA_PATH = 'schema.sql'

FIRST_NAMES = ["Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Henry", "Ivy", "Jack", "Olivia", "Noah", "Sophia", "Liam", "Mia", "Ethan", "Ava", "Oliver", "Isabella", "William"]
LAST_NAMES = ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson"]
CITIES = ["New York", "London", "Tokyo", "Paris", "Berlin", "Sydney", "Beijing", "Moscow", "Dubai", "Singapore", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia"]
COUNTRIES = ["USA", "UK", "Japan", "France", "Germany", "Australia", "China", "Russia", "UAE", "Singapore", "Canada", "Brazil", "India", "Italy", "Spain"]
CATEGORY_NAMES = ["Electronics", "Clothing", "Home", "Sports", "Books", "Toys", "Beauty", "Grocery", "Auto", "Garden"]
BRAND_NAMES = ["Apple", "Samsung", "Nike", "Sony", "Microsoft", "Adidas", "LG", "Coca-Cola", "Toyota", "IKEA", "Amazon", "Google", "Meta", "Tesla", "Netflix"]
PRODUCT_PREFIXES = ["Premium", "Deluxe", "Super", "Ultra", "Mega", "Pro", "Elite", "Classic", "Modern", "Basic", "Advanced", "Essential", "Ultimate", "Professional", "Compact"]
PRODUCT_SUFFIXES = ["Widget", "Gadget", "Device", "Kit", "Set", "System", "Package", "Bundle", "Essential", "Collection", "Solution", "Tool", "Appliance", "Accessory", "Component"]
ORDER_STATUSES = ["pending", "processing", "shipped", "delivered", "cancelled"]
PAYMENT_METHODS = ["credit_card", "paypal", "bank_transfer", "digital_wallet"]
PAYMENT_STATUSES = ["pending", "completed", "failed", "refunded"]
SHIPPING_CARRIERS = ["FedEx", "UPS", "DHL", "USPS", "Amazon Logistics"]
SHIPPING_STATUSES = ["preparing", "in_transit", "out_for_delivery", "delivered"]
REVIEW_TEXTS = ["Great product!", "Very satisfied", "Could be better", "Highly recommend", "Not worth the price", "Excellent quality", "Fast shipping", "Worst purchase ever", "Perfect", "Just okay", "Amazing!", "Disappointing", "Will buy again", "Terrible experience", "Above expectations"]
GENDERS = ["M", "F", "O"]
MARITAL_STATUS = ["single", "married", "divorced", "widowed"]
OCCUPATIONS = ["Engineer", "Teacher", "Doctor", "Lawyer", "Artist", "Manager", "Sales", "Accountant", "Designer", "Developer"]
LOYALTY_TIERS = ["Bronze", "Silver", "Gold", "Platinum"]
ACCOUNT_TYPES = ["personal", "business"]
CURRENCIES = ["USD", "EUR", "GBP", "JPY", "CNY"]
LANGUAGES = ["en", "es", "fr", "de", "ja", "zh"]
TIMEZONES = ["UTC", "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney"]

def create_database():
    if os.path.exists(DB_PATH):
        try:
            os.remove(DB_PATH)
        except:
            pass
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    with open(SCHEMA_PATH, 'r', encoding='utf-8') as f:
        schema_sql = f.read()
    
    for statement in schema_sql.split(';'):
        statement = statement.strip()
        if statement:
            cursor.execute(statement)
    
    conn.commit()
    conn.close()
    print("Database schema created successfully")

def generate_test_data(row_count=100):
    conn = sqlite3.connect(DB_PATH)
    conn.execute('PRAGMA foreign_keys = ON')
    
    generate_categories(conn)
    generate_brands(conn)
    generate_users(conn, row_count)
    generate_products(conn, row_count)
    generate_inventory(conn, row_count)
    generate_orders(conn, row_count)
    generate_order_items(conn, row_count)
    generate_payments(conn, row_count)
    generate_shipping(conn, row_count)
    generate_reviews(conn, row_count)
    
    conn.commit()
    conn.close()
    print(f"Test data generated for {row_count} rows")

def generate_categories(conn):
    cursor = conn.cursor()
    for i, name in enumerate(CATEGORY_NAMES):
        cursor.execute('''
            INSERT INTO categories (category_name, category_slug, is_visible, is_featured)
            VALUES (?, ?, ?, ?)
        ''', (name, name.lower().replace(' ', '-'), 1, random.choice([0, 1])))
    print(f"Added {len(CATEGORY_NAMES)} categories")

def generate_brands(conn):
    cursor = conn.cursor()
    for i, name in enumerate(BRAND_NAMES):
        cursor.execute('''
            INSERT INTO brands (brand_name, brand_slug, is_visible, is_featured)
            VALUES (?, ?, ?, ?)
        ''', (name, name.lower().replace(' ', '-'), 1, random.choice([0, 1])))
    print(f"Added {len(BRAND_NAMES)} brands")

def generate_users(conn, count):
    cursor = conn.cursor()
    for i in range(count):
        first_name = random.choice(FIRST_NAMES)
        last_name = random.choice(LAST_NAMES)
        username = f"{first_name.lower()}{last_name.lower()}{i}"
        cursor.execute('''
            INSERT INTO users (username, email, password_hash, first_name, last_name, city, country, 
                             gender, marital_status, occupation, loyalty_tier, account_type, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (username, f"{username}@example.com", f"hashed_password_{i}", first_name, last_name,
              random.choice(CITIES), random.choice(COUNTRIES), random.choice(GENDERS),
              random.choice(MARITAL_STATUS), random.choice(OCCUPATIONS),
              random.choice(LOYALTY_TIERS), random.choice(ACCOUNT_TYPES), 1))
    print(f"Added {count} users")

def generate_products(conn, count):
    cursor = conn.cursor()
    for i in range(count):
        product_name = f"{random.choice(PRODUCT_PREFIXES)} {random.choice(PRODUCT_SUFFIXES)} {i}"
        price = round(random.uniform(1, 1000), 2)
        cursor.execute('''
            INSERT INTO products (product_name, product_slug, sku, description, price, cost_price, 
                                 category_id, brand_id, is_active, is_visible, stock_quantity)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (product_name, product_name.lower().replace(' ', '-'), f"SKU-{i:08d}",
              "High quality product", price, round(price * 0.7, 2),
              random.randint(1, 10), random.randint(1, 10), 1, 1, random.randint(1, 1000)))
    print(f"Added {count} products")

def generate_inventory(conn, count):
    cursor = conn.cursor()
    for i in range(1, count + 1):
        qty = random.randint(1, 1000)
        cursor.execute('''
            INSERT INTO inventory (product_id, warehouse_location, quantity, available_quantity, status)
            VALUES (?, ?, ?, ?, ?)
        ''', (i, f"Warehouse {chr(ord('A') + random.randint(0, 4))}", qty, qty - random.randint(0, qty // 2), "active"))
    print(f"Added {count} inventory records")

def generate_orders(conn, count):
    cursor = conn.cursor()
    for i in range(count):
        subtotal = round(random.uniform(1, 5000), 2)
        tax = round(subtotal * 0.08, 2)
        shipping = round(random.uniform(5, 50), 2)
        total = round(subtotal + tax + shipping, 2)
        cursor.execute('''
            INSERT INTO orders (order_number, user_id, customer_email, total_amount, subtotal, 
                               tax_amount, shipping_amount, grand_total, base_total_amount, 
                               order_status, fulfillment_status, payment_status, 
                               shipping_city, shipping_country, billing_city, billing_country)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (f"ORD-{i:08d}", random.randint(1, min(count, 50)), f"customer{i}@example.com",
              total, subtotal, tax, shipping, total, total,
              random.choice(ORDER_STATUSES), random.choice(['fulfilled', 'unfulfilled']),
              random.choice(PAYMENT_STATUSES), random.choice(CITIES), random.choice(COUNTRIES),
              random.choice(CITIES), random.choice(COUNTRIES)))
    print(f"Added {count} orders")

def generate_order_items(conn, count):
    cursor = conn.cursor()
    for i in range(count):
        order_id = random.randint(1, min(count, count // 2))
        product_id = random.randint(1, min(count, count // 4))
        quantity = random.randint(1, 5)
        unit_price = round(random.uniform(1, 1000), 2)
        subtotal = round(unit_price * quantity, 2)
        cursor.execute('''
            INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, 
                                     subtotal, total, fulfillment_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', (order_id, product_id, f"Product {product_id}", quantity, unit_price,
              subtotal, subtotal, random.choice(['fulfilled', 'unfulfilled'])))
    print(f"Added {count} order_items")

def generate_payments(conn, count):
    cursor = conn.cursor()
    for i in range(1, count + 1):
        cursor.execute('''
            INSERT INTO payments (order_id, transaction_id, payment_method, payment_processor, amount, status)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (i, f"TXN-{i}", random.choice(PAYMENT_METHODS), "Stripe",
              round(random.uniform(1, 5000), 2), random.choice(PAYMENT_STATUSES)))
    print(f"Added {count} payments")

def generate_shipping(conn, count):
    cursor = conn.cursor()
    for i in range(1, count + 1):
        cursor.execute('''
            INSERT INTO shipping (order_id, shipment_number, carrier, service_name, tracking_number, status)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (i, f"SHIP-{i:08d}", random.choice(SHIPPING_CARRIERS), "Standard",
              f"TRK-{abs(random.randint(1000000, 9999999))}", random.choice(SHIPPING_STATUSES)))
    print(f"Added {count} shipping records")

def generate_reviews(conn, count):
    cursor = conn.cursor()
    for i in range(count):
        cursor.execute('''
            INSERT INTO reviews (product_id, user_id, rating, review_text, is_verified_purchase, is_approved, helpful_count, recommended)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', (random.randint(1, min(count, count)), random.randint(1, min(count, count // 3)),
              random.randint(1, 5), random.choice(REVIEW_TEXTS), random.choice([0, 1]),
              random.choice([0, 1]), random.randint(0, 100), random.choice([0, 1])))
    print(f"Added {count} reviews")

if __name__ == '__main__':
    create_database()
    generate_test_data(100)