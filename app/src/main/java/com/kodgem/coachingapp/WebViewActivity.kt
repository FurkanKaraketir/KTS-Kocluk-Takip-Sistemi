package com.kodgem.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kodgem.coachingapp.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val webView = binding.webView
        val link = intent.getStringExtra("link")
        if (link != null) {
            webView.loadUrl(link)
        }

    }
}