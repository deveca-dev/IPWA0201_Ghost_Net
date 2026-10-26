package de.ghostnet.web;

import java.util.List;

import de.ghostnet.domain.Person;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class LandingBean {

    private static final String NETZUEBERSICHT = "netzuebersicht.xhtml?faces-redirect=true";
    private static final int MAX_BENUTZERNAME = 50;

    @Inject
    private UserContext userContext;

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
        if (ausgewaehltePersonId == null) {
            fehlermeldung = "Bitte einen vorhandenen Benutzernamen auswählen.";
            return null;
        }
        // TODO: Person über den Person-Service laden und userContext.personAuswaehlen(person) aufrufen.
        hinweismeldung = "Die Auswahl eines vorhandenen Benutzernamens ist noch nicht möglich, "
                + "weil der Person-Service noch nicht angebunden ist. Es wurde niemand angemeldet.";
        return null;
    }

    public String neuenBenutzerAnlegen() {
        fehlermeldung = null;
        hinweismeldung = null;
        String name = neuerBenutzername == null ? "" : neuerBenutzername.trim();
        if (name.isEmpty()) {
            fehlermeldung = "Bitte einen Benutzernamen eingeben.";
            return null;
        }
        if (name.length() > MAX_BENUTZERNAME) {
            fehlermeldung = "Der Benutzername darf höchstens " + MAX_BENUTZERNAME + " Zeichen lang sein.";
            return null;
        }
        neuerBenutzername = name;
        // TODO: Person über den Person-Service anlegen und userContext.personAuswaehlen(person) aufrufen.
        hinweismeldung = "Der Benutzername wurde nicht angelegt, weil der Person-Service noch nicht angebunden ist. "
                + "Es wurde niemand angemeldet.";
        return null;
    }

    public List<Person> getVorhandenePersonen() {
        // TODO: Vorhandene Personen über den Person-Service laden.
        return List.of();
    }

    public String getPersonenHinweis() {
        return "Vorhandene Benutzernamen können noch nicht geladen werden, weil der Person-Service nicht angebunden ist.";
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
