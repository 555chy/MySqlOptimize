
-- Query 1: 需要优化的查询
SELECT DISTINCT u.user_id, u.username, u.email FROM users u WHERE u.is_active = 1 AND u.user_id IN (SELECT o.user_id FROM orders o WHERE o.order_date >= '2024-01-01');

-- Query 2: 无需优化的简单查询
SELECT COUNT(*) FROM users WHERE is_active = 1;

-- Query 3: 需要优化的查询
SELECT p.product_id, p.product_name, COALESCE(p.stock_quantity, 0) as stock FROM products p LEFT JOIN categories c ON p.category_id = c.category_id WHERE p.is_active = 1 AND p.price > 100;

-- Query 4: 无需优化的查询
SELECT * FROM orders ORDER BY order_date DESC LIMIT 10;
