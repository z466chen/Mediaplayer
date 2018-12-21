package com.example.ziwei.musicplayer.di

import com.example.ziwei.musicplayer.MainActivity
import dagger.Component
import dagger.Subcomponent
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Subcomponent(modules =
[MainActivityModule::class])
@PerActivity
interface MainActivityComponent :
    AndroidInjector<MainActivity>{
  @Subcomponent.Builder
  abstract class Builder:
      AndroidInjector.Builder<MainActivity>()
}