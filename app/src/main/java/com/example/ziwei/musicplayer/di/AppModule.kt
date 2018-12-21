package com.example.ziwei.musicplayer.di

import android.app.Application
import android.content.Context
import com.example.ziwei.musicplayer.DownloadRepository
import com.example.ziwei.musicplayer.DownloadRepositoryFactory
import com.example.ziwei.musicplayer.DownloadRepositoryImpl
import com.example.ziwei.musicplayer.DownloadUsecase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [MainActivityComponent::class,
  SongParserServiceComponent::class, SongPlayerActivityComponent::class])
class AppModule {

  @Provides
  @Singleton
  fun provideContext(application: Application): Context { return application}

  @Provides
  @Singleton
  fun provideDownloadRepositoryFactory():
    DownloadRepositoryFactory {
    return DownloadRepositoryFactory()
  }

  @Provides
  @Singleton
  fun provideDownloadRepository
      (mDownloadRepositoryFactory:
      DownloadRepositoryFactory):
      DownloadRepository {
    return DownloadRepositoryImpl(
        mDownloadRepositoryFactory)
  }

}