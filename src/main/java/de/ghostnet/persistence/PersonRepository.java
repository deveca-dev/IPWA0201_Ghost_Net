package de.ghostnet.persistence;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import de.ghostnet.domain.Person;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@RequestScoped
public class PersonRepository {

    @Inject
    private EntityManager entityManager;

    public PersonRepository() {
    }

    public PersonRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    public Optional<Person> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(entityManager.find(Person.class, id));
    }

    public List<Person> findAll() {
        return entityManager.createQuery(
                "select p from Person p order by lower(p.benutzername), p.id", Person.class)
                .getResultList();
    }

    public Optional<Person> findByBenutzername(String benutzername) {
        if (benutzername == null || benutzername.isBlank()) {
            return Optional.empty();
        }
        return entityManager.createQuery(
                "select p from Person p where lower(p.benutzername) = :name", Person.class)
                .setParameter("name", benutzername.strip().toLowerCase(Locale.ROOT))
                .getResultStream().findFirst();
    }

    public void persist(Person person) {
        if (!entityManager.getTransaction().isActive()) {
            throw new IllegalStateException("Speichern benötigt eine aktive Transaktion.");
        }
        entityManager.persist(Objects.requireNonNull(person));
    }
}
