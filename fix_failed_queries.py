import sqlite3

db_path = 'e:/work/trae/mySqlparse/test.db'
sql_file_path = 'e:/work/trae/mySqlparse/test_sqlite_queries_50.sql'

with open(sql_file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    '-- Query 4: 库存分析\nSELECT i.product_id, p.product_name, p.sku, p.price, i.quantity, i.available_quantity,\n    i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,\n    CASE WHEN i.available_quantity <= COALESCE(i.reorder_threshold, 10) THEN \'需要补货\' \n         WHEN i.available_quantity <= COALESCE(i.minimum_quantity, 20) THEN \'库存偏低\' \n         WHEN i.available_quantity >= COALESCE(i.maximum_quantity, 100) THEN \'库存充足\' \n         ELSE \'库存正常\' END AS stock_status \nFROM inventory i\nINNER JOIN products p ON i.product_id = p.product_id\nORDER BY stock_status LIMIT 100;',
    '-- Query 4: 库存分析\nSELECT i.product_id, COALESCE(p.product_name, \'Unknown Product\') AS product_name, COALESCE(p.sku, \'N/A\') AS sku, COALESCE(p.price, 0) AS price, i.quantity, i.available_quantity,\n    i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,\n    CASE WHEN i.available_quantity <= COALESCE(i.reorder_threshold, 10) THEN \'需要补货\' \n         WHEN i.available_quantity <= COALESCE(i.minimum_quantity, 20) THEN \'库存偏低\' \n         WHEN i.available_quantity >= COALESCE(i.maximum_quantity, 100) THEN \'库存充足\' \n         ELSE \'库存正常\' END AS stock_status \nFROM inventory i\nLEFT JOIN products p ON i.product_id = p.product_id\nORDER BY stock_status LIMIT 100;'
)

content = content.replace(
    '-- Query 13: 产品交叉销售分析\nSELECT o.order_id, COUNT(DISTINCT oi.product_id) AS product_count,\n    GROUP_CONCAT(DISTINCT oi.product_id) AS product_ids\nFROM orders o\nLEFT JOIN order_items oi ON o.order_id = oi.order_id\nGROUP BY o.order_id\nHAVING product_count >= 1\nORDER BY product_count DESC LIMIT 100;',
    '-- Query 13: 产品交叉销售分析\nSELECT o.order_id, o.order_number, COUNT(DISTINCT oi.product_id) AS product_count,\n    COALESCE(GROUP_CONCAT(DISTINCT oi.product_id), \'\') AS product_ids\nFROM orders o\nLEFT JOIN order_items oi ON o.order_id = oi.order_id\nGROUP BY o.order_id, o.order_number\nORDER BY product_count DESC LIMIT 100;'
)

content = content.replace(
    '-- Query 18: 产品库存周转率分析\nSELECT i.product_id, p.product_name, p.sku, i.quantity AS current_inventory, \n    i.available_quantity, COALESCE(SUM(oi.quantity), 0) AS units_sold,\n    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue_generated,\n    ROUND(COALESCE(SUM(oi.quantity) / NULLIF(i.quantity, 0), 0), 2) AS inventory_turnover_ratio,\n    CASE WHEN COALESCE(SUM(oi.quantity), 0) / NULLIF(i.quantity, 0) >= 0.5 THEN \'高周转\' \n         WHEN COALESCE(SUM(oi.quantity), 0) / NULLIF(i.quantity, 0) >= 0.2 THEN \'正常周转\' \n         ELSE \'低周转\' END AS turnover_status \nFROM inventory i\nINNER JOIN products p ON i.product_id = p.product_id\nLEFT JOIN order_items oi ON i.product_id = oi.product_id\nLEFT JOIN orders o ON oi.order_id = o.order_id\nGROUP BY i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity\nORDER BY inventory_turnover_ratio DESC LIMIT 100;',
    '-- Query 18: 产品库存周转率分析\nSELECT i.product_id, COALESCE(p.product_name, \'Unknown Product\') AS product_name, COALESCE(p.sku, \'N/A\') AS sku, i.quantity AS current_inventory, \n    i.available_quantity, COALESCE(SUM(oi.quantity), 0) AS units_sold,\n    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue_generated,\n    ROUND(COALESCE(SUM(oi.quantity) / NULLIF(i.quantity, 0), 0), 2) AS inventory_turnover_ratio,\n    CASE WHEN COALESCE(SUM(oi.quantity), 0) / NULLIF(i.quantity, 0) >= 0.5 THEN \'高周转\' \n         WHEN COALESCE(SUM(oi.quantity), 0) / NULLIF(i.quantity, 0) >= 0.2 THEN \'正常周转\' \n         ELSE \'低周转\' END AS turnover_status \nFROM inventory i\nLEFT JOIN products p ON i.product_id = p.product_id\nLEFT JOIN order_items oi ON i.product_id = oi.product_id\nLEFT JOIN orders o ON oi.order_id = o.order_id\nGROUP BY i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity\nORDER BY inventory_turnover_ratio DESC LIMIT 100;'
)

