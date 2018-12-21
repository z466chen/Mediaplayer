package com.example.ziwei.musicplayer

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.LayoutManager

import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.widget.Toast
import com.example.ziwei.musicplayer.SongParserService.SongParserServiceBinder
import com.example.ziwei.musicplayer.musicFileManager.getSingleSong
import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.other_button_1
import kotlinx.android.synthetic.main.activity_main.scroll_view

import kotlinx.android.synthetic.main.activity_main.song_holder
import java.io.File
import javax.inject.Inject


class MainActivity : AppCompatActivity(), OnTouchListener {

  var mAdapter: ListofMusicAdapter? = null
  var mLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

  val SongParserServiceVideo = "songparserservicestart"

  var mService : SongParserServiceBinder? = null

  private var songList: ArrayList<SingleSong>? = null

  val url = "https://sendto.club/tuWBZB:GEe3rB"

  @Inject
  lateinit var mDownloadUsecase: DownloadUsecase
  val notificationId = 9000
  @Inject
  lateinit var notificationManager: NotificationManager


  private val compositeDisposable = CompositeDisposable()

  val mServiceConnection = object : ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      Log.e("MainActivity", "serviceconnected" + mService?.loadSongList());
      mService = service as SongParserServiceBinder
      songList = mService?.loadSongList()
      mAdapter = ListofMusicAdapter(this@MainActivity, songList)
      song_holder.adapter = mAdapter
      song_holder.layoutManager = mLayoutManager
      bindView()
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    Log.e("MainActivity", "onCreate");
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (Build.VERSION.SDK_INT >= VERSION_CODES.M  &&
        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 0)
    } else {
      val i = Intent(this, SongParserService::class.java)
      startService(i)
      val bindIntent = Intent(this, SongParserService::class.java)
      bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }
  }


  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
      grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 0 &&
        permissions.size > 0 &&
        grantResults.size > 0 &&
        grantResults[0].equals(PERMISSION_GRANTED)) {
      val i = Intent(this, SongParserService::class.java)
      startService(i)
      val bindIntent = Intent(this, SongParserService::class.java)
      bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
    } else if (requestCode == 1 && permissions.size >0
        && grantResults.size > 0 && grantResults[0].equals(PERMISSION_GRANTED)) {
      startDownload()
    } else {
      finish()
    }
  }



  private var scrollEmitter: Emitter<Int>? = null

  override fun onDestroy() {
    Log.e("MainActivity", "Destroy");
    val i = Intent(this, SongParserService::class.java)
    unbindService(mServiceConnection)
    super.onDestroy()
  }

  fun bindView() {
/*    check_playlist.setOnClickListener {
      SongParserService().setPlayerList()
    }

    cancel_playlist.setOnClickListener {

    }*/

    other_button_1.setOnClickListener { view ->
      RxJavaPlugins.setErrorHandler { it.printStackTrace() }
      if(VERSION.SDK_INT >= VERSION_CODES.M) {
        val permissionResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionResult == PackageManager.PERMISSION_GRANTED) startDownload()
        else requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
      } else {
        startDownload()
      }

    }

    song_holder.viewTreeObserver.addOnScrollChangedListener {
      detectScrollTop { mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 }
    }


/*    song_holder.post(object: Runnable {
      override fun run() {
        scrollObservable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext { t: Throwable ->
              return@onErrorResumeNext scrollObservable
            }
            .subscribe({},{},
                {updateSongLists()})
        song_holder.removeCallbacks(this)
      }

    })*/
  }

  private var isShowingScrollView = false


  override fun onTouch(v: View?, event: MotionEvent?): Boolean {
    Log.e("scroll" , "visible")
    if (isShowingScrollView) {
      Log.e("scroll" , "visible")
      if (event?.action == MotionEvent.ACTION_DOWN) {
        scroll_view.visibility = VISIBLE
      } else if (event?.action == MotionEvent.ACTION_UP) {
        scrollEmitter?.onError(Throwable("user drag ended"))
      }
      scrollEmitter?.onNext(0)
      return true
    } else {
      return super.onTouchEvent(event)
    }
  }

  private fun detectScrollTop(isTop: () -> Boolean) {
    var currentTime = 0L
    if (isTop.invoke()) {
      Observable.create<Int> {
        Log.e("scroll" , "show")
        currentTime = System.currentTimeMillis()
        scrollEmitter = it
        isShowingScrollView = true
        song_holder
      }.subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            if (System.currentTimeMillis() - currentTime >= 1000) {
              scrollEmitter?.onComplete()
            }
          }, { hideScrollView()}, {updateSongLists()})

    } else {
      Log.e("scroll" , "hide")
      scrollEmitter?.onError(Throwable("user scroll back"))

    }
  }

  private fun hideScrollView() {
    scroll_view.visibility = GONE
    scrollEmitter = null
    isShowingScrollView = false
  }


  private fun updateSongLists() {
    songList?.forEach {
      val file = File(it.songUrl)
      if (!file.exists()) songList!!.remove(it)
    }
    mAdapter?.notifyDataSetChanged()
  }


  private fun startDownload() {
    mDownloadUsecase.execute(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          var progress = 0f
          mDownloadUsecase.getFile()?.length().apply {
            progress = if (this == null ||
                mDownloadUsecase.getTotalLength() == 0L)
              0f else this.toFloat()/mDownloadUsecase.getTotalLength()
          }
          showDownloadNotification(progress)
        },
            {
              it.printStackTrace()
              Toast.makeText(this,
                "fail to download due to: " + it.message, Toast.LENGTH_SHORT).show()
              showDownloadNotification(1f)
            },

            { Toast.makeText(this,
                "download successfully", Toast.LENGTH_SHORT).show()
              songList?.add(getSingleSong(mDownloadUsecase.getFile()))
              mAdapter?.notifyDataSetChanged()
              showDownloadNotification(1f)
            }
        )
        .let { compositeDisposable.add(it) }
  }

  fun startSongActivity(pos: Int) {
    mService?.startSongAt(pos, this)
    val i = Intent(this, SongPlayerActivity::class.java)
    i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    startActivity(i)
  }

  private fun showDownloadNotification(progress: Float) {
    if (progress == 1f) {
      notificationManager.cancel(notificationId)
    }
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      notificationManager.createNotificationChannel(NotificationChannel("2",
          "channel2", NotificationManager.IMPORTANCE_LOW))
    }
    val pendingIntent = PendingIntent.getActivity(this, 1, Intent(
        this, MainActivity::class.java), FLAG_CANCEL_CURRENT)
    val notification = NotificationCompat.Builder(this, "2")
        .setSmallIcon(R.drawable.ic_launcher_foreground).setContentText("Downloading $progress")
        .setContentIntent(pendingIntent).setProgress(10000, (progress*10000).toInt(), false)
        .build()
    notificationManager.notify(notificationId, notification)
  }


}
