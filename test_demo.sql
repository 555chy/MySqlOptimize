
-- 简单查询1 - 应该无需优化
SELECT user_id, username FROM users;

-- 查询2 - 需要优化
SELECT DISTINCT u.user_id FROM users u WHERE u.is_active = 1;

-- 查询3 - 简单查询 - 无需优化
SELECT product_id, product_name, price FROM products WHERE price > 50;

-- 查询4 - 有需要优化的子查询
SELECT * FROM products p WHERE EXISTS (SELECT 1 FROM orders o WHERE o.product_id = p.product_id);
