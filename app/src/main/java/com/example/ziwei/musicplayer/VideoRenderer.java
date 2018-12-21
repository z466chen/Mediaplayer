package com.example.ziwei.musicplayer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoRenderer implements GLSurfaceView.Renderer {

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
  }

  @Override public void onDrawFrame(GL10 gl) {
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glTranslatef(0.6f, 0.8f, -1.5f);
    gl.glRotatef(0f, 0f, 0.1f, 0f);
  }

}
