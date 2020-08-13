package dev.jatzuk.servocontroller.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

private const val RADIUS_OFFSET_LABEL = 25
private const val RADIUS_OFFSET_INDICATOR = -5
private const val LABEL_TEXT_SIZE = 22f
private const val ZOOM_TEXT_SIZE = LABEL_TEXT_SIZE * 2
private const val TAG = "CustomView"

class SwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0f
    var positionInDegrees = 0
        private set
    private val pointPosition = PointF(0f, 0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }

//    private var bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
//    private lateinit var shader: Shader
//    private val shaderPaint = Paint()
//    private val shaderMatrix = Matrix()
//    private val magnifierSize = 50f
//    private var zoomPoint = PointF(0f, 0f)

    private var isZooming = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2 * 0.8).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.GREEN
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        drawPointer(positionInDegrees, canvas)
        drawLabels(canvas)

//        if (!isZooming) {
//            buildDrawingCache()
//        } else {
//            shader = BitmapShader(drawingCache, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//            shaderPaint.shader = shader
//            shaderMatrix.reset()
//            shaderMatrix.postScale(2f, 2f, zoomPoint.x, zoomPoint.y)
//            shaderPaint.shader.setLocalMatrix(shaderMatrix)
//            canvas.drawCircle(width / 2f, height / 8f, magnifierSize, shaderPaint)
//        }

        if (isZooming) {
            paint.textSize = ZOOM_TEXT_SIZE
            canvas.drawText(positionInDegrees.toString(), width / 2f, height / 8f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        val x = width / 2 - event.x
        val y = height / 2 - event.y

        positionInDegrees = getPositionInDegrees(y, x)
//        zoomPoint.computeXY(positionInDegrees, radius + 10)

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
        y = (radius * sin(angle)).toFloat() + height / 2
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
}
