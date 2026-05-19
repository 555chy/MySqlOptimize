
# mySqlparse - SQL 优化器

一个基于 Java 的 SQL 解析和优化工具，提供多种智能优化规则，提升数据库查询性能。

## 文档导航

| 文档 | 说明 |
|------|------|
| [README.md](README.md) | 项目主文档（本文档），包含项目介绍、快速开始、使用说明 |
| [DATABASE_README.md](DATABASE_README.md) | 完整电商测试数据库说明 |
| [test_queries_100.sql](test_queries_100.sql) | 100条复杂SQL测试用例 |
| [example_test_report.md](example_test_report.md) | 示例测试报告 |

## 重要提示

查看 [DATABASE_README.md](DATABASE_README.md) 获取关于完整电商测试数据库的详细说明。

## 项目介绍

mySqlparse 是一个强大的 SQL 优化工具，它能够自动分析 SQL 查询并应用多种优化规则，在保证查询结果一致性的前提下，显著提升查询执行效率。

### 核心特性

- 自动 SQL 解析与优化
- 多种内置优化规则
- 数据库元数据分析
- 优化前后结果一致性验证
- 命令行友好的使用方式

## 快速开始

### 环境要求

- Java 11 或更高版本
- Maven 3.6+

### 构建项目

```bash
mvn clean package
```

### 快速演示

我们提供了两个独立的演示程序，无需完整 Maven 构建即可运行：

#### 1. SQL优化器快速演示

编译并运行 `QuickDemo.java` 查看 SQL 优化功能：

```bash
# 编译
javac -cp "target/classes;D:\Documents\.m2\repository\net\sf\jsqlparser\jsqlparser\4.5\jsqlparser-4.5.jar" QuickDemo.java

# 运行
java -cp ".;target/classes;D:\Documents\.m2\repository\net\sf\jsqlparser\jsqlparser\4.5\jsqlparser-4.5.jar" QuickDemo
```

#### 2. 创建完整测试数据库

编译并运行 `CreateFullTestDatabase.java` 创建包含10个业务表的电商数据库：

```bash
# 编译
javac -cp "target/classes;D:\Documents\.m2\repository\org\xerial\sqlite-jdbc\3.50.2.0\sqlite-jdbc-3.50.2.0.jar" CreateFullTestDatabase.java

# 运行
java -cp ".;target/classes;D:\Documents\.m2\repository\org\xerial\sqlite-jdbc\3.50.2.0\sqlite-jdbc-3.50.2.0.jar" CreateFullTestDatabase
```

或者直接双击运行批处理脚本（Windows）：
```
run_db_creator.bat
```

### 基本使用

```bash
# ========================================
# 1. 初始化测试数据库
# ========================================

# 初始化测试数据库（默认100条数据）
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --init-db

# 初始化测试数据库，自定义数据行数
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --init-db --rows 500000

# 使用指定数据库并自定义行数
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --init-db -db jdbc:sqlite:test.db --rows 500000

# ========================================
# 2. 运行完整测试套件
# ========================================

# 使用默认的 test_queries_100.sql（100条SQL测试）
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test

# 使用 test_queries_3.sql（3条SQL测试）
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test test_queries_3.sql

# 使用 test_queries_100.sql
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test test_queries_100.sql

# 同时指定数据库和测试文件
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test test_queries_3.sql -db jdbc:sqlite:test.db

# ========================================
# 3. 优化SQL语句
# ========================================

# 优化SQL（默认从 test_queries_100.sql 读取100条SQL）
java -jar target/mySqlparse-1.0-SNAPSHOT.jar

# 优化SQL，从指定文件读取
java -jar target/mySqlparse-1.0-SNAPSHOT.jar -f demo.sql

# 优化单个SQL语句
java -jar target/mySqlparse-1.0-SNAPSHOT.jar "SELECT COUNT(1) FROM users"

# 指定数据库连接
java -jar target/mySqlparse-1.0-SNAPSHOT.jar -db jdbc:sqlite:test.db "SELECT * FROM users"

# 显示详细优化信息（包括应用的规则列表）
java -jar target/mySqlparse-1.0-SNAPSHOT.jar -v "SELECT COUNT(1) FROM users"

# 验证优化前后结果一致性
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --validate "SELECT * FROM users"

# ========================================
# 4. 命令行参数说明
# ========================================

# -h, --help          显示帮助信息
# -db <url>           数据库连接URL (默认: jdbc:sqlite:test.db)
# -f <file>           从文件读取SQL语句 (默认: test_queries_100.sql)
# -v                  显示详细优化信息
# --validate          验证优化前后结果一致性
# --test              运行完整的测试套件
# --init-db           初始化测试数据库
# --rows <count>      生成测试数据的行数 (默认: 100)
```

## 架构说明

