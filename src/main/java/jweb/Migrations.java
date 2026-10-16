package jweb;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Versioned SQL migrations, applied once each, in order, and remembered in
 * the database.
 *
 * <p>A migration is a file named {@code V<version>__<name>.sql} — the Flyway
 * convention, so the files move to Flyway unchanged the day an app wants it —
 * under {@code db/migration} on the classpath ({@code src/main/resources/db/migration}):</p>
 *
 * <pre>
 * db/migration/V1__create_users.sql
 * db/migration/V2__add_users_email_index.sql
 * db/migration/V2.1__backfill_names.sql
 * </pre>
 *
 * <p>At startup, when the app has a {@code DataSource} bean, the pending ones
 * run before any {@code ApplicationRunner} of the app's own. Each file runs in
 * one transaction (where the database makes DDL transactional: PostgreSQL, H2,
 * SQLite — MySQL commits DDL as it goes) and is recorded in
 * {@code jweb_migrations} with a checksum; a file edited after it was applied
 * fails the next start with the version named, which is the point.
 * {@link #repair(DataSource)} accepts the edited checksums when the edit was
 * deliberate.</p>
 *
 * <p>Several instances starting at once take turns: one holds a lock row in
 * the history table while it migrates; the others wait for it and then find
 * nothing pending.</p>
 *
 * <p>Statements are split on {@code ;} outside quotes, comments and PostgreSQL
 * {@code $$} bodies. A file whose first line is {@code -- jweb:whole} is sent
 * as one statement.</p>
 *
 * <p>Configuration: {@code jweb.sql.migrate} ({@code true}) runs them at
 * startup; {@code jweb.sql.migrations} ({@code classpath:db/migration}) is
 * where to look. When Flyway or Liquibase is on the classpath, this class
 * steps aside and lets it own the schema.</p>
 *
 * <pre>
 * Migrations.Report report = Migrations.migrate(dataSource);
 * report.applied()            // the migrations this call ran
 * Migrations.pending(dataSource)
 * Migrations.applied(dataSource)
 * </pre>
 */
public final class Migrations {

    /** Where migrations live unless configured otherwise. */
    public static final String DEFAULT_LOCATION = "classpath:db/migration";

    /** The history table. */
    public static final String TABLE = "jweb_migrations";

    private static final String LOCK_VERSION = "__lock__";
    private static final Pattern FILE_NAME = Pattern.compile("V([0-9][0-9._]*)__(.+)\\.sql");
    private static final long LOCK_WAIT_MS = 60_000;
    private static final long LOCK_STALE_MS = 10 * 60_000;

    private Migrations() {}

    /** One migration file. */
    public record Migration(String version, String name, String checksum, String sql, String file)
        implements Comparable<Migration> {

        @Override
        public int compareTo(Migration other) {
            return compareVersions(version, other.version);
        }
    }

    /** One row of the history table. */
    public record Applied(String version, String name, String checksum, Instant appliedAt) {}

    /** What a {@link #migrate(DataSource)} call did. */
    public record Report(List<Migration> applied, int alreadyApplied) {

        /** How many ran in this call. */
        public int count() {
            return applied.size();
        }
    }

    /** A migration that could not run, or a history that does not match the files. */
    public static class MigrationException extends RuntimeException {
        public MigrationException(String message) {
            super(message);
        }

        public MigrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ==================== migrate ====================

    /** Applies the pending migrations under {@link #DEFAULT_LOCATION}. */
    public static Report migrate(DataSource dataSource) {
        return migrate(dataSource, DEFAULT_LOCATION);
    }

    /**
     * Applies the pending migrations under the location ({@code classpath:…} or
     * {@code file:…}), validating the already-applied ones against their files.
     */
    public static Report migrate(DataSource dataSource, String location) {
        List<Migration> files = load(location);
        try (Connection connection = dataSource.getConnection()) {
            ensureTable(connection);
            acquireLock(connection);
            try {
                Map<String, Applied> history = readHistory(connection);
                validate(files, history);
                List<Migration> applied = new ArrayList<>();
                String latest = history.keySet().stream().max(Migrations::compareVersions).orElse(null);
                for (Migration migration : files) {
                    if (history.containsKey(migration.version())) continue;
                    if (latest != null && compareVersions(migration.version(), latest) < 0) {
                        log("Applying {} out of order: {} is already applied", migration.file(), latest);
                    }
                    apply(connection, migration);
                    applied.add(migration);
                }
                return new Report(applied, history.size());
            } finally {
                releaseLock(connection);
            }
        } catch (SQLException e) {
            throw new MigrationException("Could not open the database for migrations: " + e.getMessage(), e);
        }
    }

    /** The migrations under the default location that the database has not applied. */
    public static List<Migration> pending(DataSource dataSource) {
        return pending(dataSource, DEFAULT_LOCATION);
    }

    public static List<Migration> pending(DataSource dataSource, String location) {
        List<Migration> files = load(location);
        Map<String, Applied> history = applied(dataSource).stream()
            .collect(HashMap::new, (m, a) -> m.put(a.version(), a), HashMap::putAll);
        return files.stream().filter(m -> !history.containsKey(m.version())).toList();
    }

    /** The history table, in version order; empty when it does not exist yet. */
    public static List<Applied> applied(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection)) return List.of();
            List<Applied> out = new ArrayList<>(readHistory(connection).values());
            out.sort((a, b) -> compareVersions(a.version(), b.version()));
            return out;
        } catch (SQLException e) {
            throw new MigrationException("Could not read " + TABLE + ": " + e.getMessage(), e);
        }
    }

    /** Rewrites the stored checksums to the files' current ones, for migrations edited on purpose. */
    public static int repair(DataSource dataSource) {
        return repair(dataSource, DEFAULT_LOCATION);
    }

    public static int repair(DataSource dataSource, String location) {
        List<Migration> files = load(location);
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection)) return 0;
            Map<String, Applied> history = readHistory(connection);
            int repaired = 0;
            try (PreparedStatement update = connection.prepareStatement(
                "update " + TABLE + " set checksum = ? where version = ?")) {
                for (Migration migration : files) {
                    Applied applied = history.get(migration.version());
                    if (applied == null || applied.checksum().equals(migration.checksum())) continue;
                    update.setString(1, migration.checksum());
                    update.setString(2, migration.version());
                    repaired += update.executeUpdate();
                }
            }
            return repaired;
        } catch (SQLException e) {
            throw new MigrationException("Could not repair " + TABLE + ": " + e.getMessage(), e);
        }
    }

    // ==================== loading ====================

    /** The migration files at the location, sorted by version. */
    public static List<Migration> load(String location) {
        List<Migration> out = new ArrayList<>();
        String pattern = location.endsWith("/") ? location + "V*__*.sql" : location + "/V*__*.sql";
        if (location.startsWith("classpath:") && !location.startsWith("classpath*:")) {
            pattern = "classpath*:" + pattern.substring("classpath:".length());
        }
        try {
            var resolver = new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
            for (var resource : resolver.getResources(pattern)) {
                String file = resource.getFilename();
                if (file == null) continue;
                Matcher m = FILE_NAME.matcher(file);
                if (!m.matches()) continue;
                String sql;
                try (InputStream in = resource.getInputStream()) {
                    sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                sql = sql.replace("\r\n", "\n");
                out.add(new Migration(m.group(1), m.group(2).replace('_', ' '), checksum(sql), sql, file));
            }
        } catch (IOException e) {
            throw new MigrationException("Could not read migrations at " + location + ": " + e.getMessage(), e);
        }
        out.sort(Comparator.naturalOrder());
        for (int i = 1; i < out.size(); i++) {
            if (out.get(i).version().equals(out.get(i - 1).version())) {
                throw new MigrationException("Two migrations carry version " + out.get(i).version() + ": "
                    + out.get(i - 1).file() + " and " + out.get(i).file());
            }
        }
        return out;
    }

    /** The SQL of one migration split into the statements it runs. */
    public static List<String> statements(String sql) {
        List<String> out = new ArrayList<>();
        if (sql.stripLeading().startsWith("-- jweb:whole")) {
            String whole = sql.strip();
            if (!whole.isEmpty()) out.add(whole);
            return out;
        }
        StringBuilder current = new StringBuilder();
        int n = sql.length();
        int i = 0;
        while (i < n) {
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                int end = sql.indexOf(c, i + 1);
                while (end >= 0 && end + 1 < n && sql.charAt(end + 1) == c && c == '\'') {
                    end = sql.indexOf(c, end + 2);
                }
                end = end < 0 ? n - 1 : end;
                current.append(sql, i, end + 1);
                i = end + 1;
            } else if (c == '-' && i + 1 < n && sql.charAt(i + 1) == '-') {
                int end = sql.indexOf('\n', i);
                i = end < 0 ? n : end;   // the comment is dropped
            } else if (c == '/' && i + 1 < n && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                i = end < 0 ? n : end + 2;
            } else if (c == '$') {
                int tagEnd = sql.indexOf('$', i + 1);
                if (tagEnd > 0 && sql.substring(i + 1, tagEnd).chars().allMatch(ch -> Character.isLetterOrDigit(ch) || ch == '_')) {
                    String tag = sql.substring(i, tagEnd + 1);
                    int end = sql.indexOf(tag, tagEnd + 1);
                    end = end < 0 ? n : end + tag.length();
                    current.append(sql, i, end);
                    i = end;
                } else {
                    current.append(c);
                    i++;
                }
            } else if (c == ';') {
                String statement = current.toString().strip();
                if (!statement.isEmpty()) out.add(statement);
                current.setLength(0);
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        String last = current.toString().strip();
        if (!last.isEmpty()) out.add(last);
        return out;
    }

    /** {@code 1.2} before {@code 1.10}; numeric per segment, split on {@code .} and {@code _}. */
    public static int compareVersions(String a, String b) {
        String[] as = a.split("[._]");
        String[] bs = b.split("[._]");
        int n = Math.max(as.length, bs.length);
        for (int i = 0; i < n; i++) {
            long x = i < as.length ? parse(as[i]) : 0;
            long y = i < bs.length ? parse(bs[i]) : 0;
            if (x != y) return Long.compare(x, y);
        }
        return 0;
    }

    private static long parse(String segment) {
        try {
            return Long.parseLong(segment);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String checksum(String sql) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(sql.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ==================== applying ====================

    private static void validate(List<Migration> files, Map<String, Applied> history) {
        for (Migration migration : files) {
            Applied applied = history.get(migration.version());
            if (applied != null && !applied.checksum().equals(migration.checksum())) {
                throw new MigrationException("Migration " + migration.file() + " was edited after it was applied "
                    + "(checksum " + migration.checksum().substring(0, 12) + " now, "
                    + applied.checksum().substring(0, 12) + " in " + TABLE + "). Put the change in a new "
                    + "migration, or run Migrations.repair(dataSource) if the edit was deliberate.");
            }
        }
    }

    private static void apply(Connection connection, Migration migration) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String running = null;
        try {
            for (String statement : statements(migration.sql())) {
                running = statement;
                try (Statement s = connection.createStatement()) {
                    s.execute(statement);
                }
            }
            try (PreparedStatement insert = connection.prepareStatement(
                "insert into " + TABLE + " (version, name, checksum, applied_at, success) values (?, ?, ?, ?, ?)")) {
                insert.setString(1, migration.version());
                insert.setString(2, migration.name());
                insert.setString(3, migration.checksum());
                insert.setTimestamp(4, Timestamp.from(Instant.now()));
                insert.setBoolean(5, true);
                insert.executeUpdate();
            }
            connection.commit();
            log("Applied migration {} ({})", migration.file(), migration.name());
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollback) {
                e.addSuppressed(rollback);
            }
            throw new MigrationException("Migration " + migration.file() + " failed: " + e.getMessage()
                + (running == null ? "" : "\n  statement: " + abbreviate(running)), e);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private static String abbreviate(String sql) {
        String one = sql.replaceAll("\\s+", " ").strip();
        return one.length() > 200 ? one.substring(0, 200) + "…" : one;
    }

    // ==================== the history table ====================

    private static void ensureTable(Connection connection) throws SQLException {
        if (tableExists(connection)) return;
        try (Statement statement = connection.createStatement()) {
            statement.execute("create table " + TABLE + " ("
                + "version varchar(50) not null primary key, "
                + "name varchar(200) not null, "
                + "checksum varchar(64) not null, "
                + "applied_at timestamp not null, "
                + "success boolean not null)");
        } catch (SQLException e) {
            // another instance created it between the check and the create
            if (!tableExists(connection)) throw e;
        }
    }

    static boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        for (String name : new String[]{TABLE, TABLE.toUpperCase(), TABLE.toLowerCase()}) {
            try (ResultSet tables = meta.getTables(null, null, name, null)) {
                if (tables.next()) return true;
            }
        }
        return false;
    }

    private static Map<String, Applied> readHistory(Connection connection) throws SQLException {
        Map<String, Applied> out = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                 "select version, name, checksum, applied_at from " + TABLE + " where success = true")) {
            while (rs.next()) {
                String version = rs.getString(1);
                if (LOCK_VERSION.equals(version)) continue;
                Timestamp at = rs.getTimestamp(4);
                out.put(version, new Applied(version, rs.getString(2), rs.getString(3), at == null ? null : at.toInstant()));
            }
        }
        return out;
    }

    /** The lock is a row with a reserved version: the primary key lets exactly one instance insert it. */
    private static void acquireLock(Connection connection) throws SQLException {
        long deadline = System.currentTimeMillis() + LOCK_WAIT_MS;
        while (true) {
            try (PreparedStatement insert = connection.prepareStatement(
                "insert into " + TABLE + " (version, name, checksum, applied_at, success) values (?, ?, ?, ?, ?)")) {
                insert.setString(1, LOCK_VERSION);
                insert.setString(2, "lock");
                insert.setString(3, "-");
                insert.setTimestamp(4, Timestamp.from(Instant.now()));
                insert.setBoolean(5, false);
                insert.executeUpdate();
                if (!connection.getAutoCommit()) connection.commit();
                return;
            } catch (SQLException taken) {
                if (!connection.getAutoCommit()) connection.rollback();
                // only a duplicate key means another instance holds the lock (SQLState class 23, integrity
                // violation); anything else — a dropped connection, a missing grant — is a real error, and
                // waiting a minute to blame a lock nobody holds would hide it
                String state = taken.getSQLState();
                if (state == null || !state.startsWith("23")) throw taken;
                // held by another instance: stale ones are stolen, live ones waited for
                try (PreparedStatement stale = connection.prepareStatement(
                    "delete from " + TABLE + " where version = ? and applied_at < ?")) {
                    stale.setString(1, LOCK_VERSION);
                    stale.setTimestamp(2, Timestamp.from(Instant.now().minusMillis(LOCK_STALE_MS)));
                    if (stale.executeUpdate() > 0) continue;
                }
                if (System.currentTimeMillis() > deadline) {
                    throw new MigrationException("Another instance has held the migration lock for over "
                        + LOCK_WAIT_MS / 1000 + "s; delete the '" + LOCK_VERSION + "' row from " + TABLE
                        + " if it crashed");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new MigrationException("Interrupted waiting for the migration lock");
                }
            }
        }
    }

    private static void releaseLock(Connection connection) {
        try (PreparedStatement delete = connection.prepareStatement("delete from " + TABLE + " where version = ?")) {
            delete.setString(1, LOCK_VERSION);
            delete.executeUpdate();
            if (!connection.getAutoCommit()) connection.commit();
        } catch (SQLException e) {
            log("Could not release the migration lock: {}", e.getMessage());
        }
    }

    private static void log(String format, Object... args) {
        com.osmig.Jweb.framework.util.Log.info(format, args);
    }
}
