package dev.jatzuk.servocontroller.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.servocontroller.R

class BottomPaddingDecoration(
    context: Context,
    private var padding: Int = 0
) : RecyclerView.ItemDecoration() {

    init {
        if (padding == 0) {
            padding =
                context.resources.getDimensionPixelSize(R.dimen.item_decoration_padding_offset)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(padding, padding, padding, padding)
    }
}