### 项目结构

```
mySqlparse/
├── src/
│   └── main/java/com/sqlparse/
│       ├── Main.java                    # 主入口类
│       ├── SqlOptimizer.java            # 优化器核心
│       ├── metadata/                    # 数据库元数据模块
│       │   ├── DatabaseConnectionManager.java
│       │   ├── DatabaseMetadata.java
│       │   ├── MetadataFetcher.java
│       │   ├── TableInfo.java
│       │   ├── ColumnInfo.java
│       │   ├── IndexInfo.java
│       │   └── ForeignKeyInfo.java
│       ├── optimizer/                   # 优化器模块
│       │   ├── RuleEngine.java
│       │   ├── OptimizationRule.java
│       │   ├── OptimizationResult.java
│       │   ├── OptimizationContext.java
│       │   ├── RuleApplication.java
│       │   ├── OptimizationLog.java
│       │   └── rules/                  # 优化规则实现 (21条)
│       │       ├── SimplifyCountStarRule.java
│       │       ├── RemoveRedundantDistinctRule.java
│       │       ├── RemoveUnnecessaryOrderByRule.java
│       │       ├── RemoveRedundantLimitRule.java
│       │       ├── SimplifyWithClauseRule.java
│       │       ├── MergeNestedSubqueryRule.java
│       │       ├── UseExistsInsteadOfInRule.java
│       │       ├── OptimizeJoinOrderRule.java
│       │       ├── PredicatePushdownRule.java
│       │       ├── RemoveAlwaysTrueOrFalseRule.java
│       │       ├── ConstantFoldingRule.java
│       │       ├── MergeUnionRule.java
│       │       ├── RemoveRedundantJoinsRule.java
│       │       ├── ColumnPruningRule.java
│       │       ├── SubqueryToJoinRule.java
│       │       ├── OrConditionRewriteRule.java
│       │       ├── CaseSimplificationRule.java
│       │       ├── NotInToNotExistsRule.java
│       │       ├── NullCheckEliminationRule.java
│       │       ├── ArithmeticComparisonOptimizeRule.java
│       │       └── LikePatternOptimizeRule.java
│       ├── parser/                     # SQL 解析模块
│       │   ├── SqlParser.java
│       │   ├── SqlTypeDetector.java
│       │   └── SqlNode.java
│       ├── testdata/                   # 测试数据生成
│       │   ├── TestDataGenerator.java
│       │   └── EcommerceSchemaGenerator.java
│       └── validator/                  # 结果验证模块
│           ├── ResultValidator.java
│           ├── QueryResult.java
│           └── RowData.java
├── pom.xml                              # Maven 配置
├── dependency-reduced-pom.xml            # Maven shade插件生成的简化POM
├── README.md                            # 项目说明
├── DATABASE_README.md                   # 数据库详细说明
├── test_queries_100.sql                 # 100条复杂SQL测试用例
├── schema.sql                           # 数据库建表SQL
├── test_report.md                       # 最新测试报告
├── example_test_report.md               # 示例测试报告
├── test.db                              # 标准测试数据库
└── target/                              # Maven构建输出目录
    ├── classes/                         # 编译后的class文件
    └── mySqlparse-1.0-SNAPSHOT.jar      # 可执行的JAR包
```

### 数据库文件说明

项目包含多个SQLite数据库文件，用途如下：

| 文件名 | 用途说明 |
|--------|----------|
| `test.db` | 标准测试数据库，包含基础电商数据，用于常规优化测试 |

**注意：** 所有.db文件都可以通过程序重新生成，不需要手动维护。

### 核心模块说明

#### 1. metadata (元数据模块)
负责获取和管理数据库元数据信息，包括表结构、索引、外键等。

#### 2. optimizer (优化器模块)
核心优化引擎，包含规则引擎和各种优化规则实现。

#### 3. parser (解析器模块)
基于 JSqlParser 的 SQL 解析功能，将 SQL 转换为抽象语法树。

#### 4. validator (验证模块)
验证优化前后 SQL 查询结果的一致性。

### SQL优化流程

mySqlparse 的 SQL 优化流程包含**六个核心阶段**，每个阶段都有明确的职责和处理逻辑：

### 阶段一：输入处理与参数解析

**处理内容**：
- 接收命令行参数（SQL语句、配置文件、数据库连接等）
- 解析命令行参数，确定执行模式（单次优化、批量优化、测试模式等）
- 建立数据库连接（如需要）

**关键代码**：`Main.java` 中的参数解析和初始化逻辑

### 阶段二：SQL解析（AST构建）

**处理内容**：
- 使用 JSqlParser 将原始 SQL 字符串解析为**抽象语法树（AST）**
- AST 是 SQL 结构化表示，包含 Select、From、Where、GroupBy、OrderBy 等子句节点
- 同时检测 SQL 类型（SELECT/INSERT/UPDATE/DELETE），本优化器仅处理 SELECT 查询

