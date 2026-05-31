package com.karaketir.coachingapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.coachingapp.AllStudentsActivity
import com.karaketir.coachingapp.ClassesActivity
import com.karaketir.coachingapp.GoalsActivity
import com.karaketir.coachingapp.LoginActivity
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.MessageActivity
import com.karaketir.coachingapp.NoReportActivity
import com.karaketir.coachingapp.PreviousRatingsActivity
import com.karaketir.coachingapp.ProfileActivity
import com.karaketir.coachingapp.ProgramActivity
import com.karaketir.coachingapp.TopStudentsActivity
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.adapter.StudiesRecyclerAdapter
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.FragmentMainBinding
import com.karaketir.coachingapp.models.Student
import com.karaketir.coachingapp.models.Study
import com.karaketir.coachingapp.services.AppUpdateChecker
import com.karaketir.coachingapp.services.AppUpdatePrompt
import com.karaketir.coachingapp.services.ExcelExportHelper
import com.karaketir.coachingapp.services.StatRangeScaling
import com.karaketir.coachingapp.services.StudentWeeklyGoalsPreferences
import com.karaketir.coachingapp.services.StudyQueryHelper
import com.karaketir.coachingapp.services.clearCache
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentValues
import android.os.Environment

class MainFragment : Fragment() {

