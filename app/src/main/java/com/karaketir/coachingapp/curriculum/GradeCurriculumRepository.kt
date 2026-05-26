package com.karaketir.coachingapp.curriculum

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object GradeCurriculumRepository {

    private const val CONFIG_COLLECTION = "Settings"
    private const val CONFIG_DOCUMENT = "gradeCurriculum"

    val defaultGradeModes: Map<Int, GradeCurriculumMode> = mapOf(
        0 to GradeCurriculumMode.TYMM,
        9 to GradeCurriculumMode.TYMM,
        10 to GradeCurriculumMode.TYMM,
        11 to GradeCurriculumMode.TYMM,
        12 to GradeCurriculumMode.CHOICE,
        13 to GradeCurriculumMode.LEGACY,
    )

    fun defaultConfig(): GradeCurriculumConfig = GradeCurriculumConfig(defaultGradeModes)

    fun modeForGrade(config: GradeCurriculumConfig, grade: Int): GradeCurriculumMode {
        config.gradeModes[grade]?.let { return it }
        return if (grade >= 11) GradeCurriculumMode.LEGACY else GradeCurriculumMode.TYMM
    }

    fun offersProgramChoice(config: GradeCurriculumConfig, grade: Int): Boolean {
        return modeForGrade(config, grade) == GradeCurriculumMode.CHOICE
    }

    fun programForGrade(config: GradeCurriculumConfig, grade: Int): CurriculumProgram {
        return when (modeForGrade(config, grade)) {
            GradeCurriculumMode.LEGACY -> CurriculumProgram.LEGACY
            GradeCurriculumMode.TYMM -> CurriculumProgram.TYMM
            GradeCurriculumMode.CHOICE -> defaultProgramForChoiceGrade(grade)
        }
    }

    private fun defaultProgramForChoiceGrade(grade: Int): CurriculumProgram {
        return if (grade >= 11) CurriculumProgram.LEGACY else CurriculumProgram.TYMM
    }

    /** Profile preference when set; otherwise the admin default for the grade. */
    fun preferredProgram(
        config: GradeCurriculumConfig,
        grade: Int,
        profileProgram: CurriculumProgram?,
    ): CurriculumProgram {
        if (offersProgramChoice(config, grade) && profileProgram != null) {
            return profileProgram
        }
        return programForGrade(config, grade)
    }

    fun activeProgram(
        config: GradeCurriculumConfig,
        grade: Int,
        profileProgram: CurriculumProgram?,
        sessionProgram: CurriculumProgram?,
    ): CurriculumProgram {
        sessionProgram?.let { return it }
        return preferredProgram(config, grade, profileProgram)
    }

    suspend fun load(db: FirebaseFirestore = FirebaseFirestore.getInstance()): GradeCurriculumConfig {
        val gradeModes = defaultGradeModes.toMutableMap()
        try {
            val snap = db.collection(CONFIG_COLLECTION).document(CONFIG_DOCUMENT).get().await()
            if (snap.exists()) {
                val data = snap.data ?: return GradeCurriculumConfig(gradeModes)
                @Suppress("UNCHECKED_CAST")
                val stored = (data["grades"] as? Map<String, Any>) ?: data
                for ((key, value) in stored) {
                    val g = key.toIntOrNull() ?: continue
                    val mode = GradeCurriculumMode.fromFirestore(value.toString()) ?: continue
                    gradeModes[g] = mode
                }
            }
        } catch (_: Exception) {
            // Use defaults on failure
        }
        return GradeCurriculumConfig(gradeModes)
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
