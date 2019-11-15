package com.justai.junction

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.justai.junction.databinding.ActivityMainBinding
import me.lambdatamer.kandroid.components.KActivity

class MainActivity : KActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.progress.setOnClickListener {
            //Intercept anything
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.progress)
            .into(binding.progress)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainFragment())
            .commit()
    }

    fun setProgressVisibility(isVisible: Boolean) {
        binding.progress.isVisible = isVisible
    }
}

