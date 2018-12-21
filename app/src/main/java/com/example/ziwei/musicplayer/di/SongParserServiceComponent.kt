package com.example.ziwei.musicplayer.di

import android.content.Context
import com.example.ziwei.musicplayer.SongParserService
import dagger.Component
import dagger.Subcomponent
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Subcomponent(modules =
[SongParserServiceModule::class])
@PerActivity
interface SongParserServiceComponent:
    AndroidInjector<SongParserService> {

  @Subcomponent.Builder
  abstract class Builder:
      AndroidInjector.Builder<SongParserService>()
}