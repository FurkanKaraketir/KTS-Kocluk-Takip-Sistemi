package com.karaketir.coachingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.adapter.GradeOptionAdapter
import com.karaketir.coachingapp.adapter.SubjectTileAdapter
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.curriculum.GradeCurriculumConfig
import com.karaketir.coachingapp.curriculum.GradeCurriculumRepository
import com.karaketir.coachingapp.curriculum.Subjects
import com.karaketir.coachingapp.databinding.ActivityClassesBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ClassesActivity : AppCompatActivity() {

    private enum class PickerStep { GRADE, SUBJECT }

    private lateinit var binding: ActivityClassesBinding
    private var kurumKodu = 0
    private var profileGrade = 12
    private var selectedGrade = 12
    private var gradeConfig = GradeCurriculumConfig(GradeCurriculumRepository.defaultGradePrograms)
    private var currentStep = PickerStep.GRADE
    private var subjectBusy = false

    private lateinit var gradeAdapter: GradeOptionAdapter
    private lateinit var subjectAdapter: SubjectTileAdapter

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kurumKodu = intent.getStringExtra("kurumKodu").orEmpty().toIntOrNull() ?: 763455

        gradeAdapter = GradeOptionAdapter(profileGrade) { grade -> onGradeSelected(grade) }
        subjectAdapter = SubjectTileAdapter(emptyList()) { tile -> handleSubjectClick(tile.name) }

        binding.pickerRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.pickerRecyclerView.adapter = gradeAdapter

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (currentStep == PickerStep.SUBJECT) {
                        showGradeStep()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        loadProfileGrade()
    }

    private fun loadProfileGrade() {
        lifecycleScope.launch {
            try {
                val uid = auth.uid ?: return@launch
                val userSnap = db.collection("User").document(uid).get().await()
                profileGrade = userSnap.getLong("grade")?.toInt()
                    ?: userSnap.getString("grade")?.toIntOrNull()
                    ?: 12
                gradeConfig = GradeCurriculumRepository.load(db)
            } catch (_: Exception) {
                gradeConfig = GradeCurriculumConfig(GradeCurriculumRepository.defaultGradePrograms)
            }
            selectedGrade = profileGrade
            gradeAdapter = GradeOptionAdapter(profileGrade) { grade -> onGradeSelected(grade) }
            if (currentStep == PickerStep.GRADE) {
                binding.pickerRecyclerView.adapter = gradeAdapter
            }
            showGradeStep()
        }
    }

    private fun onGradeSelected(grade: Int) {
        selectedGrade = grade
        showSubjectStep()
    }

    private fun showGradeStep() {
        currentStep = PickerStep.GRADE
        binding.selectClassTitleText.setText(R.string.sinif_sec)
        binding.programSubtitleText.visibility = View.GONE
        binding.pickerRecyclerView.adapter = gradeAdapter
    }

    private fun showSubjectStep() {
        currentStep = PickerStep.SUBJECT
        val program = GradeCurriculumRepository.programForGrade(gradeConfig, selectedGrade)
        val programLabel = Subjects.programHeaderLabel(program)
        binding.selectClassTitleText.setText(R.string.ders_sec)
        binding.programSubtitleText.visibility = View.VISIBLE
        binding.programSubtitleText.text =
            "${Subjects.gradeOptionLabel(selectedGrade)} · $programLabel"

        val tiles = Subjects.studySubjectTilesForProgram(program, selectedGrade)
        subjectAdapter.submitList(tiles)
        binding.pickerRecyclerView.adapter = subjectAdapter
    }

    private fun programForSelectedGrade(): CurriculumProgram {
        return GradeCurriculumRepository.programForGrade(gradeConfig, selectedGrade)
    }

    private fun handleSubjectClick(dersAdi: String) {
        if (subjectBusy) return
        when (dersAdi) {
            "Paragraf", "Problem" -> openLegacyStudy(dersAdi, "TYT")
            "Diğer" -> openOtherStudy()
            else -> when (programForSelectedGrade()) {
                CurriculumProgram.LEGACY -> showLegacyExamTypePopup(dersAdi)
                CurriculumProgram.TYMM -> showTemaPickerAndOpen(dersAdi)
            }
        }
    }

    private fun openOtherStudy() {
        when (programForSelectedGrade()) {
            CurriculumProgram.LEGACY -> showLegacyExamTypePopup("Diğer")
            CurriculumProgram.TYMM -> openMaarifStudy(
                dersAdi = "Diğer",
                sinif = selectedGrade,
                temaId = "",
                temaAdi = "Diğer",
            )
        }
    }

    private fun showLegacyExamTypePopup(dersAdi: String) {
        val popup = PopupMenu(this, binding.selectClassAndSubjectText)
        popup.menuInflater.inflate(R.menu.subject_context, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            val studyType = when (item.itemId) {
                R.id.TYT -> "TYT"
                R.id.AYT -> "AYT"
                else -> return@setOnMenuItemClickListener false
            }
            openLegacyStudy(dersAdi, studyType)
            true
        }
        popup.show()
    }

    private fun openLegacyStudy(dersAdi: String, studyType: String) {
        startActivity(
            Intent(this, EnterStudyActivity::class.java).apply {
                putExtra("kurumKodu", kurumKodu.toString())
                putExtra("dersAdi", dersAdi)
                putExtra("studyType", studyType)
                putExtra("program", CurriculumProgram.LEGACY.firestoreValue)
                putExtra("sinif", selectedGrade)
            },
        )
    }

    private fun showTemaPickerAndOpen(dersAdi: String) {
        subjectBusy = true
        lifecycleScope.launch {
            try {
                val temalar = GradeCurriculumRepository.loadTymmThemes(db, dersAdi, selectedGrade)
                if (temalar.isEmpty()) {
                    Toast.makeText(
                        this@ClassesActivity,
                        getString(R.string.maarif_tema_yok),
                        Toast.LENGTH_LONG,
                    ).show()
                    return@launch
                }
                val names = temalar.map { it.name }.toTypedArray()
                AlertDialog.Builder(this@ClassesActivity)
                    .setTitle(R.string.tema_sec)
                    .setItems(names) { _, which ->
                        val tema = temalar[which]
                        openMaarifStudy(dersAdi, selectedGrade, tema.id, tema.name)
                    }
                    .setOnDismissListener { subjectBusy = false }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ClassesActivity,
                    e.localizedMessage ?: "Tema yüklenemedi",
                    Toast.LENGTH_SHORT,
                ).show()
                subjectBusy = false
            }
        }
    }

    private fun openMaarifStudy(dersAdi: String, sinif: Int, temaId: String, temaAdi: String) {
        startActivity(
            Intent(this, EnterStudyActivity::class.java).apply {
                putExtra("kurumKodu", kurumKodu.toString())
                putExtra("dersAdi", dersAdi)
                putExtra("program", CurriculumProgram.TYMM.firestoreValue)
                putExtra("sinif", sinif)
                putExtra("temaId", temaId)
                putExtra("temaAdi", temaAdi)
            },
        )
    }
}
