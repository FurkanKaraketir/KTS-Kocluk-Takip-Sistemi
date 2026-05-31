package com.karaketir.coachingapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.os.bundleOf
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.curriculum.GradeCurriculumConfig
import com.karaketir.coachingapp.curriculum.GradeCurriculumRepository
import com.karaketir.coachingapp.curriculum.Subjects
import com.karaketir.coachingapp.curriculum.StudyLabels
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.FragmentSettingsBinding
import com.karaketir.coachingapp.services.StudentWeeklyGoalsPreferences
import com.karaketir.coachingapp.services.StudyQueryHelper
import com.karaketir.coachingapp.services.openLink
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsFragment: Fragment() {

    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        this.mainActivity = activity
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var kurumKodu = 0
    private var name = ""
    private var personType = ""
    private var grade = 0
    private var selectedCurriculumProgram: CurriculumProgram? = null
    private var gradeOffersProgramChoice = false

    private var _binding: FragmentSettingsBinding? = null
    private var isViewCreated = false

    // This property is only valid between onCreateView and
// onDestroyView.


    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewCreated = false
    }

    private fun isBindingAvailable(): Boolean {
        return isViewCreated && _binding != null
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore
        isViewCreated = true

        if (isBindingAvailable()) {
            val mBinding = binding

            db.collection("User").document(auth.uid.toString()).get()
                .addOnSuccessListener { snapshot ->
                    name = snapshot.get("nameAndSurname").toString()
                    grade = try {
                        snapshot.get("grade").toString().toInt()
                    } catch (e: Exception) {
                        0
                    }
                    personType = snapshot.get("personType").toString()
                    kurumKodu = try {
                        snapshot.get("kurumKodu").toString().toInt()
                    } catch (e: Exception) {
                        763455
                    }


                    val developerButton = mBinding.developerButtonProfile

                    developerButton.setOnClickListener {
                        mainActivity?.let { it1 ->
                            openLink(
                                "https://www.linkedin.com/in/furkankaraketir/", it1
                            )
                        }
                    }


                    val saveButton = mBinding.saveProfileButton
                    val deleteUser = mBinding.deleteAccountButton
                    val nameChangeEditText = mBinding.changeNameEditText
                    val gradeChangeEditText = mBinding.changeGradeEditText
                    val textInputChangeGrade = mBinding.TextInputChangeGrade

                    nameChangeEditText.setText(name)
                    if (personType == "Student") {
                        textInputChangeGrade.visibility = View.VISIBLE
                        gradeChangeEditText.setText(grade.toString())
                        loadCurriculumProgramUi(mBinding, grade)
                        showWeeklyGoalsSection(mBinding)
                    } else {
                        textInputChangeGrade.visibility = View.GONE
                        mBinding.curriculumProgramText.visibility = View.GONE
                        hideWeeklyGoalsSection(mBinding)
                    }



                    saveButton.setOnClickListener {

                        val alertDialog = mainActivity?.let { it1 -> AlertDialog.Builder(it1) }
                        alertDialog?.setTitle("Kaydet")
                        alertDialog?.setMessage("Değişiklikleri Kaydetmek İstediğinize Emin misiniz?")
                        alertDialog?.setPositiveButton("Kaydet") { _, _ ->

                            if (nameChangeEditText.text.toString().isNotEmpty()) {


                                db.collection("User").document(auth.uid.toString())
                                    .update("nameAndSurname", nameChangeEditText.text.toString())

                                db.collection("School").document(kurumKodu.toString())
                                    .collection(personType).document(auth.uid.toString())
                                    .update("nameAndSurname", nameChangeEditText.text.toString())


                            }
                            if (gradeChangeEditText.text.toString().isNotEmpty()) {
                                db.collection("User").document(auth.uid.toString())
                                    .update("grade", gradeChangeEditText.text.toString().toInt())

                                db.collection("School").document(kurumKodu.toString())
                                    .collection(personType).document(auth.uid.toString())
                                    .update("grade", gradeChangeEditText.text.toString().toInt())
                            }
                            selectedCurriculumProgram?.let { program ->
                                db.collection("User").document(auth.uid.toString())
                                    .update("curriculumProgram", program.firestoreValue)
                                db.collection("School").document(kurumKodu.toString())
                                    .collection(personType).document(auth.uid.toString())
                                    .update("curriculumProgram", program.firestoreValue)
                            }
                            if (personType == "Student") {
                                val activity = mainActivity ?: return@setPositiveButton
                                if (!saveWeeklyGoalsFromUi(mBinding, activity)) {
                                    return@setPositiveButton
                                }
                            }
                            Toast.makeText(mainActivity, "İşlem Başarılı!", Toast.LENGTH_SHORT)
                                .show()


                        }
                        alertDialog?.setNegativeButton("İptal") { _, _ ->

                        }
                        alertDialog?.show()

                    }

                    deleteUser.setOnClickListener {
                        val alertDialog = mainActivity?.let { it1 -> AlertDialog.Builder(it1) }
                        alertDialog?.setTitle("Hesabı Sil")
                        alertDialog?.setMessage("Hesabınızı Silmek İstediğinize Emin misiniz?\nBu İşlem Geri Alınamaz!!")
                        alertDialog?.setPositiveButton("Sil") { _, _ ->

                            db.collection("School").document(kurumKodu.toString())
                                .collection(personType).document(auth.uid.toString()).delete()
                                .addOnSuccessListener {
                                    db.collection("User").document(auth.uid.toString()).delete()
                                        .addOnSuccessListener {
                                            Firebase.auth.currentUser!!.delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        mainActivity,
                                                        "İşlem Başarılı!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                }


                        }
                        alertDialog?.setNegativeButton("İptal") { _, _ ->

                        }
                        alertDialog?.show()
                    }


                }


        }


    }

    private fun showWeeklyGoalsSection(mBinding: FragmentSettingsBinding) {
        val prefs = StudentWeeklyGoalsPreferences(requireContext())
        Log.d(
            WEEKLY_GOALS_TAG,
            "showWeeklyGoalsSection: hours=${prefs.getStudyHours()} questions=${prefs.getQuestions()} reportDays=${prefs.getReportDays()}",
        )
        mBinding.weeklyGoalsSectionTitle.visibility = View.VISIBLE
        mBinding.weeklyGoalsHintText.visibility = View.VISIBLE
        mBinding.TextInputWeeklyGoalMinutes.visibility = View.VISIBLE
        mBinding.TextInputWeeklyGoalQuestions.visibility = View.VISIBLE
        mBinding.TextInputWeeklyGoalReportDays.visibility = View.VISIBLE
        mBinding.weeklyGoalMinutesEditText.setText(prefs.getStudyHours().toString())
        mBinding.weeklyGoalQuestionsEditText.setText(prefs.getQuestions().toString())
        mBinding.weeklyGoalReportDaysEditText.setText(prefs.getReportDays().toString())
    }

    private fun hideWeeklyGoalsSection(mBinding: FragmentSettingsBinding) {
        mBinding.weeklyGoalsSectionTitle.visibility = View.GONE
        mBinding.weeklyGoalsHintText.visibility = View.GONE
        mBinding.TextInputWeeklyGoalMinutes.visibility = View.GONE
        mBinding.TextInputWeeklyGoalQuestions.visibility = View.GONE
        mBinding.TextInputWeeklyGoalReportDays.visibility = View.GONE
    }

    private fun saveWeeklyGoalsFromUi(
        mBinding: FragmentSettingsBinding,
        activity: MainActivity,
    ): Boolean {
        val hours = StudentWeeklyGoalsPreferences.parseIntField(
            mBinding.weeklyGoalMinutesEditText.text?.toString().orEmpty(),
        )
        val questions = StudentWeeklyGoalsPreferences.parseIntField(
            mBinding.weeklyGoalQuestionsEditText.text?.toString().orEmpty(),
        )
        val reportDays = StudentWeeklyGoalsPreferences.parseIntField(
            mBinding.weeklyGoalReportDaysEditText.text?.toString().orEmpty(),
        )

        if (hours == null || !StudentWeeklyGoalsPreferences.validateHours(hours)) {
            Log.w(WEEKLY_GOALS_TAG, "saveWeeklyGoalsFromUi: invalid hours=$hours")
            mBinding.TextInputWeeklyGoalMinutes.error = activity.getString(
                R.string.settings_goal_minutes_invalid,
                StudentWeeklyGoalsPreferences.MIN_HOURS,
                StudentWeeklyGoalsPreferences.MAX_HOURS,
            )
            return false
        }
        mBinding.TextInputWeeklyGoalMinutes.error = null

        if (questions == null || !StudentWeeklyGoalsPreferences.validateQuestions(questions)) {
            Log.w(WEEKLY_GOALS_TAG, "saveWeeklyGoalsFromUi: invalid questions=$questions")
            mBinding.TextInputWeeklyGoalQuestions.error = activity.getString(
                R.string.settings_goal_questions_invalid,
                StudentWeeklyGoalsPreferences.MIN_QUESTIONS,
                StudentWeeklyGoalsPreferences.MAX_QUESTIONS,
            )
            return false
        }
        mBinding.TextInputWeeklyGoalQuestions.error = null

        if (reportDays == null || !StudentWeeklyGoalsPreferences.validateReportDays(reportDays)) {
            mBinding.TextInputWeeklyGoalReportDays.error = activity.getString(
                R.string.settings_goal_report_days_invalid,
                StudentWeeklyGoalsPreferences.MIN_REPORT_DAYS,
                StudentWeeklyGoalsPreferences.MAX_REPORT_DAYS,
            )
            return false
        }
        mBinding.TextInputWeeklyGoalReportDays.error = null

        StudentWeeklyGoalsPreferences(activity).saveStudyHours(hours, questions, reportDays)
        Log.d(
            WEEKLY_GOALS_TAG,
            "saveWeeklyGoalsFromUi: saved hours=$hours questions=$questions reportDays=$reportDays",
        )
        syncWeeklyGoalsToFirestore(
            minutes = StudentWeeklyGoalsPreferences.hoursToMinutes(hours),
            questions = questions,
            reportDays = reportDays,
        )
        parentFragmentManager.setFragmentResult(
            MainActivity.REQUEST_WEEKLY_GOALS_CHANGED,
            bundleOf(),
        )
        mainActivity?.onStudentWeeklyGoalsSaved()
        return true
    }

    private fun loadCurriculumProgramUi(
        mBinding: FragmentSettingsBinding,
        studentGrade: Int,
    ) {
        lifecycleScope.launch {
            val config = try {
                GradeCurriculumRepository.load(db)
            } catch (_: Exception) {
                GradeCurriculumRepository.defaultConfig()
            }
            val profileProgram = try {
                val snap = db.collection("User").document(auth.uid.toString()).get().await()
                CurriculumProgram.fromFirestore(snap.getString("curriculumProgram"))
            } catch (_: Exception) {
                null
            }
            if (!isBindingAvailable()) return@launch
            gradeOffersProgramChoice =
                GradeCurriculumRepository.offersProgramChoice(config, studentGrade)
            val program = GradeCurriculumRepository.preferredProgram(
                config,
                studentGrade,
                profileProgram,
            )
            selectedCurriculumProgram = if (gradeOffersProgramChoice) program else null
            mBinding.curriculumProgramText.visibility = View.VISIBLE
            if (gradeOffersProgramChoice) {
                mBinding.curriculumProgramText.text = getString(
                    R.string.varsayilan_mufredat,
                ) + ": " + Subjects.programHeaderLabel(program)
                mBinding.curriculumProgramText.isClickable = true
                mBinding.curriculumProgramText.setOnClickListener {
                    showCurriculumProgramPicker(mBinding, studentGrade, config, profileProgram)
                }
            } else {
                mBinding.curriculumProgramText.setOnClickListener(null)
                mBinding.curriculumProgramText.isClickable = false
                mBinding.curriculumProgramText.text =
                    StudyLabels.programDisplayName(program, studentGrade)
            }
        }
    }

    private fun showCurriculumProgramPicker(
        mBinding: FragmentSettingsBinding,
        studentGrade: Int,
        config: GradeCurriculumConfig,
        profileProgram: CurriculumProgram?,
    ) {
        val activity = mainActivity ?: return
        val options = arrayOf(
            Subjects.programHeaderLabel(CurriculumProgram.LEGACY),
            Subjects.programHeaderLabel(CurriculumProgram.TYMM),
        )
        val current = GradeCurriculumRepository.preferredProgram(
            config,
            studentGrade,
            profileProgram ?: selectedCurriculumProgram,
        )
        val checked = if (current == CurriculumProgram.TYMM) 1 else 0
        AlertDialog.Builder(activity)
            .setTitle(R.string.varsayilan_mufredat)
            .setSingleChoiceItems(options, checked) { dialog, which ->
                selectedCurriculumProgram = if (which == 1) {
                    CurriculumProgram.TYMM
                } else {
                    CurriculumProgram.LEGACY
                }
                mBinding.curriculumProgramText.text = getString(R.string.varsayilan_mufredat) +
                    ": " + Subjects.programHeaderLabel(selectedCurriculumProgram!!)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun syncWeeklyGoalsToFirestore(minutes: Int, questions: Int, reportDays: Int) {
        val uid = auth.currentUser?.uid ?: return
        if (kurumKodu == 0) {
            Log.w(WEEKLY_GOALS_TAG, "syncWeeklyGoalsToFirestore: kurumKodu not loaded")
            return
        }
        val fields = StudentWeeklyGoalsPreferences(requireContext())
            .firestoreSyncFields(minutes, questions, reportDays)
        lifecycleScope.launch {
            try {
                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(uid)
                    .set(fields, com.google.firebase.firestore.SetOptions.merge())
                    .await()
                Log.d(
                    WEEKLY_GOALS_TAG,
                    "syncWeeklyGoalsToFirestore: uid=...${uid.takeLast(4)} fields=$fields",
                )
            } catch (error: Exception) {
                Log.w(WEEKLY_GOALS_TAG, "syncWeeklyGoalsToFirestore: failed ${error.message}")
            }
        }
    }

    companion object {
        private const val WEEKLY_GOALS_TAG = "KTS:WeeklyGoals"
    }
}