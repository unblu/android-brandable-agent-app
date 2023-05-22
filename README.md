# Android Brandable Agent App

A basic application you can adapt to your organization's branding to create a fully functional agent application that works with the Unblu Collaboration Server.

## Rename the app
To rename an application, follow these steps:

### Step 1: Update the App Name

The name of your app as it appears on the device can be changed in the `strings.xml` file.

1. Open your project in Android Studio.
2. Go to `res > values > strings.xml`.
3. Change the `app_name` string to your new name.

```xml
<resources>
    <string name="app_name">Your New App Name</string>
</resources>
```

### Step 2: Update the Package Name

Click on the Android perspective in the Project pane in Android Studio (top left).
Make sure the Flattened option is unchecked under the dropdown menu in the Project pane.
Right-click on your package under java, select Refactor > Rename, and change the name.
Android Studio will highlight any errors due to this change, follow the recommended fixes.
Please note that if you've already published your app on the Google Play Store under a specific package name, changing it will make Google Play treat it as a completely new app.

### Step 3: Update Application ID
The Application ID uniquely identifies your app on the device and in the Google Play Store, so changing this is often required when renaming the app.

Go to Gradle Scripts > build.gradle (Module: app).
Find the defaultConfig section and change the applicationId to your new package name.

```groovy
    defaultConfig {
    applicationId "com.example.newpackagename"
    minSdkVersion 15
    targetSdkVersion 29
    versionCode 1
    versionName "1.0"
}
```

Sync the project by clicking Sync Now in the top right corner.

### Step 4: Refactor the code to match new package name (Optional)
If you have renamed your package, you should refactor all occurrences of the old package name in your codebase to match the new name.
Use the find and replace function (Ctrl + Shift + R on Windows/Linux or Cmd + Shift + R on MacOS) in Android Studio to make this easier.

After following these steps, your Android app should have the new name.


## Replace the app icon

### Step 1: Prepare your Icon

Before changing the app icon, make sure your new icon is prepared. Android requires that these icons be in PNG format, and recommends icon sizes of 48x48, 72x72, 96x96, 144x144 and 192x192 pixels to accommodate various device screen sizes and resolutions.

### Step 2: Add the Icon to the Project

1. Once your icons are ready, open your project in Android Studio.
2. Navigate to `res` directory in the project pane.
3. Under `res`, you'll find directories for different pixel densities like `mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi`.
4. Replace the `ic_launcher.png` files in all the `mipmap` folders with your new icon, keeping each icon's size appropriate for the density of the folder.

### Step 3: Update the Manifest

In most cases, Android Studio automatically updates the manifest file when you change the app icon. However, if this doesn't happen, you can do it manually.

1. Go to `AndroidManifest.xml`.
2. In the `<application>` tag, update the `android:icon` and `android:roundIcon` attributes to `@mipmap/ic_launcher`.

```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:label="@string/app_name">
```

## Changing the Splash Screen Logo

In the case of the splash screen, you need to replace the image in the `SplashScreen.kt` file. This file contains a composable function which defines the splash screen UI, including the logo.

### Step 1: Prepare your Logo

Just like with the app icon, ensure your new logo image is prepared. It should be a PNG file, and you'll need to size it appropriately for your splash screen.

### Step 2: Add the Logo to the Project

1. Once your logo is ready, open your project in Android Studio.
2. Navigate to the `res > drawable` directory in the project pane.
3. Add your new logo file into this `drawable` folder.

### Step 3: Update the SplashScreen.kt

1. Go to the `SplashScreen.kt` file in your project.
2. Look for the `Image` composable function in the file. It should be something like:

```kotlin
Image(
    painter = painterResource(id = R.drawable.logo),
    //use this color filter if you wish to change the color
    colorFilter = ColorFilter.tint(Color.White),
    contentDescription = null,
    modifier = Modifier.size(128.dp)
)
```
3. Replace R.drawable.old_logo with R.drawable.new_logo in the painterResource function. Make sure new_logo is the name of your new logo file that you placed in the res > drawable directory. So, it should look like:
```kotlin
Image(
    painter = painterResource(id = R.drawable.new_logo),
    //use this color filter if you wish to change the color
    colorFilter = ColorFilter.tint(Color.White),
    contentDescription = null,
    modifier = Modifier.size(128.dp)
)
```

After following these steps, your Android app's splash screen should now display the new logo.


## Localizing the Android App

Here are the steps to localize your Android app:

### Step 1: Define your Strings

1. Open your project in Android Studio.
2. Navigate to `res > values > strings.xml`.
3. Make sure all the text content of your app is defined here. For example:

```xml
<resources>
    <string name="app_name">My App</string>
    <string name="hello">Hello</string>
</resources>
```

### Step 2: Create values directories for languages
For each language that you want to support, create a new values directory with a suffix indicating the language code. For instance, values-de for German, values-fr for French.

### Step 3: Add a strings.xml file for each language
In each new values directory, create a new strings.xml file. In these files, translate each string value into the appropriate language. For example, the strings.xml in values-de might look like this:
```xml
<resources>
    <string name="app_name">Mi Aplicación</string>
    <string name="hello">Hallo</string>
</resources>
```

### Step 4: Use string resources in the code
In your code, always reference these string resources instead of hardcoding text. For example, in a Kotlin file, you might get the string resource for "hello" like this:

```kotlin
    label = stringResource(R.string.login_username_label)
```

In an XML layout file, you would reference it like this:

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/login_username_label" />
```

## Authentication methods
To set the authentication method, adapt the properties of the `AppConfiguration.kt` object.
There are three ways to authenticate:

- Direct authentication.
Set the following properties:
	- `unbluServerUrl` - The HTTP address of your Unblu Collaboration Server
	- `entryPath` - The entry path
	- `authType = AuthenticationType.Direct`

- Authentication with a reverse proxy.
Set the following properties:
	- `unbluServerUrl` - The HTTP address of you proxy server
	- `entryPath` - The entry path
	- `authType = AuthenticationType.WebProxy`

- With an external identity provider and authorization header. 
Set the following properties:
    - `unbluServerUrl` - The HTTP address of you proxy server
    - `entryPath` - The entry path
    - `authType = AuthenticationType.OAuth`
    - `authProvider` - The information your identity provider requires.
    - `oAuthClientId` - The client ID is a unique identifier for a client application that is registered in an authorization server. For instance, your value could be `"3eb7f2d1-45c6-497b-8496-721b4563c0d0"`.
    - `oAuthRedirectUri` - The redirect URI is where the client gets sent after the authorization step. For instance, your value could be `"msauth://com.unblu.brandeableagentapp/q6RIFzsc0JFs79rGhixu0w9s9QF3"`.
    - `oAuthEndpoint` - This is the URL that the client needs to access in order to start the OAuth flow. For instance, your value could be `"https://login.microsoftonline.com/app-id/oauth2/v2.0/authorize"`.
    - `oAuthTokenEndpoint` - This is the URL that the client needs to access in order to exchange an authorization code for an access token. For instance, your value could be `"https://login.microsoftonline.com/app-id/oauth2/v2.0/token"`.

You should replace the sample values with the ones you obtained from your OAuth 2.0 server.

Refer to you identity provider's documentation for how to fill in these settings correctly.

Once you've done that, your application will only work with these domains.