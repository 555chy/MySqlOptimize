package com.sqlparse.testdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EcommerceSchemaGenerator {

    public static List<String> getCreateTableStatements() {
        List<String> statements = new ArrayList<>();

        statements.add(
            "CREATE TABLE IF NOT EXISTS users (" +
            "    user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    username VARCHAR(50) NOT NULL UNIQUE," +
            "    email VARCHAR(100) NOT NULL UNIQUE," +
            "    password_hash VARCHAR(255) NOT NULL," +
            "    first_name VARCHAR(50)," +
            "    last_name VARCHAR(50)," +
            "    middle_name VARCHAR(50)," +
            "    phone VARCHAR(20)," +
            "    mobile_phone VARCHAR(20)," +
            "    work_phone VARCHAR(20)," +
            "    address_line1 TEXT," +
            "    address_line2 TEXT," +
            "    city VARCHAR(50)," +
            "    state_province VARCHAR(50)," +
            "    postal_code VARCHAR(20)," +
            "    country VARCHAR(50)," +
            "    date_of_birth DATE," +
            "    gender CHAR(1)," +
            "    marital_status VARCHAR(20)," +
            "    occupation VARCHAR(100)," +
            "    annual_income DECIMAL(15,2)," +
            "    currency_code CHAR(3) DEFAULT 'USD'," +
            "    language_preference VARCHAR(10) DEFAULT 'en'," +
            "    timezone VARCHAR(50) DEFAULT 'UTC'," +
            "    is_verified BOOLEAN DEFAULT 0," +
            "    verification_date TIMESTAMP," +
            "    verification_method VARCHAR(50)," +
            "    last_login_at TIMESTAMP," +
            "    last_login_ip VARCHAR(45)," +
            "    login_attempts INTEGER DEFAULT 0," +
            "    account_locked BOOLEAN DEFAULT 0," +
            "    account_locked_until TIMESTAMP," +
            "    password_changed_at TIMESTAMP," +
            "    password_expiry_date TIMESTAMP," +
            "    two_factor_enabled BOOLEAN DEFAULT 0," +
            "    two_factor_method VARCHAR(20)," +
            "    newsletter_subscribed BOOLEAN DEFAULT 1," +
            "    marketing_opt_in BOOLEAN DEFAULT 0," +
            "    sms_notifications BOOLEAN DEFAULT 0," +
            "    push_notifications BOOLEAN DEFAULT 1," +
            "    email_notifications BOOLEAN DEFAULT 1," +
            "    preferred_shipping_carrier VARCHAR(50)," +
            "    preferred_payment_method VARCHAR(50)," +
            "    loyalty_points INTEGER DEFAULT 0," +
            "    loyalty_tier VARCHAR(20) DEFAULT 'Bronze'," +
            "    referral_code VARCHAR(50) UNIQUE," +
            "    referred_by_user_id INTEGER," +
            "    avatar_url VARCHAR(255)," +
            "    bio TEXT," +
            "    website_url VARCHAR(255)," +
            "    social_facebook VARCHAR(100)," +
            "    social_twitter VARCHAR(100)," +
            "    social_linkedin VARCHAR(100)," +
            "    social_instagram VARCHAR(100)," +
            "    tax_id VARCHAR(50)," +
            "    tax_identification_type VARCHAR(20)," +
            "    company_name VARCHAR(100)," +
            "    company_registration_number VARCHAR(50)," +
            "    is_business_account BOOLEAN DEFAULT 0," +
            "    account_type VARCHAR(20) DEFAULT 'personal'," +
            "    customer_segment VARCHAR(30)," +
            "    customer_lifetime_value DECIMAL(15,2) DEFAULT 0," +
            "    last_purchase_date TIMESTAMP," +
            "    total_purchases INTEGER DEFAULT 0," +
            "    total_spent DECIMAL(15,2) DEFAULT 0," +
            "    average_order_value DECIMAL(10,2)," +
            "    favorite_category_id INTEGER," +
            "    favorite_brand_id INTEGER," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    deleted_at TIMESTAMP," +
            "    is_active BOOLEAN DEFAULT 1," +
            "    notes TEXT," +
            "    admin_notes TEXT," +
            "    FOREIGN KEY (referred_by_user_id) REFERENCES users(user_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS categories (" +
            "    category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    category_name VARCHAR(100) NOT NULL," +
            "    category_slug VARCHAR(100) UNIQUE," +
            "    parent_category_id INTEGER," +
            "    description TEXT," +
            "    short_description TEXT," +
            "    meta_title VARCHAR(200)," +
            "    meta_description TEXT," +
            "    meta_keywords TEXT," +
            "    category_image_url VARCHAR(255)," +
            "    thumbnail_url VARCHAR(255)," +
            "    banner_url VARCHAR(255)," +
            "    display_order INTEGER DEFAULT 0," +
            "    is_visible BOOLEAN DEFAULT 1," +
            "    is_featured BOOLEAN DEFAULT 0," +
            "    show_in_navigation BOOLEAN DEFAULT 1," +
            "    searchable BOOLEAN DEFAULT 1," +
            "    page_title VARCHAR(200)," +
            "    url_path VARCHAR(255)," +
            "    view_count INTEGER DEFAULT 0," +
            "    product_count INTEGER DEFAULT 0," +
            "    template VARCHAR(50)," +
            "    layout VARCHAR(50)," +
            "    color_code VARCHAR(20)," +
            "    icon_class VARCHAR(50)," +
            "    custom_attributes TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    created_by INTEGER," +
            "    updated_by INTEGER," +
            "    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS brands (" +
            "    brand_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    brand_name VARCHAR(100) NOT NULL UNIQUE," +
            "    brand_slug VARCHAR(100) UNIQUE," +
            "    logo_url VARCHAR(255)," +
            "    logo_svg VARCHAR(255)," +
            "    banner_url VARCHAR(255)," +
            "    thumbnail_url VARCHAR(255)," +
            "    description TEXT," +
            "    short_description TEXT," +
            "    founded_year INTEGER," +
            "    founded_country VARCHAR(50)," +
            "    headquarters_location VARCHAR(100)," +
            "    website_url VARCHAR(255)," +
            "    contact_email VARCHAR(100)," +
            "    contact_phone VARCHAR(20)," +
            "    support_phone VARCHAR(20)," +
            "    support_email VARCHAR(100)," +
            "    social_facebook VARCHAR(100)," +
            "    social_twitter VARCHAR(100)," +
            "    social_instagram VARCHAR(100)," +
            "    social_youtube VARCHAR(100)," +
            "    social_linkedin VARCHAR(100)," +
            "    meta_title VARCHAR(200)," +
            "    meta_description TEXT," +
            "    meta_keywords TEXT," +
            "    brand_story TEXT," +
            "    mission_statement TEXT," +
            "    vision_statement TEXT," +
            "    warranty_info TEXT," +
            "    return_policy TEXT," +
            "    shipping_policy TEXT," +
            "    is_visible BOOLEAN DEFAULT 1," +
            "    is_featured BOOLEAN DEFAULT 0," +
            "    is_premium BOOLEAN DEFAULT 0," +
            "    display_order INTEGER DEFAULT 0," +
            "    view_count INTEGER DEFAULT 0," +
            "    product_count INTEGER DEFAULT 0," +
            "    average_rating DECIMAL(3,2)," +
            "    total_reviews INTEGER DEFAULT 0," +
            "    rating_distribution TEXT," +
            "    country_of_origin VARCHAR(50)," +
            "    primary_category_id INTEGER," +
            "    parent_brand_id INTEGER," +
            "    custom_attributes TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    created_by INTEGER," +
            "    updated_by INTEGER," +
            "    FOREIGN KEY (primary_category_id) REFERENCES categories(category_id)," +
            "    FOREIGN KEY (parent_brand_id) REFERENCES brands(brand_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS products (" +
            "    product_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    product_name VARCHAR(200) NOT NULL," +
            "    product_slug VARCHAR(200) UNIQUE," +
            "    sku VARCHAR(50) UNIQUE," +
            "    upc VARCHAR(20)," +
            "    ean VARCHAR(20)," +
            "    jan VARCHAR(20)," +
            "    isbn VARCHAR(20)," +
            "    mpn VARCHAR(50)," +
            "    description TEXT," +
            "    short_description TEXT," +
            "    long_description TEXT," +
            "    meta_title VARCHAR(200)," +
            "    meta_description TEXT," +
            "    meta_keywords TEXT," +
            "    price DECIMAL(10,2) NOT NULL," +
            "    cost_price DECIMAL(10,2)," +
            "    sale_price DECIMAL(10,2)," +
            "    sale_price_start_date TIMESTAMP," +
            "    sale_price_end_date TIMESTAMP," +
            "    msrp DECIMAL(10,2)," +
            "    currency_code CHAR(3) DEFAULT 'USD'," +
            "    price_display VARCHAR(20) DEFAULT 'tax_exclusive'," +
            "    tax_class VARCHAR(50)," +
            "    tax_rate DECIMAL(5,4)," +
            "    weight DECIMAL(10,3)," +
            "    weight_unit VARCHAR(10) DEFAULT 'kg'," +
            "    length DECIMAL(10,3)," +
            "    width DECIMAL(10,3)," +
            "    height DECIMAL(10,3)," +
            "    dimension_unit VARCHAR(10) DEFAULT 'cm'," +
            "    volume DECIMAL(10,3)," +
            "    volume_unit VARCHAR(10) DEFAULT 'l'," +
            "    category_id INTEGER," +
            "    brand_id INTEGER," +
            "    vendor_id INTEGER," +
            "    vendor_sku VARCHAR(50)," +
            "    vendor_cost DECIMAL(10,2)," +
            "    is_active BOOLEAN DEFAULT 1," +
            "    is_visible BOOLEAN DEFAULT 1," +
            "    is_featured BOOLEAN DEFAULT 0," +
            "    is_new BOOLEAN DEFAULT 0," +
            "    is_on_sale BOOLEAN DEFAULT 0," +
            "    is_backorder BOOLEAN DEFAULT 0," +
            "    is_preorder BOOLEAN DEFAULT 0," +
            "    preorder_available_date TIMESTAMP," +
            "    requires_shipping BOOLEAN DEFAULT 1," +
            "    is_virtual BOOLEAN DEFAULT 0," +
            "    is_downloadable BOOLEAN DEFAULT 0," +
            "    downloadable_file_url VARCHAR(255)," +
            "    max_downloads INTEGER," +
            "    download_expiry_days INTEGER," +
            "    has_options BOOLEAN DEFAULT 0," +
            "    option_set_id INTEGER," +
            "    stock_quantity INTEGER DEFAULT 0," +
            "    stock_availability VARCHAR(20) DEFAULT 'in_stock'," +
            "    out_of_stock_action VARCHAR(20) DEFAULT 'hide'," +
            "    allow_backorder BOOLEAN DEFAULT 0," +
            "    backorder_quantity INTEGER DEFAULT 0," +
            "    min_purchase_quantity INTEGER DEFAULT 1," +
            "    max_purchase_quantity INTEGER," +
            "    quantity_increment INTEGER DEFAULT 1," +
            "    notify_qty_below INTEGER," +
            "    notify_stock_owner BOOLEAN DEFAULT 0," +
            "    manage_stock BOOLEAN DEFAULT 1," +
            "    stock_status VARCHAR(20) DEFAULT 'in_stock'," +
            "    available_date TIMESTAMP," +
            "    availability_description TEXT," +
            "    image_url VARCHAR(255)," +
            "    thumbnail_url VARCHAR(255)," +
            "    small_image_url VARCHAR(255)," +
            "    gallery_image_urls TEXT," +
            "    video_url VARCHAR(255)," +
            "    warranty TEXT," +
            "    return_policy TEXT," +
            "    shipping_policy TEXT," +
            "    search_keywords TEXT," +
            "    tags TEXT," +
            "    search_weight INTEGER DEFAULT 0," +
            "    view_count INTEGER DEFAULT 0," +
            "    wishlist_count INTEGER DEFAULT 0," +
            "    cart_count INTEGER DEFAULT 0," +
            "    order_count INTEGER DEFAULT 0," +
            "    sold_quantity INTEGER DEFAULT 0," +
            "    revenue_total DECIMAL(15,2) DEFAULT 0," +
            "    average_rating DECIMAL(3,2)," +
            "    review_count INTEGER DEFAULT 0," +
            "    rating_distribution TEXT," +
            "    last_review_date TIMESTAMP," +
            "    show_reviews BOOLEAN DEFAULT 1," +
            "    show_questions BOOLEAN DEFAULT 1," +
            "    show_prices BOOLEAN DEFAULT 1," +
            "    allow_add_to_cart BOOLEAN DEFAULT 1," +
            "    allow_wishlist BOOLEAN DEFAULT 1," +
            "    allow_compare BOOLEAN DEFAULT 1," +
            "    template VARCHAR(50)," +
            "    layout VARCHAR(50)," +
            "    color_code VARCHAR(20)," +
            "    custom_attributes TEXT," +
            "    custom_options TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    published_at TIMESTAMP," +
            "    created_by INTEGER," +
            "    updated_by INTEGER," +
            "    FOREIGN KEY (category_id) REFERENCES categories(category_id)," +
            "    FOREIGN KEY (brand_id) REFERENCES brands(brand_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS inventory (" +
            "    inventory_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    product_id INTEGER NOT NULL," +
            "    warehouse_id INTEGER," +
            "    warehouse_location VARCHAR(100)," +
            "    warehouse_zone VARCHAR(50)," +
            "    warehouse_aisle VARCHAR(20)," +
            "    warehouse_rack VARCHAR(20)," +
            "    warehouse_shelf VARCHAR(20)," +
            "    warehouse_bin VARCHAR(20)," +
            "    quantity INTEGER NOT NULL DEFAULT 0," +
            "    available_quantity INTEGER DEFAULT 0," +
            "    reserved_quantity INTEGER DEFAULT 0," +
            "    incoming_quantity INTEGER DEFAULT 0," +
            "    damaged_quantity INTEGER DEFAULT 0," +
            "    defective_quantity INTEGER DEFAULT 0," +
            "    returned_quantity INTEGER DEFAULT 0," +
            "    on_hold_quantity INTEGER DEFAULT 0," +
            "    backordered_quantity INTEGER DEFAULT 0," +
            "    minimum_quantity INTEGER DEFAULT 0," +
            "    maximum_quantity INTEGER," +
            "    reorder_threshold INTEGER DEFAULT 10," +
            "    reorder_quantity INTEGER DEFAULT 50," +
            "    reorder_point INTEGER DEFAULT 10," +
            "    economic_order_quantity INTEGER," +
            "    safety_stock INTEGER DEFAULT 0," +
            "    last_physical_count TIMESTAMP," +
            "    last_physical_count_quantity INTEGER," +
            "    next_physical_count_date TIMESTAMP," +
            "    last_received_date TIMESTAMP," +
            "    last_shipped_date TIMESTAMP," +
            "    last_adjustment_date TIMESTAMP," +
            "    last_adjustment_note TEXT," +
            "    last_adjustment_quantity INTEGER," +
            "    last_adjustment_type VARCHAR(20)," +
            "    inventory_value DECIMAL(15,2)," +
            "    cost_method VARCHAR(20) DEFAULT 'fifo'," +
            "    average_cost DECIMAL(10,2)," +
            "    last_cost DECIMAL(10,2)," +
            "    standard_cost DECIMAL(10,2)," +
            "    is_serialized BOOLEAN DEFAULT 0," +
            "    is_lot_tracked BOOLEAN DEFAULT 0," +
            "    is_expiration_tracked BOOLEAN DEFAULT 0," +
            "    lot_number VARCHAR(50)," +
            "    serial_number VARCHAR(50)," +
            "    expiration_date TIMESTAMP," +
            "    manufacture_date TIMESTAMP," +
            "    batch_number VARCHAR(50)," +
            "    supplier_batch_number VARCHAR(50)," +
            "    country_of_origin VARCHAR(50)," +
            "    state VARCHAR(20) DEFAULT 'available'," +
            "    status VARCHAR(20) DEFAULT 'active'," +
            "    notes TEXT," +
            "    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    last_updated_by INTEGER," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (product_id) REFERENCES products(product_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS orders (" +
            "    order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    order_number VARCHAR(50) UNIQUE NOT NULL," +
            "    user_id INTEGER NOT NULL," +
            "    customer_id INTEGER," +
            "    customer_email VARCHAR(100)," +
            "    customer_phone VARCHAR(20)," +
            "    customer_ip VARCHAR(45)," +
            "    customer_user_agent TEXT," +
            "    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    order_status VARCHAR(50) NOT NULL DEFAULT 'pending'," +
            "    status_updated_at TIMESTAMP," +
            "    status_updated_by INTEGER," +
            "    fulfillment_status VARCHAR(50) DEFAULT 'unfulfilled'," +
            "    fulfillment_updated_at TIMESTAMP," +
            "    payment_status VARCHAR(50) DEFAULT 'unpaid'," +
            "    payment_updated_at TIMESTAMP," +
            "    total_amount DECIMAL(10,2) NOT NULL," +
            "    subtotal DECIMAL(10,2) NOT NULL," +
            "    tax_amount DECIMAL(10,2) DEFAULT 0," +
            "    tax_rate DECIMAL(5,4)," +
            "    tax_name VARCHAR(50)," +
            "    tax_details TEXT," +
            "    shipping_amount DECIMAL(10,2) DEFAULT 0," +
            "    shipping_tax_amount DECIMAL(10,2) DEFAULT 0," +
            "    discount_amount DECIMAL(10,2) DEFAULT 0," +
            "    discount_details TEXT," +
            "    coupon_code VARCHAR(50)," +
            "    coupon_discount_amount DECIMAL(10,2) DEFAULT 0," +
            "    gift_card_amount DECIMAL(10,2) DEFAULT 0," +
            "    loyalty_points_used INTEGER DEFAULT 0," +
            "    loyalty_points_discount DECIMAL(10,2) DEFAULT 0," +
            "    grand_total DECIMAL(10,2) NOT NULL," +
            "    currency_code CHAR(3) DEFAULT 'USD'," +
            "    exchange_rate DECIMAL(10,6) DEFAULT 1," +
            "    base_currency_code CHAR(3) DEFAULT 'USD'," +
            "    base_total_amount DECIMAL(10,2) NOT NULL," +
            "    weight_total DECIMAL(10,3)," +
            "    weight_unit VARCHAR(10) DEFAULT 'kg'," +
            "    item_count INTEGER DEFAULT 0," +
            "    product_count INTEGER DEFAULT 0," +
            "    shipping_method_id INTEGER," +
            "    shipping_method_name VARCHAR(100)," +
            "    shipping_carrier VARCHAR(100)," +
            "    shipping_service VARCHAR(100)," +
            "    estimated_delivery_date TIMESTAMP," +
            "    tracking_number VARCHAR(100)," +
            "    tracking_url VARCHAR(255)," +
            "    shipping_address_line1 TEXT," +
            "    shipping_address_line2 TEXT," +
            "    shipping_city VARCHAR(50)," +
            "    shipping_state_province VARCHAR(50)," +
            "    shipping_postal_code VARCHAR(20)," +
            "    shipping_country VARCHAR(50)," +
            "    shipping_first_name VARCHAR(50)," +
            "    shipping_last_name VARCHAR(50)," +
            "    shipping_company VARCHAR(100)," +
            "    shipping_phone VARCHAR(20)," +
            "    shipping_email VARCHAR(100)," +
            "    shipping_instructions TEXT," +
            "    billing_address_line1 TEXT," +
            "    billing_address_line2 TEXT," +
            "    billing_city VARCHAR(50)," +
            "    billing_state_province VARCHAR(50)," +
            "    billing_postal_code VARCHAR(20)," +
            "    billing_country VARCHAR(50)," +
            "    billing_first_name VARCHAR(50)," +
            "    billing_last_name VARCHAR(50)," +
            "    billing_company VARCHAR(100)," +
            "    billing_phone VARCHAR(20)," +
            "    billing_email VARCHAR(100)," +
            "    tax_id VARCHAR(50)," +
            "    tax_identification_type VARCHAR(20)," +
            "    invoice_number VARCHAR(50) UNIQUE," +
            "    invoice_date TIMESTAMP," +
            "    invoice_sent_at TIMESTAMP," +
            "    packing_slip_printed_at TIMESTAMP," +
            "    packing_slip_print_count INTEGER DEFAULT 0," +
            "    order_notes TEXT," +
            "    customer_notes TEXT," +
            "    admin_notes TEXT," +
            "    staff_notes TEXT," +
            "    private_notes TEXT," +
            "    source VARCHAR(50) DEFAULT 'website'," +
            "    channel VARCHAR(50) DEFAULT 'online'," +
            "    affiliate_id INTEGER," +
            "    affiliate_commission DECIMAL(10,2) DEFAULT 0," +
            "    referred_by VARCHAR(100)," +
            "    utm_source VARCHAR(100)," +
            "    utm_medium VARCHAR(100)," +
            "    utm_campaign VARCHAR(100)," +
            "    utm_term VARCHAR(100)," +
            "    utm_content VARCHAR(100)," +
            "    landing_page_url VARCHAR(255)," +
            "    referrer_url VARCHAR(255)," +
            "    is_guest_order BOOLEAN DEFAULT 0," +
            "    is_manual_order BOOLEAN DEFAULT 0," +
            "    is_wholesale_order BOOLEAN DEFAULT 0," +
            "    is_gift BOOLEAN DEFAULT 0," +
            "    gift_message TEXT," +
            "    gift_wrap BOOLEAN DEFAULT 0," +
            "    gift_wrap_amount DECIMAL(10,2) DEFAULT 0," +
            "    loyalty_points_earned INTEGER DEFAULT 0," +
            "    loyalty_points_redeemable BOOLEAN DEFAULT 1," +
            "    customer_order_sequence INTEGER," +
            "    is_test_order BOOLEAN DEFAULT 0," +
            "    has_return BOOLEAN DEFAULT 0," +
            "    has_refund BOOLEAN DEFAULT 0," +
            "    has_exchange BOOLEAN DEFAULT 0," +
            "    refunded_amount DECIMAL(10,2) DEFAULT 0," +
            "    returned_item_count INTEGER DEFAULT 0," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    completed_at TIMESTAMP," +
            "    cancelled_at TIMESTAMP," +
            "    cancel_reason TEXT," +
            "    cancelled_by INTEGER," +
            "    created_by INTEGER," +
            "    updated_by INTEGER," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    order_id INTEGER NOT NULL," +
            "    product_id INTEGER NOT NULL," +
            "    variant_id INTEGER," +
            "    parent_item_id INTEGER," +
            "    product_name VARCHAR(200) NOT NULL," +
            "    product_sku VARCHAR(50)," +
            "    product_slug VARCHAR(200)," +
            "    product_image_url VARCHAR(255)," +
            "    product_type VARCHAR(50)," +
            "    vendor_name VARCHAR(100)," +
            "    brand_name VARCHAR(100)," +
            "    category_name VARCHAR(100)," +
            "    quantity INTEGER NOT NULL," +
            "    quantity_shipped INTEGER DEFAULT 0," +
            "    quantity_returned INTEGER DEFAULT 0," +
            "    quantity_cancelled INTEGER DEFAULT 0," +
            "    quantity_refunded INTEGER DEFAULT 0," +
            "    unit_price DECIMAL(10,2) NOT NULL," +
            "    original_unit_price DECIMAL(10,2)," +
            "    cost_price DECIMAL(10,2)," +
            "    subtotal DECIMAL(10,2) NOT NULL," +
            "    discount_amount DECIMAL(10,2) DEFAULT 0," +
            "    discount_details TEXT," +
            "    tax_amount DECIMAL(10,2) DEFAULT 0," +
            "    tax_rate DECIMAL(5,4)," +
            "    tax_name VARCHAR(50)," +
            "    tax_details TEXT," +
            "    shipping_amount DECIMAL(10,2) DEFAULT 0," +
            "    shipping_tax_amount DECIMAL(10,2) DEFAULT 0," +
            "    total DECIMAL(10,2) NOT NULL," +
            "    weight DECIMAL(10,3)," +
            "    weight_unit VARCHAR(10) DEFAULT 'kg'," +
            "    length DECIMAL(10,3)," +
            "    width DECIMAL(10,3)," +
            "    height DECIMAL(10,3)," +
            "    dimension_unit VARCHAR(10) DEFAULT 'cm'," +
            "    variant_title VARCHAR(255)," +
            "    variant_options TEXT," +
            "    customizations TEXT," +
            "    product_attributes TEXT," +
            "    is_gift BOOLEAN DEFAULT 0," +
            "    gift_message TEXT," +
            "    gift_wrap BOOLEAN DEFAULT 0," +
            "    is_backorder BOOLEAN DEFAULT 0," +
            "    is_preorder BOOLEAN DEFAULT 0," +
            "    backorder_date TIMESTAMP," +
            "    expected_ship_date TIMESTAMP," +
            "    estimated_arrival_date TIMESTAMP," +
            "    warehouse_id INTEGER," +
            "    warehouse_location VARCHAR(100)," +
            "    fulfillment_status VARCHAR(50) DEFAULT 'unfulfilled'," +
            "    fulfillment_service VARCHAR(100)," +
            "    fulfillment_location VARCHAR(100)," +
            "    tracking_number VARCHAR(100)," +
            "    tracking_url VARCHAR(255)," +
            "    ship_date TIMESTAMP," +
            "    carrier VARCHAR(100)," +
            "    shipping_service VARCHAR(100)," +
            "    return_status VARCHAR(50)," +
            "    return_date TIMESTAMP," +
            "    return_reason TEXT," +
            "    refund_amount DECIMAL(10,2) DEFAULT 0," +
            "    refund_date TIMESTAMP," +
            "    refund_reason TEXT," +
            "    exchange_status VARCHAR(50)," +
            "    exchange_order_item_id INTEGER," +
            "    line_item_notes TEXT," +
            "    admin_notes TEXT," +
            "    sort_order INTEGER DEFAULT 0," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
            "    FOREIGN KEY (product_id) REFERENCES products(product_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS payments (" +
            "    payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    order_id INTEGER NOT NULL," +
            "    user_id INTEGER," +
            "    customer_id INTEGER," +
            "    transaction_id VARCHAR(100) UNIQUE," +
            "    gateway_transaction_id VARCHAR(100) UNIQUE," +
            "    parent_transaction_id INTEGER," +
            "    payment_method VARCHAR(50) NOT NULL," +
            "    payment_method_type VARCHAR(50)," +
            "    payment_processor VARCHAR(50) NOT NULL," +
            "    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    amount DECIMAL(10,2) NOT NULL," +
            "    currency_code CHAR(3) DEFAULT 'USD'," +
            "    exchange_rate DECIMAL(10,6) DEFAULT 1," +
            "    status VARCHAR(50) NOT NULL," +
            "    failure_code VARCHAR(100)," +
            "    failure_message TEXT," +
            "    failure_reason TEXT," +
            "    gateway_response TEXT," +
            "    gateway_response_code VARCHAR(100)," +
            "    gateway_response_message TEXT," +
            "    avs_response VARCHAR(10)," +
            "    cvv_response VARCHAR(10)," +
            "    risk_score DECIMAL(5,2)," +
            "    risk_level VARCHAR(20)," +
            "    risk_assessment TEXT," +
            "    is_manual BOOLEAN DEFAULT 0," +
            "    is_offline BOOLEAN DEFAULT 0," +
            "    is_test BOOLEAN DEFAULT 0," +
            "    is_recurring BOOLEAN DEFAULT 0," +
            "    is_subscription BOOLEAN DEFAULT 0," +
            "    subscription_id INTEGER," +
            "    authorization_code VARCHAR(50)," +
            "    authorization_date TIMESTAMP," +
            "    authorization_expires_at TIMESTAMP," +
            "    captured_at TIMESTAMP," +
            "    captured_amount DECIMAL(10,2) DEFAULT 0," +
            "    refunded_at TIMESTAMP," +
            "    refunded_amount DECIMAL(10,2) DEFAULT 0," +
            "    voided_at TIMESTAMP," +
            "    voided_amount DECIMAL(10,2) DEFAULT 0," +
            "    payment_card_last4 VARCHAR(4)," +
            "    payment_card_brand VARCHAR(50)," +
            "    payment_card_type VARCHAR(50)," +
            "    payment_card_expiry_month INTEGER," +
            "    payment_card_expiry_year INTEGER," +
            "    payment_card_holder_name VARCHAR(100)," +
            "    paypal_email VARCHAR(100)," +
            "    paypal_payer_id VARCHAR(50)," +
            "    apple_pay_token VARCHAR(255)," +
            "    google_pay_token VARCHAR(255)," +
            "    bank_transfer_reference VARCHAR(100)," +
            "    bank_transfer_date TIMESTAMP," +
            "    bank_name VARCHAR(100)," +
            "    bank_account_last4 VARCHAR(4)," +
            "    check_number VARCHAR(50)," +
            "    check_date TIMESTAMP," +
            "    cash_received_by VARCHAR(100)," +
            "    cash_received_date TIMESTAMP," +
            "    cash_register_id INTEGER," +
            "    customer_ip VARCHAR(45)," +
            "    customer_user_agent TEXT," +
            "    billing_address_line1 TEXT," +
            "    billing_address_line2 TEXT," +
            "    billing_city VARCHAR(50)," +
            "    billing_state_province VARCHAR(50)," +
            "    billing_postal_code VARCHAR(20)," +
            "    billing_country VARCHAR(50)," +
            "    notes TEXT," +
            "    admin_notes TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    created_by INTEGER," +
            "    updated_by INTEGER," +
            "    FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS shipping (" +
            "    shipping_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    order_id INTEGER NOT NULL," +
            "    user_id INTEGER," +
            "    shipment_number VARCHAR(100) UNIQUE," +
            "    carrier VARCHAR(100) NOT NULL," +
            "    carrier_code VARCHAR(50)," +
            "    service_name VARCHAR(100) NOT NULL," +
            "    service_code VARCHAR(50)," +
            "    service_level VARCHAR(50)," +
            "    tracking_number VARCHAR(100) UNIQUE," +
            "    tracking_url VARCHAR(255)," +
            "    return_tracking_number VARCHAR(100)," +
            "    return_tracking_url VARCHAR(255)," +
            "    shipping_date TIMESTAMP," +
            "    estimated_delivery_date TIMESTAMP," +
            "    actual_delivery_date TIMESTAMP," +
            "    first_scan_date TIMESTAMP," +
            "    last_scan_date TIMESTAMP," +
            "    status VARCHAR(50) NOT NULL," +
            "    status_updated_at TIMESTAMP," +
            "    status_updated_by INTEGER," +
            "    weight DECIMAL(10,3)," +
            "    weight_unit VARCHAR(10) DEFAULT 'kg'," +
            "    length DECIMAL(10,3)," +
            "    width DECIMAL(10,3)," +
            "    height DECIMAL(10,3)," +
            "    dimension_unit VARCHAR(10) DEFAULT 'cm'," +
            "    volume DECIMAL(10,3)," +
            "    volume_unit VARCHAR(10) DEFAULT 'l'," +
            "    declared_value DECIMAL(10,2)," +
            "    insurance_amount DECIMAL(10,2) DEFAULT 0," +
            "    insurance_required BOOLEAN DEFAULT 0," +
            "    signature_required BOOLEAN DEFAULT 0," +
            "    adult_signature_required BOOLEAN DEFAULT 0," +
            "    residential_delivery BOOLEAN DEFAULT 1," +
            "    saturday_delivery BOOLEAN DEFAULT 0," +
            "    saturday_pickup BOOLEAN DEFAULT 0," +
            "    delivery_instructions TEXT," +
            "    special_instructions TEXT," +
            "    shipping_label_url VARCHAR(255)," +
            "    packing_slip_url VARCHAR(255)," +
            "    commercial_invoice_url VARCHAR(255)," +
            "    customs_declaration_url VARCHAR(255)," +
            "    label_printed_at TIMESTAMP," +
            "    label_print_count INTEGER DEFAULT 0," +
            "    tracking_status_history TEXT," +
            "    latest_tracking_event TEXT," +
            "    latest_tracking_location VARCHAR(255)," +
            "    latest_tracking_status VARCHAR(100)," +
            "    latest_tracking_timestamp TIMESTAMP," +
            "    shipping_address_line1 TEXT," +
            "    shipping_address_line2 TEXT," +
            "    shipping_city VARCHAR(50)," +
            "    shipping_state_province VARCHAR(50)," +
            "    shipping_postal_code VARCHAR(20)," +
            "    shipping_country VARCHAR(50)," +
            "    shipping_first_name VARCHAR(50)," +
            "    shipping_last_name VARCHAR(50)," +
            "    shipping_company VARCHAR(100)," +
            "    shipping_phone VARCHAR(20)," +
            "    shipping_email VARCHAR(100)," +
            "    origin_address_line1 TEXT," +
            "    origin_address_line2 TEXT," +
            "    origin_city VARCHAR(50)," +
            "    origin_state_province VARCHAR(50)," +
            "    origin_postal_code VARCHAR(20)," +
            "    origin_country VARCHAR(50)," +
            "    origin_company VARCHAR(100)," +
            "    origin_phone VARCHAR(20)," +
            "    warehouse_id INTEGER," +
            "    warehouse_name VARCHAR(100)," +
            "    fulfillment_location VARCHAR(100)," +
            "    package_type VARCHAR(50)," +
            "    packages TEXT," +
            "    items TEXT," +
            "    item_count INTEGER DEFAULT 0," +
            "    product_count INTEGER DEFAULT 0," +
            "    total_quantity INTEGER DEFAULT 0," +
            "    shipping_cost DECIMAL(10,2) DEFAULT 0," +
            "    shipping_tax_amount DECIMAL(10,2) DEFAULT 0," +
            "    fuel_surcharge DECIMAL(10,2) DEFAULT 0," +
            "    handling_fee DECIMAL(10,2) DEFAULT 0," +
            "    insurance_fee DECIMAL(10,2) DEFAULT 0," +
            "    other_surcharges DECIMAL(10,2) DEFAULT 0," +
            "    total_shipping_cost DECIMAL(10,2) DEFAULT 0," +
            "    customer_pays_shipping BOOLEAN DEFAULT 1," +
            "    amount_charged_to_customer DECIMAL(10,2) DEFAULT 0," +
            "    is_free_shipping BOOLEAN DEFAULT 0," +
            "    is_expedited BOOLEAN DEFAULT 0," +
            "    is_international BOOLEAN DEFAULT 0," +
            "    customs_value DECIMAL(10,2)," +
            "    customs_description TEXT," +
            "    customs_tariff_number VARCHAR(50)," +
            "    country_of_origin VARCHAR(50)," +
            "    harmonized_code VARCHAR(50)," +
            "    duties_taxes_paid DECIMAL(10,2) DEFAULT 0," +
            "    duties_taxes_payable_by VARCHAR(20) DEFAULT 'recipient'," +
            "    incoterms VARCHAR(20) DEFAULT 'ddu'," +
            "    commercial_invoice_number VARCHAR(100)," +
            "    commercial_invoice_date TIMESTAMP," +
            "    notes TEXT," +
            "    admin_notes TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    created_by INTEGER," +
            "    updated_by INTEGER," +
            "    FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")"
        );

        statements.add(
            "CREATE TABLE IF NOT EXISTS reviews (" +
            "    review_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    product_id INTEGER NOT NULL," +
            "    user_id INTEGER NOT NULL," +
            "    order_id INTEGER," +
            "    order_item_id INTEGER," +
            "    parent_review_id INTEGER," +
            "    title VARCHAR(200)," +
            "    review_text TEXT NOT NULL," +
            "    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5)," +
            "    rating_details TEXT," +
            "    quality_rating INTEGER," +
            "    value_rating INTEGER," +
            "    price_rating INTEGER," +
            "    shipping_rating INTEGER," +
            "    customer_service_rating INTEGER," +
            "    is_verified_purchase BOOLEAN DEFAULT 0," +
            "    is_anonymous BOOLEAN DEFAULT 0," +
            "    is_featured BOOLEAN DEFAULT 0," +
            "    is_pinned BOOLEAN DEFAULT 0," +
            "    is_approved BOOLEAN DEFAULT 0," +
            "    is_spam BOOLEAN DEFAULT 0," +
            "    is_abuse BOOLEAN DEFAULT 0," +
            "    approved_at TIMESTAMP," +
            "    approved_by INTEGER," +
            "    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    last_edited_at TIMESTAMP," +
            "    last_edited_by INTEGER," +
            "    helpful_count INTEGER DEFAULT 0," +
            "    not_helpful_count INTEGER DEFAULT 0," +
            "    report_count INTEGER DEFAULT 0," +
            "    abuse_reports TEXT," +
            "    reviewer_name VARCHAR(100)," +
            "    reviewer_email VARCHAR(100)," +
            "    reviewer_ip VARCHAR(45)," +
            "    reviewer_user_agent TEXT," +
            "    reviewer_country VARCHAR(50)," +
            "    reviewer_region VARCHAR(50)," +
            "    reviewer_city VARCHAR(50)," +
            "    purchase_date TIMESTAMP," +
            "    use_case VARCHAR(100)," +
            "    use_duration VARCHAR(50)," +
            "    recommended BOOLEAN DEFAULT 0," +
            "    would_buy_again BOOLEAN DEFAULT 0," +
            "    pros TEXT," +
            "    cons TEXT," +
            "    bottom_line TEXT," +
            "    video_url VARCHAR(255)," +
            "    photo_urls TEXT," +
            "    video_uploaded BOOLEAN DEFAULT 0," +
            "    photo_uploaded BOOLEAN DEFAULT 0," +
            "    moderation_notes TEXT," +
            "    admin_response TEXT," +
            "    admin_response_date TIMESTAMP," +
            "    admin_response_by INTEGER," +
            "    source VARCHAR(50) DEFAULT 'website'," +
            "    channel VARCHAR(50) DEFAULT 'organic'," +
            "    import_source VARCHAR(50)," +
            "    import_id VARCHAR(100)," +
            "    import_date TIMESTAMP," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    deleted_at TIMESTAMP," +
            "    FOREIGN KEY (product_id) REFERENCES products(product_id)," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)," +
            "    FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
            "    FOREIGN KEY (parent_review_id) REFERENCES reviews(review_id)" +
            ")"
        );

        return statements;
    }

    public static List<String> getCreateIndexStatements() {
        List<String> indexes = new ArrayList<>();
        
        indexes.add("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_users_country ON users(country)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_users_status ON users(is_active, account_type)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_users_loyalty ON users(loyalty_tier, loyalty_points)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_category_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(category_slug)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_categories_visible ON categories(is_visible, display_order)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_brands_slug ON brands(brand_slug)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_brands_status ON brands(is_visible, is_featured)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_slug ON products(product_slug)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_name ON products(product_name)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_status ON products(is_active, is_visible, is_featured)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_stock ON products(stock_status, stock_quantity)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_price ON products(price, is_on_sale)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_products_rating ON products(average_rating DESC, review_count DESC)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_inventory_product ON inventory(product_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON inventory(warehouse_id, warehouse_location)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_inventory_quantity ON inventory(quantity, available_quantity)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status, fulfillment_status, payment_status)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_orders_number ON orders(order_number)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_email, customer_phone)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_order_items_product ON order_items(product_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_order_items_fulfillment ON order_items(fulfillment_status, is_backorder)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_payments_transaction ON payments(transaction_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status, payment_method)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_shipping_order ON shipping(order_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_shipping_tracking ON shipping(tracking_number)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_shipping_status ON shipping(status, carrier)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_shipping_date ON shipping(shipping_date, actual_delivery_date)");

        indexes.add("CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating DESC, review_date DESC)");
        indexes.add("CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(is_approved, is_featured, is_verified_purchase)");

        return indexes;
    }

    public static void createSchema(String dbUrl) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            
            for (String sql : getCreateTableStatements()) {
                stmt.execute(sql);
            }
            
            for (String sql : getCreateIndexStatements()) {
                stmt.execute(sql);
            }
        }
    }
}
