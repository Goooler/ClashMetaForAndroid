## Contributing to Tabby

Thank you for contributing to this project.

## Project Overview

**Tabby** is an Android GUI application for [Mihomo](https://github.com/MetaCubeX/mihomo)
(formerly Clash Meta), a rule-based proxy kernel.
The app is written in Kotlin with Jetpack Compose for the UI, and embeds a compiled Go binary
(`libclash.so`) built from the Mihomo submodule.

- **Application ID**: `io.github.goooler.tabby`
- **Min SDK**: 28 | **Compile SDK**: 37 | **Target SDK**: 35
- **Default branch**: `trunk`

## Module Structure

```text
Tabby/
├── app/          # Application shell: MainActivity, MainApplication, AppModule (Koin), BroadcastReceivers, TileService
├── core/         # Mihomo bridge: Go/JNI bindings, data models, C++ CMake layer
│                 #   └── src/foss/golang/clash/  (git submodule → MetaCubeX/mihomo)
├── service/      # Background VPN service, Room database, IPC via kaidl, OkHttp profile fetching
├── common/       # Shared constants, store providers, and utility extensions; includes Android-specific helpers
├── glue/         # Dependency-injection wiring via Koin; exposes api() of core, service, common
└── ui/           # Shared UI components, theme, icons (also a library module)
    ├── crash/    # Crash reporting screen
    ├── home/     # Dashboard / tunnel toggle
    ├── log/      # Real-time logcat viewer
    ├── proxy/    # Proxy group selector
    ├── profile/  # Profile management
    └── settings/ # App settings
```

### Module dependency graph (simplified)

```text
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
| Build      | Gradle 9+ with Kotlin DSL, Version Catalog (`gradle/libs.versions.toml`) |

## Before You Start

- Search existing issues and pull requests before opening a new one.
- For bug reports, include steps to reproduce, expected behavior, and logs/screenshots when
  possible.
- For feature requests, explain the use case and scope clearly.

## Development Setup

1. Fork this repository and create a branch from `trunk`.
2. Clone with submodules (or initialize them after clone):
   ```bash
   git clone --recurse-submodules https://github.com/Goooler/Tabby.git
   # or after a plain clone:
   git submodule update --init --recursive
   ```
3. Install required tools:
   - JDK 21 or above
   - Android SDK (set `sdk.dir` in `local.properties`)
   - NDK `29.0.14206865` (installed automatically by AGP)
   - CMake 4.x
   - Go 1.26+
4. Create `local.properties` in the project root:
   ```properties
   sdk.dir=/path/to/android-sdk
   # Optional: skip downloading geo data files if they already exist
   # skip.downloadGeoFiles=true
   ```

## Build Commands

```bash
# Check and fix code style (required before every commit)
./gradlew spotlessApply

# Build a debug APK (required before every commit)
./gradlew app:assembleDebug

# Build a release APK (don't have to run this in general developments)
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

```text
com.github.kr328.clash.<module-name>
```

For example:

- `:app` → `com.github.kr328.clash.app`
- `:glue` → `com.github.kr328.clash.glue`
- `:ui:home` → `com.github.kr328.clash.home`

This is enforced automatically in the root `build.gradle.kts` via
`namespace = "com.github.kr328.clash.${project.name}"`.

## Dependency Injection (Koin)

- The current Koin module definition lives in `app/AppModule.kt`.
- `MainApplication` starts Koin with `appModule`, so add or update DI
  bindings there unless the application startup is changed.
- UI modules should consume injected dependencies via `koinInject()` /
  `getKoin()` in Compose; avoid constructor injection in `Activity`.

## IPC (kaidl)

- The `service` module defines IPC interfaces using `kaidl` annotations.
- Generated code is produced by `ksp(libs.kaidl.compiler)` at build time.
- Do not hand-edit generated files.

## Signing

- The repository ships `app/release.keystore` with its credentials already
  configured in `app/build.gradle.kts`.
- **Both debug and release builds** are signed automatically with this keystore;
  no extra files or manual configuration are required.
- Do **not** replace or remove `app/release.keystore`, and do not commit any
  personal keystores or override signing credentials.

## String Resources / Localization

When adding or modifying string/text resources, please ensure that:
1. **Alphabetical Sorting**: All resource entries must be sorted alphabetically by their `name` keys.
2. **Single-Line Format**: Each resource entry must be written entirely on a single line (no manual line wraps or breaks), and do not modify any other unrelated lines.
3. **Complete Translations**: For any translatable content, make sure to completely supplement the internationalized translations for all supported languages in the project.

## CI / Automated Checks

GitHub Actions runs on every push to `trunk` and on every pull request:

| Job            | Command                         | Description                                               |
|----------------|---------------------------------|-----------------------------------------------------------|
| `check-style`  | `./gradlew spotlessCheck`       | Fails if formatting issues exist                          |
| `lint`         | `./gradlew lintDebug`           | Runs Android lint checks for the debug variant            |
| `build`        | `./gradlew app:assembleRelease` | Full release build including Go cross-compilation         |
| `final-status` | —                               | Required branch-protection status combining all CI checks |

A nightly pre-release is published automatically on pushes to `trunk`.

## Pull Request Guidelines

- Branch from `trunk`.
- Keep changes focused and small.
- Include a clear description of what changed and why.
- Run `./gradlew spotlessApply` and `./gradlew app:assembleDebug` locally
  before opening a PR.
- Link related issues when applicable.
- Update docs when behavior or developer workflow changes.
