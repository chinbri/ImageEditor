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
import com.application.chindev.imageeditor.bitmap.BitmapUtils
import com.application.chindev.imageeditor.bitmap.CopyBitmapAsyncTask
import com.example.test.imageeditorview.R
import kotlinx.android.synthetic.main.image_editor.view.*

class ImageEditorView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    val REGEXP_HEX_COLOR = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
    val DEFAULT_STROKE_WIDTH = 15f

    private var path = Path()
    private var scaledPath = Path()
    private var scale = 1f

    var strokeWidth = DEFAULT_STROKE_WIDTH
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
                if(REGEXP_HEX_COLOR.toRegex().containsMatchIn(color)){
                    addColor(Color.parseColor(color))
                }
            }
        }
    }

    private fun getTypedArrayAttrs(attributeSet: AttributeSet): TypedArray {
        return context.theme.obtainStyledAttributes(attributeSet, R.styleable.ImageEditorViewAttrs, 0, 0)
    }

    private fun getStrokeWidthAttr(typedArray: TypedArray): Float {
        return typedArray.getFloat(R.styleable.ImageEditorViewAttrs_strokeWidth, DEFAULT_STROKE_WIDTH)
    }

    private fun getColorListAttr(typedArray: TypedArray): String? {
        return typedArray.getString(R.styleable.ImageEditorViewAttrs_colorList)
    }

    fun addColor(color: Int){

        colorList.add(color)

        val colorView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.color_palette, llColours, false)
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

        CopyBitmapAsyncTask(context).execute(originalBitmap).get()?.let {

            modifiedBitmap = it

            var scaledBitmap = originalBitmap

            ivMainImage.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        ivMainImage.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        if(!imageInitialized){

                            scaledBitmap = BitmapUtils.scaleBitmapToView(modifiedBitmap, ivMainImage)

                            scale = modifiedBitmap.width.toFloat() / scaledBitmap.width

                            scaledPaint.strokeWidth = strokeWidth * scale

                            imageInitialized = true
                        }

                        val tempCanvas = Canvas(scaledBitmap)
                        val originalCanvas = Canvas(modifiedBitmap)

                        val bitmapWidth = scaledBitmap.width
                        val bitmapHeight = scaledBitmap.height

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
                        ivMainImage.setImageBitmap(scaledBitmap)

                        initializeMotionCapture(originalCanvas, tempCanvas, scaledBitmap, paint, scaledPaint)

                    }
                })
        }

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