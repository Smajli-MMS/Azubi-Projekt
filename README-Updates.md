# Automatische Updates auf stabile Versionen

Ein Git-Tag wie `1.0.1` startet die CI. Erst nach erfolgreichen Prüfungen wird
`ensarsmajli/azubi-projekt:1.0.1` auf Docker Hub veröffentlicht. Der Updater
prüft direkt die veröffentlichten Registry-Tags und berücksichtigt ausschließlich
stabile Versionen im Format MAJOR.MINOR.PATCH ohne führende Nullen und ohne v-Präfix.
Vorabversionen, Build-Metadaten, latest und SHA-Tags werden ignoriert.
Der Vergleich ist numerisch: 1.10.0 ist höher als 1.9.0. Auch höhere Major-Versionen
werden automatisch installiert; Releases müssen deshalb für den Server geeignet sein.
Ein schon veröffentlichtes Versions-Tag darf nicht mit anderem Inhalt überschrieben
werden. Änderungen bekommen immer eine höhere Versionsnummer.

## Einrichtung auf der bestehenden Trainings-VM

Diese Befehle sind für den Benutzer `smajliensar7` und das Verzeichnis
`/home/smajliensar7/Azubi-Projekt` vorbereitet. Bei anderen Benutzern/Pfaden müssen
User, WorkingDirectory und ExecStart in deploy/relin-updater.service angepasst werden.
Python 3, CA-Zertifikate, Docker und das Compose-Plugin müssen installiert sein.
Der Benutzer muss Docker ohne sudo ausführen dürfen, wie beim bisherigen Compose-Start.

Nach dem Merge des PRs im SSH-Terminal:

```sh
cd ~/Azubi-Projekt
git pull --ff-only origin main
python3 --version
docker compose version
grep '^BACKEND_TAG=' .env.compose
docker compose --env-file .env.compose config --quiet
docker compose --env-file .env.compose ps
python3 scripts/update_backend.py --check
```

BACKEND_TAG muss bereits auf `1.0.0` oder einer anderen veröffentlichten stabilen
Version stehen. Der laufende Container muss dazu passen. Die vorhandene private
Datei .env.compose mit dem Datenbankpasswort wird beim Pull nicht überschrieben.
Das Skript verwendet die vorhandene Docker-Anmeldung desselben Benutzers aus
~/.docker/config.json oder dem dort konfigurierten Credential Helper.
Falls der Zugriff fehlschlägt, als dieser Benutzer erneut anmelden:

```sh
docker login --username ensarsmajli
```

Den persönlichen Docker-Hub-Token am Passwort-Prompt eingeben. Keinen Token in
Skript oder Service-Datei schreiben. `--check` prüft einmal, ohne Container oder
Versionskonfiguration zu ändern. Es wird lediglich die lokale Sperrdatei angelegt.

Wenn der Check erfolgreich ist, den Dienst installieren und aktivieren:

```sh
sudo install -m 644 deploy/relin-updater.service /etc/systemd/system/relin-updater.service
sudo systemctl daemon-reload
sudo systemctl enable --now relin-updater.service
sudo systemctl status relin-updater.service --no-pager
sudo journalctl -u relin-updater.service -n 30 --no-pager
```

`enable` aktiviert den Start beim Hochfahren, `--now` startet den Dienst sofort.
Der Dienst läuft unter dem bisherigen Docker-Benutzer und verwendet dessen
Docker-Anmeldung. Das Python-Skript benötigt keine zusätzlichen pip-Pakete.
Einmal installiert, lädt dieser Dienst nicht selbst den Quellcode von GitHub:
Spätere Änderungen am Updater müssen separat per git pull übernommen und mit
`sudo systemctl restart relin-updater.service` aktiviert werden.

## Ablauf

1. Compose-Konfiguration und laufende Image-Version prüfen. Bei Abweichungen wird
   kein Update ausgeführt; die Versionen müssen zuerst manuell abgeglichen werden.
