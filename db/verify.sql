-- Nach der Migration in einer Entwicklungsdatenbank ausführen.
-- Sämtliche Testdaten werden am Ende zurückgerollt.
BEGIN;

DO $$
DECLARE
    person_id bigint;
    netz_id bigint;
    test_name text := 'pruefung_' || substr(md5(random()::text), 1, 16);
BEGIN
    INSERT INTO person(benutzername) VALUES(test_name) RETURNING id INTO person_id;
    INSERT INTO geisternetz(breitengrad, laengengrad, geschaetzte_groesse_m2, status, gemeldet_am)
    VALUES(54, 10, 25, 'GEMELDET', CURRENT_TIMESTAMP) RETURNING id INTO netz_id;

    BEGIN
        INSERT INTO person(benutzername) VALUES(upper(test_name));
        RAISE EXCEPTION 'Namenseindeutigkeit wurde nicht erzwungen';
    EXCEPTION WHEN unique_violation THEN NULL;
    END;

    BEGIN
        UPDATE geisternetz SET breitengrad = 91 WHERE id = netz_id;
        RAISE EXCEPTION 'Ungültige Position wurde akzeptiert';
    EXCEPTION WHEN check_violation THEN NULL;
    END;

    BEGIN
        UPDATE geisternetz SET geschaetzte_groesse_m2 = 'NaN'::numeric WHERE id = netz_id;
        RAISE EXCEPTION 'Ungültige Größe wurde akzeptiert';
    EXCEPTION WHEN check_violation THEN NULL;
    END;

    BEGIN
        UPDATE geisternetz SET status = 'GEBORGEN' WHERE id = netz_id;
        RAISE EXCEPTION 'Geborgen ohne Zuordnung wurde akzeptiert';
    EXCEPTION WHEN check_violation THEN NULL;
    END;

    UPDATE geisternetz SET status = 'BERGUNG_BEVORSTEHEND', bergende_person_id = person_id,
        uebernommen_am = CURRENT_TIMESTAMP, version = version + 1 WHERE id = netz_id;
    UPDATE geisternetz SET status = 'VERSCHOLLEN', verschollen_am = CURRENT_TIMESTAMP,
        version = version + 1 WHERE id = netz_id;

    BEGIN
        DELETE FROM person WHERE id = person_id;
        RAISE EXCEPTION 'Referenzierte Person konnte gelöscht werden';
    EXCEPTION WHEN foreign_key_violation THEN NULL;
    END;
    RAISE NOTICE 'Schema-Prüfungen erfolgreich';
END $$;

ROLLBACK;
