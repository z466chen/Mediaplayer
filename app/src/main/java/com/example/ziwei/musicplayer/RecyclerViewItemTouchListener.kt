package com.example.ziwei.musicplayer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.single_song_layout.view.song_list_checkbox


class RecyclerViewItemTouchListener: RecyclerView.OnItemTouchListener {
  var isPressed = false;
  var currentTime = 0L;
  var songSize = 0;
  companion object {
    var preparedSongList = ArrayList<Boolean>()
    fun resetPreparedsongList(songSize: Int) {
      preparedSongList = ArrayList()
      for (index in 0..songSize - 1) {
        preparedSongList.add(true)
      }
    }
  }


  fun RecyclerViewItemTouchListener(songSize: Int) {
    this.songSize = songSize
    for (index in 0..songSize - 1) {
      preparedSongList.add(true)
    }

  }

  override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
    when (e.action) {
      MotionEvent.ACTION_DOWN-> {
        isPressed = true
        currentTime = System.currentTimeMillis()
      }
      MotionEvent.ACTION_UP -> {
        isPressed = false
        currentTime = 0
      }
    }
    if (System.currentTimeMillis() - currentTime > 2000 && isPressed) {
      val view = rv.findChildViewUnder(e.x, e.y)
      view!!.song_list_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
          preparedSongList[rv.getChildPosition(view)] = isChecked
      }
      isPressed = false
      currentTime = 0
    }

    return true
  }

  override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}