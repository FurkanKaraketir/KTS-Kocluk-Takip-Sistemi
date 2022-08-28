package com.kodgem.coachingapp.models

import com.google.firebase.Timestamp


class Duty(
    var konuAdi: String,
    var toplamCalisma: String,
    var studyOwnerID: String,
    var dersAdi: String,
    var tur: String,
    var cozulenSoru: String,
    var bitisZamani: Timestamp,
    var dutyID: String

)