package com.sqlparse;

import com.sqlparse.metadata.DatabaseConnectionManager;
import com.sqlparse.metadata.DatabaseMetadata;
import com.sqlparse.metadata.MetadataFetcher;
import com.sqlparse.optimizer.OptimizationResult;
import com.sqlparse.optimizer.RuleApplication;
import com.sqlparse.testdata.EcommerceSchemaGenerator;
import com.sqlparse.testdata.TestDataGenerator;
import com.sqlparse.validator.QueryResult;
import com.sqlparse.validator.ResultValidator;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                formatter.printHelp("sql-optimizer", options);
                return;
            }

            if (cmd.hasOption("test")) {
                String testFile = cmd.getOptionValue("test", "test_queries_100.sql");
                String dbUrl = cmd.getOptionValue("db", "jdbc:sqlite:test.db");
                boolean verbose = cmd.hasOption("v");
                boolean validate = cmd.hasOption("validate");
                runTestSuite(dbUrl, testFile, verbose, validate);
                return;
            }

            if (cmd.hasOption("init-db")) {
                String dbUrl = cmd.getOptionValue("db", "jdbc:sqlite:test.db");
                int rowCount = 100;
                if (cmd.hasOption("rows")) {
                    rowCount = Integer.parseInt(cmd.getOptionValue("rows"));
                }
                initializeTestDatabase(dbUrl, rowCount);
                return;
            }

            String dbUrl = cmd.getOptionValue("db", "jdbc:sqlite:test.db");
            boolean verbose = cmd.hasOption("v");
            boolean validate = cmd.hasOption("validate");
            
            List<String> sqlList = new ArrayList<>();
            
            String filePath = cmd.hasOption("f") ? cmd.getOptionValue("f") : "test_queries_100.sql";
            try {
                sqlList = loadSqlFromFile(filePath);
                System.out.println("从文件加载了 " + sqlList.size() + " 条SQL");
            } catch (IOException e) {
                System.out.println("警告: " + e.getMessage() + "，请通过命令行参数提供SQL语句");
            }
            
            String[] remainingArgs = cmd.getArgs();
            for (String arg : remainingArgs) {
                sqlList.add(arg);
            }
            
            if (sqlList.isEmpty()) {
                System.out.println("SQL列表为空，请检查文件或提供SQL语句");
                formatter.printHelp("sql-optimizer", options);
                return;
            }

            processSqlOptimization(dbUrl, sqlList, verbose, validate);

        } catch (ParseException e) {
            System.err.println("参数解析错误: " + e.getMessage());
            formatter.printHelp("sql-optimizer", options);
        } catch (Exception e) {
            System.err.println("执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> loadSqlFromFile(String filePath) throws IOException {
        List<String> queries = new ArrayList<>();
        StringBuilder currentQuery = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--")) {
                    continue;
                }
                if (line.isEmpty()) {
                    if (currentQuery.length() > 0) {
                        String sql = currentQuery.toString().trim();
                        if (!sql.isEmpty()) {
                            queries.add(sql);
                        }
                        currentQuery = new StringBuilder();
                    }
                    continue;
                }
                currentQuery.append(line).append(" ");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IOException("不支持的编码: UTF-8", e);
        }
        
        if (currentQuery.length() > 0) {
            String sql = currentQuery.toString().trim();
            if (!sql.isEmpty()) {
                queries.add(sql);
            }
        }
        
        return queries;
    }

    private static Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("显示帮助信息")
                .build());

        options.addOption(Option.builder("db")
                .longOpt("database")
                .hasArg()
                .argName("url")
                .desc("数据库连接URL (默认: jdbc:sqlite:test.db)")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("file")
                .hasArg()
                .argName("file")
                .desc("从文件读取SQL语句")
                .build());

        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("显示详细优化信息")
                .build());

        options.addOption(Option.builder()
                .longOpt("validate")
                .desc("验证优化前后结果一致性")
                .build());

        options.addOption(Option.builder()
                .longOpt("test")
                .hasArg()
                .argName("file")
                .desc("运行完整的测试套件 (可选: 指定SQL文件, 如 test_queries_3.sql 或 test_queries_100.sql)")
                .build());

        options.addOption(Option.builder()
                .longOpt("init-db")
                .desc("初始化测试数据库")
                .build());

        options.addOption(Option.builder()
                .longOpt("rows")
                .hasArg()
                .argName("count")
                .desc("初始化测试数据时生成的数据行数 (默认: 100)")
                .build());

        return options;
    }

    private static void runTestSuite(String dbUrl, String testFile, boolean verbose, boolean validate) {
        System.out.println("========================================");
        System.out.println("  运行完整的测试套件");
        System.out.println("========================================\n");

        try {
            initializeTestDatabase(dbUrl, 100);
            
            List<String> sqlList = loadSqlFromFile(testFile);
            System.out.println("\n从 " + testFile + " 加载了 " + sqlList.size() + " 条SQL进行测试\n");
            
            int successCount = 0;
            int failCount = 0;
            int optimizedCount = 0;
            int consistentCount = 0;
            long totalOriginalTime = 0;
            long totalOptimizedTime = 0;
            
            for (int i = 0; i < sqlList.size(); i++) {
                String sql = sqlList.get(i);
                String sqlPreview = sql.length() > 60 ? sql.substring(0, 60) + "..." : sql;
                System.out.println("\n" + "=".repeat(60));
                System.out.println("测试 [" + (i + 1) + "/" + sqlList.size() + "]");
                System.out.println("=".repeat(60));
                
                try {
                    DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(dbUrl);
                    SqlOptimizer optimizer = new SqlOptimizer();
                    
                    OptimizationResult result = optimizer.optimize(sql);
                    String optimizedSql = result.getOptimizedSql();
                    
                    int appliedRules = 0;
                    for (RuleApplication app : result.getAppliedRules()) {
                        if (app.isApplied()) {
                            appliedRules++;
                        }
                    }
                    
                    if (verbose) {
                        System.out.println("\n--- 原始SQL ---");
                        System.out.println(sql);
                        System.out.println("\n--- 优化后SQL ---");
                        System.out.println(optimizedSql);
                    }
                    
                    if (!sql.equals(optimizedSql)) {
                        optimizedCount++;
                        System.out.println("\n[优化结果] ✓ 优化成功，应用了 " + appliedRules + " 条规则");
                        
                        if (verbose) {
                            System.out.println("\n[详细规则列表]");
                            for (RuleApplication app : result.getAppliedRules()) {
                                if (app.isApplied()) {
                                    System.out.printf("  #%02d ✓ %s - %s%n", 
                                        app.getRuleId(), app.getRuleName(), app.getRuleDescription());
                                }
                            }
                        }
                        
                        if (validate) {
                            System.out.println("\n[验证结果]");
                            ResultValidator validator = new ResultValidator(connectionManager);
                            ResultValidator.QueryResultWithStats stats1 = validator.executeQueryWithStats(sql);
                            ResultValidator.QueryResultWithStats stats2 = validator.executeQueryWithStats(optimizedSql);
                            
                            totalOriginalTime += stats1.getExecutionTimeNanos();
                            totalOptimizedTime += stats2.getExecutionTimeNanos();
                            
                            if (stats1.isSuccess() && stats2.isSuccess()) {
                                QueryResult qr1 = stats1.getQueryResult();
                                QueryResult qr2 = stats2.getQueryResult();
                                
                                System.out.printf("  原始SQL: %d 行, %d 列 | 耗时: %d 纳秒%n",
                                    qr1.getRowCount(), qr1.getColumnCount(), stats1.getExecutionTimeNanos());
                                System.out.printf("  优化SQL: %d 行, %d 列 | 耗时: %d 纳秒%n",
                                    qr2.getRowCount(), qr2.getColumnCount(), stats2.getExecutionTimeNanos());
                                
                                if (qr1.getRowCount() == qr2.getRowCount() && qr1.getColumnCount() == qr2.getColumnCount()) {
                                    boolean consistent = true;
                                    for (int r = 0; r < qr1.getRowCount() && consistent; r++) {
                                        if (!qr1.getRow(r).equals(qr2.getRow(r))) {
                                            consistent = false;
                                        }
                                    }
                                    if (consistent) {
                                        consistentCount++;
                                        System.out.println("  数据一致性: ✓ 一致");
                                    } else {
                                        System.out.println("  数据一致性: ✗ 不一致");
                                    }
                                } else {
                                    System.out.println("  数据一致性: ✗ 行数或列数不一致");
                                }
                                
                                long timeDiff = stats1.getExecutionTimeNanos() - stats2.getExecutionTimeNanos();
                                double improvement = stats1.getExecutionTimeNanos() > 0 ? 
                                    (double) timeDiff / stats1.getExecutionTimeNanos() * 100 : 0;
                                if (improvement > 0) {
                                    System.out.printf("  性能提升: %.1f%% (节省 %d 纳秒)%n", improvement, timeDiff);
                                } else {
                                    System.out.printf("  性能变化: %.1f%% (增加 %d 纳秒)%n", -improvement, -timeDiff);
                                }
                            } else {
                                System.out.println("  SQL执行失败:");
                                if (!stats1.isSuccess()) {
                                    System.out.println("    原始SQL错误: " + stats1.getErrorMessage());
                                }
                                if (!stats2.isSuccess()) {
                                    System.out.println("    优化SQL错误: " + stats2.getErrorMessage());
                                }
                            }
                        }
                    } else {
                        System.out.println("\n[优化结果] - 无需优化");
                    }
                    
                    connectionManager.closeConnection();
                } catch (Exception e) {
                    failCount++;
                    System.out.println("\n[错误] ✗ " + e.getMessage());
                }
            }
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("  测试完成！");
            System.out.println("=".repeat(60));
            System.out.println("\n统计信息:");
            System.out.println("  总SQL数: " + sqlList.size());
            System.out.println("  优化成功: " + optimizedCount);
            System.out.println("  执行失败: " + failCount);
            if (validate && consistentCount > 0) {
                System.out.println("  结果一致: " + consistentCount + "/" + optimizedCount);
                if (totalOriginalTime > 0) {
                    double totalImprovement = (double) (totalOriginalTime - totalOptimizedTime) / totalOriginalTime * 100;
                    System.out.printf("  总体性能提升: %.1f%%%n", totalImprovement);
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试运行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeTestDatabase(String dbUrl, int rowCount) {
        try {
            System.out.println("   创建数据库表结构...");
            EcommerceSchemaGenerator.createSchema(dbUrl);
            
            System.out.println("   生成测试数据 (" + rowCount + " 行)...");
            TestDataGenerator.generateTestData(dbUrl, rowCount);
            
            System.out.println("   ✓ 测试数据库初始化完成！");
        } catch (Exception e) {
            System.err.println("   ✗ 数据库初始化失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void processSqlOptimization(String dbUrl, List<String> sqlList, boolean verbose, boolean validate) {
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(dbUrl);
        SqlOptimizer optimizer = new SqlOptimizer();
        
        try {
            DatabaseMetadata metadata = null;
            if (connectionManager.testConnection()) {
                MetadataFetcher fetcher = new MetadataFetcher(connectionManager);
                metadata = fetcher.fetchAllMetadata();
                optimizer.setMetadata(metadata);
            }

            for (String sql : sqlList) {
                System.out.println("\n========== 处理SQL ==========");
                System.out.println("原始SQL: " + sql);
                
                try {
                    OptimizationResult result = optimizer.optimize(sql);
                    
                    System.out.println("\n优化后SQL: " + result.getOptimizedSql());
                    
                    if (verbose) {
                        System.out.println("\n=== 详细优化信息 ===");
                        System.out.println("应用规则数: " + result.getAppliedRuleCount());
                        
                        for (RuleApplication application : result.getAppliedRules()) {
                            String status = application.isApplied() ? "✓ 应用" : "✗ 未应用";
                            System.out.printf("%s: %s - %s%n", status, application.getRuleName(), application.getRuleDescription());
                        }
                    }
                    
                    if (validate && !sql.equals(result.getOptimizedSql())) {
                        ResultValidator validator = new ResultValidator(connectionManager);
                        ResultValidator.ValidationResult validationResult = validator.validate(sql, result.getOptimizedSql());
                        
                        System.out.println("\n=== 结果一致性验证 ===");
                        if (validationResult.isConsistent()) {
                            System.out.println("✓ " + validationResult.getMessage());
                        } else {
                            System.out.println("✗ " + validationResult.getMessage());
                        }
                    }
                    
                } catch (JSQLParserException e) {
                    System.err.println("SQL解析错误: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("处理错误: " + e.getMessage());
                    if (verbose) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("初始化错误: " + e.getMessage());
        } finally {
            connectionManager.closeConnection();
        }
    }
}
