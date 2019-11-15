package me.lambdatamer.kandroid.extensions

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.os.postDelayed
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop


private const val RIPPLE_CLICK_DELAY = 150L

fun View.onClick(listener: (View) -> Unit) = setOnClickListener(listener)

fun View.onRippleClick(
    delay: Long = RIPPLE_CLICK_DELAY,
    disableOnClick: Boolean = true,
    listener: (View) -> Unit
) = onClick {
    if (disableOnClick) isEnabled = false
    handler.postDelayed(delay) {
        isEnabled = true
        listener(this)
    }
}