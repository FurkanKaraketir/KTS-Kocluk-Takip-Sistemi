package com.karaketir.coachingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.karaketir.coachingapp.curriculum.StudyLabels

class Duty(
    var konuAdi: String,
    var toplamCalisma: String,
    var studyOwnerID: String,
    var dersAdi: String,
    var tur: String,
    var cozulenSoru: String,
    var bitisZamani: Timestamp,
    var dutyID: String,
    var dutyTamamlandi: Boolean,
    var program: String? = null,
    var temaId: String? = null,
    var temaAdi: String? = null,
    var sinif: Int? = null,
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot, ownerId: String): Duty {
            val program = doc.getString("program")
            val turRaw = doc.getString("tür").orEmpty()
            val temaAdi = doc.getString("temaAdi")
            val konuAdiRaw = doc.getString("konuAdi").orEmpty()
            val sinif = doc.getLong("sinif")?.toInt()
            return Duty(
                konuAdi = StudyLabels.studyTrackingLabel(program, turRaw, temaAdi, konuAdiRaw),
                toplamCalisma = doc.get("toplamCalisma").toString(),
                studyOwnerID = ownerId,
                dersAdi = doc.getString("dersAdi").orEmpty(),
                tur = StudyLabels.studyTypeLabel(program, turRaw, sinif, temaAdi),
                cozulenSoru = doc.get("çözülenSoru").toString(),
                bitisZamani = doc.getTimestamp("bitisZamani")!!,
                dutyID = doc.id,
                dutyTamamlandi = doc.getBoolean("tamamlandi") == true,
                program = program,
                temaId = doc.getString("temaId"),
                temaAdi = temaAdi,
                sinif = sinif,
            )
        }
    }
}