**关键代码**：
- `SqlParser.java` - SQL 解析核心
- `SqlTypeDetector.java` - SQL 类型检测

**处理示例**：
```
输入: SELECT COUNT(1) FROM users WHERE status = 'active'
输出: AST 树结构
  └── Select
        ├── SelectItem: COUNT(1)
        ├── From: users
        └── Where: status = 'active'
```

### 阶段三：元数据收集（可选）

**处理内容**：
- 通过 JDBC 获取数据库元数据（表结构、索引、外键、行数统计等）
- 元数据用于支持高级优化规则（如JOIN顺序优化、列裁剪）
- 如果未连接数据库，仅执行基于语法的优化

**关键代码**：
- `MetadataFetcher.java` - 元数据获取
- `DatabaseMetadata.java` - 元数据模型

### 阶段四：规则引擎执行（核心优化）

**处理内容**：
- **规则注册与管理**：RuleEngine 维护21条优化规则的有序列表
- **规则遍历与匹配**：按顺序遍历每条规则，检查当前 AST 是否匹配规则触发条件
- **规则应用**：对匹配的规则执行优化转换，生成新的 AST
- **优化日志记录**：记录每条规则的应用情况（成功/失败/跳过）

**规则执行顺序**：

| 序号 | 规则名称 | 优先级 | 说明 |
|------|---------|--------|------|
| 1 | ConstantFoldingRule | 最高 | 先进行常量计算，简化后续规则处理 |
| 2 | RemoveAlwaysTrueOrFalseRule | 高 | 移除无意义条件 |
| 3 | ArithmeticComparisonOptimizeRule | 高 | 简化算术比较 |
| 4 | NullCheckEliminationRule | 高 | 消除冗余NULL检查 |
| 5 | SimplifyCountStarRule | 高 | 简化COUNT表达式 |
| 6 | RemoveRedundantDistinctRule | 中 | 移除冗余DISTINCT |
| 7 | RemoveUnnecessaryOrderByRule | 中 | 移除不必要排序 |
| 8 | RemoveRedundantLimitRule | 中 | 移除冗余LIMIT |
| 9 | CaseSimplificationRule | 中 | 简化CASE表达式 |
| 10 | OrConditionRewriteRule | 中 | 重写OR条件 |
| 11 | LikePatternOptimizeRule | 中 | 优化LIKE模式 |
| 12 | UseExistsInsteadOfInRule | 中 | IN转EXISTS |
| 13 | NotInToNotExistsRule | 中 | NOT IN转NOT EXISTS |
| 14 | SubqueryToJoinRule | 中 | 子查询转JOIN |
| 15 | PredicatePushdownRule | 中 | 谓词下推 |
| 16 | MergeNestedSubqueryRule | 中 | 合并嵌套子查询 |
| 17 | SimplifyWithClauseRule | 中 | 简化CTE |
| 18 | RemoveRedundantJoinsRule | 低 | 移除冗余JOIN |
| 19 | ColumnPruningRule | 低 | 列裁剪（需元数据） |
| 20 | OptimizeJoinOrderRule | 低 | 优化JOIN顺序（需元数据） |
| 21 | MergeUnionRule | 最低 | 合并UNION |

**关键代码**：
- `RuleEngine.java` - 规则引擎核心
- `OptimizationRule.java` - 规则接口定义
- `OptimizationContext.java` - 优化上下文（传递AST和元数据）

### 阶段五：结果生成

**处理内容**：
- 将优化后的 AST 转换回 SQL 字符串
- 格式化输出，保持可读性
- 记录优化日志（应用的规则列表、优化前后对比）

**关键代码**：`OptimizationResult.java` - 优化结果封装

### 阶段六：结果验证（可选）

**处理内容**：
- 执行原始 SQL 和优化后的 SQL
- 对比两者的查询结果（行数、数据内容）
- 验证优化的正确性，确保语义等价
- 记录验证结果到报告

**关键代码**：
- `ResultValidator.java` - 结果验证核心
- `QueryResult.java` - 查询结果封装

---

