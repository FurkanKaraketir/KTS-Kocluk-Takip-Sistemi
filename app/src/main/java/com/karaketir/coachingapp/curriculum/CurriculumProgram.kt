package com.karaketir.coachingapp.curriculum

enum class CurriculumProgram(val firestoreValue: String) {
    LEGACY("legacy"),
    TYMM("tymm");

    companion object {
        fun fromFirestore(value: String?): CurriculumProgram? = when (value) {
            LEGACY.firestoreValue -> LEGACY
            TYMM.firestoreValue -> TYMM
            else -> null
        }
    }
}

data class TemaOption(
    val id: String,
    val name: String
)

data class GradeCurriculumConfig(
    val grades: Map<Int, CurriculumProgram>
)

data class StudyRecordFields(
    val program: CurriculumProgram?,
    val dersAdi: String,
    val konuAdi: String,
    val tur: String? = null,
    val sinif: Int? = null,
    val temaAdi: String? = null,
    val temaId: String? = null
)
