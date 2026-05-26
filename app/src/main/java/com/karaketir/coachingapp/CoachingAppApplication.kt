package com.karaketir.coachingapp

import android.app.Application

class CoachingAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        configurePoiXmlStreamFactories()
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
