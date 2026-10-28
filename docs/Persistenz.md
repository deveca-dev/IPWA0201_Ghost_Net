# Persistenz und Datenbankschema

## Aufbau

Die Persistenzschicht liegt unter `de.ghostnet.persistence`:

| Klasse | Verantwortung |
|---|---|
| `DatabaseConfig` | Liest externe Einstellungen; enthält keine fest hinterlegten Zugangsdaten. |
| `JpaResources` | Eine EntityManagerFactory je Anwendung, ein EntityManager je HTTP-Request; schließt beide am Ende ihrer Lebensdauer. |
| `TransactionExecutor` | Beginnt und beendet eine lokale Transaktion; rollt bei Fehlern zurück und bereinigt den Persistenzkontext. |
| `PersonRepository` | Personen anlegen, auflisten und über ID oder Namen suchen. |
| `NetzRepository` | Netze anlegen, mit Personenbezügen laden und nach Status abfragen. |

Die Dienste werden diese Komponenten gemeinsam verwenden. Repositories beginnen keine eigenen Transaktionen und prüfen keine fachlichen Berechtigungen. Ein Dienst ruft `execute` auf und erledigt darin alle zusammengehörigen Zugriffe. CDI injiziert denselben EntityManager des Requests in Executor und Repositories. Bereits geladene Entitäten werden innerhalb dieser Transaktion geändert; ein pauschales `merge` von Formularobjekten ist nicht vorgesehen. Die Ergebnisse werden im späteren Dienst in Ausgabeobjekte übersetzt.

Verschachtelte Transaktionen werden bewusst abgewiesen. Die ursprüngliche Ausnahme bleibt erhalten, auch wenn ein Rollback ebenfalls scheitert. Bei einem Fehler soll der Dienst den Vorgang beenden und die Oberfläche eine neue Anfrage auslösen.

## Schema anlegen

Für diesen kleinen Prototyp werden SQL-Migrationen explizit mit `psql` ausgeführt. Es gibt noch keinen automatischen Migrationsrunner. Die Datei `db/migration/V001__initial_schema.sql` ist die erste, einmalige Migration. Jede Migration wird in einer Transaktion ausgeführt, bei SQL-Fehlern wird abgebrochen. `schema_migration` protokolliert die angewandte Version.

Voraussetzung: Docker läuft und die lokale `.env` für den PostgreSQL-Container wurde eingerichtet.

```sh
docker compose up -d
docker compose ps
sh scripts/migrate.sh db/migration/V001__initial_schema.sql
```

Die Migration erstellt ausschließlich neue Tabellen. Sie löscht keine bestehenden Daten. Auf einer bereits initialisierten Datenbank schlägt die erneute Ausführung fehl und wird zurückgerollt. Eine vorhandene Datenbank mit eigenen Tabellen muss zuerst geprüft werden; nicht das Volume löschen, um einen Migrationsfehler zu umgehen. Spätere Änderungen erhalten eine neue Datei und einen neuen Eintrag in `schema_migration`; bereits angewandte Dateien werden nicht nachträglich verändert.

Das Schema erzwingt eindeutige, auch ohne Beachtung der Groß-/Kleinschreibung unterschiedene Benutzernamen, gültige Koordinaten und Größen, Fremdschlüssel, erlaubte Statuswerte und zusammengehörige Status-/Zeitangaben. Ein verschollenes Netz darf eine vorherige Bergungszuordnung behalten. Die Datenbank prüft konsistente Zustände, ersetzt aber nicht die im Service umzusetzenden Regeln für den Wechsel zwischen Zuständen oder die handelnde Person.

## Tomcat konfigurieren

Die `persistence.xml` liegt unter `src/main/resources/META-INF/` und wird damit als `WEB-INF/classes/META-INF/persistence.xml` in das WAR übernommen. Hibernate prüft das Schema mit `validate`; es erzeugt oder löscht keine Tabellen beim Start.

