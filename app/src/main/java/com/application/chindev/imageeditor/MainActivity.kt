package com.application.chindev.imageeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.application.chindev.imageeditor.palette.PaletteConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = BitmapFactory.Options()
        options.inMutable = false

        val originalBitmap = BitmapFactory.decodeStream(assets.open("cat-4260536_1920.jpg"), null, options)

        originalBitmap?.let {

            //we can set source image as an attribute in the layout, or we can set a bitmap
            imageEditor.bitmap = originalBitmap

            //also we could change stroke width
            imageEditor.strokeWidth = 12f

            //we can add new colors in addition to xml attributes
            imageEditor.addColorToPalette("#008577")

            //configure color palette position and orientation
            imageEditor.configureColorsPalette(
                PaletteConfiguration(
                    true,
                    GravityEnum.TOP_RIGHT,
                    20,
                    "#D81B60",
                    OrientationEnum.HORIZONTAL,
                    "#FF6F00"
                )
            )
            //change current color
            imageEditor.setCurrentColor("#FF85B1")
        }

        btnSave.setOnClickListener {
            saveToFile(imageEditor.obtainBitmap())
        }

        btnErase.setOnClickListener {
            BitmapFactory.decodeStream(assets.open("cat-4260536_1920.jpg"), null, options)?.let {
                imageEditor.erase( it )
            }
        }

    }

    private fun saveToFile(bitmap: Bitmap){
        val f = File("$externalCacheDir/file.jpg")

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            FileOutputStream(f)
        )
    }

}
