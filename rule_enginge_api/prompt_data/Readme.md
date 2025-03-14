
<p align="center">
  <img src="images/405845937-f08a57cb-d90a-4edc-a298-c1f7d99c2942.png" alt="Abstract Illustration of a Modern Urban Traffic Intersection" width="50%">
</p>




# Notice

This readme was the brainstorming script, which can be read for more details. The short challenge descriptions are in __challenge_descriptions_(german|english).md_ which should bring everybody up to speed. The text below switches to german at some pont because of the terminology and easier explanation.

## Introduction

In the current state of AI development, one of the most frustrating challenges is the variability in responses provided by large language models (LLMs), such as GPT-4 and other frontier models. This inconsistency—commonly referred to as the "hallucination problem"—poses a significant issue when reliable, deterministic outputs are critical. The problem becomes particularly troubling in domains like the legal environment and the public sector, where outputs must meet stringent requirements for "Rechtssicherheit" (legal certainty) and "Verhältnismäßigkeit" (proportionality).

Legal certainty ensures that AI-generated responses can withstand scrutiny in court, while proportionality demands that actions taken based on these outputs are balanced, justifiable, and compliant with overarching laws and regulations.
In public sector operations, employees are tasked with applying applicable laws and guidelines to fulfill their responsibilities. This process requires a deep understanding of specific legal provisions, careful consideration of jurisdictional nuances, and an ability to account for political decisions or communal directives. For example:

- A referendum mandating the expansion of bicycle lanes may necessitate removing a car lane, even if this results in a traffic jam, as long as legal documentation supports this outcome.
- Legal frameworks and local decisions must guide actions to ensure accountability and compliance.

This level of decision-making demands deterministic, contextually accurate, and legally safe outputs from AI systems. However, the inherent probabilistic nature of current LLMs often fails to deliver such reliability, highlighting gaps in their design for high-stakes, legally sensitive environments.

# Problem statement

With this limitations in mind here is my challenge for you. I cureently work as a traffic engineer and project manager for the mobility department at the city of munich, more specific we are responsable for programming and planning traffic light systems. The challenge I'm giving you is rather simple learnable for a human, has a steep learning curve and always needs experience from the real world, their reactions and review by a human. Still, many preperation work could be done by an llm agent. The data that can be provided is rather unstructered, and the team who wants to do the challenge should also think about a data model for the llm agents to query based on the given information. In general, the search and retrieve mechanisms are important as the legal & guideline framework is large and so are the examples. Every example is individual or single in its form because of a local variablitly in how a cross section is designed and we will focus on one type of legal verification. The topic is about getting a specific traffic sign for cyclists to a cross section. From here on I will switch to german as the terminiology gets specfic to german.

Wir sind im Mobilitätsreferat angesiedelt und im speziellen für Ampeln, bei uns Lichtsignalanlagen genannt, zuständig. Uns erreichen viele Bürgeranfragen zu Änderungswünschen im Kreuzungsbereich dieser Anlagen welche wir dann prüfen und das bindet Zeit und Ressourcen. Eine Vorprüfung und Bewertung der Anfrage würde Zeit und auch mehr Objektivität bei der Bewertung ermöglichen, wenn die Bewertung von Sprachmodellen konsistent genug sind. Ich würde mich auf eine häufig auftretende Anfrage beschränken bei welcher ich gespannt bin, auf welche Methoden die Studierenden kommen um eine solche Bearbeitung erleichtern. Der Beispiel plan in den generierten Challenge Beschreibungen in Englisch und Deutsch sind als Orientierung gedacht.

Uns erreicht oft die Anfrage einen Grünpfeilschild für Radfahrer an einer bestimmten Stelle anzuordnen, das ganze läuft so ab:

Der Sachbearbeiter bekommt eine Anfrage, das für einen oder mehrere Knotenpunkte geprüft werden soll ob ein Rechtsabbiege Grünpfeil für Radfahrer an einer bestimmten Fahrbeziehung einer Kreuzung möglich ist. Ein Bild des Verkehrszeichens VZ 721 seht ihr hier:

<p align="center">
  <img src="images/406058830-6c0a0438-d9c3-48f1-8c34-f3dbcba0b6a3.png" alt="VZ 721" width="30%">
</p>

Hierfür müssen bei uns für jede Fahbeziehung der Kreuzung, Einmündung usw. mit einer Lichtzeichen- / Lichtsignalanlage diverse Kriterien geprüft werden. Lichtzeichen ist der verkehrsrechtlich korrekte begriff, der in der Verwaltung equivalent benutze Begriff ist Lichtsignalanlagen. Dies geschieht über eine Bewertung verschiedener Gegebenheiten, auf welchen basierend ein Grünfpeil angeordnet werden kann oder es ist aufgrund von Kriterium X nicht möglich.

Die Rechtsgrundlage für eine Bearbeitung ist die STVO (§37 Abs. 2) & VwV-StVo (VwV-StVO zu § 37 zu den Nummern 1 und 2) und für eine Bewertung muss ein gewisses Grundverständnis für den Kreuzungsbereich vorhanden sein, alles Dinge die beim prompt engineering mit reinkommen und es müsste mulitmodal, bspw. basierend auf Lageplänen oder eigenen Bildern vor Ort gearbeitet werden. 
Daten die gebraucht werden, können im Zuge des Hackathons von den Studierenden beim Challengegeber angefragt werden und er versucht sie bereitzustellen.

