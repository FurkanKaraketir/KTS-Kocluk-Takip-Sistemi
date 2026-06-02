package com.karaketir.coachingapp

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.notifications.NotificationChannelRegistry

class CoachingAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationChannelRegistry.ensureCreated(this)
        registerActivityLifecycleCallbacks(SystemBarsCallbacks())
        configurePoiXmlStreamFactories()
    }

    private class SystemBarsCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
            configureWindowSystemBars(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            configureWindowSystemBars(activity)
            hideSystemTitleBar(activity)
            applySystemBarInsetsToContent(activity)
        }

        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit

        private fun hideSystemTitleBar(activity: Activity) {
            (activity as? AppCompatActivity)?.supportActionBar?.hide()
        }

        private fun configureWindowSystemBars(activity: Activity) {
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            val statusBarColor = ContextCompat.getColor(activity, R.color.system_status_bar)
            val navigationBarColor = ContextCompat.getColor(activity, R.color.system_navigation_bar)
            window.statusBarColor = statusBarColor
            window.navigationBarColor = navigationBarColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars =
                navigationBarColor == ContextCompat.getColor(activity, R.color.white)
        }

        private fun applySystemBarInsetsToContent(activity: Activity) {
            val contentRoot = activity.findViewById<View>(android.R.id.content) ?: return
            ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { view, windowInsets ->
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                WindowInsetsCompat.CONSUMED
            }
            ViewCompat.requestApplyInsets(contentRoot)
        }
    }

    private fun configurePoiXmlStreamFactories() {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }
}
