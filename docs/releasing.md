# Releasing a signed APK

Pushing an annotated tag named `vX.Y.Z` publishes a GitHub Release with a signed APK and SHA-256 checksum. The tag controls the Android version: `v1.2.3` becomes version name `1.2.3`; version code is `1_002_003`.

## One-time signing setup

Create a dedicated release key on a trusted machine. Choose strong passwords and retain an offline backup of both the keystore and its passwords; losing this key prevents seamless updates to published builds.

```bash
keytool -genkeypair -v \
  -keystore refractive-index-release.jks \
  -alias refractiveindex \
  -keyalg RSA -keysize 4096 -validity 10000
```

Do not commit the keystore. Store it in a password manager or encrypted offline backup. Then add these repository Actions secrets in GitHub under **Settings → Secrets and variables → Actions**:

| Secret | Value |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | `base64 -w 0 refractive-index-release.jks` |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | `refractiveindex` (or your chosen alias) |
| `RELEASE_KEY_PASSWORD` | Key password |

The `base64 -w 0` command is for GNU/Linux. On macOS, use `base64 -i refractive-index-release.jks | tr -d '\n'`.

## Verify locally

Set the four signing variables for the current shell, then build a release APK:

```bash
export RELEASE_STORE_FILE=/absolute/path/to/refractive-index-release.jks
export RELEASE_STORE_PASSWORD='…'
export RELEASE_KEY_ALIAS=refractiveindex
export RELEASE_KEY_PASSWORD='…'
./gradlew :app:assembleRelease \
  -PreleaseVersion=1.0.0 \
  -PreleaseVersionCode=1000000
```

The output is `app/build/outputs/apk/release/app-release.apk`. Install it on a test device before the first public release.

## Publish

After the workflow and secrets are on the default branch, create and push the tag:

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

The workflow checks out the public `scientificPlot` submodule, runs unit tests, signs the release APK, generates a checksum, and creates the GitHub Release. If the app was previously installed under the old `com.example.refractiveindexapp` application ID, uninstall that development build before installing the public release: Android treats the new ID as a separate app.
