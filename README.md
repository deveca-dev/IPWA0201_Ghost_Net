# IPWA0201_Ghost_Net
Öffentliches Repo zur Aufgabenstellung der Fallstudie zum Modul IPWA02-01.

## Einrichten der Datenbank mit Docker
Kopiere die .env.example Datei und benenne sie in .env um.
```bash
cp .env.example .env
```

Starte die Datenbank mit Docker
```bash
docker compose up -d
```

Stoppe die Datenbank mit Docker
```bash
docker compose down
```

### Spätere Verwendung der Datenbank
Wenn die Datenbank erfolgreich gestartet wurde, und der Service `healty`anzeigt kann später via JDBC die folgende Adresse verwendet werden:
```bash
jdbc:postgresql://localhost:5432/ghostnet
```

## Starten der Anwendung
Starte die Anwendung mit Maven
```bash
mvn spring-boot:run
```

## Stoppen der Anwendung
Stoppe die Anwendung mit Maven
```bash
mvn spring-boot:stop
```

## Beenden der Datenbank
Stoppe die Datenbank mit Docker
```bash
docker compose down
```

# Datenbank-Migrationen
Um die Migration um laufenden Docker anzuwenden folgender Befehl ausführen:
```bash
docker compose exec -T postgres sh -c \
  'psql -X -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -v ON_ERROR_STOP=1 --single-transaction -f -' \
  < db/migration/V001_initial_schema.sql
```