Unter [Relevanter Kontext](#Relevanter-Kontext) sind die zugehörigen relevanten Richtlinien, Gesetzte und Verwaltungsvorschriften zu finden.

# Relevanter Kontext

Als Verkehrsplaner*in / Verkehrsingenieur*in spezialisiert auf Lichtsignalanlagen ist man verantwortlich für die Planung von Bearbeitung von Anfragen an Ampeln. Die Grundlage auf welcher Planungen umgesetzt werden und auch Verkehrsingeursarbeitsplatzsoftware programmiert wird ist die Straßenverkehrsordnung, ihre Verwaltungsvorschrift wie bereits erwähnt. Zusätzlich stellen die Dokumente der Forschungsgesellschaft FSGV, speziell die Richtlinie für Lichtsignalanlagen (RiLSA) die genauen Anforderung an die Planung eines signalisiserten Kreuzungsbereichs, die Programmierung und Sicherheits dieser Anlagen. Dies ist für diese spezielle Anfrage um die es bei der Challenge geht nicht so relevant, aber sollte mal genannt worden sein.

- Die Straßenverkehrsordnung (StVO)ist öffentlich und existiert [hier](https://www.gesetze-im-internet.de/stvo_2013/BJNR036710013.html)
- Die Verwaltungsvorschrift der STVO (VwV-StVO) [hier](https://www.verwaltungsvorschriften-im-internet.de/bsvwvbund_26012001_S3236420014.htm)

Durch Oberlandesgerichtsurteile, Anmerkungen & Erläuterungen zur STVO in Fachpublikationen oder von Expertenaussagen findet manchmal noch eine ausgibiege Recherche oder Unterstützung der Rechtsabteilung statt. Ein Beispiel wäre z.B. die Abgrenzung zwischen Radwegen welche noch signalisiert werden müssen oder unabhängig geführt werden, bspw. LSA 251 Elsenheimer-/Westendstr. Abbiegen möglich ohne Schilder möglich aufgrund der baulichen Gestaltung des Radwegs, da in diesem Fall der Wirkungsbereich der Signalisierung nicht mehr rechtlich wirksam ist.

### Auszug des Paragraphen § 37 welcher für die Anfrage relevant ist

<details>
<summary>Click to expand</summary>

§ 37 Wechsellichtzeichen, Dauerlichtzeichen und Grünpfeil
(1) Lichtzeichen gehen Vorrangregeln und Vorrang regelnden Verkehrszeichen vor. Wer ein Fahrzeug führt, darf bis zu 10 m vor einem Lichtzeichen nicht halten, wenn es dadurch verdeckt wird.
(2) Wechsellichtzeichen haben die Farbfolge Grün – Gelb – Rot – Rot und Gelb (gleichzeitig) – Grün. Rot ist oben, Gelb in der Mitte und Grün unten.

1.An Kreuzungen bedeuten:

Grün: „Der Verkehr ist freigegeben“.

Er kann nach den Regeln des § 9 abbiegen, nach links jedoch nur, wenn er Schienenfahrzeuge dadurch nicht behindert.

Grüner Pfeil: „Nur in Richtung des Pfeils ist der Verkehr freigegeben“.

Ein grüner Pfeil links hinter der Kreuzung zeigt an, dass der Gegenverkehr durch Rotlicht angehalten ist und dass, wer links abbiegt, die Kreuzung in Richtung des grünen Pfeils ungehindert befahren und räumen kann.

Gelb ordnet an: „Vor der Kreuzung auf das nächste Zeichen warten“.

Keines dieser Zeichen entbindet von der Sorgfaltspflicht.

Rot ordnet an: „Halt vor der Kreuzung“.

Nach dem Anhalten ist das Abbiegen nach rechts auch bei Rot erlaubt, wenn rechts neben dem Lichtzeichen Rot ein Schild mit grünem Pfeil auf schwarzem Grund (Grünpfeil) angebracht ist. Durch das Zeichen

wird der Grünpfeil auf den Radverkehr beschränkt.
Wer ein Fahrzeug führt, darf nur aus dem rechten Fahrstreifen abbiegen. Soweit der Radverkehr die Lichtzeichen für den Fahrverkehr zu beachten hat, dürfen Rad Fahrende auch aus einem am rechten Fahrbahnrand befindlichen Radfahrstreifen oder aus straßenbegleitenden, nicht abgesetzten, baulich angelegten Radwegen abbiegen. Dabei muss man sich so verhalten, dass eine Behinderung oder Gefährdung anderer Verkehrsteilnehmer, insbesondere des Fußgänger- und Fahrzeugverkehrs der freigegebenen Verkehrsrichtung, ausgeschlossen ist.
Schwarzer Pfeil auf Rot ordnet das Halten, schwarzer Pfeil auf Gelb das Warten nur für die angegebene Richtung an.

Ein einfeldiger Signalgeber mit Grünpfeil zeigt an, dass bei Rot für die Geradeaus-Richtung nach rechts abgebogen werden darf.

2. An anderen Straßenstellen, wie an Einmündungen und an Markierungen für den Fußgängerverkehr, haben die Lichtzeichen entsprechende Bedeutung.

3. Lichtzeichenanlagen können auf die Farbfolge Gelb-Rot beschränkt sein.

4. Für jeden von mehreren markierten Fahrstreifen (Zeichen 295, 296 oder 340) kann ein eigenes Lichtzeichen gegeben werden. Für Schienenbahnen können besondere Zeichen, auch in abweichenden Phasen, gegeben werden; das gilt auch für Omnibusse des Linienverkehrs und nach dem Personenbeförderungsrecht mit dem Schulbus-Zeichen zu kennzeichnende Fahrzeuge des Schüler- und Behindertenverkehrs, wenn diese einen vom übrigen Verkehr freigehaltenen Verkehrsraum benutzen; dies gilt zudem für Krankenfahrzeuge, Fahrräder, Taxen und Busse im Gelegenheitsverkehr, soweit diese durch Zusatzzeichen dort ebenfalls zugelassen sind.

5. Gelten die Lichtzeichen nur für zu Fuß Gehende oder nur für Rad Fahrende, wird das durch das Sinnbild „Fußgänger“ oder „Radverkehr“ angezeigt. Für zu Fuß Gehende ist die Farbfolge Grün-Rot-Grün; für Rad Fahrende kann sie so sein. Wechselt Grün auf Rot, während zu Fuß Gehende die Fahrbahn überschreiten, haben sie ihren Weg zügig fortzusetzen.

6. Wer ein Rad fährt, hat die Lichtzeichen für den Fahrverkehr zu beachten. Davon abweichend sind auf Radverkehrsführungen die besonderen Lichtzeichen für den Radverkehr zu beachten. An Lichtzeichenanlagen mit Radverkehrsführungen ohne besondere Lichtzeichen für Rad Fahrende müssen Rad Fahrende bis zum 31. Dezember 2016 weiterhin die Lichtzeichen für zu Fuß Gehende beachten, soweit eine Radfahrerfurt an eine Fußgängerfurt grenzt.
(3) Dauerlichtzeichen über einem Fahrstreifen sperren ihn oder geben ihn zum Befahren frei.

Rote gekreuzte Schrägbalken ordnen an:

„Der Fahrstreifen darf nicht benutzt werden“.

Ein grüner, nach unten gerichteter Pfeil bedeutet:

„Der Verkehr auf dem Fahrstreifen ist freigegeben“.

Ein gelb blinkender, schräg nach unten gerichteter Pfeil ordnet an:

„Fahrstreifen in Pfeilrichtung wechseln“.
(4) Wo Lichtzeichen den Verkehr regeln, darf nebeneinander gefahren werden, auch wenn die Verkehrsdichte das nicht rechtfertigt.
(5) Wer ein Fahrzeug führt, darf auf Fahrstreifen mit Dauerlichtzeichen nicht halten.
Nichtamtliches Inhaltsverzeichnis

</details>




### Der Ausschnitt der VwV-StVo zu § 37:


<details>
<summary>Click to expand</summary>

Zu § 37 Wechsellichtzeichen, Dauerlichtzeichen und Grünpfeil
1	
Die Gleichungen der Farbgrenzlinien in der Farbtafel nach DIN 6163 Blatt 5 sind einzuhalten.
 
Zu Absatz 1

2. So bleiben z. B. die Zeichen 209 ff. "Vorgeschriebene Fahrtrichtung" neben Lichtzeichen gültig, ebenso die die Benutzung von Fahrstreifen regelnden Längsmarkierungen (Zeichen 295, 296, 297, 340).
 
Zu Absatz 2
3	
I.
Die Regelung des Verkehrs durch Lichtzeichen setzt eine genaue Prüfung der örtlichen Gegebenheiten baulicher und verkehrlicher Art voraus und trägt auch nur dann zu einer Verbesserung des Verkehrsablaufs bei, wenn die Regelung unter Berücksichtigung der Einflüsse und Auswirkungen im Gesamtstraßennetz sachgerecht geplant wird. Die danach erforderlichen Untersuchungen müssen von Sachverständigen durchgeführt werden.
 
4	
II.
Wechsellichtzeichen dürfen nicht blinken, auch nicht vor Farbwechsel.
 
5	
III.
Die Lichtzeichen sind rund, soweit sie nicht Pfeile oder Sinnbilder darstellen. Die Unterkante der Lichtzeichen soll in der Regel 2,10 m und, wenn die Lichtzeichen über der Fahrbahn angebracht sind, 4,50 m vom Boden entfernt sein.
 
6	
IV.
Die Haltlinie (Zeichen 294) sollte nur so weit vor der Lichtzeichenanlage angebracht werden, daß die Lichtzeichen aus einem vor ihr wartenden Personenkraftwagen noch ohne Schwierigkeit beobachtet werden können (vgl. aber Nummer III 3 zu § 25; Rn. 5). Befindet sich z. B. die Unterkante des grünen Lichtzeichens 2,10 m über einem Gehweg, so sollte der Abstand zur Haltlinie 3,50 m betragen, jedenfalls über 2,50 m. Sind die Lichtzeichen wesentlich höher angebracht oder muß die Haltlinie in geringerem Abstand markiert werden, so empfiehlt es sich, die Lichtzeichen verkleinert weiter unten am gleichen Pfosten zu wiederholen.
 
Zu den Nummern 1 und 2
7	
I.
An Kreuzungen und Einmündungen sind Lichtzeichenanlagen für den Fahrverkehr erforderlich,

1. wo es wegen fehlender Übersicht immer wieder zu Unfällen kommt und es nicht möglich ist, die Sichtverhältnisse zu verbessern oder den kreuzenden oder einmündenden Verkehr zu verbieten,
 
8		

2. wo immer wieder die Vorfahrt verletzt wird, ohne daß dies mit schlechter Erkennbarkeit der Kreuzung oder mangelnder Verständlichkeit der Vorfahrtregelung zusammenhängt, was jeweils durch Unfalluntersuchungen zu klären ist,
 
9		

3. wo auf einer der Straßen, sei es auch nur während der Spitzenstunden, der Verkehr so stark ist, daß sich in den wartepflichtigen Kreuzungszufahrten ein großer Rückstau bildet oder einzelne Wartepflichtige unzumutbar lange warten müssen.
 
10	
II.
Auf Straßenabschnitten, die mit mehr als 70 km/h befahren werden dürfen, sollen Lichtzeichenanlagen nicht eingerichtet werden; sonst ist die Geschwindigkeit durch Zeichen 274 in ausreichender Entfernung zu beschränken.
 
11	
III.
Bei Lichtzeichen, vor allem auf Straßen, die mit mehr als 50 km/h befahren werden dürfen, soll geprüft werden, ob es erforderlich ist, durch geeignete Maßnahmen
(z. B. Blenden hinter den Lichtzeichen, übergroße oder wiederholte Lichtzeichen, entsprechende Gestaltung der Optik) dafür zu sorgen, daß sie auf ausreichende Entfernung erkennbar sind. Ferner ist die Wiederholung von Lichtzeichen links von der Fahrbahn, auf Inseln oder über der Straße zu erwägen, weil nur rechts stehende Lichtzeichen durch voranfahrende größere Fahrzeuge verdeckt werden können.
 
12	
IV.
Sind im Zuge einer Straße mehrere Lichtzeichenanlagen eingerichtet, so empfiehlt es sich in der Regel, sie aufeinander abzustimmen (z. B. auf eine Grüne Welle). Jedenfalls sollte dafür gesorgt werden, daß bei dicht benachbarten Kreuzungen der Verkehr, der eine Kreuzung noch bei "Grün" durchfahren konnte, auch an der nächsten Kreuzung "Grün" vorfindet.
 
13	
V.
Häufig kann es sich empfehlen, Lichtzeichenanlagen verkehrsabhängig so zu schalten, daß die Stärke des Verkehrs die Länge der jeweiligen Grünphase bestimmt. An Kreuzungen und Einmündungen, an denen der Querverkehr schwach ist, kann sogar erwogen werden, der Hauptrichtung ständig Grün zu geben, das von Fahrzeugen und Fußgängern aus der Querrichtung erforderlichenfalls unterbrochen werden kann.
 
14	
VI.
Lichtzeichenanlagen sollten in der Regel auch nachts in Betrieb gehalten werden; ist die Verkehrsbelastung nachts schwächer, so empfiehlt es sich, für diese Zeit ein besonderes Lichtzeichenprogramm zu wählen, das alle Verkehrsteilnehmer möglichst nur kurz warten läßt. Nächtliches Ausschalten ist nur dann zu verantworten, wenn eingehend geprüft ist, daß auch ohne Lichtzeichen ein sicherer Verkehr möglich ist. Solange die Lichtzeichenanlagen, die nicht nurausnahmsweise in Betrieb sind, nachts abgeschaltet sind, soll in den wartepflichtigen Kreuzungszufahrten gelbes Blinklicht gegeben werden. Darüber hinaus kann es sich empfehlen, negative Vorfahrtzeichen (Zeichen 205 und 206) von innen zu beleuchten. Solange Lichtzeichen gegeben werden, dürfen diese Vorfahrtzeichen dagegen nicht beleuchtet sein.
 
15	
VII.
Bei der Errichtung von Lichtzeichenanlagen an bestehenden Kreuzungen und Einmündungen muß immer geprüft werden, ob neue Markierungen (z. B. Abbiegestreifen) anzubringen sind oder alte Markierungen (z. B. Fußgängerüberwege) verlegt oder aufgehoben werden müssen, ob Verkehrseinrichtungen (z. B. Geländer für Fußgänger) anzubringen oder ob bei der Straßenbaubehörde anzuregende bauliche Maßnahmen (Verbreiterung der Straßen zur Schaffung von Stauraum) erforderlich sind.
 
16	
VIII.
Die Schaltung von Lichtzeichenanlagen bedarf stets gründlicher Prüfung. Dabei ist auch besonders auf die sichere Führung der Abbieger zu achten.
 
17	
IX.
Besonders sorgfältig sind die Zeiten zu bestimmen, die zwischen dem Ende der Grünphase für die eine Verkehrsrichtung und dem Beginn der Grünphase für die andere (kreuzende) Verkehrsrichtung liegen. Die Zeiten für Gelb und Rot-Gelb sind unabhängig von dieser Zwischenzeit festzulegen. Die Übergangszeit Rot und Gelb (gleichzeitig) soll für Kraftfahrzeugströme eine Sekunde dauern, darf aber nicht länger als zwei Sekunden sein. Die Übergangszeit Gelb richtet sich bei Kraftfahrzeugströmen nach der zulässigen Höchstgeschwindigkeit in der Zufahrt. In der Regel beträgt die Gelbzeit 3 s bei zul. V = 50 km/h, 4 s bei zul. V = 60 km/h und 5 s bei zul. V = 70 km/h. Bei Lichtzeichenanlagen, die im Rahmen einer Zuflussregelungsanlage aufgestellt werden, sind abweichend hiervon für Rot mindestens 2 s und für die Übergangssignale Rot und Gelb (gleichzeitig) bzw. Gelb mindestens 1 s zu wählen. Bei verkehrsabhängigen Lichtzeichenanlagen ist beim Rücksprung in die gleiche Phase eine Alles-Rot-Zeit von mindestens 1 s einzuhalten, ebenso bei Fußgänger-Lichtzeichenanlagen mit der Grundstellung Dunkel für den Fahrzeugverkehr. Bei Fußgänger-Lichtzeichenanlagen soll bei Ausführung eines Rücksprungs in die gleiche Fahrzeugphase die Mindestsperrzeit für den Fahrzeugverkehr 4 s betragen.
 
18	
X.
Pfeile in Lichtzeichen

1. Solange ein grüner Pfeil gezeigt wird, darf kein anderer Verkehrsstrom Grün haben, der den durch den Pfeil gelenkten kreuzt; auch darf Fußgängern, die in der Nähe den gelenkten Verkehrsstrom kreuzen, nicht durch Markierung eines Fußgängerüberwegs Vorrang gegeben werden. Schwarze Pfeile auf Grün dürfen nicht verwendet werden.
 
19		

2. Wenn in einem von drei Leuchtfeldern ein Pfeil erscheint, müssen auch in den anderen Feldern Pfeile gezeigt werden, die in die gleiche Richtung weisen. Vgl. Nummer X 6.
 
20		

3. Darf aus einer Kreuzungszufahrt, die durch ein Lichtzeichen geregelt ist, nicht in allen Richtungen weitergefahren werden, so ist die Fahrtrichtung durch die Zeichen 209 bis 214 vorzuschreiben. Vgl. dazu Nummer III. zu den Zeichen 209 bis 214 (Randnummer 3). Dort, wo Mißverständnisse sich auf andere Weise nicht beheben lassen, kann es sich empfehlen, zusätzlich durch Pfeile in den Lichtzeichen die vorgeschriebene Fahrtrichtung zum Ausdruck zu bringen; dabei sind schwarze Pfeile auf Rot und Gelb zu verwenden.
 
21		

4. Pfeile in Lichtzeichen dürfen nicht in Richtungen weisen, die durch die Zeichen 209 bis 214 verboten sind.
 
22		

5. Werden nicht alle Fahrstreifen einer Kreuzungszufahrt zur gleichen Zeit durch Lichtzeichen freigegeben, so kann auf Pfeile in den Lichtzeichen dann verzichtet werden, wenn die in die verschiedenen Richtungen weiterführenden Fahrstreifen baulich so getrennt sind, daß zweifelsfrei erkennbar ist, für welche Richtung die verschiedenen Lichtzeichen gelten. Sonst ist die Richtung, für die die Lichtzeichen gelten, durch Pfeile in den Lichtzeichen zum Ausdruck zu bringen.
 
23		
Hierbei sind Pfeile in allen Lichtzeichen nicht immer erforderlich. Hat z. B. eine Kreuzungszufahrt mit Abbiegestreifen ohne bauliche Trennung ein besonderes Lichtzeichen für den Abbiegeverkehr, so genügen in der Regel Pfeile in diesen Lichtzeichen. Für den anderen Verkehr sollten Lichtzeichen ohne Pfeile gezeigt werden. Werden kombinierte Pfeile in solchen Lichtzeichen verwendet, dann darf in keinem Fall gleichzeitig der zur Hauptrichtung parallel gehende Fußgängerverkehr freigegeben werden (vgl. Nummer Xl; Rn. 27 ff.).
 
24		

6. Wo für verschiedene Fahrstreifen besondere Lichtzeichen gegeben werden sollen, ist die Anbringung der Lichtzeichen besonders sorgfältig zu prüfen (z. B. Lichtzeichenbrücken, Peitschenmaste, Wiederholung am linken Fahrbahnrand). Wo der links abbiegende Verkehr vom übrigen Verkehr getrennt geregelt ist, sollte das Lichtzeichen für den Linksabbieger nach Möglichkeit zusätzlich über der Fahrbahn angebracht werden; eine Anbringung allein links ist in der Regel nur bei Fahrbahnen für eine Richtung möglich, wenn es für Linksabbieger lediglich einen Fahrstreifen gibt.
 
25		

7. Wo der Gegenverkehr durch Rotlicht aufgehalten wird, um Linksabbiegern, die sich bereits auf der Kreuzung oder Einmündung befinden, die Räumung zu ermöglichen, kann das diesen durch einen nach links gerichteten grünen Pfeil, der links hinter der Kreuzung angebracht ist, angezeigt werden. Gelbes Licht darf zu diesem Zweck nicht verwendet werden.
 
26		

8. Eine getrennte Regelung des abbiegenden Verkehrs setzt in der Regel voraus, daß für ihn auf der Fahrbahn ein besonderer Fahrstreifen mit Richtungspfeilen markiert ist (Zeichen 297).
 
XI.
Grünpfeil
27		

1. Der Einsatz des Schildes mit grünem Pfeil auf schwarzem Grund (Grünpfeil) kommt nur in Betracht, wenn der Rechtsabbieger Fußgänger- und Fahrzeugverkehr der freigegebenen Verkehrsrichtungen ausreichend einsehen kann, um die ihm auferlegten Sorgfaltspflichten zu erfüllen. Es darf nicht verwendet werden, wenn
 
28		
a)
dem entgegenkommenden Verkehr ein konfliktfreies Abbiegen nach links signalisiert wird,
 
29		
b)
für den entgegenkommenden Linksabbieger der grüne Pfeil gemäß § 37 Abs. 2 Nr. 1 Satz 4 verwendet wird,
 
30		
c)
Pfeile in den für den Rechtsabbieger gültigen Lichtzeichen die Fahrtrichtung vorschreiben,
 
