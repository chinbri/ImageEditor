package com.application.chindev.imageeditor

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.graphics.drawable.BitmapDrawable
import android.view.ViewTreeObserver
import com.application.chindev.imageeditor.paint.PaintBuilder
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.ImageView
import com.example.test.imageeditorview.R
import kotlinx.android.synthetic.main.image_editor.view.*
import java.io.RandomAccessFile
import java.nio.channels.FileChannel


class ImageEditorView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var path = Path()
    private var scaledPath = Path()
    private var scale = 1f

    var strokeWidth = 15f
        set(value) {
            paint.strokeWidth = value
            scaledPaint.strokeWidth = value
            field = value
        }

    private var paint: Paint = PaintBuilder()
        .setColor(resources.getColor(android.R.color.transparent))
        .setStrokeWidth(strokeWidth)
        .setStyle(Paint.Style.STROKE)
        .build()

    private var scaledPaint: Paint = PaintBuilder()
        .setColor(resources.getColor(android.R.color.transparent))
        .setStrokeWidth(strokeWidth)
        .setStyle(Paint.Style.STROKE)
        .build()

    private val colorList: MutableList<Int> = mutableListOf()

    private lateinit var modifiedBitmap: Bitmap

    var imageInitialized = false


    constructor(context: Context) : this(context, null){
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init(attrs)
    }

    private fun init(attrs: AttributeSet? = null) {
        View.inflate(context, R.layout.image_editor, this)

        attrs?.let {
            setUpAttrs(it)
        }
    }

    private fun setUpAttrs(attrs: AttributeSet) {
        val typedArray = getTypedArrayAttrs(attrs)
        this.strokeWidth = getStrokeWidthAttr(typedArray)
        val colorList = getColorListAttr(typedArray)
        colorList?.let {
            val colorSplit = it.split(",")

            for(color in colorSplit){
                if("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex().containsMatchIn(color)){
                    addColor(Color.parseColor(color))
                }
            }
        }
    }

    private fun getTypedArrayAttrs(attributeSet: AttributeSet): TypedArray {
        return context.theme.obtainStyledAttributes(attributeSet, R.styleable.ImageEditorViewAttrs, 0, 0)
    }

    private fun getStrokeWidthAttr(typedArray: TypedArray): Float {
        return typedArray.getFloat(R.styleable.ImageEditorViewAttrs_strokeWidth, 15f)
    }

    private fun getColorListAttr(typedArray: TypedArray): String? {
        return typedArray.getString(R.styleable.ImageEditorViewAttrs_colorList)
    }

    fun addColor(color: Int){

        colorList.add(color)

        val colorView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.color_palette, null)
        val ivColor = colorView.findViewById<ImageView>(R.id.ivColor)

        ivColor.setColorFilter(color)

        llColours.addView(colorView)

        colorView.setOnClickListener {
            paint.color = color
            scaledPaint.color = color
            highlightColor(it)
        }

    }


    private fun highlightColor(colorView: View) {
        if (llColours.childCount > 0) {
            for (i in 0 until llColours.childCount) {
                val child = llColours.getChildAt(i)
                val ivHighlight = child.findViewById<ImageView>(R.id.ivHighlight)
                ivHighlight.visibility = View.INVISIBLE
            }
        }
        colorView.findViewById<ImageView>(R.id.ivHighlight).visibility = View.VISIBLE
    }

    fun setup(originalBitmap: Bitmap){

//        modifiedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true)

        modifiedBitmap = copyBitmapEfficiently(originalBitmap)

        var tempBitmap = originalBitmap

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

                        scaledPaint.strokeWidth = strokeWidth * scale

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

    private fun copyBitmapEfficiently(originalBitmap: Bitmap): Bitmap{

        val file = "${context.externalCacheDir}/tempFile"

        val randomAccessFile = RandomAccessFile(file, "rw")

        val channel = randomAccessFile.getChannel()
        val map = channel.map(FileChannel.MapMode.READ_WRITE, 0, originalBitmap.width.toLong() * originalBitmap.height * 4)
        originalBitmap.copyPixelsToBuffer(map)
        originalBitmap.recycle()

        val mutableBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, originalBitmap.config)
        map.position(0)
        mutableBitmap.copyPixelsFromBuffer(map)

        channel.close()
        randomAccessFile.close()

        return mutableBitmap
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