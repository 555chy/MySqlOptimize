

# 完整电商测试数据库说明

## 数据库文件

- **文件名**: `test.db` (或其他测试数据库文件)
- **数据库类型**: SQLite
- **说明**: 包含10个完整的电商业务表，带有适当的索引和外键约束

## 10个业务表

| 序号 | 表名 | 说明 | 主键 |
|------|------|------|------|
| 1 | users | 用户表（82个字段） | user_id |
| 2 | categories | 商品分类表（31个字段） | category_id |
| 3 | brands | 品牌表（53个字段） | brand_id |
| 4 | products | 商品表（101个字段） | product_id |
| 5 | inventory | 库存表（63个字段） | inventory_id |
| 6 | orders | 订单表（109个字段） | order_id |
| 7 | order_items | 订单明细表（91个字段） | order_item_id |
| 8 | payments | 支付表（83个字段） | payment_id |
| 9 | shipping | 物流表（125个字段） | shipping_id |
| 10 | reviews | 评论表（79个字段） | review_id |

## 索引列表 (33个索引)

### users 表索引 (5个)
1. idx_users_email - 邮箱索引
2. idx_users_username - 用户名索引
3. idx_users_country - 国家索引
4. idx_users_status - 状态索引
5. idx_users_loyalty - 会员等级索引

### categories 表索引 (3个)
6. idx_categories_parent - 父分类索引
7. idx_categories_slug - 别名索引
8. idx_categories_visible - 可见性索引

### brands 表索引 (2个)
9. idx_brands_slug - 品牌别名索引
10. idx_brands_status - 品牌状态索引

### products 表索引 (9个)
11. idx_products_category - 商品分类索引
12. idx_products_brand - 商品品牌索引
13. idx_products_slug - 商品别名索引
14. idx_products_sku - SKU索引
15. idx_products_name - 商品名称索引
16. idx_products_status - 商品状态索引
17. idx_products_stock - 库存状态索引
18. idx_products_price - 价格索引
19. idx_products_rating - 评分索引

### inventory 表索引 (3个)
20. idx_inventory_product - 库存商品索引
21. idx_inventory_warehouse - 仓库位置索引
22. idx_inventory_quantity - 库存数量索引

### orders 表索引 (5个)
23. idx_orders_user - 订单用户索引
24. idx_orders_date - 订单日期索引
25. idx_orders_status - 订单状态索引
26. idx_orders_number - 订单号索引
27. idx_orders_customer - 客户信息索引

### order_items 表索引 (3个)
28. idx_order_items_order - 订单项订单索引
29. idx_order_items_product - 订单项商品索引
30. idx_order_items_fulfillment - 订单项履约状态索引

### payments 表索引 (4个)
31. idx_payments_order - 支付订单索引
32. idx_payments_transaction - 交易号索引
33. idx_payments_status - 支付状态索引
34. idx_payments_date - 支付日期索引

### shipping 表索引 (4个)
35. idx_shipping_order - 物流订单索引
36. idx_shipping_tracking - 运单号索引
37. idx_shipping_status - 物流状态索引
38. idx_shipping_date - 物流日期索引

### reviews 表索引 (4个)
39. idx_reviews_product - 评论商品索引
40. idx_reviews_user - 评论用户索引
41. idx_reviews_rating - 评分索引
42. idx_reviews_status - 评论状态索引

## 数据规模说明

由于生成数千万条数据会非常耗时且占用大量磁盘空间，我们提供：

- **小测试数据**: 100条规模 (用于开发测试)
- **性能测试数据**: 可通过 `--rows` 参数自定义生成数据量

## 如何创建完整数据库

### 方法1: 使用命令行程序 (推荐)

```bash
mvn clean package
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --init-db -db "jdbc:sqlite:test.db" --rows 1000
```

### 方法2: 运行测试自动创建

```bash
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test -db "jdbc:sqlite:test.db"
```

## 表结构详情

### users (用户表 - 82个字段)

