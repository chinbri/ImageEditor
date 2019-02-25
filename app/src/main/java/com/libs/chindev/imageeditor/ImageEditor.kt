package com.libs.chindev.imageeditor

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.image_editor.view.*


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

    }

}