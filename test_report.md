# SQL优化测试报告

**生成时间:** 2026-05-17 18:14:36

## 摘要

| 指标 | 数值 |
|------|------|
| 总测试用例数 | 100 |
| 成功测试数 | 100 (100.0%) |
| 结果一致性(优化vs原始) | 100 (100.0%) |
| 已优化查询数 | 53 (53.0%) |
| 原始SQL总查询行数 | 931 |
| 优化SQL总查询行数 | 931 |
| 原始SQL总查询列数 | 376 |
| 优化SQL总查询列数 | 376 |
| 平均原始执行时间(纳秒) | 1018417 |
| 平均优化后执行时间(纳秒) | 440475 |
| 性能提升 | 56.7% |

## 详细结果

### 测试用例 1

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 7885000 纳秒
- 优化后: 565200 纳秒
- 提升: 92.8%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH user_order_stats AS (    SELECT user_id, COUNT(*) AS order_count, SUM(total_amount) AS total_spent    FROM orders    GROUP BY user_id)SELECT u.username, u.email, uos.order_count, uos.total_spent FROM users u JOIN user_order_stats uos ON u.user_id = uos.user_id WHERE uos.total_spent > (SELECT AVG(total_spent) FROM user_order_stats) ORDER BY uos.total_spent DESC
```

**优化后SQL:**
```sql
WITH user_order_stats AS (SELECT user_id, COUNT(*) AS order_count, SUM(total_amount) AS total_spent FROM orders GROUP BY user_id) SELECT u.username, u.email, uos.order_count, uos.total_spent FROM users u JOIN user_order_stats uos ON u.user_id = uos.user_id WHERE uos.total_spent > (SELECT AVG(total_spent) FROM user_order_stats) ORDER BY uos.total_spent DESC
```

---

### 测试用例 2

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 13 行, 7 列
- 优化SQL: 13 行, 7 列

**执行时间:**
- 原始: 1569100 纳秒
- 优化后: 1146400 纳秒
- 提升: 26.9%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, c.category_name, b.brand_name, COALESCE(AVG(r.rating), 0) AS avg_rating, COUNT(DISTINCT r.review_id) AS review_count FROM products p LEFT JOIN categories c ON p.category_id = c.category_id LEFT JOIN brands b ON p.brand_id = b.brand_id LEFT JOIN reviews r ON p.product_id = r.product_id WHERE p.is_active = 1 GROUP BY p.product_id, p.product_name, p.price, c.category_name, b.brand_name HAVING COUNT(DISTINCT r.review_id) > 0 ORDER BY avg_rating DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, c.category_name, b.brand_name, COALESCE(AVG(r.rating), 0) AS avg_rating, COUNT(DISTINCT r.review_id) AS review_count FROM products p LEFT JOIN categories c ON p.category_id = c.category_id LEFT JOIN brands b ON p.brand_id = b.brand_id LEFT JOIN reviews r ON p.product_id = r.product_id WHERE p.is_active = 1 GROUP BY p.product_id, p.product_name, p.price, c.category_name, b.brand_name HAVING COUNT(DISTINCT r.review_id) > 0 ORDER BY avg_rating DESC
```

---

### 测试用例 3

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 8 列
- 优化SQL: 0 行, 8 列

**执行时间:**
- 原始: 1103900 纳秒
- 优化后: 346500 纳秒
- 提升: 68.6%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT o.order_id, o.order_date, u.username, o.total_amount, oi.product_count, p.payment_method, s.carrier, s.tracking_number FROM orders o JOIN users u ON o.user_id = u.user_id JOIN (    SELECT order_id, COUNT(*) AS product_count    FROM order_items    GROUP BY order_id ) oi ON o.order_id = oi.order_id JOIN payments p ON o.order_id = p.order_id JOIN shipping s ON o.order_id = s.order_id WHERE o.status = 'delivered' AND p.status = 'completed' ORDER BY o.order_date DESC
```

**优化后SQL:**
```sql
SELECT o.order_id, o.order_date, u.username, o.total_amount, oi.product_count, p.payment_method, s.carrier, s.tracking_number FROM orders o JOIN users u ON o.user_id = u.user_id JOIN (SELECT order_id, COUNT(*) AS product_count FROM order_items GROUP BY order_id) oi ON o.order_id = oi.order_id JOIN payments p ON o.order_id = p.order_id JOIN shipping s ON o.order_id = s.order_id WHERE o.status = 'delivered' AND p.status = 'completed' ORDER BY o.order_date DESC
```

---

### 测试用例 4

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 6 列
- 优化SQL: 10 行, 6 列

**执行时间:**
- 原始: 1889300 纳秒
- 优化后: 952200 纳秒
- 提升: 49.6%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT c.category_id, c.category_name, COUNT(DISTINCT p.product_id) AS product_count, SUM(oi.quantity) AS total_units_sold, SUM(oi.subtotal) AS total_revenue, (SELECT AVG(rating) FROM reviews WHERE product_id IN (    SELECT product_id FROM products WHERE category_id = c.category_id )) AS avg_category_rating FROM categories c LEFT JOIN products p ON c.category_id = p.category_id LEFT JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name ORDER BY total_revenue DESC
```

**优化后SQL:**
```sql
SELECT c.category_id, c.category_name, COUNT(DISTINCT p.product_id) AS product_count, SUM(oi.quantity) AS total_units_sold, SUM(oi.subtotal) AS total_revenue, (SELECT AVG(rating) FROM reviews WHERE product_id IN (SELECT product_id FROM products WHERE category_id = c.category_id)) AS avg_category_rating FROM categories c LEFT JOIN products p ON c.category_id = p.category_id LEFT JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name ORDER BY total_revenue DESC
```

---

### 测试用例 5

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 4 列
- 优化SQL: 1 行, 4 列