### 完整优化流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SQL优化完整流程                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐              │
│  │  输入处理    │───▶│   SQL解析    │───▶│  元数据收集      │              │
│  │  参数解析    │    │  (AST构建)   │    │  (可选)         │              │
│  └──────────────┘    └──────────────┘    └────────┬─────────┘              │
│                                                   │                        │
│                                                   ▼                        │
│  ┌──────────────────────────────────────────────────────────────┐          │
│  │                    阶段四：规则引擎执行                        │          │
│  │  ┌─────────────────────────────────────────────────────┐     │          │
│  │  │ RuleEngine: 按优先级顺序应用21条优化规则              │     │          │
│  │  │                                                     │     │          │
│  │  │  规则1 → 规则2 → 规则3 → ... → 规则21               │     │          │
│  │  │  (每规则: 匹配检测 → 条件验证 → 优化转换 → 日志记录)   │     │          │
│  │  └─────────────────────────────────────────────────────┘     │          │
│  └──────────────────────────────────────────────────────────────┘          │
│                              │                                            │
│                              ▼                                            │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐              │
│  │  结果生成    │───▶│  结果验证    │───▶│   输出报告      │              │
│  │  (AST→SQL)   │    │  (可选)      │    │                 │              │
│  └──────────────┘    └──────────────┘    └──────────────────┘              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 规则应用示例

**输入 SQL**：
```sql
SELECT DISTINCT COUNT(1) 
FROM orders 
WHERE user_id IN (SELECT id FROM users WHERE 1=1 AND status = 'active') 
ORDER BY id DESC;
```

**优化过程**：

| 规则 | 应用结果 | 说明 |
|------|---------|------|
| ConstantFoldingRule | 移除 `1=1` | 恒真条件无意义 |
| SimplifyCountStarRule | `COUNT(1)` → `COUNT(*)` | COUNT(*)性能最优 |
| RemoveRedundantDistinctRule | 移除 `DISTINCT` | COUNT(*)结果唯一 |
| RemoveUnnecessaryOrderByRule | 移除 `ORDER BY` | 聚合结果只有一行 |
| UseExistsInsteadOfInRule | `IN` → `EXISTS` | EXISTS性能更优 |

**优化后 SQL**：
```sql
SELECT COUNT(*) 
FROM orders 
WHERE EXISTS (SELECT 1 FROM users WHERE status = 'active' AND users.id = orders.user_id);
```

---

### 优化日志记录

每次优化都会生成详细的日志，记录：
- 原始 SQL 和优化后的 SQL
- 应用的规则列表（成功/跳过/失败）
- 每条规则的处理时间
- 优化前后执行时间对比（如启用验证）

日志格式示例：
```
优化结果:
  原始 SQL: SELECT COUNT(1) FROM users WHERE 1=1 AND status = 'active'
  优化后: SELECT COUNT(*) FROM users WHERE status = 'active'
  应用规则:
    ✓ ConstantFoldingRule (0.1ms)
    ✓ SimplifyCountStarRule (0.05ms)
  性能提升: 预计 15-20%
```

## 优化规则列表

本SQL优化器实现了以下8条核心优化规则，每条规则都附带详细示例说明。

### 1. SimplifyCountStarRule - 简化COUNT表达式

**功能说明**：将 `COUNT(1)` 或 `COUNT(column)` 简化为 `COUNT(*)`，因为 `COUNT(*)` 是数据库内部优化过的计数方式，不需要检查NULL值，性能最佳。

**触发条件**：SQL中包含 `COUNT(1)` 或 `COUNT(id)` 表达式

**示例**：
```sql
-- 优化前
SELECT COUNT(1) FROM users;

-- 优化后
SELECT COUNT(*) FROM users;
```

```sql
-- 优化前
SELECT COUNT(id) FROM products WHERE price > 100;

-- 优化后
SELECT COUNT(*) FROM products WHERE price > 100;
```

### 2. RemoveRedundantDistinctRule - 移除冗余DISTINCT

**功能说明**：当查询已经使用 `GROUP BY` 进行分组时，`SELECT DISTINCT` 是多余的，因为 GROUP BY 本身就会产生唯一结果。同时在存在 `LEFT JOIN` 的查询中会跳过此优化以保证结果正确性。

**触发条件**：包含 `SELECT DISTINCT` 且有 `GROUP BY` 子句（或 `LIMIT 1`）

**示例**：
```sql
-- 优化前（GROUP BY已经保证唯一性）
SELECT DISTINCT name, COUNT(*) FROM users GROUP BY name;

-- 优化后
SELECT name, COUNT(*) FROM users GROUP BY name;
```

```sql
-- 优化前
SELECT DISTINCT user_id FROM orders GROUP BY user_id LIMIT 10;

-- 优化后
SELECT user_id FROM orders GROUP BY user_id LIMIT 10;
```

**安全机制**：当检测到 `LEFT JOIN` 时跳过此优化，避免改变查询语义。

### 3. RemoveUnnecessaryOrderByRule - 移除不必要的ORDER BY

**功能说明**：对于仅包含聚合函数（如 `COUNT(*)`）的单行查询，`ORDER BY` 子句没有任何意义，因为聚合结果只有一行。该规则智能识别这种场景并移除无用的排序。

**触发条件**：查询只有一个 `SELECT` 项，且该项是聚合函数（如 `COUNT(*)`）

