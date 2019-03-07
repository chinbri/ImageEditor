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
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream

class ImageEditor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val STROKE_WIDTH = 15f
    }

    private val colorOne = context.resources.getColor(R.color.defaultColorOne)
    private val colorTwo = context.resources.getColor(R.color.defaultColorTwo)
    private val colorThree = context.resources.getColor(R.color.defaultColorThree)

    constructor(context: Context) : this(context, null){
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }

    var imageInitialized = false

    private fun init() {
        View.inflate(context, R.layout.image_editor, this)

        val options = BitmapFactory.Options()
        options.inMutable = true

//        var originalBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.ic_launcher_background))
        var originalBitmap = BitmapFactory.decodeStream(context.assets.open("wheel.jpg"), null, options)
        var tempBitmap = originalBitmap
        var scale = 1f

        val paint = PaintBuilder()
            .setColor(colorThree)
            .setStrokeWidth(STROKE_WIDTH)
            .build()
        paint.style = Paint.Style.STROKE

        var scaledPaint: Paint = paint

        ivMainImage.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    ivMainImage.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    if(!imageInitialized){
                        val aspectRatioBitmap = originalBitmap.width.toFloat() / originalBitmap.height
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

                        tempBitmap = Bitmap.createScaledBitmap(originalBitmap, newBitmapWidth, newBitmapHeight, false)
                        scale = originalBitmap.width.toFloat() / newBitmapWidth

                        scaledPaint = PaintBuilder()
                            .setColor(colorThree)
                            .setStrokeWidth(STROKE_WIDTH * scale)
                            .build()
                        scaledPaint.style = Paint.Style.STROKE

                        imageInitialized = true
                    }


                    val tempCanvas = Canvas(tempBitmap)
                    val originalCanvas = Canvas(originalBitmap)

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

                    initializeMotionCapture(originalCanvas, originalBitmap, tempCanvas, tempBitmap, paint, scaledPaint, scale)

                }
            })
//        initializeMotionCapture(tempCanvas, tempBitmap, paint)

        ivColorOne.setOnClickListener {

        }

        ivColorTwo.setOnClickListener {

        }

        ivColorThree.setOnClickListener {

        }

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

    var path = Path()
    var scaledPath = Path()

    private fun initializeMotionCapture(
        originalCanvas: Canvas,
        originalBitmap: Bitmap?,
        tempCanvas: Canvas,
        tempBitmap: Bitmap?,
        paint: Paint,
        scaledPaint: Paint,
        scale: Float
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

                        println("DOWN ${event.x}, ${event.y}")
                    }

                    MotionEvent.ACTION_MOVE -> {

                        //Draw the image bitmap into the cavas
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

                    MotionEvent.ACTION_UP -> {

                        tempCanvas.drawPath(path, paint)
                        originalCanvas.drawPath(scaledPath, scaledPaint)

                        path = Path()
                        scaledPath = Path()

                        val f = File("${context.externalCacheDir}/file.jpg")

                        originalBitmap?.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            FileOutputStream(f)
                        )
                    }

                    else -> return false
                }

                return true
            }

        })
    }

}