package com.example.ziwei.musicplayer

import android.os.Environment
import io.reactivex.Observable
import java.io.File

enum class ReadStatus {
  PROGRESS
}

interface DownloadUsecase {
  fun execute(url:String): Observable<ReadStatus>

  fun getFile(): File?

  fun getTotalLength(): Long
}

class DownloadUsecaseImpl(
    val mDownloadRepository: DownloadRepository
): DownloadUsecase{
  private var file: File? = null
  private var totalLength = 0L


  override fun execute(url: String): Observable<ReadStatus> {
    val uri = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).path + "/1.mp3"
    file = File(uri)
    file!!.createNewFile()
    return Observable.create {
      val response = mDownloadRepository.response(file!!, url, it)
      totalLength = mDownloadRepository.getTotalLength()
      DownloadRepositoryFactory.readFile(file!!, response.body()?.byteStream()!!, it)
    }
  }

  override fun getFile(): File? {
    return file
  }

  override fun getTotalLength(): Long {
    return totalLength
  }

}