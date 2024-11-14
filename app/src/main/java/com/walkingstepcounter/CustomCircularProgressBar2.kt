package com.walkingstepcounter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable

class CustomCircularProgressBar2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private var maxProgress = 100f
    private var walkingStepsCounter = "0" // Default value
    private var letterSpacing = 0f // Default letter spacing
    private var belowText = "" // New text to be added below the two texts

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 35f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        color = Color.parseColor("#00F1FF") // Default text color
        textSize = 80f
    }

    private val textPaint2 = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        color = Color.parseColor("#00F1FF") // Default text color
        textSize = 40f
    }

    private val textPaint3 = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        color = Color.parseColor("#00F1FF") // Default text color
        textSize = 40f
    }

    private val iconDrawable = ContextCompat.getDrawable(context, R.drawable.walking_ic)
    private val iconDrawable2 = ContextCompat.getDrawable(context, R.drawable.running_ic)
    private var iconSize = 10f
    private var circleMargin = 20f


    /*

        // Create LottieDrawable for iconDrawable2
        private val lottieDrawable = LottieDrawable().apply {
            // Load the Lottie animation
            LottieCompositionFactory.fromRawRes(context, R.raw.lottie_animation).addListener { composition ->
                this.composition = composition
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
            }
        }
    */



    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomCircularProgressBar,
            0, 0
        ).apply {
            try {
                progress = getFloat(R.styleable.CustomCircularProgressBar_progress, progress)
                maxProgress =
                    getFloat(R.styleable.CustomCircularProgressBar_maxProgress, maxProgress)
                progressPaint.color =
                    getColor(R.styleable.CustomCircularProgressBar_progressColor, Color.BLUE)
                paint.color =
                    getColor(R.styleable.CustomCircularProgressBar_backgroundColor, Color.GRAY)
                textPaint.color = getColor(
                    R.styleable.CustomCircularProgressBar_textColor,
                    Color.parseColor("#00F1FF")
                )
                textPaint.textSize =
                    getDimension(R.styleable.CustomCircularProgressBar_textSize, 80f)
                circleMargin =
                    getDimension(R.styleable.CustomCircularProgressBar_circleMargins, circleMargin)
                walkingStepsCounter =
                    getString(R.styleable.CustomCircularProgressBar_walkingStepsCounter) ?: "0"
                letterSpacing = getFloat(R.styleable.CustomCircularProgressBar_letterSpacing, 0f)
                textPaint.letterSpacing = letterSpacing
                belowText = getString(R.styleable.CustomCircularProgressBar_totalSteps)
                    ?: "" // Get the belowText from XML

                val fontId = getResourceId(R.styleable.CustomCircularProgressBar_TextFont, 0)
                if (fontId != 0) {
                    textPaint.typeface = ResourcesCompat.getFont(context, fontId)
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate dimensions considering padding
        val widthWithPadding = width - paddingLeft - paddingRight
        val heightWithPadding = height - paddingTop - paddingBottom

        val radius =
            (widthWithPadding.coerceAtMost(heightWithPadding) / 2f) - circleMargin - progressPaint.strokeWidth
        val centerX = widthWithPadding / 2f + paddingLeft
        val centerY = heightWithPadding / 2f + paddingTop

        // Draw background circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw progress arc
        val sweepAngle = (progress / maxProgress) * 360f
        val startAngle = -90f

        val rectF = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rectF, startAngle, sweepAngle, false, progressPaint)

        // Draw walkingStepsCounter in the center
        canvas.drawText(walkingStepsCounter, centerX, centerY + textPaint.textSize / 4, textPaint)

        // Calculate and draw the icon at the center of the progress arc
        val arcCenterAngle = startAngle + sweepAngle
        val radianAngle = Math.toRadians(arcCenterAngle.toDouble())
        val iconX = centerX + (radius - iconSize / 2) * Math.cos(radianAngle) - iconSize / 2
        val iconY = centerY + (radius - iconSize / 2) * Math.sin(radianAngle) - iconSize / 2

        iconDrawable?.setBounds(
            iconX.toInt() - 30,
            iconY.toInt() - 30,
            (iconX + iconSize).toInt() + 30,
            (iconY + iconSize).toInt() + 30
        )
        iconDrawable?.draw(canvas)

        // Draw the image on top of the text
        /*iconDrawable2?.let {
            val imageWidth = it.intrinsicWidth
            val imageHeight = it.intrinsicHeight
            val imageLeft = (centerX - imageWidth / 2).toInt()
            val imageTop = (centerY - imageHeight - textPaint.textSize).toInt()
            val imageRight = (centerX + imageWidth / 2).toInt()
            val imageBottom = (centerY - textPaint.textSize).toInt()

            it.setBounds(imageLeft, imageTop, imageRight, imageBottom)
            it.draw(canvas)
        }*/

        // Draw the image (iconDrawable2) on top of the text
        iconDrawable2?.let {
            // Set static dimensions for the iconDrawable2
            val staticWidth = 120 // Set the desired width
            val staticHeight = 120 // Set the desired height

            val imageLeft = (centerX - staticWidth / 2).toInt()
            val imageTop = (centerY - staticHeight - textPaint.textSize).toInt()
            val imageRight = (centerX + staticWidth / 2).toInt()
            val imageBottom = (centerY - textPaint.textSize).toInt()

            it.setBounds(imageLeft, imageTop, imageRight, imageBottom)
            it.draw(canvas)
        }


        // Set up the text paints for the two texts
        val whiteTextPaint = Paint(textPaint2).apply {
            color = resources.getColor(R.color.white)
        }
        val blueTextPaint = Paint(textPaint2).apply {
            color = resources.getColor(R.color.blue)
            typeface = Typeface.create(typeface, Typeface.BOLD)
            textSize = 50f
        }

        // Define the two texts
        val todayTxt = "Today"
        val goalTxt = "Goal"

        // Calculate positions for the texts
        val textY = centerY + textPaint.textSize + 40 // Adjust 40 based on the desired spacing
        val textPadding = 10f // Adjust this value to control spacing between the texts
        val leftTextWidth = whiteTextPaint.measureText(todayTxt)
        val rightTextWidth = blueTextPaint.measureText(goalTxt) - 80

        val leftTextX = centerX - (leftTextWidth + textPadding + rightTextWidth) / 2
        val rightTextX = leftTextX + leftTextWidth + textPadding

        // Draw the two texts side by side
        canvas.drawText(todayTxt, leftTextX, textY, whiteTextPaint)
        canvas.drawText(goalTxt, rightTextX, textY, blueTextPaint)

        // Draw the new text below the two texts
        val belowTextPaint = Paint(textPaint3).apply {
            color = resources.getColor(R.color.blue) // Set the color as desired
            textSize = 60f // Set the size as desired
        }

        val belowTextY = textY + belowTextPaint.textSize + 40 // Adjust 30 for height spacing

        canvas.drawText(belowText, centerX, belowTextY, belowTextPaint)
    }

    fun setProgress(progress: Double) {
        this.progress = progress.toFloat()
        invalidate()
    }

    fun setMaxProgress(maxProgress: Float) {
        this.maxProgress = maxProgress
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setTotalStepsText(text: String) {
        this.belowText = text
        invalidate()
    }

    fun setWalkingStepCounter(text: String) {
        this.walkingStepsCounter = text
        invalidate()
    }
}
