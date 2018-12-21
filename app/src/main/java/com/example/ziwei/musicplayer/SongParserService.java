package com.example.ziwei.musicplayer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Switch;
import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.inject.Inject;
import org.jetbrains.annotations.Nullable;

public class SongParserService extends Service{

  private final String SongParserServiceStart = "songparserservicestart";
  public final static String SongParserServiceVideo = "songparserservicevideo";
  private String uri =
      Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
  private ArrayList<SingleSong> playerlist;


  @Inject
  MediaPlayer mPlayer;
  @Inject
  MusicPlayerNotificaitonManager notificationManager;
  @Inject
  PlayerChangeUsecase mPlayerChangeUsecase;

  private int currentIndex = 0;
  public static final int SEQUENCE_MODE = 0;
  public static final int RANDOM_MODE = 1;
  public static final int SEQUENCE_LOOP_MODE = 2;
  public static final int SINGLE_LOOP_MODE = 3;
  private boolean isStarted = false;
  private boolean isNewSong = false;
  private boolean isPaused = false;
  private int mode = SEQUENCE_MODE;
  private boolean isVideoPlaying = false;
  private long currentSongTime = 0;
  private boolean isSurfacePaused = false;
  private Notification notification = null;
  private SongParserServiceBinder binder =
      new SongParserServiceBinder();

  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private ObservableEmitter<Integer> updateActivityEmitter = null;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    AndroidInjection.inject(this);