**执行时间:**
- 原始: 1007900 纳秒
- 优化后: 469700 纳秒
- 提升: 53.4%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH monthly_sales AS (    SELECT strftime('%Y-%m', order_date) AS month, SUM(total_amount) AS monthly_total    FROM orders    GROUP BY strftime('%Y-%m', order_date)) SELECT month, monthly_total, monthly_total - LAG(monthly_total) OVER (ORDER BY month) AS growth_from_previous, (monthly_total / (SELECT AVG(monthly_total) FROM monthly_sales)) - 1 AS deviation_from_avg FROM monthly_sales ORDER BY month
```

**优化后SQL:**
```sql
WITH monthly_sales AS (SELECT strftime('%Y-%m', order_date) AS month, SUM(total_amount) AS monthly_total FROM orders GROUP BY strftime('%Y-%m', order_date)) SELECT month, monthly_total, monthly_total - LAG(monthly_total) OVER (ORDER BY month) AS growth_from_previous, (monthly_total / (SELECT AVG(monthly_total) FROM monthly_sales)) - 1 AS deviation_from_avg FROM monthly_sales ORDER BY month
```

---

### 测试用例 6

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 8 列
- 优化SQL: 10 行, 8 列

**执行时间:**
- 原始: 1211400 纳秒
- 优化后: 816500 纳秒
- 提升: 32.6%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT b.brand_id, b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, AVG(p.price) AS avg_price, MAX(p.price) AS max_price, MIN(p.price) AS min_price, COUNT(DISTINCT o.order_id) AS order_count, SUM(oi.quantity) AS total_units_sold FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name HAVING product_count > 0 ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT b.brand_id, b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, AVG(p.price) AS avg_price, MAX(p.price) AS max_price, MIN(p.price) AS min_price, COUNT(DISTINCT o.order_id) AS order_count, SUM(oi.quantity) AS total_units_sold FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name HAVING product_count > 0 ORDER BY order_count DESC
```

---

### 测试用例 7

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 7 列
- 优化SQL: 10 行, 7 列

**执行时间:**
- 原始: 1449000 纳秒
- 优化后: 975000 纳秒
- 提升: 32.7%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, u.email, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS lifetime_spending, (SELECT MAX(order_date) FROM orders WHERE user_id = u.user_id) AS last_order_date, (SELECT COUNT(*) FROM reviews WHERE user_id = u.user_id) AS review_count FROM users u LEFT JOIN orders o ON u.user_id = o.user_id WHERE EXISTS (    SELECT 1 FROM orders o2 WHERE o2.user_id = u.user_id ) GROUP BY u.user_id, u.username, u.email ORDER BY lifetime_spending DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, u.email, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS lifetime_spending, (SELECT MAX(order_date) FROM orders WHERE user_id = u.user_id) AS last_order_date, (SELECT COUNT(*) FROM reviews WHERE user_id = u.user_id) AS review_count FROM users u LEFT JOIN orders o ON u.user_id = o.user_id WHERE EXISTS (SELECT 1 FROM orders o2 WHERE o2.user_id = u.user_id) GROUP BY u.user_id, u.username, u.email ORDER BY lifetime_spending DESC LIMIT 20
```

---

### 测试用例 8

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 7 列
- 优化SQL: 20 行, 7 列

**执行时间:**
- 原始: 1701400 纳秒
- 优化后: 1242700 纳秒
- 提升: 27.0%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, i.quantity AS stock_quantity, CASE WHEN i.quantity < i.reorder_threshold THEN 'Low'      WHEN i.quantity < i.reorder_threshold * 2 THEN 'Medium'      ELSE 'High' END AS stock_status, COUNT(DISTINCT oi.order_item_id) AS total_order_items, SUM(oi.quantity) AS total_units_sold FROM products p JOIN inventory i ON p.product_id = i.product_id LEFT JOIN order_items oi ON p.product_id = oi.product_id GROUP BY p.product_id, p.product_name, p.price, i.quantity, i.reorder_threshold ORDER BY stock_status, p.product_name
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, i.quantity AS stock_quantity, CASE WHEN i.quantity < i.reorder_threshold THEN 'Low' WHEN i.quantity < i.reorder_threshold * 2 THEN 'Medium' ELSE 'High' END AS stock_status, COUNT(DISTINCT oi.order_item_id) AS total_order_items, SUM(oi.quantity) AS total_units_sold FROM products p JOIN inventory i ON p.product_id = i.product_id LEFT JOIN order_items oi ON p.product_id = oi.product_id GROUP BY p.product_id, p.product_name, p.price, i.quantity, i.reorder_threshold ORDER BY stock_status, p.product_name
```

---

### 测试用例 9

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 5 行, 6 列
- 优化SQL: 5 行, 6 列

**执行时间:**
- 原始: 964000 纳秒
- 优化后: 569600 纳秒
- 提升: 40.9%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT strftime('%Y', o.order_date) AS year, strftime('%m', o.order_date) AS month, c.category_name, COUNT(DISTINCT o.order_id) AS order_count, SUM(oi.subtotal) AS total_sales, AVG(oi.unit_price) AS avg_unit_price FROM orders o JOIN order_items oi ON o.order_id = oi.order_id JOIN products p ON oi.product_id = p.product_id JOIN categories c ON p.category_id = c.category_id WHERE o.status = 'delivered' GROUP BY strftime('%Y', o.order_date), strftime('%m', o.order_date), c.category_name ORDER BY year DESC, month DESC, total_sales DESC
```

**优化后SQL:**
```sql
SELECT strftime('%Y', o.order_date) AS year, strftime('%m', o.order_date) AS month, c.category_name, COUNT(DISTINCT o.order_id) AS order_count, SUM(oi.subtotal) AS total_sales, AVG(oi.unit_price) AS avg_unit_price FROM orders o JOIN order_items oi ON o.order_id = oi.order_id JOIN products p ON oi.product_id = p.product_id JOIN categories c ON p.category_id = c.category_id WHERE o.status = 'delivered' GROUP BY strftime('%Y', o.order_date), strftime('%m', o.order_date), c.category_name ORDER BY year DESC, month DESC, total_sales DESC
```

---

### 测试用例 10

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 4 列
- 优化SQL: 10 行, 4 列

**执行时间:**
- 原始: 1361700 纳秒
- 优化后: 918700 纳秒
- 提升: 32.5%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH top_products AS (    SELECT p.product_id, p.product_name, SUM(oi.quantity) AS total_sold    FROM products p    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY p.product_id, p.product_name    ORDER BY total_sold DESC    LIMIT 10 ), product_reviews AS (    SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count    FROM reviews    GROUP BY product_id ) SELECT tp.product_name, tp.total_sold, pr.avg_rating, pr.review_count FROM top_products tp LEFT JOIN product_reviews pr ON tp.product_id = pr.product_id ORDER BY tp.total_sold DESC
```

