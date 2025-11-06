# Publishing Guide for KXDS

This document provides instructions for publishing the KXDS plugin to GitHub Packages.

## Prerequisites

### For GitHub Packages
No additional setup required - GitHub Actions will use the built-in `GITHUB_TOKEN`.

## Publishing Methods

### Option 1: Automated Publishing via GitHub Release

1. Create a new release on GitHub:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

2. Go to GitHub → Releases → Draft a new release
3. Select the tag you just created
4. Fill in the release notes
5. Click "Publish release"

The GitHub Actions workflow will automatically build and publish to GitHub Packages.

### Option 2: Manual Publishing via GitHub Actions

1. Go to Actions → Publish to GitHub Packages
2. Click "Run workflow"
3. Click "Run workflow" button to start the publishing process

### Option 3: Local Publishing

#### Publish to GitHub Packages locally
```bash
export GITHUB_TOKEN=your_github_token
export GITHUB_ACTOR=your_github_username
./gradlew publishMavenPublicationToGitHubPackagesRepository
```

#### Publish to local Maven repository (for testing)
```bash
./gradlew publishToMavenLocal
```

## Version Management

Update the version in `kxds-hosting/build.gradle.kts`:

```kotlin
version = "0.1.0"  // Release version
version = "0.2.0-SNAPSHOT"  // Development version
```

## Using the Published Plugin

### From GitHub Packages

Users need to authenticate with GitHub Packages. Add to their `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/gimlet2/kxds")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Then in their `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.restmonkeys:kxds-hosting:0.1.0")
    ksp("org.restmonkeys:kxds-hosting:0.1.0")
}
```

## Troubleshooting

### GitHub Packages Authentication
- Verify the `GITHUB_TOKEN` has `write:packages` permission
- For personal access tokens, ensure the token has appropriate scopes

## Post-Release Checklist

After publishing a release:

1. ✅ Verify the artifact appears in GitHub Packages
2. ✅ Update the README with the new version number
3. ✅ Create release notes documenting changes
4. ✅ Update version to next SNAPSHOT version for development
