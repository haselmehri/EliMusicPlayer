package haselmehri.app.com.elimusicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chibde.visualizer.BarVisualizer;
import com.chibde.visualizer.CircleBarVisualizer;
import com.chibde.visualizer.CircleVisualizer;
import com.chibde.visualizer.LineBarVisualizer;
import com.chibde.visualizer.LineVisualizer;
import com.crashlytics.android.Crashlytics;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import haselmehri.app.com.elimusicplayer.SQLiteHelper.MusicPlayerSQLiteHelper;
import haselmehri.app.com.elimusicplayer.SharedPreferencesHelper.MusicPlayerSharedPrefManager;
import haselmehri.app.com.elimusicplayer.model.Favorite;
import haselmehri.app.com.elimusicplayer.model.MusicPlayerSetting;

public class MusicPlayerActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "ServiceMusicPlayerActiv";
    private static final int REQUEST_CODE_PICK_MUSIC = 1001;
    private static final int REQUEST_CODE_PICK_FILE_FOLDER = 1002;
    public static final int REQUSET_CODE_SETTING = 10010;
    public static final String ACTION_MUSIC_PLAYER_UI_UPDATE = "com.example.haselmehri.firstapplication.MUSIC_PLAYER_UI_UPDATE";
    public static final String ACTION_MUSIC_PLAYER_CLOSE = "com.example.haselmehri.firstapplication.MUSIC_PLAYER_CLOSE";
    public static final String ACTION_PLAY_BUTTON_IMAGE_CHANGE = "com.example.haselmehri.firstapplication.ACTION_PLAY_BUTTON_IMAGE_CHANGE";
    public static final String ACTION_FAVORITE_IMAGE_CHANGE = "com.example.haselmehri.firstapplication.ACTION_FAVORITE_IMAGE_CHANGE";
    private boolean isVisibleVolumeSeekbar = false;
    private MediaPlayer mediaPlayer;
    private ImageView coverImage;
    private ImageView navigationHeaderImage;
    private TextView txtCurrentDuration;
    private TextView txtMusicDuration;
    private ImageView playButton;
    private SeekBar seekBar;
    private Timer timer;
    private TextView txtMusicInfo;
    private TextView txtSelectedCountMusic;
    private ImageView favoriteImage;
    private ImageView volumeSettingImage;
    private RelativeLayout contentRelativeLayout;
    private CoordinatorLayout musicPlayerCoordinator;
    private DrawerLayout drawerLayout;
    private static final int BAR_VISUALIZER_ID = 10101011;
    private static final int LINE_BAR_VISUALIZER_ID = 10101012;
    private static final int LINE_VISUALIZER_ID = 10101013;
    private static final int CIRCLE_VISUALIZER_ID = 10101014;
    private static final int CIRCLE_BAR_VISUALIZER_ID = 10101015;
    private boolean SetAudioVisualizerSession = false;
    private UpdateMusicPlayerUIListener updateMusicPlayerUIListener;
    private SettingsContentObserver settingsContentObserver;
    private MusicPlayerSQLiteHelper musicPlayerSQLiteHelper;
    private MusicPlayerService musicPlayerService;
    private FirebaseAnalytics mFirebaseAnalytics;

    private SeekBar volumeSeekbar = null;
    private AudioManager audioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eli_music);

        bindService(new Intent(this, MusicPlayerService.class), this, BIND_AUTO_CREATE);

        setupViews();
        setAnimationOnMusicInfo();
        setToolbar();
        setupNavigationView();
    }

    private void setupNavigationView() {
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);

        View headerLayout = navigationView.getHeaderView(0);
        navigationHeaderImage = headerLayout.findViewById(R.id.navigation_header_image);

        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    Utilities.applyFontToMenuItem(this, subMenuItem, BaseApplication.getIranianSansFont());
                }
            }

            //the method we have create in activity
            Utilities.applyFontToMenuItem(this, mi, BaseApplication.getIranianSansFont());
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu_select_musics:
                        selectMusicFile();
                        break;
                    case R.id.navigation_menu_favorite_list:
                        loadFavoriteMusics();
                        break;
                    case R.id.navigation_menu_select_from_storage:
                        boolean result = Utilities.checkPermission(MusicPlayerActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE,
                                Utilities.PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE,
                                getResources().getString(R.string.message_access_external_storage), "Access Dialog", "Yes", "No");
                        if (result)
                            selectFileOrFolder();
                        break;
                    case R.id.navigation_menu_setting:
                        startActivityForResult(new Intent(MusicPlayerActivity.this, MusicPlayerSettingActivity.class), REQUSET_CODE_SETTING);
                        break;
                    case R.id.navigation_menu_about:
                        AboutDialog aboutDialog = new AboutDialog(MusicPlayerActivity.this);
                        aboutDialog.setCancelable(false);
                        aboutDialog.show();
                        break;
                    case R.id.navigation_menu_exit:
                        finish();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

        });
    }

    private void setCurrentVisualizerVisibility(int visibility) {
        LineBarVisualizer lineBarVisulizer = contentRelativeLayout.findViewById(LINE_BAR_VISUALIZER_ID);
        if (lineBarVisulizer != null) {
            lineBarVisulizer.setVisibility(visibility);
        }

        LineVisualizer lineVisulizer = contentRelativeLayout.findViewById(LINE_VISUALIZER_ID);
        if (lineVisulizer != null) {
            lineVisulizer.setVisibility(visibility);
        }

        CircleVisualizer circleVisulizer = contentRelativeLayout.findViewById(CIRCLE_VISUALIZER_ID);
        if (circleVisulizer != null) {
            circleVisulizer.setVisibility(visibility);
        }

        CircleBarVisualizer circleBarVisulizer = contentRelativeLayout.findViewById(CIRCLE_BAR_VISUALIZER_ID);
        if (circleBarVisulizer != null) {
            circleBarVisulizer.setVisibility(visibility);
        }

        BarVisualizer barVisualizer = contentRelativeLayout.findViewById(BAR_VISUALIZER_ID);
        if (barVisualizer != null) {
            barVisualizer.setVisibility(visibility);
        }
    }

    private void disconnectCurrentVisualizer() {
        LineBarVisualizer lineBarVisulizer = contentRelativeLayout.findViewById(LINE_BAR_VISUALIZER_ID);
        if (lineBarVisulizer != null) {
            lineBarVisulizer.release();
            contentRelativeLayout.removeView(lineBarVisulizer);
        }

        LineVisualizer lineVisulizer = contentRelativeLayout.findViewById(LINE_VISUALIZER_ID);
        if (lineVisulizer != null) {
            lineVisulizer.release();
            contentRelativeLayout.removeView(lineVisulizer);
        }

        CircleVisualizer circleVisulizer = contentRelativeLayout.findViewById(CIRCLE_VISUALIZER_ID);
        if (circleVisulizer != null) {
            circleVisulizer.release();
            contentRelativeLayout.removeView(circleVisulizer);
        }

        CircleBarVisualizer circleBarVisulizer = contentRelativeLayout.findViewById(CIRCLE_BAR_VISUALIZER_ID);
        if (circleBarVisulizer != null) {
            circleBarVisulizer.release();
            contentRelativeLayout.removeView(circleBarVisulizer);
        }

        BarVisualizer barVisualizer = contentRelativeLayout.findViewById(BAR_VISUALIZER_ID);
        if (barVisualizer != null) {
            barVisualizer.release();
            contentRelativeLayout.removeView(barVisualizer);
        }
    }

    private void loadUserSetting() {
        try {
            MusicPlayerSharedPrefManager musicPlayerSharedPrefManager = new MusicPlayerSharedPrefManager(this);
            MusicPlayerSetting musicPlayerSetting = musicPlayerSharedPrefManager.getSetting();
            if (musicPlayerSetting.isShowAudioVisualizer()) {
                disconnectCurrentVisualizer();

                RelativeLayout.LayoutParams params;
                switch (musicPlayerSetting.getAudioVisualizerType()) {
                    case BarVisualizer:
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        params.addRule(RelativeLayout.BELOW, R.id.music_info_textview);
                        params.addRule(RelativeLayout.ABOVE, R.id.favorite_image);
                        BarVisualizer barVisualizer = new BarVisualizer(this);
                        barVisualizer.setId(BAR_VISUALIZER_ID);
                        barVisualizer.setLayoutParams(params);
                        barVisualizer.setDensity(70);
                        barVisualizer.setColor(ContextCompat.getColor(this, R.color.color_orange_dark));

                        barVisualizer.setPlayer(musicPlayerService.getMediaPlayer().getAudioSessionId());

                        contentRelativeLayout.addView(barVisualizer);

                        break;
                    case LineVisualizer:
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        params.addRule(RelativeLayout.BELOW, R.id.music_info_textview);
                        params.addRule(RelativeLayout.ABOVE, R.id.favorite_image);
                        LineVisualizer lineVisualizer = new LineVisualizer(this);
                        lineVisualizer.setId(LINE_VISUALIZER_ID);
                        lineVisualizer.setLayoutParams(params);
                        lineVisualizer.setStrokeWidth(1);
                        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.color_orange_dark));

                        lineVisualizer.setPlayer(musicPlayerService.getMediaPlayer().getAudioSessionId());

                        contentRelativeLayout.addView(lineVisualizer);

                        break;
                    case CircleBarVisualizer:
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        params.addRule(RelativeLayout.BELOW, R.id.music_info_textview);
                        params.addRule(RelativeLayout.ABOVE, R.id.favorite_image);
                        CircleBarVisualizer circleBarVisualizer = new CircleBarVisualizer(this);
                        circleBarVisualizer.setId(CIRCLE_BAR_VISUALIZER_ID);
                        circleBarVisualizer.setLayoutParams(params);
                        circleBarVisualizer.setColor(ContextCompat.getColor(this, R.color.color_orange_dark));

                        circleBarVisualizer.setPlayer(musicPlayerService.getMediaPlayer().getAudioSessionId());

                        contentRelativeLayout.addView(circleBarVisualizer);

                        break;
                    case CircleVisualizer:
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        params.addRule(RelativeLayout.BELOW, R.id.music_info_textview);
                        params.addRule(RelativeLayout.ABOVE, R.id.favorite_image);
                        CircleVisualizer circleVisualizer = new CircleVisualizer(this);
                        circleVisualizer.setId(CIRCLE_VISUALIZER_ID);

                        // Customize the size of the circle. by defalut multipliers is 1.
                        circleVisualizer.setRadiusMultiplier(2f);
                        // set the line with for the visualizer between 1-10 default 1.
                        circleVisualizer.setStrokeWidth(2);

                        circleVisualizer.setLayoutParams(params);
                        circleVisualizer.setColor(ContextCompat.getColor(this, R.color.color_orange_dark));

                        circleVisualizer.setPlayer(musicPlayerService.getMediaPlayer().getAudioSessionId());

                        contentRelativeLayout.addView(circleVisualizer);

                        break;
                    case LineBarVisualizer:
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        params.addRule(RelativeLayout.BELOW, R.id.music_info_textview);
                        params.addRule(RelativeLayout.ABOVE, R.id.favorite_image);
                        LineBarVisualizer lineBarVisualizer = new LineBarVisualizer(this);
                        lineBarVisualizer.setId(LINE_BAR_VISUALIZER_ID);

                        // define custom number of bars you want in the visualizer between (10 - 256).
                        lineBarVisualizer.setDensity(90f);

                        lineBarVisualizer.setLayoutParams(params);
                        lineBarVisualizer.setColor(ContextCompat.getColor(this, R.color.color_orange_dark));

                        lineBarVisualizer.setPlayer(musicPlayerService.getMediaPlayer().getAudioSessionId());

                        contentRelativeLayout.addView(lineBarVisualizer);

                        break;
                }


            } else {
                disconnectCurrentVisualizer();
            }
        } catch (Exception e) {
            Log.e(TAG, "loadUserSetting: " + e.toString());
        }
    }

    private void loadFavoriteMusics() {
        List<Favorite> favorites = musicPlayerSQLiteHelper.getFavorites();
        if (favorites != null && favorites.size() > 0) {
            ArrayList<MediaFile> tempMediaFiles = new ArrayList<>();
            MediaFile mediaFile;
            for (Favorite favorite : favorites) {
                mediaFile = new MediaFile();
                mediaFile.setPath(favorite.getFilePath());
                tempMediaFiles.add(mediaFile);
            }
            musicPlayerService.setMediaFiles(tempMediaFiles);

            musicPlayerService.setCurrentMusicIndex(0);
            Uri fileUri = Uri.fromFile(new File(musicPlayerService.getMediaFiles().get(0).getPath()));
            musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
        } else {
            Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_music_added_favorite), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
            snackbar.show();
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.music_player_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ActionBarDrawerToggle actionBarDrawerToggle = new
                ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void setAnimationOnMusicInfo() {
        txtMusicInfo = findViewById(R.id.music_info_textview);
        YoYo.with(Techniques.Shake).repeat(YoYo.INFINITE).interpolate(new BounceInterpolator()).duration(5000).playOn(txtMusicInfo);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void prepareViews() {
        MediaFile mediaFile = musicPlayerService.getMediaFiles().get(musicPlayerService.getCurrentMusicIndex());

        Bitmap coverSong = Utilities.getCoverPictureBySong(mediaFile.getPath());
        if (coverSong != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            coverSong.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            coverImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(this)
                    .load(coverSong)
                    .into(coverImage);

            navigationHeaderImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(this)
                    .load(coverSong)
                    .into(navigationHeaderImage);

        } else {
            //coverImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.music_player_background, null));
            Picasso.get().load(R.drawable.music_player_background).into(coverImage);
            coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Picasso.get().load(R.drawable.music_player_background).into(navigationHeaderImage);
            navigationHeaderImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        txtMusicDuration.setText(formatDuration(mediaPlayer.getDuration()));
        txtCurrentDuration.setText(formatDuration(0));
        seekBar.setMax(mediaPlayer.getDuration());

        txtSelectedCountMusic.setText(String.format("%s%s%s%s",
                getResources().getString(R.string.label_musics),
                (musicPlayerService.getCurrentMusicIndex() + 1),
                getResources().getString(R.string.label_music_of),
                musicPlayerService.getMediaFiles().size()));

        txtMusicInfo.setText(mediaFile.getPath().substring(mediaFile.getPath().lastIndexOf("/") + 1));

        if (mediaPlayer.isPlaying()) {
            playButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, null));
            setCurrentVisualizerVisibility(View.VISIBLE);
        } else {
            playButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, null));
            setCurrentVisualizerVisibility(View.INVISIBLE);
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        timer.schedule(new MusicPlayerActivity.MainTimer(), 0, 1000);

        setFavoriteImage();

        setupAudioVisualizer();
    }

    private void setFavoriteImage() {
        if (musicPlayerService.IsCurrentMusicInFavoriteList()) {
            favoriteImage.setImageResource(R.drawable.ic_favorite_heart_gold);
        } else {
            favoriteImage.setImageResource(R.drawable.ic_favorite_heart_gray);
        }
    }

    private void setupViews() {
        musicPlayerSQLiteHelper = new MusicPlayerSQLiteHelper(this);
        musicPlayerCoordinator = findViewById(R.id.music_player_coordinator);
        contentRelativeLayout = findViewById(R.id.content_relative_layout);

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicPlayerService.getMediaFiles() != null && musicPlayerService.getMediaFiles().size() > 0) {
                    if (!MusicPlayerService.isRunningService()) {
                        Intent intent = new Intent(MusicPlayerActivity.this, MusicPlayerService.class);
                        startService(intent);
                    }

                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, null));
                        setCurrentVisualizerVisibility(View.INVISIBLE);
                    } else {
                        mediaPlayer.start();
                        playButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, null));
                        setCurrentVisualizerVisibility(View.VISIBLE);
                    }
                    musicPlayerService.updatePlayButtonImage(true);
                } else {
                    Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_music_selected_to_play), Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                    snackbar.show();
                }
            }
        });

        coverImage = findViewById(R.id.cover_image);
        txtMusicDuration = findViewById(R.id.music_duration_text);
        txtMusicDuration.setText(formatDuration(0));

        txtCurrentDuration = findViewById(R.id.music_current_duration_text);
        txtCurrentDuration.setText(formatDuration(0));

        final ImageView forwardButton = findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 30000);
            }
        });
        ImageView rewindButton = findViewById(R.id.rewind_button);
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 30000);
            }
        });

        ImageView skipNextButton = findViewById(R.id.skip_next_button);
        skipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMusicPlay();
            }
        });

        ImageView skipPreviousButton = findViewById(R.id.skip_previous_button);
        skipPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMusicPlay();
            }
        });

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicPlayerService != null &&
                        musicPlayerService.getMediaFiles() != null && musicPlayerService.getMediaFiles().size() > 0) {
                    mediaPlayer.seekTo(progress);
                    //txtCurrentDuration.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtSelectedCountMusic = findViewById(R.id.selected_music_count_textview);
        favoriteImage = findViewById(R.id.favorite_image);
        favoriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayerService.setFavoriteStatus();
            }
        });

        volumeSettingImage = findViewById(R.id.volume_setting_image);
        volumeSettingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVisibleVolumeSeekbar) {
                    volumeSeekbar.setVisibility(View.VISIBLE);
                } else {
                    volumeSeekbar.setVisibility(View.INVISIBLE);
                }
                isVisibleVolumeSeekbar = !isVisibleVolumeSeekbar;
            }
        });

        try {
            volumeSeekbar = findViewById(R.id.volumeSeekbar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volumeSeekbar.setMax(maxVolume);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumeSeekbar.setProgress(currentVolume);

            if (currentVolume == 0)
                volumeSettingImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_off, null));
            else
                volumeSettingImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_up, null));

            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                    if (fromUser) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                        if (progress == 0)
                            volumeSettingImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_off, null));
                        else
                            volumeSettingImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_up, null));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.START_DATE, formatter.format(date));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utilities.PERMISSIONS_REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAudioVisualizer();
            } else {
                setCurrentVisualizerVisibility(View.INVISIBLE);

                Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_permission_voice_recorder), Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                snackbar.show();
            }
        } else if (requestCode == Utilities.PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFileOrFolder();
            } else {
                Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_permission_external_storage), Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                snackbar.show();
            }
        }
    }

    private void setupAudioVisualizer() {
        // Set your media player to the visualizer.
        boolean result = Utilities.checkPermission(MusicPlayerActivity.this, Manifest.permission.RECORD_AUDIO,
                Utilities.PERMISSIONS_REQUEST_CODE_RECORD_AUDIO, getResources().getString(R.string.message_access_voice_recorder),
                "Access Dialog", "Yes", "No");

        if (result && !SetAudioVisualizerSession) {
            loadUserSetting();
            SetAudioVisualizerSession = true;
            if (mediaPlayer.isPlaying())
                setCurrentVisualizerVisibility(View.VISIBLE);
            else
                setCurrentVisualizerVisibility(View.INVISIBLE);
        }
    }

    private void nextMusicPlay() {
        if (musicPlayerService.getMediaFiles() != null && musicPlayerService.getMediaFiles().size() > 0) {
            if (musicPlayerService.getCurrentMusicIndex() < musicPlayerService.getMediaFiles().size() - 1) {
                musicPlayerService.setCurrentMusicIndex(musicPlayerService.getCurrentMusicIndex() + 1);
                MediaFile mediaFile = musicPlayerService.getMediaFiles().get(musicPlayerService.getCurrentMusicIndex());
                Uri fileUri = Uri.fromFile(new File(mediaFile.getPath()));
                musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
            } else {
                musicPlayerService.setCurrentMusicIndex(0);
                MediaFile mediaFile = musicPlayerService.getMediaFiles().get(0);
                Uri fileUri = Uri.fromFile(new File(mediaFile.getPath()));
                musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
            }
        } else {
            Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_music_selected_to_play), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
            snackbar.show();
        }
    }

    private void previousMusicPlay() {
        if (musicPlayerService.getMediaFiles() != null && musicPlayerService.getMediaFiles().size() > 0) {
            if (musicPlayerService.getCurrentMusicIndex() > 0) {
                musicPlayerService.setCurrentMusicIndex(musicPlayerService.getCurrentMusicIndex() - 1);
                MediaFile mediaFile = musicPlayerService.getMediaFiles().get(musicPlayerService.getCurrentMusicIndex());
                Uri fileUri = Uri.fromFile(new File(mediaFile.getPath()));
                musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
            } else {
                musicPlayerService.setCurrentMusicIndex(musicPlayerService.getMediaFiles().size() - 1);
                MediaFile mediaFile = musicPlayerService.getMediaFiles().get(musicPlayerService.getCurrentMusicIndex());
                Uri fileUri = Uri.fromFile(new File(mediaFile.getPath()));
                musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
            }
        } else {
            Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_music_selected_to_play), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
            snackbar.show();
        }
    }

    private String formatDuration(long duration) {
        int seconds = (int) (duration / 1000);
        int minutes = seconds / 60;
        seconds %= 60;

        return String.format(Locale.ENGLISH, "%02d", minutes) + ":" + String.format(Locale.ENGLISH, "%02d", seconds);
    }

    private class MainTimer extends TimerTask {
        //بصورت پیش فرض در Therad اصلی اجرا نمیشود
        //پس از runOnUiThread استقاده میکنیم تا به Viewها دسترسی داشته باشیم
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        try {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            txtCurrentDuration.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                        } catch (Exception exp) {
                            Log.i("MainTimer Exception", "run: " + mediaPlayer.getCurrentPosition());
                        }
                    }
                }
            });
        }
    }

    private void selectMusicFile() {
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                .setCheckPermission(true)
                .setShowImages(false)
                .setShowVideos(false)
                .setShowAudios(true)
                .setMaxSelection(-1)
                .build());
        startActivityForResult(intent, REQUEST_CODE_PICK_MUSIC);
    }

    private void selectFileOrFolder() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("audio/*"); // Set MIME type as per requirement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        Intent intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE_FOLDER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_MUSIC) {
                try {
                    ArrayList<MediaFile> tempMediaFiles = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                    //Do something with files
                    if (tempMediaFiles.size() > 0) {
                        musicPlayerService.setMediaFiles(tempMediaFiles);

                        musicPlayerService.setCurrentMusicIndex(0);
                        MediaFile mediaFile = musicPlayerService.getMediaFiles().get(0);
                        Uri fileUri = Uri.fromFile(new File(mediaFile.getPath()));
                        musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());

                        //tempMediaFiles.clear();
                    } else {
                        Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_not_music_selected), Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                        snackbar.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar snackbar = Snackbar.make(musicPlayerCoordinator, getResources().getString(R.string.message_music_unknown_error), Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                    snackbar.show();
                }

            } else if (requestCode == REQUEST_CODE_PICK_FILE_FOLDER) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    File file = new File(Utilities.getUriRealPath(this, uri).replace("/mnt/media_rw", "/storage"));
                    if (file.exists()) {
                        ArrayList<MediaFile> tempMediaFiles = new ArrayList<>();

                        MediaFile mediaFile = new MediaFile();
                        mediaFile.setPath(file.getPath());
                        tempMediaFiles.add(mediaFile);
                        musicPlayerService.setMediaFiles(tempMediaFiles);

                        musicPlayerService.setCurrentMusicIndex(0);
                        Uri fileUri = Uri.fromFile(new File(musicPlayerService.getMediaFiles().get(0).getPath()));
                        musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && data != null && data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            ArrayList<MediaFile> tempMediaFiles = new ArrayList<>();
                            MediaFile mediaFile;
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                Uri uri = item.getUri();
                                String path = Utilities.getUriRealPath(this, uri).replace("/mnt/media_rw", "/storage");
                                if (new File(path).exists()) {
                                    mediaFile = new MediaFile();
                                    mediaFile.setPath(path);
                                    tempMediaFiles.add(mediaFile);
                                }
                            }
                            if (tempMediaFiles.size() > 0) {
                                musicPlayerService.setMediaFiles(tempMediaFiles);

                                musicPlayerService.setCurrentMusicIndex(0);
                                Uri fileUri = Uri.fromFile(new File(musicPlayerService.getMediaFiles().get(0).getPath()));
                                musicPlayerService.prepareMediaPlayer(fileUri, mediaPlayer.isPlaying());
                            }
                        }
                    }
                }
            }
        } else if (requestCode == REQUSET_CODE_SETTING) {
            if (data != null && data.getBooleanExtra("Setting_Save_Ok", false)) {
                SetAudioVisualizerSession = false;
                setupAudioVisualizer();
            }
        }
    }

    @Override
    protected void onStart() {
        if (updateMusicPlayerUIListener == null) {
            updateMusicPlayerUIListener = new UpdateMusicPlayerUIListener();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_MUSIC_PLAYER_UI_UPDATE);
            filter.addAction(ACTION_MUSIC_PLAYER_CLOSE);
            filter.addAction(ACTION_PLAY_BUTTON_IMAGE_CHANGE);
            filter.addAction(ACTION_FAVORITE_IMAGE_CHANGE);

            registerReceiver(updateMusicPlayerUIListener, filter);
        }
        if (settingsContentObserver == null) {
            settingsContentObserver = new SettingsContentObserver(new Handler());
            this.getApplicationContext().getContentResolver().registerContentObserver(
                    android.provider.Settings.System.CONTENT_URI, true,
                    settingsContentObserver);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if (updateMusicPlayerUIListener != null)
                unregisterReceiver(updateMusicPlayerUIListener);

            if (settingsContentObserver != null) {
                this.getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, TAG, "MusicPlayerActivity : OnDestroy");
            Crashlytics.logException(e);
        }
        unbindService(this);

        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        Log.i(TAG, "onDestroy: onDestroy()");
        finish();

        super.onDestroy();
    }

    //=== start implements ServiceConnection methods===
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicPlayerService.MusicPlayerBinder musicPlayerBinder = (MusicPlayerService.MusicPlayerBinder) service;
        musicPlayerService = musicPlayerBinder.getService();

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(MusicPlayerService.NOTIF_ID);
            }
            musicPlayerService.stopForeground(true);
            //musicPlayerService.stopSelf();

            Log.i(TAG, "onServiceConnected: " + MusicPlayerService.isRunningService());
            if (!MusicPlayerService.isRunningService()) {
                Intent service_intent = new Intent(MusicPlayerActivity.this, MusicPlayerService.class);

                startService(service_intent);
            }

            String realPath = Utilities.getUriRealPath(this, intent.getData()).replace("/mnt/media_rw", "/storage");
            File file = new File(realPath);
            if (file.exists()) {
                mediaPlayer = musicPlayerService.getMediaPlayer();
                ArrayList<MediaFile> tempMediaFiles = new ArrayList<>();
                MediaFile mediaFile = new MediaFile();

                mediaFile.setPath(realPath);
                tempMediaFiles.add(mediaFile);

                musicPlayerService.setMediaFiles(tempMediaFiles);
                musicPlayerService.setCurrentMusicIndex(0);
                Uri fileUri = Uri.fromFile(new File(musicPlayerService.getMediaFiles().get(0).getPath()));

                musicPlayerService.prepareMediaPlayer(fileUri, true);
                prepareViews();
            }
        } else {
            if (musicPlayerService.getMediaPlayer() != null && musicPlayerService.getMediaFiles() != null && musicPlayerService.getMediaFiles().size() > 0) {
                mediaPlayer = musicPlayerService.getMediaPlayer();
                prepareViews();
            } else {
                mediaPlayer = musicPlayerService.getMediaPlayer();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    //=== end implements ServiceConnection methods===

    private class UpdateMusicPlayerUIListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_MUSIC_PLAYER_UI_UPDATE: {
                        intent.setAction("");
                        prepareViews();
                        break;
                    }
                    case ACTION_PLAY_BUTTON_IMAGE_CHANGE: {
                        if (mediaPlayer.isPlaying()) {
                            playButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, null));
                            setCurrentVisualizerVisibility(View.VISIBLE);
                        } else {
                            playButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, null));
                            setCurrentVisualizerVisibility(View.INVISIBLE);
                        }
                        intent.setAction("");
                        break;
                    }
                    case ACTION_FAVORITE_IMAGE_CHANGE: {
                        setFavoriteImage();
                        intent.setAction("");
                        break;
                    }
                    case ACTION_MUSIC_PLAYER_CLOSE: {
                        Log.i(TAG, "onReceive: ACTION_MUSIC_PLAYER_CLOSE");

                        Intent service_intent = new Intent(MusicPlayerActivity.this, MusicPlayerService.class);
                        stopService(service_intent);

                        intent.setAction("");
                        finish();
                        break;
                    }
                }
            }
        }
    }

    public class SettingsContentObserver extends ContentObserver {

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.v(TAG, "Settings change detected");
            if (audioManager != null && volumeSeekbar != null && volumeSettingImage != null)
            {
               int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
               volumeSeekbar.setProgress(currentVolume);
               if (currentVolume == 0)
                   volumeSettingImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_off, null));
               else
                   volumeSettingImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_up, null));
            }
        }
    }
}
