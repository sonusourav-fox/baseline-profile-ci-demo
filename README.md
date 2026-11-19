# Baseline Profile CI Demo

Minimal Android app that generates **Baseline Profiles** (and a Startup Profile) on a
**self-hosted GitHub Actions runner**. Upload this repo to a personal GitHub account and
run the workflow after registering a runner.

## What you get

| Piece | Role |
|---|---|
| `:app` | Tiny Compose home feed (`com.example.baselineprofiledemo`) |
| `:baselineprofile` | Macrobenchmark generators (startup + scroll) |
| `.github/workflows/generate-baseline-profile.yml` | Manual workflow for self-hosted runners |

Generated files land in:

```
app/src/main/generated/baselineProfiles/baseline-prof.txt
app/src/main/generated/baselineProfiles/startup-prof.txt
```

## Runner modes

### 1) GMD emulator (default) — Linux self-hosted

Best for CI without a phone.

1. Register a **Linux** self-hosted runner with labels: `self-hosted`, `linux`, `android`.
2. Install JDK 17 + Android SDK (platform-tools, emulator, system images). Set `ANDROID_HOME`.
3. Ensure **KVM** works: `/dev/kvm` must exist (`kvm-ok` / nested virt on VMs).
4. In GitHub: **Actions → Generate Baseline Profile → Run workflow**
   - `mode`: `gmd`
   - `runner_labels_json`: `["self-hosted","linux","android"]`

### 2) Physical USB device — macOS (or Linux) self-hosted

1. Register a runner with labels: `self-hosted`, `macOS`, `android-physical` (or your own).
2. Plug in **one** unlocked Android **13+** phone with USB debugging authorized.
3. Run workflow with:
   - `mode`: `connected`
   - `runner_labels_json`: `["self-hosted","macOS","android-physical"]`

## Local commands

```bash
# Emulator / GMD
./gradlew :app:generateBaselineProfile \
  -Pbaselineprofile.useConnectedDevices=false \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect

# Attached phone (API 33+)
./gradlew :app:generateBaselineProfile \
  -Pbaselineprofile.useConnectedDevices=true \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
```

## Upload to your personal GitHub account

```bash
cd /path/to/baseline-profile-ci-demo
git init -b main
git add .
git commit -m "Initial Baseline Profile CI demo"
gh repo create baseline-profile-ci-demo --private --source=. --remote=origin --push
```

Then: **Settings → Actions → Runners → New self-hosted runner**, follow the download /
configure steps, and add the labels used in `runner_labels_json`.

Also enable: **Settings → Actions → General → Allow GitHub Actions to create and approve
pull requests** if you want the workflow to open a PR with regenerated profiles.

## Security notes

- Self-hosted runners execute whatever the workflow checks out. Keep the repo private
  and do **not** run untrusted fork PRs on the runner.
- Prefer a dedicated test phone / VM. Do not use a device with personal accounts/data.

## Verified locally

`./gradlew :app:generateBaselineProfile` completed successfully on a Gradle Managed
Device (`pixel6Api34`). Sample outputs are already in
`app/src/main/generated/baselineProfiles/` so you can see the expected shape before CI.

## Stack

- AGP `8.9.2` · Kotlin `2.1.10` · Gradle `8.11.1`
- Baseline Profile plugin / Macrobenchmark `1.5.0`
- ProfileInstaller `1.4.1`
- GMD: `pixel6Api34` (`aosp`)
