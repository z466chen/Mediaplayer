package com.example.ziwei.musicplayer

import io.reactivex.Emitter
import io.reactivex.Observable

enum class SongAction {
  PREVIOUS,
  NEXT,
  START,
  STOP
}

interface PlayerChangeUsecase {
  fun execute(): Observable<SongAction>
  fun provideEmitter(): Emitter<SongAction>?
}

class PlayerChangeUsecaseImpl: PlayerChangeUsecase {

  private var mEmitter: Emitter<SongAction>? = null

  override fun provideEmitter(): Emitter<SongAction>? {
    return mEmitter
  }

  override fun execute(): Observable<SongAction> {
    return Observable.create {
      mEmitter = it
    }
  }
}