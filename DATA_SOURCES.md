# Data sources

## refractiveindex.info database

Material catalogue and material data are sourced from the [refractiveindex.info database](https://github.com/polyanskiy/refractiveindex.info-database), maintained by M. N. Polyanskiy. The database is dedicated to the public domain under the [CC0 1.0 Universal Public Domain Dedication](https://creativecommons.org/publicdomain/zero/1.0/).

The app is an independent interface and is not affiliated with or endorsed by refractiveindex.info or M. N. Polyanskiy.

### Citation

Please cite the database when using data obtained through the app:

> Polyanskiy, M. N. Refractiveindex.info database of optical constants. *Scientific Data* **11**, 94 (2024). https://doi.org/10.1038/s41597-023-02898-2

### Bundled catalogue snapshot

`app/src/main/assets/catalog-nk.yml` is a copy of the upstream catalogue and retains its upstream CC0 header. It entered this repository on 2026-07-26. The original upstream revision was not recorded at that time.

Future catalogue updates must record the upstream Git commit or release tag, retrieval date, and SHA-256 hash in this document. Runtime material records are fetched from the upstream repository; their per-record references remain visible in the application.

The application defaults to the upstream `main` branch to retrieve the newest catalogue and material records. The data-acquisition API also accepts an immutable upstream Git commit SHA when a reproducible catalogue/data pair is required.