content = content.replace(
    '-- Query 36: 库存成本分析\nSELECT i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity, \n    i.average_cost, i.last_cost, i.standard_cost, i.inventory_value,\n    (i.quantity * COALESCE(i.average_cost, 0)) AS calculated_inventory_value\nFROM inventory i\nINNER JOIN products p ON i.product_id = p.product_id\nORDER BY calculated_inventory_value DESC LIMIT 100;',
    '-- Query 36: 库存成本分析\nSELECT i.product_id, COALESCE(p.product_name, \'Unknown Product\') AS product_name, COALESCE(p.sku, \'N/A\') AS sku, i.quantity, i.available_quantity, \n    i.average_cost, i.last_cost, i.standard_cost, i.inventory_value,\n    (i.quantity * COALESCE(i.average_cost, 0)) AS calculated_inventory_value\nFROM inventory i\nLEFT JOIN products p ON i.product_id = p.product_id\nORDER BY calculated_inventory_value DESC LIMIT 100;'
)

content = content.replace(
    '-- Query 49: 产品库存预警分析\nSELECT i.product_id, p.product_name, p.sku, i.quantity, i.available_quantity,\n    i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,\n    (COALESCE(i.maximum_quantity, 100) - i.available_quantity) AS quantity_needed,\n    CASE WHEN i.available_quantity <= COALESCE(i.reorder_threshold, 10) THEN \'紧急\' \n         WHEN i.available_quantity <= COALESCE(i.minimum_quantity, 20) THEN \'高\' \n         WHEN i.available_quantity <= COALESCE(i.maximum_quantity, 100) * 0.5 THEN \'中\' \n         ELSE \'低\' END AS alert_level \nFROM inventory i\nINNER JOIN products p ON i.product_id = p.product_id\nORDER BY CASE alert_level WHEN \'紧急\' THEN 1 WHEN \'高\' THEN 2 WHEN \'中\' THEN 3 ELSE 4 END LIMIT 100;',
    '-- Query 49: 产品库存预警分析\nSELECT i.product_id, COALESCE(p.product_name, \'Unknown Product\') AS product_name, COALESCE(p.sku, \'N/A\') AS sku, i.quantity, i.available_quantity,\n    i.reorder_threshold, i.minimum_quantity, i.maximum_quantity,\n    (COALESCE(i.maximum_quantity, 100) - i.available_quantity) AS quantity_needed,\n    CASE WHEN i.available_quantity <= COALESCE(i.reorder_threshold, 10) THEN \'紧急\' \n         WHEN i.available_quantity <= COALESCE(i.minimum_quantity, 20) THEN \'高\' \n         WHEN i.available_quantity <= COALESCE(i.maximum_quantity, 100) * 0.5 THEN \'中\' \n         ELSE \'低\' END AS alert_level \nFROM inventory i\nLEFT JOIN products p ON i.product_id = p.product_id\nORDER BY CASE alert_level WHEN \'紧急\' THEN 1 WHEN \'高\' THEN 2 WHEN \'中\' THEN 3 ELSE 4 END LIMIT 100;'
)

with open(sql_file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("已修复5个失败的查询")