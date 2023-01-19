package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.karaketir.coachingapp.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    init {
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