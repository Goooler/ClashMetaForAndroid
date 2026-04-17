## Contributing to Mihomo for Android

Thank you for contributing to this project.

### Before you start

- Search existing issues and pull requests before opening a new one.
- For bug reports, include steps to reproduce, expected behavior, and logs/screenshots when possible.
- For feature requests, explain the use case and scope clearly.

### Development setup

1. Fork this repository and create a branch from `trunk`.
2. Initialize submodules:

   ```bash
   git submodule update --init --recursive --force
   ```

3. Install required tools:
   - JDK 21
   - Android SDK
   - CMake
   - Go 1.26

4. Create `local.properties` in the project root:

   ```properties
   sdk.dir=/path/to/android-sdk
   ```

### Code style and validation

Please use Android Studio or IntelliJ IDEA with the project style settings.

Run checks before submitting a pull request:

```bash
./gradlew spotlessCheck
./gradlew app:assembleMetaRelease
```

### Pull request guidelines

- Keep changes focused and small.
- Include a clear description of what changed and why.
- Link related issues when applicable.
- Update docs when behavior or developer workflow changes.

### Kernel contributions

This project uses the Meta kernel from `MetaCubeX/Clash.Meta` (`android-real` branch).

- General kernel changes should be proposed to the `Alpha` branch in the Meta kernel repository.
- Android-specific kernel patches should be proposed to the `android-open` branch in the Meta kernel repository.

### License

By contributing, you agree that your changes are provided under the project license ([GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)).
