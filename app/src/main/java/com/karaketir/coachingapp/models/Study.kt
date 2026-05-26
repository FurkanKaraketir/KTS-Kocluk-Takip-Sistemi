package com.karaketir.coachingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.karaketir.coachingapp.curriculum.StudyLabels

class Study(
    var studyName: String,
    var studyCount: String,
    var studyOwnerID: String,
    var studyDersAdi: String,
    var dersTur: String,
    var soruSayisi: String,
    val timestamp: Timestamp,
    var studyID: String,
    var program: String? = null,
    var temaId: String? = null,
    var temaAdi: String? = null,
    var sinif: Int? = null
) {
    val trackingLabel: String
        get() = StudyLabels.studyTrackingLabel(program, dersTur, temaAdi, studyName)

    val typeLabel: String
        get() = StudyLabels.studyTypeLabel(program, dersTur, sinif, temaAdi)

    companion object {
        fun fromDocument(doc: DocumentSnapshot, ownerId: String): Study {
            val konuAdi = doc.getString("konuAdi").orEmpty()
            val tur = doc.getString("tür").orEmpty()
            val program = doc.getString("program")
            val temaAdi = doc.getString("temaAdi")
            val temaId = doc.getString("temaId")
            val sinif = doc.getLong("sinif")?.toInt()
            val study = Study(
                studyName = konuAdi,
                studyCount = doc.get("toplamCalisma").toString(),
                studyOwnerID = ownerId,
                studyDersAdi = doc.getString("dersAdi").orEmpty(),
                dersTur = tur,
                soruSayisi = doc.get("çözülenSoru").toString(),
                timestamp = doc.getTimestamp("timestamp")!!,
                studyID = doc.id,
                program = program,
                temaId = temaId,
                temaAdi = temaAdi,
                sinif = sinif
            )
            study.studyName = study.trackingLabel
            study.dersTur = study.typeLabel
            return study
        }
    }
}
