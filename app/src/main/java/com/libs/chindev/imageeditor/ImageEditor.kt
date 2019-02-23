package com.libs.chindev.imageeditor

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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

        ivColorOne.setOnClickListener {

        }

        ivColorTwo.setOnClickListener {

        }

        ivColorThree.setOnClickListener {

        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action){

            MotionEvent.ACTION_DOWN -> {

            }

            MotionEvent.ACTION_MOVE -> {

            }

        }
        performClick()
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}