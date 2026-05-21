-- Query 1: 用户购买频次和消费能力分析
WITH user_purchase_stats AS (
    SELECT u.user_id, u.username, u.email,
        COUNT(o.order_id) AS total_orders,
        COALESCE(SUM(o.total_amount), 0) AS total_spent,
        COALESCE(AVG(o.total_amount), 0) AS avg_order_value,
        MIN(o.order_date) AS first_order_date,
        MAX(o.order_date) AS last_order_date,
        COUNT(DISTINCT oi.product_id) AS unique_products_purchased,
        COALESCE(SUM(oi.quantity), 0) AS total_items_purchased
    FROM users u
    LEFT JOIN orders o ON u.user_id = o.user_id
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    WHERE u.is_active = 1
    GROUP BY u.user_id, u.username, u.email
),
user_segments AS (
    SELECT ups.*,
        CASE WHEN total_orders >= 3 THEN '高频用户' WHEN total_orders >= 1 THEN '中频用户' ELSE '低频用户' END AS purchase_frequency_segment,
        CASE WHEN total_spent >= 500 THEN '高消费用户' WHEN total_spent >= 100 THEN '中消费用户' WHEN total_spent >= 1 THEN '低消费用户' ELSE '未消费用户' END AS spending_segment
    FROM user_purchase_stats ups
)
SELECT user_id, username, email, total_orders, total_spent, avg_order_value, first_order_date, last_order_date, unique_products_purchased, total_items_purchased, purchase_frequency_segment, spending_segment FROM user_segments ORDER BY total_spent DESC LIMIT 100;

-- Query 2: 产品销售排行
WITH product_sales AS (
    SELECT p.product_id, p.product_name, p.sku, p.price, c.category_name, b.brand_name,
        COALESCE(SUM(oi.quantity), 0) AS total_units_sold,
        COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue,
        COALESCE(SUM(oi.quantity * COALESCE(p.cost_price, 0)), 0) AS total_cost,
        COUNT(DISTINCT o.order_id) AS total_orders,
        COUNT(DISTINCT o.user_id) AS unique_customers
    FROM products p
    LEFT JOIN order_items oi ON p.product_id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.order_id
    LEFT JOIN categories c ON p.category_id = c.category_id
    LEFT JOIN brands b ON p.brand_id = b.brand_id
    WHERE p.is_active = 1
    GROUP BY p.product_id, p.product_name, p.sku, p.price, c.category_name, b.brand_name
),
product_rankings AS (
    SELECT ps.*,
        ROUND(COALESCE(total_revenue / NULLIF(total_units_sold, 0), 0), 2) AS avg_price_per_unit,
        ROUND(COALESCE((total_revenue - total_cost) / NULLIF(total_revenue, 0) * 100, 0), 2) AS profit_margin_percent,
        RANK() OVER (ORDER BY total_revenue DESC) AS revenue_rank
    FROM product_sales ps
)
SELECT product_id, product_name, sku, price, category_name, brand_name, total_units_sold, total_revenue, total_cost, avg_price_per_unit, profit_margin_percent, total_orders, unique_customers, revenue_rank FROM product_rankings ORDER BY total_revenue DESC LIMIT 100;

-- Query 3: 订单交付分析
WITH order_delivery AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.shipping_amount,
        COUNT(oi.order_item_id) AS item_count, COALESCE(SUM(oi.quantity), 0) AS total_items
    FROM orders o
    LEFT JOIN users u ON o.user_id = u.user_id
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    GROUP BY o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.shipping_amount
),
delivery_metrics AS (
    SELECT od.*, ROUND(COALESCE(shipping_amount / NULLIF(total_amount, 0) * 100, 0), 2) AS shipping_cost_percent FROM order_delivery od
),
method_stats AS (
    SELECT COUNT(DISTINCT order_id) AS total_orders, AVG(total_amount) AS avg_order_value FROM delivery_metrics
)
SELECT dm.order_id, dm.order_number, dm.username, dm.order_date, dm.total_amount, dm.shipping_amount, dm.item_count, dm.total_items, dm.shipping_cost_percent, ms.total_orders AS overall_total_orders FROM delivery_metrics dm CROSS JOIN method_stats ms ORDER BY dm.order_date DESC LIMIT 100;

-- Query 4: 库存分析
SELECT i.product_id, COALESCE(p.product_name, 'Unknown Product') AS product_name, COALESCE(p.sku, 'N/A') AS sku, COALESCE(p.price, 0) AS price, i.quantity, i.available_quantity,
    i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,
    CASE WHEN i.available_quantity <= COALESCE(i.reorder_threshold, 10) THEN '需要补货' 
         WHEN i.available_quantity <= COALESCE(i.minimum_quantity, 20) THEN '库存偏低' 
         WHEN i.available_quantity >= COALESCE(i.maximum_quantity, 100) THEN '库存充足' 
         ELSE '库存正常' END AS stock_status 
FROM inventory i
LEFT JOIN products p ON i.product_id = p.product_id
ORDER BY stock_status LIMIT 100;

-- Query 5: 支付方式分析
WITH payment_analysis AS (
    SELECT p.payment_id, p.order_id, o.order_number, u.username, p.payment_method, p.amount, p.status, p.payment_date,
        CASE WHEN p.status IN ('completed', 'captured') THEN '成功' WHEN p.status IN ('failed', 'declined') THEN '失败' WHEN p.status IN ('pending') THEN '处理中' ELSE '其他' END AS payment_status_category,
        CASE WHEN p.payment_method IN ('credit_card', 'debit_card') THEN '银行卡' WHEN p.payment_method = 'paypal' THEN 'PayPal' WHEN p.payment_method = 'bank_transfer' THEN '银行转账' ELSE '其他' END AS payment_category
    FROM payments p
    LEFT JOIN orders o ON p.order_id = o.order_id
    LEFT JOIN users u ON p.user_id = u.user_id
),
method_stats AS (
    SELECT payment_method, payment_category, COUNT(DISTINCT payment_id) AS total_transactions, COALESCE(SUM(amount), 0) AS total_amount_processed, SUM(CASE WHEN payment_status_category = '成功' THEN 1 ELSE 0 END) AS successful_count FROM payment_analysis GROUP BY payment_method, payment_category
),
success_rates AS (
    SELECT *, ROUND(COALESCE(successful_count * 100.0 / NULLIF(total_transactions, 0), 0), 2) AS success_rate_percent FROM method_stats
)
SELECT pa.payment_id, pa.order_id, pa.order_number, pa.username, pa.payment_method, pa.payment_category, pa.amount, pa.status, pa.payment_status_category, pa.payment_date, sr.success_rate_percent FROM payment_analysis pa LEFT JOIN success_rates sr ON pa.payment_method = sr.payment_method ORDER BY pa.payment_date DESC LIMIT 100;

