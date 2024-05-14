package com.karaketir.coachingapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.coachingapp.AllStudentsActivity
import com.karaketir.coachingapp.BuildConfig
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
import com.karaketir.coachingapp.databinding.FragmentMainBinding
import com.karaketir.coachingapp.models.Student
import com.karaketir.coachingapp.models.Study
import com.karaketir.coachingapp.services.addData
import com.karaketir.coachingapp.services.clearCache
import com.karaketir.coachingapp.services.createExcel
import com.karaketir.coachingapp.services.createSheetHeader
import com.karaketir.coachingapp.services.getHeaderStyle
import com.karaketir.coachingapp.services.openLink
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainFragment(private var mainActivity: MainActivity) : Fragment() {

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }

    private lateinit var auth: FirebaseAuth
    private var kurumKodu = 763455
    private lateinit var recyclerViewPreviousStudies: RecyclerView
    private lateinit var recyclerViewMyStudents: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private val workbook = XSSFWorkbook()
    private var studyList = ArrayList<Study>()
    private var secilenGrade = "Bütün Sınıflar"
    private var secilenZaman = "Seçiniz"
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")
    private var raporGondermeyenList = ArrayList<Student>()
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

    private lateinit var filteredList: ArrayList<Student>

    private var studentList = ArrayList<Student>()
    private var grade = 0
    private var teacher = ""
    private var personType = ""
    private var name = ""
    private lateinit var textYKSsayac: TextView
    private lateinit var mBinding: FragmentMainBinding
    private lateinit var userDoc: DocumentSnapshot
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var dersProgramiButton: Button
    private lateinit var hedeflerStudentButton: Button
    private lateinit var excelButton: Button
    private lateinit var noReportButton: Button
    private lateinit var messageButton: Button
    private lateinit var kocOgretmenTextView: TextView
    private lateinit var searchBarTeacher: View
    private lateinit var updateLayout: View
    private lateinit var updateButton: Button
    private lateinit var nameAndSurnameTextView: TextView
    private lateinit var sayacContainer: View
    private lateinit var topStudentsButton: Button
    private lateinit var addStudyButton: Button
    private lateinit var signOutButton: Button
    private lateinit var previousRatingsButton: Button
    private lateinit var allStudentsBtn: Button
    private lateinit var searchEditText: TextView
    private lateinit var gradeSpinner: Spinner
    private lateinit var teacherSpinner: Spinner
    private lateinit var teacherCardView: View

    private lateinit var customDateLayout: View
    private lateinit var baslangicTarihiTextView: TextView
    private lateinit var bitisTarihiTextView: TextView

    private var sayacVisible = false
    private var kocID = ""


    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
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

        auth = Firebase.auth
        db = Firebase.firestore

        isViewCreated = true
        if (isBindingAvailable()) {
            mBinding = binding
            nameAndSurnameTextView = mBinding.nameAndSurnameTextView
            sayacContainer = mBinding.sayacContainer

            sayacVisible = false

            topStudentsButton = mBinding.topStudentsButton
            addStudyButton = mBinding.addStudyButton
            addStudyButton.visibility = View.GONE
            signOutButton = mBinding.signOutButton
            previousRatingsButton = mBinding.previousRatingsButton
            allStudentsBtn = mBinding.allStudentsBtn
            searchEditText = mBinding.searchStudentMainActivityEditText
            hedeflerStudentButton = mBinding.hedefStudentButton
            gradeSpinner = mBinding.gradeSpinner
            kocOgretmenTextView = mBinding.kocOgretmenTextView
            teacherSpinner = mBinding.studyZamanAraligiSpinner
            searchBarTeacher = mBinding.searchBarTeacher
            updateLayout = mBinding.updateLayout
            updateButton = mBinding.updateButton
            excelButton = mBinding.excelButton
            dersProgramiButton = mBinding.dersProgramiButton
            noReportButton = mBinding.noReportButton
            textYKSsayac = mBinding.YKSsayac
            teacherCardView = mBinding.teacherCardView
            messageButton = mBinding.sendMessageButton

            customDateLayout = mBinding.customDateLayout
            baslangicTarihiTextView = mBinding.baslangicTarihiTextView
            bitisTarihiTextView = mBinding.bitisTarihiTextView

            kocID = ""

            updateButton.setOnClickListener {
                openLink(
                    "https://play.google.com/store/apps/details?id=com.karaketir.coachingapp",
                    mainActivity
                )
            }

            dersProgramiButton.setOnClickListener {
                val newIntent = Intent(activity, ProgramActivity::class.java)
                newIntent.putExtra("personType", personType)
                newIntent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(newIntent)
            }

            lifecycleScope.launch {
                try {
                    val versionDoc =
                        db.collection("VersionCode").document("60qzy2yuxMwCCau44HdF").get().await()
                    val myVersion = BuildConfig.VERSION_CODE
                    val latestVersion = versionDoc.get("latestVersion").toString().toInt()
                    if (myVersion < latestVersion) {
                        signOutButton.visibility = View.GONE
                        updateLayout.visibility = View.VISIBLE
                    } else {
                        signOutButton.visibility = View.VISIBLE
                        updateLayout.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
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
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    filteredList = ArrayList()
                    if (p0.toString().isNotEmpty()) {
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

                override fun afterTextChanged(p0: Editable?) {}
            })

            lifecycleScope.launch {
                try {
                    val userDoc = db.collection("User").document(auth.uid.toString()).get().await()
                    name = userDoc.get("nameAndSurname").toString()
                    nameAndSurnameTextView.text = name

                    kurumKodu = userDoc.get("kurumKodu")?.toString()?.toInt() ?: 763455
                    FirebaseMessaging.getInstance().subscribeToTopic(kurumKodu.toString())

                    when (userDoc.get("personType").toString()) {
                        "Student" -> handleStudent()
                        "Teacher" -> handleTeacher()
                    }
                    TransitionManager.beginDelayedTransition(sayacContainer as LinearLayout)
                    sayacVisible = !sayacVisible
                } catch (e: Exception) {
                    println(e.localizedMessage)
                }
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
                val myIntent = Intent(activity, NoReportActivity::class.java)
                myIntent.putExtra("kurumKodu", kurumKodu.toString())
                myIntent.putExtra("grade", secilenGrade)
                myIntent.putExtra("baslangicTarihi", baslangicTarihi)
                myIntent.putExtra("bitisTarihi", bitisTarihi)

                raporGondermeyenList.clear()

                studentList.forEachIndexed { index, student ->
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(student.id).collection("Studies")
                        .whereGreaterThan("timestamp", baslangicTarihi)
                        .whereLessThan("timestamp", bitisTarihi).get()
                        .addOnSuccessListener { value ->
                            if (value.isEmpty) {
                                raporGondermeyenList.add(student)
                            }
                            if (index == studentList.size - 1) {
                                myIntent.putExtra("list", raporGondermeyenList)
                                myIntent.putExtra("secilenZaman", secilenZaman)
                                mainActivity.startActivity(myIntent)
                            }
                        }
                }
            }

            signOutButton.setOnClickListener {
                val signOutAlertDialog = AlertDialog.Builder(mainActivity)
                signOutAlertDialog.setTitle("Çıkış Yap")
                signOutAlertDialog.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
                signOutAlertDialog.setPositiveButton("Çıkış") { _, _ ->
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(auth.uid.toString())
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(kurumKodu.toString())
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(kocID)
                    signOut()
                }
                signOutAlertDialog.setNegativeButton("İptal", null)
                signOutAlertDialog.show()
            }
        }
    }

    private fun setupStudentRecyclerView(list: ArrayList<Student>) {
        recyclerViewMyStudents.layoutManager = LinearLayoutManager(mainActivity)
        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(list, kurumKodu, baslangicTarihi, bitisTarihi, secilenZaman)
        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter
    }

    private fun setupStudyRecyclerView(list: ArrayList<Study>) {
        recyclerViewPreviousStudies.layoutManager = GridLayoutManager(mainActivity, 2)
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

        val query = db.collection("School").document(kurumKodu.toString()).collection("Student")
            .whereEqualTo("teacher", auth.uid.toString())

        if (secilenGrade != "Bütün Sınıflar") {
            query.whereEqualTo("grade", secilenGrade.toInt())
        }

        query.get().addOnSuccessListener { documents ->
            studentList.clear()
            for (document in documents) {
                val studentGrade = document.get("grade").toString().toInt()
                val studentName = document.get("nameAndSurname").toString()
                val teacher = document.get("teacher").toString()
                val id = document.get("id").toString()
                val currentStudent = Student(studentName, teacher, id, studentGrade)
                studentList.add(currentStudent)
            }
            mBinding.progressBar.visibility = View.GONE
            mBinding.studentCountTextView.text = "Öğrenci Sayısı: ${studentList.size}"
            studentList.sortBy { it.studentName }
            setupStudentRecyclerView(studentList)
            recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        mainActivity.finish()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTime(grade: Int) {
        val currentDate = Calendar.getInstance()
        val eventDate = Calendar.getInstance()

        val dateDocId = when (grade) {
            12, 0 -> "date12"
            11 -> "date11"
            10 -> "date10"
            9 -> "date9"
            else -> return
        }

        lifecycleScope.launch {
            try {
                val dateDoc = db.collection("Dates").document(dateDocId).get().await()
                eventDate.set(Calendar.YEAR, dateDoc.get("year").toString().toInt())
                eventDate.set(Calendar.MONTH, dateDoc.get("month").toString().toInt() - 1)
                eventDate.set(Calendar.DAY_OF_MONTH, dateDoc.get("day").toString().toInt())
                eventDate.set(Calendar.HOUR_OF_DAY, 10)
                eventDate.set(Calendar.MINUTE, 15)
                eventDate.set(Calendar.SECOND, 0)

                val diff = eventDate.timeInMillis - currentDate.timeInMillis
                val days = diff / (24 * 60 * 60 * 1000)
                val years = days / 365
                val remainingDays = days - (years * 365)
                val months = remainingDays / 30

                textYKSsayac.text = "Son: $years Yıl, $months Ay, ${remainingDays % 30} Gün"
                mBinding.YKSsayacCardView.visibility = View.VISIBLE
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createExcel(mainActivity, secilenGrade, secilenZaman, workbook)
            }
        }

    private fun askForPermissions() {
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            createExcel(mainActivity, secilenGrade, secilenZaman, workbook)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private suspend fun handleStudent() {
        personType = "Student"
        val cal = Calendar.getInstance()
        grade = userDoc.get("grade").toString().toInt()
        updateTime(grade)
        teacher = userDoc.get("teacher").toString()
        dersProgramiButton.visibility = View.VISIBLE
        addStudyButton.visibility = View.VISIBLE
        kocOgretmenTextView.visibility = View.VISIBLE
        excelButton.visibility = View.GONE
        teacherCardView.visibility = View.GONE
        customDateLayout.visibility = View.GONE

        val studentDoc =
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(auth.uid.toString()).get().await()
        kocID = studentDoc.get("teacher").toString()
        if (kocID.isEmpty()) {
            kocOgretmenTextView.text = "Koç Öğretmenin Bulunmuyor"
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic(kocID)
            val teacherDoc =
                db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                    .document(kocID).get().await()
            kocOgretmenTextView.text =
                "Koç Öğretmenin:\n" + teacherDoc.get("nameAndSurname").toString()
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

        cal.add(Calendar.DAY_OF_YEAR, 2)
        val bitisTarihi = cal.time

        cal.add(Calendar.DAY_OF_YEAR, -7)
        val baslangicTarihi = cal.time

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(auth.uid.toString()).collection("Studies")
            .whereGreaterThan("timestamp", baslangicTarihi).whereLessThan("timestamp", bitisTarihi)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { it1, _ ->
                studyList.clear()
                val documents = it1!!.documents
                for (document in documents) {
                    val subjectTheme = document.get("konuAdi").toString()
                    val subjectCount = document.get("toplamCalisma").toString()
                    val studyDersAdi = document.get("dersAdi").toString()
                    val studyTur = document.get("tür").toString()
                    val soruSayisi = document.get("çözülenSoru").toString()
                    val timestamp = document.get("timestamp") as Timestamp
                    val currentStudy = Study(
                        subjectTheme,
                        subjectCount,
                        auth.uid.toString(),
                        studyDersAdi,
                        studyTur,
                        soruSayisi,
                        timestamp,
                        document.id
                    )
                    studyList.add(currentStudy)
                }
                setupStudyRecyclerView(studyList)
                recyclerViewPreviousStudiesAdapter.notifyDataSetChanged()
            }
    }

    private fun handleTeacher() {
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
        updateTime(grade)

        messageButton.setOnClickListener {
            val newIntent = Intent(mainActivity, MessageActivity::class.java)
            newIntent.putExtra("kurumKodu", kurumKodu.toString())
            this.startActivity(newIntent)
        }

        val sheet: Sheet = workbook.createSheet("Sayfa 1")
        val cellStyle = getHeaderStyle(workbook)
        createSheetHeader(cellStyle, sheet)

        excelButton.setOnClickListener {
            clearCache(mainActivity)
            Toast.makeText(activity, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT).show()
            addData(
                sheet,
                secilenZaman,
                secilenGrade,
                kurumKodu.toString(),
                auth,
                db,
                mainActivity,
                workbook
            )
            askForPermissions()
        }

        val tarihAdapter = ArrayAdapter(
            mainActivity, android.R.layout.simple_spinner_item, zamanAraliklari
        )

        tarihAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teacherSpinner.adapter = tarihAdapter
        teacherSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SimpleDateFormat", "SetTextI18n")
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
                        DatePickerDialog(
                            mainActivity,
                            dateSetListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    bitisTarihiTextView.setOnClickListener {
                        DatePickerDialog(
                            mainActivity,
                            dateSetListener2,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                } else {
                    customDateLayout.visibility = View.GONE
                    var cal = Calendar.getInstance()
                    cal[Calendar.HOUR_OF_DAY] = 0
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
                            cal[Calendar.HOUR_OF_DAY] = 0
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
                            cal[Calendar.HOUR_OF_DAY] = 0
                            cal.clear(Calendar.MINUTE)
                            cal.clear(Calendar.SECOND)
                            cal.clear(Calendar.MILLISECOND)
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

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val gradeAdapter = ArrayAdapter(
            mainActivity, android.R.layout.simple_spinner_item, gradeList
        )
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeSpinner.adapter = gradeAdapter
        gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
            ) {
                secilenGrade = gradeList[p2]
                getData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}