31		
d)
beim Rechtsabbiegen Gleise von Schienenfahrzeugen gekreuzt oder befahren werden müssen,
 
32		
e)
der freigegebene Fahrradverkehr auf dem zu kreuzenden Radweg für beide Richtungen zugelassen ist oder der Fahrradverkehr trotz Verbotes in der Gegenrichtung in erheblichem Umfang stattfindet und durch geeignete Maßnahmen nicht ausreichend eingeschränkt werden kann,
 
33		
f)
für das Rechtsabbiegen mehrere markierte Fahrstreifen zur Verfügung stehen,
 
34		
g)
die Lichtzeichenanlage überwiegend der Schulwegsicherung dient oder
 
35		
h)
sich im unmittelbaren Bereich des rechtsabbiegenden Fahrverkehrs eine Aufstellfläche für das Linksabbiegen mit indirekter Radverkehrsführung befindet.
 
36		

2. An Kreuzungen und Einmündungen, die häufig von seh- oder gehbehinderten Personen überquert werden, soll die Grünpfeil-Regelung nicht angewandt werden. Ist sie ausnahmsweise an Kreuzungen oder Einmündungen erforderlich, die häufig von Blinden oder Sehbehinderten überquert werden, so sind Lichtzeichenanlagen dort mit akustischen oder anderen geeigneten Zusatzeinrichtungen auszustatten.
 