-- Query 6: 评论情感分析
WITH review_analysis AS (
    SELECT r.review_id, r.product_id, p.product_name, c.category_name, u.username, r.rating, r.review_text, r.is_verified_purchase, r.review_date, r.helpful_count, r.not_helpful_count,
        CASE WHEN r.rating >= 4 THEN '正面' WHEN r.rating = 3 THEN '中性' ELSE '负面' END AS sentiment,
        (r.helpful_count + r.not_helpful_count) AS total_votes,
        ROUND(COALESCE(r.helpful_count * 100.0 / NULLIF(r.helpful_count + r.not_helpful_count, 0), 0), 2) AS helpful_percent
    FROM reviews r
    LEFT JOIN products p ON r.product_id = p.product_id
    LEFT JOIN categories c ON p.category_id = c.category_id
    LEFT JOIN users u ON r.user_id = u.user_id
),
product_review_stats AS (
    SELECT ra.product_id, COUNT(DISTINCT ra.review_id) AS total_reviews, COALESCE(AVG(ra.rating), 0) AS avg_rating,
        SUM(CASE WHEN ra.sentiment = '正面' THEN 1 ELSE 0 END) AS positive_count
    FROM review_analysis ra GROUP BY ra.product_id
)
SELECT ra.review_id, ra.product_id, ra.product_name, ra.category_name, ra.username, ra.rating, ra.sentiment, ra.review_text, ra.is_verified_purchase, ra.review_date, ra.helpful_count, ra.total_votes, ra.helpful_percent, prs.total_reviews, prs.avg_rating FROM review_analysis ra LEFT JOIN product_review_stats prs ON ra.product_id = prs.product_id ORDER BY ra.review_date DESC LIMIT 100;

-- Query 7: 用户留存分析
WITH user_first_purchase AS (
    SELECT u.user_id, u.username, MIN(o.order_date) AS first_purchase_date FROM users u LEFT JOIN orders o ON u.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username
),
user_purchase_history AS (
    SELECT u.user_id, COUNT(o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent, MAX(o.order_date) AS last_order_date, CAST(julianday('now') - julianday(MAX(o.order_date)) AS INTEGER) AS days_since_last_order FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id
)
SELECT ufp.user_id, ufp.username, ufp.first_purchase_date, uph.total_orders, uph.total_spent, uph.last_order_date, uph.days_since_last_order,
    CASE WHEN uph.total_orders >= 3 THEN '高留存用户' WHEN uph.total_orders >= 1 THEN '中等留存用户' ELSE '一次性用户' END AS retention_segment
FROM user_first_purchase ufp LEFT JOIN user_purchase_history uph ON ufp.user_id = uph.user_id ORDER BY uph.total_spent DESC LIMIT 100;

-- Query 8: 促销效果分析
WITH promotion_usage AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.subtotal, o.discount_amount, o.coupon_code,
        COUNT(oi.order_item_id) AS item_count, COALESCE(SUM(oi.quantity), 0) AS total_items,
        CASE WHEN o.coupon_code IS NOT NULL THEN 1 ELSE 0 END AS used_coupon
    FROM orders o
    LEFT JOIN users u ON o.user_id = u.user_id
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    GROUP BY o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.subtotal, o.discount_amount, o.coupon_code
)
SELECT order_id, order_number, username, order_date, total_amount, subtotal, discount_amount, coupon_code, item_count, total_items, used_coupon, ROUND(COALESCE(discount_amount / NULLIF(subtotal, 0) * 100, 0), 2) AS order_discount_percent FROM promotion_usage ORDER BY order_date DESC LIMIT 100;

-- Query 9: 物流配送分析
WITH shipment_details AS (
    SELECT s.shipping_id, s.order_id, o.order_number, u.username, s.carrier, s.service_name, s.status, s.weight, s.item_count, s.total_quantity, s.shipping_cost, s.is_free_shipping, o.total_amount AS order_total
    FROM shipping s
    LEFT JOIN orders o ON s.order_id = o.order_id
    LEFT JOIN users u ON s.user_id = u.user_id
),
carrier_performance AS (
    SELECT sd.carrier, COUNT(DISTINCT sd.shipping_id) AS total_shipments, AVG(COALESCE(sd.shipping_cost, 0)) AS avg_shipping_cost FROM shipment_details sd GROUP BY sd.carrier
)
SELECT sd.shipping_id, sd.order_id, sd.order_number, sd.username, sd.carrier, sd.service_name, sd.status, sd.weight, sd.item_count, sd.total_quantity, sd.shipping_cost, sd.is_free_shipping, sd.order_total, cp.avg_shipping_cost FROM shipment_details sd LEFT JOIN carrier_performance cp ON sd.carrier = cp.carrier ORDER BY sd.carrier LIMIT 100;

-- Query 10: 品牌表现分析
WITH brand_sales AS (
    SELECT b.brand_id, b.brand_name, b.is_premium, COUNT(DISTINCT p.product_id) AS product_count, COALESCE(SUM(oi.quantity), 0) AS total_units_sold, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name, b.is_premium
),
brand_rankings AS (
    SELECT bs.*, RANK() OVER (ORDER BY total_revenue DESC) AS revenue_rank FROM brand_sales bs
)
SELECT brand_id, brand_name, is_premium, product_count, total_units_sold, total_revenue, total_orders, unique_customers, revenue_rank, CASE WHEN is_premium = 1 THEN '高端品牌' ELSE '大众品牌' END AS brand_segment FROM brand_rankings ORDER BY total_revenue DESC LIMIT 100;

