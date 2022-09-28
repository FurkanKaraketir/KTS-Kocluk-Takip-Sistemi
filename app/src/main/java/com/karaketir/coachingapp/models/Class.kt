package com.karaketir.coachingapp.models

import java.util.Date

class Class(
    var dersAdi: String,
    var studentID: String,
    var baslangicTarihi: Date,
    var bitisTarihi: Date,
    var secilenZamanAraligi: String,
    var cozulenSoru: Int,
    var toplamCalisma: Int
)