# mySqlparse Code Wiki

---

## 1. 项目概述

### 1.1 项目简介

**mySqlparse** 是一个基于 Java 的 SQL 解析和优化工具，提供多种智能优化规则，在保证查询结果一致性的前提下，显著提升数据库查询性能。

### 1.2 核心特性

| 特性 | 说明 |
|------|------|
| 自动 SQL 解析 | 基于 JSqlParser 将 SQL 转换为抽象语法树 |
| 多规则优化 | 实现 21 条优化规则，覆盖 SQL 优化的各个方面 |
| 元数据分析 | 支持获取数据库元数据（表结构、索引、外键等） |
| 结果验证 | 验证优化前后 SQL 查询结果的一致性 |
| 命令行友好 | 提供丰富的命令行参数支持各种使用场景 |

### 1.3 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 11 |
| 构建工具 | Maven | 3.6+ |
| SQL 解析 | JSqlParser | 4.5 |
| 数据库 | SQLite | 3.50.2.0 |
| 命令行解析 | Apache Commons CLI | 1.9.0 |
| 测试框架 | JUnit 5 | 5.10.0 |

---

## 2. 项目架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           mySqlparse 架构                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────────┐   │
│  │   CLI Interface │────▶│    Main.java    │────▶│   SqlOptimizer      │   │
│  │   (命令行入口)   │     │   (主入口类)     │     │   (优化器核心)      │   │
│  └─────────────────┘     └─────────────────┘     └──────────┬──────────┘   │
│                                                              │              │
│                                                              ▼              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                        RuleEngine (规则引擎)                          │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │   │
│  │  │ Rule 1   │ │ Rule 2   │ │ Rule 3   │ │ ...     │ │ Rule 21  │  │   │
│  │  │ Constant │ │ Remove   │ │ Arith    │ │         │ │ Like     │  │   │
│  │  │ Folding  │ │ True/False││ Compare  │ │         │ │ Pattern  │  │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                        │                                   │
│          ┌─────────────────────────────┼─────────────────────────────┐     │
│          ▼                             ▼                             ▼     │
│  ┌───────────────┐          ┌─────────────────┐          ┌─────────────────┐ │
│  │   Metadata    │          │     Parser      │          │    Validator    │ │
│  │  (元数据模块)  │          │   (解析模块)     │          │   (验证模块)     │ │
│  └───────────────┘          └─────────────────┘          └─────────────────┘ │
│          │                             │                             │       │
│          ▼                             ▼                             ▼       │
│  ┌───────────────┐          ┌─────────────────┐          ┌─────────────────┘ │
│  │  Database     │          │   SqlNode       │                             │
│  │  Connection   │          │   SqlTypeDetector│                            │
│  └───────────────┘          └─────────────────┘                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 模块职责说明

| 模块 | 包路径 | 职责 |
|------|--------|------|
| **主入口** | `com.sqlparse.Main` | 命令行参数解析、执行模式调度 |
| **优化器核心** | `com.sqlparse.SqlOptimizer` | 优化流程编排、规则引擎管理 |
| **规则引擎** | `com.sqlparse.optimizer` | 优化规则注册、执行、日志记录 |
| **优化规则** | `com.sqlparse.optimizer.rules` | 21条具体优化规则实现 |
| **元数据** | `com.sqlparse.metadata` | 数据库元数据获取与管理 |
| **解析器** | `com.sqlparse.parser` | SQL解析、AST构建、类型检测 |
| **验证器** | `com.sqlparse.validator` | 优化前后结果一致性验证 |
| **测试数据** | `com.sqlparse.testdata` | 测试数据库生成、测试数据填充 |

---

## 3. 核心类与函数说明

### 3.1 主入口模块

#### Main.java

**职责**: 命令行入口，参数解析，执行模式调度

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `main(String[] args)` | `void` | 程序主入口，解析命令行参数 |
| `createOptions()` | `Options` | 创建命令行选项定义 |
| `loadSqlFromFile(String)` | `List<String>` | 从文件加载 SQL 语句列表 |
| `runTestSuite(String)` | `void` | 运行完整测试套件 |
| `initializeTestDatabase(String, int)` | `void` | 初始化测试数据库 |
| `processSqlOptimization(String, List, boolean, boolean)` | `void` | 处理 SQL 优化请求 |

