package com.example.ziwei.musicplayer;

import android.animation.ObjectAnimator;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Rational;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;
import java.util.Map;
import java.util.logging.LogRecord;
import javax.inject.Inject;
import org.jetbrains.annotations.Nullable;

public class SongPlayerActivity extends AppCompatActivity {

  private Boolean mStopped = false;
  private ViewGroup mRoot;
  private ImageButton mPreviousButton;
  private ImageButton mNextButton;
  private ImageButton mStopButton ;
  private ProgressBar mProgressBar;
  private GLSurfaceView mGLSurfaceView;
  private ImageView mProgressBarButton;
  private int mProgressBarWidth;
  private ImageButton mPlayerModeButton;
  private float ProgressBarButtonOriginalX;
  private Runnable updateProgressBarRunnable;
  private Runnable songPlayingRunnable;

  @Inject
  Handler mHandler;

  private int mPlayerMode;
  private boolean isInPictureInPictureMode = false;

  private SongParserService.SongParserServiceBinder binder = null;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      binder = (SongParserService.SongParserServiceBinder) service;
      Disposable updateDisposible  = binder.updateActivityView().doOnNext(new Consumer<Integer>() {
        @Override public void accept(Integer integer) throws Exception {
          if (binder.getIsPaused()) {
            mStopped = true;
            mStopButton.setBackground(getImage(SongPlayerActivity.this, "play_button"));
          } else {
            mStopped = false;
            mStopButton.setBackground(getImage(SongPlayerActivity.this, "stop_button"));
          }
        }
      }).subscribe();
      compositeDisposable.add(updateDisposible);
      resumeActivity();
    }

    @Override public void onServiceDisconnected(ComponentName name) {

    }
  };


  private View.OnTouchListener mGLSurfaceViewTouchListener = new View.OnTouchListener() {
    MotionEvent.PointerCoords middleStartPoint = new MotionEvent.PointerCoords();
    int startId;
    int viewHeight;
    int viewWidth;
    boolean viewParamGetted = false;
    ArrayList<Integer> startPointList = new ArrayList<>();
    ArrayList<MotionEvent.PointerCoords> startCoordList = new ArrayList<>();
    @Override public boolean onTouch(View v, MotionEvent event) {
      int action = event.getActionMasked();
      if (action == MotionEvent.ACTION_DOWN) {
        if (!viewParamGetted) {
          viewHeight = mGLSurfaceView.getHeight();
          viewWidth = mGLSurfaceView.getWidth();
        }
        viewParamGetted = true;
        middleStartPoint.x = event.getRawX();
        middleStartPoint.y = event.getRawY();
        startCoordList.add(middleStartPoint);
        startPointList = new ArrayList<>();
        startId = event.getPointerId(0);
        startPointList.add(startId);
      } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
        MotionEvent.PointerCoords currentCoords = new MotionEvent.PointerCoords();
        event.getPointerCoords(event.getActionIndex(), currentCoords);
        int id = event.getPointerId(event.getActionIndex());
        if (!startPointList.contains(id)) {
          startPointList.add(id);
          startCoordList.add(currentCoords);
        }
        middleStartPoint = middlePoint(startCoordList, event.getPointerCount());
      } else if (action == MotionEvent.ACTION_MOVE) {
        int count = event.getPointerCount();
        MotionEvent.PointerCoords middlePoint = middlePoint(
            listofCoords(event, startPointList, count), count);
        if (count == 1) {
          move(v, middlePoint);
          return true;
        }
        shrink(v, 1 - (0.5/distance(new PointF(viewWidth, viewHeight),
            new PointF(0,0)))*distance(
                new PointF(middleStartPoint.x, middleStartPoint.y),
            new PointF(middlePoint.x, middlePoint.y)));
        move(v, middleStartPoint);
      } else if (action == MotionEvent.ACTION_POINTER_UP) {
        int index = startPointList.indexOf(event.getPointerId(event.getActionIndex()));
        startPointList.remove(index);
        startCoordList.remove(index);
        middleStartPoint = middlePoint(startCoordList, startCoordList.size());
      }
      return true;
    }

    private MotionEvent.PointerCoords middlePoint(ArrayList<MotionEvent.PointerCoords> listofCoords,
        int length) {
      float resultX = 0;
      float resultY = 0;
      for (int i = 0; i < length; ++i) {
        resultX += listofCoords.get(i).x;
        resultY += listofCoords.get(i).y;
      }

      MotionEvent.PointerCoords result = new MotionEvent.PointerCoords();
      result.x = resultX/length;
      result.y = resultY/length;
      return result;
    }

    private float distance(PointF a, PointF b) {
      return (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private ArrayList<MotionEvent.PointerCoords> listofCoords(MotionEvent event
        ,ArrayList<Integer> startPointList, int length) {
      ArrayList<MotionEvent.PointerCoords> result = new ArrayList<>(length);
      for (int i = 0; i < length; ++i) {
        result.add(new MotionEvent.PointerCoords());
        int index = event.findPointerIndex(startPointList.get(i));
        event.getPointerCoords(index, result.get(i));
      }
      return result;
    }

    private void move(View v, MotionEvent.PointerCoords des) {
      v.setTranslationX(des.x - viewWidth/2);
      v.setTranslationY(des.y - viewHeight/2);
      v.requestLayout();
    }

    private void shrink(View v, double ratio) {
      ViewGroup.LayoutParams lp = v.getLayoutParams();
      lp.width =  (int)(viewWidth*ratio);
      lp.height = (int)(viewHeight*ratio);
      v.requestLayout();
    }
  };

  public static String UPDATE_STOP_BUTTON = "updatestopbutton";
  public static String EXTRA_VIDEO = "video";

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.e("SongPlayer", "onCreate");
    Log.e("songPlayer", " " + Thread.currentThread().getId());
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);


    setContentView(R.layout.song_player_layout);
    bindView();

    mProgressBar.post(new Runnable() {
      @Override public void run() {
        mProgressBarWidth = mProgressBar.getWidth();
        updatingProgressBar();
      }
    });

    Intent i = new Intent(this, SongParserService.class);
    bindService(i, mServiceConnection, BIND_AUTO_CREATE);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    compositeDisposable.clear();
    unbindService(mServiceConnection);
  }

  private void resumeActivity() {
    mProgressBarWidth = mProgressBar.getWidth();
    Log.e("SongPlayer", "bindView" + mProgressBarWidth);
    mStopped = mStopped || binder.getIsPaused();
    if (mStopped) {
      Log.e("songplayeractivity", "stop");
      mStopButton.setBackground(getImage(SongPlayerActivity.this,
          "play_button"));
    }

    startPlayerOnce(true);

    showControls();
    startPlayers();
    startUpdatingProgressBar();
    updatingPlayerMode();
    if (isInPictureInPictureMode) {
      showControls();
    }
  }

  @Override protected void onResume() {
    super.onResume();

    if (binder != null) {
      resumeActivity();
    }
  }

  @Override protected void onPause() {
    stopUpdatingProgressBar();
    endPlayers();
    binder.setIsSurfacePaused(true);
    super.onPause();

  }


  void bindView() {
    Log.e("SongPlayer", "bindView");
    mRoot = findViewById(R.id.view_root);
    mPreviousButton = (ImageButton) findViewById(R.id.previous_button);
    mNextButton = (ImageButton) findViewById(R.id.next_Button);
    mStopButton = (ImageButton) findViewById(R.id.stop_play_button);
    mProgressBar = (ProgressBar) findViewById(R.id.music_progressbar);
    mProgressBarButton = (ImageView) findViewById(R.id.progressbar_button);
    mPlayerModeButton = (ImageButton) findViewById(R.id.play_mode_button);


    int [] progressBarPosition = new int[2];
    mProgressBarButton.getLocationOnScreen(progressBarPosition);
    ProgressBarButtonOriginalX = progressBarPosition[0];
    mProgressBar.setMax(1000);

    mRoot.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
/*          case MotionEvent.ACTION_UP : {
            changeControls(isNavigationShown);
            isNavigationShown = !isNavigationShown;
          }*/

        }
        return true;
      }
    });

    mPreviousButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.e("SongPlayer", "onClickPrevious");
        binder.startPreviousSong(SongPlayerActivity.this);
        resetLayout();
      }
    });

    mStopButton.setOnClickListener(new View.OnClickListener() {

      @Override public void onClick(View v) {
        Log.e("SongPlayer", "onClickStop");
        if (!mStopped) {
          binder.stopCurrentSong(SongPlayerActivity.this);
          v.setBackground(getImage(SongPlayerActivity.this,
              "play_button"));
          mStopped = true;

        } else {
          v.setBackground(getImage(SongPlayerActivity.this,
              "stop_button"));
          binder.restartCurrentSong(SongPlayerActivity.this);
          mStopped = false;
        }
      }
    });

    mNextButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.e("SongPlayer", "onClickNext");
        binder.startNextSong(SongPlayerActivity.this);
        resetLayout();
      }
    });

    mProgressBarButton.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN: { break; }
          case (MotionEvent.ACTION_MOVE): {
            if ((event.getRawX() - 138 < ProgressBarButtonOriginalX)
                || (event.getRawX() - 138 >
                ProgressBarButtonOriginalX + mProgressBar.getWidth() - 10)) {break;}
            Log.e("SongPlayer", "onDrag");
            v.setX(event.getRawX() - 138);
            binder.setCurrentSongProgress((v.getX()
                -ProgressBarButtonOriginalX)/mProgressBar.getWidth());
            updatingProgressBar();
            break;
          }
          case MotionEvent.ACTION_UP: {
            v.performClick();
            break;
          }
        }
        return true;
      }

    });

    mPlayerModeButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mPlayerMode = (mPlayerMode + 1) % 4;
        binder.setPlayerMode(mPlayerMode, SongPlayerActivity.this);
        updatingPlayerMode();
      }
    });
  }

  private static Drawable getImage(Context c, String ImageName) {
    return c.getResources().getDrawable(c.getResources().getIdentifier(
        ImageName, "drawable", c.getPackageName()));
  }

  private void resetLayout() {
    mStopButton.setBackground(getImage(SongPlayerActivity.this, "stop_button"));
    mStopped = false;
  }

  private void startPlayers(){
      songPlayingRunnable = new Runnable() {
        @Override public void run() {
          startPlayerOnce(binder.isNewSong());
          mHandler.postDelayed(this, 500);
        }
      };
      mHandler.postDelayed(songPlayingRunnable, 500);
  }

  private void startPlayerOnce(boolean isNew) {
    Log.e("startPlayer", "startplayeronce");
    if (isNew) {
      if (binder.isVideoPlaying()) {
        Log.e("startPlayer", "isvideoplaying");
        if (mGLSurfaceView != null) {
          Log.e("startPlayer", "viewremoved");
          mRoot.removeView(mGLSurfaceView);
        }
        mGLSurfaceView = new GLSurfaceView(SongPlayerActivity.this);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mGLSurfaceView.setOnTouchListener(mGLSurfaceViewTouchListener);
        mRoot.addView(mGLSurfaceView,0,p);
        try {
          if (binder.isNewSong()) {
            binder.startVideoPlayer(SongPlayerActivity.this, mGLSurfaceView);
          } else if (binder.isStarted()) {
            binder.startVideoPlayerAfterPause(this, mGLSurfaceView);
          }
        } catch (Exception e) {
          Log.e("songPlayerActivity", "startVideoSong");
        }
      } else {
        if (mGLSurfaceView != null) mGLSurfaceView.setVisibility(View.GONE);
        mRoot.removeView(mGLSurfaceView);
        Log.e("songPlayerActivity", "startAudioSong");
      }
      binder.songSeted();
    }
  }

  private void endPlayers() {
    mHandler.removeCallbacks(songPlayingRunnable);
  }


  private void startUpdatingProgressBar() {
    Log.e("service", "startUpdatingProgressBar");

    updateProgressBarRunnable = new Runnable() {
      @Override public void run() {
        try {
          Log.e("service", "asynctask");
          updatingProgressBar();
        } catch (Exception e) {
          e.printStackTrace();
        }
        mHandler.postDelayed(this, 2000);
      }
    };
    mHandler.postDelayed(updateProgressBarRunnable, 2000);
  }



  private void stopUpdatingProgressBar() {
    mHandler.removeCallbacks(updateProgressBarRunnable);
  }

  public void updatingProgressBar() {
    if (binder.getmPlayer() == null
        || !binder.getmPlayer().isPlaying()) return;
    float progress = (float)(binder.getCurrentSongPosition()*1000)
        / (float) (binder.getCurrentSongDuration());
    Log.e("service", "updatingProgressBar");
    if (mProgressBar != null) {
      Log.e("service", "why it is not null");
      mProgressBar.setProgress((int) progress);
    } else {
      Log.e("service", "why it is null");
    }
    ObjectAnimator animator = ObjectAnimator.ofFloat(mProgressBarButton, "TranslationX"
    , (progress*mProgressBarWidth/1000)
            + ProgressBarButtonOriginalX);
    animator.start();
    float result = mProgressBarWidth;
    Log.e("service", "" + result);
  }

  public void updatingPlayerMode() {
     mPlayerMode = binder.getPlayerMode();
    if (mPlayerMode == SongParserService.SEQUENCE_MODE) {
      mPlayerModeButton.setBackground(getImage(this, "sequence_icon"));
    } else if (mPlayerMode == SongParserService.RANDOM_MODE) {
      mPlayerModeButton.setBackground(getImage(this, "shuffle_icon"));
    } else if (mPlayerMode == SongParserService.SEQUENCE_LOOP_MODE) {
      mPlayerModeButton.setBackground(getImage(this, "loop_icon"));
    } else if (mPlayerMode == SongParserService.SINGLE_LOOP_MODE){
      mPlayerModeButton.setBackground(getImage(this, "single_loop_icon"));
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch(keyCode) {
      case KeyEvent.KEYCODE_BACK : {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && binder.isVideoPlaying()) {
          PictureInPictureParams.Builder pm = new PictureInPictureParams.Builder();
          int height = getWindow().getDecorView().getHeight();
          int width = getWindow().getDecorView().getWidth();
          pm.setAspectRatio(new Rational(width, height));
          isInPictureInPictureMode = true;
          hideControls();
          enterPictureInPictureMode(pm.build());
          break;
        }
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  private void changeControls(boolean hide) {
    if (hide) {
      mStopButton.setVisibility(View.GONE);
      mPreviousButton.setVisibility(View.GONE);
      mNextButton.setVisibility(View.GONE);
      mProgressBarButton.setVisibility(View.GONE);
      mProgressBar.setVisibility(View.GONE);
      mPlayerModeButton.setVisibility(View.GONE);
    } else {
      mStopButton.setVisibility(View.VISIBLE);
      mPreviousButton.setVisibility(View.VISIBLE);
      mNextButton.setVisibility(View.VISIBLE);
      mProgressBarButton.setVisibility(View.VISIBLE);
      mProgressBar.setVisibility(View.VISIBLE);
      mPlayerModeButton.setVisibility(View.VISIBLE);
    }
  }
  private void showControls() {
    changeControls(false);
  }

  private void hideControls() {
    changeControls(true);
  }
}