    Log.e("service", "onStartCommand");
    initPlayerList();
    Disposable mPlayerDisposible =  mPlayerChangeUsecase.execute().subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).doOnNext(new Consumer<SongAction>() {
      @Override public void accept(SongAction songAction) throws Exception {
        dealWithAction(songAction, SongParserService.this);
      }
    }).subscribe();
    compositeDisposable.add(mPlayerDisposible);
    notificationManager = new MusicPlayerNotificaitonManager(
        this, isPaused);
    SongParserServiceReceiver.registerEmitter(mPlayerChangeUsecase.provideEmitter());
    notification = notificationManager.getNotification();
    startForeground(MusicPlayerNotificaitonManager.NOTIFICATION_ID, notification);


    return super.onStartCommand(intent, flags, startId);

  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return binder;
  }



  public class SongParserServiceBinder extends Binder {
    public void startSongAt(int index, Context mContext) { startSongImpl(index, mContext);}

    public void startNextSong(Context mContext) { startNextSongImpl(mContext);}

    public void startPreviousSong(Context mContext) { startPreviousSongImpl(mContext);}



    public ArrayList<SingleSong> loadSongList() { return playerlist;}

    public void setPlayerMode(final int playerMode, Context mContext) {
      setPlayerModeImpl(playerMode, mContext);
    }

    public Observable<Integer> updateActivityView() {
      return Observable.create(new ObservableOnSubscribe<Integer>() {
        @Override public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
          updateActivityEmitter = emitter;
        }
      });
    }

    public boolean getIsPaused() {return getIsPausedImpl();}

    public void startVideoPlayerAfterPause(final Context mContext,
        final GLSurfaceView mGLSurfaceView) throws IOException {
      startVideoPlayerAfterPauseImpl(mContext, mGLSurfaceView);
    }

    public boolean isVideoPlaying() { return isVideoPlayingImpl();}

    public boolean isStarted() { return isStartedImpl();}

    public void setIsSurfacePaused(boolean isPaused) {
      setIsSurfacePausedImpl(isPaused);
    }

    public void stopCurrentSong(Context mContext) { stopCurrentSongImpl(mContext);}

    public void restartCurrentSong(Context mContext) { restartCurrentSongImpl(mContext);}

    public void startVideoPlayer(Context mContext, GLSurfaceView mGLSurfaceView) throws IOException {
      startVideoPlayerImpl(mContext, mGLSurfaceView);
    }

    public boolean isNewSong() { return isNewSongImpl();}

    public void songSeted() { songSetedImpl();}

    public MediaPlayer getmPlayer() { return getmPlayerImpl();}

    public int getCurrentSongPosition() { return SongParserService.this.getCurrentSongPosition();}

    public int getCurrentSongDuration() { return SongParserService.this.getCurrentSongDuration();}

    public int getPlayerMode() { return SongParserService.this.getPlayerMode();}

    public void setCurrentSongProgress(float progress) {
      SongParserService.this.setCurrentSongProgress(progress);
    }

    public void setVideoSurface(Context mContext, GLSurfaceView mGLSurfaceView) {
      setVideoSurfaceImpl(mGLSurfaceView, mContext);
    }
  }







  private void initPlayerList() {
    playerlist = musicFileManager.getSongList();
  }



  String getUri() {
    return uri;
  }

  private void dealWithAction(SongAction songAction, Context mContext) {
    Log.e("service", "deakwithAction");
    switch (songAction) {
      case PREVIOUS: {
        startPreviousSongImpl(mContext);
        break;
      }
      case NEXT: {
        startNextSongImpl(mContext);
        break;
      }
      case STOP: {
        stopCurrentSongImpl(mContext);
        break;
      }
      case START: {
        restartCurrentSongImpl(mContext);
        break;
      }
    }
  }


  public void startVideoPlayerAfterPauseImpl(final Context mContext,
      final GLSurfaceView mGLSurfaceView) throws IOException {
    if (!isSurfacePaused || !isVideoPlaying ||
        mPlayer == null || mGLSurfaceView == null) return;
    currentSongTime = mPlayer.getCurrentPosition();
    mPlayer.reset();
    mPlayer.release();
    mPlayer = new MediaPlayer();
    final Uri uri = Uri.parse(playerlist.get(currentIndex).getSongUrl());
    Log.e("wrong", " " + uri.toString());
    mPlayer.setDataSource(new FileInputStream(new File(uri.toString())).getFD());
    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        final SurfaceHolder SHolder = mGLSurfaceView.getHolder();
        mGLSurfaceView.setRenderer(new VideoRenderer());
        SHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        SHolder.setFixedSize(176, 180);
        setPlayerModeImpl(mode, mContext);
        mPlayer.seekTo((int) currentSongTime);
        mPlayer.setDisplay(SHolder);
        if (!isPaused) mPlayer.start();
        isSurfacePaused = false;
      }
    });
    mPlayer.prepareAsync();
  }

  public void setPlayerListImpl(ArrayList<SingleSong> songList) {
    playerlist = songList;
  }

  public MediaPlayer getmPlayerImpl() { return mPlayer;}

  public void startCurrentSongImpl(final Context mContext) {
    Log.e("startCurrentSongImpl", "startcurentsong");
    if (currentIndex < 0
        || currentIndex >= playerlist.size()) {
      return;
    }
    try {
      if (!isStarted) {
        final Uri uri = Uri.parse(playerlist.get(currentIndex).getSongUrl());
        isVideoPlaying = isVideoTypeImpl(uri);
        isNewSong = true;
        if(!isVideoPlaying) startAudioPlayerImpl(mContext);
      }
      isStarted = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Boolean getIsPausedImpl() {
    return isPaused;
  }

  public boolean isNewSongImpl() {
    return isNewSong;
  }

  public void songSetedImpl() {
    isNewSong = false;
  }

  public boolean isStartedImpl() {
    return isStarted;
  }

  public void startAudioPlayerImpl(Context mContext) throws IOException {
    final Uri uri = Uri.parse(playerlist.get(currentIndex).getSongUrl());
    if (mPlayer != null) {
      mPlayer.reset();
      mPlayer.release();
    }
    mPlayer = new MediaPlayer();
    setPlayerModeImpl(mode, mContext);
    Log.e("wrong", " " + uri.toString());
    mPlayer.setDataSource(new FileInputStream(new File(uri.toString())).getFD());
    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
      }
    });
    mPlayer.prepareAsync();
  }


  public void setIsSurfacePausedImpl(boolean paused) {
    isSurfacePaused = paused;
  }

  public void setPlayerModeImpl(final int playerMode, Context mContext) {
    mode = playerMode;
    final Context context = mContext;
    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
        resetSongState(context);
        startSongImpl(getNextSong(playerMode), context);
      }
    });
  }

  public void startVideoPlayerImpl(final Context mContext,
      final GLSurfaceView mGLSurfaceView)
      throws IOException {
    final Uri uri = Uri.parse(playerlist.get(currentIndex).getSongUrl());
    if (mPlayer != null) {
      mPlayer.reset();
      mPlayer.release();
    }
    mPlayer = new MediaPlayer();
    setPlayerModeImpl(mode, mContext);
    Log.e("wrong", " " + uri.toString());
    mPlayer.setDataSource(new FileInputStream(new File(uri.toString())).getFD());
    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        setVideoSurfaceImpl(mGLSurfaceView, mContext);
        mPlayer.start();
      }
    });
    mPlayer.prepareAsync();
  }

  private boolean isVideoTypeImpl(Uri uri) {
    return (uri.toString()).endsWith(".mp4") || (uri.toString()).endsWith(".MP4");
  }

  private void setVideoSurfaceImpl(GLSurfaceView mGLSurfaceView, Context mContext) {
    if (mGLSurfaceView == null || !isVideoPlaying) return;
    final SurfaceHolder SHolder = mGLSurfaceView.getHolder();
    mGLSurfaceView.setRenderer(new VideoRenderer());
    if(mPlayer != null) mPlayer.setDisplay(SHolder);
    SHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    SHolder.setFixedSize(176, 180);
    Log.e("songParserService", "setVideoService");
  }

  public boolean isVideoPlayingImpl() {
    return isVideoPlaying;
  }

  public void stopCurrentSongImpl(Context mContext) {
    Log.e("service", "stopCurrentSong");
    mPlayer.pause();
    isPaused = true;
    Log.e("service", "togglenotificationstopcurrentsong" + isPaused);
    updateNotificationButton(mContext);
  }

  public void restartCurrentSongImpl(Context mContext) {
    Log.e("service", "togglenotificationresetcurrentsong" + isPaused);
    mPlayer.start();
    isPaused = false;
    updateNotificationButton(mContext);
  }

  public void startNextSongImpl(Context mContext) {
    Log.e("service", "startNextSong");
    if (getNextSong(mode) < playerlist.size()) {
      startSongImpl(getNextSong(mode), mContext);
    }
  }

  public void startPreviousSongImpl(Context mContext) {
    Log.e("service", "startPreviousSong");
    if (getPreviousSong(mode) >= 0) {
      startSongImpl(getPreviousSong(mode), mContext);
    }
  }

  public void startSongImpl(int index, Context mContext) {
    Log.e("service", "startSong " + index);
    if (currentIndex != index) resetSongState(mContext);
    currentIndex = index;

    startCurrentSongImpl(mContext);
  }



  @Override public void onDestroy() {
    Log.e("service", "onDestroy");
    if (mPlayer != null && mPlayer.isPlaying()) {
      mPlayer.reset();
      mPlayer.release();
    }
    isStarted = false;
    isPaused = false;

    notificationManager.cancelNotification();
    compositeDisposable.clear();
    super.onDestroy();
  }

  public int getPlayerMode() {
    return mode;
  }

  public void setCurrentSongProgress(float newProgress) {
    mPlayer.seekTo((int) (newProgress*mPlayer.getDuration()));
  }

  private void resetSongState(Context mContext) {
    isPaused = false;
    isStarted = false;
    isVideoPlaying = false;
    isNewSong = false;
    isSurfacePaused = false;
    currentSongTime = 0;
    Log.e("service", "togglenotificationreset" + isPaused);
    updateNotificationButton(mContext);
  }

  private void updateNotificationButton(Context mContext) {
    Log.e("service", "togglenotification" + isPaused);
    notificationManager.toggleNotification(isPaused,
        mContext);
    if (updateActivityEmitter != null) updateActivityEmitter.onNext(0);
  }

  public int getCurrentSongPosition() {
    if (mPlayer != null) {
      return mPlayer.getCurrentPosition();
    } else {
      return 0;
    }
  }

  public int getCurrentSongDuration() {
    if (mPlayer != null) {
      return mPlayer.getDuration();
    } else {
      return 1;
    }
  }

  private int getNextSong(int playerMode) {
    switch (playerMode) {
      case SEQUENCE_MODE:
        return currentIndex + 1;
      case RANDOM_MODE:
        return new Random().nextInt(playerlist.size());
      case SEQUENCE_LOOP_MODE:
        return (currentIndex + 1) % playerlist.size();
      case SINGLE_LOOP_MODE:
        return currentIndex;
      default: return -1;
    }
  }

  private int getPreviousSong(int playerMode) {
    switch (playerMode) {
      case SEQUENCE_MODE:
        return currentIndex - 1;
      case RANDOM_MODE:
        return new Random().nextInt(playerlist.size());
      case SEQUENCE_LOOP_MODE:
        return (currentIndex + playerlist.size() - 1) % playerlist.size();
      case SINGLE_LOOP_MODE:
        return currentIndex;
      default: return -1;
    }
  }


}
