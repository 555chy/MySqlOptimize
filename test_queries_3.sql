-- Test SQL Queries for SQL Parser (3 queries for quick testing)
-- Optimized for SQLite syntax

-- Query 1: User Lifetime Value and Purchase Behavior Analysis
WITH user_purchases AS (
    SELECT
        u.user_id,
        u.username,
        u.email,
        u.created_at AS registration_date,
        u.city,
        u.state_province,
        u.loyalty_tier AS tier,
        COUNT(o.order_id) AS total_orders,
        SUM(o.total_amount) AS total_spent,
        AVG(o.total_amount) AS avg_order_value,
        SUM(o.total_amount) / NULLIF(COUNT(o.order_id), 0) AS avg_ltv_per_order,
        MAX(o.order_date) AS last_purchase_date,
        MIN(o.order_date) AS first_purchase_date,
        CAST(julianday('now') - julianday(MAX(o.order_date)) AS INTEGER) AS days_since_last_purchase,
        CAST(julianday(MAX(o.order_date)) - julianday(MIN(o.order_date)) AS INTEGER) AS customer_tenure_days
    FROM users u
    INNER JOIN orders o ON u.user_id = o.user_id
    INNER JOIN order_items oi ON o.order_id = oi.order_id
    INNER JOIN products p ON oi.product_id = p.product_id
    INNER JOIN categories c ON p.category_id = c.category_id
    WHERE o.order_status IN ('pending', 'processing', 'shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-365 days')
        AND u.is_active = 1
    GROUP BY u.user_id, u.username, u.email, u.created_at, u.city, u.state_province, u.loyalty_tier
),
user_segments AS (
    SELECT
        up.*,
        CASE
            WHEN days_since_last_purchase <= 30 THEN 'Highly Active'
            WHEN days_since_last_purchase <= 60 THEN 'Moderately Active'
            WHEN days_since_last_purchase <= 90 THEN 'At Risk'
            WHEN days_since_last_purchase <= 180 THEN 'Dormant'
            ELSE 'Churned'
        END AS activity_segment,
        CASE
            WHEN total_spent >= 10000 THEN 'Platinum'
            WHEN total_spent >= 5000 THEN 'Gold'
            WHEN total_spent >= 1000 THEN 'Silver'
            WHEN total_spent >= 500 THEN 'Bronze'
            ELSE 'Basic'
        END AS value_tier
    FROM user_purchases up
)
SELECT
    us.user_id,
    us.username,
    us.email,
    us.city,
    us.state_province,
    us.tier,
    us.total_orders,
    us.total_spent,
    us.avg_order_value,
    us.avg_ltv_per_order,
    us.last_purchase_date,
    us.first_purchase_date,
    us.days_since_last_purchase,
    us.customer_tenure_days,
    us.activity_segment,
    us.value_tier
FROM user_segments us
WHERE us.total_orders > 0
    AND us.total_spent > 0
    AND us.avg_order_value IS NOT NULL
    AND us.days_since_last_purchase IS NOT NULL
HAVING us.total_spent >= 100
ORDER BY us.total_spent DESC, us.total_orders DESC;

