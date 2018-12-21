package com.example.ziwei.musicplayer.di

import android.app.Activity
import android.app.Application
import android.app.Service
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class MusicPlayerApplication: Application(), HasActivityInjector, HasServiceInjector{

  @Inject
  lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

  @Inject
  lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

  override fun activityInjector(): AndroidInjector<Activity> {
    return activityDispatchingAndroidInjector
  }

  override fun serviceInjector(): AndroidInjector<Service> {
    return serviceDispatchingAndroidInjector
  }

  override fun onCreate() {
    DaggerAppComponent
        .builder()
        .application(this)
        .build()
        .inject(this);

    super.onCreate()
  }

}