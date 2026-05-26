package com.karaketir.coachingapp.services

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.coachingapp.models.StudentRowStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Date

data class StudyTotals(
    val minutes: Int = 0,
    val questions: Int = 0,
)

object StudyQueryHelper {

    private const val PARALLEL_STUDENT_QUERIES = 8

    fun studiesInRangeQuery(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentId: String,
        start: Date,
        end: Date,
    ): Query = db.collection("School")
        .document(kurumKodu)
        .collection("Student")
        .document(studentId)
        .collection("Studies")
        .whereGreaterThan("timestamp", start)
        .whereLessThan("timestamp", end)

    fun aggregateByDers(documents: List<DocumentSnapshot>): Map<String, StudyTotals> {
        val result = mutableMapOf<String, StudyTotals>()
        for (study in documents) {
            val ders = study.getString("dersAdi").orEmpty()
            if (ders.isBlank()) continue
            val minutes = study.get("toplamCalisma")?.toString()?.toIntOrNull() ?: 0
            val questions = study.get("çözülenSoru")?.toString()?.toIntOrNull() ?: 0
            val current = result[ders] ?: StudyTotals()
            result[ders] = StudyTotals(
                minutes = current.minutes + minutes,
                questions = current.questions + questions,
            )
        }
        return result
    }

    fun totalFromDocuments(documents: List<DocumentSnapshot>): StudyTotals {
        var minutes = 0
        var questions = 0
        for (study in documents) {
            minutes += study.get("toplamCalisma")?.toString()?.toIntOrNull() ?: 0
            questions += study.get("çözülenSoru")?.toString()?.toIntOrNull() ?: 0
        }
        return StudyTotals(minutes, questions)
    }

    suspend fun fetchAggregatedByDers(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentId: String,
        start: Date,
        end: Date,
    ): Map<String, StudyTotals> {
        val snap = studiesInRangeQuery(db, kurumKodu, studentId, start, end).get().await()
        return aggregateByDers(snap.documents)
    }

    /** Excel export ve benzeri ekranlar için öğrenci başına Studies dokümanları (paralel). */
    suspend fun fetchStudiesByStudentIds(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentIds: List<String>,
        start: Date,
        end: Date,
    ): Map<String, List<DocumentSnapshot>> {
        if (studentIds.isEmpty()) return emptyMap()
        return coroutineScope {
            studentIds.chunked(PARALLEL_STUDENT_QUERIES).flatMap { chunk ->
                chunk.map { studentId ->
                    async {
                        val snap = studiesInRangeQuery(db, kurumKodu, studentId, start, end).get().await()
                        studentId to snap.documents
                    }
                }.awaitAll()
            }.toMap()
        }
    }

    /** Tüm öğrencilerin ders bazlı toplamlarını birleştirir (istatistik ekranı için). */
    suspend fun fetchClassStatsAggregates(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentIds: List<String>,
        start: Date,
        end: Date,
    ): Map<String, StudyTotals> {
        if (studentIds.isEmpty()) return emptyMap()

        val merged = mutableMapOf<String, StudyTotals>()
        coroutineScope {
            studentIds.chunked(PARALLEL_STUDENT_QUERIES).forEach { chunk ->
                chunk.map { studentId ->
                    async { fetchAggregatedByDers(db, kurumKodu, studentId, start, end) }
                }.awaitAll().forEach { perStudent ->
                    perStudent.forEach { (ders, totals) ->
                        val current = merged[ders] ?: StudyTotals()
                        merged[ders] = StudyTotals(
                            minutes = current.minutes + totals.minutes,
                            questions = current.questions + totals.questions,
                        )
                    }
                }
            }
        }
        return merged
    }

    suspend fun fetchStudentIdsForTeacher(
        db: FirebaseFirestore,
        kurumKodu: String,
        teacherId: String,
        grade: Int? = null,
    ): List<String> {
        var query = db.collection("School")
            .document(kurumKodu)
            .collection("Student")
            .whereEqualTo("teacher", teacherId)
        if (grade != null) {
            query = query.whereEqualTo("grade", grade)
        }
        return query.get().await().documents.map { it.id }
    }

