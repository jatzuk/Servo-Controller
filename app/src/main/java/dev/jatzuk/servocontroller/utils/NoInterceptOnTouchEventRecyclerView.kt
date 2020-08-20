package dev.jatzuk.servocontroller.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NoInterceptOnTouchEventRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onInterceptTouchEvent(e: MotionEvent?) = false
}
