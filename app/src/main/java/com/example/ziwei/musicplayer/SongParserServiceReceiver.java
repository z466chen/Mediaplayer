package com.example.ziwei.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import io.reactivex.Emitter;

public class SongParserServiceReceiver extends BroadcastReceiver {
  public static final String PREVIOUS = "previous";
  public static final String NEXT = "next";
  public static final String STOP = "stop";
  public static final String START = "start";
  private static Emitter<SongAction> emitter = null;


  public static void registerEmitter(Emitter<SongAction> mEmitter) { emitter = mEmitter; }

  @Override public void onReceive(Context context, Intent intent) {
    if (PREVIOUS.equals(intent.getAction())) {
      Log.e("songServiceReceiver", "PREVIOUS");
      if (emitter != null) emitter.onNext(SongAction.PREVIOUS);
    } else if (NEXT.equals(intent.getAction())) {
      Log.e("songServiceReceiver", "NEXT");
      if (emitter != null) emitter.onNext(SongAction.NEXT);
    } else if (STOP.equals(intent.getAction())) {
      Log.e("songServiceReceiver", "STOP");
      if (emitter != null) emitter.onNext(SongAction.STOP);
    } else if (START.equals(intent.getAction())) {
      Log.e("songServiceReceiver", "START");
      if (emitter != null) emitter.onNext(SongAction.START);
    } else {
      Log.e("songServiceReceiver", "other");
    }
  }


 }
