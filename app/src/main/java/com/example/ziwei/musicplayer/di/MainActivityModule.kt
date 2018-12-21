package com.example.ziwei.musicplayer.di

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import com.example.ziwei.musicplayer.DownloadRepository
import com.example.ziwei.musicplayer.DownloadUsecase
import com.example.ziwei.musicplayer.DownloadUsecaseImpl
import com.example.ziwei.musicplayer.MainActivity
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class MainActivityModule {
  @Provides
  @Named("MainActivity")
  @PerActivity
  fun provideContext(mainActivity: MainActivity): Context { return mainActivity}


  @Provides
  @PerActivity
  fun provideDownloadUsecase(mDownloadRepository: DownloadRepository):
      DownloadUsecase {
    return DownloadUsecaseImpl(mDownloadRepository)
  }

  @Provides
  @PerActivity
  fun provideNotificationManager(@Named("MainActivity") context: Context)
      : NotificationManager {
    return context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  }
}