**示例**：
```sql
-- 优化前（COUNT查询不需要排序）
SELECT COUNT(*) FROM users ORDER BY id DESC;

-- 优化后
SELECT COUNT(*) FROM users;
```

```sql
-- 优化前
SELECT SUM(amount) FROM orders ORDER BY created_at ASC;

-- 优化后
SELECT SUM(amount) FROM orders;
```

### 4. RemoveRedundantLimitRule - 移除冗余LIMIT子句

**功能说明**：当查询中出现了嵌套的 `LIMIT` 子句（如 `LIMIT 100` 出现两次）时，内部的 `LIMIT 100` 是多余的，因为外部已经限制了结果集大小。

**触发条件**：SQL中包含两个或以上的 `LIMIT` 子句

**示例**：
```sql
-- 优化前（存在冗余的LIMIT 100）
SELECT * FROM (SELECT * FROM products LIMIT 100) t LIMIT 100;

-- 优化后
SELECT * FROM (SELECT * FROM products) t;
```

```sql
-- 优化前
SELECT * FROM products WHERE status = 1 LIMIT 100;

-- 优化后
SELECT * FROM products WHERE status = 1;
```

### 5. SimplifyWithClauseRule - 简化WITH子句（CTE）

**功能说明**：将只使用一次的 CTE（Common Table Expression，WITH子句）内联到主查询中，减少数据库的解析开销，避免创建临时结果集。

**触发条件**：SQL以 `WITH` 开头且包含 CTE 定义

**示例**：
```sql
-- 优化前（CTE只使用一次）
WITH temp AS (SELECT id, name FROM users WHERE active = 1)
SELECT * FROM temp WHERE id > 100;

-- 优化后（CTE被内联）
SELECT * FROM (SELECT id, name FROM users WHERE active = 1) WHERE id > 100;
```

```sql
-- 优化前
WITH user_orders AS (SELECT user_id, SUM(amount) as total FROM orders GROUP BY user_id)
SELECT u.name, user_orders.total FROM users u, user_orders WHERE u.id = user_orders.user_id;

-- 优化后
SELECT u.name, t.total FROM users u, (SELECT user_id, SUM(amount) as total FROM orders GROUP BY user_id) t WHERE u.id = t.user_id;
```

### 6. MergeNestedSubqueryRule - 合并嵌套子查询

**功能说明**：将多层嵌套的子查询（FROM (SELECT ...)）合并为单层查询，减少查询层级的深度，提高查询执行效率。

**触发条件**：SQL中包含 `FROM (SELECT ...` 形式的子查询

**示例**：
```sql
-- 优化前（存在嵌套子查询）
SELECT * FROM (SELECT id, name FROM (SELECT * FROM users WHERE active = 1) t1) t2;

-- 优化后（合并为单层）
SELECT id, name FROM users WHERE active = 1;
```

```sql
-- 优化前
SELECT product_name, price FROM (SELECT * FROM (SELECT id, name as product_name, price FROM products) p) pp WHERE price > 50;

-- 优化后
SELECT product_name, price FROM (SELECT id, name as product_name, price FROM products) p WHERE price > 50;
```

### 7. UseExistsInsteadOfInRule - IN子查询转换为EXISTS

**功能说明**：将 `IN (SELECT ...)` 子查询转换为 `EXISTS` 形式。`EXISTS` 在找到匹配行后立即返回，而 `IN` 需要遍历完整个子查询结果，因此 `EXISTS` 在大数据量场景下性能更优。

**触发条件**：SQL中包含 `IN (SELECT` 形式的子查询

**示例**：
```sql
-- 优化前（IN子查询）
SELECT * FROM orders WHERE user_id IN (SELECT id FROM users WHERE status = 'active');

-- 优化后（EXISTS形式）
SELECT * FROM orders WHERE EXISTS (SELECT 1 FROM users WHERE status = 'active' AND users.id = orders.user_id);
```

```sql
-- 优化前
SELECT product_name FROM products WHERE category_id IN (SELECT id FROM categories WHERE parent_id = 1);

-- 优化后
SELECT product_name FROM products WHERE EXISTS (SELECT 1 FROM categories WHERE parent_id = 1 AND categories.id = products.category_id);
```

### 8. OptimizeJoinOrderRule - 优化JOIN顺序

**功能说明**：根据表的实际数据量（元数据）智能调整多表JOIN的顺序。原则是先连接数据量小的表（驱动表），可以快速减少中间结果集的大小。但该规则目前为占位实现，实际的JOIN顺序优化需要结合数据库的统计信息。

**触发条件**：查询包含2个或以上的 `JOIN` 操作

**当前状态**：该规则已注册并可被触发，优化逻辑依赖数据库元数据信息。

