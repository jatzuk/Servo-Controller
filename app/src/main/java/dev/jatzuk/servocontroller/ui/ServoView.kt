package dev.jatzuk.servocontroller.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dev.jatzuk.servocontroller.R
import kotlin.math.*

private const val RADIUS_OFFSET_LABEL = 25
private const val LABEL_TEXT_SIZE = 22f
private const val ZOOM_TEXT_SIZE = LABEL_TEXT_SIZE * 2
private const val SERVO_BASE_OFFSET = 170f
private const val SETUP_DIALOG_CLICK_LISTENER_DELAY = 1000L
private const val TAG = "ServoView"

class ServoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // TODO: 15/08/2020 fix better servo images
    private val servoBase = BitmapFactory.decodeResource(resources, R.drawable.servo_base)
    private val servoPointerMatrix = Matrix()
    private val servoPointer = BitmapFactory.decodeResource(resources, R.drawable.servo_pointer)
    private var servoSetupRectF = RectF()
    private var lastClickTime = 0L

    private var radius = 0f
    var positionInDegrees = 0
        private set
    private val pointPosition = PointF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var isAdjusting = false

    lateinit var onSetupClickListener: OnSetupClickListener

    private var tag = "Test Tag"

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2 * 0.9).toFloat()

        servoSetupRectF.apply {
            left = (width / 2f) - servoBase.width / 2
            top = 300f
            right = (width / 2f) + servoBase.width / 2
            bottom = height.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(
            servoBase,
            (width / 2f) - servoBase.width / 2,
            SERVO_BASE_OFFSET,
            paint
        )

        servoPointerMatrix.apply {
            reset()
            postRotate(
                positionInDegrees.toFloat(),
                servoPointer.width - 50f,
                servoPointer.height / 2f - 10f
            )
            postTranslate(
                width / 2f - servoBase.width - 70,
                servoPointer.height / 2 + SERVO_BASE_OFFSET
            )
        }
        canvas.drawBitmap(
            servoPointer,
            servoPointerMatrix,
            null
        )

        drawLabels(canvas)

        if (isAdjusting) {
            paint.textSize = ZOOM_TEXT_SIZE
            canvas.drawText(positionInDegrees.toString(), width / 2f, height / 6f, paint)
        }

        drawTag(canvas)
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

        positionInDegrees = getPositionInDegrees(y, x)
        if (positionInDegrees !in 0..180) return false

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
        paint.apply {
            textSize = LABEL_TEXT_SIZE
            color = Color.BLACK
        }
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        repeat(19) { i ->
            pointPosition.computeXY(i * 10, labelRadius)
            canvas.drawText((i * 10).toString(), pointPosition.x, pointPosition.y, paint)
        }
    }

    private fun drawTag(canvas: Canvas) {
        paint.apply {
            textSize = LABEL_TEXT_SIZE * 2
            color = Color.BLACK
            canvas.drawText(tag, 100f, height - LABEL_TEXT_SIZE, this)
        }
    }

    interface OnSetupClickListener {

        fun onClick()
    }
}
