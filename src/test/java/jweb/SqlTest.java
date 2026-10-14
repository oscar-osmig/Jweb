package jweb;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Sql} against an in-memory H2: queries to records, parameters by
 * position and by name, inserts with keys, transactions, and the table helper.
 */
class SqlTest {

    enum Role { ADMIN, USER }

    record User(Long id, String name, String email, Role role, boolean active,
                BigDecimal balance, LocalDate born, LocalDateTime createdAt, UUID token) {}

    record NameOnly(String name) {}

    record Primitive(long id, int count, double ratio, boolean flag) {}

    static JdbcDataSource dataSource;

    @BeforeAll
    static void connect() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:sqltest;DB_CLOSE_DELAY=-1");
        Sql.connect(dataSource);
        Sql.execute("create table users ("
            + "id bigint auto_increment primary key, name varchar(100) not null, email varchar(200), "
            + "role varchar(20), active boolean not null default true, balance decimal(10,2), "
            + "born date, created_at timestamp, token uuid)");
    }

    @AfterAll
    static void disconnect() {
        Sql.execute("drop table users");
        Sql.disconnect();
    }

    @BeforeEach
    void reset() {
        Sql.update("delete from users");
    }

    @Test
    void queriesMapRowsToRecordsByColumnName() {
        UUID token = UUID.randomUUID();
        Sql.update("insert into users (name, email, role, active, balance, born, created_at, token) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?)",
            "Ada", "ada@example.com", Role.ADMIN, true, new BigDecimal("12.50"),
            LocalDate.of(1815, 12, 10), LocalDateTime.of(2026, 1, 2, 3, 4, 5), token);

        List<User> users = Sql.query("select * from users").list(User.class);
        assertEquals(1, users.size());
        User ada = users.get(0);
        assertNotNull(ada.id());
        assertEquals("Ada", ada.name());
        assertEquals(Role.ADMIN, ada.role());
        assertTrue(ada.active());
        assertEquals(new BigDecimal("12.50"), ada.balance());
        assertEquals(LocalDate.of(1815, 12, 10), ada.born());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5), ada.createdAt());   // created_at -> createdAt
        assertEquals(token, ada.token());
    }

    @Test
    void firstOneScalarAndExists() {
        Sql.update("insert into users (name) values (?)", "Ada");
        Sql.update("insert into users (name) values (?)", "Grace");

        Optional<User> ada = Sql.query("select * from users where name = ?", "Ada").first(User.class);
        assertTrue(ada.isPresent());
        assertTrue(Sql.query("select * from users where name = ?", "Nobody").first(User.class).isEmpty());

        assertEquals("Grace", Sql.query("select * from users where name = ?", "Grace").one(User.class).name());
        Sql.SqlException several = assertThrows(Sql.SqlException.class,
            () -> Sql.query("select * from users").one(User.class));
        assertTrue(several.getMessage().contains("got 2"), several.getMessage());

        assertEquals(2L, Sql.query("select count(*) from users").scalar(Long.class));
        assertEquals(2, Sql.query("select count(*) from users").scalar(Integer.class));
        assertNull(Sql.query("select name from users where name = ?", "x").scalar(String.class));
        assertTrue(Sql.query("select 1 from users where name = ?", "Ada").exists());
        assertFalse(Sql.query("select 1 from users where name = ?", "x").exists());

        // a non-record type maps the first column
        assertEquals(List.of("Ada", "Grace"), Sql.query("select name from users order by name").list(String.class));
        assertEquals(List.of(new NameOnly("Ada"), new NameOnly("Grace")),
            Sql.query("select name, email from users order by name").list(NameOnly.class));
    }

    @Test
    void rowsHaveTypedGettersAndMappers() {
        Sql.update("insert into users (name, balance, active) values (?, ?, ?)", "Ada", new BigDecimal("3.25"), false);

        Sql.Row row = Sql.query("select id, name, balance, active, email from users").first().orElseThrow();
        assertEquals("Ada", row.getString("name"));
        assertEquals("Ada", row.getString("NAME"));
        assertEquals(3.25, row.getDouble("balance"), 0.0001);
        assertFalse(row.getBoolean("active"));
        assertTrue(row.isNull("email"));
        assertEquals(0, row.getInt("email"));
        assertFalse(row.has("missing"));
        assertNull(row.get("missing"));
        assertEquals(List.of("ID", "NAME", "BALANCE", "ACTIVE", "EMAIL"), row.columns());

        List<String> names = Sql.query("select name from users").list(r -> r.getString("name") + "!");
        assertEquals(List.of("Ada!"), names);

        Map<String, Object> map = row.toMap();
        assertEquals("Ada", map.get("NAME"));
    }

    @Test
    void namedParametersBindFromAMap() {
        Sql.update("insert into users (name, email) values (:name, :email)", Map.of("name", "Ada", "email", "a@x"));
        Sql.update("insert into users (name, email) values (:name, :email)", Map.of("name", "Bob", "email", "b@x"));

        List<User> found = Sql.query("select * from users where email = :email and name <> ':name'", Map.of("email", "b@x"))
            .list(User.class);
        assertEquals(1, found.size());
        assertEquals("Bob", found.get(0).name());

        Sql.SqlException missing = assertThrows(Sql.SqlException.class,
            () -> Sql.query("select * from users where name = :who", Map.of("name", "x")).rows());
        assertTrue(missing.getMessage().contains(":who"), missing.getMessage());

        // a PostgreSQL-style cast is not a parameter
        Sql.Named cast = Sql.Named.parse("select :id::text, 'a:b' from t -- :comment", Map.of("id", 7));
        assertEquals("select ?::text, 'a:b' from t -- :comment", cast.sql);
        assertEquals(1, cast.params.length);
    }

    @Test
    void insertReturnsTheGeneratedKey() {
        Sql.Insert first = Sql.insert("insert into users (name) values (?)", "Ada");
        Sql.Insert second = Sql.insert("insert into users (name) values (?)", "Bob");
        assertEquals(1, first.count());
        long a = first.key(Long.class);
        long b = second.key(Long.class);
        assertTrue(b > a, a + " < " + b);
        assertEquals(a, (long) first.key("id", Long.class));
    }

    @Test
    void batchRunsOneStatementPerRow() {
        int[] counts = Sql.batch("insert into users (name, email) values (?, ?)",
            List.of(new Object[]{"a", "a@x"}, new Object[]{"b", "b@x"}, new Object[]{"c", null}));
        assertEquals(3, counts.length);
        assertEquals(3L, Sql.query("select count(*) from users").scalar(Long.class));
    }

    @Test
    void transactionsCommitTogetherAndRollBackOnAnyException() {
        Sql.transaction(() -> {
            Sql.update("insert into users (name) values (?)", "one");
            assertTrue(Sql.database().inTransaction());
            Sql.transaction(() -> Sql.update("insert into users (name) values (?)", "two"));   // nested: joins
        });
        assertEquals(2L, Sql.query("select count(*) from users").scalar(Long.class));
        assertFalse(Sql.database().inTransaction());

        IllegalStateException boom = assertThrows(IllegalStateException.class, () -> Sql.transaction(() -> {
            Sql.update("insert into users (name) values (?)", "three");
            throw new IllegalStateException("boom");
        }));
        assertEquals("boom", boom.getMessage());
        assertEquals(2L, Sql.query("select count(*) from users").scalar(Long.class), "rolled back");

        String result = Sql.transaction(() -> {
            Sql.update("insert into users (name) values (?)", "four");
            return "done";
        });
        assertEquals("done", result);
        assertEquals(3L, Sql.query("select count(*) from users").scalar(Long.class));
    }

    @Test
    void tableHelperDoesTheCrudARecordNeeds() {
        Sql.Table<User> users = Sql.table("users", User.class);

        User ada = users.insert(new User(null, "Ada", "ada@x", Role.ADMIN, true, null,
            LocalDate.of(1815, 12, 10), null, null));
        assertNotNull(ada.id(), "the generated id is filled in");
        assertEquals("Ada", users.get(ada.id()).name());
        assertEquals(Role.ADMIN, users.find(ada.id()).orElseThrow().role());
        assertTrue(users.find(-1L).isEmpty());
        assertTrue(assertThrows(Sql.SqlException.class, () -> users.get(-1L)).getMessage().contains("users"));

        users.insert(new User(null, "Bob", "bob@x", Role.USER, false, null, null, null, null));
        assertEquals(2, users.count());
        assertEquals(1, users.count("active = ?", true));
        assertEquals(List.of("Bob", "Ada"), users.where("1 = 1 order by name desc").stream().map(User::name).toList());
        assertEquals("Bob", users.first("role = ?", Role.USER).orElseThrow().name());
        assertEquals(2, users.all().size());

        User renamed = new User(ada.id(), "Ada Lovelace", ada.email(), ada.role(), ada.active(),
            new BigDecimal("1.00"), ada.born(), ada.createdAt(), UUID.randomUUID());
        assertEquals(1, users.update(renamed));
        assertEquals("Ada Lovelace", users.get(ada.id()).name());
        assertEquals(renamed.token(), users.get(ada.id()).token());

        // save: null id inserts, an id updates, an unknown id inserts
        User saved = users.save(new User(null, "Cy", null, null, true, null, null, null, null));
        assertNotNull(saved.id());
        assertEquals("Cy", users.get(saved.id()).name());
        users.save(new User(saved.id(), "Cyrus", null, null, true, null, null, null, null));
        assertEquals("Cyrus", users.get(saved.id()).name());

        assertTrue(users.delete(saved.id()));
        assertFalse(users.delete(saved.id()));
        assertEquals(1, users.deleteWhere("role = ?", Role.USER));
        assertEquals(1, users.count());

        Sql.SqlException nullId = assertThrows(Sql.SqlException.class,
            () -> users.update(new User(null, "x", null, null, true, null, null, null, null)));
        assertTrue(nullId.getMessage().contains("id is null"));
    }

    @Test
    void primitivesReadZeroForNullAndSnakeCaseIsPredictable() {
        Sql.execute("create table prims (id bigint primary key, count int, ratio double, flag boolean)");
        try {
            Sql.update("insert into prims (id) values (1)");
            Primitive p = Sql.query("select * from prims").one(Primitive.class);
            assertEquals(1L, p.id());
            assertEquals(0, p.count());
            assertEquals(0d, p.ratio());
            assertFalse(p.flag());
        } finally {
            Sql.execute("drop table prims");
        }
        assertEquals("created_at", Sql.snakeCase("createdAt"));
        assertEquals("user_id", Sql.snakeCase("userID"));
        assertEquals("id", Sql.snakeCase("id"));
        assertEquals("html5_ready", Sql.snakeCase("html5Ready"));
    }

    @Test
    void conversionsCoverTheCommonJdbcShapes() {
        Instant now = Instant.parse("2026-10-13T20:00:00Z");
        assertEquals(now, Sql.Convert.to(java.sql.Timestamp.from(now), Instant.class));
        assertEquals(LocalDate.of(2026, 10, 13), Sql.Convert.to(java.sql.Date.valueOf("2026-10-13"), LocalDate.class));
        assertEquals(7, Sql.Convert.to(7L, Integer.class));
        assertEquals(7L, Sql.Convert.to(new BigDecimal("7"), Long.class));
        assertEquals(Boolean.TRUE, Sql.Convert.to(1, Boolean.class));
        assertEquals(Boolean.TRUE, Sql.Convert.to("t", Boolean.class));
        assertEquals(Role.USER, Sql.Convert.to("user", Role.class));
        assertEquals("7", Sql.Convert.to(7, String.class));
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            Sql.Convert.to("123e4567-e89b-12d3-a456-426614174000", UUID.class));
        assertEquals(0, (int) Sql.Convert.to(null, int.class));
        assertNull(Sql.Convert.to(null, Integer.class));
        Sql.SqlException bad = assertThrows(Sql.SqlException.class, () -> Sql.Convert.to("nope", Role.class));
        assertTrue(bad.getMessage().contains("Role"), bad.getMessage());
    }

    @Test
    void failuresCarryTheSqlAndTheMissingDatabaseSaysWhatToAdd() {
        Sql.SqlException bad = assertThrows(Sql.SqlException.class, () -> Sql.query("select * from nowhere").rows());
        assertEquals("select * from nowhere", bad.sql());
        assertTrue(bad.getMessage().contains("sql: select * from nowhere"), bad.getMessage());

        Sql.Database other = Sql.of(dataSource);
        assertEquals(0L, other.query("select count(*) from users").scalar(Long.class));

        Sql.disconnect();
        try {
            Sql.SqlException none = assertThrows(Sql.SqlException.class, () -> Sql.query("select 1").rows());
            assertTrue(none.getMessage().contains("spring-boot-starter-jdbc"), none.getMessage());
            assertFalse(Sql.isConnected());
        } finally {
            Sql.connect(dataSource);
        }
    }
}