**示例**：
```sql
-- 优化前（JOIN顺序可能不是最优）
SELECT o.id, u.name, p.title
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON o.product_id = p.id;

-- 优化后（根据表大小重新排序JOIN顺序）
-- 假设 users表100行，products表10000行，orders表100000行
-- 则应先驱动 users(100) -> orders(100) -> products(10000)
SELECT o.id, u.name, p.title
FROM users u
JOIN orders o ON u.id = o.user_id
JOIN products p ON o.product_id = p.id;
```

## 优化规则完整列表总结

本SQL优化器共实现了**21条优化规则**，涵盖了SQL查询优化的各个方面：

| 序号 | 规则名称 | 英文名 | 功能描述 |
|------|---------|--------|----------|
| 1 | 简化COUNT表达式 | SimplifyCountStarRule | 将COUNT(1)/COUNT(column)简化为COUNT(*) |
| 2 | 移除冗余DISTINCT | RemoveRedundantDistinctRule | 移除GROUP BY中多余的DISTINCT |
| 3 | 移除不必要ORDER BY | RemoveUnnecessaryOrderByRule | 移除聚合查询中的无用排序 |
| 4 | 移除冗余LIMIT | RemoveRedundantLimitRule | 移除嵌套的冗余LIMIT子句 |
| 5 | 简化WITH子句 | SimplifyWithClauseRule | 内联只使用一次的CTE |
| 6 | 合并嵌套子查询 | MergeNestedSubqueryRule | 合并多层嵌套子查询为单层 |
| 7 | IN转EXISTS | UseExistsInsteadOfInRule | 将IN子查询转换为EXISTS |
| 8 | 优化JOIN顺序 | OptimizeJoinOrderRule | 根据表大小优化多表JOIN顺序 |
| 9 | 谓词下推 | PredicatePushdownRule | 将WHERE条件下推到最深子查询层 |
| 10 | 移除恒真/恒假条件 | RemoveAlwaysTrueOrFalseRule | 删除1=1、1=0等无意义条件 |
| 11 | 常量折叠 | ConstantFoldingRule | 编译期计算常量表达式 |
| 12 | 合并UNION | MergeUnionRule | 合并连续的结构相同的UNION |
| 13 | 移除冗余JOIN | RemoveRedundantJoinsRule | 移除未被引用的表的JOIN |
| 14 | 列裁剪 | ColumnPruningRule | SELECT *时裁剪不需要的列 |
| 15 | 子查询转JOIN | SubqueryToJoinRule | 将符合条件的子查询转为JOIN |
| 16 | OR条件重写 | OrConditionRewriteRule | 将多个OR重写为UNION利用索引 |
| 17 | CASE简化 | CaseSimplificationRule | 简化冗余的CASE表达式分支 |
| 18 | NOT IN转NOT EXISTS | NotInToNotExistsRule | 将NOT IN子查询转为NOT EXISTS |
| 19 | 消除冗余NULL检查 | NullCheckEliminationRule | 移除非空列上不必要的NULL检查 |
| 20 | 算术比较优化 | ArithmeticComparisonOptimizeRule | 简化列*1>0为列>0 |
| 21 | LIKE模式优化 | LikePatternOptimizeRule | 优化LIKE模式以更好利用索引 |

### 9. PredicatePushdownRule - 谓词下推

**功能说明**：将WHERE条件尽可能下推到最深层的子查询中执行，减少中间结果集的大小，提升查询效率。

**触发条件**：SQL中包含WHERE子句和子查询

**示例**：
```sql
-- 优化前（WHERE条件在外层）
SELECT * FROM (SELECT * FROM users WHERE active = 1) t WHERE t.id > 100;

-- 优化后（条件被下推到子查询）
SELECT * FROM (SELECT * FROM users WHERE active = 1 AND id > 100) t;
```

### 10. RemoveAlwaysTrueOrFalseRule - 移除恒真/恒假条件

**功能说明**：删除SQL中永远为真（如 `1=1`、`TRUE`）或永远为假（如 `1=0`、`FALSE`）的条件表达式，简化查询逻辑。

**触发条件**：SQL中包含 `1=1`、`1=0`、`TRUE`、`FALSE` 等常量比较

**示例**：
```sql
-- 优化前（存在恒真条件）
SELECT * FROM users WHERE 1=1 AND status = 'active';

-- 优化后
SELECT * FROM users WHERE status = 'active';
```

```sql
-- 优化前
SELECT * FROM orders WHERE order_date > '2024-01-01' AND 1=1 AND shipped = 0;

-- 优化后
SELECT * FROM orders WHERE order_date > '2024-01-01' AND shipped = 0;
```

### 11. ConstantFoldingRule - 常量折叠

**功能说明**：在查询解析阶段预先计算包含常量的算术表达式（如 `1+2`、`100*2`），避免在运行时重复计算。

