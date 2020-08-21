package dev.jatzuk.servocontroller.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
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

    private lateinit var servoBase: Bitmap

    //            = BitmapFactory.decodeResource(resources, R.drawable.servo_base)
    private val servoPointerMatrix = Matrix()
    private lateinit var servoHead: Bitmap

    //            = BitmapFactory.decodeResource(resources, R.drawable.servo_head)
    private var servoSetupRectF = RectF()
    private var lastClickTime = 0L
    private var servoBaseOffset = 170f
    private val servoPointerOffset = 50f
    private var randomOffset = 0f

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
        val multiplier = 0f
        desiredHeight = (windowHeight * 0.4).toInt()

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

        servoBase = createScaledBitmap(R.drawable.servo_base, 100, 100)
        servoHead = createScaledBitmap(R.drawable.servo_head, 50, 100)

        servoSetupRectF.apply {
            left = (width / 2f) - servoBase.width / 2
            top = servoBase.height / 2f
            right = (width / 2f) + servoBase.width / 2
            bottom = height.toFloat()
        }

        val servosSize = settingsHolder.servosCount
        if (servosSize > 2) {
//            servoBase = servoBase.scale(width / 2, (height / (servosSize * 1.5f)).toInt())

//        servoPointer = servoHead.scale(width / 4, height / 4)
        } else {
//            servoBase = servoBase.scale(servoBase.width, 300)
        }

        shouldRotateTagToFitSelf = paint.measureText(tag) > width / 2 - servoBase.width / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(
            servoBase,
            width / 2f - servoBase.width / 2,
            height / 2f,
            paint
        )

        servoPointerMatrix.apply {
            reset()
            postRotate(
                positionInDegrees - 90f,
                servoHead.height / 7f,
                servoHead.height.toFloat() * 0.83f //290f//height / 2f
            )
            postTranslate(
                width / 2f - servoHead.width / 2,
                height / 2f - servoBase.height / 2 //servoHead.height / 6f
            )
        }
        canvas.drawBitmap(servoHead, servoPointerMatrix, paint)

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

        fun onClick()
    }
}
