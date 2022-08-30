package com.kodgem.coachingapp.models

import com.google.firebase.Timestamp

class Deneme(
    var denemeID:String,
    var denemeAdi: String,
    var denemeToplamNet:Float,
    var denemeTarihi: Timestamp,
    var denemeStudentID: String
)