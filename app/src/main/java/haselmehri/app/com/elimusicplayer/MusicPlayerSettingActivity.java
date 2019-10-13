package haselmehri.app.com.elimusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import haselmehri.app.com.elimusicplayer.SharedPreferencesHelper.MusicPlayerSharedPrefManager;
import haselmehri.app.com.elimusicplayer.model.MusicPlayerSetting;

public class MusicPlayerSettingActivity extends AppCompatActivity {

    private static final String TAG = "MusicPlayerSettingActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player_setting);

        setupToolbar();
        final MusicPlayerSharedPrefManager musicPlayerSharedPrefManager = new MusicPlayerSharedPrefManager(MusicPlayerSettingActivity.this);
        final SwitchCompat showAudioVisualizer = findViewById(R.id.switch_show_audio_visualizer);
        final RadioButton lineRadio = findViewById(R.id.line_radio);
        final RadioButton barRadio = findViewById(R.id.bar_radio);
        final RadioButton circleRadio = findViewById(R.id.circle_radio);
        final RadioButton circleBarRadio = findViewById(R.id.circle_bar_radio);
        final RadioButton lineBarRadio = findViewById(R.id.line_bar_radio);

        MusicPlayerSetting currentMusicPlayerSetting = musicPlayerSharedPrefManager.getSetting();
        if (currentMusicPlayerSetting != null) {
            showAudioVisualizer.setChecked(currentMusicPlayerSetting.isShowAudioVisualizer());
            barRadio.setChecked(currentMusicPlayerSetting.getAudioVisualizerType() == MusicPlayerSetting.AudioVisualizerTypes.BarVisualizer);
            lineRadio.setChecked(currentMusicPlayerSetting.getAudioVisualizerType() == MusicPlayerSetting.AudioVisualizerTypes.LineVisualizer);
            circleRadio.setChecked(currentMusicPlayerSetting.getAudioVisualizerType() == MusicPlayerSetting.AudioVisualizerTypes.CircleVisualizer);
            circleBarRadio.setChecked(currentMusicPlayerSetting.getAudioVisualizerType() == MusicPlayerSetting.AudioVisualizerTypes.CircleBarVisualizer);
            lineBarRadio.setChecked(currentMusicPlayerSetting.getAudioVisualizerType() == MusicPlayerSetting.AudioVisualizerTypes.LineBarVisualizer);
        }

        Button btnSaveSetting = findViewById(R.id.button_save_setting);
        btnSaveSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerSetting musicPlayerSetting = new MusicPlayerSetting();
                musicPlayerSetting.setShowAudioVisualizer(showAudioVisualizer.isChecked());
                if (barRadio.isChecked())
                    musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.BarVisualizer);
                else if (circleRadio.isChecked())
                    musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.CircleVisualizer);
                else if (lineRadio.isChecked())
                    musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.LineVisualizer);
                else if (circleBarRadio.isChecked())
                    musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.CircleBarVisualizer);
                else if (lineBarRadio.isChecked())
                    musicPlayerSetting.setAudioVisualizerType(MusicPlayerSetting.AudioVisualizerTypes.LineBarVisualizer);

                Boolean result = musicPlayerSharedPrefManager.saveSetting(musicPlayerSetting);
                if (result) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Setting_Save_Ok", true);
                    setResult(MusicPlayerActivity.REQUSET_CODE_SETTING, resultIntent);
                    finish();
                }
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_music_player_setting);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Utilities.setTypefaceToolbar(toolbar);
    }
}
