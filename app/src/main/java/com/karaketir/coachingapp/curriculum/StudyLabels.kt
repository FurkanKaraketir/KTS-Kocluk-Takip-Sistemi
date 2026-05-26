package com.karaketir.coachingapp.curriculum

import com.google.firebase.firestore.DocumentSnapshot

object StudyLabels {

    fun isMaarifStudy(program: String?, tur: String?): Boolean {
        return program == CurriculumProgram.TYMM.firestoreValue || tur == "Maarif"
    }

    fun isMaarifStudy(data: Map<String, Any?>): Boolean {
        return isMaarifStudy(
            data["program"]?.toString(),
            data["tür"]?.toString()
        )
    }

    fun isMaarifStudy(doc: DocumentSnapshot): Boolean {
        return isMaarifStudy(
            doc.getString("program"),
            doc.getString("tür")
        )
    }

    fun studyTrackingLabel(
        program: String?,
        tur: String?,
        temaAdi: String?,
        konuAdi: String?
    ): String {
        return if (isMaarifStudy(program, tur)) {
            temaAdi?.takeIf { it.isNotBlank() } ?: konuAdi.orEmpty()
        } else {
            konuAdi.orEmpty()
        }
    }

    fun studyTrackingLabel(doc: DocumentSnapshot): String {
        return studyTrackingLabel(
            doc.getString("program"),
            doc.getString("tür"),
            doc.getString("temaAdi"),
            doc.getString("konuAdi")
        )
    }

    fun studyTypeLabel(
        program: String?,
        tur: String?,
        sinif: Int?,
        temaAdi: String?
    ): String {
        if (program == CurriculumProgram.TYMM.firestoreValue) {
            val parts = mutableListOf("Maarif")
            sinif?.let { parts.add("$it. sınıf") }
            temaAdi?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
            return parts.joinToString(" · ")
        }
        val normalizedTur = if (tur == "Maarif") "Maarif" else tur.orEmpty()
        if (normalizedTur == "Maarif") {
            return if (!temaAdi.isNullOrBlank()) "Maarif · $temaAdi" else "Maarif"
        }
        return tur?.takeIf { it.isNotBlank() } ?: "—"
    }

    fun studyTypeLabel(doc: DocumentSnapshot): String {
        val sinif = doc.getLong("sinif")?.toInt()
        return studyTypeLabel(
            doc.getString("program"),
            doc.getString("tür"),
            sinif,
            doc.getString("temaAdi")
        )
    }

    fun programDisplayName(program: CurriculumProgram, grade: Int): String {
        return when (program) {
            CurriculumProgram.TYMM -> "Maarif · $grade. sınıf"
            CurriculumProgram.LEGACY -> "TYT/AYT · $grade. sınıf"
        }
    }

    fun programLabel(program: String?): String {
        return when (program) {
            CurriculumProgram.TYMM.firestoreValue -> "Maarif"
            CurriculumProgram.LEGACY.firestoreValue -> "TYT/AYT"
            else -> program.orEmpty()
        }
    }
}
