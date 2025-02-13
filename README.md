Dieses Projekt zielt darauf ab, eine Android-App für die Pulsmessung zu entwickeln. Die App ist speziell für Wearables ausgelegt und ermöglicht es, die Herzfrequenzdaten eines Benutzers zu messen, anzuzeigen und zu speichern.
Der Quellcode umfasst ausschließlich die Android-App und wurde in **Android Studio Jellyfish (Version 2023.3.1)** erstellt. Die App wurde für **Android API 31+** entwickelt und richtet sich an Menschen, die ihre Gesundheit überwachen möchten.

## Pflichtenheft

### Einführung
#### Vision
Ein Wearable-Pulsmesser, der die Herzfrequenz des Benutzers über das Handgelenk misst und die Daten nahtlos in eine Smartphone-App überträgt.
#### Zielgruppe
Dieses System richtet sich an alle, die ihre Herzgesundheit oder allgemeine Fitness überwachen möchten. Es eignet sich besonders für gesundheitsbewusste Personen und Sportler.
#### Hintergrund
Die Herzfrequenz ist ein zentraler Indikator für die Gesundheit und das Wohlbefinden. Eine regelmäßige Überwachung kann dabei helfen, gesundheitliche Risiken frühzeitig zu erkennen und präventive Maßnahmen einzuleiten.
#### Referenzen
- IEEE 802.15 - Standard für drahtlose Verbindungen
- ISO 13485 – ISO-Norm für das Design und die Herstellung von Medizinprodukten


### Produktfunktionen

**PF10: Die Pulsmessung**

Das Messen der Herzfrequenz eines Menschen mithilfe eines Pulssensors, der mit einem Armband befestigt wurde.

**PF20: Speicherung der Daten**

Die gemessene Herzfrequenz wird in der Smartphone App gespeichert.
Anforderungen

### Funktionale Anforderungen

**FN10: Herzfrequenzmessung**

Kurz: Das System soll in der Lage sein, die Herzfrequenz des Benutzers zu erfassen.

Lang: Während der Benutzung soll das System die Herzfrequenz des Benutzers über seinem Handgelenk messen.

Begründung: Das ist die Hauptfunktion des Systems.

**FN20: Messergebnis darstellen**

Kurz: Darstellung der Herzfrequenz in einer Smartphone-App.

Lang: Die gemessene Herzfrequenz muss in einer Smartphone App angezeigt werden.

Begründung: Das System selbst besitzt kein Display. Zählt zu der Hauptfunktion des Systems.

**FN30: Speichern der Messdaten**

Kurz: Das System soll Messungen speichern können.

Lang: Die aktuelle Messung kann vom Benutzer gespeichert werden. Die gespeicherten Messergebnisse werden in einer separaten Seite angezeigt. Der Benutzer kann maximal 100 Messungen speichern.

Begründung: Gesundheitsrisiken erkennen

**FN40: Löschen der Messdaten**

Kurz: Das System soll Messungen löschen können.

Lang: Der Benutzer kann gespeicherte Messungen wieder löschen. Wenn die maximale Anzahl von 100 Messungen erreicht wurde, soll die älteste Messung automatisch vom System gelöscht werden.

Begründung: Speicherschonend.

**FN50: Darstellung einer Warnung**

Kurz: Das System zeigt eine Warnung an, wenn der Puls einen Grenzwert über- oder unterschreitet.

Lang: Wenn der Benutzer einen Ruhepuls unter 50 BPM oder von über 100 BPM hat, wird eine Warnung in der App angezeigt, um den Benutzer über mögliche gesundheitliche Risiken zu warnen.

Begründung: Mögliche Gesundheitsrisiken erkennen.

### Nicht funktionale Anforderungen

**NF10: Lange Laufzeit**

Kurz: Das System soll eine Laufzeit von mindestens zwei Stunden haben.

Lang: Die Laufzeit soll mindestens zwei Stunden betragen, wenn der Anwender den Pulsmesser durchgehend trägt.

Begründung: Besseres Benutzererlebnis.

**NF20: Kompakte Bauweise**

Kurz: Das System muss kompakt gebaut werden.

Lang: Das Gehäuse des Systems darf nicht die Länge und die Breite von 7cm überschreiten. Die Höhe darf maximal 5cm betragen.

Begründung: Designentscheidung

**NF30: Robuste Bauweise**

Kurz: Das System muss einen 1m Sturz aushalten.

Lang: Der Pulsmesser soll von der Hand aus 1m Höhe (+ - 5cm) fallengelassen werden können. Es muss nach diesem Sturz voll funktionsfähig sein.

Begründung: DIN EN 60068-2-31: Prüfung von Kippfallen und Umstürzen, vornehmlich für Geräte.

### Ergebnis
#### App
##### Homescreen
![HomeFragment](https://github.com/user-attachments/assets/38419d95-53ef-4aa2-9c87-ccfe345c413d)

Die Startseite bietet eine Übersicht und leitet den Benutzer durch die Funktionen der App.

##### Protocolscreen
![ProtocolFragment](https://github.com/user-attachments/assets/c9210cee-5b9b-47fc-97fe-7547cd7f8c0a)

Eine Liste der gespeicherten Messungen wird hier angezeigt.

##### Resultscreen
![ResultFragment](https://github.com/user-attachments/assets/90fbecb5-d564-44fd-8bf3-5f1975d7e861)
#### Pulsmesser
##### Außen
![image](https://github.com/user-attachments/assets/743459a3-c05e-458d-9fd9-df171adb1bd2)

##### Innen
![image](https://github.com/user-attachments/assets/425fd346-247d-4e6f-8053-4a3087f089d8)

Die Innenseite enthält den Pulssensor und die Elektronik für die Datenübertragung.
