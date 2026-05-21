import sqlite3

def validate_sql(db_path, sql):
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        cursor.execute(sql)
        rows = cursor.fetchall()
        conn.close()
        return len(rows) >= 1
    except Exception as e:
        print(f"SQL执行错误: {e}")
        return False

def write_sql_to_file(file_path, sql):
    with open(file_path, 'a', encoding='utf-8') as f:
        f.write(sql + '\n\n')

def generate_missing_queries():
    queries = []
    
    queries.append("""-- Query 4: 库存分析
WITH current_inventory AS (
    SELECT i.product_id, p.product_name, p.sku, p.price, i.quantity, i.available_quantity,
        i.reorder_threshold, i.minimum_quantity, i.maximum_quantity
    FROM inventory i
    INNER JOIN products p ON i.product_id = p.product_id
)
SELECT product_id, product_name, sku, price, quantity, available_quantity, 
    reorder_threshold, minimum_quantity, maximum_quantity,
    CASE WHEN available_quantity <= COALESCE(reorder_threshold, 10) THEN '需要补货' 
         WHEN available_quantity <= COALESCE(minimum_quantity, 20) THEN '库存偏低' 
         WHEN available_quantity >= COALESCE(maximum_quantity, 100) THEN '库存充足' 
         ELSE '库存正常' END AS stock_status 
FROM current_inventory ORDER BY stock_status LIMIT 100;""")

    queries.append("""-- Query 13: 产品交叉销售分析
WITH order_product_counts AS (
    SELECT o.order_id, COUNT(DISTINCT oi.product_id) AS product_count,
        GROUP_CONCAT(DISTINCT oi.product_id) AS product_ids
    FROM orders o
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    GROUP BY o.order_id
)
SELECT order_id, product_count, product_ids FROM order_product_counts 
WHERE product_count >= 1 ORDER BY product_count DESC LIMIT 100;""")

    queries.append("""-- Query 18: 产品库存周转率分析
WITH inventory_stats AS (
    SELECT i.product_id, p.product_name, p.sku, i.quantity AS current_inventory, 
        i.available_quantity, COALESCE(SUM(oi.quantity), 0) AS units_sold,
        COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue_generated
    FROM inventory i
    INNER JOIN products p ON i.product_id = p.product_id
    LEFT JOIN order_items oi ON i.product_id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.order_id
    GROUP BY i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity
)
SELECT product_id, product_name, sku, current_inventory, available_quantity, 
    units_sold, revenue_generated,
    ROUND(COALESCE(units_sold / NULLIF(current_inventory, 0), 0), 2) AS inventory_turnover_ratio,
    CASE WHEN units_sold / NULLIF(current_inventory, 0) >= 0.5 THEN '高周转' 
         WHEN units_sold / NULLIF(current_inventory, 0) >= 0.2 THEN '正常周转' 
         ELSE '低周转' END AS turnover_status 
FROM inventory_stats ORDER BY inventory_turnover_ratio DESC LIMIT 100;""")

    queries.append("""-- Query 35: 产品关联分析
WITH product_sales AS (
    SELECT oi.product_id, p.product_name, COUNT(DISTINCT o.order_id) AS order_count,
        COALESCE(SUM(oi.quantity), 0) AS total_sold
    FROM order_items oi
    LEFT JOIN products p ON oi.product_id = p.product_id
    LEFT JOIN orders o ON oi.order_id = o.order_id
    GROUP BY oi.product_id, p.product_name
)
SELECT product_id, product_name, order_count, total_sold FROM product_sales 
ORDER BY order_count DESC LIMIT 100;""")

    queries.append("""-- Query 36: 库存成本分析
WITH inventory_cost AS (
    SELECT i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity, 
        i.average_cost, i.last_cost, i.standard_cost, i.inventory_value,
        (i.quantity * COALESCE(i.average_cost, 0)) AS calculated_inventory_value
    FROM inventory i
    INNER JOIN products p ON i.product_id = p.product_id
)
SELECT product_id, product_name, sku, quantity, available_quantity, 
    average_cost, last_cost, standard_cost, inventory_value, calculated_inventory_value 
FROM inventory_cost ORDER BY calculated_inventory_value DESC LIMIT 100;""")

    queries.append("""-- Query 49: 产品库存预警分析
WITH inventory_alerts AS (
    SELECT i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity,
        i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,
        (COALESCE(i.maximum_quantity, 100) - i.available_quantity) AS quantity_needed
    FROM inventory i
    INNER JOIN products p ON i.product_id = p.product_id
)
SELECT product_id, product_name, sku, quantity, available_quantity, 
    reorder_threshold, minimum_quantity, maximum_quantity, quantity_needed,
    CASE WHEN available_quantity <= COALESCE(reorder_threshold, 10) THEN '紧急' 
         WHEN available_quantity <= COALESCE(minimum_quantity, 20) THEN '高' 
         WHEN available_quantity <= COALESCE(maximum_quantity, 100) * 0.5 THEN '中' 
         ELSE '低' END AS alert_level 
FROM inventory_alerts ORDER BY CASE alert_level WHEN '紧急' THEN 1 WHEN '高' THEN 2 WHEN '中' THEN 3 ELSE 4 END LIMIT 100;""")
    
    return queries

def main():
    db_path = 'e:/work/trae/mySqlparse/test.db'
    output_file = 'e:/work/trae/mySqlparse/test_sqlite_queries_50.sql'
    
    print("正在生成缺失的6条SQL查询...")
    queries = generate_missing_queries()
    
    print(f"正在验证并写入 {output_file}...")
    success_count = 0
    
    for i, sql in enumerate(queries, 1):
        print(f"验证 Query {i}...")
        if validate_sql(db_path, sql):
            write_sql_to_file(output_file, sql)
            success_count += 1
            print(f"  ✓ Query {i} 验证通过，已写入文件")
        else:
            print(f"  ✗ Query {i} 未返回数据，跳过")
    
    print(f"\n完成！成功写入 {success_count}/6 条SQL查询")

if __name__ == "__main__":
    main()