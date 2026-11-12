# Publishing JIIT Timer on Google Play

This guide covers the steps to publish this app on Google Play. Requirements change; the official links at the end are the source of truth.

Current app state:

| Item | Value |
|---|---|
| Application ID | `com.profesorfalken.jiittimer` |
| Version | `2.1.0` (`versionCode` 3) |
| Target SDK | 36 |
| Min SDK | 24 |
| Format | Android App Bundle (`.aab`) |
| Ads / in-app purchases | None |
| Network / accounts / analytics | None |

## 1. Before you start

You need:

- A Google account and a Google Play Console developer account. Registration has a one-time US$25 fee and requires identity verification.
- A personal or organization account. Personal accounts created after November 13, 2023 must run a closed test with at least **12 testers opted in continuously for 14 days** before applying for production access.
- A privacy policy URL. Google requires one even when the app collects no data.
- An upload keystore. Keep it outside the repository and back it up.
- Store graphics: a 512×512 app icon, a 1024×500 feature graphic and at least two phone screenshots.

The current `targetSdk` is 36. From August 31, 2026, new apps and updates must target Android 16 (API 36) or higher, so this project already meets that requirement.

## 2. Create the upload keystore

Create this once and keep it somewhere safe. Losing it can make future updates impossible unless you use Play App Signing and request an upload key reset.

```bash
keytool -genkeypair \
  -v \
  -keystore upload-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Copy `keystore.properties.example` to `keystore.properties` and fill it in:

```properties
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=your-store-password
keyAlias=upload
keyPassword=your-key-password
```

`keystore.properties`, `*.jks` and `*.keystore` are ignored by Git. Do not commit them.

The Gradle build uses this file automatically when it exists:

```bash
./gradlew bundleRelease
```

Output:

```text
app/build/outputs/bundle/release/app-release.aab
```

The AAB is signed with the upload key. On your first upload, enroll in **Play App Signing**. Google keeps the app signing key; your upload key is only used to authenticate uploads. This is the recommended setup.

## 3. Create the app in Play Console

1. Open [Play Console](https://play.google.com/console) and choose **Create app**.
2. Enter the app name: `JIIT Timer`.
3. Choose the default language, app type (App), and free/paid status. A free app cannot be changed to paid later.
4. Accept the developer program policies and US export declarations.
5. Set the package name to `com.profesorfalken.jiittimer` when uploading. The application ID cannot be changed after the first release.
6. Complete the **Dashboard** setup tasks.

## 4. Complete App content

In **Policy and programs > App content**, complete every section:

### Privacy policy

Host the page in `docs/privacy-policy.html` and enter its public URL. With GitHub Pages enabled from the `docs/` folder of the `master` branch, the URL will be:

```text
https://profesorfalken.github.io/JIITTimer/privacy-policy.html
```

Update the contact details in that page if needed.

### App access

Select **All functionality is available without special access**. The app has no login, invite or restricted content.

### Ads

Select **No, my app does not contain ads**.

### Content rating

Complete the questionnaire. This is a fitness timer with no user-generated content, violence, gambling, sexual content, drugs, or external links. The result should be suitable for a general audience. Do not claim medical or health-treatment functionality.

### Target audience

Choose the appropriate age group. For a general fitness timer, 13+ is a reasonable starting point. Avoid selecting children unless the app is specifically designed for them, because additional Families policies apply.

### Data safety

For this app:

- **Does your app collect or share any required user data types?** No.
- **Is all user data encrypted in transit?** Not applicable; the app has no network access.
- **Do you provide a way for users to request data deletion?** Not applicable; no data is collected.
- The app stores programs and preferences locally on the device. Android may include app data in the user's own device backup if the user has system backup enabled.

Review the answers carefully. Google reviews the declaration against the app behavior.

### Health apps

This is a workout timer, not a medical device and not a health-data app. Do not declare access to health data or medical functionality unless that changes in a future version.

### Foreground service declaration

The app uses a foreground service of type `specialUse` to keep the interval timer running while the screen is off or the app is in the background. In the Play Console foreground service declaration, provide:

- **Foreground service type:** `specialUse`
- **Use case:** workout/interval timer started explicitly by the user
- **Description:** "JIIT Timer runs a user-started HIIT workout timer. The foreground service keeps the interval countdown and audio/vibration cues working when the app is in the background or the screen is off. It shows a persistent notification with Pause, Resume, Skip and Stop actions."
- **Video:** upload an unlisted YouTube video showing: opening the app, starting a workout, pressing Home or turning the screen off, and controlling the timer from the notification. The video must demonstrate the foreground service feature in use.

The manifest declares:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<service
    android:name=".service.WorkoutTimerService"
    android:foregroundServiceType="specialUse" />
```

## 5. Store listing

Use the assets in the [`store/`](../store/) folder:

- App name: `JIIT Timer`
- Short description and full description: [listing-en-US.md](../store/listing-en-US.md)
- App icon: `store/icon-512.png`
- Feature graphic: `store/feature-graphic-1024x500.png`
- Phone screenshots: `store/screenshots/`
- Category: **Health & Fitness**
- Contact email: your developer email
- Privacy policy: the public URL

Store listing limits:

- App name: 30 characters.
- Short description: 80 characters.
- Full description: 4,000 characters.
- App icon: 512×512 PNG, up to 1 MB.
- Feature graphic: 1024×500 PNG or JPEG.
- Phone screenshots: 2–8 images, 1080–7680 px, showing the app UI without device frames.

## 6. Testing tracks

1. **Internal testing** (recommended first): create a release, upload the AAB, add your own Google account and a few testers, and install through the opt-in link.
2. **Closed testing**: required for new personal accounts. Add at least 12 testers, make sure they remain opted in for 14 consecutive days, collect feedback and fix issues.
3. **Apply for production access** from the closed testing track. You will answer questions about your testers, feature coverage, feedback and production readiness.
4. **Production**: complete the store listing, countries, pricing and all App content declarations, then create the production release.

The first review can take from a few hours to several days. Subsequent updates are usually faster.

## 7. Publish the production release

1. Go to **Production > Create new release**.
2. Upload `app/build/outputs/bundle/release/app-release.aab`.
3. Add release notes.
4. Check the warning-free release summary.
5. Choose countries and rollout percentage.
6. Start the rollout.

Keep the same upload key for every update. Increase `versionCode` and `versionName` in `app/build.gradle.kts` before each upload. `versionCode` must always increase.

## 8. Common rejection reasons

- Missing or mismatched privacy policy.
- Incomplete Data safety form.
- Missing foreground service declaration or demonstration video.
- Target API level below the current requirement.
- Requesting permissions that are not needed.
- Claiming medical or health-treatment benefits that the app does not provide.
- Using screenshots that do not show the actual app.
- Uploading an APK instead of an AAB for a new app.

## Official links

- [Target API level requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
- [App testing requirements for new personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Play Console developer account setup](https://support.google.com/googleplay/android-developer/answer/6112435)
- [Create and set up an app](https://support.google.com/googleplay/android-developer/answer/9859152)
- [Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/9859455)
- [Data safety section](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Foreground service and full-screen intent requirements](https://support.google.com/googleplay/android-developer/answer/13392821)
- [Store listing and graphic assets](https://support.google.com/googleplay/android-developer/answer/9866151)
