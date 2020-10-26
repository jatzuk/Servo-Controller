package dev.jatzuk.servocontroller.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME
import kotlin.math.*

private const val DEFAULT_ZOOM_TEXT_SIZE = 10f
private const val DEFAULT_LABEL_TEXT_SIZE = 14f
private const val DEFAULT_VALUE_TEXT_SIZE = 24f
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
    private val frameRect = RectF()
    private lateinit var servoHead: Bitmap
    private val servoHeadMatrix = Matrix()

    private var radius = 0f
    private var positionInDegrees = 90
    private val labelPosition = PointF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }
    private val defaultColor = ContextCompat.getColor(context, R.color.colorPrimary)

    private var isAdjusting = false

    var tag = ""
    private var shouldRotateTagToFitSelf = false

    private var labelTextSize = 0f
    private var zoomTextSize = 0f
    private var valueTextSize = 0f
    private var valueOffsetLabel = 0f

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val isAngleGridIsShown = sharedPreferences.getBoolean(
        context.getString(R.string.key_is_angle_grid_should_show),
        true
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setDefaultTextSizes()
    }

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

        val scaleFactor = height / 2
        servoBase = createScaledBitmap(R.drawable.ic_servo_base, scaleFactor / 2, scaleFactor)
        servoHead = createScaledBitmap(R.drawable.ic_servo_head, scaleFactor / 3, scaleFactor)

        setMeasuredDimension(width, height)
    }

    private fun setDefaultTextSizes() {
        labelTextSize = DEFAULT_LABEL_TEXT_SIZE
        zoomTextSize = DEFAULT_ZOOM_TEXT_SIZE
        valueTextSize = DEFAULT_VALUE_TEXT_SIZE
        val density = resources.displayMetrics.density
        labelTextSize *= density
        zoomTextSize *= density
        valueTextSize *= density
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / Math.PI).toFloat()

        labelTextSize = min(labelTextSize, width / 18f)

        servoSetupArea.set(
            width / 2f - servoBase.width / 2,
            height / 2f,
            width / 2f + servoBase.width / 2,
            height.toFloat()
        )
        frameRect.set(0f, 0f, width.toFloat(), height.toFloat())

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
        drawViewFrame(canvas)
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
        if (event.action == MotionEvent.ACTION_DOWN
            && isIntersects && clickTime - lastClickTime > SETUP_DIALOG_CLICK_LISTENER_DELAY
        ) {
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
            val labelRadius = radius + labelTextSize * 2
            val step = 2
            repeat(18 / step + 1) { i ->
                labelPosition.computeXY(i * 10 * step, labelRadius)
                canvas.drawText((i * 10 * step).toString(), labelPosition.x, labelPosition.y, paint)
            }

            val pointsOffset = labelRadius - 15 * resources.displayMetrics.density
            repeat(19) { i ->
                labelPosition.computeXY(i * 10, pointsOffset)
                canvas.drawCircle(labelPosition.x, labelPosition.y, 5f, paint)
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
        paint.textSize = zoomTextSize * 2

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

    private fun drawViewFrame(canvas: Canvas) {
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawRoundRect(frameRect, 0f, 0f, paint)
    }

    private fun createScaledBitmap(
        @DrawableRes resourceId: Int,
        targetWidth: Int,
        targetHeight: Int
    ) = Bitmap.createBitmap(
        targetWidth,
        targetHeight,
        Bitmap.Config.ARGB_8888
    ).run {
        val canvas = Canvas(this)
        val drawable = AppCompatResources.getDrawable(context, resourceId)!!
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        this
    }

    interface OnSetupClickListener {

        fun onSetupAreaClicked()

        fun onFinalPositionDetected(position: Int)
    }
}
