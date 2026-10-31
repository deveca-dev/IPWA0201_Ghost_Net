package de.ghostnet.application;

import java.util.List;
import java.util.Optional;

import de.ghostnet.domain.Person;
import de.ghostnet.persistence.PersonRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

@RequestScoped
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    @Inject
    private EntityManager entityManager;

    public List<Person> alleLaden() {
        return personRepository.alleLaden();
    }

    public Optional<Person> anhandIdLaden(Long id) {
        return personRepository.anhandIdLaden(id);
    }

    public Person anlegen(String benutzername) {
        if (benutzername == null || benutzername.isBlank()) {
            throw new IllegalArgumentException(
                    "Bitte einen Benutzernamen eingeben."
            );
        }

        String name = benutzername.trim();

        if (name.length() > 50) {
            throw new IllegalArgumentException(
                    "Der Benutzername darf höchstens 50 Zeichen lang sein."
            );
        }

        Person person = new Person(name);
        EntityTransaction transaktion = entityManager.getTransaction();

        try {
            transaktion.begin();

            personRepository.speichern(person);

            transaktion.commit();

            return person;
        } catch (RuntimeException fehler) {
            if (transaktion.isActive()) {
                transaktion.rollback();
            }

            throw fehler;
        }
    }
}