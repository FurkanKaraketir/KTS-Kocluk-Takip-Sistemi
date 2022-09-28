package com.karaketir.coachingapp.models

import com.google.firebase.Timestamp

class Study(
    var studyName: String,
    var studyCount: String,
    var studyOwnerID: String,
    var studyDersAdi: String,
    var dersTur: String,
    var soruSayisi: String,
    val timestamp: Timestamp,
    var studyID: String

)