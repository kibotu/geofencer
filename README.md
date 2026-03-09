# Geofencer

[![Android CI](https://github.com/kibotu/geofencer/actions/workflows/build.yml/badge.svg)](https://github.com/kibotu/geofencer/actions/workflows/build.yml) [![Maven Central Version](https://img.shields.io/maven-central/v/net.kibotu/geofencer)](https://central.sonatype.com/artifact/net.kibotu/geofencer) [![](https://jitpack.io/v/kibotu/geofencer.svg)](https://jitpack.io/#kibotu/geofencer) [![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23) [![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Convenience library to receive user location updates and geofence events with minimal effort. Survives app kills and device reboots.

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Initialization](#initialization)
- [Quick Start](#quick-start)
  - [Geofencing](#geofencing)
  - [Location Tracking](#location-tracking)
- [API Reference](#api-reference)
- [Permissions](#permissions)
- [Maestro UI Tests](#maestro-ui-tests)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- Kotlin-first API with DSL builders and coroutines
- Geofence transitions: enter, exit, dwell
- Continuous location updates via `SharedFlow`
- Survives app kill and device reboot (via WorkManager)
- Auto-initializes via built-in ContentProvider (no external dependencies)
- Configurable update intervals, displacement, and priority
- Custom actions for geofence and location events
- minSdk 23, targets Android 15+

## Installation

### Maven Central

```kotlin
implementation("net.kibotu:geofencer:3.0.0")
```

### JitPack

Add the JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency:

```kotlin
implementation("com.github.kibotu:geofencer:latest")
```

## Initialization

The library auto-initializes using a `ContentProvider` — no manual setup needed.

### Customizing init order

The `ContentProvider` uses `android:initOrder` to control initialization priority relative to other providers. The default value is `100`. Higher values mean the provider is initialized earlier.

Override the priority in your app's `res/values/integers.xml` (or any values resource file):

```xml
<resources>
    <integer name="geofencer_init_priority">200</integer>
</resources>
```

### Disabling auto-initialization

Add a boolean resource override in your app's `res/values/` to disable the `ContentProvider` from initializing the library:

```xml
<resources>
    <bool name="geofencer_auto_init_enabled">false</bool>
</resources>
```

Then initialize manually in your `Application.onCreate()`:

```kotlin
Geofencer.init(applicationContext)
```

Alternatively, you can remove the provider entirely via manifest merging:

```xml
<provider
    android:name="net.kibotu.geofencer.internal.GeofencerInitializer"
    android:authorities="${applicationId}.geofencer-init"
    tools:node="remove" />
```

## Quick Start

### Geofencing

#### 1. Add a geofence

```kotlin
Geofencer.add {
    latitude = 52.520008
    longitude = 13.404954
    radius = 200.0
    label = "Berlin"
    message = "Welcome to Berlin!"
    transitions = setOf(Geofence.Transition.Enter, Geofence.Transition.Exit)
    action<NotificationAction>()
}
```

#### 2. Observe events

```kotlin
lifecycleScope.launch {
    Geofencer.events.collect { event ->
        Log.d("Geofence", "${event.transition.name} ${event.geofence.label}")
    }
}
```

#### 3. Create a custom action

Actions run even when the app is killed:

```kotlin
class NotificationAction : GeofenceAction() {
    override fun onTriggered(context: Context, event: GeofenceEvent) {
        // send notification, log analytics, etc.
    }
}
```

#### 4. Manage geofences

```kotlin
// list all active geofences
val all: List<Geofence> = Geofencer.geofences.value

// observe changes
Geofencer.geofences.collect { list -> /* update UI */ }

// look up by id
val fence: Geofence? = Geofencer["some-id"]

// remove
Geofencer.remove("some-id")
Geofencer.removeAll()
```

### Location Tracking

#### 1. Start tracking

```kotlin
LocationTracker.start(context) {
    interval = 10.seconds
    fastest = 5.seconds
    displacement = 50f
    action<LocationLogAction>()
}
```

#### 2. Observe locations

```kotlin
lifecycleScope.launch {
    LocationTracker.locations.collect { location ->
        Log.d("Location", "${location.latitude}, ${location.longitude}")
    }
}
```

#### 3. Create a custom action

```kotlin
class LocationLogAction : LocationAction() {
    override fun onUpdate(context: Context, result: LocationResult) {
        // persist, upload, etc.
    }
}
```

#### 4. Stop tracking

```kotlin
LocationTracker.stop(context)
```

## API Reference

| Class | Description |
|---|---|
| `Geofencer` | Singleton to add/remove geofences and observe events via `SharedFlow` |
| `Geofence` | Data class holding coordinates, radius, transitions, and metadata |
| `GeofenceBuilder` | DSL builder for `Geofence` instances |
| `GeofenceEvent` | Emitted event containing the geofence, transition type, and triggering location |
| `GeofenceAction` | Abstract class for custom geofence event handlers (survives app kill) |
| `LocationTracker` | Singleton to start/stop location updates and observe via `SharedFlow` |
| `LocationConfig` | DSL builder for location request parameters |
| `LocationAction` | Abstract class for custom location update handlers (survives app kill) |

## Permissions

The library declares these permissions in its manifest (merged automatically):

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Your app must request `ACCESS_FINE_LOCATION` (and `ACCESS_BACKGROUND_LOCATION` on Android 10+) at runtime before adding geofences or starting location tracking.

## Maestro UI Tests

The demo app includes [Maestro](https://maestro.mobile.dev/) flows in the `.maestro/` directory.

### Prerequisites

1. Install Maestro:

```bash
curl -Ls "https://get.maestro.mobile.dev" | bash
```

2. Connect an Android device or start an emulator.

3. Install the debug build:

```bash
./gradlew :app:installDebug
```

### Running the tests

Run all flows in the `.maestro/` directory:

```bash
maestro test .maestro/
```

Run a specific flow:

```bash
maestro test .maestro/geofence_full_test.yaml
```

### What the test covers

| Area | Details |
|------|---------|
| Recording toggle | Toggles location tracking off and on |
| Battery / Performance | Switches between high-frequency and battery-saving mode |
| My location | Centers the map on the current position |
| Map styles | Cycles through all 5 styles (Pokemon GO, Steampunk, Light, Dark 3D, Satellite) |
| Event log | Opens the bottom sheet, verifies it shows, clears entries, closes |
| Geofence at current location | Runs the full wizard (location → radius → message) |
| Search + geofence | Searches for "Alexanderplatz Berlin", selects the result, completes the wizard |

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

```
MIT License

Copyright (c) 2026 Geofencer Developers
```

See [LICENSE](LICENSE) for the full text.
