package jweb;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Migrations}: the files under a location run once each in version
 * order, are remembered, and an edited one is refused.
 */
class MigrationsTest {

    private static JdbcDataSource freshDatabase(String name) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        return ds;
    }

    @Test
    void appliesPendingMigrationsOnceInOrder() {
        JdbcDataSource ds = freshDatabase("migrations1");
        Sql.Database db = Sql.of(ds);

        List<Migrations.Migration> pending = Migrations.pending(ds, "classpath:db/test-migration");
        assertEquals(List.of("1", "2"), pending.stream().map(Migrations.Migration::version).toList());
        assertEquals("create notes", pending.get(0).name());

        Migrations.Report first = Migrations.migrate(ds, "classpath:db/test-migration");
        assertEquals(2, first.count());
        assertEquals(0, first.alreadyApplied());

        // V1 seeded a row whose text contains a semicolon; V2 added the column and index
        assertEquals("seeded by V1; has a ; in a string",
            db.query("select body from notes where title = ?", "first").scalar(String.class));
        db.update("update notes set tag = ? where title = ?", "x", "first");

        Migrations.Report second = Migrations.migrate(ds, "classpath:db/test-migration");
        assertEquals(0, second.count());
        assertEquals(2, second.alreadyApplied());
        assertTrue(Migrations.pending(ds, "classpath:db/test-migration").isEmpty());

        List<Migrations.Applied> applied = Migrations.applied(ds);
        assertEquals(List.of("1", "2"), applied.stream().map(Migrations.Applied::version).toList());
        assertTrue(applied.get(0).appliedAt() != null);

        // the lock row never shows in the history
        assertEquals(2L, db.query("select count(*) from " + Migrations.TABLE).scalar(Long.class));
    }

    @Test
    void anEditedMigrationIsRefusedUntilRepaired() {
        JdbcDataSource ds = freshDatabase("migrations2");
        Migrations.migrate(ds, "classpath:db/test-migration");

        Migrations.MigrationException edited = assertThrows(Migrations.MigrationException.class,
            () -> Migrations.migrate(ds, "classpath:db/edited-migration"));
        assertTrue(edited.getMessage().contains("V1__create_notes.sql"), edited.getMessage());
        assertTrue(edited.getMessage().contains("repair"), edited.getMessage());

        assertEquals(1, Migrations.repair(ds, "classpath:db/edited-migration"));
        assertEquals(0, Migrations.migrate(ds, "classpath:db/edited-migration").count());
        assertEquals(0, Migrations.repair(ds, "classpath:db/edited-migration"));
    }

    @Test
    void aFailingMigrationNamesTheFileAndLeavesNoHistoryRow() {
        JdbcDataSource ds = freshDatabase("migrations3");
        Sql.Database db = Sql.of(ds);
        // V1 of the edited set creates notes; the test set's V1 then fails on the same table
        Migrations.migrate(ds, "classpath:db/edited-migration");
        db.update("delete from " + Migrations.TABLE);

        Migrations.MigrationException failed = assertThrows(Migrations.MigrationException.class,
            () -> Migrations.migrate(ds, "classpath:db/test-migration"));
        assertTrue(failed.getMessage().contains("V1__create_notes.sql"), failed.getMessage());
        assertTrue(failed.getMessage().contains("statement:"), failed.getMessage());
        assertTrue(Migrations.applied(ds).isEmpty(), "nothing recorded for the failed one");
        assertEquals(0L, db.query("select count(*) from " + Migrations.TABLE).scalar(Long.class), "lock released");
    }

    @Test
    void anEmptyLocationIsNotAnError() {
        JdbcDataSource ds = freshDatabase("migrations4");
        Migrations.Report report = Migrations.migrate(ds, "classpath:db/nowhere");
        assertEquals(0, report.count());
        assertTrue(Migrations.applied(ds).isEmpty());
    }

    @Test
    void statementsSplitOnSemicolonsOutsideQuotesCommentsAndDollarBodies() {
        List<String> parts = Migrations.statements("""
            -- a comment; not a statement
            create table t (a varchar(10)); /* block; comment */
            insert into t values ('x;y');
            insert into t values ('it''s; fine');
            create function f() returns int as $$ begin return 1; end $$ language plpgsql;
            select 1
            """);
        assertEquals(List.of(
            "create table t (a varchar(10))",
            "insert into t values ('x;y')",
            "insert into t values ('it''s; fine')",
            "create function f() returns int as $$ begin return 1; end $$ language plpgsql",
            "select 1"), parts);

        assertEquals(List.of("-- jweb:whole\ncreate a; thing b;"),
            Migrations.statements("-- jweb:whole\ncreate a; thing b;\n"));
        assertEquals(List.of(), Migrations.statements("-- nothing here\n"));
    }

    @Test
    void versionsCompareNumericallyPerSegment() {
        assertTrue(Migrations.compareVersions("2", "10") < 0);
        assertTrue(Migrations.compareVersions("1.2", "1.10") < 0);
        assertTrue(Migrations.compareVersions("1.1", "1") > 0);
        assertEquals(0, Migrations.compareVersions("1.0", "1"));
        assertTrue(Migrations.compareVersions("20261013_1", "20261013_2") < 0);
    }
}
