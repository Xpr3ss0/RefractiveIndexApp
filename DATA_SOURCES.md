# Data sources

## refractiveindex.info database

Material catalogue and material data are sourced from the [refractiveindex.info database](https://github.com/polyanskiy/refractiveindex.info-database), maintained by M. N. Polyanskiy. The database is dedicated to the public domain under the [CC0 1.0 Universal Public Domain Dedication](https://creativecommons.org/publicdomain/zero/1.0/).

The app is an independent interface and is not affiliated with or endorsed by refractiveindex.info or M. N. Polyanskiy.

### Citation

Please cite the database when using data obtained through the app:

> Polyanskiy, M. N. Refractiveindex.info database of optical constants. *Scientific Data* **11**, 94 (2024). https://doi.org/10.1038/s41597-023-02898-2

### Bundled catalogue snapshot

`app/src/main/assets/catalog-nk.yml` is the curated catalogue snapshot bundled with this app and retains its upstream CC0 header.

| Upstream commit | Retrieved | SHA-256 |
| --- | --- | --- |
| `c5c2f188e848453def5970e347399d653df2ffc2` | 2026-09-05 | `d4df827ad10482a563794486d6bbd5057adca79df226e1aa4e729ebf221d6b0f` |

Future catalogue updates must record the upstream Git commit, retrieval date, and SHA-256 hash in this document. The app can also download and persist catalogues for user-selected immutable commits. Runtime material records are fetched from the active snapshot's matching upstream revision; their per-record references remain visible in the application.
