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

## 2. Docker lokal

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

## Validierung

- OpenAPI validiert; clean build mit Java 21 erfolgreich.
- Alle 22 Tests erfolgreich.
- JAR-Manifest und enthaltene Startklasse geprüft.
- java -jar erreicht RelinApplication; PostgreSQL ist in der Testumgebung nicht vorhanden.
- Docker-Laufzeittest offen: Docker ist in der Testumgebung nicht installiert.

## Quelle

https://spring.io/guides/gs/spring-boot-docker/
