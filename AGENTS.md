# AGENTS.md

This file provides guidance for AI coding agents working in this repository.

## Project Overview

**Tabby** is an Android GUI application for [Mihomo](https://github.com/MetaCubeX/mihomo)
(formerly Clash Meta), a rule-based proxy kernel.
The app is written in Kotlin with Jetpack Compose for the UI, and embeds a compiled Go binary
(`libclash.so`) built from the Mihomo submodule.

- **Application ID**: `io.github.goooler.tabby`
- **Min SDK**: 28 | **Compile SDK**: 37 | **Target SDK**: 35
- **Default branch**: `trunk`

## Module Structure

```
Tabby/
├── app/          # Application shell: MainActivity, MainApplication, AppModule (Koin), BroadcastReceivers, TileService
├── core/         # Mihomo bridge: Go/JNI bindings, data models, C++ CMake layer
│                 #   └── src/foss/golang/clash/  (git submodule → MetaCubeX/mihomo)
├── service/      # Background VPN service, Room database, IPC via kaidl, OkHttp profile fetching
├── common/       # Shared constants, store providers, utility extensions (no Android framework dependencies)
├── glue/         # Dependency-injection wiring via Koin; exposes api() of core, service, common
└── ui/           # Compose screens (multi-module)
    ├── crash/    # Crash reporting screen
    ├── home/     # Dashboard / tunnel toggle
    ├── log/      # Real-time logcat viewer
    ├── proxy/    # Proxy group selector
    ├── profile/  # Profile management
    └── settings/ # App settings
```

### Module dependency graph (simplified)

```
app → glue → core → common
               └── service → core
                          └── common
app → ui/*
```

## Tech Stack

| Layer      | Technology                                                               |
|------------|--------------------------------------------------------------------------|
| Language   | Kotlin 2.x, Go 1.26+                                                     |
| UI         | Jetpack Compose + Material 3, Navigation3                                |
| DI         | Koin 4                                                                   |
| IPC        | kaidl (custom AIDL generator)                                            |
| DB         | Room                                                                     |
| Network    | OkHttp 5                                                                 |
| Core proxy | Mihomo (Go submodule via golang-gradle-plugin)                           |
| Code style | ktfmt (Google style) via Spotless                                        |
| Build      | Gradle 8+ with Kotlin DSL, Version Catalog (`gradle/libs.versions.toml`) |

## Development Setup

1. **Clone with submodules**:
   ```bash
   git clone --recurse-submodules https://github.com/Goooler/Tabby.git
   # or after a plain clone:
   git submodule update --init --recursive
   ```

2. **Required toolchain**:
  - JDK 21
  - Android SDK (set `sdk.dir` in `local.properties`)
  - NDK `29.0.14206865` (installed automatically by AGP)
  - CMake 4.x
  - Go 1.26+

3. **`local.properties`** (create in project root):
   ```properties
   sdk.dir=/path/to/android-sdk
   # Optional: skip downloading geo data files if they already exist
   # skip.downloadGeoFiles=true
   ```

## Build Commands

```bash
# Check code style (required before every PR)
./gradlew spotlessCheck

# Auto-fix style violations
./gradlew spotlessApply

# Build a debug APK (skips geo file download by default if present)
./gradlew app:assembleDebug

# Build a release APK (full validation; requires signing config)
./gradlew app:assembleRelease

# Run all checks
./gradlew check
```

> **Note**: The first build downloads three geo database files from
> `MetaCubeX/meta-rules-dat` into `app/src/main/assets/`. Set
> `skip.downloadGeoFiles=true` in `local.properties` to skip this when the
> files already exist.

## Code Style & Conventions

- **Formatter**: ktfmt (Google style). Run `./gradlew spotlessApply` to fix.
  All Kotlin files in `src/**/*.kt` and `*.gradle.kts` are covered.
- **Warnings as errors**: `allWarningsAsErrors = true` for all Kotlin
  compilations. Fix every warning before merging.
- **JVM target**: Java 21.
- **Opt-ins** applied project-wide:
  - `kotlin.uuid.ExperimentalUuidApi`
  - `androidx.compose.foundation.ExperimentalFoundationApi`
  - `androidx.compose.material3.ExperimentalMaterial3Api`
- **Compiler flags**: `-Xcontext-sensitive-resolution`, `-Xexplicit-backing-fields`.
- **Imports**: No wildcard imports (star import threshold is `Int.MAX_VALUE`).
  `ktfmt` manages ordering automatically.
- **Trailing commas**: allowed in both declarations and call sites.
- **Indentation**: 2 spaces for Kotlin/Gradle, 4 spaces for C/CMake.

## Namespace Convention

Every Android module's namespace follows the pattern:

```
com.github.kr328.clash.<module-name>
```

For example:

- `:app` → `com.github.kr328.clash.app`
- `:glue` → `com.github.kr328.clash.glue`
- `:ui:home` → `com.github.kr328.clash.home`

This is enforced automatically in the root `build.gradle.kts` via
`namespace = "com.github.kr328.clash.${project.name}"`.

## Dependency Injection (Koin)

- All DI modules are defined in the `glue` module.
- `app/AppModule.kt` provides app-level bindings and is started in
  `MainApplication`.
- UI modules should consume injected dependencies via `koinInject()` /
  `getKoin()` in Compose; avoid constructor injection in `Activity`.

## IPC (kaidl)

- The `service` module defines IPC interfaces using `kaidl` annotations.
- Generated code is produced by `ksp(libs.kaidl.compiler)` at build time.
- Do not hand-edit generated files.

## Signing

- Debug builds use the default debug keystore.
- Release builds read from `signing.properties` (not committed). Create it
  locally if you need signed release APKs:
  ```properties
  keystore.password=<password>
  key.alias=<alias>
  key.password=<key-password>
  ```

## CI / Automated Checks

GitHub Actions runs on every push to `trunk` and on every pull request:

| Job            | Command                         | Description                                           |
|----------------|---------------------------------|-------------------------------------------------------|
| `check-style`  | `./gradlew spotlessCheck`       | Fails if formatting issues exist                      |
| `build`        | `./gradlew app:assembleRelease` | Full release build including Go cross-compilation     |
| `final-status` | —                               | Required branch-protection status combining both jobs |

A nightly pre-release is published automatically on pushes to `trunk`.

## Pull Request Guidelines

- Branch from `trunk`.
- Keep changes focused and small.
- Run `./gradlew spotlessCheck` and `./gradlew app:assembleRelease` locally
  before opening a PR.
- Link related issues in the PR description.
- Update docs when behavior or developer workflow changes.
- Do **not** commit `local.properties`, `signing.properties`, or
  `release.keystore`.
