package com.karaketir.coachingapp.curriculum

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object GradeCurriculumRepository {

    private const val CONFIG_COLLECTION = "Settings"
    private const val CONFIG_DOCUMENT = "gradeCurriculum"

    val defaultGradePrograms: Map<Int, CurriculumProgram> = mapOf(
        0 to CurriculumProgram.TYMM,
        9 to CurriculumProgram.TYMM,
        10 to CurriculumProgram.TYMM,
        11 to CurriculumProgram.LEGACY,
        12 to CurriculumProgram.LEGACY,
        13 to CurriculumProgram.LEGACY
    )

    fun programForGrade(config: GradeCurriculumConfig, grade: Int): CurriculumProgram {
        config.grades[grade]?.let { return it }
        return if (grade >= 11) CurriculumProgram.LEGACY else CurriculumProgram.TYMM
    }

    suspend fun load(db: FirebaseFirestore = FirebaseFirestore.getInstance()): GradeCurriculumConfig {
        val grades = defaultGradePrograms.toMutableMap()
        try {
            val snap = db.collection(CONFIG_COLLECTION).document(CONFIG_DOCUMENT).get().await()
            if (snap.exists()) {
                val data = snap.data ?: return GradeCurriculumConfig(grades)
                @Suppress("UNCHECKED_CAST")
                val stored = (data["grades"] as? Map<String, Any>) ?: data
                for ((key, value) in stored) {
                    val g = key.toIntOrNull() ?: continue
                    val program = CurriculumProgram.fromFirestore(value.toString()) ?: continue
                    grades[g] = program
                }
            }
        } catch (_: Exception) {
            // Use defaults on failure
        }
        return GradeCurriculumConfig(grades)
    }

    suspend fun loadTymmThemes(
        db: FirebaseFirestore,
        dersAdi: String,
        sinif: Int
    ): List<TemaOption> {
        val snap = db.collection(LessonPaths.TYMM_LESSONS)
            .document(dersAdi)
            .collection(sinif.toString())
            .orderBy("temaAdi", Query.Direction.ASCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val temaAdi = doc.getString("temaAdi") ?: return@mapNotNull null
            TemaOption(id = doc.id, name = temaAdi)
        }
    }
}