    suspend fun fetchTotalMinutesPerStudent(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentIds: List<String>,
    ): Map<String, Int> {
        if (studentIds.isEmpty()) return emptyMap()
        return coroutineScope {
            studentIds.chunked(PARALLEL_STUDENT_QUERIES).flatMap { chunk ->
                chunk.map { id ->
                    async {
                        val snap = db.collection("School")
                            .document(kurumKodu)
                            .collection("Student")
                            .document(id)
                            .collection("Studies")
                            .get()
                            .await()
                        id to totalFromDocuments(snap.documents).minutes
                    }
                }.awaitAll()
            }.toMap()
        }
    }

    /** Öğretmen öğrenci listesi satırı: çalışma var mı, son değerlendirme, son rapor. */
    suspend fun fetchStudentRowStatuses(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentIds: List<String>,
        start: Date,
        end: Date,
    ): Map<String, StudentRowStatus> {
        if (studentIds.isEmpty()) return emptyMap()
        return coroutineScope {
            studentIds.chunked(PARALLEL_STUDENT_QUERIES).flatMap { chunk ->
                chunk.map { studentId ->
                    async {
                        val schoolRef = db.collection("School").document(kurumKodu)
                        val hasStudy = schoolRef.collection("Student")
                            .document(studentId)
                            .collection("Studies")
                            .whereGreaterThan("timestamp", start)
                            .whereLessThan("timestamp", end)
                            .limit(1)
                            .get()
                            .await()
                            .isEmpty
                            .not()

                        val ratingSnap = schoolRef.collection("Student")
                            .document(studentId)
                            .collection("Degerlendirme")
                            .orderBy("degerlendirmeDate", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .await()
                        val ratingDoc = ratingSnap.documents.firstOrNull()

                        val reportSnap = schoolRef.collection("LastReports")
                            .document(studentId)
                            .get()
                            .await()

                        studentId to StudentRowStatus(
                            hasStudyInRange = hasStudy,
                            ratingStars = ratingDoc?.getLong("yildizSayisi")?.toInt(),
                            ratingDate = ratingDoc?.getTimestamp("degerlendirmeDate")?.toDate(),
                            reportTimestamp = reportSnap.getTimestamp("timestamp")?.toDate(),
                        )
                    }
                }.awaitAll()
            }.toMap()
        }
    }

    /** NoDayReport koleksiyonundaki kayıt sayısı (tarih aralığında). */
    suspend fun fetchNoDayReportCounts(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentIds: List<String>,
        start: Date,
        end: Date,
    ): Map<String, Int> {
        if (studentIds.isEmpty()) return emptyMap()
        return coroutineScope {
            studentIds.chunked(PARALLEL_STUDENT_QUERIES).flatMap { chunk ->
                chunk.map { studentId ->
                    async {
                        val snap = db.collection("School")
                            .document(kurumKodu)
                            .collection("Student")
                            .document(studentId)
                            .collection("NoDayReport")
                            .whereGreaterThan("timestamp", start)
                            .whereLessThan("timestamp", end)
                            .get()
                            .await()
                        studentId to snap.size()
                    }
                }.awaitAll()
            }.toMap()
        }
    }

    /** Verilen aralıkta hiç çalışma kaydı olmayan öğrenci kimlikleri. */
    suspend fun studentIdsWithoutStudiesInRange(
        db: FirebaseFirestore,
        kurumKodu: String,
        studentIds: List<String>,
        start: Date,
        end: Date,
    ): List<String> {
        if (studentIds.isEmpty()) return emptyList()
        return coroutineScope {
            studentIds.chunked(PARALLEL_STUDENT_QUERIES).flatMap { chunk ->
                chunk.map { id ->
                    async {
                        val snap = studiesInRangeQuery(db, kurumKodu, id, start, end)
                            .limit(1)
                            .get()
                            .await()
                        if (snap.isEmpty) id else null
                    }
                }.awaitAll().filterNotNull()
            }
        }
    }
}
