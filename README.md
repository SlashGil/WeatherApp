# 🌤️ WeatherApp – Native Android 7-Day Forecast App

A native Android application built in **Kotlin** displaying a **7-day daily weather forecast** using **MVVM + Clean Architecture**, **Jetpack Compose**, **Room**, **Retrofit**, and **Dagger Hilt**.

---

## 📱 Features & Highlights

- **7-Day Daily Forecast:** Displays weather icons, human-readable WMO weather code descriptions, max/min temperatures, and precipitation probability.
- **Offline-First Caching:** Automatically persists weather forecasts in Room (`ForecastDatabase`). Survives app restarts and process death in airplane mode.
- **Non-Blocking Refresh Errors:** When cached data exists and a network refresh fails, the cached forecast stays visible on screen while a non-blocking `Snackbar` alerts the user.
- **GPS Location & Fallback:** Reads device location via `FusedLocationProviderClient`. Falls back gracefully to default city (**New York, NY: `40.7128, -74.0060`**) when location permissions are denied or unavailable.
- **Pull-to-Refresh Gesture:** Supports intuitive drag/swipe-down gesture on the forecast screen to trigger refresh.
- **Hidden Developer Mode & A/B Testing:**
  - Double-tapping the helper text on the **Empty State screen** activates **Developer Mode** and reveals a **Gear Icon (`Settings`)** in the `TopAppBar`.
  - Tapping the Gear Icon opens a dedicated `SettingsActivity` backed by Room A/B test flags (`put_default_city`, `give_coords`, `select_city`).
- **Zero API Keys Required:** Powered by Open-Meteo Weather and Geocoding APIs.

---

## 🏗️ Clean Architecture & Data Flow

```text
com.slashgil.weather
├── core/
│   ├── location/            # LocationTracker (FusedLocationProviderClient wrapper)
│   └── util/                # WmoWeatherCodeMapper, DateFormatter
├── data/
│   ├── local/
│   │   ├── dao/             # ForecastDao, ABTestDao
│   │   ├── entity/          # ForecastEntity, DailyForecastEntity, ABTestEntity
│   │   └── WeatherDatabase.kt # Room database with A/B seed callback
│   ├── remote/
│   │   ├── api/             # OpenMeteoForecastApi, OpenMeteoGeocodingApi
│   │   └── dto/             # ForecastResponseDto, GeocodingResponseDto
│   ├── mapper/              # WeatherMappers.kt
│   └── repository/          # WeatherRepositoryImpl.kt, ABTestRepositoryImpl.kt
├── domain/
│   ├── model/               # WeeklyForecast, DailyForecast, Coordinates, ABTestFlag, LocationResolutionResult
│   ├── repository/          # WeatherRepository, ABTestRepository
│   ├── strategy/            # LocationResolutionStrategy, LocationStrategyFactory, Concrete Strategies
│   └── usecase/             # GetWeeklyForecastUseCase, ObserveABTestsUseCase, ToggleABTestUseCase, etc.
├── presentation/
│   ├── main/                # MainActivity, WeatherScreen, WeatherViewModel, WeatherUiState
│   │   └── components/      # ForecastDayCard, EmptyStateView, CoordsInputModal, CityInputModal
│   └── settings/            # SettingsActivity, SettingsScreen, SettingsViewModel
└── di/                      # DatabaseModule, NetworkModule, RepositoryModule, LocationModule
```

### Offline Caching Workflow
1. **Startup / Offline Load:** `WeatherViewModel` collects from `ObserveCachedForecastUseCase`, which observes `ForecastDao.getForecastWithDailyFlow()`. Room immediately emits the last saved forecast if available.
2. **Network Refresh:** `RefreshWeeklyForecastUseCase` determines location via active `LocationResolutionStrategy`, fetches the fresh 7-day forecast from `OpenMeteoForecastApi`, and saves it to Room via `ForecastDao.saveForecast(...)`.
3. **Failure Handling:** If network fails and cached data exists in Room, the cached UI remains visible while a non-blocking `Snackbar` informs the user of the network error.

---

## 🎨 Explicit Design Patterns

| Pattern Category | Pattern Name | Class / File Location | Purpose & Rationale |
| :--- | :--- | :--- | :--- |
| **Creational** | **Factory** | [`LocationStrategyFactory`](app/src/main/java/com/slashgil/weather/domain/strategy/LocationStrategyFactory.kt) | Dynamically instantiates the appropriate `LocationResolutionStrategy` implementation based on active Room A/B test flags (`put_default_city`, `give_coords`, `select_city`) and location availability. |
| **Structural** | **Repository & Adapter** | [`WeatherRepositoryImpl`](app/src/main/java/com/slashgil/weather/data/repository/WeatherRepositoryImpl.kt)<br>[`WeatherMappers`](app/src/main/java/com/slashgil/weather/data/mapper/WeatherMappers.kt) | Adapts Open-Meteo's parallel-array DTOs and Room relational entities into clean domain models (`WeeklyForecast`) while managing offline-first caching logic. |
| **Behavioral** | **Strategy** | [`LocationResolutionStrategy`](app/src/main/java/com/slashgil/weather/domain/strategy/LocationResolutionStrategy.kt) | Encapsulates interchangeable location resolution algorithms (`DeviceGpsStrategy`, `DefaultCityStrategy`, `ManualCoordsStrategy`, `ManualCityStrategy`) behind a common interface, decoupling location logic from domain and UI layers. |

---

## 🛠️ A/B Testing & Hidden Developer Mode Guide

1. **Activate Developer Mode:**
   - Launch app without internet or database data to view the **Empty State Screen**.
   - Double-tap the message text *"Check your network connection and pull to refresh."*
   - A **Gear Icon (`Settings`)** will appear in the top-right corner of the `TopAppBar`.

2. **Configure A/B Tests in `SettingsActivity`:**
   - Tap the **Gear Icon** to launch `SettingsActivity`.
   - Toggle one of the 3 A/B test options (toggling one `true` automatically resets the other two to `false`):
     - **`put_default_city`**: Skips GPS and fetches forecast for hardcoded default city (**New York, NY**).
     - **`give_coords`**: Shows a modal dialog requesting custom **Latitude** (`-90..90`) and **Longitude** (`-180..180`).
     - **`select_city`**: Shows a modal dialog requesting city search in `"city,state,country"` format resolved via Open-Meteo Geocoding API.
     - **All flags `false`**: Standard GPS mode using `FusedLocationProviderClient`.

---

## 🚀 Building & Running

### Requirements
- **JDK:** Java 11 / 17
- **Android Studio:** Ladybug (2024.2) or newer
- **Compile SDK:** 37
- **Min SDK:** 29

### Gradle Commands
```bash
# Build Debug APK
./gradlew assembleDebug

# Run All Unit Tests
./gradlew testDebugUnitTest
```

---

## 🔮 Future Improvements / Next Steps

1. **Hourly Forecast Breakdown:** Add detailed 24-hour temperature and precipitation graphs using Compose Canvas / Charts.
2. **Dynamic Dark / Light Themes:** Expand Material 3 theme palette with dynamic color extraction based on current weather conditions (e.g. sunny, stormy, snowy).
3. **Multi-Location Favorites:** Allow users to save multiple favorite cities and swipe between weather cards.

---

## 📡 API Endpoints (Open-Meteo)

1. **7-Day Weather Forecast:**
   ```http
   GET https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&forecast_days=7
   ```
2. **City Geocoding Search:**
   ```http
   GET https://geocoding-api.open-meteo.com/v1/search?name={cityName}&count=5&language=en&format=json
   ```
