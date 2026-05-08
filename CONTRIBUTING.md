## Contributing to Tabby

Thank you for contributing to this project.

### Before you start

- Search existing issues and pull requests before opening a new one.
- For bug reports, include steps to reproduce, expected behavior, and logs/screenshots when possible.
- For feature requests, explain the use case and scope clearly.

### Development setup

1. Fork this repository and create a branch from `trunk`.
2. Initialize submodules:

   ```bash
   git submodule update --init --recursive
   ```

3. Install required tools:
   - JDK 21
   - Android SDK
   - CMake
   - Go 1.26 or above

4. Create `local.properties` in the project root:

   ```properties
   sdk.dir=/path/to/android-sdk
   ```

### Validations

Run checks before submitting a pull request:

```bash
./gradlew spotlessCheck # for checking code style.
./gradlew app:assembleRelease # for checking build.
```

### Pull request guidelines

- Keep changes focused and small.
- Include a clear description of what changed and why.
- Link related issues when applicable.
- Update docs when behavior or developer workflow changes.
