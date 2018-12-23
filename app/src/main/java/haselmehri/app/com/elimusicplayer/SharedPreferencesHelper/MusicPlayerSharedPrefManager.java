package haselmehri.app.com.elimusicplayer.SharedPreferencesHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import haselmehri.app.com.elimusicplayer.model.MusicPlayerSetting;

public class MusicPlayerSharedPrefManager {
    private static final String MUSIC_PLAYER_SHARED_PREF_NAME = "music_player_shared_pref";

    private final static String KEY_SHOW_AUDIO_VISUALIZER = "show_audio_visualizer";
    private final static String KEY_AUDIO_VISUALIZER_TYPE = "audio_visualizer_type";

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

    public MusicPlayerSetting getSetting() {
        MusicPlayerSetting musicPlayerSetting = new MusicPlayerSetting();

        musicPlayerSetting.setShowAudioVisualizer(sharedPreferences.getBoolean(KEY_SHOW_AUDIO_VISUALIZER, true));
        String audioVisualizerType = sharedPreferences.getString(KEY_AUDIO_VISUALIZER_TYPE, "LineBarVisualizer");
        if (!TextUtils.isEmpty(audioVisualizerType))
            musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.valueOf(audioVisualizerType));

        return musicPlayerSetting;
    }
}
