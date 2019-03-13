package com.application.chindev.imageeditor.bitmap

import android.graphics.Bitmap
import android.view.View

class BitmapUtils {

    companion object {

        fun scaleBitmapToView(bitmap: Bitmap, view: View): Bitmap{

            val aspectRatioBitmap = bitmap.width.toFloat() / bitmap.height
            val aspectRatioImageView = view.width / view.height

            var newBitmapWidth: Int
            var newBitmapHeight: Int

            if(aspectRatioBitmap > aspectRatioImageView){
                newBitmapWidth = view.width
                newBitmapHeight = (view.width / aspectRatioBitmap).toInt()

                if(newBitmapHeight > view.height){
                    newBitmapWidth = newBitmapWidth * view.height / newBitmapHeight
                    newBitmapHeight = view.height
                }
            }else{
                newBitmapHeight = view.height
                newBitmapWidth = (view.height * aspectRatioBitmap).toInt()

                if(newBitmapWidth > view.width){
                    newBitmapHeight = newBitmapHeight * view.width / newBitmapWidth
                    newBitmapWidth = view.width
                }
            }

            return Bitmap.createScaledBitmap(bitmap, newBitmapWidth, newBitmapHeight, false)

        }
    }

}