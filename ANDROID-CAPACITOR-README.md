# Midad AI — Android / Capacitor setup

This project has been prepared for Capacitor with:
- `appId`: `com.midaad.app`
- `appName`: `midaad-ai`
- WebView server URL: `https://midaad-ai.vercel.app/`
- `webDir`: `public`

## Complete Android generation and APK build

Run these commands from the project root on a machine with internet access, Node.js, Android Studio, and an installed Android SDK:

```bash
npm install --no-audit --no-fund --legacy-peer-deps
npx cap add android
npx cap sync
```

In `android/app/src/main/AndroidManifest.xml`, ensure the manifest includes:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

And that the `<application>` element includes:

```xml
android:usesCleartextTraffic="true"
```

Build a debug APK from the project root:

```bash
cd android
./gradlew assembleDebug
```

On Windows use:

```bat
cd android
gradlew.bat assembleDebug
```

The APK is produced at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Important

This environment did not have the Android SDK/Gradle installed, and npm package installation could not complete before the network timeout. Therefore the Android native platform and APK have **not** been generated in this archive. Run the commands above to generate them. The existing Next.js `output: 'standalone'` setting is retained; it is not `output: 'export'` and does not force static export.
