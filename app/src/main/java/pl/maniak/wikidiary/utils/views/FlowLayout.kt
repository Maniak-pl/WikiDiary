package pl.maniak.wikidiary.utils.views


import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import pl.maniak.wikidiary.R


class FlowLayout(context: Context, attrs: AttributeSet?, defStyle: Int) :
    ViewGroup(context, attrs, defStyle) {
    private var mGravity = (if (isIcs) Gravity.START else Gravity.LEFT) or Gravity.TOP
    private val mLines: MutableList<MutableList<View>> = ArrayList<MutableList<View>>()
    private val mLineHeights: MutableList<Int> = ArrayList()
    private val mLineMargins: MutableList<Int> = ArrayList()

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    init {
//        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout, defStyle, 0)
//        try {
//            val index = a.getInt(R.styleable.FlowLayout_android_gravity, -1)
//            if (index > 0) {
//                gravity = index
//            }
//        } finally {
//            a.recycle()
//        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        var width = 0
        var height = paddingTop + paddingBottom
        var lineWidth = 0
        var lineHeight = 0
        val childCount = childCount
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            val lastChild = i == childCount - 1
            if (child.getVisibility() === View.GONE) {
                if (lastChild) {
                    width = Math.max(width, lineWidth)
                    height += lineHeight
                }
                continue
            }
            measureChildWithMargins(child, widthMeasureSpec, lineWidth, heightMeasureSpec, height)
            val lp = child.getLayoutParams() as LayoutParams
            var childWidthMode = MeasureSpec.AT_MOST
            var childWidthSize = sizeWidth
            var childHeightMode = MeasureSpec.AT_MOST
            var childHeightSize = sizeHeight
            if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                childWidthMode = MeasureSpec.EXACTLY
                childWidthSize -= lp.leftMargin + lp.rightMargin
            } else if (lp.width >= 0) {
                childWidthMode = MeasureSpec.EXACTLY
                childWidthSize = lp.width
            }
            if (lp.height >= 0) {
                childHeightMode = MeasureSpec.EXACTLY
                childHeightSize = lp.height
            } else if (modeHeight == MeasureSpec.UNSPECIFIED) {
                childHeightMode = MeasureSpec.UNSPECIFIED
                childHeightSize = 0
            }
            child.measure(
                MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode)
            )
            val childWidth: Int = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin
            if (lineWidth + childWidth > sizeWidth) {
                width = Math.max(width, lineWidth)
                lineWidth = childWidth
                height += lineHeight
                lineHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin
            } else {
                lineWidth += childWidth
                lineHeight =
                    Math.max(lineHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin)
            }
            if (lastChild) {
                width = Math.max(width, lineWidth)
                height += lineHeight
            }
        }
        width += paddingLeft + paddingRight
        setMeasuredDimension(
            if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else width,
            if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else height
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mLines.clear()
        mLineHeights.clear()
        mLineMargins.clear()
        val width = width
        val height = height
        var linesSum = paddingTop
        var lineWidth = 0
        var lineHeight = 0
        var lineViews: MutableList<View> = ArrayList<View>()
        val horizontalGravityFactor: Float
        horizontalGravityFactor =
            when (mGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> 0f
                Gravity.CENTER_HORIZONTAL -> .5f
                Gravity.RIGHT -> 1f
                else -> 0f
            }
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            if (child.getVisibility() === View.GONE) {
                continue
            }
            val lp = child.getLayoutParams() as LayoutParams
            val childWidth: Int = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin
            val childHeight: Int = child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin
            if (lineWidth + childWidth > width) {
                mLineHeights.add(lineHeight)
                mLines.add(lineViews)
                mLineMargins.add(((width - lineWidth) * horizontalGravityFactor).toInt() + paddingLeft)
                linesSum += lineHeight
                lineHeight = 0
                lineWidth = 0
                lineViews = ArrayList<View>()
            }
            lineWidth += childWidth
            lineHeight = Math.max(lineHeight, childHeight)
            lineViews.add(child)
        }
        mLineHeights.add(lineHeight)
        mLines.add(lineViews)
        mLineMargins.add(((width - lineWidth) * horizontalGravityFactor).toInt() + paddingLeft)
        linesSum += lineHeight
        var verticalGravityMargin = 0
        when (mGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> {}
            Gravity.CENTER_VERTICAL -> verticalGravityMargin = (height - linesSum) / 2
            Gravity.BOTTOM -> verticalGravityMargin = height - linesSum
            else -> {}
        }
        val numLines = mLines.size
        var left: Int
        var top = paddingTop
        for (i in 0 until numLines) {
            lineHeight = mLineHeights[i]
            lineViews = mLines[i]
            left = mLineMargins[i]
            val children = lineViews.size
            for (j in 0 until children) {
                val child: View = lineViews[j]
                if (child.getVisibility() === View.GONE) {
                    continue
                }
                val lp = child.getLayoutParams() as LayoutParams

                // if height is match_parent we need to remeasure child to line height
                if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    var childWidthMode = MeasureSpec.AT_MOST
                    var childWidthSize = lineWidth
                    if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                        childWidthMode = MeasureSpec.EXACTLY
                    } else if (lp.width >= 0) {
                        childWidthMode = MeasureSpec.EXACTLY
                        childWidthSize = lp.width
                    }
                    child.measure(
                        MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                        MeasureSpec.makeMeasureSpec(
                            lineHeight - lp.topMargin - lp.bottomMargin,
                            MeasureSpec.EXACTLY
                        )
                    )
                }
                val childWidth: Int = child.getMeasuredWidth()
                val childHeight: Int = child.getMeasuredHeight()
                var gravityMargin = 0
                if (Gravity.isVertical(lp.gravity)) {
                    when (lp.gravity) {
                        Gravity.TOP -> {}
                        Gravity.CENTER_VERTICAL, Gravity.CENTER -> gravityMargin =
                            (lineHeight - childHeight - lp.topMargin - lp.bottomMargin) / 2
                        Gravity.BOTTOM -> gravityMargin =
                            lineHeight - childHeight - lp.topMargin - lp.bottomMargin
                        else -> {}
                    }
                }
                child.layout(
                    left + lp.leftMargin,
                    top + lp.topMargin + gravityMargin + verticalGravityMargin,
                    left + childWidth + lp.leftMargin,
                    top + childHeight + lp.topMargin + gravityMargin + verticalGravityMargin
                )
                left += childWidth + lp.leftMargin + lp.rightMargin
            }
            top += lineHeight
        }
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p)
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    /**
     * {@inheritDoc}
     */
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return super.checkLayoutParams(p) && p is LayoutParams
    }

    var gravity: Int
        get() = mGravity
        set(gravity) {
            var gravity = gravity
            if (mGravity != gravity) {
                if (gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == 0) {
                    gravity = gravity or if (isIcs) Gravity.START else Gravity.LEFT
                }
                if (gravity and Gravity.VERTICAL_GRAVITY_MASK == 0) {
                    gravity = gravity or Gravity.TOP
                }
                mGravity = gravity
                requestLayout()
            }
        }

    class LayoutParams : MarginLayoutParams {
        var gravity = -1

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a: TypedArray = c.obtainStyledAttributes(attrs, R.styleable.FlowLayout_Layout)
            gravity = try {
                a.getInt(R.styleable.FlowLayout_Layout_android_layout_gravity, -1)
            } finally {
                a.recycle()
            }
        }

        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
    }

    companion object {
        /**
         * @return `true` if device is running ICS or grater version of Android.
         */
        private val isIcs: Boolean
            private get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
    }
}