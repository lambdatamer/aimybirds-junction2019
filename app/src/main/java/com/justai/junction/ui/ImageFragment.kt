package com.justai.junction.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.justai.junction.databinding.FragmentImageBinding
import kotlinx.android.parcel.Parcelize
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.createLogger
import me.lambdatamer.kandroid.extensions.toPx
import me.lambdatamer.kandroid.extensions.unpack

class ImageFragment : KFragment() {

    private val L = createLogger()

    private val arguments by lazy { requireArguments().unpack<Arguments>() }

    lateinit var animator: ValueAnimator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentImageBinding.inflate(inflater, container, false)

        if (arguments.imageUrl != null) {
            Glide.with(requireContext())
                .load(arguments.imageUrl)
                .into(binding.image)
        } else {
            binding.image.setImageResource(arguments.resId!!)
        }

        animator = ValueAnimator.ofInt(32.toPx(requireContext()), 64.toPx(requireContext())).apply {
                addUpdateListener {
                    binding.image.setPadding(it.animatedValue as Int)
                    binding.image.invalidate()
                }

                duration = 1000
                repeatMode = ValueAnimator.REVERSE
                repeatCount = -1
            }

        animator.start()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animator.cancel()
    }

    @Parcelize
    data class Arguments(val imageUrl: String? = null, val resId: Int? = null) : Parcelable
}