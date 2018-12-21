package com.example.ziwei.musicplayer.di

import android.content.Context
import android.media.MediaPlayer
import com.example.ziwei.musicplayer.MusicPlayerNotificaitonManager
import com.example.ziwei.musicplayer.PlayerChangeUsecase
import com.example.ziwei.musicplayer.PlayerChangeUsecaseImpl
import com.example.ziwei.musicplayer.SongParserService
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Module
class SongParserServiceModule @Inject
constructor() {

  @Provides
  @Named("SongParserService")
  @PerActivity
  fun getContext(songParserService:
  SongParserService): Context {
    return songParserService
  }

  @Provides
  @PerActivity
  fun provideMediaPlayer(): MediaPlayer {
    return MediaPlayer()
  }

  @Provides
  @PerActivity
  fun provideNotificationManager(@Named
  ("SongParserService") context: Context):
      MusicPlayerNotificaitonManager {
    return MusicPlayerNotificaitonManager(context, false)
  }

  @Provides
  @PerActivity
  fun providePlayerChangeUsecase(): PlayerChangeUsecase {
    return PlayerChangeUsecaseImpl()
  }
}