```
user_id, username, email, password_hash, first_name, last_name, middle_name,
phone, mobile_phone, work_phone, address_line1, address_line2, city, state_province,
postal_code, country, date_of_birth, gender, marital_status, occupation, annual_income,
currency_code, language_preference, timezone, is_verified, verification_date, verification_method,
last_login_at, last_login_ip, login_attempts, account_locked, account_locked_until,
password_changed_at, password_expiry_date, two_factor_enabled, two_factor_method,
newsletter_subscribed, marketing_opt_in, sms_notifications, push_notifications, email_notifications,
preferred_shipping_carrier, preferred_payment_method, loyalty_points, loyalty_tier,
referral_code, referred_by_user_id, avatar_url, bio, website_url,
social_facebook, social_twitter, social_linkedin, social_instagram,
tax_id, tax_identification_type, company_name, company_registration_number,
is_business_account, account_type, customer_segment, customer_lifetime_value,
last_purchase_date, total_purchases, total_spent, average_order_value,
favorite_category_id, favorite_brand_id, created_at, updated_at, deleted_at,
is_active, notes, admin_notes
```

### categories (商品分类表 - 31个字段)

```
category_id, category_name, category_slug, parent_category_id, description, short_description,
meta_title, meta_description, meta_keywords, category_image_url, thumbnail_url, banner_url,
display_order, is_visible, is_featured, show_in_navigation, searchable, page_title,
url_path, view_count, product_count, template, layout, color_code, icon_class,
custom_attributes, created_at, updated_at, created_by, updated_by
```

### brands (品牌表 - 53个字段)

```
brand_id, brand_name, brand_slug, logo_url, logo_svg, banner_url, thumbnail_url,
description, short_description, founded_year, founded_country, headquarters_location,
website_url, contact_email, contact_phone, support_phone, support_email,
social_facebook, social_twitter, social_instagram, social_youtube, social_linkedin,
meta_title, meta_description, meta_keywords, brand_story, mission_statement, vision_statement,
warranty_info, return_policy, shipping_policy, is_visible, is_featured, is_premium,
display_order, view_count, product_count, average_rating, total_reviews, rating_distribution,
country_of_origin, primary_category_id, parent_brand_id, custom_attributes,
created_at, updated_at, created_by, updated_by
```

### products (商品表 - 101个字段)

```
product_id, product_name, product_slug, sku, upc, ean, jan, isbn, mpn,
description, short_description, long_description, meta_title, meta_description, meta_keywords,
price, cost_price, sale_price, sale_price_start_date, sale_price_end_date, msrp,
currency_code, price_display, tax_class, tax_rate, weight, weight_unit,
length, width, height, dimension_unit, volume, volume_unit,
category_id, brand_id, vendor_id, vendor_sku, vendor_cost,
is_active, is_visible, is_featured, is_new, is_on_sale, is_backorder, is_preorder,
preorder_available_date, requires_shipping, is_virtual, is_downloadable,
downloadable_file_url, max_downloads, download_expiry_days,
has_options, option_set_id, stock_quantity, stock_availability,
out_of_stock_action, allow_backorder, backorder_quantity,
min_purchase_quantity, max_purchase_quantity, quantity_increment,
notify_qty_below, notify_stock_owner, manage_stock, stock_status,
available_date, availability_description,
image_url, thumbnail_url, small_image_url, gallery_image_urls, video_url,
warranty, return_policy, shipping_policy,
search_keywords, tags, search_weight,
view_count, wishlist_count, cart_count, order_count, sold_quantity, revenue_total,
average_rating, review_count, rating_distribution, last_review_date,
show_reviews, show_questions, show_prices, allow_add_to_cart, allow_wishlist, allow_compare,
template, layout, color_code, custom_attributes, custom_options,
created_at, updated_at, published_at, created_by, updated_by
```

### inventory (库存表 - 63个字段)

```
inventory_id, product_id, warehouse_id, warehouse_location, warehouse_zone,
warehouse_aisle, warehouse_rack, warehouse_shelf, warehouse_bin,
quantity, available_quantity, reserved_quantity, incoming_quantity,
damaged_quantity, defective_quantity, returned_quantity, on_hold_quantity, backordered_quantity,
minimum_quantity, maximum_quantity, reorder_threshold, reorder_quantity, reorder_point,
economic_order_quantity, safety_stock,
last_physical_count, last_physical_count_quantity, next_physical_count_date,
last_received_date, last_shipped_date,
last_adjustment_date, last_adjustment_note, last_adjustment_quantity, last_adjustment_type,
inventory_value, cost_method, average_cost, last_cost, standard_cost,
is_serialized, is_lot_tracked, is_expiration_tracked,
lot_number, serial_number, expiration_date, manufacture_date, batch_number, supplier_batch_number,
country_of_origin, state, status, notes,
last_updated, last_updated_by, created_at, updated_at
```

