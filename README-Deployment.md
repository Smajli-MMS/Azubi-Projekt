# Gemeinsame Bereitstellung: jetzt vorbereiten, später per SSH aktivieren

## Jetzt ohne SSH möglich

1. Frontend-PR für den Docker-Build und den Fix der Nginx-Startprüfung nach grüner
   CI mergen. Im Frontend-Repository sind DOCKERHUB_USERNAME (Variable: ensarsmajli)
   und DOCKERHUB_TOKEN (Secret mit Schreibrechten) erforderlich.
2. Auf Docker Hub das Zielrepository ensarsmajli/own-webshop-frontend anlegen,
   falls es noch nicht existiert. Sichtbarkeit nach Projektvorgabe wählen.
3. Im Frontend-Projekt auf dem Laptop die erste freie stabile Version veröffentlichen:

   ```sh
   git switch main
   git pull --ff-only origin main
   git tag --list
   git tag -a 1.0.0 -m "Frontend Release 1.0.0"
   git push origin 1.0.0
   ```

   1.0.0 nur verwenden, wenn der Tag noch frei ist. Vorhandene Tags nicht verschieben.
   Die CI einschließlich Docker-Push muss erfolgreich sein. Bei einem anderen Tag
   später FRONTEND_TAG entsprechend setzen.
4. Den PR für die gemeinsame Compose-/Updater-Konfiguration nach erfolgreicher
   CI mergen und das Backend-Projekt auf dem Laptop per git pull aktualisieren.
   Das vorhandene Backend-Image 1.0.0 kann weiter verwendet werden: Änderungen
   am Host-Updater benötigen keinen neuen Backend-JAR-Build.

Damit sind Dateien und Images vorbereitet. Die VM behält bis zur folgenden
Einrichtung ihren bisherigen Zustand. Auf ihr wurden die neuen Dateien noch
nicht installiert oder getestet.

## Später mit SSH: Container vorbereiten

Alle folgenden Befehle gehören auf die VM. Falls relin-updater.service schon
installiert ist, zuerst `sudo systemctl stop relin-updater.service` ausführen.
Falls er noch nicht existiert, diesen einzelnen Schritt überspringen.

```sh
cd ~/Azubi-Projekt
git pull --ff-only origin main
nano .env.compose
```

Die vorhandene private Datei um FRONTEND_TAG ergänzen. Beispiel:

```dotenv
BACKEND_TAG=1.0.0
FRONTEND_TAG=1.0.0
```

Die tatsächlich verwendete Backend-Version übernehmen, falls sie inzwischen höher
ist, und eine veröffentlichte Frontend-Version einsetzen. Das Datenbankpasswort
beibehalten. .env.compose.example nicht über die private Datei kopieren.
Speichern mit Strg+O, Enter, Strg+X.

```sh
chmod 600 .env.compose
docker compose --env-file .env.compose config --quiet
docker compose --env-file .env.compose pull
docker compose --env-file .env.compose up -d
docker compose --env-file .env.compose ps
```

Falls der Pull-Zugriff fehlt, als smajliensar7 mit `docker login --username ensarsmajli`
anmelden. Den Token ausschließlich am Passwort-Prompt eingeben. Die Datenbank
liegt weiter im persistenten Ordner /srv/postgres/data. Auf den vollständigen
Spring-Boot-Start warten und dann prüfen:

```sh
curl --fail --max-time 30 http://127.0.0.1:8080/api/markets
curl --fail --max-time 30 http://127.0.0.1:8082/
curl --fail --max-time 30 http://127.0.0.1:8082/api/markets
```

Erwartet: Backend-JSON, Frontend-HTML und Backend-JSON über den Frontend-Proxy.
Bei Fehlern vor der Nginx-Umschaltung die Protokolle prüfen:

```sh
docker compose --env-file .env.compose logs --tail=100 backend frontend
```

## Später mit SSH: Host-Nginx umstellen

Die vorbereitete Datei ersetzt die bisherige relin.conf. In der alten
/etc/nginx/sites-available/default darf Port 80 nicht zusätzlich als default_server
eingetragen sein, wie bereits bei der ursprünglichen Einrichtung korrigiert.

```sh
sudo cp -a /etc/nginx/conf.d/relin.conf /etc/nginx/relin.conf.before-frontend
sudo install -m 644 deploy/nginx/relin.conf /etc/nginx/conf.d/relin.conf
sudo /usr/sbin/nginx -t && sudo systemctl reload nginx
curl --fail --max-time 30 http://127.0.0.1:80/
curl --fail --max-time 30 http://127.0.0.1:80/api/markets
```

Erwartet: Webseite und Markt-Liste. Verbindung: Host-Port 80 -> Host-Nginx ->
Frontend-Port 8082 -> Frontend-Nginx; /api/ geht im Compose-Netzwerk weiter an
backend:8081. Die Host-Ports 8080 und 8082 bleiben an Loopback gebunden.

Vom Laptop `http://EXTERNE-IP/` und `http://EXTERNE-IP/api/markets` prüfen;
gegebenenfalls die zur VM passende Google-Cloud-Firewall für TCP 80 prüfen.
Die IP steht in Compute Engine -> VM-Instanzen. Die bestehende Domain leitet
HTTP weiterhin auf HTTPS um. Deren abgelaufenes Zertifikat und Weiterleitung
auf das Frontend werden separat eingerichtet.

Bei Bedarf die vorherige Host-Nginx-Konfiguration wiederherstellen:

```sh
sudo cp -a /etc/nginx/relin.conf.before-frontend /etc/nginx/conf.d/relin.conf
sudo /usr/sbin/nginx -t && sudo systemctl reload nginx
```

## Später mit SSH: Autostart für beide Anwendungen aktivieren

Beide Container müssen laufen und zu ihren Versionsvariablen passen. Der Updater
darf während des manuellen Checks noch nicht laufen:

```sh
python3 scripts/update_backend.py --check
sudo install -m 644 deploy/relin-updater.service /etc/systemd/system/relin-updater.service
sudo systemctl daemon-reload
sudo systemctl enable --now relin-updater.service
sudo systemctl status relin-updater.service --no-pager
sudo journalctl -u relin-updater.service -n 50 --no-pager
```

Bei fehlgeschlagenem Check zuerst die Ursache beheben. Die Service-Datei ist für
smajliensar7 und /home/smajliensar7/Azubi-Projekt vorbereitet. Der bestehende
Dienstname relin-updater bleibt erhalten, geprüft werden jetzt beide Anwendungen.

## Späterer Abschlusstest

Auf dem Laptop im jeweiligen Repository einen höheren freien Git-Tag wie 1.0.1
erstellen und pushen. Backend und Frontend einzeln testen. Erfolgreichen Image-Push
in GitHub Actions abwarten. Auf der VM beobachten:

```sh
sudo journalctl -u relin-updater.service -f
```

Strg+C beendet die Ansicht, nicht den Dienst. Anschließend:

```sh
docker compose --env-file .env.compose ps
grep -E '^(BACKEND_TAG|FRONTEND_TAG)=' .env.compose
curl --fail --max-time 30 http://127.0.0.1:80/
curl --fail --max-time 30 http://127.0.0.1:80/api/markets
```

Bei einem verabredeten VM-Neustart zusätzlich prüfen, ob Container, Nginx und
Updater selbstständig wieder starten. Diese Anleitung löst keinen Neustart aus.
Details und Grenzen: [README-Updates.md](README-Updates.md).
