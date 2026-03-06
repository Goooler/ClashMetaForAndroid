# AGENTS.md — Guidance for AI Coding Agents

This file provides context and conventions for AI coding agents working in this repository.

## Project Overview

**Clash Meta for Android (CMFA)** is an Android GUI for the [Clash.Meta (mihomo)](https://github.com/MetaCubeX/mihomo) proxy kernel.
It bridges a Golang-based networking library with an Android UI, using the NDK and CGo to compile the native kernel.

- Minimum Android SDK: **26** (Android 8.0)
- Target SDK: **35**
- Supported ABIs: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`

## Repository Layout

```
ClashMetaForAndroid/
├── app/           # Main Android application (Activities, UI wiring)
├── core/          # Native bridge: wraps the Golang kernel via CGo/JNI
├── service/       # Android Services, profile management, Room database
├── design/        # UI components and data-binding layouts
├── common/        # Shared utilities (extensions, models, constants)
├── hideapi/       # Stub declarations for hidden Android APIs
├── gradle/
│   └── libs.versions.toml   # Version catalog (single source of truth for deps)
├── build.gradle.kts          # Root build script (AGP/Kotlin-wide config)
└── settings.gradle.kts       # Module declarations and feature previews
```

### Module dependency graph

```
app ──► core, service, design, common
service ──► core, common
design ──► core, service, common
core ──► common
```

`hideapi` is `compileOnly` for `app`; it is never shipped in the APK.

## Build System

| Tool | Version |
|------|---------|
| Gradle | 9.4 (wrapper) |
| Android Gradle Plugin (AGP) | 9.x |
| Kotlin | 2.x |
| JVM target | 21 |
| NDK | declared in `build.gradle.kts` (`ndkVersion`) |
| CMake | 4.x (declared in `core/build.gradle.kts`) |
| Go | required on PATH (kernel is compiled by the `golang` Gradle plugin) |

Feature previews enabled in `settings.gradle.kts`:
- `TYPESAFE_PROJECT_ACCESSORS` — use `projects.core` instead of `":core"`
- `STABLE_CONFIGURATION_CACHE`

## Build Flavors

Two product flavors share the `feature` dimension:

| Flavor | Application ID suffix | Notes |
|--------|-----------------------|-------|
| `meta` | `.meta` (default) | Production flavor, published to F-Droid |
| `alpha` | `.alpha` | Pre-release / testing flavor |

Both flavors add `src/foss/java` as an additional source directory.

## How to Build

### Prerequisites

1. JDK 21 (`JAVA_HOME` must point to it)
2. Android SDK with `sdk.dir` in `local.properties`
3. Go toolchain on `PATH` (for compiling the kernel submodule)
4. Initialize submodules:

   ```bash
   git submodule update --init --recursive
   ```

### Common Gradle tasks

```bash
# Assemble the meta release APK (production)
./gradlew app:assembleMetaRelease

# Assemble the alpha release APK
./gradlew app:assembleAlphaRelease

# Debug build (no signing config required)
./gradlew app:assembleMetaDebug

# Download geo-data assets (runs automatically before preBuild)
./gradlew app:downloadGeoFiles

# Clean build outputs
./gradlew clean
```

> **Note:** The `downloadGeoFiles` task fetches `geoip.metadb`, `geosite.dat`, and `GeoLite2-ASN.mmdb`
> from GitHub releases into `app/src/main/assets/` before every build. Network access is required.

### Optional: custom application ID

Add to `local.properties`:

```properties
custom.application.id=com.my.compile.clash
remove.suffix=true
```

### Optional: release signing

Create `signing.properties` in the project root:

```properties
keystore.path=/path/to/keystore/file
keystore.password=<store password>
key.alias=<key alias>
key.password=<key password>
```

## Testing

There are currently no local unit or instrumentation tests in this repository.
Validation is done through the CI build workflow (`.github/workflows/build.yml`).

## Coding Conventions

- **Language:** Kotlin for all Android code; Go for the kernel (submodule).
- **Style:** Follow the project's EditorConfig (`.editorconfig`) and the IntelliJ/Android Studio "Project" code style profile.
  - Indent: 4 spaces for `.kt`/`.kts`/`.java`; 2 spaces for everything else.
  - Trailing commas allowed in Kotlin (both declaration and call sites).
  - No star imports (threshold set to `Integer.MAX_VALUE`).
- **Kotlin code style:** `kotlin.code.style=official` (set in `gradle.properties`).
- **Build scripts:** Use version-catalog aliases (`libs.versions.toml`) for all dependency versions and plugin IDs. Never hard-code version strings in `build.gradle.kts` files.
- **Module namespaces:** Follow the pattern `com.github.kr328.clash.<module-name>` (set automatically by the root build script).
- **IPC/Binder:** The project uses [kaidl](https://github.com/kr328/kaidl) (KSP-processed) for inter-process communication between `app` and `service`.
- **Database:** Room is used in `service` for profile persistence. Use KSP (`ksp`) for annotation processing in modules that use Room or kaidl.
- **Data binding:** `buildFeatures { dataBinding = true }` is enabled in `app` and `design`. Use Android Data Binding in layouts; avoid View Binding.

## Key Plugin Aliases (from `gradle/libs.versions.toml`)

| Alias | Plugin ID |
|-------|-----------|
| `android.application` | `com.android.application` |
| `android.library` | `com.android.library` |
| `android.legacyKapt` | `com.android.legacy-kapt` |
| `kotlin.serialization` | `org.jetbrains.kotlin.plugin.serialization` |
| `ksp` | `com.google.devtools.ksp` |
| `golang` | `io.github.goooler.golang` |
| `download` | `de.undercouch.download` |

> **AGP 9 note:** `kotlin-android` is built into AGP and does not need to be applied explicitly.
> Use `com.android.legacy-kapt` (alias `android.legacyKapt`) instead of `kotlin-kapt`.

## CI Workflows

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `build.yml` | push to `trunk`, PRs, manual | Assembles `metaRelease`; publishes nightly to GitHub Releases |
| `release.yml` | manual | Tags and publishes a versioned release |
| `update-go-dependencies.yml` | automatic (kernel update) | Updates Go deps and opens a PR |

## Automation / Intent API

App package name: `com.github.metacubex.clash.meta`

| Action | Intent target | Intent action |
|--------|---------------|---------------|
| Toggle service | `ExternalControlActivity` | `com.github.metacubex.clash.meta.action.TOGGLE_CLASH` |
| Start service | `ExternalControlActivity` | `com.github.metacubex.clash.meta.action.START_CLASH` |
| Stop service | `ExternalControlActivity` | `com.github.metacubex.clash.meta.action.STOP_CLASH` |
| Import profile | URL scheme | `clash://install-config?url=<encoded URI>` |

## Submodule

The Clash.Meta (mihomo) kernel lives at `core/src/foss/golang/clash` and tracks the `Meta` branch of
`https://github.com/MetaCubeX/mihomo`. When making kernel changes, submit PRs to that upstream
repository rather than editing the submodule directly.
