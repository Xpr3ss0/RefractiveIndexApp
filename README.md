# Refractive Index

A mobile-first Android interface for refractiveindex.info data. It provides material selection, dispersion and extinction-coefficient plots, derived optical constants, and a Fresnel calculator.

## Install

Download the signed APK from the [latest GitHub release](https://github.com/Xpr3ss0/RefractiveIndexApp/releases/latest). Android may ask you to allow the browser or file manager to install unknown apps. Updates installed from later releases retain app data when they are signed with the same release key.

## Data

Material data is sourced from the [refractiveindex.info database](https://refractiveindex.info/). Please consult that project for its data licensing, citations, and source references.

## Development

Open the project in Android Studio or build a debug APK with:

```bash
./gradlew :app:assembleDebug
```

See [the release guide](docs/releasing.md) for signing and publishing a tagged release.

## License

This application is licensed under the [Apache License 2.0](LICENSE).