### orders (订单表 - 109个字段)

```
order_id, order_number, user_id, customer_id, customer_email, customer_phone,
customer_ip, customer_user_agent, order_date, updated_at,
order_status, status_updated_at, status_updated_by,
fulfillment_status, fulfillment_updated_at,
payment_status, payment_updated_at,
total_amount, subtotal, tax_amount, tax_rate, tax_name, tax_details,
shipping_amount, shipping_tax_amount,
discount_amount, discount_details, coupon_code, coupon_discount_amount,
gift_card_amount, loyalty_points_used, loyalty_points_discount,
grand_total, currency_code, exchange_rate, base_currency_code, base_total_amount,
weight_total, weight_unit, item_count, product_count,
shipping_method_id, shipping_method_name, shipping_carrier, shipping_service,
estimated_delivery_date, tracking_number, tracking_url,
shipping_address_line1, shipping_address_line2, shipping_city, shipping_state_province,
shipping_postal_code, shipping_country, shipping_first_name, shipping_last_name,
shipping_company, shipping_phone, shipping_email, shipping_instructions,
billing_address_line1, billing_address_line2, billing_city, billing_state_province,
billing_postal_code, billing_country, billing_first_name, billing_last_name,
billing_company, billing_phone, billing_email,
tax_id, tax_identification_type,
invoice_number, invoice_date, invoice_sent_at, packing_slip_printed_at, packing_slip_print_count,
order_notes, customer_notes, admin_notes, staff_notes, private_notes,
source, channel, affiliate_id, affiliate_commission,
referred_by, utm_source, utm_medium, utm_campaign, utm_term, utm_content,
landing_page_url, referrer_url,
is_guest_order, is_manual_order, is_wholesale_order, is_gift,
gift_message, gift_wrap, gift_wrap_amount,
loyalty_points_earned, loyalty_points_redeemable, customer_order_sequence, is_test_order,
has_return, has_refund, has_exchange, refunded_amount, returned_item_count,
created_at, completed_at, cancelled_at, cancel_reason, cancelled_by, created_by, updated_by
```

### order_items (订单明细表 - 91个字段)

```
order_item_id, order_id, product_id, variant_id, parent_item_id,
product_name, product_sku, product_slug, product_image_url, product_type,
vendor_name, brand_name, category_name,
quantity, quantity_shipped, quantity_returned, quantity_cancelled, quantity_refunded,
unit_price, original_unit_price, cost_price,
subtotal, discount_amount, discount_details, tax_amount, tax_rate, tax_name, tax_details,
shipping_amount, shipping_tax_amount, total,
weight, weight_unit, length, width, height, dimension_unit,
variant_title, variant_options, customizations, product_attributes,
is_gift, gift_message, gift_wrap,
is_backorder, is_preorder, backorder_date, expected_ship_date, estimated_arrival_date,
warehouse_id, warehouse_location,
fulfillment_status, fulfillment_service, fulfillment_location,
tracking_number, tracking_url, ship_date, carrier, shipping_service,
return_status, return_date, return_reason, refund_amount, refund_date, refund_reason,
exchange_status, exchange_order_item_id,
line_item_notes, admin_notes, sort_order, created_at, updated_at
```

### payments (支付表 - 83个字段)

```
payment_id, order_id, user_id, customer_id,
transaction_id, gateway_transaction_id, parent_transaction_id,
payment_method, payment_method_type, payment_processor,
payment_date, amount, currency_code, exchange_rate, status,
failure_code, failure_message, failure_reason,
gateway_response, gateway_response_code, gateway_response_message,
avs_response, cvv_response, risk_score, risk_level, risk_assessment,
is_manual, is_offline, is_test, is_recurring, is_subscription, subscription_id,
authorization_code, authorization_date, authorization_expires_at,
captured_at, captured_amount, refunded_at, refunded_amount, voided_at, voided_amount,
payment_card_last4, payment_card_brand, payment_card_type,
payment_card_expiry_month, payment_card_expiry_year, payment_card_holder_name,
paypal_email, paypal_payer_id, apple_pay_token, google_pay_token,
bank_transfer_reference, bank_transfer_date, bank_name, bank_account_last4,
check_number, check_date, cash_received_by, cash_received_date, cash_register_id,
customer_ip, customer_user_agent,
billing_address_line1, billing_address_line2, billing_city, billing_state_province,
billing_postal_code, billing_country,
notes, admin_notes, created_at, updated_at, created_by, updated_by
```

