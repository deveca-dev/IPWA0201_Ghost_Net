package de.ghostnet.persistence;

import java.util.Map;

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
    public void initialisieren() {
        Map<String, Object> datenbankKonfiguration = Map.of(
                "jakarta.persistence.jdbc.url",
                einstellungLesen("GHOSTNET_DB_URL"),

                "jakarta.persistence.jdbc.user",
                einstellungLesen("GHOSTNET_DB_USER"),

                "jakarta.persistence.jdbc.password",
                einstellungLesen("GHOSTNET_DB_PASSWORD")
        );

        factory = Persistence.createEntityManagerFactory(
                "ghostnet",
                datenbankKonfiguration
        );
    }

    @Produces
    @RequestScoped
    public EntityManager entityManagerErstellen() {
        return factory.createEntityManager();
    }

    public void entityManagerSchliessen(
            @Disposes EntityManager entityManager) {

        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @PreDestroy
    public void beenden() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    private String einstellungLesen(String name) {
        String wert = System.getenv(name);

        if (wert == null || wert.isBlank()) {
            throw new IllegalStateException(
                    "Datenbankkonfiguration fehlt: " + name
            );
        }

        return wert;
    }
}