package de.ghostnet.persistence;

import java.util.Objects;
import java.util.function.Function;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

@RequestScoped
public class TransactionExecutor {

    @Inject
    private EntityManager entityManager;

    public TransactionExecutor() {
    }

    public TransactionExecutor(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    public <T> T execute(Function<EntityManager, T> work) {
        Objects.requireNonNull(work);
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Verschachtelte Transaktionen werden nicht unterstützt.");
        }
        try {
            transaction.begin();
            T result = work.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException | Error failure) {
            try {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            try {
                entityManager.clear();
            } catch (RuntimeException clearFailure) {
                failure.addSuppressed(clearFailure);
            }
            throw failure;
        }
    }
}
