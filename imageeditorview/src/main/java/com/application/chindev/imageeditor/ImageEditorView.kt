package com.application.chindev.imageeditor

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.application.chindev.imageeditor.bitmap.BitmapUtils
import com.application.chindev.imageeditor.bitmap.CopyBitmapAsyncTask
import com.application.chindev.imageeditor.paint.PaintBuilder
import kotlinx.android.synthetic.main.image_editor.view.*




class ImageEditorView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val REGEXP_HEX_COLOR = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
        const val DEFAULT_STROKE_WIDTH = 15f
    }

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

    private lateinit var originalBitmap: Bitmap

    private lateinit var modifiedBitmap: Bitmap

    private lateinit var scaledBitmap: Bitmap

    var imageInitialized = false


    constructor(context: Context) : this(context, null){
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init(attrs)
    }

    private fun init(attrs: AttributeSet? = null) {
        View.inflate(context, com.example.test.imageeditorview.R.layout.image_editor, this)

        attrs?.let {
            setUpAttrs(it)
        }
    }

    private fun setUpAttrs(attrs: AttributeSet) {
        val typedArray = getTypedArrayAttrs(attrs)
        this.strokeWidth = getStrokeWidthAttr(typedArray)
        val colorList = getColorListAttr(typedArray)
        colorList?.let {
            it.split(",").forEach {
                addColor(it)
            }
        }

        val source = getSourceAttr(typedArray)

        if(source > 0){
            setBitmap(BitmapFactory.decodeResource(context.resources, source))
        }

    }

    private fun getTypedArrayAttrs(attributeSet: AttributeSet): TypedArray {
        return context.theme.obtainStyledAttributes(attributeSet, com.example.test.imageeditorview.R.styleable.ImageEditorViewAttrs, 0, 0)
    }

    private fun getStrokeWidthAttr(typedArray: TypedArray) =
        typedArray.getFloat(com.example.test.imageeditorview.R.styleable.ImageEditorViewAttrs_strokeWidth, DEFAULT_STROKE_WIDTH)


    private fun getColorListAttr(typedArray: TypedArray) =
        typedArray.getString(com.example.test.imageeditorview.R.styleable.ImageEditorViewAttrs_colorList)


    private fun getSourceAttr(typedArray: TypedArray) =
        typedArray.getResourceId(com.example.test.imageeditorview.R.styleable.ImageEditorViewAttrs_source, -1)


    fun setCurrentColor(color: String){

        if(checkColorFormat(color)){
            val colorInt = Color.parseColor(color)

            paint.color = colorInt
            scaledPaint.color = colorInt
        }

    }

    fun addColor(color: String){

        if(checkColorFormat(color)){

            val colorInt = Color.parseColor(color)

            colorList.add(colorInt)

            val colorView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(com.example.test.imageeditorview.R.layout.color_palette, llColours, false)

            val ivColor = colorView.findViewById<ImageView>(com.example.test.imageeditorview.R.id.ivColor)

            ivColor.setColorFilter(colorInt)

            llColours.addView(colorView)

            colorView.setOnClickListener {
                paint.color = colorInt
                scaledPaint.color = colorInt
                highlightColor(it)
            }

        }

    }

    fun configureColorsPalette(
        gravity: GravityEnum = GravityEnum.TOP_LEFT,
        margin: Int = 0,
        backgroundColor: String = "#FFFFFF",
        orientation: OrientationEnum = OrientationEnum.VERTICAL){

        val constraintSet = ConstraintSet()
        constraintSet.clone(clMain)

        constraintSet.clear(llColours.id, ConstraintSet.TOP)
        constraintSet.clear(llColours.id, ConstraintSet.BOTTOM)
        constraintSet.clear(llColours.id, ConstraintSet.END)
        constraintSet.clear(llColours.id, ConstraintSet.START)

        when(gravity){
            GravityEnum.BOTTOM_LEFT -> {
                constraintSet.connect(llColours.id, ConstraintSet.START, clMain.id, ConstraintSet.START, margin)
                constraintSet.connect(llColours.id, ConstraintSet.BOTTOM, clMain.id, ConstraintSet.BOTTOM, margin)
            }
            GravityEnum.TOP_LEFT -> {
                constraintSet.connect(llColours.id, ConstraintSet.START, clMain.id, ConstraintSet.START, margin)
                constraintSet.connect(llColours.id, ConstraintSet.TOP, clMain.id, ConstraintSet.TOP, margin)
            }
            GravityEnum.BOTTOM_RIGHT -> {
                constraintSet.connect(llColours.id, ConstraintSet.END, clMain.id, ConstraintSet.END, margin)
                constraintSet.connect(llColours.id, ConstraintSet.BOTTOM, clMain.id, ConstraintSet.BOTTOM, margin)
            }
            GravityEnum.TOP_RIGHT -> {
                constraintSet.connect(llColours.id, ConstraintSet.END, clMain.id, ConstraintSet.END, margin)
                constraintSet.connect(llColours.id, ConstraintSet.TOP, clMain.id, ConstraintSet.TOP, margin)
            }
        }

        constraintSet.applyTo(clMain)

        when(orientation){
            OrientationEnum.HORIZONTAL -> llColours.orientation = LinearLayout.HORIZONTAL
            OrientationEnum.VERTICAL -> llColours.orientation = LinearLayout.VERTICAL
        }

        if(checkColorFormat(backgroundColor)){
            val gd = GradientDrawable()
            gd.setColor(Color.parseColor(backgroundColor))
            gd.cornerRadius = 34f
            llColours.setBackgroundDrawable(gd)
        }

    }

    private fun checkColorFormat(color: String): Boolean {

        val rightColor = REGEXP_HEX_COLOR.toRegex().containsMatchIn(color)

        if(!rightColor) {
            println("Wrong color format: $color. Hex color String expected (e.g. #FF11FF")
        }

        return rightColor
    }

    private fun highlightColor(colorView: View) {
        if (llColours.childCount > 0) {
            for (i in 0 until llColours.childCount) {
                val child = llColours.getChildAt(i)
                val ivHighlight = child.findViewById<ImageView>(com.example.test.imageeditorview.R.id.ivHighlight)
                ivHighlight.visibility = View.INVISIBLE
            }
        }
        colorView.findViewById<ImageView>(com.example.test.imageeditorview.R.id.ivHighlight).visibility = View.VISIBLE
    }

    fun setBitmap(originalBitmap: Bitmap){

        this.originalBitmap = originalBitmap

        CopyBitmapAsyncTask(context).execute(originalBitmap).get()?.let {

            modifiedBitmap = it

            ivMainImage.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        ivMainImage.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        setupImage()

                    }
                })
        }

    }

    private fun setupImage() {

        if (!imageInitialized) {

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

    fun erase(originalBitmap: Bitmap){
        imageInitialized = false
        CopyBitmapAsyncTask(context).execute(originalBitmap).get()?.let {
            modifiedBitmap = it
            setupImage()
        }
    }

}