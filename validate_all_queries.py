import sqlite3

def validate_all_queries():
    db_path = 'e:/work/trae/mySqlparse/test.db'
    
    with open('e:/work/trae/mySqlparse/test_sqlite_queries_50.sql', 'r', encoding='utf-8') as f:
        content = f.read()
    
    lines = content.split('\n')
    sql_queries = []
    current_query = []
    
    for line in lines:
        stripped_line = line.strip()
        if stripped_line.startswith('--'):
            if current_query:
                sql_queries.append('\n'.join(current_query).strip())
                current_query = []
            continue
        
        if stripped_line:
            current_query.append(line)
        
        if ';' in line:
            if current_query:
                sql_queries.append('\n'.join(current_query).strip())
                current_query = []
    
    if current_query:
        sql_queries.append('\n'.join(current_query).strip())
    
    sql_queries = [q for q in sql_queries if q.strip()]
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    passed = 0
    failed = 0
    failed_queries = []
    
    for i, query in enumerate(sql_queries, 1):
        if not query.strip():
            continue
            
        try:
            cursor.execute(query)
            results = cursor.fetchall()
            
            if len(results) >= 1:
                print(f"✓ Query {i}: 通过 ({len(results)} 行)")
                passed += 1
            else:
                print(f"✗ Query {i}: 未返回数据")
                failed += 1
                failed_queries.append(i)
        except Exception as e:
            print(f"✗ Query {i}: 执行错误 - {str(e)[:100]}")
            failed += 1
            failed_queries.append(i)
    
    conn.close()
    
    print(f"\n验证完成: {passed}/{len(sql_queries)} 通过")
    if failed_queries:
        print(f"失败的查询序号: {', '.join(map(str, failed_queries))}")
    
    return passed == len(sql_queries)

if __name__ == '__main__':
    validate_all_queries()