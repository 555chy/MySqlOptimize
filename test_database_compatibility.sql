-- Oracle特定语法测试
SELECT * FROM users WHERE ROWNUM <= 10;

-- MySQL/LINUXite语法测试
SELECT * FROM users LIMIT 10;

-- 带OFFSET的LIMIT测试
SELECT * FROM users LIMIT 10 OFFSET 20;

-- TOP N语法测试
SELECT TOP 10 * FROM users;

-- FETCH FIRST语法测试
SELECT * FROM users FETCH FIRST 10 ROWS ONLY;

-- 字符串拼接测试
SELECT first_name || ' ' || last_name FROM users;

-- 日期函数测试
SELECT * FROM orders WHERE created_at > NOW();
SELECT * FROM orders WHERE created_at > CURRENT_TIMESTAMP;

-- COUNT函数优化测试
SELECT COUNT(1) FROM users;

-- 恒等表达式测试
SELECT * FROM users WHERE 1=1 AND id > 100;

-- 子查询测试
SELECT COUNT(*) FROM (SELECT * FROM users);