-- Query 2: Product Performance and Category Analysis
WITH product_metrics AS (
    SELECT
        p.product_id,
        p.product_name,
        p.sku,
        p.price,
        p.cost_price AS cost,
        p.stock_quantity,
        p.reorder_threshold AS reorder_point,
        p.category_id,
        c.category_name,
        c.parent_category_id,
        pc.category_name AS parent_category_name,
        b.brand_id,
        b.brand_name,
        COUNT(DISTINCT oi.order_id) AS order_count,
        SUM(oi.quantity) AS total_quantity_sold,
        SUM(oi.quantity * oi.unit_price) AS gross_revenue,
        SUM(oi.quantity * p.cost_price) AS total_cost,
        SUM(oi.quantity * (oi.unit_price - p.cost_price)) AS gross_profit,
        AVG(oi.unit_price) AS avg_selling_price,
        MIN(oi.unit_price) AS min_selling_price,
        MAX(oi.unit_price) AS max_selling_price,
        COUNT(DISTINCT o.user_id) AS unique_customers,
        MAX(o.order_date) AS last_sale_date,
        MIN(o.order_date) AS first_sale_date
    FROM products p
    INNER JOIN categories c ON p.category_id = c.category_id
    LEFT JOIN categories pc ON c.parent_category_id = pc.category_id
    INNER JOIN brands b ON p.brand_id = b.brand_id
    INNER JOIN order_items oi ON p.product_id = oi.product_id
    INNER JOIN orders o ON oi.order_id = o.order_id
    WHERE o.order_status IN ('pending', 'processing', 'shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-90 days')
        AND p.is_active = 1
    GROUP BY p.product_id, p.product_name, p.sku, p.price, p.cost_price, p.stock_quantity,
             p.reorder_threshold, p.category_id, c.category_name, c.parent_category_id,
             pc.category_name, b.brand_id, b.brand_name
),
product_rankings AS (
    SELECT
        pm.*,
        CASE
            WHEN pm.stock_quantity <= pm.reorder_point THEN 'Low Stock'
            WHEN pm.stock_quantity <= pm.reorder_point * 2 THEN 'Medium Stock'
            WHEN pm.stock_quantity <= pm.reorder_point * 5 THEN 'Healthy Stock'
            ELSE 'Overstocked'
        END AS stock_status,
        CASE
            WHEN pm.gross_profit / NULLIF(pm.gross_revenue, 0) >= 0.5 THEN 'High Margin'
            WHEN pm.gross_profit / NULLIF(pm.gross_revenue, 0) >= 0.3 THEN 'Medium Margin'
            WHEN pm.gross_profit / NULLIF(pm.gross_revenue, 0) >= 0.15 THEN 'Low Margin'
            ELSE 'Below Margin Threshold'
        END AS margin_category
    FROM product_metrics pm
)
SELECT
    pr.product_id,
    pr.product_name,
    pr.sku,
    pr.price,
    pr.cost,
    pr.stock_quantity,
    pr.reorder_point,
    pr.category_name,
    pr.parent_category_name,
    pr.brand_name,
    pr.order_count,
    pr.total_quantity_sold,
    pr.gross_revenue,
    pr.gross_profit,
    pr.avg_selling_price,
    pr.min_selling_price,
    pr.max_selling_price,
    pr.unique_customers,
    pr.last_sale_date,
    pr.first_sale_date,
    pr.stock_status,
    pr.margin_category
FROM product_rankings pr
WHERE pr.order_count > 0
    AND pr.gross_revenue > 0
    AND pr.gross_profit IS NOT NULL
    AND pr.unique_customers > 0
HAVING pr.gross_revenue >= 1000
ORDER BY pr.gross_revenue DESC, pr.order_count DESC;

