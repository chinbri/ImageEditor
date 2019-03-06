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


class ImageEditor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val EVENT_DISTANCE = 10
    }

    private val colorOne = context.resources.getColor(R.color.defaultColorOne)
    private val colorTwo = context.resources.getColor(R.color.defaultColorTwo)
    private val colorThree = context.resources.getColor(R.color.defaultColorThree)

    private var previousCoordinates: PointF? = null

    constructor(context: Context) : this(context, null){
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.image_editor, this)

        val options = BitmapFactory.Options()
        options.inMutable = true

        //Create a new image bitmap and attach a brand new canvas to it
        val tempBitmap = BitmapFactory.decodeStream(context.assets.open("map.png"), null, options)
        var tempCanvas = Canvas(tempBitmap)

        val paint = PaintBuilder()
            .setColor(colorOne)
            .setStrokeWidth(20f)
            .build()

        ivMainImage.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                performClick()

                when (event?.action){

                    MotionEvent.ACTION_DOWN -> {
                        println("DOWN ${event.x}, ${event.y}")
                    }

                    MotionEvent.ACTION_MOVE -> {

                        //Draw the image bitmap into the cavas
                        tempCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

                        if(checkMovement(previousCoordinates, event)){

                            tempCanvas.drawLine(
                                previousCoordinates?.x ?: event.x,
                                previousCoordinates?.y ?: event.y,
                                event.x,
                                event.y,
                                paint)

                        }

                        tempCanvas.save();
                        tempCanvas.translate(0f, 0f);
                        tempCanvas.restore();

                    }

                    MotionEvent.ACTION_UP -> {

                        //Attach the canvas to the ImageView
                        ivMainImage.setImageDrawable(BitmapDrawable(resources, tempBitmap))

                    }

                    else -> println("OTHER")
                }

                println("PREVIOUSEVENT ${previousCoordinates?.x} ${previousCoordinates?.y}  ---   EVENT ${event?.x} ${event?.y}")

                if(previousCoordinates == null || checkMovement(previousCoordinates, event)){

                    previousCoordinates = PointF(event!!.x, event.y)

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

    }

    fun checkMovement(previousEvent: PointF?, event: MotionEvent?): Boolean{

        if(previousEvent != null && event != null){
            return Math.abs(previousEvent.x - event.x) > EVENT_DISTANCE
                    || Math.abs(previousEvent.y - event.y) > EVENT_DISTANCE
        }else{
            return false
        }

    }

}