37		

3. Für Knotenpunktzufahrten mit Grünpfeil ist das Unfallgeschehen regelmäßig mindestens anhand von Unfallsteckkarten auszuwerten. Im Falle einer Häufung von Unfällen, bei denen der Grünpfeil ein unfallbegünstigender Faktor war, ist der Grünpfeil zu entfernen, soweit nicht verkehrstechnische Verbesserungen möglich sind. Eine Unfallhäufung liegt in der Regel vor, wenn in einem Zeitraum von drei Jahren zwei oder mehr Unfälle mit Personenschaden, drei Unfälle mit schwerwiegendem oder fünf Unfälle mit geringfügigem Verkehrsverstoß geschehen sind.
 
38		

4. Der auf schwarzem Grund ausgeführte grüne Pfeil darf nicht leuchten, nicht beleuchtet sein und nicht retroreflektieren. Das Schild hat eine Breite von 250 mm und eine Höhe von 250 mm.
 
XII.
Grünpfeil für den Radverkehr
 
39	

1. Für die Anordnung des Grünpfeils für den Radverkehr (Zeichen 721) gelten die Vorgaben der Nummer XI mit Ausnahme der Nummer 1 Buchstabe e und der Nummer 4 Satz 2 entsprechend.
 
40	

2. Über die in Nummer XI Nummer 1 Satz 2 genannten Fälle hinaus kommt eine Anordnung des Grünpfeils für den Radverkehr nicht in Betracht, wenn
 
