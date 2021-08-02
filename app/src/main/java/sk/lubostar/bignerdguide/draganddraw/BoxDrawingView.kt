package sk.lubostar.bignerdguide.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class BoxDrawingView(context: Context, attrs: AttributeSet? = null): View(context, attrs) {
    companion object {
        private const val TAG = "BoxDrawingView"
        private const val KEY_SUPER_STATE = "super_state"
        private const val KEY_COUNT = "key_count"
    }

    private var currentBox: Box? = null
    private val boxen = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000.toInt()
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        var action = ""
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                action = "ACTION_DOWN"
                currentBox = Box(current).also {
                    boxen.add(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                action = "ACTION_MOVE"
                updateCurrentBox(current)
            }
            MotionEvent.ACTION_UP -> {
                action = "ACTION_UP"
                updateCurrentBox(current)
                currentBox = null
            }
            MotionEvent.ACTION_CANCEL -> {
                action = "ACTION_CANCEL"
                currentBox = null
            }
        }

        Log.i(TAG, "$action at x=${current.x}, y=${current.y}")

        return true
    }

    private fun updateCurrentBox(current: PointF) {
        currentBox?.let {
            it.end = current
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)

        boxen.forEach { box ->
            canvas.drawRect(box.left, box.top, box.right, box.bottom, boxPaint)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val parcelable: Parcelable? = super.onSaveInstanceState()
        with(Bundle()) {
            putParcelable(KEY_SUPER_STATE, parcelable)

            var index = 0;
            putInt(KEY_COUNT, boxen.size)
            boxen.forEach {
                putFloat("${index}_L", it.left)
                putFloat("${index}_T", it.top)
                putFloat("${index}_R", it.right)
                putFloat("${index}_B", it.bottom)
                index++
            }

            return this
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null && state is Bundle) {
            val size = state.getInt(KEY_COUNT) - 1
            for (index in 0..size) {
                boxen.add(Box(PointF(
                    state.getFloat("${index}_L"),
                    state.getFloat("${index}_T")
                )).apply {
                    end = PointF(
                        state.getFloat("${index}_R"),
                        state.getFloat("${index}_B")
                    )
                })
            }

            val superState: Parcelable? = state.getParcelable(KEY_SUPER_STATE)
            super.onRestoreInstanceState(superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