-- Query 11: 客户生命周期价值分析
WITH customer_lifetime AS (
    SELECT u.user_id, u.username, u.email, u.created_at AS registration_date, u.loyalty_tier, u.country, COUNT(o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent, COALESCE(AVG(o.total_amount), 0) AS avg_order_value, MIN(o.order_date) AS first_purchase_date, MAX(o.order_date) AS last_purchase_date, COUNT(DISTINCT oi.product_id) AS unique_products_purchased FROM users u LEFT JOIN orders o ON u.user_id = o.user_id LEFT JOIN order_items oi ON o.order_id = oi.order_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.email, u.created_at, u.loyalty_tier, u.country
)
SELECT user_id, username, email, registration_date, loyalty_tier, country, total_orders, total_spent, avg_order_value, first_purchase_date, last_purchase_date, unique_products_purchased, CASE WHEN total_spent >= 500 THEN '高价值客户' WHEN total_spent >= 100 THEN '中等价值客户' ELSE '普通客户' END AS value_segment FROM customer_lifetime ORDER BY total_spent DESC LIMIT 100;

-- Query 12: 销售地域分布分析
WITH regional_sales AS (
    SELECT o.shipping_country AS country, o.shipping_city AS city, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(AVG(o.total_amount), 0) AS avg_order_value, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(oi.quantity), 0) AS total_units_sold FROM orders o LEFT JOIN order_items oi ON o.order_id = oi.order_id GROUP BY o.shipping_country, o.shipping_city
),
country_stats AS (
    SELECT country, COUNT(DISTINCT city) AS cities_count, SUM(total_orders) AS country_total_orders, SUM(total_revenue) AS country_total_revenue, ROUND(SUM(total_revenue) * 100.0 / (SELECT COALESCE(SUM(total_revenue), 1) FROM regional_sales), 2) AS country_revenue_share FROM regional_sales GROUP BY country
)
SELECT rs.country, rs.city, cs.cities_count, rs.total_orders, rs.total_revenue, rs.avg_order_value, rs.unique_customers, rs.total_units_sold, cs.country_total_orders, cs.country_total_revenue, cs.country_revenue_share FROM regional_sales rs LEFT JOIN country_stats cs ON rs.country = cs.country ORDER BY rs.country LIMIT 100;

-- Query 13: 产品交叉销售分析
SELECT o.order_id, o.order_number, COUNT(DISTINCT oi.product_id) AS product_count,
    COALESCE(GROUP_CONCAT(DISTINCT oi.product_id), '') AS product_ids
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id, o.order_number
ORDER BY product_count DESC LIMIT 100;

-- Query 14: 退货和退款分析
WITH return_analysis AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.has_return, o.has_refund, o.refunded_amount, COUNT(oi.order_item_id) AS total_items, COALESCE(SUM(oi.quantity_returned), 0) AS total_returned_quantity FROM orders o LEFT JOIN users u ON o.user_id = u.user_id LEFT JOIN order_items oi ON o.order_id = oi.order_id GROUP BY o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.has_return, o.has_refund, o.refunded_amount
),
return_stats AS (
    SELECT COUNT(DISTINCT order_id) AS total_orders_with_return, COALESCE(SUM(refunded_amount), 0) AS total_refunded_amount FROM return_analysis
)
SELECT ra.order_id, ra.order_number, ra.username, ra.order_date, ra.total_amount, ra.has_return, ra.has_refund, ra.refunded_amount, ra.total_items, ra.total_returned_quantity, ROUND(COALESCE(ra.refunded_amount / NULLIF(ra.total_amount, 0) * 100, 0), 2) AS refund_percent_of_order, rs.total_orders_with_return FROM return_analysis ra CROSS JOIN return_stats rs ORDER BY ra.order_date DESC LIMIT 100;

-- Query 15: 时间维度销售趋势分析
WITH daily_sales AS (
    SELECT DATE(o.order_date) AS sale_date, strftime('%Y-%m', o.order_date) AS sale_month, strftime('%w', o.order_date) AS day_of_week, strftime('%H', o.order_date) AS hour_of_day, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(SUM(oi.quantity), 0) AS total_units_sold FROM orders o LEFT JOIN order_items oi ON o.order_id = oi.order_id GROUP BY DATE(o.order_date), strftime('%Y-%m', o.order_date), strftime('%w', o.order_date), strftime('%H', o.order_date)
),
weekly_patterns AS (
    SELECT day_of_week, CASE day_of_week WHEN '0' THEN '周日' WHEN '1' THEN '周一' WHEN '2' THEN '周二' WHEN '3' THEN '周三' WHEN '4' THEN '周四' WHEN '5' THEN '周五' ELSE '周六' END AS day_name, AVG(total_orders) AS avg_orders_per_day FROM daily_sales GROUP BY day_of_week
)
SELECT ds.sale_date, ds.sale_month, ds.day_of_week, wp.day_name, ds.hour_of_day, ds.total_orders, ds.unique_customers, ds.total_revenue, ds.total_units_sold, wp.avg_orders_per_day FROM daily_sales ds LEFT JOIN weekly_patterns wp ON ds.day_of_week = wp.day_of_week ORDER BY ds.sale_date, ds.hour_of_day LIMIT 100;

-- Query 16: 用户推荐网络分析
WITH referral_network AS (
    SELECT u.user_id AS referrer_id, u.username AS referrer_username, u.loyalty_tier AS referrer_tier, ref.user_id AS referred_user_id, ref.username AS referred_username, COUNT(o.order_id) AS referred_user_orders, COALESCE(SUM(o.total_amount), 0) AS referred_user_total_spent FROM users u LEFT JOIN users ref ON u.user_id = ref.referred_by_user_id LEFT JOIN orders o ON ref.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.loyalty_tier, ref.user_id, ref.username
),
referrer_stats AS (
    SELECT referrer_id, referrer_username, referrer_tier, COUNT(DISTINCT referred_user_id) AS total_referrals, COALESCE(SUM(referred_user_total_spent), 0) AS total_revenue_from_referrals FROM referral_network GROUP BY referrer_id, referrer_username, referrer_tier
)
SELECT referrer_id, referrer_username, referrer_tier, total_referrals, total_revenue_from_referrals FROM referrer_stats ORDER BY total_revenue_from_referrals DESC LIMIT 100;

