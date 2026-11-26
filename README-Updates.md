# Automatische Updates für Backend und Frontend

Die Installationsreihenfolge steht in [README-Deployment.md](README-Deployment.md).
Solange kein SSH-Zugriff besteht, werden PRs, CI und Images vorbereitet. Ein Merge
installiert keinen Dienst auf der VM.

| Service | Docker-Hub-Repository | Variable | Bereitschaftsprüfung |
| --- | --- | --- | --- |
| backend | ensarsmajli/azubi-projekt | BACKEND_TAG | localhost:8080/api/markets liefert JSON-Liste |
| frontend | ensarsmajli/own-webshop-frontend | FRONTEND_TAG | localhost:8082 liefert React und /api/markets eine JSON-Liste |

Beide GitHub-Repositories veröffentlichen ihre eigenen Versionen. Ein Frontend-Tag
ändert die Backend-Version nicht. Das Skript fragt tatsächlich veröffentlichte
Registry-Tags ab: Ein Git-Tag ohne erfolgreiches Image genügt nicht. Erlaubt sind
stabile MAJOR.MINOR.PATCH-Versionen ohne v-Präfix, Vorabversion, Build-Metadaten
oder führende Nullen. 1.10.0 ist höher als 1.9.0. Höhere Major-Versionen werden
ebenfalls installiert. Veröffentlichte Versionen nicht überschreiben.

## Ablauf

scripts/update_backend.py behält seinen Namen, unterstützt aber beide Services.
So bleibt der Einstiegspunkt einer vorhandenen Service-Datei gültig. Der Dienst
relin-updater prüft zuerst backend, danach frontend:

1. Konfigurierte und laufende Image-Version abgleichen.
2. Registry mit der vorhandenen Docker-Anmeldung abfragen, einschließlich aller Tag-Seiten.
3. Die höchste stabile Version auswählen, sofern sie höher als die aktuelle ist.
4. Nur das betroffene Image herunterladen und nur diesen Container neu erstellen.
5. Bis zu fünf Minuten auf die jeweilige Bereitschaftsprüfung warten.
6. Erst bei Erfolg die zugehörige Versionsvariable atomar in .env.compose speichern.
   Die andere Versionsvariable und das Passwort bleiben erhalten; Dateirechte 0600.
7. Bei Startfehlern versuchen, den vorherigen Container desselben Services wiederherzustellen.
8. Nach dem Durchlauf zehn Sekunden warten. Downloads und Starts laufen seriell
   und können länger dauern. Ein Service-Fehler verhindert die Prüfung des anderen nicht.

Eine Sperrdatei verhindert parallele Updater. Exportierte BACKEND_TAG- und
FRONTEND_TAG-Variablen werden ignoriert; maßgeblich ist die private .env.compose.
Bei einer Abweichung zwischen laufendem Image und eingestellter Version wird für
diesen Service kein Update ausgeführt. Den Zustand zuerst manuell abgleichen.

## Aufrufe nach der späteren Servereinrichtung

```sh
# Ohne Container oder Versionsdatei zu ändern:
python3 scripts/update_backend.py --check
python3 scripts/update_backend.py --check --service frontend
# Einmalig prüfen und bei Bedarf aktualisieren:
python3 scripts/update_backend.py --once --service backend
# Dauerbetrieb für beide Services, normalerweise über systemd:
python3 scripts/update_backend.py
```

Vor manuellen Aufrufen einen laufenden Updater stoppen, sonst blockiert die
Sperrdatei den zweiten Prozess. --check legt nur diese Sperrdatei an.

```sh
sudo systemctl stop relin-updater.service
sudo journalctl -u relin-updater.service -n 50 --no-pager
sudo systemctl enable --now relin-updater.service
```

Der Dienst läuft als smajliensar7 in /home/smajliensar7/Azubi-Projekt. Bei anderen
Benutzern/Pfaden die Service-Datei anpassen. Er nutzt dieselbe Docker-Anmeldung
wie Compose; Python 3 genügt, pip-Pakete sind nicht erforderlich. Falls nötig als
derselbe Benutzer erneut `docker login --username ensarsmajli` ausführen. Tokens
gehören nicht ins Skript oder in Git. enable aktiviert den Start beim Booten.
Docker muss gestartet und für den Benutzer ohne sudo zugänglich sein.
Updates des Skripts selbst erfolgen per git pull und Dienst-Neustart.

## Grenzen und Validierung

Containerwechsel verursachen eine Unterbrechung. Die Wiederherstellung eines
Images macht keine Flyway-Migrationen rückgängig; Migrationen müssen
rückwärtskompatibel sein. Fehlerhafte Releases werden erneut versucht. Mit
`sudo systemctl disable --now relin-updater.service` den Updater bei Bedarf
anhalten; laufende Container bleiben bestehen. Nach einer Unterbrechung während
eines Deployments gegebenenfalls Image und Versionsdatei manuell abgleichen.
Der Updater löscht keine Datenbankdaten und aktualisiert weder PostgreSQL noch Nginx.

```sh
python3 -m unittest discover -s tests -v
```

Die Tests simulieren Registry und Docker, einschließlich getrennter Versionsstände,
Containerwechsel, Fehlern und Wiederherstellung. CI prüft zusätzlich Compose und
die Host-Nginx-Konfiguration. Echte Downloads, Versionswechsel, Port-80-Zugriff
und Start nach VM-Reboot werden später auf der VM geprüft. Das abgelaufene
HTTPS-Zertifikat bleibt separat zu behandeln.

Quellen:
- https://semver.org/
- https://distribution.github.io/distribution/spec/api/
- https://docs.docker.com/reference/api/registry/auth/
- https://docs.docker.com/reference/cli/docker/login/
- https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html