### shipping (物流表 - 125个字段)

```
shipping_id, order_id, user_id, shipment_number,
carrier, carrier_code, service_name, service_code, service_level,
tracking_number, tracking_url, return_tracking_number, return_tracking_url,
shipping_date, estimated_delivery_date, actual_delivery_date,
first_scan_date, last_scan_date, status, status_updated_at, status_updated_by,
weight, weight_unit, length, width, height, dimension_unit, volume, volume_unit,
declared_value, insurance_amount, insurance_required,
signature_required, adult_signature_required,
residential_delivery, saturday_delivery, saturday_pickup,
delivery_instructions, special_instructions,
shipping_label_url, packing_slip_url, commercial_invoice_url, customs_declaration_url,
label_printed_at, label_print_count,
tracking_status_history, latest_tracking_event, latest_tracking_location,
latest_tracking_status, latest_tracking_timestamp,
shipping_address_line1, shipping_address_line2, shipping_city, shipping_state_province,
shipping_postal_code, shipping_country, shipping_first_name, shipping_last_name,
shipping_company, shipping_phone, shipping_email,
origin_address_line1, origin_address_line2, origin_city, origin_state_province,
origin_postal_code, origin_country, origin_company, origin_phone,
warehouse_id, warehouse_name, fulfillment_location, package_type, packages, items,
item_count, product_count, total_quantity,
shipping_cost, shipping_tax_amount, fuel_surcharge, handling_fee,
insurance_fee, other_surcharges, total_shipping_cost,
customer_pays_shipping, amount_charged_to_customer,
is_free_shipping, is_expedited, is_international,
customs_value, customs_description, customs_tariff_number,
country_of_origin, harmonized_code, duties_taxes_paid, duties_taxes_payable_by, incoterms,
commercial_invoice_number, commercial_invoice_date,
notes, admin_notes, created_at, updated_at, created_by, updated_by
```

### reviews (评论表 - 79个字段)

```
review_id, product_id, user_id, order_id, order_item_id, parent_review_id,
title, review_text, rating, rating_details,
quality_rating, value_rating, price_rating, shipping_rating, customer_service_rating,
is_verified_purchase, is_anonymous, is_featured, is_pinned, is_approved, is_spam, is_abuse,
approved_at, approved_by, review_date, last_edited_at, last_edited_by,
helpful_count, not_helpful_count, report_count, abuse_reports,
reviewer_name, reviewer_email, reviewer_ip, reviewer_user_agent,
reviewer_country, reviewer_region, reviewer_city,
purchase_date, use_case, use_duration, recommended, would_buy_again,
pros, cons, bottom_line, video_url, photo_urls,
video_uploaded, photo_uploaded, moderation_notes,
admin_response, admin_response_date, admin_response_by,
source, channel, import_source, import_id, import_date,
created_at, updated_at, deleted_at
```

## 外键关系

- products.category_id -> categories.category_id
- products.brand_id -> brands.brand_id
- inventory.product_id -> products.product_id
- orders.user_id -> users.user_id
- order_items.order_id -> orders.order_id
- order_items.product_id -> products.product_id
- payments.order_id -> orders.order_id
- payments.user_id -> users.user_id
- shipping.order_id -> orders.order_id
- shipping.user_id -> users.user_id
- reviews.product_id -> products.product_id
- reviews.user_id -> users.user_id
- reviews.order_id -> orders.order_id
- reviews.parent_review_id -> reviews.review_id
- categories.parent_category_id -> categories.category_id
- brands.primary_category_id -> categories.category_id
- brands.parent_brand_id -> brands.brand_id
- users.referred_by_user_id -> users.user_id

## 注意事项

1. **数据量控制**: 使用 `--rows` 参数控制生成的数据行数
2. **生成时间**: 大数据量生成可能需要较长时间，请耐心等待
3. **磁盘空间**: 确保有足够的磁盘空间用于数据库文件
4. **SQLite限制**: SQLite在处理超大规模数据时性能可能不如专业数据库，但足够用于SQL优化验证
