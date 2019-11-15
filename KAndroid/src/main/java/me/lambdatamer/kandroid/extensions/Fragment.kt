package me.lambdatamer.kandroid.extensions

import android.os.Parcelable
import androidx.annotation.TransitionRes
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import androidx.transition.TransitionInflater

fun Fragment.inflateTransition(@TransitionRes transitionRes: Int): Transition =
    TransitionInflater.from(context).inflateTransition(transitionRes)

fun <T: Fragment> T.withArguments(args: Parcelable) = apply { arguments = args.pack() }