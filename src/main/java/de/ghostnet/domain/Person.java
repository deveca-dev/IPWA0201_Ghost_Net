package de.ghostnet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String benutzername;

    protected Person() {
        // Für JPA erforderlich
    }

    public Person(String benutzername) {
        this.benutzername = benutzername;
    }

    public Long getId() {
        return id;
    }

    public String getBenutzername() {
        return benutzername;
    }
}