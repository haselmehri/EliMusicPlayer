
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

```bash
<service android:name=".MusicPlayerService" />
```

## Usage

```python
import foobar

foobar.pluralize('word') # returns 'words'
foobar.pluralize('goose') # returns 'geese'
foobar.singularize('phenomena') # returns 'phenomenon'
```

## License
[MIT](https://choosealicense.com/licenses/mit/)