-- Query 17: 优惠券和折扣效果分析
WITH coupon_usage AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.subtotal, o.discount_amount, o.coupon_code, o.gift_card_amount, o.loyalty_points_used, COUNT(oi.order_item_id) AS item_count, COALESCE(SUM(oi.quantity), 0) AS total_items, CASE WHEN o.coupon_code IS NOT NULL THEN 1 ELSE 0 END AS used_coupon, CASE WHEN COALESCE(o.gift_card_amount, 0) > 0 THEN 1 ELSE 0 END AS used_gift_card, ROUND(COALESCE(o.discount_amount / NULLIF(o.subtotal, 0) * 100, 0), 2) AS discount_percentage FROM orders o LEFT JOIN users u ON o.user_id = u.user_id LEFT JOIN order_items oi ON o.order_id = oi.order_id GROUP BY o.order_id, o.order_number, u.username, o.order_date, o.total_amount, o.subtotal, o.discount_amount, o.coupon_code, o.gift_card_amount, o.loyalty_points_used
)
SELECT order_id, order_number, username, order_date, total_amount, subtotal, discount_amount, coupon_code, gift_card_amount, loyalty_points_used, item_count, total_items, used_coupon, used_gift_card, discount_percentage FROM coupon_usage ORDER BY order_date DESC LIMIT 100;

-- Query 18: 产品库存周转率分析
SELECT i.product_id, COALESCE(p.product_name, 'Unknown Product') AS product_name, COALESCE(p.sku, 'N/A') AS sku, i.quantity AS current_inventory, 
    i.available_quantity, COALESCE(SUM(oi.quantity), 0) AS units_sold,
    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue_generated,
    ROUND(COALESCE(SUM(oi.quantity) / NULLIF(i.quantity, 0), 0), 2) AS inventory_turnover_ratio,
    CASE WHEN COALESCE(SUM(oi.quantity), 0) / NULLIF(i.quantity, 0) >= 0.5 THEN '高周转' 
         WHEN COALESCE(SUM(oi.quantity), 0) / NULLIF(i.quantity, 0) >= 0.2 THEN '正常周转' 
         ELSE '低周转' END AS turnover_status 
FROM inventory i
LEFT JOIN products p ON i.product_id = p.product_id
LEFT JOIN order_items oi ON i.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id
GROUP BY i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity
ORDER BY inventory_turnover_ratio DESC LIMIT 100;

-- Query 19: 客户满意度分析
WITH customer_satisfaction AS (
    SELECT u.user_id, u.username, u.email, u.country, u.loyalty_tier, COUNT(r.review_id) AS total_reviews, COALESCE(AVG(r.rating), 0) AS avg_rating, SUM(CASE WHEN r.rating >= 4 THEN 1 ELSE 0 END) AS positive_reviews, SUM(CASE WHEN r.rating <= 2 THEN 1 ELSE 0 END) AS negative_reviews, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent FROM users u LEFT JOIN reviews r ON u.user_id = r.user_id LEFT JOIN orders o ON u.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.email, u.country, u.loyalty_tier
),
satisfaction_segmentation AS (
    SELECT cs.*, CASE WHEN cs.avg_rating >= 4.0 THEN '满意' WHEN cs.avg_rating >= 3.0 THEN '一般' WHEN cs.avg_rating >= 1.0 THEN '不满意' ELSE '未评价' END AS satisfaction_level FROM customer_satisfaction cs
)
SELECT user_id, username, email, country, loyalty_tier, total_reviews, avg_rating, positive_reviews, negative_reviews, total_orders, total_spent, satisfaction_level FROM satisfaction_segmentation ORDER BY avg_rating DESC LIMIT 100;

-- Query 20: 购物车放弃分析
WITH cart_abandonment AS (
    SELECT u.user_id, u.username, u.email, COUNT(DISTINCT o.order_id) AS completed_orders, COUNT(DISTINCT p.payment_id) AS attempted_payments, SUM(CASE WHEN p.status IN ('failed', 'declined') THEN 1 ELSE 0 END) AS failed_payments, SUM(CASE WHEN p.status IN ('completed', 'captured') THEN 1 ELSE 0 END) AS successful_payments, COALESCE(SUM(o.total_amount), 0) AS total_order_value FROM users u LEFT JOIN orders o ON u.user_id = o.user_id LEFT JOIN payments p ON o.order_id = p.order_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.email
),
abandonment_segments AS (
    SELECT ca.*, ROUND(COALESCE(SUM(CASE WHEN p.status IN ('completed', 'captured') THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(DISTINCT p.payment_id), 0), 0), 2) AS payment_success_rate FROM cart_abandonment ca LEFT JOIN payments p ON 1=1 GROUP BY ca.user_id, ca.username, ca.email, ca.completed_orders, ca.attempted_payments, ca.failed_payments, ca.successful_payments, ca.total_order_value
)
SELECT user_id, username, email, completed_orders, attempted_payments, successful_payments, failed_payments, total_order_value, payment_success_rate FROM abandonment_segments ORDER BY completed_orders DESC LIMIT 100;

-- Query 21: 供应商表现分析
WITH supplier_performance AS (
    SELECT b.brand_id, b.brand_name, b.is_premium, b.country_of_origin, COUNT(DISTINCT p.product_id) AS product_count, COALESCE(SUM(p.stock_quantity), 0) AS total_stock, COALESCE(SUM(oi.quantity), 0) AS units_sold, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue_generated FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name, b.is_premium, b.country_of_origin
),
brand_comparison AS (
    SELECT sp.*, RANK() OVER (ORDER BY revenue_generated DESC) AS revenue_rank FROM supplier_performance sp
)
SELECT brand_id, brand_name, is_premium, country_of_origin, product_count, total_stock, units_sold, revenue_generated, revenue_rank, CASE WHEN is_premium = 1 THEN '高端品牌' ELSE '大众品牌' END AS brand_type FROM brand_comparison ORDER BY revenue_generated DESC LIMIT 100;

-- Query 22: 价格敏感度分析
WITH price_sensitivity AS (
    SELECT p.product_id, p.product_name, p.price, p.sale_price, c.category_name, b.brand_name, p.is_on_sale, COALESCE(SUM(oi.quantity), 0) AS total_units_sold, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue FROM products p LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id LEFT JOIN categories c ON p.category_id = c.category_id LEFT JOIN brands b ON p.brand_id = b.brand_id WHERE p.is_active = 1 GROUP BY p.product_id, p.product_name, p.price, p.sale_price, c.category_name, b.brand_name, p.is_on_sale
)
SELECT product_id, product_name, price, sale_price, category_name, brand_name, is_on_sale, total_units_sold, total_revenue, ROUND(COALESCE((price - COALESCE(sale_price, price)) / NULLIF(price, 0) * 100, 0), 2) AS discount_percent FROM price_sensitivity ORDER BY discount_percent DESC LIMIT 100;