**触发条件**：SQL中包含可计算的常量算术表达式

**示例**：
```sql
-- 优化前（算术表达式在运行时计算）
SELECT * FROM products WHERE price > 100 + 50;

-- 优化后（常量被预先计算）
SELECT * FROM products WHERE price > 150;
```

```sql
-- 优化前
SELECT (price * 1) + 0 AS net_price FROM orders;

-- 优化后
SELECT price AS net_price FROM orders;
```

### 12. MergeUnionRule - 合并UNION

**功能说明**：当多个 `UNION ALL` 操作的结构完全相同时，将其合并为一个操作，减少重复扫描的开销。

**触发条件**：SQL中包含2个或以上的 `UNION ALL` 操作

**示例**：
```sql
-- 优化前（两次扫描相同表）
SELECT id, name FROM active_users
UNION ALL
SELECT id, name FROM active_users
UNION ALL
SELECT id, name FROM active_users;

-- 优化后（只扫描一次，使用CROSS JOIN或笛卡尔积处理）
SELECT id, name FROM active_users
UNION ALL
SELECT id, name FROM active_users;
```

### 13. RemoveRedundantJoinsRule - 移除冗余JOIN

**功能说明**：检测并移除那些JOIN的表在查询中未被引用的冗余JOIN操作，减少不必要的表连接开销。

**触发条件**：查询包含JOIN操作，且某被JOIN的表在SELECT/WHERE等子句中未被引用

**示例**：
```sql
-- 优化前（products表被JOIN但未被引用）
SELECT u.id, u.name, o.order_id
FROM users u
JOIN orders o ON u.id = o.user_id
JOIN products p ON o.product_id = p.id;

-- 优化后（products的JOIN被移除）
SELECT u.id, u.name, o.order_id
FROM users u
JOIN orders o ON u.id = o.user_id;
```

```sql
-- 优化前
SELECT u.name, o.total
FROM users u
JOIN orders o ON u.id = o.user_id
JOIN categories c ON 1=1;

-- 优化后
SELECT u.name, o.total
FROM users u
JOIN orders o ON u.id = o.user_id;
```

### 14. ColumnPruningRule - 列裁剪

**功能说明**：当查询使用 `SELECT *` 获取表中所有列时，实际只需要部分列，此时裁剪掉不需要的列可以减少数据传输量。但该规则需要结合数据库元数据（表结构信息）才能准确实现。

**触发条件**：查询包含 `SELECT *` 且有数据库元数据支持

**当前状态**：该规则已注册，当获取到完整表结构信息时可自动启用。

### 15. SubqueryToJoinRule - 子查询转JOIN

**功能说明**：将某些可以使用 `EXISTS` 改写的子查询直接转换为 `JOIN` 操作。`JOIN` 操作通常比子查询更高效，因为数据库可以对 JOIN 进行更好的优化。

**触发条件**：查询包含 `EXISTS (SELECT 1 FROM ...)` 子查询

**示例**：
```sql
-- 优化前（EXISTS子查询）
SELECT * FROM users WHERE EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id);

-- 优化后（转为JOIN）
SELECT DISTINCT u.* FROM users u INNER JOIN orders o ON u.id = o.user_id;
```

### 16. OrConditionRewriteRule - OR条件重写

**功能说明**：当查询中包含多个 OR 连接的相同字段条件时（如 `WHERE status = 'A' OR status = 'B' OR status = 'C'`），将其重写为 `IN` 或 `UNION` 形式，可以更好地利用索引。

**触发条件**：查询中包含3个或以上的 OR 条件

**示例**：
```sql
-- 优化前（多个OR条件）
SELECT * FROM products WHERE category_id = 1 OR category_id = 2 OR category_id = 3;

-- 优化后（转为IN）
SELECT * FROM products WHERE category_id IN (1, 2, 3);
```

### 17. CaseSimplificationRule - CASE简化

**功能说明**：简化 CASE 表达式中的冗余分支，如 `CASE WHEN TRUE THEN x` 简化为 `x`，以及消除相同的比较条件。

**触发条件**：查询包含 CASE WHEN THEN ELSE END 表达式

**示例**：
```sql
-- 优化前（WHEN TRUE是多余的）
SELECT CASE WHEN TRUE THEN price ELSE 0 END FROM products;

-- 优化后
SELECT price FROM products;
```

```sql
-- 优化前（column = column可以消除）
SELECT CASE WHEN status = status THEN 'valid' ELSE 'invalid' END FROM orders;

-- 优化后
SELECT 'valid' FROM orders;
```

### 18. NotInToNotExistsRule - NOT IN转NOT EXISTS

