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
private const val RADIUS_OFFSET_INDICATOR = -5
private const val LABEL_TEXT_SIZE = 22f
private const val ZOOM_TEXT_SIZE = LABEL_TEXT_SIZE * 2
private const val SERVO_BASE_OFFSET = 170f
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

    private var radius = 0f
    var positionInDegrees = 0
        private set
    private val pointPosition = PointF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var isZooming = false

    private var setupRectF = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2 * 0.9).toFloat()

        setupRectF.apply {
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

        canvas.drawRect(setupRectF, paint)

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

//        drawPointer(positionInDegrees, canvas)
        drawLabels(canvas)

        if (isZooming) {
            paint.textSize = ZOOM_TEXT_SIZE
            canvas.drawText(positionInDegrees.toString(), width / 2f, height / 6f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        val x = width / 2 - event.x
        val y = height / 2 - event.y


        Log.d(TAG, "onTouchEvent: ${event.x} ${event.y}")
        val isInside =
            setupRectF.intersects(event.x - 10f, event.y - 10f, event.x + 10, event.y + 10f)
        Log.d(TAG, "onTouchEvent: isInside: $isInside")

        if (isInside) {
            (context as ServoViewSettingsOnClickListener).onClick()
            return true
        }

        positionInDegrees = getPositionInDegrees(y, x)

        if (positionInDegrees !in 0..180) return false

        return when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                isZooming = true
                performClick()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "final position: $positionInDegrees")
                isZooming = false
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

    private fun drawPointer(pos: Int, canvas: Canvas) {
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXY(pos, markerRadius)
        paint.color = Color.BLUE
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius / 24, paint)
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

    interface ServoViewSettingsOnClickListener {

        fun onClick()
    }
}