**优化后SQL:**
```sql
WITH top_products AS (SELECT p.product_id, p.product_name, SUM(oi.quantity) AS total_sold FROM products p JOIN order_items oi ON p.product_id = oi.product_id GROUP BY p.product_id, p.product_name ORDER BY total_sold DESC LIMIT 10), product_reviews AS (SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count FROM reviews GROUP BY product_id) SELECT tp.product_name, tp.total_sold, pr.avg_rating, pr.review_count FROM top_products tp LEFT JOIN product_reviews pr ON tp.product_id = pr.product_id ORDER BY tp.total_sold DESC
```

---

### 测试用例 11

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 3 行, 3 列
- 优化SQL: 3 行, 3 列

**执行时间:**
- 原始: 1106400 纳秒
- 优化后: 727000 纳秒
- 提升: 34.3%

**应用的规则:** RemoveRedundantDistinct, SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH user_purchases AS (    SELECT u.user_id, u.username, COUNT(DISTINCT o.order_id) AS total_orders    FROM users u    JOIN orders o ON u.user_id = o.user_id    GROUP BY u.user_id, u.username ), high_value_users AS (    SELECT up.user_id, up.username, up.total_orders    FROM user_purchases up    WHERE up.total_orders > 5 ), user_product_stats AS (    SELECT hvu.user_id, hvu.username, COUNT(DISTINCT oi.product_id) AS unique_products    FROM high_value_users hvu    JOIN orders o ON hvu.user_id = o.user_id    JOIN order_items oi ON o.order_id = oi.order_id    GROUP BY hvu.user_id, hvu.username ) SELECT ups.username, ups.unique_products, hvu.total_orders FROM user_product_stats ups JOIN high_value_users hvu ON ups.user_id = hvu.user_id ORDER BY ups.unique_products DESC
```

**优化后SQL:**
```sql
WITH user_purchases AS (SELECT u.user_id, u.username, COUNT(DISTINCT o.order_id) AS total_orders FROM users u JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username), high_value_users AS (SELECT up.user_id, up.username, up.total_orders FROM user_purchases up WHERE up.total_orders > 5), user_product_stats AS (SELECT hvu.user_id, hvu.username, COUNT(DISTINCT oi.product_id) AS unique_products FROM high_value_users hvu JOIN orders o ON hvu.user_id = o.user_id JOIN order_items oi ON o.order_id = oi.order_id GROUP BY hvu.user_id, hvu.username) SELECT ups.username, ups.unique_products, hvu.total_orders FROM user_product_stats ups JOIN high_value_users hvu ON ups.user_id = hvu.user_id ORDER BY ups.unique_products DESC
```

---

### 测试用例 12

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 3 列
- 优化SQL: 4 行, 3 列

**执行时间:**
- 原始: 954800 纳秒
- 优化后: 483000 纳秒
- 提升: 49.4%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT DISTINCT p.product_id, p.product_name, p.price FROM products p WHERE p.product_id IN (    SELECT oi.product_id FROM order_items oi    WHERE oi.order_id IN (        SELECT o.order_id FROM orders o        WHERE o.user_id IN (            SELECT u.user_id FROM users u            WHERE u.country = 'USA'        )    ) ) ORDER BY p.price DESC
```

**优化后SQL:**
```sql
SELECT DISTINCT p.product_id, p.product_name, p.price FROM products p WHERE p.product_id IN (SELECT oi.product_id FROM order_items oi WHERE oi.order_id IN (SELECT o.order_id FROM orders o WHERE o.user_id IN (SELECT u.user_id FROM users u WHERE u.country = 'USA'))) ORDER BY p.price DESC
```

---

### 测试用例 13

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 5 列
- 优化SQL: 0 行, 5 列

**执行时间:**
- 原始: 992700 纳秒
- 优化后: 579900 纳秒
- 提升: 41.6%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT u.username, p.product_name, c.category_name, oi.quantity, oi.unit_price FROM users u JOIN orders o ON u.user_id = o.user_id JOIN order_items oi ON o.order_id = oi.order_id JOIN products p ON oi.product_id = p.product_id JOIN categories c ON p.category_id = c.category_id JOIN brands b ON p.brand_id = b.brand_id JOIN inventory i ON p.product_id = i.product_id JOIN payments py ON o.order_id = py.order_id JOIN shipping s ON o.order_id = s.order_id WHERE o.status = 'delivered' AND u.country IN (    SELECT country FROM users GROUP BY country HAVING COUNT(*) > 10 ) ORDER BY o.order_date DESC
```

**优化后SQL:**
```sql
SELECT u.username, p.product_name, c.category_name, oi.quantity, oi.unit_price FROM users u JOIN orders o ON u.user_id = o.user_id JOIN order_items oi ON o.order_id = oi.order_id JOIN products p ON oi.product_id = p.product_id JOIN categories c ON p.category_id = c.category_id JOIN brands b ON p.brand_id = b.brand_id JOIN inventory i ON p.product_id = i.product_id JOIN payments py ON o.order_id = py.order_id JOIN shipping s ON o.order_id = s.order_id WHERE o.status = 'delivered' AND u.country IN (SELECT country FROM users GROUP BY country HAVING COUNT(*) > 10) ORDER BY o.order_date DESC
```

---

### 测试用例 14

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 837500 纳秒
- 优化后: 365700 纳秒
- 提升: 56.3%

**应用的规则:** MergeNestedSubquery, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT c.category_name, COUNT(*) AS product_count, SUM(p.price * i.quantity) AS inventory_value, (SELECT SUM(oi.subtotal) FROM order_items oi  JOIN products p2 ON oi.product_id = p2.product_id  WHERE p2.category_id = c.category_id) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN inventory i ON p.product_id = i.product_id GROUP BY c.category_id, c.category_name HAVING inventory_value > (SELECT AVG(inv_val) FROM (    SELECT SUM(p3.price * i3.quantity) AS inv_val    FROM products p3 JOIN inventory i3 ON p3.product_id = i3.product_id    JOIN categories c3 ON p3.category_id = c3.category_id    GROUP BY c3.category_id ) sub) ORDER BY inventory_value DESC
```

