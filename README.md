# WikiDiary

WikiDiary to lokalna aplikacja na Androida do szybkiego zapisywania codziennych
notatek, porządkowania ich za pomocą tagów i projektów oraz przygotowywania
wpisów w formacie Wiki.

Aplikacja nie wymaga konta ani własnego backendu. Notatki, tagi, kategorie i
rutyny są przechowywane lokalnie na urządzeniu. Połączenie z internetem jest
potrzebne tylko opcjonalnie, podczas wyszukiwania w Filmwebie.

## Najważniejsze funkcje

- **Dodawanie notatek** - wpisz treść, wybierz istniejący tag i opcjonalnie
  ustaw datę wpisu.
- **Tagi** - własne tagi można dodawać z ekranu dodawania notatki. Długie
  przytrzymanie tagu otwiera jego edycję lub usunięcie.
- **Projekty i kategorie** - projekt jest tagiem z kolorem i opcjonalną
  kategorią. Kategorie można tworzyć, zmieniać i usuwać w dolnym panelu.
- **Lista notatek** - przeglądanie wpisów oraz zaznaczanie wielu wpisów do
  jednoczesnego usunięcia.
- **Rutyny dzienne** - własna lista codziennych czynności z możliwością
  odhaczania i usuwania. Stan wykonania jest resetowany przy pierwszym
  uruchomieniu aplikacji w nowym dniu. Odhaczenie rutyny zapisuje również
  notatkę z tagiem `Routine`.
- **ToDo** - osobna lista zadań do wykonania. Dodanie zadania pozostawia
  notatkę z tagiem `ToDo`, a jego ukończenie zapisuje notatkę z tagiem `Today`.
  Ukończone zadania pozostają na liście jako odhaczone, a zadania są
  przechowywane lokalnie niezależnie od codziennego resetu rutyn.
- **Osoby i wzmianki** - chip `👥 Osoby` otwiera katalog osób, w którym można
  zapisać imię i nazwisko oraz wybrać ikonę osoby. W treści notatki wpisz `@`
  i pierwsze litery imienia, aby wybrać podpowiedź z katalogu. Po zapisaniu
  wzmianka jest zamieniana bezpośrednio w treści na pogrubiony link DokuWiki
  do przestrzeni `Community`, np.
  `**🙍🏻‍♀️ [[:Community:Dorota Płachno]]**`.
- **Notatki Filmweb** - chip `🎬 Filmweb` otwiera natywne wyszukiwanie w
  dolnym panelu aplikacji. Wyniki są pokazywane jako przewijana lista
  maksymalnie 10 filmów i seriali z okładką, tytułem oraz rokiem. Po wybraniu
  pozycji można poprawić jej metadane, a następnie zapisać zwykłą notatkę razem
  ze strukturalnymi metadanymi Filmwebu.
- **Pictogramy** - chip `🖼️ Pictogramy` otwiera katalog ikon. Można utworzyć
  pictogram, wybierając ikonę i nazwę, przypisać go do wybranego taga oraz
  edytować lub usunąć definicję. Przycisk `+` pokazuje dodatkowy zestaw ikon.
  Usuwanie przypisania jest dostępne bezpośrednio przy pictogramie na ekranie
  List Notes. Eksport tworzy osobny wpis DokuWiki, np.
  `{{ :pictograms_-_love.png?200 |}}`, pod właściwym tagiem i dniem;
  pictogramy nie są dopisywane do treści notatek.
- **Przygotowanie wpisu Wiki** - notatki są grupowane według daty, tagów i
  kategorii, a wygenerowany tekst można skopiować do schowka. Pictogramy są
  grupowane osobno pod odpowiednim tagiem. Notatki Filmweb zawierają klikalny
  link do wybranej strony oraz znacznik grafiki DokuWiki dodany po listach
  danego dnia. Nad przyciskiem kopiowania można zapisać adres strony Wiki;
  po jego zapisaniu przycisk web kopiuje pełny eksport do schowka i otwiera
  tę stronę w przeglądarce, aby można było wkleić treść.
- **Widget ekranu głównego** - pokazuje czas od ostatnio zapisanej notatki,
  wizualizuje upływ 24 godzin i otwiera aplikację po kliknięciu.

Przy pierwszym utworzeniu lokalnej bazy aplikacja dodaje przykładowe tagi,
kategorie i rutyny, m.in. `Book`, `Meeting`, `Work`, `Health` oraz `10000
kroków`.

## Ekrany aplikacji

Górny pasek udostępnia pięć obszarów:

| Ekran | Zastosowanie |
| --- | --- |
| **Add Note** | Wpisywanie treści i zapisywanie jej pod wybranym tagiem. |
| **Routines** | Zarządzanie i wykonywanie codziennych rutyn. |
| **ToDo** | Dodawanie, odhaczanie i usuwanie zadań do wykonania. |
| **Prepare Note** | Podgląd oraz kopiowanie tekstu przygotowanego w formacie Wiki. |
| **List Notes** | Przeglądanie i masowe usuwanie zapisanych notatek. |

### Typowy przepływ pracy

1. Na ekranie **Add Note** wpisz treść notatki.
2. Wybierz tag, aby zapisać wpis. Chip z datą pozwala zmienić dzień, którego
   dotyczy notatka.
3. Aby dodać film lub serial, wybierz chip **🎬 Filmweb**, wyszukaj pozycję i
   kliknij wynik na natywnej liście.
4. Zatwierdź kartę wybranej pozycji, wybierz tag, sprawdź datę i opcjonalnie
   zmień początek zdania lub metadane.
5. Aby dodać pictogram, wybierz **🖼️ Pictogramy**, wybierz tag, utwórz lub
   wybierz pictogram i kliknij **Dodaj**. Data jest pobierana z aktualnie
   wybranego dnia.
6. Na ekranie **ToDo** dodaj zadanie. Aplikacja zapisze jego dodanie jako
   notatkę pod tagiem `ToDo`.
7. Po wykonaniu zadania odhacz je. Aplikacja zapisze zakończenie jako notatkę
   pod tagiem `Today`; ukończone zadanie pozostanie na liście.
8. Wybierz **👥 Osoby**, dodaj imię i nazwisko oraz ikonę. W treści notatki
   wpisz `@` i pierwszą literę, a następnie wybierz osobę z podpowiedzi.
9. Użyj **Prepare Note**, aby wygenerować tekst i skopiować go do schowka.
10. Opcjonalnie wybierz `+` nad przyciskiem kopiowania i zapisz pełny adres
   strony Wiki (`http://` lub `https://`). Później przycisk web skopiuje
   eksport i otworzy zapisany adres.
11. Wklej przygotowany tekst do docelowej strony Wiki.

## Dane i prywatność

- Dane są przechowywane w lokalnej bazie Room `wiki_database`.
- Pozycje Filmweb są przechowywane w katalogu `film_table`, a relacje między
  nimi i notatkami w `film_note_table`; migracja bazy zachowuje istniejące
  notatki, tagi, kategorie i rutyny.
- Definicje pictogramów są przechowywane w `pictogram_table`, a ich użycia
  przypisane do dnia i taga w `pictogram_entry_table`.
- Zadania ToDo są przechowywane w lokalnej tabeli `todo_table`. Wpisy o dodaniu
  i ukończeniu zadania są zwykłymi notatkami, odpowiednio z tagami `ToDo` i
  `Today`.
- Katalog osób jest przechowywany w tabeli `person_table`, a przypisania osób
  do notatek w `note_person_table`. Usunięcie osoby z katalogu jest
  archiwizacją, dlatego wcześniejsze notatki nadal mogą eksportować poprawne
  linki do tej osoby.
- Aplikacja przechowuje referencję DokuWiki i ikonę podglądu pictogramu, ale
  nie generuje ani nie przesyła plików PNG. Plik `pictograms_-_<nazwa>.png`
  powinien istnieć w docelowej Wiki.
- Ustawienia pomocnicze, takie jak dzień ostatniego uruchomienia i czas ostatniej
  notatki oraz zapisany adres strony Wiki są przechowywane w `SharedPreferences`.
- Aplikacja nie ma backendu, logowania ani synchronizacji z chmurą.
- Usunięcie danych aplikacji lub jej odinstalowanie usuwa lokalną bazę danych.
- Wyszukiwanie Filmweb korzysta z nieoficjalnych endpointów używanych przez
  serwis (`/api/v1/live/search` i `/api/v1/title/{id}/info`). Zapytania są
  wykonywane dopiero po użyciu funkcji Filmweb; wyniki i plakaty są pobierane
  bezpośrednio z internetu i nie są zapisywane jako lokalny cache.

## Gotowy APK - instalacja bez kompilowania

W repozytorium znajduje się aktualny build debug:

- [Pobierz `WikiDiary-debug.apk`](artifacts/WikiDiary-debug.apk)
- [Pobierz bezpośrednio z GitHub](https://github.com/Maniak-pl/WikiDiary/raw/develop/artifacts/WikiDiary-debug.apk)
- [Plik z sumą SHA-256](artifacts/WikiDiary-debug.apk.sha256)

| Parametr | Wartość |
| --- | --- |
| Wersja aplikacji | `1.1` |
| `versionCode` | `2` |
| Typ builda | `debug` |
| Pakiet | `pl.maniak.wikidiary` |
| Źródło APK | `develop` + natywna lista wyników Filmweb |
| SHA-256 APK | `6186cb5b02437129c6888ec6e61d84b904249c251f8a553a13138cb11f91f026` |

### Instalacja przez ADB

Pobierz plik APK, opcjonalnie zweryfikuj sumę, a następnie zainstaluj go na
podłączonym emulatorze lub urządzeniu:

```bash
curl -L -o WikiDiary-debug.apk \
  https://github.com/Maniak-pl/WikiDiary/raw/develop/artifacts/WikiDiary-debug.apk

shasum -a 256 WikiDiary-debug.apk
adb install -r WikiDiary-debug.apk
adb shell monkey -p pl.maniak.wikidiary \
  -c android.intent.category.LAUNCHER 1
```

Na fizycznym urządzeniu należy wcześniej włączyć debugowanie USB. APK jest
podpisany kluczem debugowym i przeznaczony do testów, nie do dystrybucji
produkcyjnej.

## Technologia i architektura

- Kotlin
- Jetpack Compose i Material
- Room jako lokalna baza danych
- Koin do wstrzykiwania zależności
- Coroutines
- Gradle z Android Gradle Plugin `7.4.0`
- Kotlin plugin `1.9.10`
- `compileSdk` / `targetSdk`: `34`
- `minSdk`: `24`

Główne warstwy projektu:

- `ui` - aktywność, ViewModel, ekrany Compose i widget;
- `domain` - modele i interfejsy repozytoriów;
- `data` - encje Room, DAO, mapery i implementacje repozytoriów;
- `utils` - formatowanie dat oraz generowanie składni Wiki.

## Budowanie lokalne (opcjonalnie)

Gotowy APK nie wymaga kompilowania. Jeśli potrzebny jest nowy build, wymagane
są Android SDK z platformą 34 oraz JDK zgodne z Android Gradle Plugin 7.4.

Przykładowe polecenia dla środowiska z SDK w
`/opt/homebrew/share/android-commandlinetools`:

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT="$ANDROID_HOME"

# Potrzebne przy uruchamianiu starego KAPT na JDK 21.
export JAVA_TOOL_OPTIONS="--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED"

bash ./gradlew assembleDebug --no-daemon
bash ./gradlew testDebugUnitTest --no-daemon
```

Wygenerowany plik znajduje się w
`app/build/outputs/apk/debug/app-debug.apk`. Po zmianie kodu należy skopiować
nowy build do `artifacts/WikiDiary-debug.apk`, zaktualizować plik sumy SHA-256
i odpowiednie dane wersji w tej dokumentacji.

## Znane ograniczenia

- APK przechowywany w repozytorium jest wersją debugową.
- Edycja istniejącej notatki jest obecnie widoczna w modelu zdarzeń, ale nie ma
  jeszcze implementacji w interfejsie.
- Eksport odbywa się przez kopiowanie tekstu do schowka; aplikacja nie wysyła
  wpisów bezpośrednio na serwer Wiki. Przycisk web otwiera stronę w zewnętrznej
  przeglądarce, więc wklejenie skopiowanej treści wymaga potwierdzenia przez
  użytkownika.
- Brak synchronizacji między urządzeniami i kont użytkowników.
- Integracja Filmweb korzysta z nieoficjalnego, nieudokumentowanego API.
  Filmweb może zmienić jego strukturę, ograniczyć dostęp lub wyłączyć endpointy
  bez zapowiedzi. W takim przypadku panel pokazuje błąd z opcją ponowienia.
- Filmweb wymaga połączenia z internetem tylko podczas wyszukiwania i odczytu
  wyników. Zapisane notatki i ich metadane pozostają lokalne.