-- Query 23: 多渠道销售分析
WITH channel_sales AS (
    SELECT COALESCE(o.source, 'unknown') AS sales_channel, COALESCE(o.channel, 'unknown') AS sales_medium, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(AVG(o.total_amount), 0) AS avg_order_value FROM orders o GROUP BY o.source, o.channel
),
channel_comparison AS (
    SELECT cs.*, ROUND(cs.total_revenue * 100.0 / (SELECT COALESCE(SUM(total_revenue), 1) FROM channel_sales), 2) AS revenue_share FROM channel_sales cs
)
SELECT sales_channel, sales_medium, total_orders, unique_customers, total_revenue, avg_order_value, revenue_share FROM channel_comparison ORDER BY total_revenue DESC LIMIT 100;

-- Query 24: 产品搜索分析
WITH product_search AS (
    SELECT p.product_id, p.product_name, p.sku, p.search_keywords, p.tags, p.view_count, p.wishlist_count, p.cart_count, p.order_count, COALESCE(SUM(oi.quantity), 0) AS total_units_sold FROM products p LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id WHERE p.is_active = 1 AND p.is_visible = 1 GROUP BY p.product_id, p.product_name, p.sku, p.search_keywords, p.tags, p.view_count, p.wishlist_count, p.cart_count, p.order_count
),
search_metrics AS (
    SELECT ps.*, CASE WHEN ps.order_count > 0 THEN ps.order_count * 100.0 / NULLIF(COALESCE(ps.view_count, 1), 0) ELSE 0 END AS conversion_rate_percent FROM product_search ps
)
SELECT product_id, product_name, sku, view_count, wishlist_count, cart_count, order_count, total_units_sold, ROUND(conversion_rate_percent, 2) AS conversion_rate_percent FROM search_metrics ORDER BY view_count DESC LIMIT 100;

-- Query 25: 用户行为漏斗分析
WITH user_funnel AS (
    SELECT u.user_id, u.username, u.created_at AS registration_date, MIN(o.order_date) AS first_order_date, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent, COUNT(DISTINCT r.review_id) AS total_reviews FROM users u LEFT JOIN orders o ON u.user_id = o.user_id LEFT JOIN reviews r ON u.user_id = r.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.created_at
),
funnel_stages AS (
    SELECT uf.*, CAST(julianday(COALESCE(uf.first_order_date, 'now')) - julianday(uf.registration_date) AS INTEGER) AS days_to_first_purchase, CASE WHEN uf.total_orders = 0 THEN '注册未购买' WHEN uf.total_orders >= 1 AND uf.total_reviews = 0 THEN '购买未评论' WHEN uf.total_orders >= 1 AND uf.total_reviews >= 1 THEN '购买并评论' END AS funnel_stage FROM user_funnel uf
)
SELECT user_id, username, registration_date, first_order_date, days_to_first_purchase, total_orders, total_spent, total_reviews, funnel_stage FROM funnel_stages ORDER BY days_to_first_purchase LIMIT 100;

-- Query 26: 订阅和复购分析
WITH subscription_analysis AS (
    SELECT u.user_id, u.username, u.loyalty_tier, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT strftime('%Y-%m', o.order_date)) AS months_with_purchases, COALESCE(SUM(o.total_amount), 0) AS total_spent, COALESCE(AVG(o.total_amount), 0) AS avg_order_value, CAST(julianday('now') - julianday(MAX(o.order_date)) AS INTEGER) AS days_since_last_purchase FROM users u LEFT JOIN orders o ON u.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.loyalty_tier
),
subscription_segmentation AS (
    SELECT sa.*, CASE WHEN sa.months_with_purchases >= 2 THEN '规律购买客户' WHEN sa.months_with_purchases >= 1 THEN '偶尔购买客户' ELSE '无购买' END AS subscription_type FROM subscription_analysis sa
)
SELECT user_id, username, loyalty_tier, total_orders, months_with_purchases, total_spent, avg_order_value, days_since_last_purchase, subscription_type FROM subscription_segmentation ORDER BY months_with_purchases DESC LIMIT 100;

-- Query 27: 多币种销售分析
WITH currency_sales AS (
    SELECT COALESCE(o.currency_code, 'USD') AS currency_code, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(SUM(o.base_total_amount), 0) AS base_revenue, COALESCE(AVG(o.exchange_rate), 1) AS avg_exchange_rate FROM orders o GROUP BY o.currency_code
),
currency_stats AS (
    SELECT cs.*, ROUND(cs.total_revenue * 100.0 / (SELECT COALESCE(SUM(total_revenue), 1) FROM currency_sales), 2) AS revenue_share FROM currency_sales cs
)
SELECT currency_code, total_orders, unique_customers, total_revenue, base_revenue, avg_exchange_rate, revenue_share FROM currency_stats ORDER BY total_revenue DESC LIMIT 100;

-- Query 28: 税务分析
WITH tax_analysis AS (
    SELECT COALESCE(o.tax_name, 'unknown') AS tax_name, COALESCE(o.tax_rate, 0) AS tax_rate, COALESCE(o.shipping_country, 'unknown') AS shipping_country, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.subtotal), 0) AS total_subtotal, COALESCE(SUM(o.tax_amount), 0) AS total_tax, COALESCE(SUM(o.shipping_amount), 0) AS total_shipping, COALESCE(SUM(o.shipping_tax_amount), 0) AS total_shipping_tax FROM orders o GROUP BY o.tax_name, o.tax_rate, o.shipping_country
),
tax_summary AS (
    SELECT ta.*, ROUND(COALESCE(ta.total_tax / NULLIF(ta.total_subtotal, 0) * 100, 0), 2) AS effective_tax_rate FROM tax_analysis ta
)
SELECT tax_name, tax_rate, shipping_country, total_orders, total_subtotal, total_tax, total_shipping, total_shipping_tax, effective_tax_rate FROM tax_summary ORDER BY total_tax DESC LIMIT 100;

-- Query 29: 产品变体分析
WITH variant_analysis AS (
    SELECT p.product_id, p.product_name, p.sku, p.has_options, COALESCE(SUM(oi.quantity), 0) AS total_units_sold, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue, COUNT(DISTINCT oi.variant_id) AS variant_count FROM products p LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id WHERE p.is_active = 1 GROUP BY p.product_id, p.product_name, p.sku, p.has_options
)
SELECT product_id, product_name, sku, has_options, variant_count, total_units_sold, total_revenue FROM variant_analysis ORDER BY total_revenue DESC LIMIT 100;

