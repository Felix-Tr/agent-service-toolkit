## Testbeschreibung

Für die 3 Beispielkreuzung werden die ConnectionIds den entsprechenden Kriterien zugeordnet
die anschlagen sollen.
Wichtig ist, dass diese erkannt werden was YZie

|                                  | **Regel löst nicht aus** | **Regel löst aus** |
|----------------------------------|---------------------------------------|---------------------------------|
| **Tatsächlich: Prüfung negativ** | True Negative (TN)                     | False Positive (FP)             |
| **Tatsächlich: Prüfung positiv** | False Negative (FN)                    | True Positive (TP)              |


Ein False Negative (FN) – also das Nichterkennen eines tatsächlichen Konfliktpotentials für eine Fahrlinie birgt ein 
hohes Sicherheitsrisiko und darf nie Auftreten! 
Deswegen wird sich darauf in den Testfällen konzentriert.

### 644

CheckDiagonalGruen: 19, 4

##### Unsicherheit: 

Schwerlastverkehr in HR wäre zusätzliches Kriterium und würde 19, 4 plus 3, 14 betreffen .

### 752

CheckVollscheibeMitPfeil: 1,2, 28
CheckRechtsabbiegehilfsignal: 27

##### Unsicherheit:

Unsicherheit für S -> O:
RW benutzungspflichtig nach Regel daher kein Problem.
Aber große Kreuzung heißt auf Ausfahrt für mIV kein Mischverkehr erlaubt.
Wenn die Ausfahrt trotzdem mit einbezogen werden soll zusätzlich (S -> O, N -> W):

CheckVollscheibeMitPfeil: 18, 17
CheckDiagonalGruen: 18, 17, 6, 7, (29)

### 1040

CheckVollscheibeMitPfeil: 25, 26, 33

##### Unsicherheit:

Falls Unsicherheit wie bei 752 mit abzudecken ist, zusätzlich:

CheckDiagonalGruen: 18, 5, 27, 32, 13