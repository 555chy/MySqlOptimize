
-- Query 1: 简单查询，无需优化
SELECT user_id, username FROM users;

-- Query 2: 需要优化的查询
SELECT DISTINCT u.user_id FROM users u WHERE u.is_active = 1;
