package com.example.ziwei.musicplayer.di

import android.app.Activity
import android.app.Service
import com.example.ziwei.musicplayer.MainActivity
import com.example.ziwei.musicplayer.SongParserService
import com.example.ziwei.musicplayer.SongPlayerActivity
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module
public abstract class ActivityBuilder {

  @Binds
  @IntoMap
  @ActivityKey(MainActivity::class)
  abstract fun bindMainActivity(activity: MainActivityComponent.Builder) :
      AndroidInjector.Factory<out Activity>

  @Binds
  @IntoMap
  @ActivityKey(SongPlayerActivity::class)
  abstract fun bindSongPlayerActivity(activity: SongPlayerActivityComponent.Builder):
      AndroidInjector.Factory<out Activity>

}