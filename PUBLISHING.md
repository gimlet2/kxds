# Publishing Guide for KXDS

This document provides instructions for publishing the KXDS plugin to Maven Central and GitHub Packages.

## Prerequisites

### For GitHub Packages
No additional setup required - GitHub Actions will use the built-in `GITHUB_TOKEN`.

### For Maven Central

1. **Create a Sonatype OSSRH account**
   - Sign up at https://issues.sonatype.org/
   - Create a ticket to claim the `org.restmonkeys` group ID
   - Follow the instructions at https://central.sonatype.org/publish/publish-guide/

2. **Generate a GPG key for signing**
   ```bash
   gpg --gen-key
   # Follow the prompts to create a key
   
   # List your keys to find the key ID
   gpg --list-secret-keys --keyid-format=long
   
   # Export the private key (replace KEY_ID with your key ID)
   gpg --armor --export-secret-keys KEY_ID
   ```

3. **Add secrets to GitHub repository**
   Go to Settings → Secrets and variables → Actions and add:
   - `OSSRH_USERNAME`: Your Sonatype OSSRH username
   - `OSSRH_PASSWORD`: Your Sonatype OSSRH password
   - `SIGNING_KEY`: The exported GPG private key (entire output from the export command)
   - `SIGNING_PASSWORD`: The passphrase for your GPG key

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

The GitHub Actions workflow will automatically:
- Build the project
- Publish to GitHub Packages
- Publish to Maven Central (if credentials are configured)

### Option 2: Manual Publishing via GitHub Actions

1. Go to Actions → Publish to Maven Central and GitHub Packages
2. Click "Run workflow"
3. Select where to publish:
   - `github`: Only to GitHub Packages
   - `maven-central`: Only to Maven Central
   - `both`: To both repositories

### Option 3: Local Publishing

#### Publish to GitHub Packages locally
```bash
export GITHUB_TOKEN=your_github_token
export GITHUB_ACTOR=your_github_username
./gradlew publishMavenPublicationToGitHubPackagesRepository
```

#### Publish to Maven Central locally
```bash
export OSSRH_USERNAME=your_ossrh_username
export OSSRH_PASSWORD=your_ossrh_password
export SIGNING_KEY="$(cat your_private_key.asc)"
export SIGNING_PASSWORD=your_key_passphrase
./gradlew publishMavenPublicationToOSSRHRepository
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

### From Maven Central

Once published to Maven Central, users can simply add:

```kotlin
dependencies {
    implementation("org.restmonkeys:kxds-hosting:0.1.0")
    ksp("org.restmonkeys:kxds-hosting:0.1.0")
}
```

No additional repository configuration needed!

## Troubleshooting

### GPG Signing Issues
- Ensure the GPG key is properly formatted (including `-----BEGIN PGP PRIVATE KEY BLOCK-----` headers)
- Check that the key hasn't expired
- Verify the passphrase is correct

### Maven Central Upload Issues
- Ensure your Sonatype OSSRH account is approved
- Verify the group ID `org.restmonkeys` is claimed in your Sonatype account
- Check that all required POM elements are present

### GitHub Packages Authentication
- Verify the `GITHUB_TOKEN` has `write:packages` permission
- For personal access tokens, ensure the token has appropriate scopes

## Post-Release Checklist

After publishing a release:

1. ✅ Verify the artifact appears in GitHub Packages
2. ✅ If published to Maven Central, verify it appears on https://central.sonatype.com/
3. ✅ Update the README with the new version number
4. ✅ Create release notes documenting changes
5. ✅ Update version to next SNAPSHOT version for development
