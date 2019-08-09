
  # EliMusicPlayer
a simple Music Player for Android version 16(Jelly Bean) and above.

i used the bind service and unbind(background) service in the EliMusicPlayer and I've also used Material Design(AppbarLayout,NavigationView...)


<img src="https://github.com/haselmehri/EliMusicPlayer/blob/master/ReadmeFiles/AppImage2.jpeg" width="270" style='margin-right:5px'> <img src="https://github.com/haselmehri/EliMusicPlayer/blob/master/ReadmeFiles/AppImage1.jpeg" width="270"  style='margin-right:5px'>
<img src="https://github.com/haselmehri/EliMusicPlayer/blob/master/ReadmeFiles/AppImage4.jpeg" width="270">

## Other facilities

  1. Ability to add music to favorites
  2. Show equalizer while playing music
  3. Play music in the background and display media controls in Notification and Lock Screen
  3. Select music from Gallery and Storage
  
  
<img src="https://github.com/haselmehri/EliMusicPlayer/blob/master/ReadmeFiles/AppImage3.jpeg" width="270" style='margin-right:5px'>      <img src="https://github.com/haselmehri/EliMusicPlayer/blob/master/ReadmeFiles/AppImage5.jpeg" width="270"  style='margin-right:5px'>


## Register Service

To use the services on Android you need to register the service on AndroidManifest.xml.

```android
<service android:name=".MusicPlayerService" />
```

## Register App to Using Default App for Play Music

  to do this register following intent in AndroidManifest.xml
  
  ```android
              <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />

                <action android:name="android.intent.action.MUSIC_PLAYER" />

                <category android:name="android.intent.category.CATEGORY_APP_MUSIC" />
                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data android:scheme="content" />
                <data android:scheme="file" />
                <data android:mimeType="audio/*" />
                <data android:mimeType="audio/mpeg" />
                <data android:mimeType="application/ogg" />
                <data android:mimeType="application/x-ogg" />
                <data android:mimeType="application/itunes" />
            </intent-filter>
  ```

## License
[MIT](https://choosealicense.com/licenses/mit/)
