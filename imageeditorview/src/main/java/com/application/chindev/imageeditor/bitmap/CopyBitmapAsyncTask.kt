package com.application.chindev.imageeditor.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import java.io.File
import java.io.RandomAccessFile
import java.lang.ref.WeakReference
import java.nio.channels.FileChannel

class CopyBitmapAsyncTask(context: Context): AsyncTask<Bitmap, Void, Bitmap?>() {

    private val weakReferenceContext: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: Bitmap): Bitmap? {

        weakReferenceContext.get()?.let {
            val file = File("${it.externalCacheDir}/tempFile")

            val randomAccessFile = RandomAccessFile(file, "rw")

            randomAccessFile.use {
                val channel = randomAccessFile.channel

                channel.use {
                    val originalBitmap: Bitmap = params[0]

                    val map = channel.map(FileChannel.MapMode.READ_WRITE, 0, originalBitmap.width.toLong() * originalBitmap.height * 4)

                    originalBitmap.copyPixelsToBuffer(map)
                    originalBitmap.recycle()

                    val mutableBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, originalBitmap.config)
                    map.position(0)
                    mutableBitmap.copyPixelsFromBuffer(map)

                    file.delete()

                    return mutableBitmap
                }
            }
        }
        return null
    }
}