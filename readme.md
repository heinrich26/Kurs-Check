![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/heinrich26/Kurs-Check/total?style=flat&label=Downloads&labelColor=%23353C43&color=%236002EE&link=https%3A%2F%2Fgithub.com%2Fheinrich26%2FKurs-Check%2Freleases%2Ftag%2Flatest)
 [![Gradle Package](https://github.com/heinrich26/Kurs-Check/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/heinrich26/Kurs-Check/actions/workflows/gradle-build.yml)


# Kurs-Check
> Von Schülern für Schüler

Eine App, um die Kurswahl an Berliner Schulen zu erleichtern.

Entstanden als Software-Projekt des Informatik LKs 2021-2023 am Willi-Graf-Gymnasium.


<br><br><br>

## TODOs
- [x] Echtzeitinfo bei unvollständiger Eingabe
- [x] volle Ländernamen nach ISO-Standard
- [x] PFs & 5. anstelle von PKs in der Sidebar
- [x] Wahlmöglichkeit 2./3. erlauben
- [x] 5\. PK anstelle von 5. PF in der Übersicht
- [x] Name von PF5 in PK5 in CSV ändern
- [x] Beschreibung fürs/besseres Öffnen-Icon
- [x] Feld für Unterschrift & Datum im Output
- [x] AndRegel für Mathe/-CAS bei lili.json implementieren
- [x] Version für MacOS & Linux
- [x] Breite der Kursanzahlen an die Checkboxbreite im Grundkurswahl Fenster binden, damit es auch auf MacOS funzt
- [x] Eingabe wird ungültig angezeigt bei Grundkurswahl
- [x] Korrekte Umsetzung bei 5. PK (BLL)
- [x] Fremdsprache 3/4 ab E-Phase "richtig" umsetzen
- [ ] Dateiöffnung bei UNC-Pfaden fixen
- [ ] größere Fenstergröße
- [ ] Alternative Fächer anständig implementieren
- [ ] Schul-Check beim LUSD-Export
- [ ] **Trennung von GUI und Framework** (Auslagerung in Controller-Klassen?)
- [ ] Mehr Tooltips, Infos & Hilfen
- [ ] Möglichkeit 2. WPF nicht zu setzen, auch bei 2 WPFs (sollte nur die Standardeinstellung beeinflussen)
- [ ] Möglichkeit einsemestrige Kurse (bspw. Skifahrt) anzubieten

## Windows Paket GUID
Kurs-Check benutzt die GUID: `928348ff-a1f1-4e0d-881c-11256e369b08`

## Notes

- mit einer lokalen Kurswahldatei (in IntelliJ) ausführen:\
`:app:run --args="C:\Users\...\<...>.kurswahl"`
- Kurse mit `KonfliktRegel` nicht automatisch verstecken: 2-fach in `NotRegel` wrappen