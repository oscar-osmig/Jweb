package jweb;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SQL over any JDBC {@link DataSource}: queries that map rows to records,
 * updates, inserts that hand back the generated key, transactions, and a
 * table helper for the CRUD a record needs. No ORM and no query language of
 * its own — the SQL is yours; the mapping, the parameter binding and the
 * connection handling are the framework's.
 *
 * <pre>
 * record User(Long id, String name, String email, LocalDateTime createdAt) {}
 *
 * List&lt;User&gt; users = Sql.query("select * from users where email like ?", "%@example.com").list(User.class);
 * Optional&lt;User&gt; one = Sql.query("select * from users where id = ?", 7).first(User.class);
 * long n = Sql.query("select count(*) from users").scalar(Long.class);
 * int changed = Sql.update("update users set name = ? where id = ?", "Ada", 7);
 * long id = Sql.insert("insert into users(name, email) values(?, ?)", "Ada", "ada@example.com").key(Long.class);
 *
 * Sql.transaction(() -&gt; {
 *     Sql.update("update accounts set balance = balance - ? where id = ?", 50, from);
 *     Sql.update("update accounts set balance = balance + ? where id = ?", 50, to);
 * });
 *
 * Sql.Table&lt;User&gt; users = Sql.table("users", User.class);
 * User saved = users.insert(new User(null, "Ada", "ada@example.com", null));   // id filled in
 * users.find(saved.id());
 * users.where("email like ? order by name", "%@example.com");
 * users.update(saved);
 * users.delete(saved.id());
 * </pre>
 *
 * <p><b>Connecting.</b> The application's {@code DataSource} bean is bound at
 * startup: add {@code spring-boot-starter-jdbc} and a driver and set
 * {@code spring.datasource.url}, and every static method here works. Pending
 * migrations under {@code db/migration} run at the same time — see
 * {@link Migrations}. {@link #connect(DataSource)} binds one by hand (a test,
 * a plain {@code main}); {@link #of(DataSource)} gives a second database its
 * own {@link Database} handle with the same methods.</p>
 *
 * <p><b>Records.</b> A row maps to a record by column name: the component
 * {@code createdAt} reads the column {@code created_at} — or {@code createdAt},
 * or {@code CREATEDAT}; case and underscores are ignored. A column the row
 * does not have leaves the component {@code null} (zero or {@code false} for
 * a primitive). The types that convert: the numbers, {@code String},
 * {@code boolean}, {@code BigDecimal}, {@code LocalDate}, {@code LocalDateTime},
 * {@code LocalTime}, {@code Instant}, {@code UUID}, enums by name,
 * {@code byte[]}. A non-record class ({@code String.class}, {@code Long.class}…)
 * maps the first column.</p>
 *
 * <p><b>Parameters.</b> Positional {@code ?} with varargs, or named
 * {@code :name} with a map ({@code ::} is left alone, so a PostgreSQL cast
 * still works). A value binds by its Java type: an {@code Instant} becomes a
 * timestamp, an enum its name, a {@code UUID} the driver's UUID type or its
 * text.</p>
 *
 * <p><b>Transactions.</b> {@link #transaction(Runnable)} runs the block on one
 * connection with auto-commit off, commits at the end and rolls back on any
 * exception. Every {@code Sql} call on that thread inside the block joins it;
 * a nested {@code transaction} joins the outer one.</p>
 *
 * <p>Errors are {@link SqlException}, unchecked, carrying the SQL that failed.</p>
 */
public final class Sql {

    private static volatile Database defaultDatabase;

    private Sql() {}

    // ==================== connecting ====================

    /** Binds the database the static methods use. Called for the {@code DataSource} bean at startup. */
    public static void connect(DataSource dataSource) {
        defaultDatabase = new Database(dataSource);
    }

    /** A handle on another database, with every method {@code Sql} has as instance methods. */
    public static Database of(DataSource dataSource) {
        return new Database(dataSource);
    }

    /** Whether {@link #connect(DataSource)} has been called (by the app or at startup). */
    public static boolean isConnected() {
        return defaultDatabase != null;
    }

    /** Forgets the bound database. The {@code DataSource} itself is not closed. */
    public static void disconnect() {
        defaultDatabase = null;
    }

    /** The bound database; fails with the fix when there is none. */
    public static Database database() {
        Database db = defaultDatabase;
        if (db == null) {
            throw new SqlException("No database is connected. Add spring-boot-starter-jdbc and a JDBC driver "
                + "and set spring.datasource.url, or call Sql.connect(dataSource) yourself.", null);
        }
        return db;
    }

    /** The bound {@code DataSource}. */
    public static DataSource dataSource() {
        return database().dataSource();
    }

    // ==================== the static facade ====================

    /** A query with positional {@code ?} parameters. Nothing runs until a terminal call. */
    public static Query query(String sql, Object... params) {
        return database().query(sql, params);
    }

    /** A query with {@code :name} parameters. */
    public static Query query(String sql, Map<String, ?> params) {
        return database().query(sql, params);
    }

    /** Runs an update, insert or delete; the number of rows affected. */
    public static int update(String sql, Object... params) {
        return database().update(sql, params);
    }

    /** Runs an update with {@code :name} parameters. */
    public static int update(String sql, Map<String, ?> params) {
        return database().update(sql, params);
    }

    /** Runs an insert and returns the generated key with the row count. */
    public static Insert insert(String sql, Object... params) {
        return database().insert(sql, params);
    }

    /** Runs an insert with {@code :name} parameters. */
    public static Insert insert(String sql, Map<String, ?> params) {
        return database().insert(sql, params);
    }

    /** Runs a statement for its effect — DDL, mostly. */
    public static void execute(String sql) {
        database().execute(sql);
    }

    /** Runs one statement once per row of parameters, as a JDBC batch; the per-row counts. */
    public static int[] batch(String sql, List<Object[]> rows) {
        return database().batch(sql, rows);
    }

    /** Runs the block in one transaction: commit at the end, rollback on any exception. */
    public static void transaction(Runnable block) {
        database().transaction(block);
    }

    /** Runs the block in one transaction and returns its result. */
    public static <T> T transaction(Supplier<T> block) {
        return database().transaction(block);
    }

    /** CRUD on one table through a record: see {@link Table}. */
    public static <T> Table<T> table(String name, Class<T> type) {
        return database().table(name, type);
    }

    // ==================== Database ====================

    /**
     * One database. {@code Sql}'s static methods are these, on the bound
     * default; {@link Sql#of(DataSource)} makes one for any other.
     */
    public static final class Database {

        private final DataSource dataSource;
        private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

        Database(DataSource dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        }

        public DataSource dataSource() {
            return dataSource;
        }

        public Query query(String sql, Object... params) {
            return new Query(this, sql, params == null ? new Object[]{null} : params);
        }

        public Query query(String sql, Map<String, ?> params) {
            Named named = Named.parse(sql, params);
            return new Query(this, named.sql, named.params);
        }

        public int update(String sql, Object... params) {
            Object[] bound = params == null ? new Object[]{null} : params;
            return withConnection(sql, connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bind(statement, bound);
                    return statement.executeUpdate();
                }
            });
        }

        public int update(String sql, Map<String, ?> params) {
            Named named = Named.parse(sql, params);
            return update(named.sql, named.params);
        }

        public Insert insert(String sql, Object... params) {
            Object[] bound = params == null ? new Object[]{null} : params;
            return withConnection(sql, connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    bind(statement, bound);
                    int count = statement.executeUpdate();
                    Row keys = null;
                    try (ResultSet generated = statement.getGeneratedKeys()) {
                        if (generated != null && generated.next()) {
                            keys = Row.read(generated);
                        }
                    } catch (SQLException ignored) {
                        // a driver that cannot return keys for this statement: the insert still happened
                    }
                    return new Insert(count, keys);
                }
            });
        }

        public Insert insert(String sql, Map<String, ?> params) {
            Named named = Named.parse(sql, params);
            return insert(named.sql, named.params);
        }

        public void execute(String sql) {
            withConnection(sql, connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                    return null;
                }
            });
        }

        public int[] batch(String sql, List<Object[]> rows) {
            return withConnection(sql, connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Object[] row : rows) {
                        bind(statement, row);
                        statement.addBatch();
                    }
                    return statement.executeBatch();
                }
            });
        }

        public void transaction(Runnable block) {
            transaction(() -> {
                block.run();
                return null;
            });
        }

        public <T> T transaction(Supplier<T> block) {
            if (transactionConnection.get() != null) {
                return block.get();   // nested: join the outer transaction
            }
            Connection connection;
            try {
                connection = dataSource.getConnection();
            } catch (SQLException e) {
                throw new SqlException("Could not open a connection: " + e.getMessage(), null, e);
            }
            boolean autoCommit = true;
            try {
                autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                transactionConnection.set(connection);
                T result = block.get();
                connection.commit();
                return result;
            } catch (SQLException e) {
                rollback(connection, e);
                throw new SqlException("Transaction failed: " + e.getMessage(), null, e);
            } catch (RuntimeException | Error e) {
                rollback(connection, e);
                throw e;
            } finally {
                transactionConnection.remove();
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {
                    // the connection goes back to the pool either way
                }
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // nothing left to do with it
                }
            }
        }

        private static void rollback(Connection connection, Throwable cause) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                cause.addSuppressed(e);
            }
        }

        public <T> Table<T> table(String name, Class<T> type) {
            return new Table<>(this, name, type);
        }

        /** Whether a transaction is open on this thread. */
        public boolean inTransaction() {
            return transactionConnection.get() != null;
        }

        <T> T withConnection(String sql, ConnectionCall<T> call) {
            Connection open = transactionConnection.get();
            if (open != null) {
                try {
                    return call.apply(open);
                } catch (SQLException e) {
                    throw new SqlException(e.getMessage(), sql, e);
                }
            }
            try (Connection connection = dataSource.getConnection()) {
                return call.apply(connection);
            } catch (SQLException e) {
                throw new SqlException(e.getMessage(), sql, e);
            }
        }

        @FunctionalInterface
        interface ConnectionCall<T> {
            T apply(Connection connection) throws SQLException;
        }
    }

    // ==================== Query ====================

    /** A prepared query; each terminal method runs it. */
    public static final class Query {

        private final Database db;
        private final String sql;
        private final Object[] params;

        Query(Database db, String sql, Object[] params) {
            this.db = db;
            this.sql = sql;
            this.params = params;
        }

        /** Every row, in order. */
        public List<Row> rows() {
            List<Row> rows = new ArrayList<>();
            forEach(rows::add);
            return rows;
        }

        /** Every row mapped to the record (or scalar) type. */
        public <T> List<T> list(Class<T> type) {
            return list(row -> row.as(type));
        }

        /** Every row through the mapper. */
        public <T> List<T> list(Function<Row, T> mapper) {
            List<T> out = new ArrayList<>();
            forEach(row -> out.add(mapper.apply(row)));
            return out;
        }

        /** The first row, if any. */
        public Optional<Row> first() {
            return first(Function.identity());
        }

        /** The first row as the type, if any. */
        public <T> Optional<T> first(Class<T> type) {
            return first(row -> row.as(type));
        }

        /** The first row through the mapper, if any. */
        public <T> Optional<T> first(Function<Row, T> mapper) {
            return db.withConnection(sql, connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setMaxRows(1);
                    bind(statement, params);
                    try (ResultSet rs = statement.executeQuery()) {
                        return rs.next() ? Optional.ofNullable(mapper.apply(Row.read(rs))) : Optional.empty();
                    }
                }
            });
        }

        /** Exactly one row as the type; fails when there are none or several. */
        public <T> T one(Class<T> type) {
            List<T> rows = list(type);
            if (rows.size() != 1) {
                throw new SqlException("Expected one row, got " + rows.size(), sql, null);
            }
            return rows.get(0);
        }

        /** The first column of the first row, converted; {@code null} when there is no row. */
        public <T> T scalar(Class<T> type) {
            return first(row -> row.get(row.columns().get(0), type)).orElse(null);
        }

        /** Whether the query returns at least one row. */
        public boolean exists() {
            return first().isPresent();
        }

        /** Streams the rows to the consumer without holding them all. */
        public void forEach(Consumer<Row> consumer) {
            db.withConnection(sql, connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bind(statement, params);
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            consumer.accept(Row.read(rs));
                        }
                    }
                }
                return null;
            });
        }

        public String sql() {
            return sql;
        }
    }

    // ==================== Row ====================

    /** One result row: the columns by name, with typed getters and {@link #as(Class)}. */
    public static final class Row {

        private final LinkedHashMap<String, Object> values;
        private final Map<String, String> byNormalizedName;

        private Row(LinkedHashMap<String, Object> values) {
            this.values = values;
            this.byNormalizedName = new LinkedHashMap<>();
            for (String column : values.keySet()) {
                byNormalizedName.putIfAbsent(normalize(column), column);
            }
        }

        static Row read(ResultSet rs) throws SQLException {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            LinkedHashMap<String, Object> values = new LinkedHashMap<>();
            for (int i = 1; i <= count; i++) {
                String label = meta.getColumnLabel(i);
                if (label == null || label.isEmpty()) label = meta.getColumnName(i);
                Object value = readValue(rs, i, meta.getColumnType(i));
                if (value instanceof Blob blob) {
                    value = blob.getBytes(1, (int) blob.length());
                } else if (value instanceof Clob clob) {
                    value = clob.getSubString(1, (int) clob.length());
                }
                values.put(label, value);
            }
            return new Row(values);
        }

        /**
         * Temporal columns are read as {@code java.time} values (JDBC 4.2), never
         * through {@code java.sql.Date} and friends, whose conversions go through
         * the JVM's time zone and shift a date before 1883 by a day.
         */
        private static Object readValue(ResultSet rs, int index, int sqlType) throws SQLException {
            Class<?> temporal = switch (sqlType) {
                case java.sql.Types.DATE -> LocalDate.class;
                case java.sql.Types.TIME -> LocalTime.class;
                case java.sql.Types.TIMESTAMP -> LocalDateTime.class;
                case java.sql.Types.TIMESTAMP_WITH_TIMEZONE -> OffsetDateTime.class;
                default -> null;
            };
            if (temporal != null) {
                try {
                    return rs.getObject(index, temporal);
                } catch (SQLException | AbstractMethodError olderDriver) {
                    // fall through to the driver's own type; Convert handles it
                }
            }
            return rs.getObject(index);
        }

        /** The column labels, in result order. */
        public List<String> columns() {
            return new ArrayList<>(values.keySet());
        }

        public boolean has(String column) {
            return byNormalizedName.containsKey(normalize(column));
        }

        public boolean isNull(String column) {
            return get(column) == null;
        }

        /** The raw JDBC value, or {@code null} for a missing column. */
        public Object get(String column) {
            String actual = byNormalizedName.get(normalize(column));
            return actual == null ? null : values.get(actual);
        }

        /** The value converted to the type; {@code null} stays {@code null}. */
        public <T> T get(String column, Class<T> type) {
            return Convert.to(get(column), type);
        }

        public String getString(String column) {
            return get(column, String.class);
        }

        /** The int value, {@code 0} when null. */
        public int getInt(String column) {
            Integer v = get(column, Integer.class);
            return v == null ? 0 : v;
        }

        /** The long value, {@code 0} when null. */
        public long getLong(String column) {
            Long v = get(column, Long.class);
            return v == null ? 0L : v;
        }

        /** The double value, {@code 0} when null. */
        public double getDouble(String column) {
            Double v = get(column, Double.class);
            return v == null ? 0d : v;
        }

        /** The boolean value, {@code false} when null. */
        public boolean getBoolean(String column) {
            Boolean v = get(column, Boolean.class);
            return v != null && v;
        }

        public BigDecimal getBigDecimal(String column) {
            return get(column, BigDecimal.class);
        }

        public LocalDate getLocalDate(String column) {
            return get(column, LocalDate.class);
        }

        public LocalDateTime getLocalDateTime(String column) {
            return get(column, LocalDateTime.class);
        }

        public Instant getInstant(String column) {
            return get(column, Instant.class);
        }

        public UUID getUuid(String column) {
            return get(column, UUID.class);
        }

        /** The row as a record (by column name) or as a scalar (the first column). */
        public <T> T as(Class<T> type) {
            if (type == Row.class) {
                return type.cast(this);
            }
            if (type.isRecord()) {
                return Records.of(type).from(this);
            }
            if (Map.class.isAssignableFrom(type)) {
                return type.cast(toMap());
            }
            if (values.isEmpty()) {
                return null;
            }
            return Convert.to(values.values().iterator().next(), type);
        }

        /** The columns and raw values, in result order. */
        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(values);
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }

    // ==================== Insert ====================

    /** What an insert returned: the row count and the generated key, if the driver gave one. */
    public static final class Insert {

        private final int count;
        private final Row keys;

        Insert(int count, Row keys) {
            this.count = count;
            this.keys = keys;
        }

        /** Rows inserted. */
        public int count() {
            return count;
        }

        /** The generated key (the first generated column), or {@code null}. */
        public Object key() {
            if (keys == null || keys.columns().isEmpty()) return null;
            return keys.get(keys.columns().get(0));
        }

        /** The generated key converted to the type. */
        public <K> K key(Class<K> type) {
            return Convert.to(key(), type);
        }

        /** The generated key for a named column when the driver returns several, else the first. */
        public <K> K key(String column, Class<K> type) {
            if (keys != null && keys.has(column)) {
                return keys.get(column, type);
            }
            return key(type);
        }

        /** Every generated column the driver returned, or {@code null}. */
        public Row keys() {
            return keys;
        }
    }

    // ==================== Table ====================

    /**
     * CRUD on one table through a record type. Columns are the record's
     * components in {@code snake_case}; the component named {@code id} is the
     * key. {@link #insert(Object)} leaves a {@code null} id to the database and
     * returns the record with the generated one filled in.
     *
     * <pre>
     * record Todo(Long id, String title, boolean done) {}
     * Sql.Table&lt;Todo&gt; todos = Sql.table("todos", Todo.class);
     *
     * Todo t = todos.insert(new Todo(null, "Write docs", false));
     * todos.update(new Todo(t.id(), t.title(), true));
     * todos.where("done = ? order by id", false);
     * todos.delete(t.id());
     * </pre>
     *
     * <p>{@link #where(String, Object...)} takes the rest of the statement after
     * {@code where}, so an {@code order by} or a {@code limit} goes in the same
     * string.</p>
     */
    public static final class Table<T> {

        private final Database db;
        private final String name;
        private final Records<T> records;
        private final String idColumn;
        private final int idIndex;
        private final String selectAll;

        Table(Database db, String name, Class<T> type) {
            if (!type.isRecord()) {
                throw new SqlException("Sql.table needs a record type; " + type.getName() + " is not one", null);
            }
            this.db = db;
            this.name = name;
            this.records = Records.of(type);
            int id = -1;
            for (int i = 0; i < records.components.length; i++) {
                if (records.components[i].getName().equals("id")) {
                    id = i;
                    break;
                }
            }
            this.idIndex = id;
            this.idColumn = id < 0 ? null : records.columns[id];
            this.selectAll = "select * from " + name;
        }

        public String name() {
            return name;
        }

        /** Inserts the record; the returned copy carries the generated id when the record's was null. */
        public T insert(T record) {
            Object[] values = records.values(record);
            boolean generateId = idIndex >= 0 && values[idIndex] == null;
            List<String> columns = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                if (i == idIndex && generateId) continue;
                columns.add(records.columns[i]);
                params.add(values[i]);
            }
            String sql = "insert into " + name + " (" + String.join(", ", columns) + ") values ("
                + String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
            Insert result = db.insert(sql, params.toArray());
            if (generateId) {
                Class<?> idType = records.components[idIndex].getType();
                Object key = result.key(idColumn, idType);
                if (key != null) {
                    values[idIndex] = key;
                    return records.construct(values);
                }
            }
            return record;
        }

        /** The row with this id, if any. */
        public Optional<T> find(Object id) {
            return db.query(selectAll + " where " + requireId() + " = ?", id).first(records.type);
        }

        /** The row with this id; fails when there is none. */
        public T get(Object id) {
            return find(id).orElseThrow(() -> new SqlException("No row in " + name + " with " + idColumn + " = " + id, null));
        }

        /** Every row. */
        public List<T> all() {
            return db.query(selectAll).list(records.type);
        }

        /** The rows matching the clause — everything after {@code where}, ordering included. */
        public List<T> where(String clause, Object... params) {
            return db.query(selectAll + " where " + clause, params).list(records.type);
        }

        /** The first row matching the clause. */
        public Optional<T> first(String clause, Object... params) {
            return db.query(selectAll + " where " + clause, params).first(records.type);
        }

        public long count() {
            Long n = db.query("select count(*) from " + name).scalar(Long.class);
            return n == null ? 0 : n;
        }

        public long count(String clause, Object... params) {
            Long n = db.query("select count(*) from " + name + " where " + clause, params).scalar(Long.class);
            return n == null ? 0 : n;
        }

        /** Writes every column of the record to the row with its id; the rows changed (0 when absent). */
        public int update(T record) {
            requireId();
            Object[] values = records.values(record);
            if (values[idIndex] == null) {
                throw new SqlException("Cannot update " + name + ": the record's id is null", null);
            }
            List<String> sets = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                if (i == idIndex) continue;
                sets.add(records.columns[i] + " = ?");
                params.add(values[i]);
            }
            params.add(values[idIndex]);
            return db.update("update " + name + " set " + String.join(", ", sets) + " where " + idColumn + " = ?",
                params.toArray());
        }

        /** Inserts when the id is null, updates otherwise; the record as stored. */
        public T save(T record) {
            Object[] values = records.values(record);
            if (idIndex < 0 || values[idIndex] == null) {
                return insert(record);
            }
            if (update(record) == 0) {
                return insert(record);
            }
            return record;
        }

        /** Deletes the row with this id; whether there was one. */
        public boolean delete(Object id) {
            return db.update("delete from " + name + " where " + requireId() + " = ?", id) > 0;
        }

        /** Deletes the rows matching the clause; how many. */
        public int deleteWhere(String clause, Object... params) {
            return db.update("delete from " + name + " where " + clause, params);
        }

        private String requireId() {
            if (idColumn == null) {
                throw new SqlException("Record " + records.type.getSimpleName() + " has no 'id' component, so "
                    + name + " cannot be addressed by id", null);
            }
            return idColumn;
        }
    }

    // ==================== SqlException ====================

    /** A failed statement: the driver's message, with the SQL. */
    public static class SqlException extends RuntimeException {

        private final String sql;

        public SqlException(String message, String sql) {
            this(message, sql, null);
        }

        public SqlException(String message, String sql, Throwable cause) {
            super(sql == null ? message : message + "\n  sql: " + sql, cause);
            this.sql = sql;
        }

        /** The statement that failed, or {@code null}. */
        public String sql() {
            return sql;
        }
    }

    // ==================== binding ====================

    static void bind(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            bindOne(statement, i + 1, params[i]);
        }
    }

    private static void bindOne(PreparedStatement statement, int index, Object value) throws SQLException {
        switch (value) {
            case null -> statement.setObject(index, null);
            case Instant instant -> statement.setTimestamp(index, Timestamp.from(instant));
            case ZonedDateTime zoned -> statement.setTimestamp(index, Timestamp.from(zoned.toInstant()));
            case Enum<?> e -> statement.setString(index, e.name());
            case UUID uuid -> {
                try {
                    statement.setObject(index, uuid);
                } catch (SQLException noUuidType) {
                    statement.setString(index, uuid.toString());
                }
            }
            case Optional<?> optional -> bindOne(statement, index, optional.orElse(null));
            default -> statement.setObject(index, value);
        }
    }

    // ==================== named parameters ====================

    /** {@code :name} parameters rewritten to {@code ?} with the values in order. */
    static final class Named {

        final String sql;
        final Object[] params;

        private Named(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }

        static Named parse(String sql, Map<String, ?> values) {
            StringBuilder out = new StringBuilder(sql.length());
            List<Object> params = new ArrayList<>();
            int n = sql.length();
            int i = 0;
            while (i < n) {
                char c = sql.charAt(i);
                if (c == '\'' || c == '"') {
                    int end = sql.indexOf(c, i + 1);
                    while (end >= 0 && end + 1 < n && sql.charAt(end + 1) == c) {
                        end = sql.indexOf(c, end + 2);   // a doubled quote inside the literal
                    }
                    end = end < 0 ? n - 1 : end;
                    out.append(sql, i, end + 1);
                    i = end + 1;
                } else if (c == '-' && i + 1 < n && sql.charAt(i + 1) == '-') {
                    int end = sql.indexOf('\n', i);
                    end = end < 0 ? n : end;
                    out.append(sql, i, end);
                    i = end;
                } else if (c == '/' && i + 1 < n && sql.charAt(i + 1) == '*') {
                    int end = sql.indexOf("*/", i + 2);
                    end = end < 0 ? n : end + 2;
                    out.append(sql, i, end);
                    i = end;
                } else if (c == ':' && i + 1 < n && sql.charAt(i + 1) == ':') {
                    out.append("::");
                    i += 2;
                } else if (c == ':' && i + 1 < n && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                    int end = i + 1;
                    while (end < n && Character.isJavaIdentifierPart(sql.charAt(end))) end++;
                    String name = sql.substring(i + 1, end);
                    if (!values.containsKey(name)) {
                        throw new SqlException("No value for parameter :" + name, sql);
                    }
                    params.add(values.get(name));
                    out.append('?');
                    i = end;
                } else {
                    out.append(c);
                    i++;
                }
            }
            return new Named(out.toString(), params.toArray());
        }
    }

    // ==================== records ====================

    /** A record type's components, columns and canonical constructor, cached. */
    static final class Records<T> {

        private static final Map<Class<?>, Records<?>> CACHE = new ConcurrentHashMap<>();

        final Class<T> type;
        final RecordComponent[] components;
        final String[] columns;
        private final Constructor<T> constructor;

        private Records(Class<T> type) {
            this.type = type;
            this.components = type.getRecordComponents();
            this.columns = new String[components.length];
            Class<?>[] parameterTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                columns[i] = snakeCase(components[i].getName());
                parameterTypes[i] = components[i].getType();
            }
            try {
                constructor = type.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new SqlException("Record " + type.getName() + " has no canonical constructor", null, e);
            }
        }

        @SuppressWarnings("unchecked")
        static <T> Records<T> of(Class<T> type) {
            return (Records<T>) CACHE.computeIfAbsent(type, Records::new);
        }

        T from(Row row) {
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                args[i] = row.get(columns[i], components[i].getType());
            }
            return construct(args);
        }

        T construct(Object[] args) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null && components[i].getType().isPrimitive()) {
                    args[i] = Convert.primitiveDefault(components[i].getType());
                }
            }
            try {
                return constructor.newInstance(args);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw new SqlException("Could not construct " + type.getSimpleName() + ": " + cause.getMessage(), null, cause);
            }
        }

        Object[] values(T record) {
            Object[] values = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                try {
                    values[i] = components[i].getAccessor().invoke(record);
                } catch (ReflectiveOperationException e) {
                    throw new SqlException("Could not read " + components[i].getName() + " of " + type.getSimpleName(), null, e);
                }
            }
            return values;
        }
    }

    /** {@code createdAt} → {@code created_at}; {@code userID} → {@code user_id}. */
    static String snakeCase(String name) {
        StringBuilder out = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && (Character.isLowerCase(name.charAt(i - 1)) || Character.isDigit(name.charAt(i - 1)))) {
                    out.append('_');
                }
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** Lower case, underscores dropped: how a column and a component are matched. */
    static String normalize(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c != '_') out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    // ==================== conversion ====================

    /** JDBC values to the Java types a record declares. */
    static final class Convert {

        private Convert() {}

        static Object primitiveDefault(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            return null;
        }

        @SuppressWarnings("unchecked")
        static <T> T to(Object value, Class<T> type) {
            if (value == null) {
                return type.isPrimitive() ? (T) primitiveDefault(type) : null;
            }
            Class<?> target = wrap(type);
            if (target.isInstance(value)) {
                return (T) value;
            }
            Object converted = convert(value, target);
            if (converted == null) {
                throw new SqlException("Cannot convert " + value.getClass().getSimpleName() + " '" + value
                    + "' to " + type.getSimpleName(), null);
            }
            return (T) converted;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Object convert(Object value, Class<?> target) {
            if (target == String.class) {
                return value instanceof byte[] bytes ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : value.toString();
            }
            if (Number.class.isAssignableFrom(target)) {
                Number number = switch (value) {
                    case Number n -> n;
                    case Boolean b -> b ? 1 : 0;
                    case String s -> new BigDecimal(s.trim());
                    default -> null;
                };
                return number == null ? null : toNumber(number, target);
            }
            if (target == Boolean.class) {
                return switch (value) {
                    case Number n -> n.intValue() != 0;
                    case String s -> s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("t")
                        || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes");
                    default -> null;
                };
            }
            if (target == Character.class) {
                String s = value.toString();
                return s.isEmpty() ? null : s.charAt(0);
            }
            if (target == LocalDateTime.class) {
                return switch (value) {
                    case Timestamp t -> t.toLocalDateTime();
                    case java.sql.Date d -> d.toLocalDate().atStartOfDay();
                    case Instant i -> LocalDateTime.ofInstant(i, ZoneId.systemDefault());
                    case OffsetDateTime o -> o.toLocalDateTime();
                    case ZonedDateTime z -> z.toLocalDateTime();
                    case LocalDate d -> d.atStartOfDay();
                    case String s -> LocalDateTime.parse(s.trim().replace(' ', 'T'));
                    default -> null;
                };
            }
            if (target == LocalDate.class) {
                return switch (value) {
                    case java.sql.Date d -> d.toLocalDate();
                    case Timestamp t -> t.toLocalDateTime().toLocalDate();
                    case LocalDateTime ldt -> ldt.toLocalDate();
                    case Instant i -> LocalDate.ofInstant(i, ZoneId.systemDefault());
                    case OffsetDateTime o -> o.toLocalDate();
                    case String s -> LocalDate.parse(s.trim());
                    default -> null;
                };
            }
            if (target == LocalTime.class) {
                return switch (value) {
                    case java.sql.Time t -> t.toLocalTime();
                    case Timestamp t -> t.toLocalDateTime().toLocalTime();
                    case LocalDateTime ldt -> ldt.toLocalTime();
                    case String s -> LocalTime.parse(s.trim());
                    default -> null;
                };
            }
            if (target == Instant.class) {
                return switch (value) {
                    case Timestamp t -> t.toInstant();
                    case java.sql.Date d -> d.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
                    case LocalDateTime ldt -> ldt.atZone(ZoneId.systemDefault()).toInstant();
                    case OffsetDateTime o -> o.toInstant();
                    case ZonedDateTime z -> z.toInstant();
                    case Number n -> Instant.ofEpochMilli(n.longValue());
                    case String s -> Instant.parse(s.trim());
                    default -> null;
                };
            }
            if (target == OffsetDateTime.class) {
                return switch (value) {
                    case Timestamp t -> t.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
                    case Instant i -> i.atZone(ZoneId.systemDefault()).toOffsetDateTime();
                    case String s -> OffsetDateTime.parse(s.trim());
                    default -> null;
                };
            }
            if (target == UUID.class) {
                return switch (value) {
                    case String s -> UUID.fromString(s.trim());
                    case byte[] b when b.length == 16 -> {
                        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(b);
                        yield new UUID(buffer.getLong(), buffer.getLong());
                    }
                    default -> null;
                };
            }
            if (target.isEnum()) {
                String name = value.toString().trim();
                for (Object constant : target.getEnumConstants()) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(name)) return constant;
                }
                throw new SqlException("No " + target.getSimpleName() + " named '" + name + "'", null);
            }
            if (target == byte[].class) {
                return value instanceof String s ? s.getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;
            }
            if (target == Object.class) {
                return value;
            }
            if (Map.class.isAssignableFrom(target) && value instanceof Map<?, ?> m) {
                return new LinkedHashMap(m);
            }
            return null;
        }

        private static Object toNumber(Number n, Class<?> target) {
            if (target == Integer.class) return n.intValue();
            if (target == Long.class) return n.longValue();
            if (target == Double.class) return n.doubleValue();
            if (target == Float.class) return n.floatValue();
            if (target == Short.class) return n.shortValue();
            if (target == Byte.class) return n.byteValue();
            if (target == BigDecimal.class) {
                return n instanceof BigDecimal bd ? bd
                    : n instanceof BigInteger bi ? new BigDecimal(bi)
                    : n instanceof Double || n instanceof Float ? BigDecimal.valueOf(n.doubleValue())
                    : BigDecimal.valueOf(n.longValue());
            }
            if (target == BigInteger.class) {
                return n instanceof BigInteger bi ? bi
                    : n instanceof BigDecimal bd ? bd.toBigInteger()
                    : BigInteger.valueOf(n.longValue());
            }
            if (target == Number.class) return n;
            return null;
        }

        private static Class<?> wrap(Class<?> type) {
            if (!type.isPrimitive()) return type;
            if (type == int.class) return Integer.class;
            if (type == long.class) return Long.class;
            if (type == boolean.class) return Boolean.class;
            if (type == double.class) return Double.class;
            if (type == float.class) return Float.class;
            if (type == short.class) return Short.class;
            if (type == byte.class) return Byte.class;
            if (type == char.class) return Character.class;
            return type;
        }
    }
}