Die Java-Anwendung benötigt folgende externe Einstellungen:

| Umgebungsvariable | Alternative JVM-Systemeigenschaft | Inhalt |
|---|---|---|
| `GHOSTNET_DB_URL` | `ghostnet.db.url` | PostgreSQL-JDBC-Verbindungsadresse. |
| `GHOSTNET_DB_USER` | `ghostnet.db.user` | Datenbankbenutzer. |
| `GHOSTNET_DB_PASSWORD` | `ghostnet.db.password` | Lokal gesetztes Datenbankpasswort. |

Systemeigenschaften haben Vorrang. In Eclipse werden die Umgebungsvariablen in der Run Configuration des Tomcat-Prozesses unter „Environment“ eingetragen. Für die lokale Compose-Datenbank ist die JDBC-Adresse `jdbc:postgresql://localhost:5432/ghostnet`; Benutzer und Passwort müssen zur vorhandenen Datenbank passen. Keine echten Werte in die versionierten Dateien eintragen. Die Docker-`.env` wird von Tomcat nicht automatisch eingelesen.

Die Factory wird beim ersten Zugriff auf die Persistenzkomponente erzeugt. Fehlende Konfiguration, eine nicht erreichbare Datenbank oder ein unpassendes Schema führen zu einem Fehler statt zu einer leeren Ersatzdatenhaltung. Für diese lokale Entwicklungsstufe verwendet Hibernate seinen eingebauten Verbindungspool. Eine produktive Pool-Konfiguration ist nicht Teil dieses Prototyps.

## Prüfungen

Mit Java 25 und Maven:

```sh
mvn test
mvn package
```

Die Unit-Tests prüfen Transaktionsabschluss, Rollback bei Arbeits- und Commitfehlern, den Erhalt ursprünglicher Ausnahmen, die Ablehnung verschachtelter Transaktionen sowie fehlende und maskierte Verbindungseinstellungen.

Nach der Migration lässt sich das Schema ohne bleibende Testdaten prüfen:

```sh
sh scripts/migrate.sh db/verify.sql
```

Diese Prüfdatei ist keine Migration. Sie testet gültige Inserts und Änderungen sowie die Ablehnung von Namensduplikaten, ungültigen Koordinaten, ungültigen Größen, inkonsistenten Zuständen und dem Löschen referenzierter Personen. Ihre Testdaten werden zurückgerollt. Sie ersetzt keinen noch ausstehenden JPA-/CDI-Integrationstest.

## Prüfstand dieser Änderung

- Alle 14 Java-Quelldateien einschließlich Tests wurden mit Java 25 gegen lokal vorhandene Bibliotheken kompiliert.
- Acht JUnit-Tests wurden erfolgreich ausgeführt.
- Ein regulärer Maven-/WAR-Build wurde nicht erfolgreich ausgeführt: Es steht hier nur eine eingebettete Eclipse-Maven-Laufzeit ohne eigenständige Maven-Installation zur Verfügung.
- Der Zugriff auf den Docker-Daemon ist in der Arbeitsumgebung gesperrt. Auch der Versuch einer isolierten PostgreSQL-Testinstanz scheiterte an der Berechtigung für gemeinsamen Speicher. Deshalb wurden SQL-Prüfungen, Hibernate-Schemavalidierung und Tomcat/CDI-Laufzeit hier nicht ausgeführt.
- Services und Datenbankanbindung der vorhandenen Oberflächen-Beans bleiben der nächste Umsetzungsschritt. Die Webanwendung speichert durch diese Änderung allein noch keine Meldungen.

## Quellen

Jakarta Persistence Team. (2024, 10. April). *Jakarta Persistence* (Version 3.2). https://jakarta.ee/specifications/persistence/3.2/jakarta-persistence-spec-3.2.html

PostgreSQL Global Development Group. (o. D.). *psql*. PostgreSQL 18 documentation. https://www.postgresql.org/docs/18/app-psql.html
