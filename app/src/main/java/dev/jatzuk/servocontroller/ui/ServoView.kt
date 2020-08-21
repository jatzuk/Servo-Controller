package dev.jatzuk.servocontroller.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.utils.SettingsHolder
import kotlin.math.*

private const val RADIUS_OFFSET_LABEL = 25
private const val LABEL_TEXT_SIZE = 22f
private const val ZOOM_TEXT_SIZE = LABEL_TEXT_SIZE * 2
private const val SETUP_DIALOG_CLICK_LISTENER_DELAY = 1000L
private const val TAG = "ServoView"

class ServoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // TODO: 15/08/2020 fix better servo images
    private var servoBase = BitmapFactory.decodeResource(resources, R.drawable.servo_base)
    private val servoPointerMatrix = Matrix()
    private var servoPointer = BitmapFactory.decodeResource(resources, R.drawable.servo_pointer)
    private var servoSetupRectF = RectF()
    private var lastClickTime = 0L
    private var servoBaseOffset = 170f
    private val servoPointerOffset = 50f

    private var radius = 0f
    var positionInDegrees = 90
        private set
    private val labelPosition = PointF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = ZOOM_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var isAdjusting = false

    lateinit var onSetupClickListener: OnSetupClickListener

    var settingsHolder: SettingsHolder = SettingsHolder(context)
    private var desiredWidth = 0
    private var desiredHeight = 0
    var windowWidth = 0
    var windowHeight = 0

    var tag = "test tag!!"
    private var shouldRotateTagToFitSelf = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val servosSize = settingsHolder.servosCount
        windowWidth = context.resources.displayMetrics.widthPixels
        windowHeight = context.resources.displayMetrics.heightPixels

        desiredWidth = windowWidth

        desiredHeight =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                windowHeight / 3
            } else {
                windowHeight / 2
            }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = 220f

        servoSetupRectF.apply {
            left = (width / 2f) - servoBase.width / 2
            top = 250f
            right = (width / 2f) + servoBase.width / 2
            bottom = height.toFloat()
        }

//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//        options.inJustDecodeBounds = false

        val servosSize = settingsHolder.servosCount
        if (servosSize > 2) {
//            servoBase = servoBase.scale(width / 2, (height / (servosSize * 1.5f)).toInt())

//        servoPointer = servoPointer.scale(width / 4, height / 4)
        } else {
//            servoBase = servoBase.scale(servoBase.width, 300)
        }

        shouldRotateTagToFitSelf = paint.measureText(tag) > width / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(
            servoBase,
            (width / 2f) - servoBase.width / 2,
            servoBaseOffset,
            paint
        )

        servoPointerMatrix.apply {
            reset()
            postRotate(
                positionInDegrees.toFloat(),
                servoPointer.width - servoPointerOffset,
                servoPointer.height / 2f - 10f
            )
            postTranslate(
                width / 2f - servoBase.width - 70,
                servoPointer.height / 2 + servoBaseOffset
            )
        }
        canvas.drawBitmap(servoPointer, servoPointerMatrix, paint)

        if (isAdjusting) {
            paint.textSize = ZOOM_TEXT_SIZE
            canvas.drawText(
                positionInDegrees.toString(),
                width - paint.measureText("10"),
                height - 10f,
                paint
            )
        }

        drawLabels(canvas)
        drawTag(canvas)

        canvas.drawRect(servoSetupRectF, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        val x = width / 2 - event.x
        val y = height / 2 - event.y

        val clickTime = System.currentTimeMillis()
        val isIntersects = servoSetupRectF.intersects(event.x, event.y, event.x, event.y)
        if (isIntersects && clickTime - lastClickTime > SETUP_DIALOG_CLICK_LISTENER_DELAY) {
            lastClickTime = clickTime
            onSetupClickListener.onClick()
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
                Log.d(TAG, "final position: $positionInDegrees")
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

    private fun PointF.computeXY(radians: Int, radius: Float) {
        val startAngle = PI * (9 / 8)
        val angle = startAngle + radians * (PI / 180)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + servoBase.height / 2 + 20f
    }

    private fun getPositionInDegrees(y: Float, x: Float): Int =
        ((180 / PI) * atan2(y, x)).roundToInt()

    private fun drawLabels(canvas: Canvas) {
//        canvas.save()
//        canvas.rotate(
//            positionInDegrees.toFloat() - 90,
//            width / 2f,
//            height / 2f
//        )

        paint.apply {
            textSize = LABEL_TEXT_SIZE
            color = Color.BLACK
        }
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        repeat(19) { i ->
            labelPosition.computeXY(i * 10, labelRadius)
            canvas.drawText((i * 10).toString(), labelPosition.x, labelPosition.y, paint)
        }

//        canvas.restore()
    }

    private fun drawTag(canvas: Canvas) {
        paint.apply {
            textSize = ZOOM_TEXT_SIZE
            color = Color.BLACK
        }

        if (shouldRotateTagToFitSelf) {
            canvas.run {
                save()
                rotate(-90f, width / 2f, height / 2f)
                drawText(tag, paint.measureText(tag) / 2, paint.measureText(tag) / 2, paint)
                restore()
            }
        } else {
            canvas.drawText(tag, paint.measureText(tag) / 2f + 8f, height - 8f, paint)
        }
    }

    interface OnSetupClickListener {

        fun onClick()
    }
}
