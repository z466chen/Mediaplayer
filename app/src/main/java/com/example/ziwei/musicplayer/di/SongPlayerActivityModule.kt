package com.example.ziwei.musicplayer.di

import android.os.Looper
import dagger.Module
import dagger.Provides
import android.os.Handler
import javax.inject.Singleton

@Module
class SongPlayerActivityModule {

  @Provides
  @PerActivity
  fun provideHandler(): Handler {
    return Handler(Looper.getMainLooper())
  }
}