41		
a)
bei allgemein hohem Radverkehrsaufkommen der Anteil des geradeaus fahrenden Radverkehrs den Anteil des nach rechts abbiegenden Radverkehrs erheblich übersteigt und die Verkehrsfläche ein sicheres Überholen des wartenden Radverkehrs nicht gewährleistet oder
 
42		
b)
der nach rechts abbiegende Radverkehr in der Knotenpunktzufahrt auf einem gemeinsamen Geh- und Radweg (Zeichen 240) oder einem für den Radverkehr freigegebenen Gehweg geführt wird (Zeichen 239 in Verbindung mit Zusatzzeichen 1022-10).
 
43		
Befindet sich in der Straße, in die eingebogen wird, ein baulich angelegter Radweg, muss dieser deutlich von dem daneben befindlichen Gehweg abgegrenzt sein. Warteflächen für zu Fuß Gehende müssen über eine hinreichende Größe verfügen. Entsprechendes gilt bei Vorliegen eines getrennten Rad- und Gehweges (Zeichen 241).
 
44	

3. Zeichen 721 ist grundsätzlich am Hauptsignalgeber anzubringen. Sind besondere Lichtzeichen für den Radverkehr vorhanden, soll Zeichen 721 am Signalgeber für den Radverkehr angebracht werden, wenn hierdurch der Fußverkehr nicht gefährdet wird.
 