-- Query 30: 用户设备和平台分析
WITH device_analysis AS (
    SELECT COALESCE(o.customer_user_agent, 'unknown') AS customer_user_agent, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(AVG(o.total_amount), 0) AS avg_order_value FROM orders o GROUP BY o.customer_user_agent
)
SELECT customer_user_agent, total_orders, unique_customers, total_revenue, avg_order_value FROM device_analysis ORDER BY total_revenue DESC LIMIT 100;

-- Query 31: 售后服务分析
WITH after_sales AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, o.has_return, o.has_refund, o.has_exchange, o.refunded_amount, o.returned_item_count FROM orders o LEFT JOIN users u ON o.user_id = u.user_id
),
after_sales_stats AS (
    SELECT COUNT(DISTINCT order_id) AS total_orders, SUM(CASE WHEN COALESCE(has_return, 0) = 1 THEN 1 ELSE 0 END) AS return_count, SUM(CASE WHEN COALESCE(has_refund, 0) = 1 THEN 1 ELSE 0 END) AS refund_count, SUM(CASE WHEN COALESCE(has_exchange, 0) = 1 THEN 1 ELSE 0 END) AS exchange_count, COALESCE(SUM(refunded_amount), 0) AS total_refunded_amount FROM after_sales
)
SELECT order_id, order_number, username, order_date, has_return, has_refund, has_exchange, refunded_amount, returned_item_count FROM after_sales ORDER BY order_date DESC LIMIT 100;

-- Query 32: 广告渠道效果分析
WITH ad_channel_analysis AS (
    SELECT COALESCE(o.utm_source, 'organic') AS utm_source, COALESCE(o.utm_medium, 'unknown') AS utm_medium, COALESCE(o.utm_campaign, 'none') AS utm_campaign, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(AVG(o.total_amount), 0) AS avg_order_value FROM orders o GROUP BY o.utm_source, o.utm_medium, o.utm_campaign
),
channel_summary AS (
    SELECT aca.*, ROUND(aca.total_revenue * 100.0 / (SELECT COALESCE(SUM(total_revenue), 1) FROM ad_channel_analysis), 2) AS revenue_share FROM ad_channel_analysis aca
)
SELECT utm_source, utm_medium, utm_campaign, total_orders, unique_customers, total_revenue, avg_order_value, revenue_share FROM channel_summary ORDER BY total_revenue DESC LIMIT 100;

-- Query 33: 产品捆绑销售分析
WITH bundle_analysis AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, COUNT(DISTINCT oi.product_id) AS product_count_in_order, COUNT(DISTINCT c.category_id) AS category_count_in_order, COALESCE(SUM(oi.quantity), 0) AS total_items, COALESCE(SUM(o.total_amount), 0) AS total_amount FROM orders o LEFT JOIN users u ON o.user_id = u.user_id LEFT JOIN order_items oi ON o.order_id = oi.order_id LEFT JOIN products p ON oi.product_id = p.product_id LEFT JOIN categories c ON p.category_id = c.category_id GROUP BY o.order_id, o.order_number, u.username, o.order_date
)
SELECT order_id, order_number, username, order_date, product_count_in_order, category_count_in_order, total_items, total_amount FROM bundle_analysis ORDER BY product_count_in_order DESC LIMIT 100;

-- Query 34: 会员积分分析
WITH loyalty_analysis AS (
    SELECT u.user_id, u.username, u.loyalty_tier, u.loyalty_points, COALESCE(SUM(o.loyalty_points_used), 0) AS points_used, COALESCE(SUM(o.loyalty_points_earned), 0) AS points_earned, COALESCE(SUM(o.loyalty_points_discount), 0) AS points_discount, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent FROM users u LEFT JOIN orders o ON u.user_id = u.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.loyalty_tier, u.loyalty_points
),
loyalty_summary AS (
    SELECT la.*, la.loyalty_points + COALESCE(la.points_earned, 0) - COALESCE(la.points_used, 0) AS net_points FROM loyalty_analysis la
)
SELECT user_id, username, loyalty_tier, loyalty_points, points_used, points_earned, points_discount, net_points, total_orders, total_spent FROM loyalty_summary ORDER BY net_points DESC LIMIT 100;

-- Query 35: 产品关联分析
SELECT oi.product_id, p.product_name, COUNT(DISTINCT o.order_id) AS order_count,
    COALESCE(SUM(oi.quantity), 0) AS total_sold
FROM order_items oi
LEFT JOIN products p ON oi.product_id = p.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id
GROUP BY oi.product_id, p.product_name
ORDER BY order_count DESC LIMIT 100;

-- Query 36: 库存成本分析
SELECT i.product_id, COALESCE(p.product_name, 'Unknown Product') AS product_name, COALESCE(p.sku, 'N/A') AS sku, i.quantity, i.available_quantity, 
    i.average_cost, i.last_cost, i.standard_cost, i.inventory_value,
    (i.quantity * COALESCE(i.average_cost, 0)) AS calculated_inventory_value
FROM inventory i
LEFT JOIN products p ON i.product_id = p.product_id
ORDER BY calculated_inventory_value DESC LIMIT 100;

-- Query 37: 用户活跃度分析
WITH user_activity AS (
    SELECT u.user_id, u.username, u.created_at, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent, MAX(o.order_date) AS last_order_date, CAST(julianday('now') - julianday(MAX(o.order_date)) AS INTEGER) AS days_since_last_order, COUNT(DISTINCT r.review_id) AS total_reviews FROM users u LEFT JOIN orders o ON u.user_id = o.user_id LEFT JOIN reviews r ON u.user_id = r.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.created_at
),
activity_segmentation AS (
    SELECT ua.*, CASE WHEN ua.days_since_last_order <= 30 THEN '活跃' WHEN ua.days_since_last_order <= 90 THEN '近期活跃' WHEN ua.days_since_last_order <= 180 THEN '沉默' ELSE '流失' END AS activity_status FROM user_activity ua
)
SELECT user_id, username, created_at, total_orders, total_spent, last_order_date, days_since_last_order, total_reviews, activity_status FROM activity_segmentation ORDER BY days_since_last_order LIMIT 100;

