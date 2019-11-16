package com.justai.junction.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.justai.junction.R
import com.justai.junction.databinding.ActivityMainBinding
import com.justai.junction.viewmodel.QuestViewModel
import me.lambdatamer.kandroid.components.KActivity
import me.lambdatamer.kandroid.viewmodel.viewModel

class MainActivity : KActivity() {
    lateinit var binding: ActivityMainBinding

    private val questVM by viewModel<QuestViewModel>()

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


        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        showStartMenu()
    }

    fun showStartMenu() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, StartFragment())
            .commitAllowingStateLoss()
    }

    fun startNewGame() {
        binding.progress.isVisible = true
        questVM.questStage.value = null
        questVM.questList.value = null
        questVM.currentQuest.value = null
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainFragment())
            .commitAllowingStateLoss()
    }

    fun setProgressVisibility(isVisible: Boolean) {
        binding.progress.isVisible = isVisible
    }
}

