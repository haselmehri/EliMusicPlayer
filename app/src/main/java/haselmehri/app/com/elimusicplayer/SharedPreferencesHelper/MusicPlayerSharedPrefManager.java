package haselmehri.app.com.elimusicplayer.SharedPreferencesHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import haselmehri.app.com.elimusicplayer.model.MusicPlayerSetting;

public class MusicPlayerSharedPrefManager {
    private static final String MUSIC_PLAYER_SHARED_PREF_NAME = "music_player_shared_pref";

    private final static String KEY_SHOW_AUDIO_VISUALIZER = "show_audio_visualizer";
    private final static String KEY_AUDIO_VISUALIZER_TYPE = "audio_visualizer_type";
    private final static String KRY_LAST_SHOW_NEW_VERSION_NOTIFICATION = "last_show_new_version_notification";

    private SharedPreferences sharedPreferences;

    public MusicPlayerSharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MUSIC_PLAYER_SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean saveSetting(MusicPlayerSetting musicPlayerSetting) {
        if (musicPlayerSetting != null) {
            try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_SHOW_AUDIO_VISUALIZER, musicPlayerSetting.isShowAudioVisualizer());
                editor.putString(KEY_AUDIO_VISUALIZER_TYPE, musicPlayerSetting.getAudioVisualizerType().toString());

                editor.apply();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public boolean updateLastShowNewVersionNotification() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();

            editor.putString(KRY_LAST_SHOW_NEW_VERSION_NOTIFICATION, formatter.format(date));

            editor.apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Date getLastShowNewVersionNotification() {
        String lastCheckDate = sharedPreferences.getString(KRY_LAST_SHOW_NEW_VERSION_NOTIFICATION, "");

        if (!TextUtils.isEmpty(lastCheckDate)) {
            try {
                return new SimpleDateFormat("yyyy/MM/dd").parse(lastCheckDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public MusicPlayerSetting getSetting() {
        MusicPlayerSetting musicPlayerSetting = new MusicPlayerSetting();

        musicPlayerSetting.setShowAudioVisualizer(sharedPreferences.getBoolean(KEY_SHOW_AUDIO_VISUALIZER, true));
        String audioVisualizerType = sharedPreferences.getString(KEY_AUDIO_VISUALIZER_TYPE, "LineBarVisualizer");
        if (!TextUtils.isEmpty(audioVisualizerType))
            musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.valueOf(audioVisualizerType));

        return musicPlayerSetting;
    }
}
