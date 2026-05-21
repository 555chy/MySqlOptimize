-- Test SQL Queries for SQL Parser (10 complex queries for comprehensive testing)
-- Optimized for SQLite syntax based on actual ecommerce schema
-- Each query contains at least 100 keywords

-- Query 1: Customer Lifetime Value Analysis with Purchase Patterns
WITH user_purchase_history AS (
    SELECT
        u.user_id,
        u.username,
        u.email,
        u.city,
        u.state_province,
        u.country,
        u.loyalty_tier,
        u.created_at AS registration_date,
        COUNT(DISTINCT o.order_id) AS total_orders,
        SUM(o.total_amount) AS total_spent,
        AVG(o.total_amount) AS avg_order_value,
        MAX(o.order_date) AS last_purchase_date,
        MIN(o.order_date) AS first_purchase_date,
        CAST(julianday('now') - julianday(MAX(o.order_date)) AS INTEGER) AS days_since_last_purchase,
        CAST(julianday(MAX(o.order_date)) - julianday(MIN(o.order_date)) AS INTEGER) AS customer_tenure_days,
        COUNT(DISTINCT p.category_id) AS unique_categories_purchased,
        COUNT(DISTINCT b.brand_id) AS unique_brands_purchased,
        SUM(oi.quantity) AS total_items_purchased,
        SUM(o.discount_amount) AS total_discounts_received,
        AVG(o.shipping_amount) AS avg_shipping_cost,
        SUM(o.tax_amount) AS total_tax_paid,
        SUM(o.loyalty_points_used) AS loyalty_points_redeemed,
        SUM(o.loyalty_points_earned) AS loyalty_points_earned
    FROM users u
    INNER JOIN orders o ON u.user_id = o.user_id
    INNER JOIN order_items oi ON o.order_id = oi.order_id
    INNER JOIN products p ON oi.product_id = p.product_id
    INNER JOIN brands b ON p.brand_id = b.brand_id
    WHERE o.order_status IN ('shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-730 days')
        AND u.is_active = 1
    GROUP BY u.user_id, u.username, u.email, u.city, u.state_province, u.country, u.loyalty_tier, u.created_at
),
customer_segments AS (
    SELECT
        uph.*,
        CASE
            WHEN days_since_last_purchase <= 30 THEN 'Highly Active'
            WHEN days_since_last_purchase <= 60 THEN 'Moderately Active'
            WHEN days_since_last_purchase <= 90 THEN 'At Risk'
            WHEN days_since_last_purchase <= 180 THEN 'Dormant'
            ELSE 'Churned'
        END AS engagement_segment,
        CASE
            WHEN total_spent >= 10000 THEN 'Platinum'
            WHEN total_spent >= 5000 THEN 'Gold'
            WHEN total_spent >= 1000 THEN 'Silver'
            WHEN total_spent >= 500 THEN 'Bronze'
            ELSE 'Basic'
        END AS spending_tier,
        CASE
            WHEN total_orders >= 50 THEN 'Frequent Shopper'
            WHEN total_orders >= 20 THEN 'Regular Shopper'
            WHEN total_orders >= 5 THEN 'Occasional Shopper'
            ELSE 'Rare Shopper'
        END AS purchase_frequency_segment
    FROM user_purchase_history uph
),
monthly_recency AS (
    SELECT
        cs.user_id,
        COUNT(DISTINCT strftime('%Y-%m', o.order_date)) AS months_active,
        ROUND(COUNT(DISTINCT strftime('%Y-%m', o.order_date)) / (julianday('now') - julianday(cs.first_purchase_date)) * 30, 2) AS recency_score
    FROM customer_segments cs
    INNER JOIN orders o ON cs.user_id = o.user_id
    WHERE o.order_status IN ('shipped', 'delivered', 'completed')
    GROUP BY cs.user_id
)
SELECT
    cs.user_id,
    cs.username,
    cs.email,
    cs.city,
    cs.state_province,
    cs.country,
    cs.loyalty_tier,
    cs.registration_date,
    cs.total_orders,
    cs.total_spent,
    cs.avg_order_value,
    cs.last_purchase_date,
    cs.first_purchase_date,
    cs.days_since_last_purchase,
    cs.customer_tenure_days,
    cs.unique_categories_purchased,
    cs.unique_brands_purchased,
    cs.total_items_purchased,
    cs.total_discounts_received,
    cs.avg_shipping_cost,
    cs.total_tax_paid,
    cs.loyalty_points_redeemed,
    cs.loyalty_points_earned,
    cs.engagement_segment,
    cs.spending_tier,
    cs.purchase_frequency_segment,
    mr.months_active,
    mr.recency_score,
    ROUND(cs.total_spent / NULLIF(cs.total_orders, 0), 2) AS avg_order_value_ratio,
    ROUND(cs.total_discounts_received / NULLIF(cs.total_spent, 0) * 100, 2) AS discount_rate,
    ROUND((cs.loyalty_points_earned - cs.loyalty_points_redeemed) / NULLIF(cs.total_spent, 0) * 100, 2) AS net_loyalty_rate
FROM customer_segments cs
LEFT JOIN monthly_recency mr ON cs.user_id = mr.user_id
WHERE cs.total_spent > 0
    AND cs.total_orders > 0
    AND cs.first_purchase_date IS NOT NULL
ORDER BY cs.total_spent DESC, cs.total_orders DESC;

-- Query 2: Product Performance Analytics with Category and Brand Insights
WITH product_sales_metrics AS (
    SELECT
        p.product_id,
        p.product_name,
        p.sku,
        p.price,
        p.cost_price,
        p.stock_quantity,
        p.category_id,
        c.category_name,
        c.parent_category_id,
        pc.category_name AS parent_category_name,
        p.brand_id,
        b.brand_name,
        b.is_premium AS brand_is_premium,
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
        MIN(o.order_date) AS first_sale_date,
        SUM(oi.quantity * oi.unit_price) / NULLIF(COUNT(DISTINCT o.order_id), 0) AS avg_revenue_per_order,
        AVG(oi.quantity) AS avg_quantity_per_order,
        COUNT(DISTINCT strftime('%Y-%m', o.order_date)) AS months_with_sales
    FROM products p
    INNER JOIN categories c ON p.category_id = c.category_id
    LEFT JOIN categories pc ON c.parent_category_id = pc.category_id
    INNER JOIN brands b ON p.brand_id = b.brand_id
    INNER JOIN order_items oi ON p.product_id = oi.product_id
    INNER JOIN orders o ON oi.order_id = o.order_id
    WHERE o.order_status IN ('shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-365 days')
        AND p.is_active = 1
    GROUP BY p.product_id, p.product_name, p.sku, p.price, p.cost_price, p.stock_quantity, p.category_id, c.category_name, c.parent_category_id, pc.category_name, p.brand_id, b.brand_name, b.is_premium
),
product_rankings AS (
    SELECT
        psm.*,
        ROUND(psm.gross_profit / NULLIF(psm.gross_revenue, 0) * 100, 2) AS profit_margin_percent,
        ROUND(psm.gross_revenue / NULLIF(psm.total_quantity_sold, 0), 2) AS avg_revenue_per_unit,
        ROUND(psm.gross_profit / NULLIF(psm.total_quantity_sold, 0), 2) AS avg_profit_per_unit,
        CASE
            WHEN psm.stock_quantity <= 10 THEN 'Low Stock'
            WHEN psm.stock_quantity <= 50 THEN 'Medium Stock'
            WHEN psm.stock_quantity <= 100 THEN 'Healthy Stock'
            ELSE 'Overstocked'
        END AS stock_status,
        CASE
            WHEN psm.gross_profit / NULLIF(psm.gross_revenue, 0) >= 0.5 THEN 'High Margin'
            WHEN psm.gross_profit / NULLIF(psm.gross_revenue, 0) >= 0.3 THEN 'Medium Margin'
            WHEN psm.gross_profit / NULLIF(psm.gross_revenue, 0) >= 0.15 THEN 'Low Margin'
            ELSE 'Below Threshold'
        END AS margin_category,
        RANK() OVER (PARTITION BY psm.category_id ORDER BY psm.gross_revenue DESC) AS category_rank,
        RANK() OVER (PARTITION BY psm.brand_id ORDER BY psm.gross_profit DESC) AS brand_rank,
        RANK() OVER (ORDER BY psm.gross_revenue DESC) AS overall_rank
    FROM product_sales_metrics psm
),
category_aggregates AS (
    SELECT
        pr.category_id,
        pr.category_name,
        COUNT(DISTINCT pr.product_id) AS product_count,
        SUM(pr.gross_revenue) AS category_total_revenue,
        SUM(pr.gross_profit) AS category_total_profit,
        SUM(pr.total_quantity_sold) AS category_total_units_sold,
        AVG(pr.profit_margin_percent) AS category_avg_margin
    FROM product_rankings pr
    GROUP BY pr.category_id, pr.category_name
)
SELECT
    pr.product_id,
    pr.product_name,
    pr.sku,
    pr.price,
    pr.cost_price,
    pr.category_name,
    pr.parent_category_name,
    pr.brand_name,
    pr.brand_is_premium,
    pr.order_count,
    pr.total_quantity_sold,
    pr.gross_revenue,
    pr.total_cost,
    pr.gross_profit,
    pr.profit_margin_percent,
    pr.avg_selling_price,
    pr.min_selling_price,
    pr.max_selling_price,
    pr.avg_revenue_per_unit,
    pr.avg_profit_per_unit,
    pr.unique_customers,
    pr.last_sale_date,
    pr.first_sale_date,
    pr.avg_revenue_per_order,
    pr.avg_quantity_per_order,
    pr.months_with_sales,
    pr.stock_status,
    pr.margin_category,
    pr.category_rank,
    pr.brand_rank,
    pr.overall_rank,
    ca.category_total_revenue,
    ca.category_total_profit,
    ca.category_avg_margin,
    ROUND(pr.gross_revenue / NULLIF(ca.category_total_revenue, 0) * 100, 2) AS category_revenue_share_percent
