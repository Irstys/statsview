package ru.netology.statsview.UI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import android.view.View
import ru.netology.nmedia.Util.AndroidUtils
import ru.netology.statsview.R
import ru.netology.statsview.R.color.empty_color
import kotlin.math.min
import kotlin.random.Random

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

        init {
            context.withStyledAttributes(attrs, R.styleable.StatsView) {
                lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
                fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
                val resId = getResourceId(R.styleable.StatsView_colors, 0)
                colors = resources.getIntArray(resId).toList()
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
                invalidate()
            }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            radius = min(w, h) / 2F - lineWidth / 2
            center = PointF(w / 2F, h / 2F)
            oval = RectF(
                center.x - radius, center.y - radius,
                center.x + radius, center.y + radius,
            )
        }
        private val indexDate=data.maxOrNull()!!.times(data.count())
        override fun onDraw(canvas: Canvas) {
            if (data.isEmpty()) {
                return
            }

            var startFrom = -90F
            canvas.drawArc(oval, startFrom, fullCircleDegrees, false, paintEmpty)
            /*  for ((index, datum) in data.withIndex()) {
                val angle = 360F * datum
             }*/
            data.forEachIndexed { index, datum ->
                val angle = (datum / indexDate) * fullCircleDegrees
                paint.color = colors.getOrElse(index) { randomColor() }
                canvas.drawArc(oval, startFrom, angle, false, paint)
                startFrom += angle
            }


            val text = (data.sum() / indexDate) * 100F
            canvas.drawText(
                "%.2f%%".format(text),
                center.x,
                center.y + textPaint.textSize / 4,
                textPaint,
            )
            if (text == 100F) {
                paint.color = colors[0]
                canvas.drawArc(oval, startFrom, 1F, false, paint)
            }
        }

        private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
    }