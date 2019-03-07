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
import android.util.TypedValue
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.applyCanvas
import java.io.File
import java.io.FileOutputStream

class ImageEditor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val EVENT_DISTANCE = 0
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

    var imageInitialized = false

    private fun init() {
        View.inflate(context, R.layout.image_editor, this)

        val options = BitmapFactory.Options()
        options.inMutable = true

        //Create a new image bitmap and attach a brand new canvas to it
//        var tempBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.ic_launcher_background))
        var tempBitmap = BitmapFactory.decodeStream(context.assets.open("drone.jpg"), null, options)

        val paint = PaintBuilder()
            .setColor(colorThree)
            .setStrokeWidth(15f)
            .build()
        paint.style = Paint.Style.STROKE;

        ivMainImage.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    ivMainImage.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    if(!imageInitialized){
                        val aspectRatioBitmap = tempBitmap.width / tempBitmap.height
                        val aspectRatioImageView = ivMainImage.width / ivMainImage.height

                        val newBitmapWidth: Int
                        val newBitmapHeight: Int
                        if(aspectRatioBitmap > aspectRatioImageView){
                            newBitmapWidth = ivMainImage.width
                            newBitmapHeight = ivMainImage.width / aspectRatioBitmap
                        }else{
                            newBitmapHeight = ivMainImage.height
                            newBitmapWidth = ivMainImage.height * aspectRatioBitmap
                        }

                        tempBitmap = Bitmap.createScaledBitmap(tempBitmap, newBitmapWidth, newBitmapHeight, false)

                        imageInitialized = true
                    }


                    val tempCanvas = Canvas(tempBitmap)

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

                    initializeMotionCapture(tempCanvas, tempBitmap, paint)

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

    private fun convertToPixels(dip: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            resources.displayMetrics
        )
    }

    var path = Path()

    private fun initializeMotionCapture(
        tempCanvas: Canvas,
        tempBitmap: Bitmap?,
        paint: Paint
    ) {
        ivMainImage.setOnTouchListener(object : OnTouchListener {

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                performClick()

                when (event?.action) {

                    MotionEvent.ACTION_DOWN -> {
                        path = Path()
                        path.moveTo(event.x, event.y)
                        println("DOWN ${event.x}, ${event.y}")
                    }

                    MotionEvent.ACTION_MOVE -> {

                        //Draw the image bitmap into the cavas
                        tempCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

                        if (checkMovement(previousCoordinates, event)) {

                            path.lineTo(event.x, event.y)
                            tempCanvas.drawPath(path, paint)
//                            tempCanvas.drawLine(
//                                previousCoordinates?.x ?: event.x,
//                                previousCoordinates?.y ?: event.y,
//                                event.x,
//                                event.y,
//                                paint
//                            )

                        }

                        tempCanvas.save()
                        tempCanvas.translate(0f, 0f)
                        tempCanvas.restore()

                        //Attach the canvas to the ImageView
                        ivMainImage.setImageDrawable(BitmapDrawable(resources, tempBitmap))
                    }

                    MotionEvent.ACTION_UP -> {

                        tempCanvas.drawPath(path, paint)
                        path = Path()

                        val f = File("${context.externalCacheDir}/file.jpg")

                        tempBitmap?.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            FileOutputStream(f)
                        )
                    }

                    else -> println("OTHER")
                }

                println("PREVIOUSEVENT ${previousCoordinates?.x} ${previousCoordinates?.y}  ---   EVENT ${event?.x} ${event?.y}")

                if (previousCoordinates == null || checkMovement(previousCoordinates, event)) {

                    previousCoordinates = PointF(event!!.x, event.y)

                }

                return true
            }

        })
    }

    fun checkMovement(previousEvent: PointF?, event: MotionEvent?): Boolean{

        return if(previousEvent != null && event != null){
            (Math.abs(previousEvent.x - event.x) > EVENT_DISTANCE
                    || Math.abs(previousEvent.y - event.y) > EVENT_DISTANCE)
        }else{
            false
        }

    }

}