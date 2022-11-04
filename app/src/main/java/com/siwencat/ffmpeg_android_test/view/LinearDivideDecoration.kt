package com.siwencat.ffmpeg_android_test.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * 当drawableRes不为null时，itemView会按照drawableRes的样式去绘制分割线，
 * drawableLeftMargin和drawableRightMargin是设置drawableRes的margin；
 * left，top，right，bottom是给itemView设置margin
 * TODO：还有一种优化方式：写两个Decoration类，一个用于绘制分割线；
 * 一个用于设置itemView的margin，其中第一个itemView的Top和最后一个itemView的bottom可以单独设置。
 */
class LinearDivideDecoration(
    private val context: Context,
    orientation: Int,
    @DrawableRes private var drawableRes: Int? = null,
    drawableLeftMargin: Float = 0f,
    drawableRightMargin: Float = 0f,
    private val left: Float = 0f,
    private val top: Float = 0f,
    private val right: Float = 0f,
    private val bottom: Float = 0f,
) :
    RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null

    //宽度margin
    private var drawableLeftMargin: Float

    private var drawableRightMargin: Float

    private var mOrientation = orientation

    private val mBounds: Rect = Rect()


    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    init {
        if (drawableRes != null) {
            mDivider = context.resources.getDrawable(drawableRes!!, null)
        }
        this.drawableLeftMargin = dpToPx(drawableLeftMargin)
        this.drawableRightMargin = dpToPx(drawableRightMargin)
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        if (mDivider == null) {
            return
        }
        canvas.save()
        //设置分割线水平间距
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = (parent.paddingLeft + drawableLeftMargin).toInt()
            right = (parent.width - drawableRightMargin).toInt()
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = drawableLeftMargin.toInt()
            right = (parent.width - drawableRightMargin).toInt()
        }
        val childCount = parent.childCount
        //默认为childCount，这里设置为childCount-1，这样最后一行末尾处就没有分割线
        for (i in 0 until childCount - 1) {
            val child: View = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = (mBounds.bottom + child.translationY.roundToInt())
            val top = bottom - mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        if (mDivider == null) {
            return
        }
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(
                parent.paddingLeft, top,
                parent.width - parent.paddingRight, bottom
            )
        } else {
            top = 0
            bottom = parent.height
        }
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            val right = (mBounds.right + child.translationX.roundToInt())
            val left = right - mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        if (mOrientation == VERTICAL) {
            if (mDivider == null) {
                outRect.set(
                    dpToPx(left).toInt(),
                    dpToPx(top).toInt(),
                    dpToPx(right).toInt(),
                    dpToPx(bottom).toInt()
                )
            } else {
                outRect[dpToPx(left).toInt(), dpToPx(top).toInt(), dpToPx(right).toInt()] =
                    mDivider!!.intrinsicHeight
            }
        } else {
            if (mDivider == null) {
                outRect.set(
                    dpToPx(left).toInt(),
                    dpToPx(top).toInt(),
                    dpToPx(right).toInt(),
                    dpToPx(bottom).toInt()
                )
            } else {
                outRect[dpToPx(left).toInt(), dpToPx(top).toInt(), mDivider!!.intrinsicWidth] =
                    dpToPx(bottom).toInt()
            }
        }
    }


}