2. Docker-Hub-Authentifizierung und alle Seiten der Registry-Tagliste abrufen.
3. Die höchste stabile Version auswählen, sofern sie höher als BACKEND_TAG ist.
4. Das neue Backend-Image herunterladen. Bei Downloadfehlern läuft der alte Container weiter.
5. Nur den Backend-Container mit Compose neu erstellen. PostgreSQL wird nicht neu erstellt.
6. Bis zu fünf Minuten auf eine gültige Antwort von /api/markets auf Port 8080 warten.
7. Bei Erfolg BACKEND_TAG atomar in .env.compose speichern, mit Dateirechten 0600.
   Passwort und andere Einstellungen bleiben erhalten. Bei einem Fehler wird versucht,
   den vorherigen Backend-Container wiederherzustellen.
8. Zehn Sekunden warten, dann erneut prüfen. Während Download und Start läuft kein
   zweiter Check. Netzwerkfehler werden beim nächsten Durchlauf erneut versucht.

Die Container-Neuerstellung verursacht eine kurze Unterbrechung. Auf der kleinen
Trainings-VM kann der Spring-Boot-Start mehrere Minuten dauern. Der Updater prüft
die Anwendung direkt; die separate Nginx-Prüfung erfolgt über Port 80.
Eine Sperrdatei verhindert parallele Updater. Bei Registry-Fehlern werden keine
Versionen geändert. Wiederholt fehlgeschlagene Releases werden erneut versucht;
den Dienst zur Untersuchung stoppen. Das Wiederherstellen des alten Images macht
keine Datenbankmigration rückgängig: Migrationen müssen rückwärtskompatibel sein.
Nach einer Unterbrechung während eines Deployments gegebenenfalls laufendes Image
und BACKEND_TAG manuell abgleichen, bevor der Dienst wieder aktiviert wird.

## Ersten automatischen Wechsel gemeinsam testen

Auf dem Laptop im aktuellen Backend-Projekt nach erfolgreichem Merge und Pull:

```sh
git switch main
git pull --ff-only origin main
git tag -a 1.0.1 -m "Release 1.0.1"
git push origin 1.0.1
```

Nur verwenden, wenn 1.0.1 noch nicht existiert; sonst eine höhere freie Version wählen.
In GitHub Actions den erfolgreichen Build und Docker-Push abwarten. Danach auf der VM:

```sh
sudo journalctl -u relin-updater.service -f
```

Die Live-Ansicht mit Strg+C verlassen; der Dienst läuft weiter. Anschließend:

```sh
cd ~/Azubi-Projekt
docker compose --env-file .env.compose ps
grep '^BACKEND_TAG=' .env.compose
curl --fail --max-time 30 http://127.0.0.1:80/api/markets
```

Erwartet: Image und BACKEND_TAG stehen auf 1.0.1, und die Markt-Liste kommt über
Nginx zurück. Ein bloßer Git-Tag ohne erfolgreich veröffentlichtes Image löst kein
Deployment aus. Den Start beim Hochfahren bei Gelegenheit mit einem abgesprochenen
VM-Neustart und anschließendem systemctl status prüfen.

## Stoppen und Diagnose

```sh
sudo systemctl disable --now relin-updater.service
docker compose --env-file .env.compose logs --tail=100 backend
```

Der Dienststopp lässt die bereits gestarteten Container weiterlaufen. Für einen
manuellen Versionswechsel bei gestopptem Updater BACKEND_TAG in .env.compose ändern,
dann `docker compose --env-file .env.compose pull backend` und
`docker compose --env-file .env.compose up -d --no-deps backend` ausführen.
Anschließend die API testen und den Dienst bei Bedarf wieder aktivieren.
Passwörter und Tokens beim Teilen von Ausgaben nicht veröffentlichen.

## Prüfung vor dem Serverbetrieb

```sh
python3 -m unittest discover -s tests -v
```

Diese automatisierten Tests simulieren Registry und Docker. Der tatsächliche
Registry-Zugriff, der automatische Containerwechsel und der Start nach VM-Reboot
müssen bei der Einrichtung auf der VM geprüft werden.

Quellen:
- https://semver.org/
- https://distribution.github.io/distribution/spec/api/
- https://docs.docker.com/reference/api/registry/auth/
- https://docs.docker.com/reference/cli/docker/login/
- https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html
