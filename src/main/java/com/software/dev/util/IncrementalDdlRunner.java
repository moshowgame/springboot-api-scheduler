package com.software.dev.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 增量 DDL 执行器（简易数据库迁移工具，无第三方依赖）
 * <p>
 * 用法：
 * <pre>
 *   # 1. 存量库首次接入（已部署到某版本，补记历史，不执行任何脚本）
 *   java com.software.dev.util.IncrementalDdlRunner --baseline 1.1.0
 *
 *   # 2. 日常升级（自动执行历史表中没有记录的、从已记录版本之后到现在的全部增量）
 *   java com.software.dev.util.IncrementalDdlRunner
 *
 *   # 3. 显式指定只执行某版本之后的增量
 *   java com.software.dev.util.IncrementalDdlRunner --from 1.0.2
 *
 *   可选参数：--url / --user / --password / --dir
 * </pre>
 * <p>
 * 规则：
 * - 增量脚本放在 sql/incremental 目录，命名 V&lt;版本号&gt;__&lt;说明&gt;.sql，版本号语义化递增；
 * - 每次执行会在 schema_incremental_history 表中记录版本、脚本、执行时间与耗时（APPLY 模式）；
 * - baseline 模式只补历史记录不执行（BASELINE 模式），用于存量库初始化；
 * - 脚本需幂等（CREATE TABLE IF NOT EXISTS / ADD COLUMN IF NOT EXISTS），失败即中止，不记录历史，修复后可重跑。
 */
public class IncrementalDdlRunner {

    private static final Pattern FILE_PATTERN = Pattern.compile("V([0-9.]+)__([\\w\\-]+)\\.sql");

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/api_scheduler";
        String user = "postgres";
        String password = "root123";
        String dir = "src/main/resources/sql/incremental";
        String baseline = null;
        String from = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--url" -> url = args[++i];
                case "--user" -> user = args[++i];
                case "--password" -> password = args[++i];
                case "--dir" -> dir = args[++i];
                case "--baseline" -> baseline = args[++i];
                case "--from" -> from = args[++i];
                default -> throw new IllegalArgumentException("Unknown arg: " + args[i]);
            }
        }

        List<Script> scripts = scanScripts(Paths.get(dir));
        System.out.println("Found " + scripts.size() + " incremental script(s) in " + dir);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not on classpath. "
                    + "Add postgresql.jar, e.g.: java -cp \"target/classes;<m2>/org/postgresql/postgresql/<x>/postgresql-<x>.jar\" ...");
        }

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            ensureHistoryTable(conn);

            if (baseline != null) {
                markBaseline(conn, scripts, baseline);
            } else {
                applyPending(conn, scripts, from);
            }
            conn.commit();
        }
        System.out.println("Done.");
    }

    // ==================== 扫描 ====================

    private static List<Script> scanScripts(Path dir) throws Exception {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Incremental dir not found: " + dir.toAbsolutePath());
        }
        List<Script> scripts = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(f -> FILE_PATTERN.matcher(f.getFileName().toString()).matches())
                    .forEach(f -> {
                        Matcher m = FILE_PATTERN.matcher(f.getFileName().toString());
                        if (m.matches()) {
                            scripts.add(new Script(m.group(1), m.group(2).replace('_', ' '), f));
                        }
                    });
        }
        scripts.sort(Comparator.comparing(Script::version, IncrementalDdlRunner::compareVersions));
        return scripts;
    }

    /** 版本号数字感知比较：1.0.2 < 1.1.0 < 1.10.0 */
    static int compareVersions(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int va = i < pa.length ? Integer.parseInt(pa[i]) : 0;
            int vb = i < pb.length ? Integer.parseInt(pb[i]) : 0;
            if (va != vb) {
                return Integer.compare(va, vb);
            }
        }
        return 0;
    }

    // ==================== baseline ====================

    private static void markBaseline(Connection conn, List<Script> scripts, String baseline) throws Exception {
        int marked = 0;
        for (Script s : scripts) {
            if (compareVersions(s.version, baseline) > 0) {
                continue;
            }
            if (isApplied(conn, s.version)) {
                System.out.println("SKIP (already recorded): " + s.file.getFileName());
                continue;
            }
            insertHistory(conn, s, "BASELINE", 0);
            marked++;
            System.out.println("BASELINE marked: " + s.version + " " + s.file.getFileName());
        }
        System.out.println("Baseline " + baseline + " done, " + marked + " script(s) marked as applied.");
    }

    // ==================== apply ====================

    private static void applyPending(Connection conn, List<Script> scripts, String from) throws Exception {
        int applied = 0;
        for (Script s : scripts) {
            if (from != null && compareVersions(s.version, from) <= 0) {
                System.out.println("SKIP (--from " + from + "): " + s.file.getFileName());
                continue;
            }
            if (isApplied(conn, s.version)) {
                System.out.println("SKIP (already applied): " + s.version + " " + s.file.getFileName());
                continue;
            }
            System.out.println("APPLY: " + s.version + " " + s.file.getFileName());
            long start = System.currentTimeMillis();
            for (String stmt : splitStatements(Files.readString(s.file, StandardCharsets.UTF_8))) {
                try (Statement st = conn.createStatement()) {
                    st.execute(stmt);
                }
            }
            long cost = System.currentTimeMillis() - start;
            insertHistory(conn, s, "APPLY", (int) cost);
            applied++;
            System.out.println("APPLIED in " + cost + " ms: " + s.version);
        }
        if (applied == 0) {
            System.out.println("No pending incremental script. Database is up to date.");
        } else {
            System.out.println(applied + " incremental script(s) applied.");
        }
    }

    // ==================== history ====================

    private static void ensureHistoryTable(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS schema_incremental_history (
                        version VARCHAR(50) PRIMARY KEY,
                        script_name VARCHAR(200) NOT NULL,
                        description VARCHAR(200),
                        applied_mode VARCHAR(20) NOT NULL,
                        executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        execution_ms INT,
                        success BOOLEAN NOT NULL DEFAULT true
                    )""");
        }
    }

    private static boolean isApplied(Connection conn, String version) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM schema_incremental_history WHERE version = ?")) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertHistory(Connection conn, Script s, String mode, int costMs) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO schema_incremental_history (version, script_name, description, applied_mode, executed_at, execution_ms, success) "
                        + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, true)")) {
            ps.setString(1, s.version);
            ps.setString(2, s.file.getFileName().toString());
            ps.setString(3, s.description);
            ps.setString(4, mode);
            ps.setInt(5, costMs);
            ps.executeUpdate();
        }
    }

    // ==================== SQL 解析 ====================

    /** 去掉 -- 注释行后按分号拆分语句 */
    static List<String> splitStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
        }
        for (String part : current.toString().split(";")) {
            String stmt = part.trim();
            if (!stmt.isEmpty()) {
                statements.add(stmt);
            }
        }
        return statements;
    }

    private record Script(String version, String description, Path file) {}
}
