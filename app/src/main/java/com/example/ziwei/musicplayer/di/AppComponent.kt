package com.example.ziwei.musicplayer.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.Component.Builder
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Component(modules = [AndroidInjectionModule::class,
  ServiceBuilder::class,
  AppModule::class, ActivityBuilder::class])
@Singleton
interface AppComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance fun application(application: Application): Builder

    fun build(): AppComponent
  }

  fun inject(musicPlayerApplication:
  MusicPlayerApplication)
}