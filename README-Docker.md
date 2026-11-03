# Backend als JAR und Docker-Image

## 1. Ausführbare JAR (Java 21)

Im Hauptverzeichnis des Projekts unter Windows CMD:

```bat
gradlew.bat clean build
set "SPRING_DATASOURCE_PASSWORD=DEIN_LOKALES_DB_PASSWORT"
java -jar azubi-application\build\libs\relin.jar
```

Unter PowerShell setzt du die Variable mit
`$env:SPRING_DATASOURCE_PASSWORD = 'DEIN_LOKALES_DB_PASSWORT'` und baust mit
`.\gradlew.bat clean build`. Unter Linux/macOS: `bash ./gradlew clean build`.

PostgreSQL muss laufen. Standard: localhost:5432, Datenbank postgres,
Benutzer postgres, Schema relin_uni2. URL und Benutzer lassen sich über
SPRING_DATASOURCE_URL und SPRING_DATASOURCE_USERNAME überschreiben.
Das Passwort wird jetzt zur Laufzeit gesetzt, auch in der IntelliJ Run Configuration.
Spring Boot liest eine .env-Datei nicht automatisch ein.

Die Startklasse ist com.mediamarktsaturn.azubi.RelinApplication.
bootJar verpackt sie zusammen mit den Bibliotheken und dem Spring-Boot-Launcher.
Die leere Root-JAR und die zusätzliche normale Application-JAR werden nicht mehr gebaut.
Die JARs der Bibliotheksmodule bleiben für die Modulabhängigkeiten erhalten.
`clean` entfernt alte JARs, damit sie nicht versehentlich gestartet werden.

## 2. Docker lokal (optional)

Dieser Abschnitt gilt nur für Rechner, auf denen Docker erlaubt und installiert ist.
Auf dem Firmenlaptop ist keine Docker- oder WSL-Installation erforderlich.
Docker Desktop muss gestartet sein (Linux-Container). Zuerst die JAR bauen,
dann im Hauptverzeichnis:

```bat
docker build -t azubi-projekt:local .
copy .env.example .env
```

Trage dein Datenbankpasswort in .env ein. Die Datei wird von Git ignoriert.
Die Beispiel-URL nutzt host.docker.internal für PostgreSQL auf deinem Windows-PC.
localhost im Container würde den Container selbst bezeichnen.

```bat
docker run --rm --name relin -p 8081:8081 --env-file .env azubi-projekt:local
```

Beende vorher das direkt gestartete Backend, damit Port 8081 frei ist.
Unter Linux zusätzlich `--add-host=host.docker.internal:host-gateway` verwenden.
PostgreSQL muss Verbindungen vom Container erlauben.
Das Frontend erreicht das Backend weiterhin unter http://localhost:8081.
Ein kurzer Check: http://localhost:8081/api/products.
Flyway läuft beim Anwendungsstart gegen die konfigurierte Datenbank.

Das Dockerfile verwendet Java 21 auf Alpine, kopiert ausschließlich relin.jar,
läuft als Benutzer spring und startet mit java -jar /app/app.jar.
Das Image enthält keine PostgreSQL-Instanz.

## 3. Docker-Build und Upload in GitHub Actions

Der Workflow läuft auf ubuntu-latest bei GitHub. Dein Laptop braucht kein WSL
und kein Docker. Nach erfolgreichen OpenAPI-Prüfungen, Build und Tests wird nur
bei einem Push auf main (auch nach einem PR-Merge) das Image gebaut und zu
Docker Hub hochgeladen. PRs und manuelle Starts überspringen diese Schritte.
Der Docker-Build übernimmt die zuvor im selben Job erzeugte relin.jar.
Ein Container-Laufzeittest ist nicht enthalten.

Vor dem Merge einrichten:

1. Einen eigenen Account auf https://hub.docker.com/ erstellen.
2. Dort ein Repository namens azubi-projekt unter deinem Benutzernamen anlegen.
3. Einen Docker-Hub-Access-Token mit Lese- und Schreibrechten erstellen.
4. Auf GitHub unter Settings > Secrets and variables > Actions:
   - Unter Variables: DOCKERHUB_USERNAME = dein Docker-Hub-Benutzername.
   - Unter Secrets: DOCKERHUB_TOKEN = dein Access-Token.

Kein Passwort oder Token in den Code eintragen. Der Docker-Hub-Token ist nicht
identisch mit deinem PostgreSQL-Passwort. Für den Image-Build ist keine Datenbank
nötig. Ohne die oben genannten Einstellungen schlägt die Anmeldung fehl.

Die veröffentlichten Tags heißen BENUTZERNAME/azubi-projekt:latest und
BENUTZERNAME/azubi-projekt:sha-COMMIT_SHA. Den Lauf findest du unter Actions >
Backend CI. Das Image erscheint anschließend in deinem Docker-Hub-Repository.
Die erste tatsächliche Veröffentlichung erfolgt nach dem Merge auf main.

## Validierung

- OpenAPI validiert; clean build mit Java 21 erfolgreich.
- Alle 22 Tests erfolgreich.
- JAR-Manifest und enthaltene Startklasse geprüft.
- java -jar erreicht RelinApplication; PostgreSQL ist in der Testumgebung nicht vorhanden.
- Docker-Laufzeittest offen: Docker ist in der Testumgebung nicht installiert.

## Quelle

https://spring.io/guides/gs/spring-boot-docker/
