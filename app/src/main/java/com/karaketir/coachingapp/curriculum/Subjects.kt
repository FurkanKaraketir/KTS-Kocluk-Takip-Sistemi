package com.karaketir.coachingapp.curriculum

/**
 * Canonical subject lists and study-picker tiles (mirrors web `subjects.ts`).
 */
object Subjects {

    val LEGACY_SUBJECT_NAMES: List<String> = listOf(
        "Türkçe-Edebiyat",
        "Tarih",
        "Coğrafya",
        "Felsefe",
        "Din",
        "Matematik",
        "Fizik",
        "Kimya",
        "Biyoloji",
        "Geometri",
    )

    val TYMM_SUBJECT_NAMES: List<String> = listOf(
        "Beden Eğitimi ve Spor",
        "Biyoloji",
        "Coğrafya",
        "Din Kültürü ve Ahlak Bilgisi",
        "Felsefe",
        "Fizik",
        "Görsel Sanatlar",
        "İngilizce (9-12)",
        "İngilizce (Hazırlık-12)",
        "Kimya",
        "Matematik",
        "Müzik",
        "T.C. İnkılap Tarihi ve Atatürkçülük",
        "Tarih",
        "Türk Dili ve Edebiyatı",
    )

    val GRADE_OPTIONS: List<Int> = listOf(0, 9, 10, 11, 12, 13)

    val LEGACY_TO_TYMM_SUBJECT_MAP: Map<String, String?> = mapOf(
        "Türkçe-Edebiyat" to "Türk Dili ve Edebiyatı",
        "Tarih" to "Tarih",
        "Coğrafya" to "Coğrafya",
        "Felsefe" to "Felsefe",
        "Din" to "Din Kültürü ve Ahlak Bilgisi",
        "Matematik" to "Matematik",
        "Fizik" to "Fizik",
        "Kimya" to "Kimya",
        "Biyoloji" to "Biyoloji",
        "Geometri" to null,
    )

    val LEGACY_STUDY_SUBJECT_TILES: List<SubjectTile> = listOf(
        SubjectTile("Matematik", "∑"),
        SubjectTile("Fizik", "⚛"),
        SubjectTile("Kimya", "⚗"),
        SubjectTile("Biyoloji", "🧬"),
        SubjectTile("Türkçe-Edebiyat", "📚"),
        SubjectTile("Coğrafya", "🌍"),
        SubjectTile("Tarih", "📜"),
        SubjectTile("Geometri", "📐"),
        SubjectTile("Felsefe", "🤔"),
        SubjectTile("Din", "🕌"),
        SubjectTile("Paragraf", "📝"),
        SubjectTile("Problem", "🔢"),
        SubjectTile("Diğer", "📝"),
    )

    private val tymmTileIcons: Map<String, String> = mapOf(
        "Beden Eğitimi ve Spor" to "🏃",
        "Biyoloji" to "🧬",
        "Coğrafya" to "🌍",
        "Din Kültürü ve Ahlak Bilgisi" to "🕌",
        "Felsefe" to "🤔",
        "Fizik" to "⚛",
        "Görsel Sanatlar" to "🎨",
        "İngilizce (9-12)" to "🇬🇧",
        "İngilizce (Hazırlık-12)" to "🇬🇧",
        "Kimya" to "⚗",
        "Matematik" to "∑",
        "Müzik" to "🎵",
        "T.C. İnkılap Tarihi ve Atatürkçülük" to "📜",
        "Tarih" to "📜",
        "Türk Dili ve Edebiyatı" to "📚",
    )

    fun gradeOptionLabel(grade: Int): String = when (grade) {
        0 -> "Hazırlık"
        13 -> "Mezun"
        else -> "$grade. sınıf"
    }

    fun tymmSubjectsForGrade(grade: Int): List<String> {
        return TYMM_SUBJECT_NAMES.filter { name ->
            when (name) {
                "İngilizce (9-12)" -> grade != 0
                "İngilizce (Hazırlık-12)" -> grade == 0
                else -> true
            }
        }
    }

    fun tymmStudySubjectTiles(grade: Int): List<SubjectTile> {
        return tymmSubjectsForGrade(grade).map { name ->
            SubjectTile(name, tymmTileIcons[name] ?: "📖")
        }
    }

    fun studySubjectTilesForProgram(program: CurriculumProgram, grade: Int): List<SubjectTile> {
        return when (program) {
            CurriculumProgram.LEGACY -> LEGACY_STUDY_SUBJECT_TILES
            CurriculumProgram.TYMM -> tymmStudySubjectTiles(grade)
        }
    }

    fun programHeaderLabel(program: CurriculumProgram): String = when (program) {
        CurriculumProgram.LEGACY -> "TYT/AYT"
        CurriculumProgram.TYMM -> "Maarif"
    }
}

data class SubjectTile(
    val name: String,
    val icon: String,
)
