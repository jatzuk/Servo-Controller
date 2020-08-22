package dev.jatzuk.servocontroller.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME
import kotlin.math.*

private const val SETUP_DIALOG_CLICK_LISTENER_DELAY = 1000L
private const val TAG = "ServoView"

class ServoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    lateinit var onSetupClickListener: OnSetupClickListener
    private var lastClickTime = 0L

    private lateinit var servoBase: Bitmap
    private var servoSetupArea = RectF()
    private lateinit var servoHead: Bitmap
    private val servoHeadMatrix = Matrix()

    private var radius = 0f
    var positionInDegrees = 90
        private set
    private val labelPosition = PointF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }
    private val defaultColor = ContextCompat.getColor(context, R.color.colorPrimary)

    private var isAdjusting = false

    var tag = "test tag!!"
    private var shouldRotateTagToFitSelf = false

    private var radiusOffsetLabel = 30

    private var labelTextSize = 5f

    private var zoomTextSize = 8f
    private var valueTextSize = 12f
    private var valueOffsetLabel = 0f

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val servosCount =
        sharedPreferences.getInt(context.getString(R.string.key_servos_count), 1)

    private val isAngleGridIsShown = sharedPreferences.getBoolean(
        context.getString(R.string.key_is_angle_grid_should_show),
        false
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val windowWidth = context.resources.displayMetrics.widthPixels
        val windowHeight = context.resources.displayMetrics.heightPixels

        val desiredHeight =
            if (windowHeight > windowWidth) (windowHeight * 0.4).toInt()
            else (windowHeight * 0.6).toInt()


        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(windowWidth, widthSize)
            else -> windowWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        val density = resources.displayMetrics.density
        labelTextSize *= density
        zoomTextSize *= density
        valueTextSize *= density

        var bitmapSizeFactor = if (servosCount < 3) 2 else 4

        // if tablet or smth
        if (windowWidth / 3 > width || windowHeight / 3 > height) {
            bitmapSizeFactor = 1
        }
        val servoBaseSize = height / 5 / bitmapSizeFactor
        val servoHeadSize = height / 10 / bitmapSizeFactor

        servoBase = createScaledBitmap(R.drawable.servo_base, servoBaseSize, servoBaseSize)
        servoHead = createScaledBitmap(R.drawable.servo_head, servoHeadSize, servoHeadSize)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2 * 0.8).toFloat()

        servoSetupArea.apply {
            left = width / 2f - servoBase.width / 2
            top = height / 2f
            right = width / 2f + servoBase.width / 2
            bottom = height.toFloat()
        }

        paint.textSize = zoomTextSize
        shouldRotateTagToFitSelf = paint.measureText(tag) > width / 2 - servoBase.width / 2

        paint.textSize = valueTextSize
        valueOffsetLabel = paint.measureText("00")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawLabels(canvas)

        drawServoBase(canvas)
        drawServoHead(canvas)
        drawValue(canvas)
        drawTag(canvas)

//        canvas.drawRect(servoSetupArea, paint)
    }

    private fun drawServoBase(canvas: Canvas) {
        canvas.drawBitmap(
            servoBase,
            width / 2f - servoBase.width / 2,
            height / 2f,
            paint
        )
    }

    private fun drawServoHead(canvas: Canvas) {
        servoHeadMatrix.apply {
            reset()
            postRotate(
                positionInDegrees - 90f,
                servoHead.height / 6f,
                servoHead.height * 0.85f
            )
            postTranslate(
                width / 2f - servoHead.width / 2,
                height / 2f - servoBase.height / 2
            )
        }
        canvas.drawBitmap(servoHead, servoHeadMatrix, paint)
    }

    private fun drawValue(canvas: Canvas) {
        paint.color = defaultColor
        if (isAdjusting) {
            paint.textSize = valueTextSize
            canvas.drawText(
                positionInDegrees.toString(),
                width - valueOffsetLabel,
                valueOffsetLabel,
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        val x = width / 2 - event.x
        val y = height / 2 - event.y

        val clickTime = System.currentTimeMillis()
        val isIntersects = servoSetupArea.intersects(event.x, event.y, event.x, event.y)
        if (event.action == MotionEvent.ACTION_DOWN && isIntersects && clickTime - lastClickTime > SETUP_DIALOG_CLICK_LISTENER_DELAY) {
            lastClickTime = clickTime
            onSetupClickListener.onSetupAreaClicked()
            return true
        }

        val position = getPositionInDegrees(y, x)
        if (position !in 0..180) return false

        positionInDegrees = position

        return when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                isAdjusting = true
                performClick()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onSetupClickListener.onFinalPositionDetected(position)
                isAdjusting = false
                performClick()
            }
            else -> false
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        invalidate()
        return true
    }

    private fun getPositionInDegrees(y: Float, x: Float): Int =
        ((180 / PI) * atan2(y, x)).roundToInt()

    private fun drawLabels(canvas: Canvas) {
        if (isAngleGridIsShown) {
            paint.apply {
                textSize = labelTextSize
                color = Color.BLACK
            }
            val labelRadius = radius + radiusOffsetLabel
            repeat(19) { i ->
                labelPosition.computeXY(i * 10, labelRadius)
                canvas.drawText((i * 10).toString(), labelPosition.x, labelPosition.y, paint)
            }
        }
    }

    private fun PointF.computeXY(radians: Int, radius: Float) {
        val startAngle = PI * (9 / 8)
        val angle = startAngle + radians * (PI / 180)
        x = (radius * cos(angle)).toFloat() + width / 2f
        y = (radius * sin(angle)).toFloat() +
                servoHead.height * 0.85f + height / 2f - servoBase.height / 2
    }

    private fun drawTag(canvas: Canvas) {
        paint.textSize = zoomTextSize

        if (shouldRotateTagToFitSelf) {
            canvas.run {
                save()
                rotate(-90f, width / 2f, height / 2f)
                drawText(tag, paint.measureText(tag[0].toString()), (height * 0.23f), paint)
                restore()
            }
        } else {
            canvas.drawText(tag, paint.measureText(tag) / 2f + 8f, height - 8f, paint)
        }
    }

    private fun createScaledBitmap(
        @DrawableRes resourceId: Int,
        targetWidth: Int,
        targetHeight: Int
    ) = BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, resourceId, this)
        inSampleSize = calculateInSampleSize(this, targetWidth, targetHeight)
        inJustDecodeBounds = false
        BitmapFactory.decodeResource(resources, resourceId, this)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        val (w, h) = options.run { outWidth to outHeight }
        var inSampleSize = 1

        if (w > targetWidth || h > targetHeight) {
            val halfW = w / 2
            val halfH = h / 2

            while (halfW / inSampleSize >= targetWidth && halfH / inSampleSize >= targetHeight) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    interface OnSetupClickListener {

        fun onSetupAreaClicked()

        fun onFinalPositionDetected(position: Int)
    }
}
