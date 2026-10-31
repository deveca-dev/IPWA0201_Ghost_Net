package de.ghostnet.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import de.ghostnet.application.NetzService;

import de.ghostnet.domain.Geisternetz;
import de.ghostnet.domain.NetStatus;
import de.ghostnet.domain.Person;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@SessionScoped
public class NetzBearbeitungBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String NICHT_GESPEICHERT =
            "Es wurde nichts gespeichert, weil der Geisternetz-Service noch nicht angebunden ist.";

    private static final DateTimeFormatter ZEITPUNKT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withLocale(Locale.GERMANY)
            .withZone(ZoneId.systemDefault());

    @Inject
    private UserContext userContext;

    @Inject
    private NetzService netzService;

    private BigDecimal breitengrad;
    private BigDecimal laengengrad;
    private BigDecimal geschaetzteGroesse;
    private String beschreibung;
    private boolean anonymMelden;

    private transient Geisternetz ausgewaehltesNetz;
    private String fehlermeldung;
    private String hinweismeldung;

    public String meldeformularOeffnen() {
        fehlermeldung = null;
        hinweismeldung = null;
        return "melden.xhtml?faces-redirect=true";
    }

    public String netzAnzeigen(Geisternetz netz) {
        this.ausgewaehltesNetz = netz;
        fehlermeldung = null;
        hinweismeldung = null;
        return "netzdetails.xhtml?faces-redirect=true";
    }

    public String netzMelden() {
        fehlermeldung = null;
        hinweismeldung = null;
    
        if (!positionsdatenGueltig()) {
            return null;
        }
    
        Long meldendePersonId = null;
    
        if (!anonymMelden && !userContext.isAnonym()) {
            meldendePersonId = userContext.getPersonId();
        }
    
        try {
            netzService.melden(
                    breitengrad,
                    laengengrad,
                    geschaetzteGroesse,
                    beschreibung,
                    meldendePersonId
            );
    
            hinweismeldung = "Das Geisternetz wurde erfolgreich gemeldet.";
    
            breitengrad = null;
            laengengrad = null;
            geschaetzteGroesse = null;
            beschreibung = null;
            anonymMelden = false;
    
            return null;
    
        } catch (IllegalArgumentException fehler) {
            fehlermeldung = fehler.getMessage();
            return null;
    
        } catch (jakarta.persistence.PersistenceException fehler) {
            fehlermeldung = "Das Geisternetz konnte nicht gespeichert werden.";
            return null;
        }
    }

    public String bergungUebernehmen() {
        fehlermeldung = null;
        hinweismeldung = null;
    
        if (ausgewaehltesNetz == null) {
            fehlermeldung = "Es ist kein Netz ausgewählt.";
            return null;
        }
    
        if (userContext.isAnonym()) {
            fehlermeldung =
                    "Bitte zuerst einen Benutzernamen auswählen.";
            return null;
        }
    
        try {
            netzService.bergungUebernehmen(
                    ausgewaehltesNetz.getId(),
                    userContext.getPersonId(),
                    ausgewaehltesNetz.getVersion()
            );
    
            // Zur Übersicht zurück: Dort werden die Daten neu geladen.
            return "netzuebersicht.xhtml?faces-redirect=true";
    
        } catch (IllegalArgumentException | IllegalStateException fehler) {
            fehlermeldung = fehler.getMessage();
            return null;
    
        } catch (jakarta.persistence.PersistenceException fehler) {
            fehlermeldung =
                    "Die Bergung konnte nicht übernommen werden. "
                    + "Bitte lade die Übersicht erneut.";
            return null;
        }
    }

    public String alsGeborgenMelden() {
        fehlermeldung = null;
        hinweismeldung = null;
    
        if (ausgewaehltesNetz == null) {
            fehlermeldung = "Es ist kein Netz ausgewählt.";
            return null;
        }
    
        if (userContext.isAnonym()) {
            fehlermeldung = "Bitte zuerst eine Person auswählen.";
            return null;
        }
    
        try {
            netzService.alsGeborgenMelden(
                    ausgewaehltesNetz.getId(),
                    userContext.getPersonId(),
                    ausgewaehltesNetz.getVersion()
            );
    
            return "netzuebersicht.xhtml?faces-redirect=true";
    
        } catch (IllegalArgumentException | IllegalStateException fehler) {
            fehlermeldung = fehler.getMessage();
            return null;
    
        } catch (jakarta.persistence.PersistenceException fehler) {
            fehlermeldung =
                    "Das Netz konnte nicht als geborgen gemeldet werden. "
                    + "Bitte öffne die Übersicht erneut.";
            return null;
        }
    }

    public String alsVerschollenMelden() {
        fehlermeldung = null;
        hinweismeldung = null;
    
        if (ausgewaehltesNetz == null) {
            fehlermeldung = "Es ist kein Netz ausgewählt.";
            return null;
        }
    
        try {
            netzService.alsVerschollenMelden(
                    ausgewaehltesNetz.getId(),
                    ausgewaehltesNetz.getVersion()
            );
    
            // Die Übersicht lädt den aktualisierten Status.
            return "netzuebersicht.xhtml?faces-redirect=true";
    
        } catch (IllegalArgumentException | IllegalStateException fehler) {
            fehlermeldung = fehler.getMessage();
            return null;
    
        } catch (jakarta.persistence.PersistenceException fehler) {
            fehlermeldung =
                    "Das Netz konnte nicht als verschollen gemeldet werden. "
                    + "Bitte öffne die Übersicht erneut.";
            return null;
        }
    }

    public String statusText(NetStatus status) {
        if (status == null) {
            return "Unbekannt";
        }
        return switch (status) {
            case GEMELDET -> "Gemeldet";
            case BERGUNG_BEVORSTEHEND -> "Bergung bevorstehend";
            case GEBORGEN -> "Geborgen";
            case VERSCHOLLEN -> "Verschollen";
        };
    }

    public String personText(Person person) {
        if (person == null || person.getBenutzername() == null || person.getBenutzername().isBlank()) {
            return "Anonym";
        }
        return person.getBenutzername();
    }

    public String zeitpunkt(Instant instant) {
        if (instant == null) {
            return "–";
        }
        return ZEITPUNKT.format(instant);
    }

    private boolean positionsdatenGueltig() {
        if (breitengrad == null || breitengrad.compareTo(new BigDecimal("-90")) < 0
                || breitengrad.compareTo(new BigDecimal("90")) > 0) {
            fehlermeldung = "Der Breitengrad muss zwischen -90 und 90 liegen.";
            return false;
        }
        if (laengengrad == null || laengengrad.compareTo(new BigDecimal("-180")) < 0
                || laengengrad.compareTo(new BigDecimal("180")) > 0) {
            fehlermeldung = "Der Längengrad muss zwischen -180 und 180 liegen.";
            return false;
        }
        if (geschaetzteGroesse == null || geschaetzteGroesse.compareTo(BigDecimal.ZERO) <= 0) {
            fehlermeldung = "Die geschätzte Größe muss größer als 0 m² sein.";
            return false;
        }
        return true;
    }

    private boolean istZugeordneteBergendePerson() {
        if (userContext.isAnonym() || ausgewaehltesNetz.getBergendePerson() == null) {
            return false;
        }
        Long bergendeId = ausgewaehltesNetz.getBergendePerson().getId();
        return bergendeId != null && bergendeId.equals(userContext.getPersonId());
    }

    public BigDecimal getBreitengrad() {
        return breitengrad;
    }

    public void setBreitengrad(BigDecimal breitengrad) {
        this.breitengrad = breitengrad;
    }

    public BigDecimal getLaengengrad() {
        return laengengrad;
    }

    public void setLaengengrad(BigDecimal laengengrad) {
        this.laengengrad = laengengrad;
    }

    public BigDecimal getGeschaetzteGroesse() {
        return geschaetzteGroesse;
    }

    public void setGeschaetzteGroesse(BigDecimal geschaetzteGroesse) {
        this.geschaetzteGroesse = geschaetzteGroesse;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public boolean isAnonymMelden() {
        return anonymMelden;
    }

    public void setAnonymMelden(boolean anonymMelden) {
        this.anonymMelden = anonymMelden;
    }

    public Geisternetz getAusgewaehltesNetz() {
        return ausgewaehltesNetz;
    }

    public String getFehlermeldung() {
        return fehlermeldung;
    }

    public String getHinweismeldung() {
        return hinweismeldung;
    }
}