-- Query 38: 产品退货原因分析
WITH return_reason_analysis AS (
    SELECT COALESCE(oi.return_reason, 'unknown') AS return_reason, p.product_id, p.product_name, c.category_name, COUNT(DISTINCT oi.order_item_id) AS return_count, COALESCE(SUM(oi.quantity_returned), 0) AS total_returned_quantity, COALESCE(SUM(oi.refund_amount), 0) AS total_refund_amount FROM order_items oi LEFT JOIN products p ON oi.product_id = p.product_id LEFT JOIN categories c ON p.category_id = c.category_id GROUP BY oi.return_reason, p.product_id, p.product_name, c.category_name
)
SELECT return_reason, product_id, product_name, category_name, return_count, total_returned_quantity, total_refund_amount FROM return_reason_analysis ORDER BY return_count DESC LIMIT 100;

-- Query 39: 跨境销售分析
WITH cross_border_analysis AS (
    SELECT COALESCE(o.shipping_country, 'unknown') AS shipping_country, COALESCE(o.billing_country, 'unknown') AS billing_country, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(AVG(o.total_amount), 0) AS avg_order_value, COALESCE(SUM(o.shipping_amount), 0) AS total_shipping FROM orders o GROUP BY o.shipping_country, o.billing_country
)
SELECT shipping_country, billing_country, total_orders, unique_customers, total_revenue, avg_order_value, total_shipping FROM cross_border_analysis ORDER BY total_revenue DESC LIMIT 100;

-- Query 40: 促销活动ROI分析
WITH promotion_roi AS (
    SELECT COALESCE(o.coupon_code, 'no_coupon') AS coupon_code, COUNT(DISTINCT o.order_id) AS total_orders, COUNT(DISTINCT o.user_id) AS unique_customers, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(SUM(o.discount_amount), 0) AS total_discounts, COALESCE(SUM(o.subtotal), 0) AS total_subtotal FROM orders o GROUP BY o.coupon_code
),
roi_calculation AS (
    SELECT pr.*, ROUND(COALESCE((pr.total_revenue - pr.total_discounts) * 100.0 / NULLIF(pr.total_discounts, 1), 0), 2) AS roi_percent, ROUND(COALESCE(pr.total_discounts / NULLIF(pr.total_subtotal, 1) * 100, 0), 2) AS discount_rate FROM promotion_roi pr
)
SELECT coupon_code, total_orders, unique_customers, total_revenue, total_discounts, total_subtotal, roi_percent, discount_rate FROM roi_calculation ORDER BY roi_percent DESC LIMIT 100;

-- Query 41: 用户流失预测分析
WITH churn_analysis AS (
    SELECT u.user_id, u.username, u.created_at, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent, MAX(o.order_date) AS last_order_date, CAST(julianday('now') - julianday(MAX(o.order_date)) AS INTEGER) AS days_since_last_purchase, COALESCE(AVG(o.total_amount), 0) AS avg_order_value FROM users u LEFT JOIN orders o ON u.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.created_at
),
churn_risk AS (
    SELECT ca.*, CASE WHEN ca.days_since_last_purchase >= 90 THEN '高风险流失' WHEN ca.days_since_last_purchase >= 30 THEN '中等风险' WHEN ca.days_since_last_purchase >= 7 THEN '低风险' ELSE '活跃' END AS churn_risk_level FROM churn_analysis ca
)
SELECT user_id, username, created_at, total_orders, total_spent, last_order_date, days_since_last_purchase, avg_order_value, churn_risk_level FROM churn_risk ORDER BY days_since_last_purchase DESC LIMIT 100;

-- Query 42: 产品发布效果分析
WITH product_launch AS (
    SELECT p.product_id, p.product_name, p.sku, p.created_at AS launch_date, p.published_at, p.is_new, p.is_featured, COUNT(DISTINCT oi.order_item_id) AS total_units_sold, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue FROM products p LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id WHERE p.is_active = 1 GROUP BY p.product_id, p.product_name, p.sku, p.created_at, p.published_at, p.is_new, p.is_featured
),
launch_metrics AS (
    SELECT pl.*, CAST(julianday('now') - julianday(pl.launch_date) AS INTEGER) AS days_since_launch FROM product_launch pl
)
SELECT product_id, product_name, sku, launch_date, published_at, is_new, is_featured, days_since_launch, total_units_sold, total_revenue FROM launch_metrics ORDER BY days_since_launch LIMIT 100;

-- Query 43: 支付风险分析
WITH payment_risk AS (
    SELECT p.payment_id, p.order_id, o.order_number, u.username, p.payment_method, p.status, p.risk_score, p.risk_level, p.amount, p.failure_code, p.failure_reason FROM payments p LEFT JOIN orders o ON p.order_id = o.order_id LEFT JOIN users u ON p.user_id = u.user_id
)
SELECT payment_id, order_id, order_number, username, payment_method, status, risk_score, risk_level, amount, failure_code, failure_reason FROM payment_risk ORDER BY COALESCE(risk_score, 0) DESC LIMIT 100;

-- Query 44: 用户客单价分析
WITH avg_order_value_analysis AS (
    SELECT u.user_id, u.username, u.loyalty_tier, u.country, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent, COALESCE(AVG(o.total_amount), 0) AS avg_order_value, COALESCE(MIN(o.total_amount), 0) AS min_order_value, COALESCE(MAX(o.total_amount), 0) AS max_order_value FROM users u LEFT JOIN orders o ON u.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.loyalty_tier, u.country
),
aov_segmentation AS (
    SELECT aoa.*, CASE WHEN aoa.avg_order_value >= 100 THEN '高客单价' WHEN aoa.avg_order_value >= 20 THEN '中等客单价' ELSE '低客单价' END AS aov_segment FROM avg_order_value_analysis aoa
)
SELECT user_id, username, loyalty_tier, country, total_orders, total_spent, avg_order_value, min_order_value, max_order_value, aov_segment FROM aov_segmentation ORDER BY avg_order_value DESC LIMIT 100;

