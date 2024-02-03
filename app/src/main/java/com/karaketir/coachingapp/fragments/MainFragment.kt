package com.karaketir.coachingapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.Calendar
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
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private val workbook = XSSFWorkbook()
    private var studyList = ArrayList<Study>()
    private var secilenGrade = "Bütün Sınıflar"
    private var secilenZaman = "Bugün"
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")
    private var raporGondermeyenList = ArrayList<Student>()
    private val zamanAraliklari = arrayOf(
        "Bugün",
        "Dün",
        "Bu Hafta",
        "Geçen Hafta",
        "Bu Ay",
        "Son 30 Gün",
        "Geçen Ay",
        "Son 2 Ay",
        "Son 3 Ay",
        "Son 4 Ay",
        "Son 5 Ay",
        "Son 6 Ay",
        "Tüm Zamanlar"
    )

    private lateinit var filteredList: ArrayList<Student>
    private lateinit var filteredStudyList: ArrayList<Study>

    private var studentList = ArrayList<Student>()
    private var grade = 0
    private var teacher = ""
    private var personType = ""
    private var name = ""
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
            val excelButton = mBinding.excelButton
            val dersProgramiButton = mBinding.dersProgramiButton
            val noReportButton = mBinding.noReportButton
            textYKSsayac = mBinding.YKSsayac
            val teacherCardView = mBinding.teacherCardView
            val messageButton = mBinding.sendMessageButton


            var kocID = ""

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



            db.collection("VersionCode").document("60qzy2yuxMwCCau44HdF").get()
                .addOnSuccessListener {
                    val myVersion = BuildConfig.VERSION_CODE
                    val latestVersion = it.get("latestVersion").toString().toInt()
                    if (myVersion < latestVersion) {
                        signOutButton.visibility = View.GONE
                        updateLayout.visibility = View.VISIBLE

                    } else {
                        signOutButton.visibility = View.VISIBLE
                        updateLayout.visibility = View.GONE

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
                    filteredList = ArrayList()
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

                        var cal = Calendar.getInstance()
                        grade = it.get("grade").toString().toInt()
                        updateTime(grade)
                        teacher = it.get("teacher").toString()
                        dersProgramiButton.visibility = View.VISIBLE
                        addStudyButton.visibility = View.VISIBLE
                        kocOgretmenTextView.visibility = View.VISIBLE
                        excelButton.visibility = View.GONE
                        teacherCardView.visibility = View.GONE

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

                        cal.add(Calendar.DAY_OF_YEAR, 2)
                        val bitisTarihi = cal.time

                        cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -7)
                        val baslangicTarihi = cal.time



                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(auth.uid.toString()).collection("Studies")
                            .whereGreaterThan("timestamp", baslangicTarihi)
                            .whereLessThan("timestamp", bitisTarihi)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener { it1, _ ->
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


                    } else if (it.get("personType").toString() == "Teacher") {
                        teacher = ""
                        personType = "Teacher"
                        dersProgramiButton.visibility = View.GONE
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

                        //Create Header Cell Style
                        val cellStyle = getHeaderStyle(workbook)

                        //Creating sheet header row
                        createSheetHeader(cellStyle, sheet)

                        excelButton.setOnClickListener {

                            clearCache(mainActivity)
                            Toast.makeText(activity, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT)
                                .show()

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
                        teacherSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                                ) {
                                    secilenZaman = zamanAraliklari[p2]
                                    getData()
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                }

                            }

                        val gradeAdapter = ArrayAdapter(
                            mainActivity, android.R.layout.simple_spinner_item, gradeList
                        )
                        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                val myIntent = Intent(activity, NoReportActivity::class.java)
                myIntent.putExtra("kurumKodu", kurumKodu.toString())
                myIntent.putExtra("grade", secilenGrade)
                var cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                var baslangicTarihi = cal.time
                var bitisTarihi = cal.time


                when (secilenZaman) {

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

                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        baslangicTarihi = cal.time


                        cal.add(Calendar.MONTH, 1)
                        bitisTarihi = cal.time


                    }

                    "Son 30 Gün" -> {
                        cal = Calendar.getInstance()

                        bitisTarihi = cal.time

                        cal.add(Calendar.DAY_OF_YEAR, -30)

                        baslangicTarihi = cal.time

                    }

                    "Geçen Ay" -> {
                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        bitisTarihi = cal.time


                        cal.add(Calendar.MONTH, -1)
                        baslangicTarihi = cal.time

                    }

                    "Son 2 Ay" -> {
                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        bitisTarihi = cal.time

                        cal.add(Calendar.MONTH, -2)
                        baslangicTarihi = cal.time
                    }

                    "Son 3 Ay" -> {
                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        bitisTarihi = cal.time

                        cal.add(Calendar.MONTH, -3)
                        baslangicTarihi = cal.time
                    }

                    "Son 4 Ay" -> {
                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        bitisTarihi = cal.time

                        cal.add(Calendar.MONTH, -4)
                        baslangicTarihi = cal.time
                    }

                    "Son 5 Ay" -> {
                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        bitisTarihi = cal.time

                        cal.add(Calendar.MONTH, -5)
                        baslangicTarihi = cal.time
                    }

                    "Son 6 Ay" -> {
                        cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        bitisTarihi = cal.time

                        cal.add(Calendar.MONTH, -6)
                        baslangicTarihi = cal.time
                    }


                    "Tüm Zamanlar" -> {
                        cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                        baslangicTarihi = cal.time


                        cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                        bitisTarihi = cal.time

                    }
                }
                raporGondermeyenList.clear()

                var my = 0
                for (i in studentList) {

                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(i.id).collection("Studies")
                        .whereGreaterThan("timestamp", baslangicTarihi)
                        .whereLessThan("timestamp", bitisTarihi)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                println(error.localizedMessage)
                            }

                            if (value != null) {

                                if (value.isEmpty) {
                                    raporGondermeyenList.add(i)
                                }

                            } else {
                                raporGondermeyenList.add(i)
                            }

                            my += 1
                            if (my == studentList.size) {
                                println(raporGondermeyenList.size)
                                for (a in raporGondermeyenList) {
                                    println(a.studentName)
                                }
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
                signOutAlertDialog.setNegativeButton("İptal") { _, _ ->

                }
                signOutAlertDialog.show()

            }


        }


    }


    private fun setupStudentRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(mainActivity)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(list, secilenZaman, kurumKodu)

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter

    }

    private fun setupStudyRecyclerView(list: ArrayList<Study>) {
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

        Toast.makeText(activity, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT).show()
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

                }
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        this.startActivity(intent)
        mainActivity.finish()
    }


    @SuppressLint("SetTextI18n")
    fun updateTime(grade: Int) {

        val currentDate = Calendar.getInstance()

        val eventDate = Calendar.getInstance()
        when (grade) {
            12, 0 -> {
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


                            textYKSsayac.text = "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                            mBinding.YKSsayacCardView.visibility = View.VISIBLE
                        }

                    } else {
                        textYKSsayac.text = "Son: $years Yıl, $ay Ay, ${remainingDays % 30} Gün"
                        mBinding.YKSsayacCardView.visibility = View.VISIBLE

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
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createExcel(
                    mainActivity, secilenGrade, secilenZaman, workbook
                )
            } else {
                // Permission not granted, handle accordingly
            }
        }

    private fun askForPermissions() {
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            createExcel(mainActivity, secilenGrade, secilenZaman, workbook)
        }
    }


}