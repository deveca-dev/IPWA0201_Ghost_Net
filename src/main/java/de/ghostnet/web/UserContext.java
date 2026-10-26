package de.ghostnet.web;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import de.ghostnet.domain.Person;

@Named
@SessionScoped
public class UserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long personId;
    private String benutzername;

    public void personAuswaehlen(Person person) {
        this.personId = person.getId();
        this.benutzername = person.getBenutzername();
    }

    public void anonymFortfahren() {
        this.personId = null;
        this.benutzername = null;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getBenutzername() {
        return benutzername;
    }

    public boolean isAnonym() {
        return personId == null;
    }
}