-- Query 45: 产品评价热度分析
WITH review_popularity AS (
    SELECT p.product_id, p.product_name, p.sku, c.category_name, COUNT(DISTINCT r.review_id) AS total_reviews, COALESCE(AVG(r.rating), 0) AS avg_rating, COALESCE(SUM(r.helpful_count), 0) AS total_helpful_votes, COALESCE(SUM(r.recommended), 0) AS recommended_count, MAX(r.review_date) AS last_review_date FROM products p LEFT JOIN reviews r ON p.product_id = r.product_id LEFT JOIN categories c ON p.category_id = c.category_id WHERE p.is_active = 1 GROUP BY p.product_id, p.product_name, p.sku, c.category_name
),
popularity_ranking AS (
    SELECT rp.*, RANK() OVER (ORDER BY total_reviews DESC) AS review_rank, RANK() OVER (ORDER BY avg_rating DESC) AS rating_rank FROM review_popularity rp
)
SELECT product_id, product_name, sku, category_name, total_reviews, avg_rating, total_helpful_votes, recommended_count, last_review_date, review_rank, rating_rank FROM popularity_ranking ORDER BY total_reviews DESC LIMIT 100;

-- Query 46: 促销活动时间分析
WITH promotion_timing AS (
    SELECT DATE(o.order_date) AS promotion_date, strftime('%Y-%m', o.order_date) AS promotion_month, strftime('%w', o.order_date) AS day_of_week, COUNT(DISTINCT o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_revenue, COALESCE(SUM(o.discount_amount), 0) AS total_discounts, COUNT(DISTINCT CASE WHEN o.coupon_code IS NOT NULL THEN o.order_id END) AS coupon_orders FROM orders o GROUP BY DATE(o.order_date), strftime('%Y-%m', o.order_date), strftime('%w', o.order_date)
)
SELECT promotion_date, promotion_month, day_of_week, total_orders, total_revenue, total_discounts, coupon_orders, ROUND(COALESCE(total_discounts / NULLIF(total_revenue, 1) * 100, 0), 2) AS discount_percent FROM promotion_timing ORDER BY promotion_date LIMIT 100;

-- Query 47: 订单取消原因分析
WITH cancellation_analysis AS (
    SELECT o.order_id, o.order_number, u.username, o.order_date, o.order_status, COALESCE(o.cancel_reason, 'unknown') AS cancel_reason, o.total_amount, o.payment_status FROM orders o LEFT JOIN users u ON o.user_id = u.user_id
)
SELECT order_id, order_number, username, order_date, order_status, cancel_reason, total_amount, payment_status FROM cancellation_analysis ORDER BY order_date DESC LIMIT 100;

-- Query 48: 用户推荐效果分析
WITH referral_effectiveness AS (
    SELECT u.user_id AS referrer_id, u.username AS referrer_name, COUNT(DISTINCT ref.user_id) AS total_referrals, COUNT(DISTINCT o.order_id) AS referred_orders, COALESCE(SUM(o.total_amount), 0) AS referred_revenue FROM users u LEFT JOIN users ref ON u.user_id = ref.referred_by_user_id LEFT JOIN orders o ON ref.user_id = o.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username
),
referral_summary AS (
    SELECT re.*, CASE WHEN re.total_referrals > 0 THEN re.referred_orders * 100.0 / re.total_referrals ELSE 0 END AS conversion_rate FROM referral_effectiveness re
)
SELECT referrer_id, referrer_name, total_referrals, referred_orders, referred_revenue, ROUND(conversion_rate, 2) AS conversion_rate_percent FROM referral_summary ORDER BY referred_revenue DESC LIMIT 100;

-- Query 49: 产品库存预警分析
SELECT i.product_id, COALESCE(p.product_name, 'Unknown Product') AS product_name, COALESCE(p.sku, 'N/A') AS sku, i.quantity, i.available_quantity,
    i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,
    (COALESCE(i.maximum_quantity, 100) - i.available_quantity) AS quantity_needed,
    CASE WHEN i.available_quantity <= COALESCE(i.reorder_threshold, 10) THEN '紧急' 
         WHEN i.available_quantity <= COALESCE(i.minimum_quantity, 20) THEN '高' 
         WHEN i.available_quantity <= COALESCE(i.maximum_quantity, 100) * 0.5 THEN '中' 
         ELSE '低' END AS alert_level 
FROM inventory i
LEFT JOIN products p ON i.product_id = p.product_id
ORDER BY CASE alert_level WHEN '紧急' THEN 1 WHEN '高' THEN 2 WHEN '中' THEN 3 ELSE 4 END LIMIT 100;

-- Query 50: 全渠道客户互动分析
WITH customer_interactions AS (
    SELECT u.user_id, u.username, u.email, u.created_at, COUNT(DISTINCT o.order_id) AS order_count, COALESCE(SUM(o.total_amount), 0) AS total_spent, COUNT(DISTINCT r.review_id) AS review_count, COUNT(DISTINCT p.payment_id) AS payment_count, COUNT(DISTINCT s.shipping_id) AS shipment_count FROM users u LEFT JOIN orders o ON u.user_id = o.user_id LEFT JOIN reviews r ON u.user_id = r.user_id LEFT JOIN payments p ON u.user_id = p.user_id LEFT JOIN shipping s ON u.user_id = s.user_id WHERE u.is_active = 1 GROUP BY u.user_id, u.username, u.email, u.created_at
),
interaction_segmentation AS (
    SELECT ci.*, (ci.order_count + ci.review_count + ci.payment_count + ci.shipment_count) AS total_interactions, CASE WHEN (ci.order_count + ci.review_count + ci.payment_count + ci.shipment_count) >= 5 THEN '高互动' WHEN (ci.order_count + ci.review_count + ci.payment_count + ci.shipment_count) >= 2 THEN '中等互动' WHEN (ci.order_count + ci.review_count + ci.payment_count + ci.shipment_count) >= 1 THEN '低互动' ELSE '无互动' END AS interaction_level FROM customer_interactions ci
)
SELECT user_id, username, email, created_at, order_count, total_spent, review_count, payment_count, shipment_count, total_interactions, interaction_level FROM interaction_segmentation WHERE 5>1 ORDER BY total_interactions DESC LIMIT 100;

-- Query 51: 无需优化
SELECT count(*) FROM users

-- Query 52: 恒等常量
SELECT * FROM users WHERE 1=1 AND 5>3

-- Query 53: 冗余查询
SELECT count(1) FROM (SELECT * FROM users WHERE 3>2 order by username desc)

-- Query 54: group by 冗余
SELECT count(1) FROM (SELECT * FROM users GROUP BY user_id, username, email ORDER BY username desc)

-- Query 55: 删除排序
SELECT count(1) FROM (SELECT username, email FROM users GROUP BY city ORDER BY username desc)