45	

4. Eine gemeinsame Anordnung von Zeichen 720 und Zeichen 721 ist unzulässig, wenn der Radverkehr auf einem am rechten Fahrbahnrand befindlichen Radfahrstreifen, einem Schutzstreifen für den Radverkehr oder einem straßenbegleitenden, nicht abgesetzten, baulich angelegten Radweg geführt wird und der Radverkehr die Lichtzeichen für den Fahrverkehr zu beachten hat.
 
Zu Nummer 2
46	
Vgl. für verengte Fahrbahn Nummer II zu Zeichen 208 (Rn. 2); bei Festlegung der Phasen ist sicherzustellen, daß auch langsamer Fahrverkehr das Ende der Engstelle erreicht hat, bevor der Gegenverkehr freigegeben wird.
 
Zu Nummer 3
47	
Die Farbfolge Gelb-Rot darf lediglich dort verwendet werden, wo Lichtzeichenanlagen nur in größeren zeitlichen Abständen in Betrieb gesetzt werden müssen, z. B. an Bahnübergängen, an Ausfahrten aus Feuerwehr- und Straßenbahnhallen und Kasernen. Diese Farbfolge empfiehlt sich häufig auch an Wendeschleifen von Straßenbahnen und Oberleitungsomnibussen. Auch an Haltebuchten von Oberleitungsomnibussen und anderen Linienomnibussen ist ihre Anbringung zu erwägen, wenn auf der Straße starker Verkehr herrscht. Sie oder Lichtzeichenanlagen mit drei Farben sollten in der Regel da nicht fehlen, wo Straßenbahnen in eine andere Straße abbiegen.
 
Zu Nummer 4
48	
I.
Vgl. Nummer X 6 bis 8 zu den Nummern 1 und 2; Rn. 24 bis 26.
 
49	
II.
Besondere Zeichen sind die in der Anlage 4 der Straßenbahn-Bau- und Betriebsordnung aufgeführten. Zur Markierung vorbehaltener Fahrstreifen vgl. zu Zeichen 245.
 
Zu Nummer 5
50	
I.
Im Lichtzeichen für Fußgänger muß das rote Sinnbild einen stehenden, das grüne einen schreitenden Fußgänger zeigen. Zur Möglichkeit der Verwendung des sog. Ost-Ampelmännchens wird auf die Richtlinien für Lichtsignalanlagen (RiLSA) verwiesen.
 
51	
II.
Lichtzeichen für Radfahrer sollten in der Regel das Sinnbild eines Fahrrades zeigen. Besondere Lichtzeichen für Radfahrer, die vor der kreuzenden Straße angebracht werden, sollten in der Regel auch Gelb sowie Rot und Gelb (gleichzeitig) zeigen. Sind solche Lichtzeichen für einen abbiegenden Radfahrverkehr bestimmt, kann entweder in den Lichtzeichen zusätzlich zu dem farbigen Sinnbild des Fahrrades ein farbiger Pfeil oder über den Lichtzeichen das leuchtende Sinnbild eines Fahrrades und in den Lichtzeichen ein farbiger Pfeil gezeigt werden.
 
Zu Nummer 6
 
