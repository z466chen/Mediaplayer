package com.example.ziwei.musicplayer.di

import com.example.ziwei.musicplayer.SongPlayerActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Subcomponent(modules =
[SongPlayerActivityModule::class])
@PerActivity
interface SongPlayerActivityComponent:
    AndroidInjector<SongPlayerActivity> {
  @Subcomponent.Builder
  abstract class Builder: AndroidInjector
  .Builder<SongPlayerActivity>()
}