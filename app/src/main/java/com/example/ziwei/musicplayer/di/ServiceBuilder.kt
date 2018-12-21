package com.example.ziwei.musicplayer.di

import android.app.Service
import com.example.ziwei.musicplayer.SongParserService
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ServiceKey
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module
public abstract class ServiceBuilder {
  @Binds
  @IntoMap
  @ServiceKey(SongParserService::class)
  abstract fun bindSongParserService(service: SongParserServiceComponent.Builder):
      AndroidInjector.Factory<out Service>
}