**文件位置**: [src/main/java/com/sqlparse/Main.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/Main.java)

---

### 3.2 优化器核心模块

#### SqlOptimizer.java

**职责**: 优化器核心，管理规则引擎和优化上下文

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `SqlOptimizer()` | - | 无元数据构造函数 |
| `SqlOptimizer(DatabaseMetadata)` | - | 带元数据构造函数 |
| `optimize(String)` | `OptimizationResult` | 执行 SQL 优化 |
| `setMetadata(DatabaseMetadata)` | `void` | 设置数据库元数据 |
| `getRuleEngine()` | `RuleEngine` | 获取规则引擎实例 |

**文件位置**: [src/main/java/com/sqlparse/SqlOptimizer.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/SqlOptimizer.java)

---

### 3.3 规则引擎模块

#### RuleEngine.java

**职责**: 规则引擎，负责规则注册、遍历和执行

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `registerRule(OptimizationRule)` | `void` | 注册单条优化规则 |
| `registerRules(List<OptimizationRule>)` | `void` | 批量注册优化规则 |
| `unregisterRule(String)` | `void` | 根据名称注销规则 |
| `optimize(String)` | `OptimizationResult` | 执行 SQL 优化（字符串输入） |
| `optimize(Statement, String)` | `OptimizationResult` | 执行 SQL 优化（AST输入） |
| `getRules()` | `List<OptimizationRule>` | 获取已注册规则列表 |

**文件位置**: [src/main/java/com/sqlparse/optimizer/RuleEngine.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/RuleEngine.java)

#### OptimizationRule.java (接口)

**职责**: 优化规则接口定义

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `getName()` | `String` | 获取规则名称 |
| `getDescription()` | `String` | 获取规则描述 |
| `canApply(Statement, OptimizationContext)` | `boolean` | 判断规则是否可应用 |
| `apply(Statement, OptimizationContext)` | `Statement` | 应用规则进行优化 |

**文件位置**: [src/main/java/com/sqlparse/optimizer/OptimizationRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/OptimizationRule.java)

#### OptimizationContext.java

**职责**: 优化上下文，传递元数据和属性

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `getDatabaseMetadata()` | `DatabaseMetadata` | 获取数据库元数据 |
| `setDatabaseMetadata(DatabaseMetadata)` | `void` | 设置数据库元数据 |
| `getProperty(String)` | `Object` | 获取属性值 |
| `setProperty(String, Object)` | `void` | 设置属性值 |

**文件位置**: [src/main/java/com/sqlparse/optimizer/OptimizationContext.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/OptimizationContext.java)

#### OptimizationResult.java

**职责**: 优化结果封装

**核心方法/属性**:

| 方法/属性 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `getOriginalSql()` | `String` | 获取原始 SQL |
| `getOptimizedSql()` | `String` | 获取优化后 SQL |
| `getAppliedRules()` | `List<RuleApplication>` | 获取应用的规则列表 |
| `getAppliedRuleCount()` | `int` | 获取成功应用的规则数量 |
| `isOptimized()` | `boolean` | 判断是否进行了优化 |

**文件位置**: [src/main/java/com/sqlparse/optimizer/OptimizationResult.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/OptimizationResult.java)

---

### 3.4 元数据模块

#### DatabaseConnectionManager.java

**职责**: 数据库连接管理

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `getConnection()` | `Connection` | 获取数据库连接（懒加载） |
| `closeConnection()` | `void` | 关闭数据库连接 |
| `testConnection()` | `boolean` | 测试连接是否可用 |

**文件位置**: [src/main/java/com/sqlparse/metadata/DatabaseConnectionManager.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/metadata/DatabaseConnectionManager.java)

#### DatabaseMetadata.java

**职责**: 数据库元数据模型

**核心属性**:

| 属性名 | 类型 | 说明 |
|--------|------|------|
| `databaseName` | `String` | 数据库名称 |
| `databaseProductName` | `String` | 数据库产品名称 |
| `tables` | `List<TableInfo>` | 表信息列表 |
| `tableMap` | `Map<String, TableInfo>` | 表名到表信息的映射 |

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `addTable(TableInfo)` | `void` | 添加表信息 |
| `getTable(String)` | `TableInfo` | 根据表名获取表信息 |
| `hasTable(String)` | `boolean` | 判断表是否存在 |

**文件位置**: [src/main/java/com/sqlparse/metadata/DatabaseMetadata.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/metadata/DatabaseMetadata.java)

#### MetadataFetcher.java

**职责**: 元数据获取器

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `fetchAllMetadata()` | `DatabaseMetadata` | 获取所有数据库元数据 |
| `getTableNames(DatabaseMetaData)` | `List<String>` | 获取所有表名 |
| `fetchTableInfo(DatabaseMetaData, String)` | `TableInfo` | 获取指定表的详细信息 |
| `getTableRowCount(String)` | `long` | 获取表行数 |

**文件位置**: [src/main/java/com/sqlparse/metadata/MetadataFetcher.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/metadata/MetadataFetcher.java)

#### TableInfo.java

**职责**: 表信息模型

**核心属性**:

| 属性名 | 类型 | 说明 |
|--------|------|------|
| `name` | `String` | 表名 |
| `schema` | `String` | 模式名 |
| `type` | `String` | 表类型 |
| `columns` | `List<ColumnInfo>` | 列信息列表 |
| `indexes` | `List<IndexInfo>` | 索引信息列表 |
| `foreignKeys` | `List<ForeignKeyInfo>` | 外键信息列表 |
| `rowCount` | `long` | 行数 |

**文件位置**: [src/main/java/com/sqlparse/metadata/TableInfo.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/metadata/TableInfo.java)

#### ColumnInfo.java

**职责**: 列信息模型

**核心属性**:

| 属性名 | 类型 | 说明 |
|--------|------|------|
| `name` | `String` | 列名 |
| `type` | `String` | 数据类型 |
| `columnSize` | `int` | 列大小 |
| `nullable` | `boolean` | 是否可空 |
| `primaryKey` | `boolean` | 是否主键 |
| `defaultValue` | `String` | 默认值 |

**文件位置**: [src/main/java/com/sqlparse/metadata/ColumnInfo.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/metadata/ColumnInfo.java)

---

### 3.5 解析器模块

#### SqlParser.java

**职责**: SQL 解析器

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `parse(String)` | `SqlNode` | 解析单条 SQL 语句 |
| `parseMultiple(String)` | `List<SqlNode>` | 解析多条 SQL 语句 |
| `formatSql(String)` | `String` | 格式化 SQL |
| `isValidSql(String)` | `boolean` | 验证 SQL 有效性 |

**文件位置**: [src/main/java/com/sqlparse/parser/SqlParser.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/parser/SqlParser.java)

#### SqlTypeDetector.java

**职责**: SQL 类型检测

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `detectType(Statement)` | `SqlType` | 检测 SQL 类型 |
| `getTypeName(SqlType)` | `String` | 获取类型名称 |
| `isSelectStatement(Statement)` | `boolean` | 判断是否 SELECT 语句 |

**文件位置**: [src/main/java/com/sqlparse/parser/SqlTypeDetector.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/parser/SqlTypeDetector.java)

---

### 3.6 验证器模块

#### ResultValidator.java

**职责**: 结果一致性验证

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `validate(String, String)` | `ValidationResult` | 验证两条 SQL 结果一致性 |
| `executeQuery(String)` | `QueryResult` | 执行 SQL 并返回结果 |

**内部类 ValidationResult**:

| 属性名 | 类型 | 说明 |
|--------|------|------|
| `consistent` | `boolean` | 是否一致 |
| `message` | `String` | 验证消息 |
| `firstInconsistentRow` | `int` | 第一个不一致行的索引 |

**文件位置**: [src/main/java/com/sqlparse/validator/ResultValidator.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/validator/ResultValidator.java)

#### QueryResult.java

**职责**: 查询结果封装

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `fromResultSet(ResultSet)` | `QueryResult` | 从 ResultSet 创建实例 |
| `getColumnCount()` | `int` | 获取列数 |
| `getRowCount()` | `int` | 获取行数 |
| `getRow(int)` | `RowData` | 获取指定行数据 |

