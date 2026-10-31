package de.ghostnet.persistence;

import java.util.List;
import java.util.Optional;

import de.ghostnet.domain.Person;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@RequestScoped
public class PersonRepository {

    @Inject
    private EntityManager entityManager;

    public List<Person> alleLaden() {
        return entityManager.createQuery(
                "SELECT p FROM Person p ORDER BY p.benutzername",
                Person.class
        ).getResultList();
    }

    public Optional<Person> anhandIdLaden(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        Person person = entityManager.find(Person.class, id);
        return Optional.ofNullable(person);
    }
}