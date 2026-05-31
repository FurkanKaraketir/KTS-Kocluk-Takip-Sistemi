package com.karaketir.coachingapp.curriculum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubjectsTest {

    @Test
    fun subjectNamesForProgram_legacyMatchesLegacyTiles() {
        val names = Subjects.subjectNamesForProgram(CurriculumProgram.LEGACY, 12)
        assertEquals(Subjects.LEGACY_STUDY_SUBJECT_TILES.map { it.name }, names)
    }

    @Test
    fun subjectNamesForProgram_tymmFiltersEnglishByGrade() {
        val hazirlik = Subjects.subjectNamesForProgram(CurriculumProgram.TYMM, 0)
        assertTrue(hazirlik.contains("İngilizce (Hazırlık-12)"))
        assertTrue(!hazirlik.contains("İngilizce (9-12)"))

        val onuncu = Subjects.subjectNamesForProgram(CurriculumProgram.TYMM, 10)
        assertTrue(onuncu.contains("İngilizce (9-12)"))
        assertTrue(!onuncu.contains("İngilizce (Hazırlık-12)"))
    }
}
