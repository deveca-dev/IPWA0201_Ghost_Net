package de.ghostnet.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class TransactionExecutorTest {

    @Test
    void commitsOnlyAfterSuccessfulWork() {
        Fixture fixture = new Fixture();
        String result = fixture.executor.execute(em -> {
            assertSame(fixture.manager, em);
            fixture.calls.add("work");
            return "fertig";
        });
        assertEquals("fertig", result);
        assertEquals(List.of("begin", "work", "commit"), fixture.calls);
    }

    @Test
    void rollsBackAndDetachesAfterWorkFailure() {
        Fixture fixture = new Fixture();
        RuntimeException failure = new IllegalArgumentException("Abbruch");
        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> fixture.executor.execute(em -> { throw failure; })));
        assertEquals(List.of("begin", "rollback", "clear"), fixture.calls);
    }

    @Test
    void rollsBackWhenCommitFails() {
        Fixture fixture = new Fixture();
        fixture.failCommit = true;
        assertThrows(IllegalStateException.class, () -> fixture.executor.execute(em -> null));
        assertEquals(List.of("begin", "commit", "rollback", "clear"), fixture.calls);
    }

    @Test
    void preservesOriginalFailureWhenRollbackAlsoFails() {
        Fixture fixture = new Fixture();
        fixture.failRollback = true;
        RuntimeException original = new IllegalArgumentException("Fachlicher Fehler");
        RuntimeException result = assertThrows(IllegalArgumentException.class,
                () -> fixture.executor.execute(em -> { throw original; }));
        assertSame(original, result);
        assertEquals(1, result.getSuppressed().length);
        assertEquals(List.of("begin", "rollback", "clear"), fixture.calls);
    }

    @Test
    void doesNotTakeOverAnExistingTransaction() {
        Fixture fixture = new Fixture();
        fixture.active = true;
        assertThrows(IllegalStateException.class, () -> fixture.executor.execute(em -> null));
        assertTrue(fixture.calls.isEmpty());
        assertTrue(fixture.active);
    }

    @Test
    void rollsBackAfterAnError() {
        Fixture fixture = new Fixture();
        assertThrows(AssertionError.class,
                () -> fixture.executor.execute(em -> { throw new AssertionError("Abbruch"); }));
        assertEquals(List.of("begin", "rollback", "clear"), fixture.calls);
    }

    private static class Fixture {
        final List<String> calls = new ArrayList<>();
        boolean active;
        boolean failCommit;
        boolean failRollback;
        final EntityTransaction transaction = (EntityTransaction) Proxy.newProxyInstance(
                EntityTransaction.class.getClassLoader(), new Class<?>[]{EntityTransaction.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "isActive": return active;
                        case "begin": calls.add("begin"); active = true; return null;
                        case "commit":
                            calls.add("commit");
                            if (failCommit) throw new IllegalStateException("Commit fehlgeschlagen");
                            active = false;
                            return null;
                        case "rollback":
                            calls.add("rollback");
                            if (failRollback) throw new IllegalStateException("Rollback fehlgeschlagen");
                            active = false;
                            return null;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
        final EntityManager manager = (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(), new Class<?>[]{EntityManager.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getTransaction")) return transaction;
                    if (method.getName().equals("clear")) { calls.add("clear"); return null; }
                    throw new UnsupportedOperationException(method.getName());
                });
        final TransactionExecutor executor = new TransactionExecutor(manager);
    }
}
