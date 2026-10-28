package de.ghostnet.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class JpaResources {

    private EntityManagerFactory factory;

    @PostConstruct
    public void initialize() {
        factory = Persistence.createEntityManagerFactory(
                "ghostnet", DatabaseConfig.fromEnvironment().properties());
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return factory.createEntityManager();
    }

    public void disposeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            try {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            } finally {
                entityManager.close();
            }
        }
    }

    @PreDestroy
    public void close() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}
