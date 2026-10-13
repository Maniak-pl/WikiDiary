# WikiDiary

WikiDiary to lokalna aplikacja na Androida do szybkiego zapisywania codziennych
notatek, porządkowania ich za pomocą tagów i projektów oraz przygotowywania
wpisów w formacie Wiki.

Aplikacja nie wymaga konta ani własnego backendu. Notatki, tagi, kategorie i
rutyny są przechowywane lokalnie na urządzeniu. Połączenie z internetem jest
potrzebne tylko opcjonalnie, podczas wyszukiwania w Filmwebie.

## Najważniejsze funkcje

- **Szybki wpis (quick capture)** - jedno pole tekstowe w zakładce Dziennik
  obsługuje wszystko. Notatkę zapisuje kliknięcie tagu lub przycisk **Wyślij**,
  gdy w treści jest `#Tag` (np. `Spotkanie z zespołem #Work`; znacznik tagu
  jest usuwany z treści). Wpisanie `#`, `@` lub `/` na początku pokazuje
  podpowiedzi tagów, osób i komend. Przycisk ⊕ przy polu otwiera listę komend:

  | Komenda | Działanie |
  | --- | --- |
  | `/todo <treść>` | dodaje zadanie ToDo (i notatkę pod tagiem `ToDo`) |
  | `/film <tytuł>` | otwiera Filmweb i od razu wyszukuje tytuł |
  | `/piktogram` | otwiera panel pictogramów |
  | `/osoby` | otwiera katalog osób |
  | `/tag <nazwa>` | tworzy nowy tag |
  | `/projekt` | tworzy projekt (tag z kolorem i kategorią) |
  | `/kategorie` | otwiera zarządzanie kategoriami |
  | `/data` | zmienia dzień notatki |

  Gdy brakuje tagu lub argumentu komendy, pod polem pojawia się podpowiedź.
- **Tagi** - nowy tag tworzy komenda `/tag <nazwa>`. Długie przytrzymanie tagu
  otwiera jego edycję lub usunięcie.
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
- **Osoby i wzmianki** - komenda `/osoby` otwiera katalog osób, w którym można
  zapisać imię i nazwisko oraz wybrać ikonę osoby. W treści notatki wpisz `@`
  i pierwsze litery imienia, aby wybrać podpowiedź z katalogu. Po zapisaniu
  wzmianka jest zamieniana bezpośrednio w treści na pogrubiony link DokuWiki
  do przestrzeni `Community`, np.
  `**🙍🏻‍♀️ [[:Community:Dorota Płachno]]**`.
- **Notatki Filmweb** - komenda `/film <tytuł>` otwiera natywne wyszukiwanie w
  dolnym panelu aplikacji i od razu wyszukuje podany tytuł. Wyniki są pokazywane jako przewijana lista
  maksymalnie 10 filmów i seriali z okładką, tytułem oraz rokiem. Po wybraniu
  pozycji można poprawić jej metadane, a następnie zapisać zwykłą notatkę razem
  ze strukturalnymi metadanymi Filmwebu.
- **Pictogramy** - komenda `/piktogram` otwiera katalog ikon. Można utworzyć
  pictogram, wybierając ikonę i nazwę, przypisać go do wybranego taga oraz
  edytować lub usunąć definicję. Przycisk `+` pokazuje dodatkowy zestaw ikon.
  Usuwanie przypisania jest dostępne bezpośrednio przy pictogramie w zakładce
  Notatki. Eksport tworzy osobny wpis DokuWiki, np.
  `{{ :pictograms_-_love.png?200 |}}`, pod właściwym tagiem i dniem;
  pictogramy nie są dopisywane do treści notatek.
- **Przygotowanie wpisu Wiki** - notatki są grupowane według daty, tagów i
  kategorii, a wygenerowany tekst można skopiować do schowka. Pictogramy są
  grupowane osobno pod odpowiednim tagiem. Notatki Filmweb zawierają klikalny
  link do wybranej strony oraz znacznik grafiki DokuWiki dodany po listach
  danego dnia. Nad przyciskiem kopiowania można zapisać adres strony Wiki;
  po jego zapisaniu przycisk web kopiuje pełny eksport do schowka i otwiera
  tę stronę w przeglądarce, aby można było wkleić treść.
- **Ustawienia** - ikona ⚙️ w górnym pasku otwiera ekran ustawień:
  przełącznik ciemnego motywu (domyślnie zgodny z systemem; wybór jest
  zapamiętywany) oraz adres strony Wiki używany przy eksporcie.
- **Tryb pionowy** - aplikacja działa wyłącznie w orientacji pionowej.
- **Widget ekranu głównego** - pokazuje czas od ostatnio zapisanej notatki,
  wizualizuje upływ 24 godzin i otwiera aplikację po kliknięciu.

Przy pierwszym utworzeniu lokalnej bazy aplikacja dodaje przykładowe tagi,
kategorie i rutyny, m.in. `Book`, `Meeting`, `Work`, `Health` oraz `10000
kroków`.

## Ekrany aplikacji

Dolny pasek nawigacji (Material 3) ma pięć zakładek z etykietami; nazwa
bieżącej zakładki jest widoczna w górnym pasku. Każda zakładka zachowuje swój
stan (np. niedokończony tekst notatki) po przełączeniu na inną.

| Zakładka | Zastosowanie |
| --- | --- |
| **Dziennik** | Szybki wpis: notatka pod tagiem (kliknięcie tagu lub `#Tag` + Wyślij), wzmianki `@`, komendy `/` i menu ⊕ otwierające Filmweb, Pictogramy, Osoby, tagi, kategorie i wybór daty. |
| **Zadania** | Dodawanie, odhaczanie i usuwanie zadań ToDo. |
| **Rutyny** | Zarządzanie i wykonywanie codziennych rutyn. |
| **Notatki** | Przeglądanie i masowe usuwanie zapisanych notatek oraz pictogramów. |
| **Eksport** | Podgląd oraz kopiowanie tekstu przygotowanego w formacie Wiki. |

Panele (tagi, kategorie, Filmweb, Pictogramy, Osoby) otwierają się jako
arkusze dolne; można je zamknąć gestem w dół lub przyciskiem Wstecz.
Ikona ⚙️ w górnym pasku otwiera ekran **Ustawienia** (bez dolnego paska,
powrót strzałką lub przyciskiem Wstecz).

### Typowy przepływ pracy

1. W zakładce **Dziennik** wpisz treść notatki.
2. Wybierz tag, aby zapisać wpis, albo dopisz `#Tag` i kliknij **Wyślij**.
   Chip z datą (lub `/data`) otwiera kalendarz, w którym można zmienić dzień,
   którego dotyczy notatka.
3. Aby dodać film lub serial, wpisz `/film <tytuł>` i kliknij **Wyślij**
   (albo wybierz Filmweb z menu ⊕), a następnie kliknij wynik na natywnej
   liście.
4. Zatwierdź kartę wybranej pozycji, wybierz tag, sprawdź datę i opcjonalnie
   zmień początek zdania lub metadane.
5. Aby dodać pictogram, wpisz `/piktogram` (lub wybierz go z menu ⊕), wybierz tag, utwórz lub
   wybierz pictogram i kliknij **Dodaj**. Data jest pobierana z aktualnie
   wybranego dnia.
6. W zakładce **Zadania** dodaj zadanie (albo wpisz w Dzienniku
   `/todo <treść>`). Aplikacja zapisze jego dodanie jako
   notatkę pod tagiem `ToDo`.
7. Po wykonaniu zadania odhacz je. Aplikacja zapisze zakończenie jako notatkę
   pod tagiem `Today`; ukończone zadanie pozostanie na liście.
8. Wpisz `/osoby`, dodaj imię i nazwisko oraz ikonę. W treści notatki
   wpisz `@` i pierwszą literę, a następnie wybierz osobę z podpowiedzi.
9. Otwórz zakładkę **Eksport**, aby wygenerować tekst i skopiować go do schowka.
10. Opcjonalnie zapisz pełny adres strony Wiki (`http://` lub `https://`) w
   **Ustawieniach** albo przyciskiem `+` nad przyciskiem kopiowania. Później
   przycisk web skopiuje eksport i otworzy zapisany adres.
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
- Ustawienia pomocnicze, takie jak dzień ostatniego uruchomienia, czas ostatniej
  notatki, wybrany motyw oraz zapisany adres strony Wiki, są przechowywane w
  `SharedPreferences`.
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

- Kotlin `2.0` z kompilatorem Compose (K2)
- Jetpack Compose (BOM) i Material 3 ze stałą paletą marki (czerwony akcent),
  w wersji jasnej i ciemnej
- Navigation Compose z bezpiecznymi typowo trasami (`@Serializable`)
- tryb edge-to-edge (`enableEdgeToEdge`)
- Room jako lokalna baza danych (generowanie kodu przez KSP)
- Koin do wstrzykiwania zależności
- Coroutines
- Gradle `8.11` z Android Gradle Plugin `8.7`
- wersje zależności w katalogu `gradle/libs.versions.toml`
- `compileSdk` / `targetSdk`: `34`
- `minSdk`: `24`

Kod jest podzielony na funkcje (feature slices). Każda funkcja ma własny
pakiet z warstwami `domain` (modele, interfejsy repozytoriów, logika),
`data` (encje Room, DAO, mapery, implementacje) i `ui` (ekrany Compose) oraz
własny moduł Koin:

- `core` - baza Room, konfiguracja, motyw, wspólne komponenty UI, daty i
  składnia Wiki;
- `feature/diary` - notatki, tagi, kategorie i pole szybkiego wpisu
  (`composer/QuickCapture` zamienia tekst na akcję: notatkę, zadanie, tag lub
  nawigację);
- `feature/people` - osoby i wzmianki `@`;
- `feature/todo` - zadania ToDo;
- `feature/routines` - rutyny dzienne;
- `feature/pictograms` - pictogramy;
- `feature/filmweb` - wyszukiwanie i wpisy Filmweb;
- `feature/export` - przygotowanie notatki Wiki;
- `feature/settings` - ekran ustawień (motyw, adres Wiki);
- `ui` - aktywność, nawigacja i widget.

Nowa funkcja powinna powstać jako osobny pakiet `feature/<nazwa>` z własnym
modułem Koin dołączonym w `di/AppModule.kt`.

Warstwa UI działa w modelu jednokierunkowego przepływu danych (MVI). Każdy
ekran ma własny `ViewModel` (rejestrowany przez `viewModel { }` w module
funkcji), który wystawia `StateFlow<XxxUiState>` i przyjmuje zdarzenia przez
`onIntent(XxxIntent)`. Funkcja `XxxRoute` pobiera `ViewModel` przez
`koinViewModel()` i przekazuje stan do bezstanowego ekranu Compose.
Wybrany dzień jest współdzielony przez `SelectedDateStore`.

Powłokę aplikacji tworzy `ui/WikiApp.kt`: `Scaffold` z górnym paskiem,
dolnym `NavigationBar`, arkuszami `ModalBottomSheet` i kalendarzem
`DatePickerDialog`. Zakładki są opisane w `ui/navigation/TopLevelDestination.kt`
(trasa `AppRoute`, etykieta, tytuł, ikony), a `ui/navigation/WikiNavHost.kt`
mapuje trasy na ekrany i zachowuje stan zakładek przy przełączaniu. Arkusze są
opisane przez `AppSheet` i przetrwają zmianę konfiguracji. `MainActivity`
ustawia motyw według `Config.darkTheme` (`null` oznacza motyw systemowy),
dopasowuje kolory pasków systemowych i uruchamia `WikiApp`. Ekran ustawień to
trasa `AppRoute.Settings` poza dolnym paskiem. Nowa zakładka to wpis w
`TopLevelDestination` i odpowiadający mu `composable<AppRoute.X>` w
`WikiNavHost`.

Nową komendę pola szybkiego wpisu dodaje się jako wpis `ComposerCommand`
(słowo kluczowe, ikona, opis, cel nawigacji) i ewentualnie wariant `Capture`
obsłużony w `AddScreen`; podpowiedzi i menu ⊕ korzystają z tej samej listy.

Eksport do Wiki składa `WikiExporter` z wpisów dostarczanych przez funkcje.
Aby nowa funkcja pojawiła się w notatce Wiki, wystarczy w jej module Koin
zarejestrować implementację `WikiExportContributor` (`bind
WikiExportContributor::class`). Contributor zwraca `Flow` z liniami grup
(`GroupLine`) lub załącznikami dnia (`DayAttachment`), a `order` ustala
kolejność renderowania.

## Budowanie lokalne (opcjonalnie)

Gotowy APK nie wymaga kompilowania. Jeśli potrzebny jest nowy build, wymagane
są Android SDK z platformą 34 oraz JDK 17 lub nowsze.

Przykładowe polecenia dla środowiska z SDK w
`/opt/homebrew/share/android-commandlinetools`:

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT="$ANDROID_HOME"

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
