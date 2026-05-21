-- 1. 简化COUNT表达式(SimplifyCountStar)
-- 优化前: COUNT(1) -> 优化后: COUNT(*)
SELECT COUNT(1) FROM users WHERE is_active = 1;

-- 2. 移除冗余DISTINCT(RemoveRedundantDistinct)
-- 优化前: DISTINCT + GROUP BY -> 优化后: 移除DISTINCT
SELECT DISTINCT user_id FROM orders GROUP BY user_id;

-- 3. 移除不必要ORDER BY(RemoveUnnecessaryOrderBy)
-- 优化前: COUNT(*) + ORDER BY -> 优化后: 移除ORDER BY
SELECT COUNT(*) FROM products WHERE is_active = 1 ORDER BY product_name;

-- 4. 移除冗余LIMIT(RemoveRedundantLimit)
-- 优化前: LIMIT 1000000 -> 优化后: 移除过大的LIMIT
SELECT * FROM users LIMIT 1000000;

-- 5. 简化WITH子句(SimplifyWithClause)
-- 优化前: WITH子句只使用一次 -> 优化后: 内联到主查询
WITH active_users AS (SELECT * FROM users WHERE is_active = 1)
SELECT COUNT(*) FROM active_users;

-- 6. 合并嵌套子查询(MergeNestedSubquery)
-- 优化前: 嵌套子查询 -> 优化后: 合并为单级查询
SELECT * FROM (SELECT * FROM (SELECT * FROM users WHERE is_active = 1) WHERE country = 'USA') WHERE loyalty_tier = 'Gold';

-- 7. IN转EXISTS(UseExistsInsteadOfIn)
-- 优化前: IN子查询 -> 优化后: EXISTS子查询
SELECT * FROM orders WHERE user_id IN (SELECT user_id FROM users WHERE country = 'USA');

-- 8. 优化JOIN顺序(OptimizeJoinOrder)
-- 优化前: 大表在前 -> 优化后: 小表在前
SELECT * FROM orders 
JOIN order_items ON orders.order_id = order_items.order_id 
JOIN products ON order_items.product_id = products.product_id 
WHERE orders.order_status = 'delivered';

-- 9. 移除恒等表达式(RemoveAlwaysTrueOrFalse)
-- 优化前: WHERE 1=1 AND ... -> 优化后: 移除1=1
SELECT * FROM users WHERE 1=1 AND is_active = 1 AND 5>3;

-- 10. 移除子查询ORDER BY(SubqueryOrderByRemoval)
-- 优化前: COUNT(*) FROM (SELECT ... ORDER BY) -> 优化后: 移除ORDER BY
SELECT COUNT(*) FROM (SELECT * FROM users WHERE country = 'USA' ORDER BY username DESC);

-- 11. 谓词下推(PredicatePushdown)
-- 优化前: 外层WHERE -> 优化后: 下推到子查询
SELECT * FROM (SELECT * FROM orders WHERE order_status = 'shipped') 
WHERE shipping_amount > 100;

-- 12. 常量折叠(ConstantFolding)
-- 优化前: WHERE price > 100+50 -> 优化后: WHERE price > 150
SELECT * FROM products WHERE price > 100 + 50;

-- 13. 合并UNION(MergeUnion)
-- 优化前: 多个UNION -> 优化后: 合并
SELECT product_name FROM products WHERE category_id = 1
UNION
SELECT product_name FROM products WHERE category_id = 2
UNION
SELECT product_name FROM products WHERE category_id = 3;

-- 14. COUNT(*)子查询优化(CountStarFromSubquery)
-- 优化前: COUNT(*) FROM (SELECT * FROM ...) -> 优化后: COUNT(*) FROM ...
SELECT COUNT(*) FROM (SELECT * FROM users WHERE country = 'USA');

-- 15. 算术比较优化(ArithmeticComparisonOptimize)
-- 优化前: price * 1 > 0 -> 优化后: price > 0
SELECT * FROM products WHERE price * 1 > 0;

-- 16. LIKE模式优化(LikePatternOptimize)
-- 优化前: LIKE 'exact_match' -> 优化后: = 'exact_match'
SELECT * FROM products WHERE product_name LIKE 'Premium Widget';

-- 17. NOT IN转NOT EXISTS(NotInToNotExists)
-- 优化前: NOT IN子查询 -> 优化后: NOT EXISTS子查询
SELECT * FROM users WHERE user_id NOT IN (SELECT user_id FROM orders WHERE order_status = 'cancelled');

-- 18. CASE表达式简化(CaseSimplification)
-- 优化前: CASE WHEN 1=1 THEN x ELSE x END -> 优化后: x
SELECT 
  CASE WHEN 1=1 THEN product_name ELSE product_name END AS name
FROM products;

-- 19. 子查询转JOIN(SubqueryToJoin)
-- 优化前: EXISTS子查询 -> 优化后: JOIN
SELECT * FROM orders 
WHERE EXISTS (SELECT 1 FROM order_items WHERE orders.order_id = order_items.order_id AND quantity > 1);

-- 20. 移除冗余JOIN(RemoveRedundantJoins)
-- 优化前: JOIN后未使用 -> 优化后: 移除JOIN
SELECT users.username 
FROM users 
JOIN orders ON users.user_id = orders.user_id 
JOIN payments ON orders.order_id = payments.order_id
WHERE users.country = 'USA';

-- 21. 列剪裁(ColumnPruning)
-- 优化前: SELECT * -> 优化后: SELECT 所需列
SELECT * FROM users WHERE user_id = 1;

-- 22. NULL检查消除(NullCheckElimination)
-- 优化前: 对非空列的NULL检查 -> 优化后: 移除检查
SELECT * FROM users WHERE username IS NOT NULL AND is_active = 1;

-- 23. OR条件重写(OrConditionRewrite)
-- 优化前: 多个OR条件 -> 优化后: UNION
SELECT * FROM orders 
WHERE order_status = 'pending' 
   OR order_status = 'processing' 
   OR order_status = 'shipped';

-- 24. Oracle特定优化(OracleSpecificOptimization)
-- 优化前: NOW() -> 优化后: SYSDATE (Oracle)
SELECT * FROM orders WHERE created_at > DATETIME('now');

-- 25. MySQL特定优化(MySqlSpecificOptimization)
-- 优化前: SYSDATE -> 优化后: NOW() (MySQL)
SELECT * FROM orders WHERE created_at > DATETIME('now');

-- 26. PostgreSQL特定优化(PostgreSqlSpecificOptimization)
-- 优化前: IFNULL -> 优化后: COALESCE (PostgreSQL)
SELECT COALESCE(email, 'no-email@example.com') FROM users;

-- 27. SQLite特定优化(SqliteSpecificOptimization)
-- 优化前: NOW() -> 优化后: DATETIME('now')
SELECT * FROM orders WHERE order_date > DATETIME('now');
