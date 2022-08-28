package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.SystemClock
import android.transition.TransitionManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.kodgem.coachingapp.databinding.ActivityChronometerBinding

class ChronometerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChronometerBinding
    private var isPlay = false
    private var pauseOffSet: Long = 0

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChronometerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val plyButton = binding.plyButton
        val stpButton = binding.stpButton
        val pauseButton = binding.pauseButton

        val chronometer = binding.chronometer
        val transitionsContainer = binding.chronometerAnimationContainer

        plyButton.visibility = View.VISIBLE
        stpButton.visibility = View.GONE
        pauseButton.visibility = View.GONE

        val themeChangeSwitch = binding.themeChangeSwitch

        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> themeChangeSwitch.isChecked = true
            Configuration.UI_MODE_NIGHT_NO -> themeChangeSwitch.isChecked = false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> themeChangeSwitch.isChecked = false
        }

        if (themeChangeSwitch.isChecked) {
            themeChangeSwitch.text = "Koyu Tema"
        } else {
            themeChangeSwitch.text = "Açık Tema"
        }

        themeChangeSwitch.setOnCheckedChangeListener { _, _ ->
            if (themeChangeSwitch.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                themeChangeSwitch.text = "Koyu Tema"
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                themeChangeSwitch.text = "Açık Tema"

            }
        }




        plyButton.setOnClickListener {
            if (!isPlay) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffSet
                chronometer.start()

                TransitionManager.beginDelayedTransition(transitionsContainer)

                plyButton.visibility = View.GONE
                stpButton.visibility = View.VISIBLE
                pauseButton.visibility = View.VISIBLE
                isPlay = true
            }
        }

        stpButton.setOnClickListener {
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffSet = 0
            chronometer.stop()

            TransitionManager.beginDelayedTransition(transitionsContainer)

            plyButton.visibility = View.VISIBLE
            stpButton.visibility = View.GONE
            pauseButton.visibility = View.GONE
            isPlay = false
        }

        pauseButton.setOnClickListener {
            if (isPlay) {
                chronometer.stop()
                pauseOffSet = SystemClock.elapsedRealtime() - chronometer.base
                isPlay = false

                TransitionManager.beginDelayedTransition(transitionsContainer)

                plyButton.visibility = View.VISIBLE
                stpButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }
        }

    }
}