    private enum class ExcelReportMode {
        SUMMARY,
        BY_LESSON
    }

    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        this.mainActivity = activity
    }
    private lateinit var auth: FirebaseAuth
    private var kurumKodu = 763455
    private lateinit var recyclerViewPreviousStudies: RecyclerView
    private lateinit var recyclerViewMyStudents: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private var workbook = XSSFWorkbook()
    private var pendingExcelMode = ExcelReportMode.SUMMARY
    private var studyList = mutableListOf<Study>()
    private var secilenGrade = "Bütün Sınıflar"
    private var secilenZaman = "Seçiniz"
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0", "13")
    private var raporGondermeyenList = mutableListOf<Student>()
    private val zamanAraliklari = arrayOf(
        "Seçiniz",
        "Bugün",
        "Dün",
        "Bu Hafta",
        "Geçen Hafta",
        "Bu Ay",
        "Son 30 Gün",
        "Geçen Ay",
        "Tüm Zamanlar",
        "Özel"
    )

    private lateinit var filteredList: MutableList<Student>

    private var studentList = mutableListOf<Student>()
    private var grade = 0
    private var teacher = ""
    private var personType = ""
    private var name = ""
    private var weeklyGoalMinutes = MainHomeStatsBinder.defaultWeeklyGoalMinutes()
    private var weeklyGoalQuestions = MainHomeStatsBinder.defaultWeeklyGoalQuestions()
    private var weeklyGoalReportDays = MainHomeStatsBinder.defaultWeeklyReportDaysTarget()
    private var teacherWeeklyGoalMinutesSum = 0
    private lateinit var textYKSsayac: TextView
    private lateinit var mBinding: FragmentMainBinding

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isViewCreated = false

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewCreated = false
    }

    private fun isBindingAvailable(): Boolean {
        return isViewCreated && _binding != null
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val cal1 = Calendar.getInstance()
        baslangicTarihi = cal1.time
        bitisTarihi = cal1.time

        auth = Firebase.auth
        db = Firebase.firestore

        isViewCreated = true
        if (isBindingAvailable()) {
            mBinding = binding

            val nameAndSurnameTextView = mBinding.nameAndSurnameTextView
            val sayacContainer = mBinding.sayacContainer

            var sayacVisible = false

            val topStudentsButton = mBinding.topStudentsButton
            val addStudyButton = mBinding.addStudyButton
            addStudyButton.visibility = View.GONE
            val signOutButton = mBinding.signOutButton
            val previousRatingsButton = mBinding.previousRatingsButton
            val allStudentsBtn = mBinding.allStudentsBtn
            val searchEditText = mBinding.searchStudentMainActivityEditText
            val hedeflerStudentButton = mBinding.hedefStudentButton
            val gradeSpinner = mBinding.gradeSpinner
            val kocOgretmenTextView = mBinding.kocOgretmenTextView
            val teacherSpinner = mBinding.studyZamanAraligiSpinner
            val searchBarTeacher = mBinding.searchBarTeacher
            val updateLayout = mBinding.updateLayout
            val updateButton = mBinding.updateButton
            val updatePrompt = AppUpdatePrompt(
                fragment = this,
                updateOverlay = updateLayout,
                updateTitle = mBinding.updateText,
                updateVersionInfo = mBinding.updateVersionInfo,
                updateReleaseNotes = mBinding.updateReleaseNotes,
                updateButton = updateButton,
                signOutButton = signOutButton,
            )
            val excelButton = mBinding.excelButton
            val dersProgramiButton = mBinding.dersProgramiButton
            val noReportButton = mBinding.noReportButton
            textYKSsayac = mBinding.YKSsayac
            val teacherCardView = mBinding.teacherCardView
            val messageButton = mBinding.sendMessageButton


            val customDateLayout = mBinding.customDateLayout
            val baslangicTarihiTextView = mBinding.baslangicTarihiTextView
            val bitisTarihiTextView = mBinding.bitisTarihiTextView


            var kocID = ""

            dersProgramiButton.setOnClickListener {
                val newIntent = Intent(activity, ProgramActivity::class.java)
                newIntent.putExtra("personType", personType)
                newIntent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(newIntent)
            }



            viewLifecycleOwner.lifecycleScope.launch {
                val status = AppUpdateChecker.checkForUpdate(db)
                if (isBindingAvailable()) {
                    updatePrompt.show(status)
                }
            }

            nameAndSurnameTextView.setOnClickListener {
                val newIntent = Intent(activity, ProfileActivity::class.java)
                newIntent.putExtra("personType", personType)
                newIntent.putExtra("name", name)
                newIntent.putExtra("grade", grade.toString())
                newIntent.putExtra("kurumKodu", kurumKodu.toString())
                startActivity(newIntent)
            }

            recyclerViewPreviousStudies = mBinding.previousStudies
            recyclerViewMyStudents = mBinding.myStudents

            hedeflerStudentButton.setOnClickListener {
                val intent2 = Intent(activity, GoalsActivity::class.java)
                intent2.putExtra("personType", personType)
                intent2.putExtra("studentID", auth.currentUser?.uid.toString())
                intent2.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(intent2)
            }

            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    filteredList = mutableListOf()
                    if (p0.toString() != "") {
                        for (item in studentList) {
                            if (item.studentName.lowercase(Locale.getDefault())
                                    .contains(p0.toString().lowercase(Locale.getDefault()))
                            ) {
                                filteredList.add(item)
                            }
                        }
                        setupStudentRecyclerView(filteredList)
                    } else {
                        setupStudentRecyclerView(studentList)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })

            try {
                db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                    name = it.get("nameAndSurname").toString()
                    nameAndSurnameTextView.text = name

                    kurumKodu = try {
                        it.get("kurumKodu")?.toString()?.toInt()!!
                    } catch (eP: Exception) {
                        763455
                    }

                    FirebaseMessaging.getInstance().subscribeToTopic(kurumKodu.toString())


                    if (it.get("personType").toString() == "Student") {

                        personType = "Student"
                        grade = it.get("grade").toString().toInt()
                        updateTime(grade)
                        teacher = it.get("teacher").toString()
                        dersProgramiButton.visibility = View.VISIBLE
                        addStudyButton.visibility = View.VISIBLE
                        kocOgretmenTextView.visibility = View.VISIBLE
                        excelButton.visibility = View.GONE
                        teacherCardView.visibility = View.GONE
                        customDateLayout.visibility = View.GONE


                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(auth.uid.toString()).get().addOnSuccessListener { student ->
                                kocID = student.get("teacher").toString()
                                if (kocID.isEmpty()) {
                                    kocOgretmenTextView.text = "Koç Öğretmenin Bulunmuyor"
                                } else {

                                    FirebaseMessaging.getInstance().subscribeToTopic(kocID)

                                    db.collection("School").document(kurumKodu.toString())
                                        .collection("Teacher").document(kocID).get()
                                        .addOnSuccessListener { teacher ->
                                            kocOgretmenTextView.text =
                                                "Koç Öğretmenin:\n" + teacher.get("nameAndSurname")
                                                    .toString()
                                        }

                                }
                            }
                        searchBarTeacher.visibility = View.GONE
                        gradeSpinner.visibility = View.GONE
                        topStudentsButton.visibility = View.GONE
                        teacherSpinner.visibility = View.GONE
                        searchEditText.visibility = View.GONE
                        previousRatingsButton.visibility = View.VISIBLE
                        hedeflerStudentButton.visibility = View.VISIBLE
                        recyclerViewPreviousStudies.visibility = View.VISIBLE
                        allStudentsBtn.visibility = View.GONE
                        noReportButton.visibility = View.GONE
                        messageButton.visibility = View.GONE
                        setHomeStatsVisibility(studentVisible = true)
                        setupHomeStatsCollapse(studentVisible = true)

                        applyLocalWeeklyGoalPreferences()
                        loadWeeklyGoalsForStudent()
                        setupStudentStatsGoalsEditor()
                        refreshStudentHomeStats()

                        val cal: Calendar = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -7)
                        val baslangicTarihi = cal.time



                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(auth.uid.toString()).collection("Studies")
                            .whereGreaterThan("timestamp", baslangicTarihi)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener { it1, _ ->
                                studyList.clear()
                                val documents = it1!!.documents
                                for (document in documents) {
                                    studyList.add(
                                        Study.fromDocument(document, auth.uid.toString())
                                    )
                                }

                                setupStudyRecyclerView(studyList)

                                recyclerViewPreviousStudiesAdapter.notifyDataSetChanged()
                                applyLocalWeeklyGoalPreferences()
                                refreshStudentHomeStats()

                            }


                    } else if (it.get("personType").toString() == "Teacher") {
                        teacher = ""
                        personType = "Teacher"
                        dersProgramiButton.visibility = View.GONE
                        customDateLayout.visibility = View.VISIBLE
                        hedeflerStudentButton.visibility = View.GONE
                        excelButton.visibility = View.VISIBLE
                        searchEditText.visibility = View.VISIBLE
                        topStudentsButton.visibility = View.VISIBLE
                        teacherSpinner.visibility = View.VISIBLE
                        kocOgretmenTextView.visibility = View.GONE
                        recyclerViewMyStudents.visibility = View.VISIBLE
                        searchBarTeacher.visibility = View.VISIBLE
                        noReportButton.visibility = View.VISIBLE
                        previousRatingsButton.visibility = View.GONE
                        allStudentsBtn.visibility = View.VISIBLE
                        addStudyButton.visibility = View.GONE
                        gradeSpinner.visibility = View.VISIBLE
                        teacherCardView.visibility = View.VISIBLE
                        messageButton.visibility = View.VISIBLE
                        setHomeStatsVisibility(studentVisible = false)
                        setupHomeStatsCollapse(studentVisible = false)
                        refreshTeacherHomeStats()
                        updateTime(grade)

                        messageButton.setOnClickListener {
                            val newIntent = Intent(mainActivity, MessageActivity::class.java)
                            newIntent.putExtra("kurumKodu", kurumKodu.toString())
                            this.startActivity(newIntent)
                        }

                        excelButton.setOnClickListener {
                            if (secilenZaman == "Seçiniz") {
                                Toast.makeText(
                                    mainActivity,
                                    "Lütfen bir zaman aralığı seçiniz",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            val options = arrayOf(
                                getString(R.string.excel_rapor_ozet),
                                getString(R.string.excel_rapor_ders_bazli)
                            )
                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.excel_rapor_turu)
                                .setItems(options) { _, which ->
                                    val mode = if (which == 0) {
                                        ExcelReportMode.SUMMARY
                                    } else {
                                        ExcelReportMode.BY_LESSON
                                    }
                                    startExcelExport(mode)
                                }
                                .show()
                        }


                        val tarihAdapter = mainActivity?.let { it1 ->
                            ArrayAdapter(
                                it1, android.R.layout.simple_spinner_item, zamanAraliklari
                            )
                        }

                        tarihAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        teacherSpinner.adapter = tarihAdapter
                        teacherSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                @SuppressLint("SimpleDateFormat")
                                override fun onItemSelected(
                                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                                ) {
                                    secilenZaman = zamanAraliklari[p2]
                                    if (secilenZaman == "Özel") {
                                        customDateLayout.visibility = View.VISIBLE
                                        val cal = Calendar.getInstance()
                                        val dateSetListener =
                                            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                                cal.set(Calendar.YEAR, year)
                                                cal.set(Calendar.MONTH, month)
                                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                baslangicTarihi = cal.time
                                                baslangicTarihiTextView.text =
                                                    "Başlangıç Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                                        baslangicTarihi
                                                    )
                                                getData()
                                            }
                                        val dateSetListener2 =
                                            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                                cal.set(Calendar.YEAR, year)
                                                cal.set(Calendar.MONTH, month)
                                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                bitisTarihi = cal.time
                                                bitisTarihiTextView.text =
                                                    "Bitiş Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                                        bitisTarihi
                                                    )
                                                getData()
                                            }
                                        baslangicTarihiTextView.setOnClickListener {
                                            mainActivity?.let { it1 ->
                                                DatePickerDialog(
                                                    it1,
                                                    dateSetListener,
                                                    cal.get(Calendar.YEAR),
                                                    cal.get(Calendar.MONTH),
                                                    cal.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            }
                                        }
                                        bitisTarihiTextView.setOnClickListener {
                                            mainActivity?.let { it1 ->
                                                DatePickerDialog(
                                                    it1,
                                                    dateSetListener2,
                                                    cal.get(Calendar.YEAR),
                                                    cal.get(Calendar.MONTH),
                                                    cal.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            }
                                        }

                                    } else {
                                        customDateLayout.visibility = View.GONE
                                        var cal = Calendar.getInstance()
                                        cal[Calendar.HOUR_OF_DAY] =
                                            0 // ! clear would not reset the hour of day !

                                        cal.clear(Calendar.MINUTE)
                                        cal.clear(Calendar.SECOND)
                                        cal.clear(Calendar.MILLISECOND)

                                        when (secilenZaman) {
                                            "Seçiniz" -> {
                                                baslangicTarihi = cal.time
                                                bitisTarihi = cal.time

                                            }

                                            "Bugün" -> {
                                                baslangicTarihi = cal.time


                                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                                bitisTarihi = cal.time
                                            }

                                            "Dün" -> {
                                                bitisTarihi = cal.time

                                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                                baslangicTarihi = cal.time
                                            }

                                            "Bu Hafta" -> {
                                                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                                                baslangicTarihi = cal.time


                                                cal.add(Calendar.WEEK_OF_YEAR, 1)
                                                bitisTarihi = cal.time

                                            }

                                            "Geçen Hafta" -> {
                                                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                                                bitisTarihi = cal.time


                                                cal.add(Calendar.DAY_OF_YEAR, -7)
                                                baslangicTarihi = cal.time


                                            }

                                            "Son 30 Gün" -> {
                                                cal = Calendar.getInstance()

                                                bitisTarihi = cal.time

                                                cal.add(Calendar.DAY_OF_YEAR, -30)

                                                baslangicTarihi = cal.time

                                            }

                                            "Bu Ay" -> {

                                                cal = Calendar.getInstance()
                                                cal[Calendar.HOUR_OF_DAY] =
                                                    0 // ! clear would not reset the hour of day !

                                                cal.clear(Calendar.MINUTE)
                                                cal.clear(Calendar.SECOND)
                                                cal.clear(Calendar.MILLISECOND)

                                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                                baslangicTarihi = cal.time


                                                cal.add(Calendar.MONTH, 1)
                                                bitisTarihi = cal.time


                                            }

                                            "Geçen Ay" -> {
                                                cal = Calendar.getInstance()
                                                cal[Calendar.HOUR_OF_DAY] =
                                                    0 // ! clear would not reset the hour of day !

                                                cal.clear(Calendar.MINUTE)
                                                cal.clear(Calendar.SECOND)
                                                cal.clear(Calendar.MILLISECOND)

                                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                                bitisTarihi = cal.time


                                                cal.add(Calendar.MONTH, -1)
                                                baslangicTarihi = cal.time

                                            }

                                            "Tüm Zamanlar" -> {
                                                cal.set(
                                                    1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK
                                                )
                                                baslangicTarihi = cal.time


                                                cal.set(
                                                    2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK
                                                )
                                                bitisTarihi = cal.time

                                            }
                                        }
                                        baslangicTarihiTextView.text =
                                            "Başlangıç Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                                baslangicTarihi
                                            )

                                        bitisTarihiTextView.text =
                                            "Bitiş Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                                bitisTarihi
                                            )

                                        getData()

                                    }
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                }

                            }

                        val gradeAdapter = mainActivity?.let { it1 ->
                            ArrayAdapter(
                                it1, android.R.layout.simple_spinner_item, gradeList
                            )
                        }
                        gradeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        gradeSpinner.adapter = gradeAdapter
                        gradeSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                                ) {
                                    secilenGrade = gradeList[p2]
                                    getData()
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {

                                }

                            }

                    }



                    TransitionManager.beginDelayedTransition(sayacContainer)
                    sayacVisible = !sayacVisible

                }

            } catch (e: Exception) {
                println(e.localizedMessage)
            }



            previousRatingsButton.setOnClickListener {
                val intent = Intent(activity, PreviousRatingsActivity::class.java)
                intent.putExtra("personType", personType)
                intent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(intent)
            }

            allStudentsBtn.setOnClickListener {
                val intent = Intent(activity, AllStudentsActivity::class.java)
                intent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(intent)
            }

            addStudyButton.setOnClickListener {
                val intent = Intent(activity, ClassesActivity::class.java)
                intent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(intent)
            }

            topStudentsButton.setOnClickListener {
                val intent = Intent(activity, TopStudentsActivity::class.java)
                intent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(intent)
            }

            noReportButton.setOnClickListener {
                if (studentList.isEmpty()) {
                    Toast.makeText(mainActivity, "Öğrenci listesi boş", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                mBinding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    try {
                        val withoutIds = StudyQueryHelper.studentIdsWithoutStudiesInRange(
                            db,
                            kurumKodu.toString(),
                            studentList.map { it.id },
                            baslangicTarihi,
                            bitisTarihi,
                        ).toSet()
                        raporGondermeyenList.clear()
                        raporGondermeyenList.addAll(studentList.filter { it.id in withoutIds })

                        val myIntent = Intent(activity, NoReportActivity::class.java).apply {
                            putExtra("kurumKodu", kurumKodu.toString())
                            putExtra("grade", secilenGrade)
                            putExtra("baslangicTarihi", baslangicTarihi)
                            putExtra("bitisTarihi", bitisTarihi)
                            putExtra("list", ArrayList(raporGondermeyenList))
                            putExtra("secilenZaman", secilenZaman)
                        }
                        mainActivity?.startActivity(myIntent)
                    } catch (e: Exception) {
                        println(e.localizedMessage)
                        Toast.makeText(
                            mainActivity,
                            "Rapor listesi hazırlanamadı",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } finally {
                        if (isBindingAvailable()) {
                            mBinding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }

            signOutButton.setOnClickListener {

                val signOutAlertDialog = mainActivity?.let { it1 -> AlertDialog.Builder(it1) }
                signOutAlertDialog?.setTitle("Çıkış Yap")
                signOutAlertDialog?.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
                signOutAlertDialog?.setPositiveButton("Çıkış") { _, _ ->

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(auth.uid.toString())
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(kurumKodu.toString())
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(kocID)

                    signOut()
                }
                signOutAlertDialog?.setNegativeButton("İptal") { _, _ ->

                }
                signOutAlertDialog?.show()

            }


        }


    }


    private fun setupStudentRecyclerView(list: MutableList<Student>) {
        val layoutManager = LinearLayoutManager(mainActivity)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(list, kurumKodu, baslangicTarihi, bitisTarihi, secilenZaman)

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter

    }

    private fun setupStudyRecyclerView(list: List<Study>) {
        val layoutManager = GridLayoutManager(mainActivity, 2)

        recyclerViewPreviousStudies.layoutManager = layoutManager
        recyclerViewPreviousStudiesAdapter =
            StudiesRecyclerAdapter(list, "Bu Hafta", kurumKodu, personType)
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun getData() {

        studentList.clear()
        setupStudentRecyclerView(studentList)

        recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

        mBinding.studentCountTextView.text = "Öğrenci Sayısı: "
        mBinding.progressBar.visibility = View.VISIBLE

        if (secilenGrade == "Bütün Sınıflar") {
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString()).addSnapshotListener { documents, _ ->
                    studentList.clear()
                    if (documents != null) {
                        for (document in documents) {
                            val studentGrade = document.get("grade").toString().toInt()
                            val studentName = document.get("nameAndSurname").toString()
                            val teacher = document.get("teacher").toString()
                            val id = document.get("id").toString()
                            val currentStudent = Student(studentName, teacher, id, studentGrade)
                            studentList.add(currentStudent)

                        }
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.studentCountTextView.text = "Öğrenci Sayısı: " + studentList.size
                    }
                    studentList.sortBy { a ->
                        a.studentName
                    }
                    setupStudentRecyclerView(studentList)

                    recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()
                    refreshStudentRowStatuses()
                    refreshTeacherHomeStats()

                }
        } else {
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString())
                .whereEqualTo("grade", secilenGrade.toInt()).addSnapshotListener { documents, _ ->

                    studentList.clear()
                    if (documents != null) {
                        for (document in documents) {
                            val studentGrade = document.get("grade").toString().toInt()
                            val studentName = document.get("nameAndSurname").toString()
                            val teacher = document.get("teacher").toString()
                            val id = document.get("id").toString()
                            val currentStudent = Student(studentName, teacher, id, studentGrade)
                            studentList.add(currentStudent)

                        }
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.studentCountTextView.text = "Öğrenci Sayısı: " + studentList.size
                    }
                    studentList.sortBy { a ->
                        a.studentName
                    }
                    setupStudentRecyclerView(studentList)

                    recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()
                    refreshStudentRowStatuses()
                    refreshTeacherHomeStats()

                }
        }
    }

    private fun setHomeStatsVisibility(studentVisible: Boolean) {
        if (!isBindingAvailable()) return
        mBinding.root.findViewById<View>(R.id.studentHomeStats)?.visibility =
            if (studentVisible) View.VISIBLE else View.GONE
        mBinding.root.findViewById<View>(R.id.teacherHomeStats)?.visibility =
            if (studentVisible) View.GONE else View.VISIBLE
    }

    fun reloadStudentWeeklyGoalsFromPreferences() {
        Log.d(WEEKLY_GOALS_TAG, "reloadStudentWeeklyGoalsFromPreferences: personType=$personType")
        if (personType != "Student" || !isBindingAvailable()) {
            Log.d(WEEKLY_GOALS_TAG, "reloadStudentWeeklyGoalsFromPreferences: skipped (not student or no binding)")
            return
        }
        applyLocalWeeklyGoalPreferences()
        refreshStudentHomeStats()
    }

    private fun applyLocalWeeklyGoalPreferences() {
        val ctx = context ?: run {
            Log.w(WEEKLY_GOALS_TAG, "applyLocalWeeklyGoalPreferences: context null")
            return
        }
        val prefs = StudentWeeklyGoalsPreferences(ctx)
        weeklyGoalMinutes = prefs.getMinutes()
        weeklyGoalQuestions = prefs.getQuestions()
        weeklyGoalReportDays = prefs.getReportDays()
        Log.d(
            WEEKLY_GOALS_TAG,
            "applyLocalWeeklyGoalPreferences: source=student_prefs " +
                "minutes=$weeklyGoalMinutes questions=$weeklyGoalQuestions reportDays=$weeklyGoalReportDays",
        )
        syncWeeklyGoalsToFirestore()
    }

    private fun setupStudentStatsGoalsEditor() {
        if (!isBindingAvailable()) return
        mBinding.root.findViewById<View>(R.id.editWeeklyGoalsButton)?.setOnClickListener {
            mainActivity?.openStudentSettings()
        }
    }

    private fun setupHomeStatsCollapse(studentVisible: Boolean) {
        if (!isBindingAvailable()) return
        if (studentVisible) {
            mBinding.root.findViewById<View>(R.id.studentHomeStats)?.let {
                MainHomeStatsBinder.setupStatsCollapse(it, MainHomeStatsBinder.HomeStatsRole.STUDENT)
            }
        } else {
            mBinding.root.findViewById<View>(R.id.teacherHomeStats)?.let {
                MainHomeStatsBinder.setupStatsCollapse(it, MainHomeStatsBinder.HomeStatsRole.TEACHER)
            }
        }
    }

    private fun loadWeeklyGoalsForStudent() {
        val uid = auth.currentUser?.uid ?: run {
            Log.w(WEEKLY_GOALS_TAG, "loadWeeklyGoalsForStudent: no auth uid")
            return
        }
        Log.d(WEEKLY_GOALS_TAG, "loadWeeklyGoalsForStudent: uid=...${truncatedUid(uid)} kurumKodu=$kurumKodu")
        applyLocalWeeklyGoalPreferences()
        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(uid).collection("HaftalikHedefler")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(WEEKLY_GOALS_TAG, "loadWeeklyGoalsForStudent: Firestore error=${error.message}")
                }
                if (snapshot == null || !isBindingAvailable()) {
                    Log.w(
                        WEEKLY_GOALS_TAG,
                        "loadWeeklyGoalsForStudent: snapshot null=${snapshot == null} bindingAvailable=${isBindingAvailable()}",
                    )
                    return@addSnapshotListener
                }
                var subjectGoalMinutes = 0
                var subjectGoalQuestions = 0
                for (document in snapshot) {
                    val docMinutes = document.get("toplamCalisma")?.toString()?.toIntOrNull() ?: 0
                    val docQuestions = document.get("çözülenSoru")?.toString()?.toIntOrNull() ?: 0
                    subjectGoalMinutes += docMinutes
                    subjectGoalQuestions += docQuestions
                    Log.d(
                        WEEKLY_GOALS_TAG,
                        "HaftalikHedefler doc=...${document.id.takeLast(4)} " +
                            "toplamCalisma=$docMinutes çözülenSoru=$docQuestions",
                    )
                }
                Log.d(
                    WEEKLY_GOALS_TAG,
                    "loadWeeklyGoalsForStudent: docCount=${snapshot.size()} " +
                        "subjectGoalMinutes=$subjectGoalMinutes subjectGoalQuestions=$subjectGoalQuestions " +
                        "(per-subject teacher goals; not used for main stats rings)",
                )
                applyLocalWeeklyGoalPreferences()
                Log.d(
                    WEEKLY_GOALS_TAG,
                    "loadWeeklyGoalsForStudent: main stats source=student_prefs " +
                        "minutes=$weeklyGoalMinutes questions=$weeklyGoalQuestions reportDays=$weeklyGoalReportDays",
                )
                refreshStudentHomeStats()
            }
    }

    private fun syncWeeklyGoalsToFirestore() {
        if (personType != "Student") return
        val uid = auth.currentUser?.uid ?: return
        val ctx = context ?: return
        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(uid)
            .set(
                StudentWeeklyGoalsPreferences(ctx).firestoreSyncFields(
                    weeklyGoalMinutes,
                    weeklyGoalQuestions,
                    weeklyGoalReportDays,
                ),
                com.google.firebase.firestore.SetOptions.merge(),
            )
            .addOnSuccessListener {
                Log.d(
                    WEEKLY_GOALS_TAG,
                    "syncWeeklyGoalsToFirestore: uid=...${truncatedUid(uid)} " +
                        "minutes=$weeklyGoalMinutes questions=$weeklyGoalQuestions reportDays=$weeklyGoalReportDays",
                )
            }
            .addOnFailureListener { error ->
                Log.w(WEEKLY_GOALS_TAG, "syncWeeklyGoalsToFirestore: failed ${error.message}")
            }
    }

    private fun refreshStudentHomeStats() {
        if (personType != "Student" || !isBindingAvailable()) return
        Log.d(
            WEEKLY_GOALS_TAG,
            "refreshStudentHomeStats: source=student_prefs studyCount=${studyList.size} targets " +
                "minutes=$weeklyGoalMinutes questions=$weeklyGoalQuestions reportDays=$weeklyGoalReportDays",
        )
        val rangeDays = StatRangeScaling.DAYS_PER_WEEK
        val stats = MainHomeStatsBinder.computeStudentStats(
            studyList,
            weeklyGoalMinutes,
            weeklyGoalQuestions,
            weeklyGoalReportDays,
            rangeDays,
        )
        val root = mBinding.root
        MainHomeStatsBinder.bindStudentRings(
            MainHomeStatsBinder.ringViewsFromInclude(root.findViewById(R.id.studentRingStudy)),
            MainHomeStatsBinder.ringViewsFromInclude(root.findViewById(R.id.studentRingQuestions)),
            MainHomeStatsBinder.ringViewsFromInclude(root.findViewById(R.id.studentRingReport)),
            stats,
            resources,
            rangeDays,
        )
    }

    private fun refreshTeacherHomeStats(
        reportingCount: Int? = null,
        ratingCount: Int? = null,
        classStudyMinutes: Int? = null,
        weeklyGoalMinutesSum: Int? = null,
    ) {
        if (personType != "Teacher" || !isBindingAvailable()) return

        if (weeklyGoalMinutesSum != null) {
            teacherWeeklyGoalMinutesSum = weeklyGoalMinutesSum
        }

        val timeSelected = secilenZaman != "Seçiniz"
        val stats = MainHomeStatsBinder.computeTeacherStats(
            studentCount = studentList.size,
            reportingCount = reportingCount ?: 0,
            ratingCount = ratingCount ?: 0,
            classStudyMinutes = classStudyMinutes ?: 0,
            weeklyGoalMinutesSum = teacherWeeklyGoalMinutesSum,
            rangeDays = ExcelExportHelper.daysBetween(baslangicTarihi, bitisTarihi),
            timeRangeSelected = timeSelected,
        )
        val root = mBinding.root
        MainHomeStatsBinder.bindTeacherRings(
            MainHomeStatsBinder.ringViewsFromInclude(root.findViewById(R.id.teacherRingReporting)),
            MainHomeStatsBinder.ringViewsFromInclude(root.findViewById(R.id.teacherRingClassStudy)),
            MainHomeStatsBinder.ringViewsFromInclude(root.findViewById(R.id.teacherRingRating)),
            stats,
            resources,
        )
    }

    private fun refreshStudentRowStatuses() {
        if (personType != "Teacher" || studentList.isEmpty() || secilenZaman == "Seçiniz") {
            teacherWeeklyGoalMinutesSum = 0
            refreshTeacherHomeStats()
            return
        }
        if (!::recyclerViewMyStudentsRecyclerAdapter.isInitialized) {
            return
        }
        lifecycleScope.launch {
            try {
                val studentIds = studentList.map { it.id }
                val statuses = StudyQueryHelper.fetchStudentRowStatuses(
                    db,
                    kurumKodu.toString(),
                    studentIds,
                    baslangicTarihi,
                    bitisTarihi,
                )
                val studiesByStudent = StudyQueryHelper.fetchStudiesByStudentIds(
                    db,
                    kurumKodu.toString(),
                    studentIds,
                    baslangicTarihi,
                    bitisTarihi,
                )
                val weeklyGoalsByStudent = StudyQueryHelper.fetchWeeklyGoalMinutesByStudentIds(
                    db,
                    kurumKodu.toString(),
                    studentIds,
                )
                val weeklyGoalMinutesSum = weeklyGoalsByStudent.values.sum()
                Log.d(
                    WEEKLY_GOALS_TAG,
                    "refreshStudentRowStatuses: students=${studentIds.size} " +
                        "weeklyGoalMinutesSum=$weeklyGoalMinutesSum " +
                        "perStudent=${weeklyGoalsByStudent.mapValues { it.value }}",
                )
                val reportingCount = statuses.values.count { it.hasStudyInRange }
                val ratingCount = statuses.values.count { it.ratingStars != null }
                var classMinutes = 0
                for (documents in studiesByStudent.values) {
                    classMinutes += StudyQueryHelper.totalFromDocuments(documents).minutes
                }
                if (!isBindingAvailable() || !::recyclerViewMyStudentsRecyclerAdapter.isInitialized) {
                    return@launch
                }
                recyclerViewMyStudentsRecyclerAdapter.updateRowStatuses(statuses)
                refreshTeacherHomeStats(
                    reportingCount = reportingCount,
                    ratingCount = ratingCount,
                    classStudyMinutes = classMinutes,
                    weeklyGoalMinutesSum = weeklyGoalMinutesSum,
                )
            } catch (e: Exception) {
                println(e.localizedMessage)
                if (isBindingAvailable()) {
                    refreshTeacherHomeStats()
                }
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        this.startActivity(intent)
        mainActivity?.finish()
    }


    @SuppressLint("SetTextI18n")
    fun updateTime(grade: Int) {

        val currentDate = Calendar.getInstance()

        val eventDate = Calendar.getInstance()
        when (grade) {
            12, 0, 13 -> {
                db.collection("Dates").document("date12").get().addOnSuccessListener {
                    eventDate[Calendar.YEAR] = it.get("year").toString().toInt()
                    eventDate[Calendar.MONTH] = it.get("month").toString().toInt() - 1
                    eventDate[Calendar.DAY_OF_MONTH] = it.get("day").toString().toInt()

                    eventDate[Calendar.HOUR_OF_DAY] = 10
                    eventDate[Calendar.MINUTE] = 15
                    eventDate[Calendar.SECOND] = 0


                    val diff = eventDate.timeInMillis - currentDate.timeInMillis

                    val days = diff / (24 * 60 * 60 * 1000)
                    val years = days / 365
                    val remainingDays = days - (years * 365)
                    val ay = remainingDays / 30




                    if (personType != "Student") {
                        if (currentDate[Calendar.MONTH] == 10 && currentDate[Calendar.DAY_OF_MONTH] == 24) {
                            textYKSsayac.text = "Öğretmenler Gününüz Kutlu Olsun"
                            mBinding.YKSsayacCardView.visibility = View.VISIBLE
                            textYKSsayac.setShadowLayer(0f, 0f, 0f, Color.WHITE)
                            textYKSsayac.setTextColor(Color.WHITE)

                        } else {


                            //date cant be negative
                            if (remainingDays < 0) {
                                textYKSsayac.text = "Son: 0 Yıl, 0 Ay, 0 Gün"
                                mBinding.YKSsayacCardView.visibility = View.VISIBLE
                            } else {
                                textYKSsayac.text =
                                    "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                                mBinding.YKSsayacCardView.visibility = View.VISIBLE
                            }
                        }

                    } else {
                        //date cant be negative
                        if (remainingDays < 0) {
                            textYKSsayac.text = "Son: 0 Yıl, 0 Ay, 0 Gün"
                            mBinding.YKSsayacCardView.visibility = View.VISIBLE
                        } else {
                            textYKSsayac.text = "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                            mBinding.YKSsayacCardView.visibility = View.VISIBLE
                        }

                    }


                }

            }

            11 -> {
                db.collection("Dates").document("date11").get().addOnSuccessListener {

                    eventDate[Calendar.YEAR] = it.get("year").toString().toInt()
                    eventDate[Calendar.MONTH] = it.get("month").toString().toInt() - 1
                    eventDate[Calendar.DAY_OF_MONTH] = it.get("day").toString().toInt()

                    eventDate[Calendar.HOUR_OF_DAY] = 10
                    eventDate[Calendar.MINUTE] = 15
                    eventDate[Calendar.SECOND] = 0


                    val diff = eventDate.timeInMillis - currentDate.timeInMillis

                    val days = diff / (24 * 60 * 60 * 1000)
                    val years = days / 365
                    val remainingDays = days - (years * 365)
                    val ay = remainingDays / 30

                    textYKSsayac.text = "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                    mBinding.YKSsayacCardView.visibility = View.VISIBLE


                }
            }

            10 -> {
                db.collection("Dates").document("date10").get().addOnSuccessListener {

                    eventDate[Calendar.YEAR] = it.get("year").toString().toInt()
                    eventDate[Calendar.MONTH] = it.get("month").toString().toInt() - 1
                    eventDate[Calendar.DAY_OF_MONTH] = it.get("day").toString().toInt()

                    eventDate[Calendar.HOUR_OF_DAY] = 10
                    eventDate[Calendar.MINUTE] = 15
                    eventDate[Calendar.SECOND] = 0

                    val diff = eventDate.timeInMillis - currentDate.timeInMillis

                    val days = diff / (24 * 60 * 60 * 1000)
                    val years = days / 365
                    val remainingDays = days - (years * 365)
                    val ay = remainingDays / 30

                    textYKSsayac.text = "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                    mBinding.YKSsayacCardView.visibility = View.VISIBLE


                }
            }

            9 -> {
                db.collection("Dates").document("date9").get().addOnSuccessListener {

                    eventDate[Calendar.YEAR] = it.get("year").toString().toInt()
                    eventDate[Calendar.MONTH] = it.get("month").toString().toInt() - 1
                    eventDate[Calendar.DAY_OF_MONTH] = it.get("day").toString().toInt()

                    eventDate[Calendar.HOUR_OF_DAY] = 10
                    eventDate[Calendar.MINUTE] = 15
                    eventDate[Calendar.SECOND] = 0

                    val diff = eventDate.timeInMillis - currentDate.timeInMillis

                    val days = diff / (24 * 60 * 60 * 1000)
                    val years = days / 365
                    val remainingDays = days - (years * 365)
                    val ay = remainingDays / 30

                    textYKSsayac.text = "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                    mBinding.YKSsayacCardView.visibility = View.VISIBLE


                }
            }
        }


    }

    override fun onResume() {

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            this.startActivity(intent)
        }

        super.onResume()
        if (personType == "Student" && isBindingAvailable()) {
            mainActivity?.consumeStudentWeeklyGoalsDirty()
            applyLocalWeeklyGoalPreferences()
            refreshStudentHomeStats()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                saveExcelWorkbook(pendingExcelMode)
            } else {
                // Permission not granted, handle accordingly
            }
        }

    private fun askForPermissions() {
        if (mainActivity?.let {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveExcelWorkbook(pendingExcelMode)
        }
    }

    private fun startExcelExport(mode: ExcelReportMode) {
        pendingExcelMode = mode
        val sheetName = if (mode == ExcelReportMode.SUMMARY) "Özet" else "Ders Bazlı"
        workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet(sheetName)
        val headerStyle = ExcelExportHelper.createHeaderStyle(workbook)

        val reportContext = ExcelExportHelper.ReportContext(
            title = getString(R.string.excel_rapor_ogrenci_calisma),
            gradeLabel = secilenGrade,
            timeRangeLabel = secilenZaman,
            startDate = baslangicTarihi,
            endDate = bitisTarihi,
            extraLines = listOf("Öğrenci sayısı: ${studentList.size}")
        )

        val dataStartRow = if (mode == ExcelReportMode.SUMMARY) {
            val headers = arrayOf(
                "Öğrenci Adı",
                "Sınıf",
                "Toplam Çalışma Süresi (dk)",
                "Toplam Çalışma Süresi (saat)",
                "Günlük Ortalama Çalışma (saat)",
                "Toplam Soru Sayısı",
                "Günlük Ortalama Soru",
                "Toplam Çalışma Sayısı",
                "Günlük Ortalama Çalışma Sayısı"
            )
            val infoEnd = ExcelExportHelper.writeReportInfoBlock(sheet, reportContext, headers.size)
            ExcelExportHelper.writeStyledHeaderRow(sheet, headers, headerStyle, infoEnd)
        } else {
            // Ders sütunları veri yüklendikten sonra yazılır; üst bilgi geniş sütun aralığı kullanır.
            ExcelExportHelper.writeReportInfoBlock(sheet, reportContext, 32)
        }

        mainActivity?.let { clearCache(it) }
        Toast.makeText(activity, getString(R.string.excel_lutfen_bekleyin), Toast.LENGTH_SHORT).show()

        addData(sheet, mode, dataStartRow, headerStyle) {
            saveExcelWorkbook(mode)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun handleTimeSelection(selectedTime: String) {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        when (selectedTime) {
            "Seçiniz" -> {
                baslangicTarihi = cal.time
                bitisTarihi = cal.time
            }

            "Bugün" -> {
                baslangicTarihi = cal.time
                cal.add(Calendar.DAY_OF_YEAR, 1)
                bitisTarihi = cal.time
            }

            "Dün" -> {
                bitisTarihi = cal.time
                cal.add(Calendar.DAY_OF_YEAR, -1)
                baslangicTarihi = cal.time
            }

            "Bu Hafta" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                baslangicTarihi = cal.time
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                bitisTarihi = cal.time
            }

            "Geçen Hafta" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                bitisTarihi = cal.time
                cal.add(Calendar.DAY_OF_YEAR, -7)
                baslangicTarihi = cal.time
            }

            "Bu Ay" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                baslangicTarihi = cal.time
                cal.add(Calendar.MONTH, 1)
                bitisTarihi = cal.time
            }

            "Son 30 Gün" -> {
                bitisTarihi = cal.time
                cal.add(Calendar.DAY_OF_YEAR, -30)
                baslangicTarihi = cal.time
            }

            "Geçen Ay" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                bitisTarihi = cal.time
                cal.add(Calendar.MONTH, -1)
                baslangicTarihi = cal.time
            }

            "Tüm Zamanlar" -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time
                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time
            }
        }
    }

    private fun addData(
        sheet: Sheet,
        mode: ExcelReportMode,
        dataStartRow: Int,
        headerStyle: CellStyle,
        onComplete: () -> Unit
    ) {
        if (studentList.isEmpty()) {
            onComplete()
            return
        }

        lifecycleScope.launch {
            try {
                val studiesByStudent = StudyQueryHelper.fetchStudiesByStudentIds(
                    db,
                    kurumKodu.toString(),
                    studentList.map { it.id },
                    baslangicTarihi,
                    bitisTarihi,
                )
                if (mode == ExcelReportMode.BY_LESSON) {
                    writeLessonExcelRows(sheet, dataStartRow, headerStyle, studiesByStudent)
                } else {
                    writeSummaryExcelRows(sheet, dataStartRow, studiesByStudent)
                }
                onComplete()
            } catch (e: Exception) {
                Toast.makeText(
                    activity,
                    e.localizedMessage ?: getString(R.string.excel_lutfen_bekleyin),
                    Toast.LENGTH_SHORT,
                ).show()
                onComplete()
            }
        }
    }

    private fun writeSummaryExcelRows(
        sheet: Sheet,
        dataStartRow: Int,
        studiesByStudent: Map<String, List<com.google.firebase.firestore.DocumentSnapshot>>,
    ) {
        var rowIndex = dataStartRow
        val decimalStyle = ExcelExportHelper.createDecimalStyle(workbook)
        val format = workbook.createDataFormat()
        val daysBetween = ExcelExportHelper.daysBetween(baslangicTarihi, bitisTarihi)

        for (student in studentList) {
            val studies = studiesByStudent[student.id].orEmpty()
            val row = sheet.createRow(rowIndex++)

            row.createCell(0).setCellValue(student.studentName)
            row.createCell(1).setCellValue(student.grade.toString())

            var totalTime = 0.0
            var totalQuestions = 0.0
            val studyCount = studies.size.toDouble()

            for (study in studies) {
                totalTime += study.get("toplamCalisma")?.toString()?.toDoubleOrNull() ?: 0.0
                totalQuestions += study.get("çözülenSoru")?.toString()?.toDoubleOrNull() ?: 0.0
            }

            val avgHoursPerDay = (totalTime / 60.0) / daysBetween
            val avgQuestionsPerDay = totalQuestions / daysBetween
            val avgStudiesPerDay = studyCount / daysBetween

            row.createCell(2).apply {
                setCellValue(totalTime)
                cellStyle = decimalStyle
            }
            row.createCell(3).apply {
                setCellValue(totalTime / 60.0)
                cellStyle = decimalStyle
            }
            row.createCell(4).apply {
                setCellValue(avgHoursPerDay)
                cellStyle = decimalStyle
            }
            row.createCell(5).apply {
                setCellValue(totalQuestions)
                cellStyle = decimalStyle
            }
            row.createCell(6).apply {
                setCellValue(avgQuestionsPerDay)
                cellStyle = decimalStyle
            }
            row.createCell(7).apply {
                setCellValue(studyCount)
                cellStyle = decimalStyle
            }
            row.createCell(8).apply {
                setCellValue(avgStudiesPerDay)
                cellStyle = decimalStyle
            }
        }

        val summaryRow = sheet.createRow(rowIndex)
        val summaryStyle = workbook.createCellStyle()
        summaryStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        summaryStyle.setDataFormat(format.getFormat("0.00"))

        summaryRow.createCell(0).apply {
            setCellValue("ORTALAMA")
            cellStyle = summaryStyle
        }

        val avgFormula = { col: String -> "AVERAGE($col${dataStartRow + 1}:$col$rowIndex)" }
        for (i in 2..8) {
            val cell = summaryRow.createCell(i)
            cell.cellFormula = avgFormula(('A' + i).toString())
            cell.cellStyle = summaryStyle
        }
    }

    private data class LessonExcelRow(
        val student: Student,
        val byLesson: Map<String, Pair<Double, Double>>,
        val totalTime: Double,
        val totalQuestions: Double,
    )

    private fun writeLessonExcelRows(
        sheet: Sheet,
        headerRowIndex: Int,
        headerStyle: CellStyle,
        studiesByStudent: Map<String, List<com.google.firebase.firestore.DocumentSnapshot>>,
    ) {
        val decimalStyle = ExcelExportHelper.createDecimalStyle(workbook)
        val daysBetween = ExcelExportHelper.daysBetween(baslangicTarihi, bitisTarihi)
        val allLessons = sortedSetOf<String>()
        val rowsData = mutableListOf<LessonExcelRow>()

        for (student in studentList) {
            val studies = studiesByStudent[student.id].orEmpty()
            if (studies.isEmpty()) continue

            val byLesson = linkedMapOf<String, Pair<Double, Double>>()
            var totalTime = 0.0
            var totalQuestions = 0.0

            for (study in studies) {
                val ders = study.getString("dersAdi")?.takeIf { it.isNotBlank() } ?: "Belirtilmemiş"
                val time = study.get("toplamCalisma")?.toString()?.toDoubleOrNull() ?: 0.0
                val questions = study.get("çözülenSoru")?.toString()?.toDoubleOrNull() ?: 0.0
                val current = byLesson.getOrDefault(ders, 0.0 to 0.0)
                byLesson[ders] = (current.first + time) to (current.second + questions)
                totalTime += time
                totalQuestions += questions
            }

            allLessons.addAll(byLesson.keys)
            rowsData.add(LessonExcelRow(student, byLesson, totalTime, totalQuestions))
        }

        val sortedLessons = allLessons.sorted()
        val lessonHeaders = sortedLessons.flatMap { listOf("$it Süre (dk)", "$it Soru") }
        val headers = arrayOf(
            "Öğrenci Adı",
            "Sınıf",
            *lessonHeaders.toTypedArray(),
            "Toplam Süre (dk)",
            "Toplam Süre (saat)",
            "Toplam Soru",
            "Günlük Ortalama Soru",
        )

        var rowIndex = ExcelExportHelper.writeStyledHeaderRow(
            sheet, headers, headerStyle, headerRowIndex
        )

        for (rowData in rowsData) {
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(rowData.student.studentName)
            row.createCell(1).setCellValue(rowData.student.grade.toString())

            var col = 2
            for (ders in sortedLessons) {
                val totals = rowData.byLesson[ders] ?: (0.0 to 0.0)
                row.createCell(col++).apply {
                    setCellValue(totals.first)
                    cellStyle = decimalStyle
                }
                row.createCell(col++).apply {
                    setCellValue(totals.second)
                    cellStyle = decimalStyle
                }
            }

            row.createCell(col++).apply {
                setCellValue(rowData.totalTime)
                cellStyle = decimalStyle
            }
            row.createCell(col++).apply {
                setCellValue(rowData.totalTime / 60.0)
                cellStyle = decimalStyle
            }
            row.createCell(col++).apply {
                setCellValue(rowData.totalQuestions)
                cellStyle = decimalStyle
            }
            row.createCell(col).apply {
                setCellValue(rowData.totalQuestions / daysBetween)
                cellStyle = decimalStyle
            }
        }
    }

    private fun saveExcelWorkbook(mode: ExcelReportMode) {
        val activity = mainActivity ?: return
        val prefix = if (mode == ExcelReportMode.SUMMARY) "Ogrenci_Ozet" else "Ogrenci_Ders"
        val fileName = ExcelExportHelper.buildFileName(prefix, secilenGrade, secilenZaman)
        ExcelExportHelper.saveWorkbook(activity, workbook, fileName)
    }

    private fun truncatedUid(uid: String): String = uid.takeLast(4)

    companion object {
        private const val WEEKLY_GOALS_TAG = "KTS:WeeklyGoals"
    }

}