**优化后SQL:**
```sql
SELECT c.category_name, COUNT(*) AS product_count, SUM(p.price * i.quantity) AS inventory_value, (SELECT SUM(oi.subtotal) FROM order_items oi JOIN products p2 ON oi.product_id = p2.product_id WHERE p2.category_id = c.category_id) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN inventory i ON p.product_id = i.product_id GROUP BY c.category_id, c.category_name HAVING inventory_value > (SELECT AVG(inv_val) FROM (SELECT SUM(p3.price * i3.quantity) AS inv_val FROM products p3 JOIN inventory i3 ON p3.product_id = i3.product_id JOIN categories c3 ON p3.category_id = c3.category_id GROUP BY c3.category_id) sub) ORDER BY inventory_value DESC
```

---

### 测试用例 15

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 4 列
- 优化SQL: 1 行, 4 列

**执行时间:**
- 原始: 877500 纳秒
- 优化后: 421200 纳秒
- 提升: 52.0%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT user_id, username, email, created_at FROM users WHERE user_id IN (    SELECT user_id FROM orders GROUP BY user_id HAVING COUNT(*) > 2 ) AND user_id NOT IN (    SELECT user_id FROM reviews WHERE rating < 3 ) ORDER BY created_at DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, email, created_at FROM users WHERE user_id IN (SELECT user_id FROM orders GROUP BY user_id HAVING COUNT(*) > 2) AND user_id NOT IN (SELECT user_id FROM reviews WHERE rating < 3) ORDER BY created_at DESC
```

---

### 测试用例 16

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 623500 纳秒
- 优化后: 116200 纳秒
- 提升: 81.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

**优化后SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

---

### 测试用例 17

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 627500 纳秒
- 优化后: 163100 纳秒
- 提升: 74.0%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

**优化后SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

---

### 测试用例 18

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 5 列
- 优化SQL: 20 行, 5 列

**执行时间:**
- 原始: 926700 纳秒
- 优化后: 409900 纳秒
- 提升: 55.8%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

---

### 测试用例 19

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 787300 纳秒
- 优化后: 312500 纳秒
- 提升: 60.3%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (    SELECT DISTINCT user_id FROM orders ) ORDER BY days_since_joined DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (SELECT DISTINCT user_id FROM orders) ORDER BY days_since_joined DESC
```

---

### 测试用例 20

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 9 行, 3 列
- 优化SQL: 9 行, 3 列

**执行时间:**
- 原始: 971100 纳秒
- 优化后: 562300 纳秒
- 提升: 42.1%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH category_sales AS (    SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales    FROM categories c    JOIN products p ON c.category_id = p.category_id    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY c.category_id, c.category_name ) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

**优化后SQL:**
```sql
WITH category_sales AS (SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

---

### 测试用例 21

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 7 行, 4 列
- 优化SQL: 7 行, 4 列

**执行时间:**
- 原始: 990500 纳秒
- 优化后: 565200 纳秒
- 提升: 42.9%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

---

### 测试用例 22

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 4 列
- 优化SQL: 20 行, 4 列

**执行时间:**
- 原始: 1081000 纳秒
- 优化后: 643800 纳秒
- 提升: 40.4%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

---

### 测试用例 23

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 1044500 纳秒
- 优化后: 460600 纳秒
- 提升: 55.9%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

---

### 测试用例 24

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 2 列
- 优化SQL: 20 行, 2 列

**执行时间:**
- 原始: 889000 纳秒
- 优化后: 410800 纳秒
- 提升: 53.8%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH active_products AS (    SELECT product_id, product_name, category_id FROM products WHERE is_active = 1 ), category_counts AS (    SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id ) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

**优化后SQL:**
```sql
WITH active_products AS (SELECT product_id, product_name, category_id FROM products WHERE is_active = 1), category_counts AS (SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

---

### 测试用例 25

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 27 行, 4 列
- 优化SQL: 27 行, 4 列

**执行时间:**
- 原始: 964100 纳秒
- 优化后: 503600 纳秒
- 提升: 47.8%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (    SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id ) ORDER BY u.user_id, o.total_amount DESC
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id) ORDER BY u.user_id, o.total_amount DESC
```

---

### 测试用例 26

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 1009400 纳秒
- 优化后: 463800 纳秒
- 提升: 54.1%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

**优化后SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

---

### 测试用例 27

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 1057700 纳秒
- 优化后: 299100 纳秒
- 提升: 71.7%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (    SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%' ) AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

**优化后SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%') AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

---

### 测试用例 28

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 3 列
- 优化SQL: 1 行, 3 列

**执行时间:**
- 原始: 785000 纳秒
- 优化后: 224100 纳秒
- 提升: 71.5%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH recent_orders AS (    SELECT * FROM orders WHERE order_date >= date('now', '-90 days') ) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

**优化后SQL:**
```sql
WITH recent_orders AS (SELECT * FROM orders WHERE order_date >= date('now', '-90 days')) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

