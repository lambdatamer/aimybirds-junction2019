package com.justai.junction.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.animation.doOnCancel
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.justai.junction.R
import me.lambdatamer.kandroid.extensions.getColorCompat
import me.lambdatamer.kandroid.extensions.toPx
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

internal class AimyboxButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var isExpanded: Boolean = false
    private var isRecording: Boolean = false

    /* Inner views */

    private val recordingView: View = View(context)
    private val actionButton = FloatingActionButton(context)
    private var contentViews = emptyList<View>()

    /* Button */

    private var buttonSize: Float = 56.toPx(context).toFloat()
    private var buttonMarginStart: Int = 0
    private var buttonMarginEnd: Int = 0
    private var buttonMarginBottom: Int = 0
    private var buttonGravity: Int = 0
    private var buttonElevation: Float = 16.toPx(context).toFloat()

    private var buttonStartDrawable: Drawable? = null
    private var buttonStopDrawable: Drawable? = null

    @ColorInt
    private var buttonDrawableExpandedColor: Int = Color.TRANSPARENT
    @ColorInt
    private var buttonDrawableCollapsedColor: Int = Color.TRANSPARENT

    /* Background Ink View */

    @ColorInt
    private var inkViewBackgroundColor: Int = Color.TRANSPARENT
    private var inkViewBackground: Drawable? = null
    private var inkViewRadiusCollapsed: Float = 0F
    private var inkViewRadiusExpanded: Float = 0F

    private var inkAnimator: ViewPropertyAnimator? = null

    /* Recording view */

    @ColorInt
    private var recordingViewBackgroundColor: Int = Color.TRANSPARENT
    private var recordingViewBackground: Drawable? = null

    private var recordingAnimator: ValueAnimator? = null
    /**
     * The flag is automatically set to true when [onRecordingVolumeChanged] is called.
     * If the flag is false, then default simple repeating recording animation will be played during recording.
     * */
    private var isVolumeInformationAvailable: Boolean = false
    private var maxSoundVolume: Float = Float.MIN_VALUE
    private var minSoundVolume: Float = Float.MAX_VALUE
    /**
     * This value will be calculated based on sound volume samples interval, no need to set it manually
     * */
    private var soundVolumeAnimationDuration = 50L
    private var lastVolumeSampleTime: Long? = null

    init {
        buttonStartDrawable = context.getDrawable(R.drawable.icon_mic)
        buttonStopDrawable = context.getDrawable(R.drawable.icon_stop)
        buttonMarginBottom = 32.toPx(context)
        buttonGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

        recordingViewBackgroundColor = context.getColorCompat(R.color.white_30)

        recordingViewBackground = createCircleShape(recordingViewBackgroundColor)

        actionButton.customSize = buttonSize.toInt()
        actionButton.elevation = buttonElevation
        actionButton.background = Color.parseColor("#FFFFFF").toDrawable()

        recordingView.background = recordingViewBackground
        recordingView.elevation = buttonElevation
        recordingView.outlineProvider = null

        addView(recordingView)
        addView(actionButton)
        onRecordingStopped()
    }

    fun onRecordingStarted() {
        isRecording = true
        recordingAnimator?.cancel()
        if (!isVolumeInformationAvailable) recordingAnimator = startSimpleRecordingAnimation()
        actionButton.setImageDrawable(buttonStopDrawable)
    }

    fun onRecordingStopped() {
        isRecording = false
        recordingAnimator?.cancel()
        actionButton.setImageDrawable(buttonStartDrawable)
        handler?.post { setRecordingViewScale(1F) }
    }

    fun onRecordingVolumeChanged(volume: Float) {
        recordingAnimator?.cancel()
        isVolumeInformationAvailable = true

        maxSoundVolume = max(maxSoundVolume, volume)
        minSoundVolume = min(minSoundVolume, volume)

        val currentTime = System.currentTimeMillis()

        lastVolumeSampleTime?.let {
            soundVolumeAnimationDuration = currentTime - it
        }
        lastVolumeSampleTime = currentTime

        val soundInterval = maxSoundVolume - minSoundVolume

        // From 0 to 1
        val soundVolumeRelative = if (soundInterval == 0F) {
            0F
        } else {
            (volume - minSoundVolume) / soundInterval
        }

        val scale = soundVolumeRelative + 1F

        recordingAnimator = smoothSetRecordingViewScale(recordingView.scaleX, scale)
    }

    private fun setRecordingViewScale(scale: Float) = recordingView.apply {
        pivotX = width / 2F
        pivotY = height / 2F
        scaleX = scale
        scaleY = scale
    }

    private fun smoothSetRecordingViewScale(fromScale: Float, toScale: Float) =
        ValueAnimator().apply {
            setFloatValues(fromScale, toScale)
            duration = soundVolumeAnimationDuration
            addUpdateListener { animator -> setRecordingViewScale(animator.animatedValue as Float) }
            start()
        }

    private fun startSimpleRecordingAnimation() = ValueAnimator().apply {
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        setFloatValues(1F, 2F)
        duration = 500
        addUpdateListener { animator -> setRecordingViewScale(animator.animatedValue as Float) }
        doOnCancel { setRecordingViewScale(1F) }
        start()
    }

    private fun View.startInkAnimation(
        scale: Float,
        duration: Long,
        onFinish: () -> Unit = {}
    ) = animate().apply {
        scaleX(scale)
        scaleY(scale)
        setDuration(duration)
        setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) = onFinish()
        })
        start()
    }

    private fun calculateExpandedRadius(
        parentWidth: Float,
        parentHeight: Float,
        x: Float,
        y: Float,
        radiusCollapsed: Float
    ): Float {
        val viewCenterX = x + radiusCollapsed
        val viewCenterY = y + radiusCollapsed

        val expandHorizontal =
            max(parentWidth - viewCenterX, parentWidth - (parentWidth - viewCenterX))
        val expandVertical =
            max(parentHeight - viewCenterY, parentHeight - (parentHeight - viewCenterY))

        return sqrt(expandHorizontal.pow(2) + expandVertical.pow(2))
    }

    private fun createCircleShape(color: Int) = ShapeDrawable(OvalShape()).apply {
        paint.color = color
    }

    override fun setOnClickListener(l: OnClickListener?) {
        actionButton.setOnClickListener(l)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        contentViews =
            children.filter { it != actionButton && it != recordingView }.toList()
        contentViews.forEach { it.isVisible = false }
        actionButton.bringToFront()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)

        MeasureSpec.makeMeasureSpec(buttonSize.toInt(), MeasureSpec.AT_MOST).let { measureSpec ->
            actionButton.measure(measureSpec, measureSpec)
            recordingView.measure(measureSpec, measureSpec)
        }

        actionButton.updateLayoutParams<LayoutParams> {
            gravity = buttonGravity
            bottomMargin = buttonMarginBottom
        }

        contentViews.forEach { it.measure(widthMeasureSpec, heightMeasureSpec) }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        recordingView.x = actionButton.x
        recordingView.y = actionButton.y
    }

}