**文件位置**: [src/main/java/com/sqlparse/validator/QueryResult.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/validator/QueryResult.java)

---

### 3.7 测试数据模块

#### EcommerceSchemaGenerator.java

**职责**: 电商数据库 Schema 生成器

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `getCreateTableStatements()` | `List<String>` | 获取所有建表语句 |
| `getCreateIndexStatements()` | `List<String>` | 获取所有建索引语句 |
| `createSchema(String)` | `void` | 创建数据库 Schema |

**文件位置**: [src/main/java/com/sqlparse/testdata/EcommerceSchemaGenerator.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/testdata/EcommerceSchemaGenerator.java)

#### TestDataGenerator.java

**职责**: 测试数据生成器

**核心方法**:

| 方法名 | 返回类型 | 功能说明 |
|--------|----------|----------|
| `generateTestData(String, int)` | `void` | 生成所有表的测试数据 |
| `generateUsers(Connection, int)` | `void` | 生成用户数据 |
| `generateProducts(Connection, int)` | `void` | 生成商品数据 |
| `generateOrders(Connection, int)` | `void` | 生成订单数据 |
| `generateReviews(Connection, int)` | `void` | 生成评论数据 |

**文件位置**: [src/main/java/com/sqlparse/testdata/TestDataGenerator.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/testdata/TestDataGenerator.java)

---

## 4. 优化规则详解

### 4.1 优化规则列表

| 序号 | 规则名称 | 优先级 | 功能描述 |
|------|---------|--------|----------|
| 1 | ConstantFoldingRule | 最高 | 编译期计算常量表达式 |
| 2 | RemoveAlwaysTrueOrFalseRule | 高 | 删除恒真/恒假条件 |
| 3 | ArithmeticComparisonOptimizeRule | 高 | 简化算术比较表达式 |
| 4 | NullCheckEliminationRule | 高 | 消除冗余 NULL 检查 |
| 5 | SimplifyCountStarRule | 高 | 简化 COUNT 表达式 |
| 6 | RemoveRedundantDistinctRule | 中 | 移除冗余 DISTINCT |
| 7 | RemoveUnnecessaryOrderByRule | 中 | 移除不必要排序 |
| 8 | RemoveRedundantLimitRule | 中 | 移除冗余 LIMIT |
| 9 | CaseSimplificationRule | 中 | 简化 CASE 表达式 |
| 10 | OrConditionRewriteRule | 中 | 重写 OR 条件为 IN |
| 11 | LikePatternOptimizeRule | 中 | 优化 LIKE 模式 |
| 12 | UseExistsInsteadOfInRule | 中 | IN 转 EXISTS |
| 13 | NotInToNotExistsRule | 中 | NOT IN 转 NOT EXISTS |
| 14 | SubqueryToJoinRule | 中 | 子查询转 JOIN |
| 15 | PredicatePushdownRule | 中 | 谓词下推 |
| 16 | MergeNestedSubqueryRule | 中 | 合并嵌套子查询 |
| 17 | SimplifyWithClauseRule | 中 | 简化 CTE |
| 18 | RemoveRedundantJoinsRule | 低 | 移除冗余 JOIN |
| 19 | ColumnPruningRule | 低 | 列裁剪（需元数据） |
| 20 | OptimizeJoinOrderRule | 低 | 优化 JOIN 顺序（需元数据） |
| 21 | MergeUnionRule | 最低 | 合并 UNION |

### 4.2 核心规则实现位置