52	
Zur gemeinsamen Signalisierung des Fußgänger- und Radverkehrs gilt Folgendes: In den roten und grünen Lichtzeichen der Fußgängerlichtzeichenanlage werden jeweils die Sinnbilder für Fußgänger und Radfahrer gemeinsam gezeigt oder neben dem Lichtzeichen für Fußgänger wird ein zweifarbiges Lichtzeichen für Radfahrer angebracht; beide Lichtzeichen müssen jeweils dieselbe Farbe zeigen. Vgl. im Übrigen zur Signalisierung für den Radverkehr die Richtlinien für Lichtsignalanlagen(RiLSA).
 
 
Zu Absatz 3
53	
I.
Dauerlichtzeichen dürfen nur über markierten Fahrstreifen (Zeichen 295, 296, 340) gezeigt werden. Ist durch Zeichen 223.1 das Befahren eines Seitenstreifens angeordnet, können Dauerlichtzeichen diese Anordnung und die Anordnungen durch Zeichen 223.2 und Zeichen 223.3 unterstützen, aber nicht ersetzen (vgl. Nummer V zu den Zeichen 223.1 bis 223.3; Rn. 5).
 
54	
II.
Die Unterkante der Lichtzeichen soll in der Regel 4,50 m vom Boden entfernt sein.
 
55	
III.
Die Lichtzeichen sind an jeder Kreuzung und Einmündung und erforderlichenfalls auch sonst in angemessenen Abständen zu wiederholen.
 
56	
IV.
Umkehrstreifen im besonderen
Wird ein Fahrstreifen wechselweise dem Verkehr der einen oder der anderen Fahrtrichtung zugewiesen, müssen die Dauerlichtzeichen für beide Fahrtrichtungen über allen Fahrstreifen gezeigt werden. Bevor die Fahrstreifenzuweisung umgestellt wird, muss für eine zur Räumung des Fahrstreifens ausreichende Zeit das Zeichen gekreuzte rote Balken für beide Richtungen gezeigt werden.


</details>

# Challenge Goals

Das Team sollte versuchen, eine Art Vorprüfung der Anfrage vorzunehmen. Es werden diverse Beispiele für positive Prüfungen vorhanden sein, hierfür wird im ersten Schritt nur der Lageplan als pdf, wenn gewünscht auch als CAD Datei (.dwg) bereitgestellt werden. Zum Aufbau des Lageplans gibt es bereits ein paar Beispielprompts und ein jupyter notebook aus einem Vergangenen Hackathon letztes Jahr, hier waren wir noch nicht so erfolgreich :)

Nichts desto trotz ging es dort nur um die Prüfung vom Verstädnis von einem Lageplan basierend auf einem Beispiel und ein paar Chain of Thought Prompting versuche die damals unzufriedend waren und mit gpt-4o getestet wurden, heißt reasoning modelle wie o1 wurden bisher noch nicht getestet.

Hier steht dem Team nun alles offen. Es gibt auch ein Concept Board, auf welchem grob der Workflow und ein paar Beispiele gezeigt werden. Im [Link zum Conceptboard](https://lhm.conceptboard.com/board/2srp-igxo-ifs6-5xtu-ya5k) sind verschiedene Beispiele für andere Anfragen und noch ein paar weitere verkehrstechnische Details zu relevantem Kontext. Wir beschränken uns auf denn Rechtsabbiegegrünpfeil für Radfahrer und den Prozessschritt der formalen Prüfung und Bewertung des Ermessungsspielraums bei Kriterien mit Interpretationsspielraum.
Es gibt auch ein Video der Vorstellung der letzten Challenge, ich kann ihn aus Datenschutzgründen nicht teilen aber zeigen. Die ganze Verkehrstechnik Terminologie ist anfangs etwas schwer zu verstehen, hierfür sollen die bereitstellten Materialien helfen, ich bin aber auch für Nachfragen erreichbar.

Zurück zur Anfrage des Rechtsabbiegegrünpfeils fürs Radfahrer. Die Bearbeitung der Anfrage basiert auf zwei Stufen:

1. Im ersten Schritt werden eindeutige Ausschlusskriterien geprüft. Diese sind beziehen sich auf einen absatz oder eine zeilennummer und lassen keinen spielraum zu. Deren Sinnhaftigkeit kann in Frage gestellt werden, diese kann aber nur an übergeordnete gesetzgebende Instanzen oder Ministerien in welchen die Verwaltungsvorschriften, Richtlinien oder Gesestzte geschrieben werden rückgemeldet werden und diese müssen es für zukunftige Änderungen miteinbeziehen. Ein Beispiel für diesen Fall ist die häufig vorkommende Situation, dass eine Rechtsabbiegebeziehung einen gegenüberliegenden Linksabbieger hat, welcher zu irgendeinem bestimmten Zeitpunkt ein konfliktfreies Abbiegen signalisiert bekommt, genauer gesagt VwV-StVO zu § 37 XI. 1. a). Ist dies der Fall, und die linksabbiegende Beziehung kann vom Radvekehr befahren werden ist es pauschal nicht möglich. Die Frage ob das der Fall ist hängt davon ab, ob der Radverkehr aus dieser Richtung mit dem motorisierten Individualverkehr signalisiert wird und mit ihm fährt oder ein indirektes Linksabbiegen gefordert wird, bzw. eine eigene Radverkehrsignalisierung vorhanden ist. Als Grundlage gibt es nur den Lageplan und vorhandene 3d maps wie google street maps, die LHM hat auch einen von infra3d beauftrage 3D Map der Stadt. Es gibt keinen digitalen Zwilling der die Infrastruktur in diesem Detail konsisten abbildet. Es existiert die CAD Datei, hat aber nur partiell eigene standartisierte Blöcke (siehe Jupyter notebook) die vermutlich nur teils untestützen aber aktuel keine text2sql ähnlichen Ansätze möglich macht. Dennoch ist diese Prüfung auf Logik / Kombinatorik basierend möglich. Sind alle der eindeutigen Ausschlusskriterien geprüft und treffen nicht zu, kann zur Stufe 2 vorgeschritten werden.
2. In der nächsten Stufe haben die zu Prüfenden kriteren einen gewissen Interpretationsspielraum des Sachbearbeiters. Hier kommt nun die Interpretationssfähigkeit des Sprachmodells ins Spiel. Die Frage ist wie gut ist bspw. ein GPT Agent in der Lage die Richtigen Abfragen zu machen wie:
	- VwV-StVO zu §37 XII. 2. a): Hier ist Spielraum vorhanden und es hängt nochmal etwas individueller von den lokalen Gegebenheiten ab, hier muss die Aufstellfläche bewertet werden und die vmtl. nur von einem Menschen zu berwertenden Sicherheitsrisiken für einen Ausschluss oder ein go sind notwendeig.
	- Die Bewertung ob in dem Bereich für gleichgerichtete (Fahrbeziehung) und gleichgeartete (Radfahrer mit X) bereits Unfallhäufungen existieren.
