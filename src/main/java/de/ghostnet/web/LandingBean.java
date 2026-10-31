package de.ghostnet.web;

import java.util.List;

import de.ghostnet.domain.Person;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import de.ghostnet.application.PersonService;

@Named
@RequestScoped
public class LandingBean {

    private static final String NETZUEBERSICHT = "netzuebersicht.xhtml?faces-redirect=true";
    private static final int MAX_BENUTZERNAME = 30;

    @Inject
    private UserContext userContext;

    @Inject
    private PersonService personService;

    private Long ausgewaehltePersonId;
    private String neuerBenutzername;
    private String fehlermeldung;
    private String hinweismeldung;

    public String anonymFortfahren() {
        userContext.anonymFortfahren();
        return NETZUEBERSICHT;
    }

    public String vorhandenePersonAuswaehlen() {
        fehlermeldung = null;
        hinweismeldung = null;
    
        return personService.anhandIdLaden(ausgewaehltePersonId)
                .map(person -> {
                    userContext.personAuswaehlen(person);
                    return NETZUEBERSICHT;
                })
                .orElseGet(() -> {
                    fehlermeldung = "Bitte einen vorhandenen Benutzernamen auswählen.";
                    return null;
                });
    }

    public String neuenBenutzerAnlegen() {
        fehlermeldung = null;
        hinweismeldung = null;
    
        try {
            Person person = personService.anlegen(neuerBenutzername);
            userContext.personAuswaehlen(person);
    
            return NETZUEBERSICHT;
        } catch (IllegalArgumentException fehler) {
            fehlermeldung = fehler.getMessage();
            return null;
        } catch (jakarta.persistence.PersistenceException fehler) {
            fehlermeldung =
                    "Die Person konnte nicht gespeichert werden. "
                    + "Prüfe, ob der Benutzername bereits vergeben ist.";
    
            return null;
        }
    }

    public List<Person> getVorhandenePersonen() {
        return personService.alleLaden();
    }

    public Long getAusgewaehltePersonId() {
        return ausgewaehltePersonId;
    }

    public void setAusgewaehltePersonId(Long ausgewaehltePersonId) {
        this.ausgewaehltePersonId = ausgewaehltePersonId;
    }

    public String getNeuerBenutzername() {
        return neuerBenutzername;
    }

    public void setNeuerBenutzername(String neuerBenutzername) {
        this.neuerBenutzername = neuerBenutzername;
    }

    public String getFehlermeldung() {
        return fehlermeldung;
    }

    public String getHinweismeldung() {
        return hinweismeldung;
    }
}