| 规则类 | 文件位置 |
|--------|----------|
| ConstantFoldingRule | [optimizer/rules/ConstantFoldingRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/ConstantFoldingRule.java) |
| RemoveAlwaysTrueOrFalseRule | [optimizer/rules/RemoveAlwaysTrueOrFalseRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/RemoveAlwaysTrueOrFalseRule.java) |
| SimplifyCountStarRule | [optimizer/rules/SimplifyCountStarRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/SimplifyCountStarRule.java) |
| RemoveRedundantDistinctRule | [optimizer/rules/RemoveRedundantDistinctRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/RemoveRedundantDistinctRule.java) |
| RemoveUnnecessaryOrderByRule | [optimizer/rules/RemoveUnnecessaryOrderByRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/RemoveUnnecessaryOrderByRule.java) |
| UseExistsInsteadOfInRule | [optimizer/rules/UseExistsInsteadOfInRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/UseExistsInsteadOfInRule.java) |
| PredicatePushdownRule | [optimizer/rules/PredicatePushdownRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/PredicatePushdownRule.java) |
| MergeNestedSubqueryRule | [optimizer/rules/MergeNestedSubqueryRule.java](file:///e:/work/trae/mySqlparse/src/main/java/com/sqlparse/optimizer/rules/MergeNestedSubqueryRule.java) |

---

## 5. SQL 优化流程

### 5.1 优化流程六阶段

```
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│ 阶段一       │───▶│ 阶段二       │───▶│ 阶段三           │
│ 输入处理     │    │ SQL解析      │    │ 元数据收集(可选) │
│ 参数解析     │    │ (AST构建)    │    │                  │
└──────────────┘    └──────────────┘    └────────┬─────────┘
                                                 │
                                                 ▼
┌──────────────────────────────────────────────────────────────┐
│                    阶段四：规则引擎执行                       │
│  RuleEngine: 按优先级顺序应用21条优化规则                      │
│  每规则: 匹配检测 → 条件验证 → 优化转换 → 日志记录            │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│ 阶段五       │───▶│ 阶段六       │───▶│   输出报告       │
│ 结果生成     │    │ 结果验证     │    │                  │
│ (AST→SQL)    │    │ (可选)       │    │                  │
└──────────────┘    └──────────────┘    └──────────────────┘
```

### 5.2 核心流程说明

**阶段一：输入处理**
- 接收命令行参数
- 解析执行模式（单次优化、批量优化、测试模式等）
- 建立数据库连接（如需要）

**阶段二：SQL 解析**
- 使用 JSqlParser 将 SQL 字符串解析为 AST
- 检测 SQL 类型（SELECT/INSERT/UPDATE/DELETE）
- 本优化器仅处理 SELECT 查询

**阶段三：元数据收集（可选）**
- 通过 JDBC 获取数据库元数据
- 元数据用于支持高级优化规则（如 JOIN 顺序优化、列裁剪）
- 如果未连接数据库，仅执行基于语法的优化

**阶段四：规则引擎执行（核心）**
- 按优先级顺序遍历 21 条优化规则
- 每条规则执行：匹配检测 → 条件验证 → 优化转换 → 日志记录
- 更新 AST 并继续下一条规则

**阶段五：结果生成**
- 将优化后的 AST 转换回 SQL 字符串
- 格式化输出保持可读性
- 记录优化日志

**阶段六：结果验证（可选）**
- 执行原始 SQL 和优化后的 SQL
- 对比查询结果（行数、数据内容）
- 验证优化的正确性，确保语义等价

---

## 6. 数据库设计

### 6.1 数据库表结构

项目包含完整的电商业务数据库，共 10 个业务表：

| 表名 | 主键 | 说明 |
|------|------|------|
| users | user_id | 用户表 |
| categories | category_id | 商品分类表 |
| brands | brand_id | 品牌表 |
| products | product_id | 商品表 |
| inventory | inventory_id | 库存表 |
| orders | order_id | 订单表 |
| order_items | order_item_id | 订单明细表 |
| payments | payment_id | 支付表 |
| shipping | shipping_id | 物流表 |
| reviews | review_id | 评论表 |

### 6.2 表关系图

```
users 1──────* orders 1──────* order_items
  │              │                 │
  │              │                 ▼
  │              │            products 1───* inventory
  │              │                 │
  │              ▼                 │
  │         payments               │
  │              │                 │
  │              │                 │
  └───────────► reviews ◄─────────┘
                   │
                   ▼
              categories ◄── brands
```

---

## 7. 依赖关系

### 7.1 Maven 依赖

```xml
<dependencies>
    <!-- SQL 解析库 -->
    <dependency>
        <groupId>com.github.jsqlparser</groupId>
        <artifactId>jsqlparser</artifactId>
        <version>4.5</version>
    </dependency>

    <!-- SQLite JDBC 驱动 -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.50.2.0</version>
    </dependency>

    <!-- 命令行参数解析 -->
    <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>1.9.0</version>
    </dependency>

    <!-- 测试框架 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 7.2 模块依赖关系

```
Main
 │
 ├─► SqlOptimizer
 │     │
 │     ├─► RuleEngine
 │     │     │
 │     │     ├─► OptimizationRule (21条规则)
 │     │     ├─► OptimizationContext
 │     │     └─► OptimizationLog
 │     │
 │     └─► OptimizationResult
 │
 ├─► DatabaseConnectionManager
 │     │
 │     └─► MetadataFetcher
 │           │
 │           └─► DatabaseMetadata
 │                 │
 │                 ├─► TableInfo
 │                 ├─► ColumnInfo
 │                 ├─► IndexInfo
 │                 └─► ForeignKeyInfo
 │
 ├─► SqlParser
 │     │
 │     └─► SqlTypeDetector
 │
 ├─► ResultValidator
 │     │
 │     ├─► QueryResult
 │     └─► RowData
 │
 └─► EcommerceSchemaGenerator
       │
       └─► TestDataGenerator
```

---

## 8. 项目运行方式

### 8.1 环境要求

- Java 11 或更高版本
- Maven 3.6+

### 8.2 构建项目

```bash
cd e:\work\trae\mySqlparse
mvn clean package
```

### 8.3 命令行参数

| 参数 | 长参数 | 说明 |
|------|--------|------|
| -h | --help | 显示帮助信息 |
| -db | --database | 数据库连接 URL（默认：jdbc:sqlite:test.db） |
| -f | --file | 从文件读取 SQL 语句 |
| -v | --verbose | 显示详细优化信息 |
| | --validate | 验证优化前后结果一致性 |
| | --test | 运行完整测试套件 |
| | --init-db | 初始化测试数据库 |
| | --rows | 生成测试数据的行数（默认：100） |

### 8.4 使用示例

**1. 初始化测试数据库**
```bash
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --init-db
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --init-db --rows 10000
```

**2. 运行完整测试套件**
```bash
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --test
```

**3. 优化 SQL 语句**
```bash
# 优化单个 SQL
java -jar target/mySqlparse-1.0-SNAPSHOT.jar "SELECT COUNT(1) FROM users"

# 从文件读取 SQL
java -jar target/mySqlparse-1.0-SNAPSHOT.jar -f test_queries_100.sql

# 显示详细信息
java -jar target/mySqlparse-1.0-SNAPSHOT.jar -v "SELECT COUNT(1) FROM users"

# 验证结果一致性
java -jar target/mySqlparse-1.0-SNAPSHOT.jar --validate "SELECT * FROM users"
```

---

## 9. 添加新优化规则指南

### 9.1 步骤说明

1. **创建规则类**：在 `com.sqlparse.optimizer.rules` 包下创建新类
2. **实现接口**：实现 `OptimizationRule` 接口的四个方法
3. **注册规则**：在 `SqlOptimizer.initializeRuleEngine()` 中注册新规则
4. **编写测试**：编写单元测试验证规则正确性

### 9.2 规则类模板

```java
package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;

public class MyOptimizationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "MyOptimizationRule";
    }

    @Override
    public String getDescription() {
        return "描述规则的功能";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        // 判断是否可以应用此规则
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        // 执行优化逻辑
        return statement;
    }
}
```

---

## 10. 项目文件结构

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
│       │       ├── ConstantFoldingRule.java
│       │       ├── SimplifyCountStarRule.java
│       │       ├── RemoveRedundantDistinctRule.java
│       │       └── ...
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
├── README.md                            # 项目说明
├── DATABASE_README.md                   # 数据库详细说明
├── test_queries_100.sql                 # 100条复杂SQL测试用例
├── schema.sql                           # 数据库建表SQL
├── test_report.md                       # 最新测试报告
└── example_test_report.md               # 示例测试报告
```

---

## 11. 许可证

本项目采用 MIT 许可证。

---

*文档版本: v1.0*  
*生成日期: 2026-05-19*