Beispiele wie diese können auch durch tool use abfragen von einem gpt agent bearbeitet werden, sind dennoch vmtl. beim Review zu beachten. Der Agent muss nicht final die Entscheidung treffen, sollte dem Sachbearbeiter aber die Bewertungsgrundlage und einschätzung nur basierend auf vorhandenen Daten geben. In der Realität findet für Beispiele mit interpretationsspielraum viel kommunikation zwischen verkehrsingenieuren und verwaltungsexperten statt um zu einer fundierten Begründung zu kommen die rechtssicher ist bzw. die Verkehrssicherheit maximiert. Auch hier sind Verwaltungsangestellte anfangs übefordert wenn ein Gesetz oder die Änderung neu ist. Sie müssen aktezptieren, dass sie ohne Erfahrungswerte Fehler machen und mit den neuen Erkenntnissen den Interpretationsspielraum neu eingrenzen. 
Ob solche Dinge in irgendeiner Form simuliert werden können keine Ahnung, es gibt hier ja Beispiele aber die sind sehr high-level [GPT-Agent House](https://www.youtube.com/watch?v=ewLMYLCWvcI) und eine Umsetzung ist sicher nicht leicht und etwas viel für einen 48h Hackathon.

Eine Erläuterung und Einordnung der Kriterien is in [Grünpfeilschild Ausschlusskriterien.xlsx](documents/Grünpfeilschild Ausschlusskriterien kommentiert.docx) zu finden. Nach diesen zwei Stufen ist eine verkehrsrechtliche Anordnung des Schild möglich, wenn kein Kriterium zutrifft. Falls schon, wird der Grund genannt und die Abfrage negativ abgelehnt. Rückmeldungen zu Sinhaftigkeiten und Fehleinschätzungen können gesammelt ausgewertet werden. Wie und welche Anforderungen das Team umsetzt ist ganz ihnen überlassen, ich freue mich auf neue Erkenntnisse. Leider sind wir nicht in der Lage perfekt strukturierte Datengrundlagen zu liefern aber so ist das im Fall der Verwaltung, es sind meist sehr individuelle Prüfungen, definitiv aber in teilen automatisierbar gerade was formale Kriterien angeht und die Berwertungsgrundlage und Vorbereitung von Prüfkriterien, welche eine menschliche Bewertung nötig macht. 
Das Team kann basierend auf den vorhandenen Information auch Nachfragen stellen zu zusätzlichen Informationen, alternativ aber auch einen tool call oder bestimmte menschliche Bewertungen mocken.
Es gibt 70 Beispiele mit verschiedenen Ausschlusskriterien für ca. 55 Anlagen, mache Kriterien sind allerdings sehr exotisch und kommen dadurch (fast) gar nicht vor. Für jedes Beispiel ist die positiv/negative Rückmeldung, die Begründung und teils die Anfrage mit dabei. Die Lagepläge stelle ich für ein paar Anlagen zur Verfügung und liefere weitere auf Nachfrage da ich sie raussuchen muss. .dwf Files gerne auch auf Nachfrage wenn benötigt, kostet auch etwas Zeit.

Aufbau der Tabelle in [examples.xlsx](documents/examples.xlsx) ist wie folgt:

- **LSA_NR**: Dies steht für die Nummer der Lichtsignalanlage (Ampel). Jede Ampel hat eine eindeutige Nummer zur Identifizierung.
- **LSA_NAME**: Dies ist der Name der Lichtsignalanlage bzw. der Standort, an dem sich die Ampel befindet.
- **Fahrbeziehung**: Diese Spalte beschreibt die spezifische Verkehrssituation oder Beziehung zwischen zwei Straßen oder Fahrtrichtungen.
- **Lageplan vor Anordnung**: Name der Datei des Lageplans unter _anlagen/_ bei Prüfung.
- **Lageplanname nach Anordnung**: Name der Datei des Lageplans nach Annordnung, wenn positiv.
- **möglich**: Diese Spalte gibt an, ob die beschriebene Fahrbeziehung möglich ist oder nicht.
- **Ausschlusskriterium aus VwV-StVO zu § 37 zu den Nummern 1 und 2**: Hier wird das spezifische Ausschlusskriterium angegeben, welches aus der Verwaltungsvorschrift zur Straßenverkehrs-Ordnung (VwV-StVO) resultiert. Bspw. "XI. 1. b)".
- **Anfrage**: Diese Spalte enthält die Anfrage oder den Antrag, der gestellt wurde falls verfügbar.
- **Antwort / Begründung / Bemerkung**: Diese Spalte bietet Raum für die Antwort auf die Anfrage, eine Begründung oder zusätzliche Bemerkungen.

In [Stammdaten_LSA_utf_8.csv](hackathon_2024/data/Stammdaten_LSA_utf_8.csv) ist LZANR=LSA_NR, wenn GPS, bzw. UMT32 Positionen gebraucht werden. 

Der Workflow und Gedankengänge wurden hoffentlich näher gebracht, zusätzliche Informationen befinden sich unter _hackathon_2024_ mit dem jupyter notebook vom letzten Hackathon. Viel Spaß!

