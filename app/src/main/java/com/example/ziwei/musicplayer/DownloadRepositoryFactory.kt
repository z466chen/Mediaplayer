package com.example.ziwei.musicplayer

import android.content.Context.MODE_PRIVATE
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import com.example.ziwei.musicplayer.ReadStatus.PROGRESS
import io.reactivex.Emitter
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Okio
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream

class DownloadRepositoryFactory {

  private var responseBody: DownloadResponseBody? = null



  private class DownloadResponseBody(val file: File): ResponseBody() {
    private var responseBody: ResponseBody? = null
    private var emitter: Emitter<ReadStatus>? = null

    override fun contentLength(): Long {
      return if (responseBody == null) 0
      else responseBody!!.contentLength()
    }

    override fun contentType(): MediaType? {
      return responseBody?.contentType()
    }

    override fun source(): BufferedSource? {
      if (responseBody == null || responseBody!!.contentLength() <= 0) {
        emitter?.onError(Throwable(if (responseBody == null)
          "response body is null" else "content length is 0"))
        return null
      }
      return Okio.buffer(object: ForwardingSource(responseBody!!.source()) {
        override fun read(sink: Buffer, byteCount: Long): Long {
          val readBytes = super.read(sink, byteCount)
          if (readBytes < 0) {
            emitter?.onError(Throwable("read bytes is smaller than 0"))
          } else if (contentLength() <= file.length()) {
            emitter?.onComplete()
          } else {
              if (file.exists()) {
/*                val reader = sink.buffer().inputStream()
                */
              } else {
                emitter?.onError(Throwable("file does not exist or file cannot be written, " +
                    "file path: " + file.absolutePath))
              }
            }
          return readBytes
        }
      })
    }

    fun setResponseBody(responseBody: ResponseBody?) {
      this.responseBody = responseBody
    }

    fun setEmitter(emitter: Emitter<ReadStatus>) {
      this.emitter = emitter
    }

  }

  fun create(file: File, emitter: Emitter<ReadStatus>): ResponseBody {
    responseBody = DownloadResponseBody(file)
    responseBody!!.setEmitter(emitter)
    return responseBody!!
  }

  fun getResposeBody(responseBody: ResponseBody?): ResponseBody {
    this.responseBody!!.setResponseBody(responseBody)
    return this.responseBody!!
  }

  fun getCurrentResponseBody(): ResponseBody? { return responseBody}

  companion object {
    fun readFile(file: File, reader: InputStream, emitter:Emitter<ReadStatus>?) {
      val bufferWriter = FileOutputStream(file, true)
      val buf = ByteArray(4096)
      try {
        var read = reader.read(buf)
        Log.e("reading: ", "count: $read, content: $buf")
        while (read != -1) {
          bufferWriter.write(buf, 0, read)
          read = reader.read(buf)
        }
        emitter?.onNext(PROGRESS)
      } catch (e: Exception) {
        e.printStackTrace()
        emitter?.onError(Throwable("writing file fails"))
      } finally {
        bufferWriter.close()
      }
    }
  }


}