---

### 测试用例 29

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 5 行, 3 列
- 优化SQL: 5 行, 3 列

**执行时间:**
- 原始: 635700 纳秒
- 优化后: 162900 纳秒
- 提升: 74.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

**优化后SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

---

### 测试用例 30

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 6 行, 4 列
- 优化SQL: 6 行, 4 列

**执行时间:**
- 原始: 927400 纳秒
- 优化后: 423600 纳秒
- 提升: 54.3%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

**优化后SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

---

### 测试用例 31

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 722500 纳秒
- 优化后: 126900 纳秒
- 提升: 82.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

**优化后SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

---

### 测试用例 32

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 690000 纳秒
- 优化后: 168700 纳秒
- 提升: 75.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

**优化后SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

---

### 测试用例 33

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 5 列
- 优化SQL: 20 行, 5 列

**执行时间:**
- 原始: 847100 纳秒
- 优化后: 377200 纳秒
- 提升: 55.5%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

---

### 测试用例 34

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 809200 纳秒
- 优化后: 482400 纳秒
- 提升: 40.4%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (    SELECT DISTINCT user_id FROM orders ) ORDER BY days_since_joined DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (SELECT DISTINCT user_id FROM orders) ORDER BY days_since_joined DESC
```

---

### 测试用例 35

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 9 行, 3 列
- 优化SQL: 9 行, 3 列

**执行时间:**
- 原始: 974100 纳秒
- 优化后: 582800 纳秒
- 提升: 40.2%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH category_sales AS (    SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales    FROM categories c    JOIN products p ON c.category_id = p.category_id    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY c.category_id, c.category_name ) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

**优化后SQL:**
```sql
WITH category_sales AS (SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

---

### 测试用例 36

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 7 行, 4 列
- 优化SQL: 7 行, 4 列

**执行时间:**
- 原始: 936700 纳秒
- 优化后: 405800 纳秒
- 提升: 56.7%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

---

### 测试用例 37

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 4 列
- 优化SQL: 20 行, 4 列

**执行时间:**
- 原始: 999100 纳秒
- 优化后: 526600 纳秒
- 提升: 47.3%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

---

### 测试用例 38

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 874800 纳秒
- 优化后: 468500 纳秒
- 提升: 46.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

---

### 测试用例 39

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 2 列
- 优化SQL: 20 行, 2 列

**执行时间:**
- 原始: 985100 纳秒
- 优化后: 325400 纳秒
- 提升: 67.0%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH active_products AS (    SELECT product_id, product_name, category_id FROM products WHERE is_active = 1 ), category_counts AS (    SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id ) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

**优化后SQL:**
```sql
WITH active_products AS (SELECT product_id, product_name, category_id FROM products WHERE is_active = 1), category_counts AS (SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

---

### 测试用例 40

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 27 行, 4 列
- 优化SQL: 27 行, 4 列

**执行时间:**
- 原始: 1093500 纳秒
- 优化后: 501700 纳秒
- 提升: 54.1%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (    SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id ) ORDER BY u.user_id, o.total_amount DESC
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id) ORDER BY u.user_id, o.total_amount DESC
```

---

### 测试用例 41

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 1558600 纳秒
- 优化后: 590800 纳秒
- 提升: 62.1%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

**优化后SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

---

### 测试用例 42

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 751200 纳秒
- 优化后: 460900 纳秒
- 提升: 38.6%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (    SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%' ) AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

**优化后SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%') AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

---

### 测试用例 43

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 3 列
- 优化SQL: 1 行, 3 列

**执行时间:**
- 原始: 840700 纳秒
- 优化后: 376400 纳秒
- 提升: 55.2%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH recent_orders AS (    SELECT * FROM orders WHERE order_date >= date('now', '-90 days') ) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

**优化后SQL:**
```sql
WITH recent_orders AS (SELECT * FROM orders WHERE order_date >= date('now', '-90 days')) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

---

### 测试用例 44

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 5 行, 3 列
- 优化SQL: 5 行, 3 列

**执行时间:**
- 原始: 756700 纳秒
- 优化后: 267500 纳秒
- 提升: 64.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

**优化后SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

---

### 测试用例 45

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 6 行, 4 列
- 优化SQL: 6 行, 4 列

**执行时间:**
- 原始: 961900 纳秒
- 优化后: 626500 纳秒
- 提升: 34.9%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

**优化后SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

---

### 测试用例 46

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 579500 纳秒
- 优化后: 112000 纳秒
- 提升: 80.7%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

**优化后SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

---

### 测试用例 47

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 797300 纳秒
- 优化后: 201500 纳秒
- 提升: 74.7%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

**优化后SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

---

### 测试用例 48

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 5 列
- 优化SQL: 20 行, 5 列

**执行时间:**
- 原始: 949700 纳秒
- 优化后: 410000 纳秒
- 提升: 56.8%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

---

### 测试用例 49

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 843300 纳秒
- 优化后: 320700 纳秒
- 提升: 62.0%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (    SELECT DISTINCT user_id FROM orders ) ORDER BY days_since_joined DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (SELECT DISTINCT user_id FROM orders) ORDER BY days_since_joined DESC
```

---

### 测试用例 50

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 9 行, 3 列
- 优化SQL: 9 行, 3 列

**执行时间:**
- 原始: 1103400 纳秒
- 优化后: 509400 纳秒
- 提升: 53.8%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH category_sales AS (    SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales    FROM categories c    JOIN products p ON c.category_id = p.category_id    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY c.category_id, c.category_name ) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

**优化后SQL:**
```sql
WITH category_sales AS (SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

---

### 测试用例 51

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 7 行, 4 列
- 优化SQL: 7 行, 4 列

**执行时间:**
- 原始: 806500 纳秒
- 优化后: 303500 纳秒
- 提升: 62.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

---

### 测试用例 52

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 4 列
- 优化SQL: 20 行, 4 列

**执行时间:**
- 原始: 1066300 纳秒
- 优化后: 655500 纳秒
- 提升: 38.5%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

---

### 测试用例 53

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 1004100 纳秒
- 优化后: 445900 纳秒
- 提升: 55.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

---

### 测试用例 54

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 2 列
- 优化SQL: 20 行, 2 列

**执行时间:**
- 原始: 907500 纳秒
- 优化后: 390900 纳秒
- 提升: 56.9%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH active_products AS (    SELECT product_id, product_name, category_id FROM products WHERE is_active = 1 ), category_counts AS (    SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id ) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

**优化后SQL:**
```sql
WITH active_products AS (SELECT product_id, product_name, category_id FROM products WHERE is_active = 1), category_counts AS (SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

---

### 测试用例 55

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 27 行, 4 列
- 优化SQL: 27 行, 4 列

**执行时间:**
- 原始: 1038700 纳秒
- 优化后: 539000 纳秒
- 提升: 48.1%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (    SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id ) ORDER BY u.user_id, o.total_amount DESC
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id) ORDER BY u.user_id, o.total_amount DESC
```

---

### 测试用例 56

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 1405100 纳秒
- 优化后: 644800 纳秒
- 提升: 54.1%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

**优化后SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

---

### 测试用例 57

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 1074600 纳秒
- 优化后: 377000 纳秒
- 提升: 64.9%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (    SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%' ) AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

**优化后SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%') AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

---

### 测试用例 58

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 3 列
- 优化SQL: 1 行, 3 列

**执行时间:**
- 原始: 733300 纳秒
- 优化后: 294900 纳秒
- 提升: 59.8%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH recent_orders AS (    SELECT * FROM orders WHERE order_date >= date('now', '-90 days') ) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

**优化后SQL:**
```sql
WITH recent_orders AS (SELECT * FROM orders WHERE order_date >= date('now', '-90 days')) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

---

### 测试用例 59

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 5 行, 3 列
- 优化SQL: 5 行, 3 列

**执行时间:**
- 原始: 688600 纳秒
- 优化后: 190500 纳秒
- 提升: 72.3%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

**优化后SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

---

### 测试用例 60

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 6 行, 4 列
- 优化SQL: 6 行, 4 列

**执行时间:**
- 原始: 1506000 纳秒
- 优化后: 629000 纳秒
- 提升: 58.2%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

**优化后SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

---

### 测试用例 61

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 687900 纳秒
- 优化后: 129600 纳秒
- 提升: 81.2%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

**优化后SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

---

### 测试用例 62

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 754700 纳秒
- 优化后: 306900 纳秒
- 提升: 59.3%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

**优化后SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

---

### 测试用例 63

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 5 列
- 优化SQL: 20 行, 5 列

**执行时间:**
- 原始: 1254200 纳秒
- 优化后: 644200 纳秒
- 提升: 48.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

---

### 测试用例 64

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 984800 纳秒
- 优化后: 485600 纳秒
- 提升: 50.7%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (    SELECT DISTINCT user_id FROM orders ) ORDER BY days_since_joined DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (SELECT DISTINCT user_id FROM orders) ORDER BY days_since_joined DESC
```

---

### 测试用例 65

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 9 行, 3 列
- 优化SQL: 9 行, 3 列

**执行时间:**
- 原始: 922300 纳秒
- 优化后: 467900 纳秒
- 提升: 49.3%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH category_sales AS (    SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales    FROM categories c    JOIN products p ON c.category_id = p.category_id    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY c.category_id, c.category_name ) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

**优化后SQL:**
```sql
WITH category_sales AS (SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

---

### 测试用例 66

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 7 行, 4 列
- 优化SQL: 7 行, 4 列

**执行时间:**
- 原始: 780300 纳秒
- 优化后: 421600 纳秒
- 提升: 46.0%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

---

### 测试用例 67

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 4 列
- 优化SQL: 20 行, 4 列

**执行时间:**
- 原始: 1360300 纳秒
- 优化后: 683300 纳秒
- 提升: 49.8%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

---

### 测试用例 68

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 1024300 纳秒
- 优化后: 615900 纳秒
- 提升: 39.9%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

---

### 测试用例 69

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 2 列
- 优化SQL: 20 行, 2 列

**执行时间:**
- 原始: 850200 纳秒
- 优化后: 287600 纳秒
- 提升: 66.2%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH active_products AS (    SELECT product_id, product_name, category_id FROM products WHERE is_active = 1 ), category_counts AS (    SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id ) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

**优化后SQL:**
```sql
WITH active_products AS (SELECT product_id, product_name, category_id FROM products WHERE is_active = 1), category_counts AS (SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

---

### 测试用例 70

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 27 行, 4 列
- 优化SQL: 27 行, 4 列

**执行时间:**
- 原始: 1192900 纳秒
- 优化后: 438700 纳秒
- 提升: 63.2%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (    SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id ) ORDER BY u.user_id, o.total_amount DESC
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id) ORDER BY u.user_id, o.total_amount DESC
```

---

### 测试用例 71

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 1406300 纳秒
- 优化后: 539400 纳秒
- 提升: 61.6%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

**优化后SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

---

### 测试用例 72

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 678100 纳秒
- 优化后: 235000 纳秒
- 提升: 65.3%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (    SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%' ) AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

**优化后SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%') AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

---

### 测试用例 73

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 3 列
- 优化SQL: 1 行, 3 列

**执行时间:**
- 原始: 706100 纳秒
- 优化后: 185900 纳秒
- 提升: 73.7%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH recent_orders AS (    SELECT * FROM orders WHERE order_date >= date('now', '-90 days') ) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

**优化后SQL:**
```sql
WITH recent_orders AS (SELECT * FROM orders WHERE order_date >= date('now', '-90 days')) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

---

### 测试用例 74

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 5 行, 3 列
- 优化SQL: 5 行, 3 列

**执行时间:**
- 原始: 546500 纳秒
- 优化后: 142600 纳秒
- 提升: 73.9%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

**优化后SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

---

### 测试用例 75

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 6 行, 4 列
- 优化SQL: 6 行, 4 列

**执行时间:**
- 原始: 973900 纳秒
- 优化后: 428000 纳秒
- 提升: 56.1%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

**优化后SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

---

### 测试用例 76

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 986000 纳秒
- 优化后: 321600 纳秒
- 提升: 67.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

**优化后SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

---

### 测试用例 77

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 896300 纳秒
- 优化后: 356400 纳秒
- 提升: 60.2%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

**优化后SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

---

### 测试用例 78

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 5 列
- 优化SQL: 20 行, 5 列

**执行时间:**
- 原始: 903800 纳秒
- 优化后: 477100 纳秒
- 提升: 47.2%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

---

### 测试用例 79

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 790900 纳秒
- 优化后: 362200 纳秒
- 提升: 54.2%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (    SELECT DISTINCT user_id FROM orders ) ORDER BY days_since_joined DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (SELECT DISTINCT user_id FROM orders) ORDER BY days_since_joined DESC
```

---

### 测试用例 80

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 9 行, 3 列
- 优化SQL: 9 行, 3 列

**执行时间:**
- 原始: 814600 纳秒
- 优化后: 340900 纳秒
- 提升: 58.2%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH category_sales AS (    SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales    FROM categories c    JOIN products p ON c.category_id = p.category_id    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY c.category_id, c.category_name ) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

**优化后SQL:**
```sql
WITH category_sales AS (SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

---

### 测试用例 81

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 7 行, 4 列
- 优化SQL: 7 行, 4 列

**执行时间:**
- 原始: 950400 纳秒
- 优化后: 611300 纳秒
- 提升: 35.7%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

---

### 测试用例 82

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 4 列
- 优化SQL: 20 行, 4 列

**执行时间:**
- 原始: 997500 纳秒
- 优化后: 602100 纳秒
- 提升: 39.6%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

---

### 测试用例 83

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 901000 纳秒
- 优化后: 378500 纳秒
- 提升: 58.0%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

---

### 测试用例 84

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 2 列
- 优化SQL: 20 行, 2 列

**执行时间:**
- 原始: 907900 纳秒
- 优化后: 278000 纳秒
- 提升: 69.4%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH active_products AS (    SELECT product_id, product_name, category_id FROM products WHERE is_active = 1 ), category_counts AS (    SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id ) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

**优化后SQL:**
```sql
WITH active_products AS (SELECT product_id, product_name, category_id FROM products WHERE is_active = 1), category_counts AS (SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

---

### 测试用例 85

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 27 行, 4 列
- 优化SQL: 27 行, 4 列

**执行时间:**
- 原始: 889900 纳秒
- 优化后: 392900 纳秒
- 提升: 55.8%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (    SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id ) ORDER BY u.user_id, o.total_amount DESC
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id) ORDER BY u.user_id, o.total_amount DESC
```

---

### 测试用例 86

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 894400 纳秒
- 优化后: 468400 纳秒
- 提升: 47.6%

**应用的规则:** OptimizeJoinOrder

**原始SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

**优化后SQL:**
```sql
SELECT b.brand_name, COUNT(DISTINCT p.product_id) AS product_count, COUNT(DISTINCT o.order_id) AS order_count FROM brands b LEFT JOIN products p ON b.brand_id = p.brand_id LEFT JOIN order_items oi ON p.product_id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.order_id GROUP BY b.brand_id, b.brand_name ORDER BY product_count DESC
```

---

### 测试用例 87

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 878800 纳秒
- 优化后: 381000 纳秒
- 提升: 56.6%

**应用的规则:** UseExistsInsteadOfIn

**原始SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (    SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%' ) AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

**优化后SQL:**
```sql
SELECT product_id, product_name, price FROM products WHERE category_id IN (SELECT category_id FROM categories WHERE category_name LIKE 'Electronics%') AND price > (SELECT AVG(price) FROM products) ORDER BY price DESC
```

---

### 测试用例 88

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 1 行, 3 列
- 优化SQL: 1 行, 3 列

**执行时间:**
- 原始: 614700 纳秒
- 优化后: 203800 纳秒
- 提升: 66.8%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH recent_orders AS (    SELECT * FROM orders WHERE order_date >= date('now', '-90 days') ) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

**优化后SQL:**
```sql
WITH recent_orders AS (SELECT * FROM orders WHERE order_date >= date('now', '-90 days')) SELECT strftime('%Y-%m', ro.order_date) AS month, COUNT(*) AS order_count, SUM(ro.total_amount) AS monthly_revenue FROM recent_orders ro GROUP BY strftime('%Y-%m', ro.order_date) ORDER BY month DESC
```

---

### 测试用例 89

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 5 行, 3 列
- 优化SQL: 5 行, 3 列

**执行时间:**
- 原始: 907100 纳秒
- 优化后: 286400 纳秒
- 提升: 68.4%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

**优化后SQL:**
```sql
SELECT r.rating, COUNT(*) AS count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reviews) AS percentage FROM reviews r GROUP BY r.rating ORDER BY r.rating DESC
```

---

### 测试用例 90

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 6 行, 4 列
- 优化SQL: 6 行, 4 列

**执行时间:**
- 原始: 816400 纳秒
- 优化后: 324100 纳秒
- 提升: 60.3%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

**优化后SQL:**
```sql
SELECT u.country, COUNT(DISTINCT u.user_id) AS user_count, COUNT(DISTINCT o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.country ORDER BY total_revenue DESC
```

---

### 测试用例 91

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 0 行, 3 列
- 优化SQL: 0 行, 3 列

**执行时间:**
- 原始: 603500 纳秒
- 优化后: 119200 纳秒
- 提升: 80.2%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

**优化后SQL:**
```sql
SELECT s.carrier, COUNT(*) AS shipment_count, AVG(julianday(actual_delivery_date) - julianday(shipping_date)) AS avg_days_to_deliver FROM shipping s WHERE actual_delivery_date IS NOT NULL AND shipping_date IS NOT NULL GROUP BY s.carrier ORDER BY avg_days_to_deliver
```

---

### 测试用例 92

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 588400 纳秒
- 优化后: 155700 纳秒
- 提升: 73.5%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

**优化后SQL:**
```sql
SELECT payment_method, COUNT(*) AS usage_count, SUM(amount) AS total_amount, AVG(amount) AS avg_amount FROM payments WHERE status = 'completed' GROUP BY payment_method ORDER BY usage_count DESC
```

---

### 测试用例 93

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 5 列
- 优化SQL: 20 行, 5 列

**执行时间:**
- 原始: 979800 纳秒
- 优化后: 405800 纳秒
- 提升: 58.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, p.price, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id AND rating = 5) AS five_star_reviews, (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) AS total_reviews FROM products p WHERE p.is_active = 1 ORDER BY five_star_reviews DESC
```

---

### 测试用例 94

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 10 行, 3 列
- 优化SQL: 10 行, 3 列

**执行时间:**
- 原始: 893300 纳秒
- 优化后: 330800 纳秒
- 提升: 63.0%

**应用的规则:** UseExistsInsteadOfIn, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (    SELECT DISTINCT user_id FROM orders ) ORDER BY days_since_joined DESC
```

**优化后SQL:**
```sql
SELECT user_id, username, julianday('now') - julianday(created_at) AS days_since_joined FROM users WHERE user_id IN (SELECT DISTINCT user_id FROM orders) ORDER BY days_since_joined DESC
```

---

### 测试用例 95

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 9 行, 3 列
- 优化SQL: 9 行, 3 列

**执行时间:**
- 原始: 941800 纳秒
- 优化后: 544600 纳秒
- 提升: 42.2%

**应用的规则:** SimplifyWithClause, OptimizeJoinOrder

**原始SQL:**
```sql
WITH category_sales AS (    SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales    FROM categories c    JOIN products p ON c.category_id = p.category_id    JOIN order_items oi ON p.product_id = oi.product_id    GROUP BY c.category_id, c.category_name ) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

**优化后SQL:**
```sql
WITH category_sales AS (SELECT c.category_id, c.category_name, SUM(oi.subtotal) AS total_sales FROM categories c JOIN products p ON c.category_id = p.category_id JOIN order_items oi ON p.product_id = oi.product_id GROUP BY c.category_id, c.category_name) SELECT category_name, total_sales, total_sales / (SELECT SUM(total_sales) FROM category_sales) AS percentage FROM category_sales ORDER BY percentage DESC
```

---

### 测试用例 96

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 7 行, 4 列
- 优化SQL: 7 行, 4 列

**执行时间:**
- 原始: 680300 纳秒
- 优化后: 254400 纳秒
- 提升: 62.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

**优化后SQL:**
```sql
SELECT order_id, order_date, total_amount, RANK() OVER (ORDER BY total_amount DESC) AS sales_rank FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 20
```

---

### 测试用例 97

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 4 列
- 优化SQL: 20 行, 4 列

**执行时间:**
- 原始: 887600 纳秒
- 优化后: 449000 纳秒
- 提升: 49.4%

**应用的规则:** RemoveRedundantDistinct, OptimizeJoinOrder

**原始SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

**优化后SQL:**
```sql
SELECT p.product_id, p.product_name, b.brand_name, COUNT(DISTINCT o.order_id) AS order_count FROM products p JOIN brands b ON p.brand_id = b.brand_id JOIN order_items oi ON p.product_id = oi.product_id JOIN orders o ON oi.order_id = o.order_id WHERE o.order_date >= date('now', '-30 days') GROUP BY p.product_id, p.product_name, b.brand_name ORDER BY order_count DESC
```

---

### 测试用例 98

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 4 行, 4 列
- 优化SQL: 4 行, 4 列

**执行时间:**
- 原始: 742900 纳秒
- 优化后: 396400 纳秒
- 提升: 46.6%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.order_id END) AS completed_orders, COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.order_id END) AS pending_orders FROM users u LEFT JOIN orders o ON u.user_id = o.user_id GROUP BY u.user_id, u.username HAVING completed_orders > 0
```

---

### 测试用例 99

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 20 行, 2 列
- 优化SQL: 20 行, 2 列

**执行时间:**
- 原始: 802200 纳秒
- 优化后: 338200 纳秒
- 提升: 57.8%

**应用的规则:** SimplifyWithClause

**原始SQL:**
```sql
WITH active_products AS (    SELECT product_id, product_name, category_id FROM products WHERE is_active = 1 ), category_counts AS (    SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id ) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

**优化后SQL:**
```sql
WITH active_products AS (SELECT product_id, product_name, category_id FROM products WHERE is_active = 1), category_counts AS (SELECT category_id, COUNT(*) AS count FROM active_products GROUP BY category_id) SELECT ap.product_name, cc.count AS category_product_count FROM active_products ap JOIN category_counts cc ON ap.category_id = cc.category_id ORDER BY cc.count DESC
```

---

### 测试用例 100

**状态:** ✅ 成功

**结果一致:** ✅ 是

**查询结果统计:**
- 原始SQL: 27 行, 4 列
- 优化SQL: 27 行, 4 列

**执行时间:**
- 原始: 912300 纳秒
- 优化后: 643000 纳秒
- 提升: 29.5%

**应用的规则:** 无

**原始SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (    SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id ) ORDER BY u.user_id, o.total_amount DESC
```

**优化后SQL:**
```sql
SELECT u.user_id, u.username, o.order_id, o.total_amount FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.total_amount > (SELECT AVG(total_amount) FROM orders WHERE user_id = u.user_id) ORDER BY u.user_id, o.total_amount DESC
```

---

