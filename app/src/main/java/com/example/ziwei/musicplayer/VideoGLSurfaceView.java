package com.example.ziwei.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoGLSurfaceView extends GLSurfaceView {
  MediaPlayer mPlayer;
  Uri currentPlayingUri;

  public VideoGLSurfaceView(Context context) {
    super(context);
    setEGLContextClientVersion(2);
    setRenderMode(RENDERMODE_WHEN_DIRTY);
    setRenderer(new VideoRenderer());
  }

  public class VideoRenderer implements GLSurfaceView.Renderer {

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override public void onDrawFrame(GL10 gl) {
    }
  }
}
