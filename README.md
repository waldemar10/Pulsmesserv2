# Pulsmesser Projekt


## Einführung

### Vision

Ein Wearable Pulsmesser, der den Puls des Benutzers über das Handgelenk misst und die Daten in eine Smartphone App überträgt.

### Zielgruppe

Für alle Menschen, die ihre Herzgesundheit bzw. Gesundheit überwachen möchten.

### Hintergrund

Die Herzfrequenz ist ein wichtiger Indikator für die Gesundheit und das allgemeine Wohlbefinden eines Menschen. Durch eine regelmäßige Überwachung können mögliche gesundheitliche Probleme frühzeitig erkannt werden.

### Referenzen

    IEEE 802.15 - Standard für drahtlose Verbindungen
    ISO 13485 – ISO-Norm für das Design und die Herstellung von Medizinprodukten


## Produktfunktionen

PF10: Die Pulsmessung

Das Messen der Herzfrequenz eines Menschen mithilfe eines Pulssensors, der mit einem Armband befestigt wurde.

PF20: Speicherung der Daten

Die gemessene Herzfrequenz wird in der Smartphone App gespeichert.
Anforderungen

## Funktionale Anforderungen

FN10: Herzfrequenzmessung

    Kurz: Das System soll in der Lage sein, die Herzfrequenz des Benutzers zu erfassen.
    Lang: Während der Benutzung soll das System die Herzfrequenz des Benutzers über seinem Handgelenk messen.
    Begründung: Das ist die Hauptfunktion des Systems.

FN20: Messergebnis darstellen

    Kurz: Darstellung der Herzfrequenz in einer Smartphone-App.
    Lang: Die gemessene Herzfrequenz muss in einer Smartphone App angezeigt werden.
    Begründung: Das System selbst besitzt kein Display. Zählt zu der Hauptfunktion des Systems.

FN30: Speichern der Messdaten

    Kurz: Das System soll Messungen speichern können.
    Lang: Die aktuelle Messung kann vom Benutzer gespeichert werden. Die gespeicherten Messergebnisse werden in einer separaten Seite angezeigt. Der Benutzer kann maximal 100 Messungen speichern.
    Begründung: Gesundheitsrisiken erkennen

FN40: Löschen der Messdaten

    Kurz: Das System soll Messungen löschen können.
    Lang: Der Benutzer kann gespeicherte Messungen wieder löschen. Wenn die maximale Anzahl von 100 Messungen erreicht wurde, soll die älteste Messung automatisch vom System gelöscht werden.
    Begründung: Speicherschonend.

FN50: Darstellung einer Warnung

    Kurz: Das System zeigt eine Warnung an, wenn der Puls einen Grenzwert über- oder unterschreitet.
    Lang: Wenn der Benutzer einen Ruhepuls unter 50 BPM oder von über 100 BPM hat, wird eine Warnung in der App angezeigt, um den Benutzer über mögliche gesundheitliche Risiken zu warnen.
    Begründung: Mögliche Gesundheitsrisiken erkennen.

## Nicht funktionale Anforderungen

NF10: Lange Laufzeit

    Kurz: Das System soll eine Laufzeit von mindestens zwei Stunden haben.
    Lang: Die Laufzeit soll mindestens zwei Stunden betragen, wenn der Anwender den Pulsmesser durchgehend trägt.
    Begründung: Besseres Benutzererlebnis.

NF20: Kompakte Bauweise

    Kurz: Das System muss kompakt gebaut werden.
    Lang: Das Gehäuse des Systems darf nicht die Länge und die Breite von 7cm überschreiten. Die Höhe darf maximal 5cm betragen.
    Begründung: Designentscheidung

NF30: Robuste Bauweise

    Kurz: Das System muss einen 1m Sturz aushalten.
    Lang: Der Pulsmesser soll von der Hand aus 1m Höhe (+ - 5cm) fallengelassen werden können. Es muss nach diesem Sturz voll funktionsfähig sein.
    Begründung: DIN EN 60068-2-31: Prüfung von Kippfallen und Umstürzen, vornehmlich für Geräte.

## Ergebniss
### App
#### Homescreen
![HomeFragment](https://github.com/user-attachments/assets/38419d95-53ef-4aa2-9c87-ccfe345c413d)
#### Protocolscreen
![ProtocolFragment](https://github.com/user-attachments/assets/c9210cee-5b9b-47fc-97fe-7547cd7f8c0a)
#### Resultscreen
![ResultFragment](https://github.com/user-attachments/assets/90fbecb5-d564-44fd-8bf3-5f1975d7e861)
### Pulsmesser
#### Außen
![image](https://github.com/user-attachments/assets/743459a3-c05e-458d-9fd9-df171adb1bd2)
#### Innen
![image](https://github.com/user-attachments/assets/425fd346-247d-4e6f-8053-4a3087f089d8)
