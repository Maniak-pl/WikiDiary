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
- **Notatki Filmweb** - chip `🎬 Filmweb` otwiera natywne wyszukiwanie w
  dolnym panelu aplikacji. Wyniki są pokazywane jako przewijana lista
  maksymalnie 10 filmów i seriali z okładką, tytułem oraz rokiem. Po wybraniu
  pozycji można poprawić jej metadane, a następnie zapisać zwykłą notatkę razem
  ze strukturalnymi metadanymi Filmwebu.
- **Przygotowanie wpisu Wiki** - notatki są grupowane według daty, tagów i
  kategorii, a wygenerowany tekst można skopiować do schowka. Notatki Filmweb
  zawierają klikalny link do wybranej strony oraz znacznik grafiki DokuWiki
  dodany po listach danego dnia.
- **Widget ekranu głównego** - pokazuje czas od ostatnio zapisanej notatki,
  wizualizuje upływ 24 godzin i otwiera aplikację po kliknięciu.

Przy pierwszym utworzeniu lokalnej bazy aplikacja dodaje przykładowe tagi,
kategorie i rutyny, m.in. `Book`, `Meeting`, `Work`, `Health` oraz `10000
kroków`.

## Ekrany aplikacji

Górny pasek udostępnia cztery obszary:

| Ekran | Zastosowanie |
| --- | --- |
| **Add Note** | Wpisywanie treści i zapisywanie jej pod wybranym tagiem. |
| **Routines** | Zarządzanie i wykonywanie codziennych rutyn. |
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
5. Użyj **Prepare Note**, aby wygenerować tekst i skopiować go do schowka.
6. Wklej przygotowany tekst do docelowej strony Wiki.

## Dane i prywatność

- Dane są przechowywane w lokalnej bazie Room `wiki_database`.
- Pozycje Filmweb są przechowywane w katalogu `film_table`, a relacje między
  nimi i notatkami w `film_note_table`; migracja bazy zachowuje istniejące
  notatki, tagi, kategorie i rutyny.
- Ustawienia pomocnicze, takie jak dzień ostatniego uruchomienia i czas ostatniej
  notatki, są przechowywane w `SharedPreferences`.
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
| Wersja aplikacji | `1.0` |
| `versionCode` | `1` |
| Typ builda | `debug` |
| Pakiet | `pl.maniak.wikidiary` |
| Źródło APK | `develop` + natywna lista wyników Filmweb |
| SHA-256 APK | `78ecfa664036ca78981238ae88c4ed6e1d57e4dbaf97fc1c1e13a018467941e1` |

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
  wpisów bezpośrednio na serwer Wiki.
- Brak synchronizacji między urządzeniami i kont użytkowników.
- Integracja Filmweb korzysta z nieoficjalnego, nieudokumentowanego API.
  Filmweb może zmienić jego strukturę, ograniczyć dostęp lub wyłączyć endpointy
  bez zapowiedzi. W takim przypadku panel pokazuje błąd z opcją ponowienia.
- Filmweb wymaga połączenia z internetem tylko podczas wyszukiwania i odczytu
  wyników. Zapisane notatki i ich metadane pozostają lokalne.
