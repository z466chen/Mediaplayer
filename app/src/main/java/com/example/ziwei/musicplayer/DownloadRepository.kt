package com.example.ziwei.musicplayer

import io.reactivex.Emitter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File

interface DownloadRepository {

  fun response(file: File, url: String, emitter: Emitter<ReadStatus>): Response

  fun getTotalLength(): Long

}

class DownloadRepositoryImpl(
   private val mDownloadResponseBodyFactory: DownloadRepositoryFactory
): DownloadRepository {
  private var responseBody: ResponseBody? = null
  override fun response(file: File, url: String, emitter: Emitter<ReadStatus>): Response {
    responseBody = mDownloadResponseBodyFactory.create(file, emitter)
    return OkHttpClient.Builder().addInterceptor {
      val originalResponse = it.proceed(it.request())
      responseBody = mDownloadResponseBodyFactory.getResposeBody(originalResponse.body())
      return@addInterceptor originalResponse.newBuilder().body(responseBody).build()
    }.build().newCall(Request.Builder().url(url).build()).execute()

  }

  override fun getTotalLength(): Long {
    var result = 0L
    mDownloadResponseBodyFactory.getCurrentResponseBody().apply {
      result = if (this == null) 0 else this.contentLength()
    }
    return result
  }
}

