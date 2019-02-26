package com.libs.chindev.imageeditor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.image_editor.view.*
import android.graphics.drawable.BitmapDrawable
import com.libs.chindev.imageeditor.paint.PaintBuilder


class ImageEditor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null){
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.image_editor, this)

        ivMainImage.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                performClick()

                when (event?.action){

                    MotionEvent.ACTION_DOWN -> {
                        println("DOWN ${event.x}, ${event.y}")
                    }

                    MotionEvent.ACTION_MOVE -> {
                        println("MOVE ${event.x}, ${event.y}")
                    }

                    else -> println("OTHER")
                }
                return true
            }

        })

        ivColorOne.setOnClickListener {

        }

        ivColorTwo.setOnClickListener {

        }

        ivColorThree.setOnClickListener {

        }


        val options = BitmapFactory.Options()
        options.inMutable = true

        //Create a new image bitmap and attach a brand new canvas to it
        val tempBitmap = BitmapFactory.decodeStream(context.assets.open("map.png"), null, options)
        val tempCanvas = Canvas(tempBitmap)

//Draw the image bitmap into the cavas
        tempCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        val paint = PaintBuilder()
            .setColor(context.resources.getColor(R.color.colorAccent))
            .setStrokeWidth(20f)
            .build()

        tempCanvas.drawLine(0f, 0f, 1000f, 1000f, paint)

        tempCanvas.save();
        tempCanvas.translate(0f, 0f);
        tempCanvas.restore();

//Attach the canvas to the ImageView
        ivMainImage.setImageDrawable(BitmapDrawable(resources, tempBitmap))

    }

}