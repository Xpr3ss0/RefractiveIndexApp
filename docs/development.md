# Development guide

## Prerequisites

- Android Studio with a current Android SDK installation
- A compatible JDK provided by Android Studio or configured for Gradle
- Git with submodule support

Clone the repository together with its plotting-library submodule:

```bash
git clone --recurse-submodules git@github.com:Xpr3ss0/RefractiveIndexApp.git
cd RefractiveIndexApp
```

For an existing checkout, initialise the submodule with:

```bash
git submodule update --init --recursive
```

## Build and test

Open the project in Android Studio, or build from the command line:

```bash
./gradlew :app:assembleDebug
./gradlew test
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Use Android Studio's device manager or `adb` to install it on an emulator or connected device.

## Project structure

- `app/` contains the Android application, Compose UI, data access, and physics calculations.
- `scientificPlot/` is a Git submodule containing the reusable Compose scientific plotting library.
- `docs/` contains contributor and release documentation.

## Data source

The app obtains catalogue and material YAML files from the [refractiveindex.info database](https://github.com/polyanskiy/refractiveindex.info-database). See [DATA_SOURCES.md](../DATA_SOURCES.md) for attribution and provenance.

## Contributing

Develop changes on the `dev` branch and keep `main` suitable for releases. Run the relevant Gradle tests before opening a pull request. If a change alters `scientificPlot`, commit and push its submodule changes first, then commit the updated submodule reference in this repository.

For creating a signed public APK, follow the [release guide](releasing.md).
