package haselmehri.app.com.elimusicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import haselmehri.app.com.elimusicplayer.SQLiteHelper.MusicPlayerSQLiteHelper;
import haselmehri.app.com.elimusicplayer.model.Favorite;

import static android.os.Build.VERSION.SDK_INT;

public class MusicPlayerService extends Service {
    private static boolean isRunningService;
    private static final String TAG = "MusicPlayerService";
    private static final String ACTION_STOP_SERVICE = "com.example.haselmehri.firstapplication.STOP_SERVICE";
    private static final String ACTION_PLAY = "com.example.haselmehri.firstapplication.PLAY_MUSIC";
    private static final String ACTION_FAVORITE = "com.example.haselmehri.firstapplication.FAVORITE_MUSIC";
    private static final String ACTION_FORWARD = "com.example.haselmehri.firstapplication.FORWARD_MUSIC";
    private static final String ACTION_REWIND = "com.example.haselmehri.firstapplication.REWIND_MUSIC";
    private static final String ACTION_NEXT = "com.example.haselmehri.firstapplication.NEXT_MUSIC";
    private static final String ACTION_PREVIOUS = "com.example.haselmehri.firstapplication.PREVIOUS_MUSIC";
    private static final int CURRENT_BUILD_API = SDK_INT;
    public static final int NOTIF_ID = 10123410;
    private Boolean isFavorite = null;
    private static final String CHANNEL_ID = "music_player_chanel_id";
    private NotificationCompat.Builder builder;
    private Notification notification;
    private RemoteViews notification_content;
    private MediaPlayer mediaPlayer;
    private ArrayList<MediaFile> mediaFiles;
    private MusicPlayerBinder musicPlayerBinder = new MusicPlayerBinder();
    private int currentMusicIndex = 0;
    private NotificationManager notificationManager;
    private HeadsetPlugUnPluggingListener headsetPlugUnPluggingListener;
    private MusicPlayerSQLiteHelper musicPlayerSQLiteHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        setupMediaPlayer();

