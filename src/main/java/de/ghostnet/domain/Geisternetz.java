package de.ghostnet.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "geisternetz")
public class Geisternetz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal breitengrad;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal laengengrad;

    @Column(name = "geschaetzte_groesse_m2",
            nullable = false, precision = 12, scale = 2)
    private BigDecimal geschaetzteGroesse;

    @Column(columnDefinition = "text")
    private String beschreibung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NetStatus status = NetStatus.GEMELDET;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meldende_person_id")
    private Person meldendePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bergende_person_id")
    private Person bergendePerson;

    @Column(name = "gemeldet_am", nullable = false)
    private Instant gemeldetAm;

    @Column(name = "uebernommen_am")
    private Instant uebernommenAm;

    @Column(name = "geborgen_am")
    private Instant geborgenAm;

    @Column(name = "verschollen_am")
    private Instant verschollenAm;

    @Version
    private long version;

    protected Geisternetz() {
    }

    public void bergungUebernehmen(Person person) {
        if (person == null) {
            throw new IllegalArgumentException(
                    "Eine bergende Person ist erforderlich."
            );
        }
    
        if (status != NetStatus.GEMELDET) {
            throw new IllegalStateException(
                    "Nur gemeldete Netze können übernommen werden."
            );
        }
    
        this.bergendePerson = person;
        this.uebernommenAm = Instant.now();
        this.status = NetStatus.BERGUNG_BEVORSTEHEND;
    }

    public Geisternetz(
            BigDecimal breitengrad,
            BigDecimal laengengrad,
            BigDecimal geschaetzteGroesse,
            String beschreibung,
            Person meldendePerson) {

        this.breitengrad = breitengrad;
        this.laengengrad = laengengrad;
        this.geschaetzteGroesse = geschaetzteGroesse;
        this.beschreibung = beschreibung;
        this.meldendePerson = meldendePerson;
        this.gemeldetAm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBreitengrad() {
        return breitengrad;
    }

    public BigDecimal getLaengengrad() {
        return laengengrad;
    }

    public BigDecimal getGeschaetzteGroesse() {
        return geschaetzteGroesse;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public NetStatus getStatus() {
        return status;
    }

    public Person getMeldendePerson() {
        return meldendePerson;
    }

    public Person getBergendePerson() {
        return bergendePerson;
    }

    public Instant getGemeldetAm() {
        return gemeldetAm;
    }

    public Instant getUebernommenAm() {
        return uebernommenAm;
    }

    public Instant getGeborgenAm() {
        return geborgenAm;
    }

    public Instant getVerschollenAm() {
        return verschollenAm;
    }

    public long getVersion() {
        return version;
    }
}