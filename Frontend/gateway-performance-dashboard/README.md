# Gateway Performance Dashboard

Dieses Projekt ist ein einfaches Dashboard zur Überwachung der Leistung eines Gateways. Es ermöglicht Benutzern, langsame und schnelle Anfragen an eine Spring-Boot-Anwendung zu senden und die Antwortzeiten in einem Diagramm darzustellen.

## Projektstruktur

- `index.html`: Die Haupt-HTML-Seite der Anwendung, die die Benutzeroberfläche enthält.
- `css/style.css`: Enthält die CSS-Stile für das Layout und Design der Anwendung.
- `js/app.js`: Implementiert die Logik für die asynchronen Anfragen und die Aktualisierung des Diagramms.
- `README.md`: Dokumentation des Projekts.

## Funktionen

- **Starte 50 langsame Anfragen**: Sendet eine Anfrage an den Endpunkt `/trigger-slow-requests` und zeigt die Antwortzeiten im Diagramm an.
- **Starte 50 schnelle Anfragen**: Sendet eine Anfrage an den Endpunkt `/trigger-fast-requests` und zeigt die Antwortzeiten im Diagramm an.
- **Diagramm-Visualisierung**: Verwendet die Chart.js-Bibliothek, um die minimalen, durchschnittlichen und maximalen Antwortzeiten darzustellen.

## Anforderungen

- Die Anwendung benötigt Zugriff auf die Spring-Boot-Anwendung, die unter `http://localhost:8080` läuft.
- CORS muss in der Spring-Boot-Anwendung korrekt konfiguriert sein, um Anfragen von der Webseite zuzulassen.

## Lokale Ausführung

1. Klone das Repository oder lade die Dateien herunter.
2. Stelle sicher, dass die Spring-Boot-Anwendung läuft.
3. Öffne die `index.html`-Datei in einem Webbrowser.
4. Klicke auf die Buttons, um die Anfragen zu starten und die Ergebnisse im Diagramm anzuzeigen.

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert.