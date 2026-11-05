# Schritt 1: Backend und PostgreSQL mit Compose vorbereiten

Die compose.yaml liegt im Hauptverzeichnis neben dem Dockerfile.
Sie nutzt das bereits veröffentlichte Backend-Image von Docker Hub.
Du brauchst auf deinem Laptop kein Docker oder WSL; ausgeführt werden diese
Befehle später auf einem Linux-Server mit Docker Engine und Compose-Plugin.
Nginx und öffentliche Erreichbarkeit folgen als eigener Schritt.

## Auf dem Server vorbereiten

```sh
cp .env.compose.example .env.compose
nano .env.compose
sudo mkdir -p /srv/postgres/data
```

Ein eigenes Datenbankpasswort zwischen die einfachen Anführungszeichen eintragen.
Die Datei .env.compose bleibt lokal und wird nicht committed.
Es handelt sich um das Passwort für die neue Container-Datenbank, nicht um den
Docker-Hub-Token. Bei leerem Passwort bricht Compose mit einer Meldung ab.
Falls der Image-Zugriff privat ist, vorher docker login ausführen.

## Prüfen und starten (erst auf dem Server)

```sh
docker compose --env-file .env.compose config --quiet
docker compose --env-file .env.compose pull
docker compose --env-file .env.compose up -d
docker compose --env-file .env.compose ps
docker compose --env-file .env.compose logs --tail=100 backend
curl --fail http://localhost:8080/api/markets
```

config --quiet prüft die Konfiguration, ohne die aufgelösten Passwörter auszugeben.
up -d startet die Dienste im Hintergrund. Ein erfolgreicher Datenbank-Healthcheck
gibt den Backendstart frei. Erst ein erfolgreicher curl-Aufruf bestätigt, dass
auch die Anwendung erreichbar ist. Flyway legt beim Backendstart Schema, Tabellen
und die vorhandenen Beispieldaten in der neuen Datenbank an.
Die Daten deiner bisherigen Laptop-Datenbank werden nicht automatisch übertragen.

## Was die Konfiguration verbindet

- db ist der interne DNS-Name der PostgreSQL-Instanz im Compose-Netzwerk.
- Backend und Datenbank bekommen dasselbe Passwort aus .env.compose.
- PostgreSQL hat keinen veröffentlichten Host-Port; das Backend erreicht db:5432.
- 127.0.0.1:8080:8081 verbindet Server-Loopback-Port 8080 mit Backend-Port 8081.
  Nginx auf demselben Server kann später http://127.0.0.1:8080 verwenden.
- /srv/postgres/data ist ein persistenter Ordner auf dem Linux-Server.
  Bei PostgreSQL 18 wird er unter /var/lib/postgresql eingebunden;
  PGDATA liegt standardmäßig darunter in 18/docker.
- restart: unless-stopped startet die Dienste nach einem Neustart wieder,
  sofern sie nicht bewusst gestoppt wurden.

Das Passwort initialisiert PostgreSQL nur bei einem leeren Datenverzeichnis.
Eine spätere Änderung der Umgebungsvariable ändert nicht das gespeicherte
Datenbankpasswort. Den Datenordner nicht zum Zurücksetzen löschen: Er enthält
die persistenten Daten.

Zum Beenden, ohne den Datenordner zu löschen:

```sh
docker compose --env-file .env.compose down
```

## Stand

Compose-Datei und Beispielkonfiguration sind vorbereitet. YAML und die Zuordnung
von Ports, Datenpfad und Zugangsdaten werden statisch geprüft.
Ein echter Compose-/Container-Laufzeittest auf dem Server steht noch aus.
Danach folgen Nginx, Port 80 und der Test über die öffentliche IP.

Quellen:
- https://hub.docker.com/_/postgres
- https://docs.docker.com/compose/how-tos/startup-order/
