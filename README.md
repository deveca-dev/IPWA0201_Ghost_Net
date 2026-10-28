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

## Anwendung bauen und starten

Das Projekt verwendet JSF/CDI und Tomcat, nicht Spring Boot. Voraussetzung ist ein JDK 25.

```bash
mvn test package
```

Das erzeugte WAR aus `target/` wird in Tomcat 11 bereitgestellt. Alternativ das Projekt in Eclipse als Maven-Projekt aktualisieren und über „Run on Server“ starten.

Vor dem ersten Zugriff auf die Persistenz müssen Schema und externe Verbindungseinstellungen eingerichtet sein:

```bash
sh scripts/migrate.sh db/migration/V001__initial_schema.sql
```

Die genaue Einrichtung, die benötigten Tomcat-Umgebungsvariablen und die noch offenen Laufzeitprüfungen stehen in [docs/Persistenz.md](docs/Persistenz.md).

**Stand:** Persistenzinfrastruktur und Repositories sind vorbereitet. Die Oberflächen-Beans enthalten weiterhin Platzhalter; Services und fachliche Speicherung sind noch anzuschließen.

## Beenden der Datenbank
Stoppe die Datenbank mit Docker
```bash
docker compose down
```