        musicPlayerSQLiteHelper = new MusicPlayerSQLiteHelper(this);
        headsetPlugUnPluggingListener = new HeadsetPlugUnPluggingListener();
        registerReceiver(headsetPlugUnPluggingListener, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        return musicPlayerBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;

        if (intent.getAction() == null)
            intent.setAction("");

        switch (intent.getAction()) {
            case ACTION_STOP_SERVICE:
                setRunningService(false);
                unregisterReceiver(headsetPlugUnPluggingListener);
                musicPlayerClose();
                notificationManager.cancel(NOTIF_ID);
                stopForeground(true);
                stopSelf();

                //setRunningService(false);

                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.END_DATE, formatter.format(date));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

                Log.i(TAG, "onStartCommand: StopService");
                break;
            case ACTION_PLAY:
                actionPlayHandle();
                break;
            case ACTION_REWIND:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 30000);
                break;
            case ACTION_FORWARD:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 30000);
                break;
            case ACTION_NEXT:
                nextMusicPlay();
                //updateNoificationActionUI();
                break;
            case ACTION_PREVIOUS:
                previousMusicPlay();
                //updateNoificationActionUI();
                break;
            case ACTION_FAVORITE:
                setFavoriteStatus();
                break;
            default:
                setRunningService(true);
                //=====content PendingIntent======
                Intent showMusicPlayerActitvityIntent = new Intent(this, MusicPlayerActivity.class);
                showMusicPlayerActitvityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //showMusicPlayerActitvityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, showMusicPlayerActitvityIntent, 0);
                //=====content PendingIntent======

                //=====close PendingIntent======
                Intent stopIntent = new Intent(this, MusicPlayerService.class);
                stopIntent.setAction(ACTION_STOP_SERVICE);
                PendingIntent closePendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                //=====play PendingIntent======

                //=====play PendingIntent======
                Intent playIntent = new Intent(this, MusicPlayerService.class);
                playIntent.setAction(ACTION_PLAY);
                PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, 0);
                //=====play PendingIntent======

                //=====previous PendingIntent======
                Intent previousIntent = new Intent(this, MusicPlayerService.class);
                previousIntent.setAction(ACTION_PREVIOUS);
                PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, 0);
                //=====previous PendingIntent======

                //=====next PendingIntent======
                Intent nextIntent = new Intent(this, MusicPlayerService.class);
                nextIntent.setAction(ACTION_NEXT);
                PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);
                //=====next PendingIntent======

                //=====rewind PendingIntent======
                Intent rewindIntent = new Intent(this, MusicPlayerService.class);
                rewindIntent.setAction(ACTION_REWIND);
                PendingIntent rewindPendingIntent = PendingIntent.getService(this, 0, rewindIntent, 0);
                //=====rewind PendingIntent======

                //=====forward PendingIntent======
                Intent forwardIntent = new Intent(this, MusicPlayerService.class);
                forwardIntent.setAction(ACTION_FORWARD);
                PendingIntent forwardPendingIntent = PendingIntent.getService(this, 0, forwardIntent, 0);
                //=====forward PendingIntent======

                //=====favorite PendingIntent======
                Intent favoriteIntent = new Intent(this, MusicPlayerService.class);
                favoriteIntent.setAction(ACTION_FAVORITE);
                PendingIntent favoritePendingIntent = PendingIntent.getService(this, 0, favoriteIntent, 0);
                //=====forward PendingIntent======

                notification_content = new RemoteViews(getPackageName(), R.layout.media_palyer_notification);
                notification_content.setOnClickPendingIntent(R.id.action_clear_button, closePendingIntent);
                notification_content.setOnClickPendingIntent(R.id.play_button, playPendingIntent);
                notification_content.setOnClickPendingIntent(R.id.forward_button, forwardPendingIntent);
                notification_content.setOnClickPendingIntent(R.id.rewind_button, rewindPendingIntent);
                notification_content.setOnClickPendingIntent(R.id.skip_next_button, nextPendingIntent);
                notification_content.setOnClickPendingIntent(R.id.skip_previous_button, previousPendingIntent);
                notification_content.setOnClickPendingIntent(R.id.favorite_image, favoritePendingIntent);

                if (mediaPlayer != null && mediaFiles != null && mediaFiles.size() > 0 && notification_content != null) {
                    notification_content.setTextViewText(R.id.text_music_name, mediaFiles.get(currentMusicIndex).getPath().substring(mediaFiles.get(currentMusicIndex).getPath().lastIndexOf("/") + 1));
                }

                builder = new NotificationCompat.Builder(this, CHANNEL_ID);

                if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                    //CharSequence ticker = getResources().getString(R.string.ticker_text);
                    notification = new Notification(R.drawable.ic_music_note_black_36dp, "Eli Music Player", System.currentTimeMillis());
                    notification.bigContentView = notification_content;
                    notification.contentIntent = contentPendingIntent;

                    notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
                    notification.defaults |= Notification.DEFAULT_LIGHTS;

                    // starting service with notification in foreground mode
                    startForeground(NOTIF_ID, notification);

                } else if (CURRENT_BUILD_API >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = builder
                            .setCustomBigContentView(notification_content)
                            .setSmallIcon(R.drawable.ic_music_note_black_36dp)
                            .setContentTitle("Eli Music Player")
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setTicker("Eli Music Player")
                            .setContentIntent(contentPendingIntent)
                            .build();

                    if (SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "chanel_name";// The user-visible name of the channel.
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                        notificationChannel.setSound(null, null);

                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    startForeground(NOTIF_ID, notification);
                }
                updatePlayButtonImage(false);
                updateNoificationActionUI();
                setFavoriteImage();

                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        if (mediaFiles != null)
            mediaFiles.clear();

        Log.i(TAG, "onDestroy: onDestroy()");
        super.onDestroy();
    }

    private void setupMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    public MediaPlayer getMediaPlayer() {
        //int sessionId = mediaPlayer.getAudioSessionId();
        return mediaPlayer;
    }

    public int getCurrentMusicIndex() {
        return currentMusicIndex;
    }

    public void setCurrentMusicIndex(int currentMusicIndex) {
        this.currentMusicIndex = currentMusicIndex;
    }

    public ArrayList<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFiles) {
        if (mediaFiles != null && mediaFiles.size() > 0) {
            this.mediaFiles = mediaFiles;
        }
    }

    public void prepareMediaPlayer(Uri fileUri, final boolean isPlaying) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, fileUri);
            //better for data(music,video...) from server,in method do not block main thread
            mediaPlayer.prepareAsync();

            //better for local data(music,video...),in method do block main thread
            //mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //prepareViews(isPlaying);
                    if (isPlaying)
                        mediaPlayer.start();

                    if (notification_content != null) {
                        updatePlayButtonImage(false);
                        updateNoificationActionUI();
                        setFavoriteImage();
                    }

                    sendToMusicPlayerUpdateBroadCast();
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.start();
                    nextMusicPlay();
                    updateNoificationActionUI();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayButtonImage(boolean updateNotification) {
        if (notification_content != null) {
            //updateNoificationActionUI();

            if (mediaPlayer.isPlaying()) {
                notification_content.setImageViewResource(R.id.play_button, R.drawable.ic_action_pause_green);
                if (updateNotification) {
                    if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                        notificationManager.notify(NOTIF_ID, notification);
                    } else {
                        notificationManager.notify(NOTIF_ID, builder.build());
                    }
                }
            } else {
                notification_content.setImageViewResource(R.id.play_button, R.drawable.ic_action_play_blue);
                if (updateNotification) {
                    if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                        notificationManager.notify(NOTIF_ID, notification);
                    } else {
                        notificationManager.notify(NOTIF_ID, builder.build());
                    }
                }
            }
        }
    }

    private void updateNoificationActionUI() {
        if (notification_content != null) {
            notification_content.setTextViewText(R.id.text_selected_music_count, "Musics : " + (getCurrentMusicIndex() + 1) + " of " + getMediaFiles().size());
            notification_content.setTextViewText(R.id.text_music_name, mediaFiles.get(currentMusicIndex).getPath().substring(mediaFiles.get(currentMusicIndex).getPath().lastIndexOf("/") + 1));

            MediaFile mediaFile = mediaFiles.get(currentMusicIndex);
            Bitmap coverSong = Utilities.getCoverPictureBySong(mediaFile.getPath());
            if (coverSong != null) {
                notification_content.setImageViewBitmap(R.id.image_thumbnail, coverSong);
                Log.i(TAG, "updateNoificationActionUI: " + mediaFiles.get(currentMusicIndex).getThumbnail());
            } else {
                notification_content.setImageViewResource(R.id.image_thumbnail, R.drawable.default_thumbnail);
                Log.i(TAG, "updateNoificationActionUI: image_thumbnail not exist");
            }
            if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                notificationManager.notify(NOTIF_ID, notification);
            } else {
                notificationManager.notify(NOTIF_ID, builder.build());
            }
        }
    }

    private void nextMusicPlay() {
        if (currentMusicIndex < mediaFiles.size() - 1) {
            currentMusicIndex += 1;
            MediaFile mediaFile = mediaFiles.get(currentMusicIndex);
            Uri musicUri = Uri.fromFile(new File(mediaFile.getPath()));
            prepareMediaPlayer(musicUri, mediaPlayer.isPlaying());
        } else {
            currentMusicIndex = 0;
            MediaFile mediaFile = mediaFiles.get(0);
            Uri musicUri = Uri.fromFile(new File(mediaFile.getPath()));
            prepareMediaPlayer(musicUri, mediaPlayer.isPlaying());
        }
    }

    private void previousMusicPlay() {
        if (currentMusicIndex > 0) {
            currentMusicIndex -= 1;
            MediaFile mediaFile = mediaFiles.get(currentMusicIndex);
            Uri musicUri = Uri.fromFile(new File(mediaFile.getPath()));
            prepareMediaPlayer(musicUri, mediaPlayer.isPlaying());
        } else {
            currentMusicIndex = mediaFiles.size() - 1;
            MediaFile mediaFile = mediaFiles.get(currentMusicIndex);
            Uri musicUri = Uri.fromFile(new File(mediaFile.getPath()));
            prepareMediaPlayer(musicUri, mediaPlayer.isPlaying());
        }
    }

    public void setFavoriteStatus() {
        if (getMediaFiles() != null && getMediaFiles().size() > 0) {
            MediaFile mediaFile = getMediaFiles().get(getCurrentMusicIndex());

            if (isFavorite) {
                if (musicPlayerSQLiteHelper.checkFavoriteExists(mediaFile.getPath())) {
                    if (musicPlayerSQLiteHelper.deleteFavorite(mediaFile.getPath()))
                        setFavoriteImage();
                } else {
                    setFavoriteImage();
                }
            } else {
                if (!musicPlayerSQLiteHelper.checkFavoriteExists(mediaFile.getPath())) {
                    Favorite favorite = new Favorite();
                    favorite.setFilePath(mediaFile.getPath());
                    if (musicPlayerSQLiteHelper.addFavorite(favorite))
                        setFavoriteImage();
                } else {
                    setFavoriteImage();
                }
            }
        }
    }

    private void setFavoriteImage() {
        if (musicPlayerSQLiteHelper.checkFavoriteExists(getMediaFiles().get(getCurrentMusicIndex()).getPath())) {
            isFavorite = true;
        } else {
            isFavorite = false;
        }

        if (notification_content != null) {
            if (isFavorite) {
                notification_content.setImageViewResource(R.id.favorite_image, R.drawable.ic_favorite_heart_gold);
            } else {
                notification_content.setImageViewResource(R.id.favorite_image, R.drawable.ic_favorite_heart_gray);
            }
            if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                notificationManager.notify(NOTIF_ID, notification);
            } else {
                notificationManager.notify(NOTIF_ID, builder.build());
            }
        }
        sendToFavoriteImageChangeBroadCast();
    }

    public boolean IsCurrentMusicInFavoriteList() {
        if (isFavorite == null) {
            setFavoriteImage();
        }
        return isFavorite;
    }

    private void actionPlayHandle() {
        if (notification_content == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notification_content.setImageViewResource(R.id.play_button, R.drawable.ic_action_play_blue);
            if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                notificationManager.notify(NOTIF_ID, notification);
            } else {
                notificationManager.notify(NOTIF_ID, builder.build());
            }
        } else {
            mediaPlayer.start();
            notification_content.setImageViewResource(R.id.play_button, R.drawable.ic_action_pause_green);
            //notificationManager.notify(NOTIF_ID, builder.setContent(notification_content).build());
            if (CURRENT_BUILD_API < Build.VERSION_CODES.HONEYCOMB) {
                notificationManager.notify(NOTIF_ID, notification);
            } else {
                notificationManager.notify(NOTIF_ID, builder.build());
            }
        }

        sendToPlayButtonImageChangeBroadCast();
    }

    private void sendToPlayButtonImageChangeBroadCast() {
        Intent broadcastIntent = new Intent(MusicPlayerActivity.ACTION_PLAY_BUTTON_IMAGE_CHANGE);
        sendBroadcast(broadcastIntent);
    }

    private void sendToFavoriteImageChangeBroadCast() {
        Intent broadcastIntent = new Intent(MusicPlayerActivity.ACTION_FAVORITE_IMAGE_CHANGE);
        sendBroadcast(broadcastIntent);
    }

    private void musicPlayerClose() {
        Intent broadcastIntent = new Intent(MusicPlayerActivity.ACTION_MUSIC_PLAYER_CLOSE);
        sendBroadcast(broadcastIntent);
    }

    private void sendToMusicPlayerUpdateBroadCast() {
        Intent broadcastIntent = new Intent(MusicPlayerActivity.ACTION_MUSIC_PLAYER_UI_UPDATE);
        sendBroadcast(broadcastIntent);
    }

    public static boolean isRunningService() {
        return isRunningService;
    }

    public static void setRunningService(boolean runningService) {
        isRunningService = runningService;
    }

    class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    private class HeadsetPlugUnPluggingListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.i(TAG, "Headset is unplugged");
                        //Toast.makeText(MusicPlayerService.this,"Headset is unplugged",Toast.LENGTH_LONG).show();
                        actionPlayHandle();
                        break;
                    case 1:
                        Log.i(TAG, "Headset is plugged");
                        //Toast.makeText(MusicPlayerService.this,"Headset is plugged",Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Log.i(TAG, "I have no idea what the headset state is");
                        //Toast.makeText(MusicPlayerService.this,"I have no idea what the headset state is",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}