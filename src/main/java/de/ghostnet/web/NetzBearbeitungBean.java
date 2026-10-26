package de.ghostnet.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
        boolean anonym = anonymMelden || userContext.isAnonym();
        // TODO: Geisternetz über den Geisternetz-Service speichern.
        // Bei anonym bleibt die meldende Person leer, sonst die Person aus dem UserContext.
        if (anonym) {
            hinweismeldung = NICHT_GESPEICHERT
                    + " Die Meldung wäre anonym gewesen und hätte keine meldende Person gespeichert.";
        } else {
            hinweismeldung = NICHT_GESPEICHERT
                    + " Als meldende Person wäre „" + userContext.getBenutzername() + "“ vorgesehen gewesen.";
        }
        return null;
    }

    public String bergungUebernehmen() {
        fehlermeldung = null;
        hinweismeldung = null;
        if (ausgewaehltesNetz == null) {
            fehlermeldung = "Es ist kein Netz ausgewählt.";
            return null;
        }
        if (ausgewaehltesNetz.getStatus() != NetStatus.GEMELDET) {
            fehlermeldung = "Nur gemeldete Netze können zur Bergung übernommen werden.";
            return null;
        }
        if (userContext.isAnonym()) {
            fehlermeldung = "Bitte zuerst einen Benutzernamen auswählen. Anonym kann keine Bergung übernommen werden.";
            return null;
        }
        // TODO: Bergung über den Geisternetz-Service der Person aus dem UserContext zuordnen.
        hinweismeldung = NICHT_GESPEICHERT
                + " Die Übernahme durch „" + userContext.getBenutzername() + "“ wurde nicht ausgeführt.";
        return null;
    }

    public String alsGeborgenMelden() {
        fehlermeldung = null;
        hinweismeldung = null;
        if (ausgewaehltesNetz == null) {
            fehlermeldung = "Es ist kein Netz ausgewählt.";
            return null;
        }
        if (ausgewaehltesNetz.getStatus() != NetStatus.BERGUNG_BEVORSTEHEND) {
            fehlermeldung = "Nur ein übernommenes Netz mit dem Status „Bergung bevorstehend“ kann als geborgen gemeldet werden.";
            return null;
        }
        if (!istZugeordneteBergendePerson()) {
            fehlermeldung = "Nur die zugeordnete bergende Person kann dieses Netz als geborgen melden.";
            return null;
        }
        // TODO: Status über den Geisternetz-Service auf GEBORGEN setzen.
        hinweismeldung = NICHT_GESPEICHERT + " Der Status wurde nicht auf „Geborgen“ geändert.";
        return null;
    }

    public String alsVerschollenMelden() {
        fehlermeldung = null;
        hinweismeldung = null;
        if (ausgewaehltesNetz == null) {
            fehlermeldung = "Es ist kein Netz ausgewählt.";
            return null;
        }
        NetStatus status = ausgewaehltesNetz.getStatus();
        if (status != NetStatus.GEMELDET && status != NetStatus.BERGUNG_BEVORSTEHEND) {
            fehlermeldung = "Nur gemeldete oder übernommene Netze können als verschollen gemeldet werden.";
            return null;
        }
        // TODO: Status über den Geisternetz-Service auf VERSCHOLLEN setzen.
        // Die Meldung ist auch ohne ausgewählte Person zulässig.
        hinweismeldung = NICHT_GESPEICHERT + " Der Status wurde nicht auf „Verschollen“ geändert.";
        return null;
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
