# Index Info

A mobile-first Android interface for refractiveindex.info data. It provides material selection, dispersion and extinction-coefficient plots, derived optical constants, and a Fresnel calculator.

## Screenshots

The app is designed around a compact material-selection flow and touch-friendly scientific plots.

<table>
  <tr>
    <td width="50%" align="center"><strong>Material selection</strong><br><br><img src="docs/images/material-selection.jpg" alt="Three-step material selection for BaB2O4" width="240"></td>
    <td width="50%" align="center"><strong>Dispersion and extinction</strong><br><br><img src="docs/images/dispersion-and-extinction-plots.jpg" alt="Dispersion and extinction coefficient plots" width="240"></td>
  </tr>
  <tr>
    <td width="50%" align="center"><strong>Fresnel reflectance</strong><br><br><img src="docs/images/fresnel-reflectance.jpg" alt="Rp and Rs Fresnel reflectance plot" width="300"></td>
    <td width="50%" align="center"><strong>Derived optical constants</strong><br><br><img src="docs/images/derived-optical-constants.jpg" alt="Derived optical constants at a selected wavelength" width="240"></td>
  </tr>
</table>

## Install

Download the signed APK from the [latest GitHub release](https://github.com/Xpr3ss0/RefractiveIndexApp/releases/latest). Android may ask you to allow the browser or file manager to install unknown apps. Updates installed from later releases retain app data when they are signed with the same release key.

## Feedback and support

Found a problem or have an idea for Index Info? Please [open an issue](https://github.com/Xpr3ss0/RefractiveIndexApp/issues/new/choose). Bug reports and feature requests are both welcome.

## Upstream Database Changes

If changes are introduced in the upstream database, data retrieval and parsing might be affected. I try to stay up to date with the latest changes on the database's main branch. However, there is also an option in the app's settings that determines the commit version of the database. Setting this to the current commit while the app is working freezes this functional state.

## Data and attribution

Material data is sourced from the [refractiveindex.info database](https://github.com/polyanskiy/refractiveindex.info-database), which is dedicated to the public domain under [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/). This is an independent application and is not affiliated with or endorsed by refractiveindex.info or M. N. Polyanskiy.

When citing this app or the included data, please cite:

> M. N. Polyanskiy, *Refractiveindex.info database of optical constants*, Scientific Data 11, 94 (2024). https://doi.org/10.1038/s41597-023-02898-2

See [DATA_SOURCES.md](DATA_SOURCES.md) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for provenance and dependency notices.

## Development

For local setup, testing, and project structure, see the [development guide](docs/development.md). Maintainers can use the [release guide](docs/releasing.md) to sign and publish tagged releases.

## License

This application is licensed under the [Apache License 2.0](LICENSE).
