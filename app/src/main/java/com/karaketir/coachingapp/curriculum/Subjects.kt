package com.karaketir.coachingapp.curriculum

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.karaketir.coachingapp.R

/**
 * Canonical subject lists and study-picker tiles (mirrors web `subjects.ts`).
 */
object Subjects {

    private val TILE_PALETTE = intArrayOf(
        R.color.tile_palette_0,
        R.color.tile_palette_1,
        R.color.tile_palette_2,
        R.color.tile_palette_3,
        R.color.tile_palette_4,
        R.color.tile_palette_5,
        R.color.tile_palette_6,
        R.color.tile_palette_7,
    )

    private val GRADE_TILE_COLOR = mapOf(
        0 to R.color.tile_palette_5,
        9 to R.color.tile_palette_0,
        10 to R.color.tile_palette_1,
        11 to R.color.tile_palette_2,
        12 to R.color.tile_palette_3,
        13 to R.color.tile_palette_4,
    )

    private val PROGRAM_TILE_COLOR = mapOf(
        CurriculumProgram.LEGACY to R.color.tile_palette_6,
        CurriculumProgram.TYMM to R.color.tile_palette_7,
    )

    private val SUBJECT_ICON_BY_NAME: Map<String, Int> = mapOf(
        "Matematik" to R.drawable.ic_subject_math,
        "Fizik" to R.drawable.ic_subject_physics,
        "Kimya" to R.drawable.ic_subject_chemistry,
        "Biyoloji" to R.drawable.ic_subject_biology,
        "Türkçe-Edebiyat" to R.drawable.ic_baseline_menu_book_24,
        "Türk Dili ve Edebiyatı" to R.drawable.ic_baseline_menu_book_24,
        "Coğrafya" to R.drawable.ic_subject_geography,
        "Tarih" to R.drawable.ic_subject_history,
        "T.C. İnkılap Tarihi ve Atatürkçülük" to R.drawable.ic_subject_history,
        "Geometri" to R.drawable.ic_subject_geometry,
        "Felsefe" to R.drawable.ic_subject_philosophy,
        "Din" to R.drawable.ic_subject_religion,
        "Din Kültürü ve Ahlak Bilgisi" to R.drawable.ic_subject_religion,
        "Paragraf" to R.drawable.ic_subject_writing,
        "Problem" to R.drawable.ic_subject_numbers,
        "Diğer" to R.drawable.ic_subject_writing,
        "Beden Eğitimi ve Spor" to R.drawable.ic_subject_sports,
        "Görsel Sanatlar" to R.drawable.ic_subject_art,
        "İngilizce (9-12)" to R.drawable.ic_subject_english,
        "İngilizce (Hazırlık-12)" to R.drawable.ic_subject_english,
        "Müzik" to R.drawable.ic_subject_music,
    )

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

    /** Paragraf/Problem — no TYMM tema; same as legacy study picker extras. */
    val TYMM_EXTRA_STUDY_SUBJECTS: List<String> = listOf("Paragraf", "Problem")

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
        SubjectTile("Matematik"),
        SubjectTile("Fizik"),
        SubjectTile("Kimya"),
        SubjectTile("Biyoloji"),
        SubjectTile("Türkçe-Edebiyat"),
        SubjectTile("Coğrafya"),
        SubjectTile("Tarih"),
        SubjectTile("Geometri"),
        SubjectTile("Felsefe"),
        SubjectTile("Din"),
        SubjectTile("Paragraf"),
        SubjectTile("Problem"),
        SubjectTile("Diğer"),
    )

    fun gradeOptionLabel(grade: Int): String = when (grade) {
        0 -> "Hazırlık"
        13 -> "Mezun"
        else -> "$grade. sınıf"
    }

    @DrawableRes
    fun gradeIconRes(grade: Int): Int = when (grade) {
        0 -> R.drawable.ic_grade_prep
        13 -> R.drawable.ic_grade_graduate
        else -> R.drawable.ic_grade_school
    }

    @DrawableRes
    fun programIconRes(program: CurriculumProgram): Int = when (program) {
        CurriculumProgram.LEGACY -> R.drawable.ic_program_exam
        CurriculumProgram.TYMM -> R.drawable.ic_program_curriculum
    }

    @ColorRes
    fun gradeTileColorRes(grade: Int): Int =
        GRADE_TILE_COLOR[grade] ?: TILE_PALETTE[grade % TILE_PALETTE.size]

    @ColorRes
    fun programTileColorRes(program: CurriculumProgram): Int =
        PROGRAM_TILE_COLOR[program] ?: R.color.tile_palette_0

    @ColorRes
    fun subjectTileColorRes(index: Int): Int = TILE_PALETTE[index % TILE_PALETTE.size]

    @ColorRes
    fun subjectTileColorResForName(name: String): Int {
        val tiles = LEGACY_STUDY_SUBJECT_TILES
        val index = tiles.indexOfFirst { it.name == name }
        if (index >= 0) return subjectTileColorRes(index)
        return TILE_PALETTE[name.hashCode().and(0x7FFFFFFF) % TILE_PALETTE.size]
    }

    @DrawableRes
    fun subjectIconRes(name: String): Int =
        SUBJECT_ICON_BY_NAME[name] ?: R.drawable.ic_baseline_menu_book_24

    fun subjectDisplayLabel(name: String): String = name

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
        val core = tymmSubjectsForGrade(grade).map { name -> SubjectTile(name) }
        val extra = TYMM_EXTRA_STUDY_SUBJECTS.map { name -> SubjectTile(name) }
        return core + extra
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

    fun subjectNamesForProgram(program: CurriculumProgram, grade: Int): List<String> =
        studySubjectTilesForProgram(program, grade).map { it.name }
}

data class SubjectTile(
    val name: String,
    @DrawableRes val iconRes: Int = Subjects.subjectIconRes(name),
)
