package de.ghostnet.application;

import java.math.BigDecimal;

import de.ghostnet.domain.Geisternetz;
import de.ghostnet.domain.Person;
import de.ghostnet.persistence.NetzRepository;
import de.ghostnet.persistence.PersonRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

@RequestScoped
public class NetzService {

    @Inject
    private NetzRepository netzRepository;

    @Inject
    private PersonRepository personRepository;

    @Inject
    private EntityManager entityManager;

    public Geisternetz melden(
            BigDecimal breitengrad,
            BigDecimal laengengrad,
            BigDecimal groesse,
            String beschreibung,
            Long meldendePersonId) {

        eingabenPruefen(breitengrad, laengengrad, groesse);

        EntityTransaction transaktion = entityManager.getTransaction();

        try {
            transaktion.begin();

            Person meldendePerson = null;

            if (meldendePersonId != null) {
                meldendePerson = personRepository
                        .anhandIdLaden(meldendePersonId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Die meldende Person wurde nicht gefunden."
                        ));
            }

            Geisternetz netz = new Geisternetz(
                    breitengrad,
                    laengengrad,
                    groesse,
                    beschreibung,
                    meldendePerson
            );

            netzRepository.speichern(netz);
            transaktion.commit();

            return netz;

        } catch (RuntimeException fehler) {
            if (transaktion.isActive()) {
                transaktion.rollback();
            }

            throw fehler;
        }
    }

    private void eingabenPruefen(
            BigDecimal breitengrad,
            BigDecimal laengengrad,
            BigDecimal groesse) {

        if (breitengrad == null
                || breitengrad.compareTo(new BigDecimal("-90")) < 0
                || breitengrad.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException(
                    "Der Breitengrad muss zwischen -90 und 90 liegen."
            );
        }

        if (laengengrad == null
                || laengengrad.compareTo(new BigDecimal("-180")) < 0
                || laengengrad.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException(
                    "Der Längengrad muss zwischen -180 und 180 liegen."
            );
        }

        if (groesse == null || groesse.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Die geschätzte Größe muss größer als 0 sein."
            );
        }
    }

    public List<Geisternetz> alleLaden() {
        return netzRepository.alleLaden();
    }

    public void bergungUebernehmen(
        Long netzId,
        Long personId,
        long erwarteteVersion) {

    if (personId == null) {
        throw new IllegalArgumentException(
                "Bitte zuerst eine Person auswählen."
        );
    }

    EntityTransaction transaktion = entityManager.getTransaction();

    try {
        transaktion.begin();

        Geisternetz netz = netzRepository.anhandIdLaden(netzId);

        if (netz == null) {
            throw new IllegalArgumentException(
                    "Das Geisternetz wurde nicht gefunden."
            );
        }

        if (netz.getVersion() != erwarteteVersion) {
            throw new IllegalStateException(
                    "Das Netz wurde zwischenzeitlich geändert. "
                    + "Bitte öffne die Übersicht erneut."
            );
        }

        Person person = personRepository.anhandIdLaden(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Die ausgewählte Person wurde nicht gefunden."
                ));

        netz.bergungUebernehmen(person);

        transaktion.commit();

    } catch (RuntimeException fehler) {
        if (transaktion.isActive()) {
            transaktion.rollback();
        }

        throw fehler;
    }
}

public void alsGeborgenMelden(
    Long netzId,
    Long personId,
    long erwarteteVersion) {

if (personId == null) {
    throw new IllegalArgumentException(
            "Bitte zuerst eine Person auswählen."
    );
}

EntityTransaction transaktion = entityManager.getTransaction();

try {
    transaktion.begin();

    Geisternetz netz = netzRepository.anhandIdLaden(netzId);

    if (netz == null) {
        throw new IllegalArgumentException(
                "Das Geisternetz wurde nicht gefunden."
        );
    }

    if (netz.getVersion() != erwarteteVersion) {
        throw new IllegalStateException(
                "Das Netz wurde zwischenzeitlich geändert. "
                + "Bitte öffne die Übersicht erneut."
        );
    }

    Person bergendePerson = netz.getBergendePerson();

    if (bergendePerson == null
            || !personId.equals(bergendePerson.getId())) {

        throw new IllegalArgumentException(
                "Nur die zugeordnete bergende Person "
                + "kann dieses Netz als geborgen melden."
        );
    }

    netz.alsGeborgenMelden();

    transaktion.commit();

} catch (RuntimeException fehler) {
    if (transaktion.isActive()) {
        transaktion.rollback();
    }

    throw fehler;
}
}

public void alsVerschollenMelden(Long netzId, long erwarteteVersion) {
    EntityTransaction transaktion = entityManager.getTransaction();

    try {
        transaktion.begin();

        Geisternetz netz = netzRepository.anhandIdLaden(netzId);

        if (netz == null) {
            throw new IllegalArgumentException(
                    "Das Geisternetz wurde nicht gefunden."
            );
        }

        if (netz.getVersion() != erwarteteVersion) {
            throw new IllegalStateException(
                    "Das Netz wurde zwischenzeitlich geändert. "
                    + "Bitte öffne die Übersicht erneut."
            );
        }

        // Die Entity prüft, ob der Statuswechsel fachlich erlaubt ist.
        netz.alsVerschollenMelden();

        transaktion.commit();

    } catch (RuntimeException fehler) {
        if (transaktion.isActive()) {
            transaktion.rollback();
        }

        throw fehler;
    }
}
}