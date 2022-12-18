package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import android.view.View
import ru.netology.statsview.util.AndroidUtils
import ru.netology.statsview.R
import ru.netology.statsview.R.color.empty_color
import kotlin.math.min
import kotlin.random.Random

const val TYPE_ROTATION = 0
const val TYPE_SEQUENTIAL = 1
const val TYPE_BIDIRECTIONAL = 2

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var fullCircleDegrees = 360F
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = 0F

    private var startFrom = -90F

    private var animationType = 0
    private var valueAnimator: ValueAnimator? = null

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            animationType = getInteger(R.styleable.StatsView_animationType, 0)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    @SuppressLint("ResourceAsColor")
    private val paintEmpty = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = empty_color
        alpha = 10
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        //invalidate()
        }

    private val indexDate = data.maxOrNull()!!.times(data.count())
    private fun getAngle(datum:Float) = (datum / indexDate) * fullCircleDegrees

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        canvas.drawArc(oval, startFrom, fullCircleDegrees, false, paintEmpty)

        when (animationType) {
            TYPE_ROTATION -> {
                rotate(canvas)
            }
            TYPE_SEQUENTIAL -> {
                sequent(canvas)
            }
            TYPE_BIDIRECTIONAL -> {
                bidirectional(canvas)
            }
        }
        val text = (data.sum() / indexDate) * 100F
        canvas.drawText(
            "%.2f%%".format(text + progress),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
        if (text==100F)
            paint.color = colors[0]
            canvas.drawArc(oval, startFrom, 1F, false, paint)
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 3000
            start()
            invalidate()
            }
    }
    private fun rotate(canvas: Canvas) {

        data.forEachIndexed { index, datum ->
            val angle = getAngle(datum)

            paint.color = colors.getOrElse(index) { randomColor() }
            canvas.drawArc(oval, startFrom, angle * progress, false, paint)
            startFrom += angle
            if (index == 0 && progress == 1F) {
                paint.color = colors[0]
                canvas.drawPoint(center.x ,center.y - radius, paint)
            }
        }
    }

    private fun sequent(canvas: Canvas) {

        var sum = 0F

        for ((index, datum) in data.withIndex()) {
            val angle = getAngle(datum)
            paint.color = colors.getOrElse(index) { randomColor() }

            if(progress > datum + sum){
                canvas.drawArc(oval, startFrom ,angle,false,paint)
            }else if(progress in sum..(datum + sum)){
                canvas.drawArc(oval, startFrom ,360 * (progress - sum),false,paint)
            }
            sum += datum
            startFrom += angle

            if (index == 0 && progress == 1F) {
                paint.color = colors[0]
                canvas.drawPoint(center.x ,center.y - radius, paint)
            }
        }
    }

    private fun bidirectional(canvas: Canvas) {

        startFrom = -45F
        data.forEachIndexed { index, datum ->
            val angle = getAngle(datum)
            paint.color = colors.getOrElse(index) { randomColor() }
            canvas.drawArc(oval,
                startFrom + (angle / 2) - (angle * progress / 2),
                angle * progress,
                false,
                paint)
            startFrom += angle
            if (progress == 1F) {
                paint.color = colors.get(0)
                canvas.drawPoint(center.x,
                    center.y - radius, paint)
            }
        }
    }
}


