package com.example.ziwei.musicplayer;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import io.reactivex.Emitter;

public class MusicPlayerNotificaitonManager {
  private static final String SPA_ACTION = "songplayeractivity";
  private static final String CHANNEL_ID = "song_channel";
  public static final int NOTIFICATION_ID = 1;
  private boolean mPaused = false;
  private Context mContext;
  private static NotificationCompat.Builder mNotification;
  private Notification notification;
  private static NotificationManager mNotificationManager;
  private NotificationChannel notificationChannel = null;

 public MusicPlayerNotificaitonManager(Context mContext, boolean mPaused) {
    this.mContext = mContext;
    this.mPaused = mPaused;
    Intent SPAIntent = new Intent(mContext, SongPlayerActivity.class);
    PendingIntent SPAPendingIntent = PendingIntent.getActivity(mContext, 0, SPAIntent, 0);
    mNotification =
        new NotificationCompat.Builder(mContext, CHANNEL_ID).setSmallIcon(
            R.drawable.music_icon_small).setContentIntent(SPAPendingIntent);
    setPreviousButton(mNotification, mContext);
    setStopButton(mNotification, mContext, mPaused);
    setNextButton(mNotification, mContext);
    mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationChannel = new NotificationChannel(
          CHANNEL_ID, "channel", NotificationManager.IMPORTANCE_LOW);
      mNotificationManager.createNotificationChannel(notificationChannel);
    }
    notification = mNotification.build();
  }

  private void setPreviousButton(NotificationCompat.Builder builder, Context mContext) {
    Intent receiverIntent = new Intent(mContext, SongParserServiceReceiver.class);
    receiverIntent.setAction(SongParserServiceReceiver.PREVIOUS);
    PendingIntent receiverPendingIntent =
        PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, receiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    NotificationCompat.Action action =
        new NotificationCompat.Action(R.drawable.reverse_button_small, "previous", receiverPendingIntent);
    builder.addAction(action);
  }

  private void setNextButton(NotificationCompat.Builder builder, Context mContext) {
    Intent receiverIntent = new Intent(mContext, SongParserServiceReceiver.class);
    receiverIntent.setAction(SongParserServiceReceiver.NEXT);
    PendingIntent receiverPendingIntent =
        PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, receiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    NotificationCompat.Action action =
        new NotificationCompat.Action(R.drawable.next_button_small, "next", receiverPendingIntent);
    builder.addAction(action);
  }

  private void setStopButton(NotificationCompat.Builder builder, Context mContext, boolean mPaused) {
    Log.e("setStopButton", "setStopButton" + mPaused);
    Intent receiverIntent = new Intent(mContext, SongParserServiceReceiver.class);
    receiverIntent.setAction((!mPaused) ? SongParserServiceReceiver.STOP : SongParserServiceReceiver.START);
    PendingIntent receiverPendingIntent =
        PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, receiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    NotificationCompat.Action action =
        new NotificationCompat.Action((!mPaused) ? R.drawable.stop_button_small
            : R.drawable.play_button_small,
            (!mPaused)? "stop": "start", receiverPendingIntent);
    builder.addAction(action);
  }

  public void notifyNotification() {
    mNotificationManager.notify(NOTIFICATION_ID, notification);
  }

  public NotificationCompat.Builder getmNotification() {return mNotification;}

  public Notification getNotification() {
    return notification;
  }

  public void cancelNotification() {
    mNotificationManager.cancel(NOTIFICATION_ID);
  }



  public void toggleNotification(boolean isPaused, Context context) {
    Log.e("toggle", "togglenotification" + isPaused);
    mNotification.mActions.clear();
    setPreviousButton(mNotification, context);
    setStopButton(mNotification, context, isPaused);
    setNextButton(mNotification, context);
    notification = mNotification.build();
    notifyNotification();
  }
}

