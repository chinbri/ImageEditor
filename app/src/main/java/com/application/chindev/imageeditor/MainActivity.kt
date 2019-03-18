package com.application.chindev.imageeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = BitmapFactory.Options()
        options.inMutable = false

        val originalBitmap = BitmapFactory.decodeStream(assets.open("wheel.jpg"), null, options)

        originalBitmap?.let {

            //we can setup source image as an attribute in the layout, or we can add a bitmap
            imageEditor.setup(originalBitmap)

            //we can add new colors in addition to xml attributes
            imageEditor.addColor("#008577")
            //also we could set stroke width
            imageEditor.strokeWidth = 12f

            //configure color palette position and orientation
            imageEditor.configureColorsPalette(
                GravityEnum.TOP_RIGHT,
                20,
                "#D81B60",
                OrientationEnum.HORIZONTAL)

            imageEditor.setCurrentColor("#FF6F00")
        }

        btnSave.setOnClickListener {
            saveToFile(imageEditor.obtainBitmap())
        }

        btnErase.setOnClickListener {
            imageEditor.erase( BitmapFactory.decodeStream(assets.open("wheel.jpg"), null, options))
        }

    }

    private fun saveToFile(bitmap: Bitmap){
        val f = File("${externalCacheDir}/file.jpg")

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            FileOutputStream(f)
        )
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