FROM product_rankings pr
LEFT JOIN category_aggregates ca ON pr.category_id = ca.category_id
WHERE pr.order_count > 0
    AND pr.gross_revenue > 0
ORDER BY pr.gross_revenue DESC, pr.order_count DESC;

-- Query 3: Order Fulfillment and Delivery Performance Dashboard
WITH order_fulfillment_details AS (
    SELECT
        o.order_id,
        o.order_number,
        o.user_id,
        u.username,
        u.email,
        o.shipping_city,
        o.shipping_state_province,
        o.shipping_country,
        o.order_date,
        o.status_updated_at AS shipped_date,
        o.completed_at AS delivered_date,
        o.order_status,
        o.fulfillment_status,
        o.payment_status,
        o.total_amount,
        o.shipping_amount,
        o.tax_amount,
        o.discount_amount,
        o.shipping_method_name,
        o.tracking_number,
        CAST(julianday(o.status_updated_at) - julianday(o.order_date) AS INTEGER) AS processing_time_days,
        CAST(julianday(o.completed_at) - julianday(o.status_updated_at) AS INTEGER) AS shipping_time_days,
        CAST(julianday(o.completed_at) - julianday(o.order_date) AS INTEGER) AS total_delivery_time_days,
        CASE
            WHEN o.shipping_method_name LIKE '%express%' OR o.shipping_method_name LIKE '%2-day%' THEN 2
            WHEN o.shipping_method_name LIKE '%priority%' OR o.shipping_method_name LIKE '%3-day%' THEN 3
            WHEN o.shipping_method_name LIKE '%standard%' THEN 5
            WHEN o.shipping_method_name LIKE '%economy%' OR o.shipping_method_name LIKE '%ground%' THEN 7
            ELSE 5
        END AS promised_delivery_days,
        COUNT(oi.order_item_id) AS item_count,
        SUM(oi.quantity) AS total_items,
        SUM(oi.quantity * oi.unit_price) AS item_subtotal
    FROM orders o
    INNER JOIN users u ON o.user_id = u.user_id
    INNER JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.order_date >= date('now', '-180 days')
        AND o.order_status IN ('shipped', 'delivered', 'completed')
    GROUP BY o.order_id, o.order_number, o.user_id, u.username, u.email, o.shipping_city, o.shipping_state_province, o.shipping_country, o.order_date, o.status_updated_at, o.completed_at, o.order_status, o.fulfillment_status, o.payment_status, o.total_amount, o.shipping_amount, o.tax_amount, o.discount_amount, o.shipping_method_name, o.tracking_number
),
fulfillment_performance AS (
    SELECT
        ofd.*,
        CASE
            WHEN ofd.processing_time_days <= 1 THEN 'Same Day'
            WHEN ofd.processing_time_days <= 2 THEN 'Next Day'
            WHEN ofd.processing_time_days <= 3 THEN 'Fast'
            WHEN ofd.processing_time_days <= 5 THEN 'Normal'
            ELSE 'Delayed'
        END AS processing_category,
        CASE
            WHEN ofd.shipping_time_days <= ofd.promised_delivery_days - 1 THEN 'Early'
            WHEN ofd.shipping_time_days <= ofd.promised_delivery_days THEN 'On Time'
            WHEN ofd.shipping_time_days <= ofd.promised_delivery_days + 1 THEN 'Slightly Late'
            WHEN ofd.shipping_time_days <= ofd.promised_delivery_days + 3 THEN 'Late'
            ELSE 'Very Late'
        END AS delivery_category,
        CASE
            WHEN ofd.total_delivery_time_days <= ofd.promised_delivery_days THEN 'Within Promise'
            WHEN ofd.total_delivery_time_days <= ofd.promised_delivery_days + 2 THEN 'Minor Delay'
            ELSE 'Major Delay'
        END AS overall_delivery_status,
        CAST((ofd.total_delivery_time_days - ofd.promised_delivery_days) AS INTEGER) AS delivery_delay_days,
        ROUND(ofd.shipping_amount / NULLIF(ofd.total_amount, 0) * 100, 2) AS shipping_cost_percent,
        ROUND(ofd.discount_amount / NULLIF(ofd.total_amount, 0) * 100, 2) AS discount_percent
    FROM order_fulfillment_details ofd
),
regional_performance AS (
    SELECT
        fp.shipping_country,
        fp.shipping_state_province,
        COUNT(DISTINCT fp.order_id) AS total_orders,
        AVG(fp.processing_time_days) AS avg_processing_time,
        AVG(fp.shipping_time_days) AS avg_shipping_time,
        AVG(fp.total_delivery_time_days) AS avg_total_delivery_time,
        SUM(CASE WHEN fp.delivery_category = 'On Time' OR fp.delivery_category = 'Early' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS on_time_delivery_rate_percent,
        SUM(fp.total_amount) AS total_revenue
    FROM fulfillment_performance fp
    GROUP BY fp.shipping_country, fp.shipping_state_province
)
SELECT
    fp.order_id,
    fp.order_number,
    fp.username,
    fp.email,
    fp.shipping_city,
    fp.shipping_state_province,
    fp.shipping_country,
    fp.order_date,
    fp.shipped_date,
    fp.delivered_date,
    fp.order_status,
    fp.fulfillment_status,
    fp.payment_status,
    fp.total_amount,
    fp.shipping_amount,
    fp.tax_amount,
    fp.discount_amount,
    fp.shipping_cost_percent,
    fp.discount_percent,
    fp.shipping_method_name,
    fp.tracking_number,
    fp.processing_time_days,
    fp.shipping_time_days,
    fp.total_delivery_time_days,
    fp.promised_delivery_days,
    fp.delivery_delay_days,
    fp.item_count,
    fp.total_items,
    fp.item_subtotal,
    fp.processing_category,
    fp.delivery_category,
    fp.overall_delivery_status,
    rp.avg_processing_time AS region_avg_processing,
    rp.avg_shipping_time AS region_avg_shipping,
    rp.on_time_delivery_rate_percent AS region_on_time_rate
FROM fulfillment_performance fp
LEFT JOIN regional_performance rp ON fp.shipping_country = rp.shipping_country AND fp.shipping_state_province = rp.shipping_state_province
WHERE fp.order_date IS NOT NULL
    AND fp.total_amount > 0
ORDER BY fp.order_date DESC, fp.total_amount DESC;

-- Query 4: Inventory Management and Stock Analysis Report
WITH inventory_current_status AS (
    SELECT
        i.inventory_id,
        i.product_id,
        p.product_name,
        p.sku,
        p.price,
        p.cost_price,
        p.category_id,
        c.category_name,
        p.brand_id,
        b.brand_name,
        i.warehouse_id,
        i.warehouse_location,
        i.warehouse_zone,
        i.warehouse_aisle,
        i.quantity,
        i.available_quantity,
        i.reserved_quantity,
        i.incoming_quantity,
        i.damaged_quantity,
        i.defective_quantity,
        i.returned_quantity,
        i.on_hold_quantity,
        i.backordered_quantity,
        i.minimum_quantity,
        i.maximum_quantity,
        i.reorder_threshold,
        i.reorder_quantity,
        i.last_received_date,
        i.last_shipped_date,
        i.last_adjustment_date,
        i.inventory_value,
        i.cost_method,
        i.average_cost,
        i.last_cost,
        i.standard_cost,
        CASE
            WHEN i.available_quantity <= i.reorder_threshold THEN 'Reorder Required'
            WHEN i.available_quantity <= i.minimum_quantity THEN 'Below Minimum'
            WHEN i.available_quantity <= i.maximum_quantity * 0.3 THEN 'Low Stock'
            WHEN i.available_quantity <= i.maximum_quantity * 0.7 THEN 'Medium Stock'
            WHEN i.available_quantity <= i.maximum_quantity THEN 'Healthy Stock'
            ELSE 'Overstocked'
        END AS stock_health_status,
        CASE
            WHEN i.damaged_quantity + i.defective_quantity > i.quantity * 0.1 THEN 'High Loss Rate'
            WHEN i.damaged_quantity + i.defective_quantity > i.quantity * 0.05 THEN 'Medium Loss Rate'
            ELSE 'Acceptable Loss Rate'
        END AS loss_rate_status,
        i.available_quantity - i.reorder_threshold AS units_until_reorder,
        i.maximum_quantity - i.available_quantity AS units_to_max_capacity
    FROM inventory i
    INNER JOIN products p ON i.product_id = p.product_id
    INNER JOIN categories c ON p.category_id = c.category_id
    INNER JOIN brands b ON p.brand_id = b.brand_id
),
inventory_movement AS (
    SELECT
        ic.product_id,
        SUM(oi.quantity) AS total_quantity_sold_90d,
        AVG(oi.quantity) AS avg_daily_sales,
        COUNT(DISTINCT o.order_id) AS orders_with_product_90d,
        MAX(o.order_date) AS last_sale_date,
        SUM(CASE WHEN o.order_date >= date('now', '-30 days') THEN oi.quantity ELSE 0 END) AS sales_last_30d,
        SUM(CASE WHEN o.order_date >= date('now', '-60 days') AND o.order_date < date('now', '-30 days') THEN oi.quantity ELSE 0 END) AS sales_30_60d_ago,
        SUM(CASE WHEN o.order_date >= date('now', '-90 days') AND o.order_date < date('now', '-60 days') THEN oi.quantity ELSE 0 END) AS sales_60_90d_ago
    FROM inventory_current_status ic
    LEFT JOIN order_items oi ON ic.product_id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.order_id
    WHERE o.order_status IN ('shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-90 days')
    GROUP BY ic.product_id
),
stock_forecast AS (
    SELECT
        ic.product_id,
        ic.available_quantity,
        im.avg_daily_sales,
        CASE
            WHEN im.avg_daily_sales > 0 THEN CAST(ic.available_quantity / im.avg_daily_sales AS INTEGER)
            ELSE NULL
        END AS days_of_stock_remaining,
        CASE
            WHEN im.avg_daily_sales > 0 AND ic.available_quantity / im.avg_daily_sales < 7 THEN 'Critical'
            WHEN im.avg_daily_sales > 0 AND ic.available_quantity / im.avg_daily_sales < 14 THEN 'Low'
            WHEN im.avg_daily_sales > 0 AND ic.available_quantity / im.avg_daily_sales < 30 THEN 'Moderate'
            ELSE 'Adequate'
        END AS stock_duration_status,
        CASE
            WHEN im.sales_last_30d > im.sales_30_60d_ago * 1.5 THEN 'Increasing'
            WHEN im.sales_last_30d < im.sales_30_60d_ago * 0.75 THEN 'Decreasing'
            ELSE 'Stable'
        END AS sales_trend
    FROM inventory_current_status ic
    LEFT JOIN inventory_movement im ON ic.product_id = im.product_id
)
SELECT
    ic.inventory_id,
    ic.product_id,
    ic.product_name,
    ic.sku,
    ic.price,
    ic.cost_price,
    ic.category_name,
    ic.brand_name,
    ic.warehouse_id,
    ic.warehouse_location,
    ic.warehouse_zone,
    ic.warehouse_aisle,
    ic.quantity,
    ic.available_quantity,
    ic.reserved_quantity,
    ic.incoming_quantity,
    ic.damaged_quantity,
    ic.defective_quantity,
    ic.returned_quantity,
    ic.on_hold_quantity,
    ic.backordered_quantity,
    ic.minimum_quantity,
    ic.maximum_quantity,
    ic.reorder_threshold,
    ic.reorder_quantity,
    ic.last_received_date,
    ic.last_shipped_date,
    ic.last_adjustment_date,
    ic.inventory_value,
    ic.cost_method,
    ic.average_cost,
    ic.stock_health_status,
    ic.loss_rate_status,
    ic.units_until_reorder,
    ic.units_to_max_capacity,
    im.total_quantity_sold_90d,
    im.avg_daily_sales,
    im.orders_with_product_90d,
    im.last_sale_date,
    im.sales_last_30d,
    im.sales_30_60d_ago,
    im.sales_60_90d_ago,
    sf.days_of_stock_remaining,
    sf.stock_duration_status,
    sf.sales_trend,
    ROUND(ic.damaged_quantity / NULLIF(ic.quantity, 0) * 100, 2) AS damage_rate_percent,
    ROUND(ic.available_quantity / NULLIF(ic.maximum_quantity, 0) * 100, 2) AS stock_utilization_percent
FROM inventory_current_status ic
LEFT JOIN inventory_movement im ON ic.product_id = im.product_id
LEFT JOIN stock_forecast sf ON ic.product_id = sf.product_id
WHERE ic.quantity > 0
ORDER BY ic.product_id, ic.warehouse_id;

-- Query 5: Payment Performance and Transaction Analysis
WITH payment_transaction_details AS (
    SELECT
        p.payment_id,
        p.order_id,
        o.order_number,
        p.user_id,
        u.username,
        u.email,
        p.transaction_id,
        p.gateway_transaction_id,
        p.payment_method,
        p.payment_method_type,
        p.payment_processor,
        p.payment_date,
        p.amount,
        p.currency_code,
        p.status,
        p.failure_code,
        p.failure_message,
        p.risk_score,
        p.risk_level,
        p.is_manual,
        p.is_offline,
        p.is_test,
        p.is_recurring,
        p.authorization_code,
        p.authorization_date,
        p.captured_at,
        p.captured_amount,
        p.refunded_at,
        p.refunded_amount,
        p.voided_at,
        p.voided_amount,
        p.payment_card_last4,
        p.payment_card_brand,
        p.payment_card_type,
        p.paypal_email,
        p.bank_transfer_reference,
        p.check_number,
        o.total_amount AS order_total,
        o.order_status,
        o.order_date,
        CAST(julianday(p.captured_at) - julianday(p.payment_date) AS INTEGER) AS capture_delay_days,
        CAST(julianday(p.refunded_at) - julianday(p.captured_at) AS INTEGER) AS refund_delay_days,
        CASE
            WHEN p.status IN ('completed', 'captured', 'successful') THEN 'Successful'
            WHEN p.status IN ('pending', 'pending_capture', 'pending_authorization') THEN 'Pending'
            WHEN p.status IN ('failed', 'declined', 'rejected') THEN 'Failed'
            WHEN p.status IN ('refunded', 'partial_refund') THEN 'Refunded'
            WHEN p.status IN ('voided', 'cancelled') THEN 'Voided'
            ELSE 'Unknown'
        END AS payment_outcome_category
    FROM payments p
    INNER JOIN orders o ON p.order_id = o.order_id
    INNER JOIN users u ON p.user_id = u.user_id
),
payment_method_aggregates AS (
    SELECT
        ptd.payment_method,
        COUNT(DISTINCT ptd.payment_id) AS total_transactions,
        SUM(ptd.amount) AS total_amount_processed,
        AVG(ptd.amount) AS avg_transaction_amount,
        MIN(ptd.amount) AS min_transaction_amount,
        MAX(ptd.amount) AS max_transaction_amount,
        SUM(CASE WHEN ptd.payment_outcome_category = 'Successful' THEN 1 ELSE 0 END) AS successful_count,
        SUM(CASE WHEN ptd.payment_outcome_category = 'Failed' THEN 1 ELSE 0 END) AS failed_count,
        SUM(CASE WHEN ptd.payment_outcome_category = 'Refunded' THEN 1 ELSE 0 END) AS refunded_count,
        SUM(CASE WHEN ptd.is_recurring = 1 THEN 1 ELSE 0 END) AS recurring_count,
        AVG(ptd.risk_score) AS avg_risk_score,
        SUM(ptd.refunded_amount) AS total_refunded_amount,
        COUNT(DISTINCT ptd.user_id) AS unique_customers
    FROM payment_transaction_details ptd
    GROUP BY ptd.payment_method
),
daily_payment_trends AS (
    SELECT
        strftime('%Y-%m-%d', ptd.payment_date) AS payment_day,
        COUNT(DISTINCT ptd.payment_id) AS daily_transactions,
        SUM(ptd.amount) AS daily_total_amount,
        AVG(ptd.amount) AS daily_avg_amount,
        SUM(CASE WHEN ptd.payment_outcome_category = 'Successful' THEN 1 ELSE 0 END) AS daily_successful_count,
        SUM(CASE WHEN ptd.payment_outcome_category = 'Failed' THEN 1 ELSE 0 END) AS daily_failed_count
    FROM payment_transaction_details ptd
    WHERE ptd.payment_date >= date('now', '-30 days')
    GROUP BY strftime('%Y-%m-%d', ptd.payment_date)
),
fraud_indicators AS (
    SELECT
        ptd.user_id,
        COUNT(DISTINCT ptd.payment_id) AS user_payment_count,
        SUM(ptd.amount) AS user_total_payments,
        AVG(ptd.risk_score) AS user_avg_risk_score,
        SUM(CASE WHEN ptd.payment_outcome_category = 'Failed' THEN 1 ELSE 0 END) AS user_failed_count,
        SUM(CASE WHEN ptd.risk_level = 'high' THEN 1 ELSE 0 END) AS user_high_risk_count,
        MAX(ptd.payment_date) AS user_last_payment_date,
        MIN(ptd.payment_date) AS user_first_payment_date
    FROM payment_transaction_details ptd
    GROUP BY ptd.user_id
    HAVING COUNT(DISTINCT ptd.payment_id) >= 5
)
SELECT
    ptd.payment_id,
    ptd.order_id,
    ptd.order_number,
    ptd.user_id,
    ptd.username,
    ptd.email,
    ptd.transaction_id,
    ptd.gateway_transaction_id,
    ptd.payment_method,
    ptd.payment_method_type,
    ptd.payment_processor,
    ptd.payment_date,
    ptd.amount,
    ptd.currency_code,
    ptd.status,
    ptd.payment_outcome_category,
    ptd.failure_code,
    ptd.failure_message,
    ptd.risk_score,
    ptd.risk_level,
    ptd.is_manual,
    ptd.is_offline,
    ptd.is_test,
    ptd.is_recurring,
    ptd.authorization_code,
    ptd.authorization_date,
    ptd.captured_at,
    ptd.captured_amount,
    ptd.refunded_at,
    ptd.refunded_amount,
    ptd.payment_card_last4,
    ptd.payment_card_brand,
    ptd.payment_card_type,
    ptd.order_total,
    ptd.order_status,
    ptd.order_date,
    ptd.capture_delay_days,
    ptd.refund_delay_days,
    pma.total_transactions AS method_total_transactions,
    pma.total_amount_processed AS method_total_amount,
    pma.avg_transaction_amount AS method_avg_amount,
    ROUND(pma.successful_count * 100.0 / NULLIF(pma.total_transactions, 0), 2) AS method_success_rate_percent,
    ROUND(pma.failed_count * 100.0 / NULLIF(pma.total_transactions, 0), 2) AS method_failure_rate_percent,
    fi.user_avg_risk_score,
    fi.user_failed_count,
    fi.user_high_risk_count
FROM payment_transaction_details ptd
LEFT JOIN payment_method_aggregates pma ON ptd.payment_method = pma.payment_method
LEFT JOIN fraud_indicators fi ON ptd.user_id = fi.user_id
WHERE ptd.payment_date >= date('now', '-90 days')
    AND ptd.amount > 0
ORDER BY ptd.payment_date DESC, ptd.amount DESC;

-- Query 6: Shipping and Logistics Performance Analysis
WITH shipment_details AS (
    SELECT
        s.shipping_id,
        s.order_id,
        o.order_number,
        s.user_id,
        u.username,
        u.email,
        s.shipment_number,
        s.carrier,
        s.carrier_code,
        s.service_name,
        s.service_code,
        s.service_level,
        s.tracking_number,
        s.tracking_url,
        s.shipping_date,
        s.estimated_delivery_date,
        s.actual_delivery_date,
        s.first_scan_date,
        s.last_scan_date,
        s.status,
        s.weight,
        s.weight_unit,
        s.length,
        s.width,
        s.height,
        s.dimension_unit,
        s.declared_value,
        s.insurance_amount,
        s.signature_required,
        s.residential_delivery,
        s.saturday_delivery,
        s.shipping_address_line1,
        s.shipping_city,
        s.shipping_state_province,
        s.shipping_country,
        s.origin_city,
        s.origin_state_province,
        s.origin_country,
        s.warehouse_id,
        s.warehouse_name,
        s.item_count,
        s.product_count,
        s.total_quantity,
        s.shipping_cost,
        s.shipping_tax_amount,
        s.fuel_surcharge,
        s.handling_fee,
        s.insurance_fee,
        s.total_shipping_cost,
        s.is_free_shipping,
        s.is_expedited,
        s.is_international,
        CAST(julianday(s.actual_delivery_date) - julianday(s.shipping_date) AS INTEGER) AS actual_shipping_days,
        CAST(julianday(s.estimated_delivery_date) - julianday(s.shipping_date) AS INTEGER) AS estimated_shipping_days,
        CAST(julianday(s.actual_delivery_date) - julianday(s.estimated_delivery_date) AS INTEGER) AS delivery_accuracy_days,
        CAST(julianday(s.first_scan_date) - julianday(s.shipping_date) AS INTEGER) AS first_scan_delay_days,
        o.total_amount AS order_total_amount,
        o.order_date
    FROM shipping s
    INNER JOIN orders o ON s.order_id = o.order_id
    INNER JOIN users u ON s.user_id = u.user_id
),
shipping_performance_metrics AS (
    SELECT
        sd.*,
        CASE
            WHEN sd.delivery_accuracy_days <= -1 THEN 'Early'
            WHEN sd.delivery_accuracy_days = 0 THEN 'On Time'
            WHEN sd.delivery_accuracy_days = 1 THEN '1 Day Late'
            WHEN sd.delivery_accuracy_days <= 3 THEN '2-3 Days Late'
            ELSE 'More Than 3 Days Late'
        END AS delivery_timeliness,
        CASE
            WHEN sd.first_scan_delay_days <= 0 THEN 'Same Day Scan'
            WHEN sd.first_scan_delay_days = 1 THEN 'Next Day Scan'
            ELSE 'Delayed Scan'
        END AS scan_timeliness,
        CASE
            WHEN sd.is_international = 1 THEN 'International'
            ELSE 'Domestic'
        END AS shipment_type,
        CASE
            WHEN sd.is_expedited = 1 THEN 'Expedited'
            WHEN sd.service_name LIKE '%express%' THEN 'Express'
            WHEN sd.service_name LIKE '%priority%' THEN 'Priority'
            WHEN sd.service_name LIKE '%standard%' THEN 'Standard'
            ELSE 'Economy'
        END AS service_tier,
        ROUND(sd.shipping_cost / NULLIF(sd.order_total_amount, 0) * 100, 2) AS shipping_cost_percent_of_order,
        ROUND(sd.total_shipping_cost / NULLIF(sd.order_total_amount, 0) * 100, 2) AS total_shipping_cost_percent
    FROM shipment_details sd
),
carrier_performance AS (
    SELECT
        spm.carrier,
        COUNT(DISTINCT spm.shipping_id) AS total_shipments,
        AVG(spm.actual_shipping_days) AS avg_shipping_days,
        AVG(spm.estimated_shipping_days) AS avg_estimated_days,
        AVG(spm.delivery_accuracy_days) AS avg_delivery_accuracy,
        SUM(CASE WHEN spm.delivery_timeliness IN ('Early', 'On Time') THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS on_time_delivery_rate_percent,
        AVG(spm.total_shipping_cost) AS avg_shipping_cost,
        SUM(spm.total_shipping_cost) AS total_shipping_cost,
        SUM(spm.declared_value) AS total_declared_value,
        COUNT(DISTINCT spm.user_id) AS unique_customers
    FROM shipping_performance_metrics spm
    GROUP BY spm.carrier
),
regional_shipping AS (
    SELECT
        spm.shipping_country,
        spm.shipping_state_province,
        COUNT(DISTINCT spm.shipping_id) AS regional_shipments,
        AVG(spm.actual_shipping_days) AS regional_avg_shipping_days,
        AVG(spm.total_shipping_cost) AS regional_avg_shipping_cost,
        SUM(spm.total_shipping_cost) AS regional_total_shipping_cost,
        SUM(CASE WHEN spm.delivery_timeliness IN ('Early', 'On Time') THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS regional_on_time_rate_percent
    FROM shipping_performance_metrics spm
    GROUP BY spm.shipping_country, spm.shipping_state_province
)
SELECT
    spm.shipping_id,
    spm.order_id,
    spm.order_number,
    spm.username,
    spm.email,
    spm.shipment_number,
    spm.carrier,
    spm.carrier_code,
    spm.service_name,
    spm.service_code,
    spm.service_level,
    spm.tracking_number,
    spm.shipping_date,
    spm.estimated_delivery_date,
    spm.actual_delivery_date,
    spm.first_scan_date,
    spm.status,
    spm.weight,
    spm.weight_unit,
    spm.declared_value,
    spm.signature_required,
    spm.residential_delivery,
    spm.saturday_delivery,
    spm.shipping_city,
    spm.shipping_state_province,
    spm.shipping_country,
    spm.origin_city,
    spm.origin_country,
    spm.warehouse_name,
    spm.item_count,
    spm.product_count,
    spm.total_quantity,
    spm.shipping_cost,
    spm.total_shipping_cost,
    spm.is_free_shipping,
    spm.is_expedited,
    spm.is_international,
    spm.actual_shipping_days,
    spm.estimated_shipping_days,
    spm.delivery_accuracy_days,
    spm.first_scan_delay_days,
    spm.order_total_amount,
    spm.delivery_timeliness,
    spm.scan_timeliness,
    spm.shipment_type,
    spm.service_tier,
    spm.shipping_cost_percent_of_order,
    spm.total_shipping_cost_percent,
    cp.avg_shipping_days AS carrier_avg_days,
    cp.on_time_delivery_rate_percent AS carrier_on_time_rate,
    cp.avg_shipping_cost AS carrier_avg_cost,
    rs.regional_avg_shipping_days,
    rs.regional_on_time_rate_percent
FROM shipping_performance_metrics spm
LEFT JOIN carrier_performance cp ON spm.carrier = cp.carrier
LEFT JOIN regional_shipping rs ON spm.shipping_country = rs.shipping_country AND spm.shipping_state_province = rs.shipping_state_province
WHERE spm.shipping_date >= date('now', '-180 days')
ORDER BY spm.shipping_date DESC, spm.order_total_amount DESC;

-- Query 7: Review Sentiment and Product Feedback Analysis
WITH review_details AS (
    SELECT
        r.review_id,
        r.product_id,
        p.product_name,
        p.sku,
        p.category_id,
        c.category_name,
        p.brand_id,
        b.brand_name,
        r.user_id,
        u.username,
        u.email,
        u.city,
        u.country,
        r.order_id,
        r.title,
        r.review_text,
        r.rating,
        r.rating_details,
        r.quality_rating,
        r.value_rating,
        r.price_rating,
        r.shipping_rating,
        r.customer_service_rating,
        r.is_verified_purchase,
        r.is_anonymous,
        r.is_featured,
        r.is_pinned,
        r.is_approved,
        r.is_spam,
        r.is_abuse,
        r.review_date,
        r.last_edited_at,
        r.helpful_count,
        r.not_helpful_count,
        r.report_count,
        r.reviewer_name,
        r.reviewer_email,
        r.reviewer_country,
        r.purchase_date,
        r.use_case,
        r.use_duration,
        r.recommended,
        r.would_buy_again,
        r.pros,
        r.cons,
        r.bottom_line,
        r.video_uploaded,
        r.photo_uploaded,
        r.source,
        r.channel,
        CASE
            WHEN r.rating >= 4 THEN 'Positive'
            WHEN r.rating = 3 THEN 'Neutral'
            ELSE 'Negative'
        END AS sentiment_category,
        CASE
            WHEN r.rating = 5 THEN 'Excellent'
            WHEN r.rating = 4 THEN 'Good'
            WHEN r.rating = 3 THEN 'Average'
            WHEN r.rating = 2 THEN 'Poor'
            ELSE 'Terrible'
        END AS rating_category,
        r.helpful_count + r.not_helpful_count AS total_votes,
        ROUND(r.helpful_count * 100.0 / NULLIF(r.helpful_count + r.not_helpful_count, 0), 2) AS helpful_percent
    FROM reviews r
    INNER JOIN products p ON r.product_id = p.product_id
    INNER JOIN categories c ON p.category_id = c.category_id
    INNER JOIN brands b ON p.brand_id = b.brand_id
    INNER JOIN users u ON r.user_id = u.user_id
),
product_review_metrics AS (
    SELECT
        rd.product_id,
        rd.product_name,
        rd.category_name,
        rd.brand_name,
        COUNT(DISTINCT rd.review_id) AS total_reviews,
        AVG(rd.rating) AS avg_rating,
        SUM(CASE WHEN rd.sentiment_category = 'Positive' THEN 1 ELSE 0 END) AS positive_count,
        SUM(CASE WHEN rd.sentiment_category = 'Neutral' THEN 1 ELSE 0 END) AS neutral_count,
        SUM(CASE WHEN rd.sentiment_category = 'Negative' THEN 1 ELSE 0 END) AS negative_count,
        SUM(CASE WHEN rd.is_verified_purchase = 1 THEN 1 ELSE 0 END) AS verified_purchase_count,
        SUM(CASE WHEN rd.is_featured = 1 THEN 1 ELSE 0 END) AS featured_count,
        SUM(CASE WHEN rd.is_approved = 1 THEN 1 ELSE 0 END) AS approved_count,
        SUM(CASE WHEN rd.is_spam = 1 THEN 1 ELSE 0 END) AS spam_count,
        SUM(rd.helpful_count) AS total_helpful_votes,
        SUM(rd.not_helpful_count) AS total_not_helpful_votes,
        SUM(rd.report_count) AS total_reports,
        AVG(rd.quality_rating) AS avg_quality_rating,
        AVG(rd.value_rating) AS avg_value_rating,
        AVG(rd.price_rating) AS avg_price_rating,
        AVG(rd.shipping_rating) AS avg_shipping_rating,
        AVG(rd.customer_service_rating) AS avg_customer_service_rating,
        SUM(CASE WHEN rd.recommended = 1 THEN 1 ELSE 0 END) AS recommended_count,
        SUM(CASE WHEN rd.would_buy_again = 1 THEN 1 ELSE 0 END) AS would_buy_again_count,
        SUM(CASE WHEN rd.video_uploaded = 1 THEN 1 ELSE 0 END) AS video_count,
        SUM(CASE WHEN rd.photo_uploaded = 1 THEN 1 ELSE 0 END) AS photo_count,
        MAX(rd.review_date) AS latest_review_date,
        MIN(rd.review_date) AS earliest_review_date
    FROM review_details rd
    GROUP BY rd.product_id, rd.product_name, rd.category_name, rd.brand_name
),
category_review_summary AS (
    SELECT
        rd.category_name,
        COUNT(DISTINCT rd.product_id) AS products_with_reviews,
        COUNT(DISTINCT rd.review_id) AS category_total_reviews,
        AVG(rd.rating) AS category_avg_rating,
        AVG(prm.avg_quality_rating) AS category_avg_quality,
        AVG(prm.avg_value_rating) AS category_avg_value,
        AVG(prm.avg_price_rating) AS category_avg_price,
        SUM(prm.positive_count) AS category_positive_count,
        SUM(prm.negative_count) AS category_negative_count,
        ROUND(SUM(prm.positive_count) * 100.0 / NULLIF(SUM(prm.total_reviews), 0), 2) AS category_positive_rate_percent
    FROM review_details rd
    LEFT JOIN product_review_metrics prm ON rd.product_id = prm.product_id
    GROUP BY rd.category_name
),
review_timeline AS (
    SELECT
        rd.product_id,
        strftime('%Y-%m', rd.review_date) AS review_month,
        COUNT(DISTINCT rd.review_id) AS monthly_reviews,
        AVG(rd.rating) AS monthly_avg_rating,
        SUM(CASE WHEN rd.sentiment_category = 'Positive' THEN 1 ELSE 0 END) AS monthly_positive_count
    FROM review_details rd
    WHERE rd.review_date >= date('now', '-365 days')
    GROUP BY rd.product_id, strftime('%Y-%m', rd.review_date)
)
SELECT
    rd.review_id,
    rd.product_id,
    rd.product_name,
    rd.sku,
    rd.category_name,
    rd.brand_name,
    rd.username,
    rd.email,
    rd.city,
    rd.country,
    rd.title,
    rd.review_text,
    rd.rating,
    rd.sentiment_category,
    rd.rating_category,
    rd.quality_rating,
    rd.value_rating,
    rd.price_rating,
    rd.shipping_rating,
    rd.customer_service_rating,
    rd.is_verified_purchase,
    rd.is_featured,
    rd.is_approved,
    rd.review_date,
    rd.helpful_count,
    rd.not_helpful_count,
    rd.total_votes,
    rd.helpful_percent,
    rd.recommended,
    rd.would_buy_again,
    rd.video_uploaded,
    rd.photo_uploaded,
    prm.total_reviews AS product_total_reviews,
    prm.avg_rating AS product_avg_rating,
    prm.positive_count AS product_positive_count,
    prm.negative_count AS product_negative_count,
    prm.approved_count AS product_approved_count,
    prm.recommended_count AS product_recommended_count,
    crs.category_avg_rating,
    crs.category_positive_rate_percent
FROM review_details rd
LEFT JOIN product_review_metrics prm ON rd.product_id = prm.product_id
LEFT JOIN category_review_summary crs ON rd.category_name = crs.category_name
WHERE rd.is_approved = 1
    AND rd.is_spam = 0
    AND rd.is_abuse = 0
ORDER BY rd.review_date DESC, rd.rating DESC;

-- Query 8: Customer Behavior and Purchase Journey Analysis
WITH customer_journey AS (
    SELECT
        u.user_id,
        u.username,
        u.email,
        u.city,
        u.state_province,
        u.country,
        u.created_at AS registration_date,
        u.is_verified,
        u.last_login_at,
        u.loyalty_tier,
        u.loyalty_points,
        u.referred_by_user_id,
        u.marketing_opt_in,
        u.newsletter_subscribed,
        o.order_id,
        o.order_number,
        o.order_date,
        o.order_status,
        o.total_amount,
        o.subtotal,
        o.discount_amount,
        o.shipping_amount,
        o.tax_amount,
        o.coupon_code,
        o.source,
        o.channel,
        o.utm_source,
        o.utm_medium,
        o.utm_campaign,
        o.landing_page_url,
        o.referrer_url,
        ROW_NUMBER() OVER (PARTITION BY u.user_id ORDER BY o.order_date) AS purchase_sequence,
        LAG(o.order_date) OVER (PARTITION BY u.user_id ORDER BY o.order_date) AS previous_order_date,
        CAST(julianday(o.order_date) - julianday(LAG(o.order_date) OVER (PARTITION BY u.user_id ORDER BY o.order_date)) AS INTEGER) AS days_since_last_purchase
    FROM users u
    LEFT JOIN orders o ON u.user_id = o.user_id
    WHERE u.is_active = 1
),
purchase_frequency AS (
    SELECT
        cj.user_id,
        COUNT(DISTINCT cj.order_id) AS total_orders,
        SUM(cj.total_amount) AS total_spent,
        AVG(cj.total_amount) AS avg_order_value,
        MIN(cj.order_date) AS first_purchase_date,
        MAX(cj.order_date) AS last_purchase_date,
        CAST(julianday(MAX(cj.order_date)) - julianday(MIN(cj.order_date)) AS INTEGER) AS purchase_tenure_days,
        CAST(julianday('now') - julianday(MAX(cj.order_date)) AS INTEGER) AS days_since_last_order,
        AVG(cj.days_since_last_purchase) AS avg_days_between_orders,
        COUNT(DISTINCT strftime('%Y-%m', cj.order_date)) AS months_with_purchases,
        SUM(CASE WHEN cj.coupon_code IS NOT NULL THEN 1 ELSE 0 END) AS orders_with_coupon,
        SUM(cj.discount_amount) AS total_discounts_used,
        SUM(cj.shipping_amount) AS total_shipping_paid,
        SUM(cj.tax_amount) AS total_taxes_paid
    FROM customer_journey cj
    WHERE cj.order_status IN ('shipped', 'delivered', 'completed')
    GROUP BY cj.user_id
),
cohort_analysis AS (
    SELECT
        strftime('%Y-%m', cj.registration_date) AS registration_cohort,
        strftime('%Y-%m', cj.order_date) AS order_month,
        COUNT(DISTINCT cj.user_id) AS cohort_users,
        COUNT(DISTINCT cj.order_id) AS cohort_orders,
        SUM(cj.total_amount) AS cohort_revenue,
        AVG(cj.total_amount) AS cohort_avg_order_value
    FROM customer_journey cj
    WHERE cj.order_status IN ('shipped', 'delivered', 'completed')
    GROUP BY strftime('%Y-%m', cj.registration_date), strftime('%Y-%m', cj.order_date)
),
customer_segmentation AS (
    SELECT
        u.user_id,
        u.username,
        u.email,
        u.city,
        u.country,
        u.loyalty_tier,
        u.loyalty_points,
        pf.total_orders,
        pf.total_spent,
        pf.avg_order_value,
        pf.first_purchase_date,
        pf.last_purchase_date,
        pf.days_since_last_order,
        pf.avg_days_between_orders,
        CASE
            WHEN pf.total_orders >= 50 THEN 'VIP'
            WHEN pf.total_orders >= 20 THEN 'Loyal'
            WHEN pf.total_orders >= 5 THEN 'Regular'
            WHEN pf.total_orders = 1 THEN 'One-Time'
            ELSE 'Never Purchased'
        END AS purchase_frequency_segment,
        CASE
            WHEN pf.total_spent >= 5000 THEN 'High Value'
            WHEN pf.total_spent >= 1000 THEN 'Medium Value'
            WHEN pf.total_spent >= 100 THEN 'Low Value'
            ELSE 'Non-Purchaser'
        END AS value_segment,
        CASE
            WHEN pf.days_since_last_order <= 30 THEN 'Active'
            WHEN pf.days_since_last_order <= 90 THEN 'Engaged'
            WHEN pf.days_since_last_order <= 180 THEN 'At Risk'
            ELSE 'Inactive'
        END AS engagement_segment
    FROM users u
    LEFT JOIN purchase_frequency pf ON u.user_id = pf.user_id
),
channel_performance AS (
    SELECT
        cj.channel,
        cj.utm_source,
        cj.utm_medium,
        cj.utm_campaign,
        COUNT(DISTINCT cj.user_id) AS unique_users,
        COUNT(DISTINCT cj.order_id) AS orders_from_channel,
        SUM(cj.total_amount) AS revenue_from_channel,
        AVG(cj.total_amount) AS avg_order_value_from_channel,
        SUM(cj.discount_amount) AS discounts_from_channel,
        ROUND(SUM(cj.total_amount) * 100.0 / (SELECT SUM(total_amount) FROM orders WHERE order_status IN ('shipped', 'delivered', 'completed')), 2) AS revenue_share_percent
    FROM customer_journey cj
    WHERE cj.order_status IN ('shipped', 'delivered', 'completed')
        AND cj.channel IS NOT NULL
    GROUP BY cj.channel, cj.utm_source, cj.utm_medium, cj.utm_campaign
)
SELECT
    cs.user_id,
    cs.username,
    cs.email,
    cs.city,
    cs.country,
    cs.loyalty_tier,
    cs.loyalty_points,
    cs.total_orders,
    cs.total_spent,
    cs.avg_order_value,
    cs.first_purchase_date,
    cs.last_purchase_date,
    cs.days_since_last_order,
    cs.avg_days_between_orders,
    cs.purchase_frequency_segment,
    cs.value_segment,
    cs.engagement_segment,
    pf.months_with_purchases,
    pf.orders_with_coupon,
    pf.total_discounts_used,
    pf.total_shipping_paid,
    pf.total_taxes_paid,
    ROUND(pf.total_discounts_used / NULLIF(pf.total_spent, 0) * 100, 2) AS discount_rate_percent,
    ROUND(pf.total_shipping_paid / NULLIF(pf.total_spent, 0) * 100, 2) AS shipping_cost_percent
FROM customer_segmentation cs
LEFT JOIN purchase_frequency pf ON cs.user_id = pf.user_id
WHERE cs.total_orders > 0
    OR cs.loyalty_points > 0
ORDER BY cs.total_spent DESC, cs.total_orders DESC;

-- Query 9: Discount and Promotion Effectiveness Analysis
WITH promotion_details AS (
    SELECT
        o.order_id,
        o.order_number,
        o.user_id,
        u.username,
        u.email,
        o.order_date,
        o.order_status,
        o.total_amount,
        o.subtotal,
        o.discount_amount,
        o.coupon_code,
        o.coupon_discount_amount,
        o.gift_card_amount,
        o.loyalty_points_used,
        o.loyalty_points_discount,
        o.shipping_amount,
        o.tax_amount,
        o.utm_campaign,
        o.utm_source,
        o.utm_medium,
        o.source,
        o.channel,
        COUNT(DISTINCT oi.order_item_id) AS item_count,
        SUM(oi.quantity) AS total_quantity,
        SUM(oi.quantity * oi.unit_price) AS items_subtotal,
        CASE
            WHEN o.coupon_code IS NOT NULL THEN 1
            WHEN o.gift_card_amount > 0 THEN 1
            WHEN o.loyalty_points_used > 0 THEN 1
            ELSE 0
        END AS used_promotion,
        CASE
            WHEN o.coupon_code IS NOT NULL THEN 'Coupon'
            WHEN o.gift_card_amount > 0 THEN 'Gift Card'
            WHEN o.loyalty_points_used > 0 THEN 'Loyalty Points'
            ELSE 'None'
        END AS promotion_type,
        CASE
            WHEN o.coupon_code LIKE '%SALE%' OR o.coupon_code LIKE '%DISCOUNT%' THEN 'Percentage Off'
            WHEN o.coupon_code LIKE '%FIXED%' OR o.coupon_code LIKE '%OFF%' THEN 'Fixed Amount'
            WHEN o.coupon_code LIKE '%FREE%' THEN 'Free Shipping'
            WHEN o.coupon_code LIKE '%BUNDLE%' THEN 'Bundle Deal'
            ELSE 'Other'
        END AS coupon_type
    FROM orders o
    INNER JOIN users u ON o.user_id = u.user_id
    INNER JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.order_status IN ('shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-365 days')
    GROUP BY o.order_id, o.order_number, o.user_id, u.username, u.email, o.order_date, o.order_status, o.total_amount, o.subtotal, o.discount_amount, o.coupon_code, o.coupon_discount_amount, o.gift_card_amount, o.loyalty_points_used, o.loyalty_points_discount, o.shipping_amount, o.tax_amount, o.utm_campaign, o.utm_source, o.utm_medium, o.source, o.channel
),
promotion_aggregates AS (
    SELECT
        pd.promotion_type,
        pd.coupon_type,
        pd.coupon_code,
        pd.utm_campaign,
        pd.utm_source,
        pd.channel,
        COUNT(DISTINCT pd.order_id) AS total_orders,
        COUNT(DISTINCT pd.user_id) AS unique_users,
        SUM(pd.total_amount) AS total_revenue,
        SUM(pd.discount_amount) AS total_discounts,
        SUM(pd.coupon_discount_amount) AS coupon_discounts,
        SUM(pd.gift_card_amount) AS gift_card_discounts,
        SUM(pd.loyalty_points_discount) AS loyalty_discounts,
        SUM(pd.loyalty_points_used) AS loyalty_points_redeemed,
        AVG(pd.total_amount) AS avg_order_value,
        AVG(pd.item_count) AS avg_items_per_order,
        AVG(pd.total_quantity) AS avg_quantity_per_order,
        SUM(pd.items_subtotal) AS total_items_revenue,
        MIN(pd.order_date) AS first_usage_date,
        MAX(pd.order_date) AS last_usage_date,
        COUNT(DISTINCT strftime('%Y-%m', pd.order_date)) AS months_active
    FROM promotion_details pd
    GROUP BY pd.promotion_type, pd.coupon_type, pd.coupon_code, pd.utm_campaign, pd.utm_source, pd.channel
),
promotion_effectiveness AS (
    SELECT
        pa.promotion_type,
        pa.coupon_type,
        pa.coupon_code,
        pa.utm_campaign,
        pa.total_orders,
        pa.unique_users,
        pa.total_revenue,
        pa.total_discounts,
        pa.avg_order_value,
        ROUND(pa.total_discounts / NULLIF(pa.total_revenue, 0) * 100, 2) AS discount_to_revenue_ratio_percent,
        ROUND(pa.total_revenue / NULLIF(pa.total_orders, 0), 2) AS revenue_per_order,
        ROUND(pa.total_revenue / NULLIF(pa.unique_users, 0), 2) AS revenue_per_user,
        CASE
            WHEN pa.total_discounts / NULLIF(pa.total_revenue, 0) < 0.1 THEN 'Highly Effective'
            WHEN pa.total_discounts / NULLIF(pa.total_revenue, 0) < 0.2 THEN 'Effective'
            WHEN pa.total_discounts / NULLIF(pa.total_revenue, 0) < 0.3 THEN 'Moderate'
            ELSE 'Low Effectiveness'
        END AS effectiveness_rating
    FROM promotion_aggregates pa
),
promotion_channel_comparison AS (
    SELECT
        pd.channel,
        COUNT(DISTINCT pd.order_id) AS channel_orders,
        COUNT(DISTINCT CASE WHEN pd.used_promotion = 1 THEN pd.order_id END) AS channel_promotion_orders,
        SUM(pd.total_amount) AS channel_revenue,
        SUM(pd.discount_amount) AS channel_discounts,
        ROUND(COUNT(DISTINCT CASE WHEN pd.used_promotion = 1 THEN pd.order_id END) * 100.0 / NULLIF(COUNT(DISTINCT pd.order_id), 0), 2) AS promotion_usage_rate_percent,
        ROUND(SUM(pd.discount_amount) * 100.0 / NULLIF(SUM(pd.total_amount), 0), 2) AS channel_discount_rate_percent,
        AVG(pd.total_amount) AS channel_avg_order_value,
        AVG(CASE WHEN pd.used_promotion = 1 THEN pd.total_amount ELSE NULL END) AS promotion_avg_order_value
    FROM promotion_details pd
    GROUP BY pd.channel
)
SELECT
    pd.order_id,
    pd.order_number,
    pd.username,
    pd.email,
    pd.order_date,
    pd.order_status,
    pd.total_amount,
    pd.subtotal,
    pd.discount_amount,
    pd.coupon_code,
    pd.coupon_discount_amount,
    pd.gift_card_amount,
    pd.loyalty_points_used,
    pd.loyalty_points_discount,
    pd.shipping_amount,
    pd.tax_amount,
    pd.utm_campaign,
    pd.utm_source,
    pd.utm_medium,
    pd.source,
    pd.channel,
    pd.item_count,
    pd.total_quantity,
    pd.items_subtotal,
    pd.used_promotion,
    pd.promotion_type,
    pd.coupon_type,
    pe.effectiveness_rating,
    pe.discount_to_revenue_ratio_percent,
    pc.promotion_usage_rate_percent,
    pc.channel_discount_rate_percent,
    ROUND(pd.discount_amount / NULLIF(pd.subtotal, 0) * 100, 2) AS order_discount_rate_percent,
    ROUND((pd.total_amount - pd.discount_amount) / NULLIF(pd.subtotal, 0) * 100, 2) AS effective_margin_percent
FROM promotion_details pd
LEFT JOIN promotion_effectiveness pe ON pd.promotion_type = pe.promotion_type AND (pd.coupon_code = pe.coupon_code OR pd.coupon_code IS NULL AND pe.coupon_code IS NULL)
LEFT JOIN promotion_channel_comparison pc ON pd.channel = pc.channel
WHERE pd.order_status IN ('shipped', 'delivered', 'completed')
ORDER BY pd.order_date DESC, pd.total_amount DESC;

-- Query 10: Multi-Channel Customer Attribution and Conversion Analysis
WITH customer_touchpoints AS (
    SELECT
        o.order_id,
        o.order_number,
        o.user_id,
        u.username,
        u.email,
        u.created_at AS registration_date,
        o.order_date,
        o.order_status,
        o.total_amount,
        o.subtotal,
        o.source,
        o.channel,
        o.utm_source,
        o.utm_medium,
        o.utm_campaign,
        o.utm_term,
        o.utm_content,
        o.landing_page_url,
        o.referrer_url,
        o.affiliate_id,
        o.affiliate_commission,
        o.referred_by,
        ROW_NUMBER() OVER (PARTITION BY o.user_id ORDER BY o.order_date) AS conversion_order,
        LAG(o.order_date) OVER (PARTITION BY o.user_id ORDER BY o.order_date) AS previous_conversion_date,
        CAST(julianday(o.order_date) - julianday(u.created_at) AS INTEGER) AS days_to_first_purchase,
        CAST(julianday(o.order_date) - julianday(LAG(o.order_date) OVER (PARTITION BY o.user_id ORDER BY o.order_date)) AS INTEGER) AS days_between_conversions
    FROM orders o
    INNER JOIN users u ON o.user_id = u.user_id
    WHERE o.order_status IN ('shipped', 'delivered', 'completed')
        AND o.order_date >= date('now', '-365 days')
),
conversion_metrics AS (
    SELECT
        ct.user_id,
        ct.username,
        ct.email,
        ct.registration_date,
        COUNT(DISTINCT ct.order_id) AS total_conversions,
        SUM(ct.total_amount) AS total_conversion_revenue,
        AVG(ct.total_amount) AS avg_conversion_value,
        MIN(ct.order_date) AS first_conversion_date,
        MAX(ct.order_date) AS last_conversion_date,
        AVG(ct.days_between_conversions) AS avg_days_between_conversions,
        MIN(ct.days_to_first_purchase) AS days_to_first_purchase,
        MAX(ct.days_between_conversions) AS max_days_between_conversions,
        SUM(CASE WHEN ct.conversion_order = 1 THEN 1 ELSE 0 END) AS first_time_conversions,
        SUM(CASE WHEN ct.conversion_order > 1 THEN 1 ELSE 0 END) AS repeat_conversions,
        ROUND(SUM(CASE WHEN ct.conversion_order > 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(DISTINCT ct.order_id), 0), 2) AS repeat_conversion_rate_percent
    FROM customer_touchpoints ct
    GROUP BY ct.user_id, ct.username, ct.email, ct.registration_date
),
channel_attribution AS (
    SELECT
        ct.channel,
        ct.utm_source,
        ct.utm_medium,
        ct.utm_campaign,
        COUNT(DISTINCT ct.user_id) AS unique_users,
        COUNT(DISTINCT ct.order_id) AS total_conversions,
        SUM(ct.total_amount) AS total_revenue,
        AVG(ct.total_amount) AS avg_conversion_value,
        SUM(CASE WHEN ct.conversion_order = 1 THEN 1 ELSE 0 END) AS first_time_conversions,
        SUM(CASE WHEN ct.conversion_order > 1 THEN 1 ELSE 0 END) AS repeat_conversions,
        MIN(ct.days_to_first_purchase) AS min_days_to_convert,
        AVG(ct.days_to_first_purchase) AS avg_days_to_convert,
        MAX(ct.days_to_first_purchase) AS max_days_to_convert,
        ROUND(SUM(ct.total_amount) * 100.0 / (SELECT SUM(total_amount) FROM orders WHERE order_status IN ('shipped', 'delivered', 'completed')), 2) AS revenue_share_percent,
        ROUND(SUM(CASE WHEN ct.conversion_order = 1 THEN ct.total_amount ELSE 0 END) * 100.0 / NULLIF(SUM(ct.total_amount), 0), 2) AS first_time_revenue_share_percent
    FROM customer_touchpoints ct
    GROUP BY ct.channel, ct.utm_source, ct.utm_medium, ct.utm_campaign
),
conversion_funnel AS (
    SELECT
        ct.conversion_order,
        COUNT(DISTINCT ct.user_id) AS users_at_step,
        COUNT(DISTINCT ct.order_id) AS conversions_at_step,
        SUM(ct.total_amount) AS revenue_at_step,
        AVG(ct.total_amount) AS avg_value_at_step,
        AVG(ct.days_between_conversions) AS avg_days_since_last_conversion,
        ROUND(COUNT(DISTINCT ct.order_id) * 100.0 / (SELECT COUNT(DISTINCT order_id) FROM orders WHERE order_status IN ('shipped', 'delivered', 'completed')), 2) AS conversion_distribution_percent
    FROM customer_touchpoints ct
    GROUP BY ct.conversion_order
)
SELECT
    cm.user_id,
    cm.username,
    cm.email,
    cm.registration_date,
    cm.total_conversions,
    cm.total_conversion_revenue,
    cm.avg_conversion_value,
    cm.first_conversion_date,
    cm.last_conversion_date,
    cm.avg_days_between_conversions,
    cm.max_days_between_conversions,
    cm.days_to_first_purchase,
    cm.first_time_conversions,
    cm.repeat_conversions,
    cm.repeat_conversion_rate_percent,
    ca.channel,
    ca.utm_source,
    ca.utm_medium,
    ca.utm_campaign,
    ca.total_conversions AS channel_conversions,
    ca.total_revenue AS channel_revenue,
    ca.avg_conversion_value AS channel_avg_conversion_value,
    ca.revenue_share_percent,
    ca.first_time_revenue_share_percent,
    cf.conversion_order,
    cf.users_at_step,
    cf.conversions_at_step,
    cf.revenue_at_step,
    cf.conversion_distribution_percent
FROM conversion_metrics cm
LEFT JOIN channel_attribution ca ON cm.user_id IN (SELECT user_id FROM customer_touchpoints WHERE channel = ca.channel)
LEFT JOIN conversion_funnel cf ON cm.total_conversions >= cf.conversion_order
WHERE cm.total_conversions > 0
ORDER BY cm.total_conversion_revenue DESC, cm.total_conversions DESC;

-- 测试简单sql
SELECT count(*) FROM users