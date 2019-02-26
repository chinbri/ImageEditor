package com.libs.chindev.imageeditor.paint

import android.graphics.Paint

class PaintBuilder {

    private var color: Int? = null
    private var stokeWidth: Float? = null

    fun build(): Paint {

        val paint = Paint()

        this.color?.let {
            paint.color = it
        }

        this.stokeWidth?.let {
            paint.strokeWidth = it
        }

        return paint

    }

    fun setColor(color: Int): PaintBuilder{
        this.color = color
        return this
    }

    fun setStrokeWidth(strokeWidth: Float): PaintBuilder{
        this.stokeWidth = strokeWidth
        return this
    }
}