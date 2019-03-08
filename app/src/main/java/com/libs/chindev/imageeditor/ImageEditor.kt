package com.libs.chindev.imageeditor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.image_editor.view.*
import android.graphics.drawable.BitmapDrawable
import android.view.ViewTreeObserver
import com.libs.chindev.imageeditor.paint.PaintBuilder
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.ImageView


class ImageEditor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val STROKE_WIDTH = 15f
    }

    private var path = Path()
    private var scaledPath = Path()
    private var scale = 1f

    private lateinit var paint: Paint
    private lateinit var scaledPaint: Paint

    private val colorList: MutableList<Int> = mutableListOf()

    private lateinit var modifiedBitmap: Bitmap

    var imageInitialized = false


    constructor(context: Context) : this(context, null){
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.image_editor, this)
    }

    fun addColor(color: Int){

        colorList.add(color)

        val colorView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.color_palette, null) as ImageView
        colorView.setColorFilter(color)
        llColours.addView(colorView)

        colorView.setOnClickListener {
            paint.color = color
            scaledPaint.color = color
        }

    }

    fun setup(originalBitmap: Bitmap){

        modifiedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true)

        var tempBitmap = originalBitmap

        paint = PaintBuilder()
            .setColor(resources.getColor(R.color.defaultColorOne))
            .setStrokeWidth(STROKE_WIDTH)
            .setStyle(Paint.Style.STROKE)
            .build()

        ivMainImage.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    ivMainImage.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    if(!imageInitialized){
                        val aspectRatioBitmap = modifiedBitmap.width.toFloat() / modifiedBitmap.height
                        val aspectRatioImageView = ivMainImage.width / ivMainImage.height

                        val newBitmapWidth: Int
                        val newBitmapHeight: Int

                        if(aspectRatioBitmap > aspectRatioImageView){
                            newBitmapWidth = ivMainImage.width
                            newBitmapHeight = (ivMainImage.width / aspectRatioBitmap).toInt()
                        }else{
                            newBitmapHeight = ivMainImage.height
                            newBitmapWidth = (ivMainImage.height * aspectRatioBitmap).toInt()
                        }

                        tempBitmap = Bitmap.createScaledBitmap(modifiedBitmap, newBitmapWidth, newBitmapHeight, false)
                        scale = modifiedBitmap.width.toFloat() / newBitmapWidth

                        scaledPaint = PaintBuilder()
                            .setColor(resources.getColor(R.color.defaultColorOne))
                            .setStrokeWidth(STROKE_WIDTH * scale)
                            .setStyle(Paint.Style.STROKE)
                            .build()

                        imageInitialized = true
                    }

                    val tempCanvas = Canvas(tempBitmap)
                    val originalCanvas = Canvas(modifiedBitmap)

                    val bitmapWidth = tempBitmap.width
                    val bitmapHeight = tempBitmap.height

                    val currentImageViewRatio =
                        ivMainImage.width.toFloat() / ivMainImage.height
                    val currentImageRatio = bitmapWidth.toFloat() / bitmapHeight

                    val layoutParams = ivMainImage.layoutParams
                    if (currentImageRatio > currentImageViewRatio) {
                        layoutParams.height = (ivMainImage.width / currentImageRatio).toInt()
                    } else {
                        layoutParams.width = (ivMainImage.height * currentImageRatio).toInt()
                    }

                    ivMainImage.layoutParams = layoutParams
                    ivMainImage.setImageBitmap(tempBitmap)

                    initializeMotionCapture(originalCanvas, tempCanvas, tempBitmap, paint, scaledPaint)

                }
            })
    }

    private fun initializeMotionCapture(
        originalCanvas: Canvas,
        tempCanvas: Canvas,
        tempBitmap: Bitmap,
        paint: Paint,
        scaledPaint: Paint
    ) {
        ivMainImage.setOnTouchListener(object : OnTouchListener {

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                performClick()

                when (event?.action) {

                    MotionEvent.ACTION_DOWN -> {
                        path = Path()
                        scaledPath = Path()

                        path.moveTo(event.x, event.y)
                        scaledPath.moveTo(event.x * scale, event.y * scale)
                    }

                    MotionEvent.ACTION_MOVE -> {

                        tempCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

                        path.lineTo(event.x, event.y)
                        scaledPath.lineTo(event.x * scale, event.y * scale)
                        tempCanvas.drawPath(path, paint)
                        originalCanvas.drawPath(scaledPath, scaledPaint)

                        tempCanvas.save()
                        tempCanvas.translate(0f, 0f)
                        tempCanvas.restore()

                        originalCanvas.save()
                        originalCanvas.translate(0f, 0f)
                        originalCanvas.restore()

                        //Attach the canvas to the ImageView
                        ivMainImage.setImageDrawable(BitmapDrawable(resources, tempBitmap))
                    }

                    else -> return false
                }

                return true
            }

        })
    }

    fun obtainBitmap(): Bitmap = modifiedBitmap

}