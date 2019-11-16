package com.justai.junction.viewmodel

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.justai.junction.databinding.FragmentImageBinding
import kotlinx.android.parcel.Parcelize
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.unpack

class ImageFragment : KFragment() {

    private val arguments by lazy { requireArguments().unpack<Arguments>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentImageBinding.inflate(inflater, container, false)
        binding.url = arguments.imageUrl

        return binding.root
    }

    @Parcelize
    data class Arguments(val imageUrl: String) : Parcelable
}