**功能说明**：将 `NOT IN (SELECT ...)` 子查询转换为 `NOT EXISTS` 形式。当子查询可能返回 NULL 值时，`NOT IN` 会导致结果不正确，而 `NOT EXISTS` 总是能返回正确结果，且性能更优。

**触发条件**：查询包含 `NOT IN (SELECT` 形式的子查询

**示例**：
```sql
-- 优化前（NOT IN子查询）
SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM blocked_users);

-- 优化后（NOT EXISTS形式）
SELECT * FROM users WHERE NOT EXISTS (SELECT 1 FROM blocked_users WHERE blocked_users.user_id = users.id);
```

### 19. NullCheckEliminationRule - 消除冗余NULL检查

**功能说明**：当对使用 `COALESCE` 或 `IFNULL` 包裹的表达式进行 NULL 检查时，如果内部表达式不可能为 NULL，可以消除冗余的 NULL 检查。

**触发条件**：查询中包含对 COALESCE/IFNULL 表达式的 IS NULL/IS NOT NULL 检查

**示例**：
```sql
-- 优化前（IFNULL不可能返回NULL）
SELECT * FROM users WHERE IFNULL(nickname, 'default') IS NOT NULL;

-- 优化后
SELECT * FROM users WHERE TRUE;
```

### 20. ArithmeticComparisonOptimizeRule - 算术比较优化

**功能说明**：简化包含算术运算的比较表达式，如 `column * 1 > 0` 简化为 `column > 0`，`column + 0 > 100` 简化为 `column > 100`。

**触发条件**：WHERE 子句中包含列与常量的算术运算比较

**示例**：
```sql
-- 优化前（column * 1 无意义）
SELECT * FROM orders WHERE total * 1 > 0;

-- 优化后
SELECT * FROM orders WHERE total > 0;
```

```sql
-- 优化前
SELECT * FROM products WHERE (price - 0) > 100;

-- 优化后
SELECT * FROM products WHERE price > 100;
```

### 21. LikePatternOptimizeRule - LIKE模式优化

**功能说明**：优化 LIKE 模式的写法，使索引能够被有效利用。如将 `'%abc'` 开头的模式优化为 `'abc%'`，因为只有 `'abc%'` 形式的前缀匹配可以利用索引。

**触发条件**：查询中包含 LIKE 模式匹配

**示例**：
```sql
-- 优化前（'%xxx'无法使用索引）
SELECT * FROM users WHERE name LIKE '%john';

-- 优化后（优化为反向搜索或其他策略）
-- 注意：这种情况下可能需要考虑使用全文索引
```

```sql
-- 优化前
SELECT * FROM products WHERE description LIKE '%electronics%';

-- 优化后（识别并标记可优化的模式）
SELECT * FROM products WHERE description LIKE 'electronics%';
```

## 完整测试数据库 (10个业务表)

项目包含完整的电商业务数据库生成器：

| 序号 | 表名 | 说明 | 主键 |
|------|------|------|------|
| 1 | users | 用户表（82个字段） | user_id |
| 2 | categories | 商品分类表（31个字段） | category_id |
| 3 | brands | 品牌表（53个字段） | brand_id |
| 4 | products | 商品表（101个字段） | product_id |
| 5 | inventory | 库存表（63个字段） | inventory_id |
| 6 | orders | 订单表（109个字段） | order_id |
| 7 | order_items | 订单明细表（91个字段） | order_item_id |
| 8 | payments | 支付表（83个字段） | payment_id |
| 9 | shipping | 物流表（125个字段） | shipping_id |
| 10 | reviews | 评论表（79个字段） | review_id |

数据库带有完整的42个索引和外键约束。详细说明请见 [DATABASE_README.md](DATABASE_README.md)。

## 如何运行测试和生成报告

### 运行完整测试并生成报告

```bash
mvn clean package
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test -db "jdbc:sqlite:test.db"
```

测试完成后，报告会保存在 `test_report.md` 文件中，包含所有测试用例的详细信息，包括每条SQL的查询结果行数和列数。

### 运行特定测试类

```bash
mvn -Dtest=EndToEndIntegrationTest test
```

### 查看测试覆盖率

```bash
mvn clean test jacoco:report
```

覆盖率报告将生成在:
```
target/site/jacoco/
```

## 开发指南

### 添加新的优化规则

1. 在 `com.sqlparse.optimizer.rules` 包下创建新的规则类
2. 实现 `OptimizationRule` 接口
3. 在 `RuleEngine.registerAllRules()` 中注册新规则
4. 编写单元测试验证规则的正确性

### 性能基准测试

使用 `PerformanceBenchmark` 类进行性能基准测试：

```java
PerformanceBenchmark benchmark = new PerformanceBenchmark(connectionManager);
benchmark.runBenchmark(sql);
```

## 许可证

本项目采用 MIT 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。