-- Query 3: Order Fulfillment and Delivery Performance Analysis
WITH order_fulfillment AS (
    SELECT
        o.order_id,
        o.order_number,
        o.user_id,
        u.username,
        u.email,
        o.shipping_city AS customer_city,
        o.shipping_state_province AS customer_region,
        o.order_date,
        o.status_updated_at AS shipped_date,
        o.completed_at AS delivered_date,
        o.order_status,
        o.total_amount,
        o.shipping_amount,
        o.tax_amount,
        o.discount_amount,
        o.coupon_code AS payment_method,
        o.payment_status,
        o.shipping_method_name AS shipping_method,
        o.tracking_number,
        CAST(julianday(o.status_updated_at) - julianday(o.order_date) AS INTEGER) AS processing_time_days,
        CAST(julianday(o.completed_at) - julianday(o.status_updated_at) AS INTEGER) AS shipping_time_days,
        CAST(julianday(o.completed_at) - julianday(o.order_date) AS INTEGER) AS total_delivery_time_days,
        CASE
            WHEN o.order_status = 'delivered' THEN CAST(julianday(o.completed_at) - julianday(o.order_date) AS INTEGER)
            WHEN o.order_status = 'shipped' THEN CAST(julianday('now') - julianday(o.order_date) AS INTEGER)
            ELSE CAST(julianday('now') - julianday(o.order_date) AS INTEGER)
        END AS actual_delivery_days,
        CASE
            WHEN o.shipping_method_name LIKE '%express%' THEN 2
            WHEN o.shipping_method_name LIKE '%priority%' THEN 3
            WHEN o.shipping_method_name LIKE '%standard%' THEN 5
            WHEN o.shipping_method_name LIKE '%economy%' THEN 7
            ELSE 5
        END AS promised_delivery_days,
        CAST(julianday('now') - julianday(o.order_date) AS INTEGER) AS days_since_order,
        COUNT(oi.order_item_id) AS item_count,
        SUM(oi.quantity) AS total_items,
        SUM(oi.quantity * oi.unit_price) AS item_subtotal
    FROM orders o
    INNER JOIN users u ON o.user_id = u.user_id
    INNER JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.order_date >= date('now', '-180 days')
        AND o.order_status IN ('pending', 'processing', 'shipped', 'delivered', 'completed')
    GROUP BY o.order_id, o.order_number, o.user_id, u.username, u.email, o.shipping_city, o.shipping_state_province,
             o.order_date, o.status_updated_at, o.completed_at, o.order_status, o.total_amount,
             o.shipping_amount, o.tax_amount, o.discount_amount, o.coupon_code, o.payment_status,
             o.shipping_method_name, o.tracking_number
),
fulfillment_metrics AS (
    SELECT
        of1.*,
        CASE
            WHEN of1.processing_time_days <= 1 THEN 'Same Day Processing'
            WHEN of1.processing_time_days <= 2 THEN 'Next Day Processing'
            WHEN of1.processing_time_days <= 3 THEN 'Fast Processing'
            WHEN of1.processing_time_days <= 5 THEN 'Normal Processing'
            ELSE 'Delayed Processing'
        END AS processing_performance,
        CASE
            WHEN of1.shipping_time_days <= of1.promised_delivery_days - 1 THEN 'Early Delivery'
            WHEN of1.shipping_time_days <= of1.promised_delivery_days THEN 'On Time Delivery'
            WHEN of1.shipping_time_days <= of1.promised_delivery_days + 1 THEN 'Slightly Late'
            WHEN of1.shipping_time_days <= of1.promised_delivery_days + 3 THEN 'Late Delivery'
            ELSE 'Very Late Delivery'
        END AS delivery_performance,
        CASE
            WHEN of1.actual_delivery_days <= of1.promised_delivery_days THEN 'Within Promise'
            WHEN of1.actual_delivery_days <= of1.promised_delivery_days + 2 THEN 'Slightly Exceeded Promise'
            ELSE 'Significantly Exceeded Promise'
        END AS promise_adherence,
        AVG(of1.processing_time_days) OVER () AS avg_processing_time,
        AVG(of1.shipping_time_days) OVER () AS avg_shipping_time,
        AVG(of1.total_amount) OVER () AS avg_order_value
    FROM order_fulfillment of1
)
SELECT
    fm.order_id,
    fm.order_number,
    fm.username,
    fm.email,
    fm.customer_city,
    fm.customer_region,
    fm.order_date,
    fm.shipped_date,
    fm.delivered_date,
    fm.order_status,
    fm.total_amount,
    fm.shipping_amount,
    fm.tax_amount,
    fm.discount_amount,
    fm.payment_method,
    fm.payment_status,
    fm.shipping_method,
    fm.tracking_number,
    fm.processing_time_days,
    fm.shipping_time_days,
    fm.total_delivery_time_days,
    fm.actual_delivery_days,
    fm.promised_delivery_days,
    fm.days_since_order,
    fm.item_count,
    fm.total_items,
    fm.item_subtotal,
    fm.processing_performance,
    fm.delivery_performance,
    fm.promise_adherence,
    fm.avg_processing_time,
    fm.avg_shipping_time,
    fm.avg_order_value
FROM fulfillment_metrics fm
WHERE fm.order_date IS NOT NULL
    AND fm.total_amount > 0
    AND fm.item_count > 0
    AND fm.user_id IS NOT NULL
HAVING fm.total_amount >= 10
ORDER BY fm.order_date DESC, fm.total_amount DESC;
