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

    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        this.mainActivity = activity
    }

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
    private var workbook = XSSFWorkbook()
    private var studyList = ArrayList<Study>()
    private var secilenGrade = "Bütün Sınıflar"
    private var secilenZaman = "Seçiniz"
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0", "13")
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

            updateButton.setOnClickListener {
                mainActivity?.let { it1 ->
                    openLink(
                        "https://play.google.com/store/apps/details?id=com.karaketir.coachingapp",
                        it1
                    )
                }
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

                        excelButton.setOnClickListener {

                            //clear list of

                            if (secilenZaman == "Seçiniz") {
                                Toast.makeText(
                                    mainActivity,
                                    "Lütfen bir zaman aralığı seçiniz",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            // Create a new workbook instance to clear previous stats
                            workbook = XSSFWorkbook()
                            val sheet: Sheet = workbook.createSheet("Sayfa 1")
                            val cellStyle = getHeaderStyle(workbook)
                            createSheetHeader(cellStyle, sheet)

                            mainActivity?.let { it1 -> clearCache(it1) }
                            Toast.makeText(activity, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT)
                                .show()

                            addData(sheet) {
                                createExcel()
                            }
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
                val myIntent = Intent(activity, NoReportActivity::class.java)
                myIntent.putExtra("kurumKodu", kurumKodu.toString())
                myIntent.putExtra("grade", secilenGrade)
                myIntent.putExtra("baslangicTarihi", baslangicTarihi)
                myIntent.putExtra("bitisTarihi", bitisTarihi)

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
                                mainActivity?.startActivity(myIntent)
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


    private fun setupStudentRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(mainActivity)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(list, kurumKodu, baslangicTarihi, bitisTarihi, secilenZaman)

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
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                mainActivity?.let {
                    createExcel(
                        it, secilenGrade, secilenZaman, workbook
                    )
                }
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
            createExcel()
        }
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        val row = sheet.createRow(0)
        
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

        headers.forEachIndexed { index, header ->
            val cell = row.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = cellStyle
            
            // Set column widths
            when (index) {
                0 -> sheet.setColumnWidth(index, 30 * 256) // Öğrenci Adı - wider
                1 -> sheet.setColumnWidth(index, 10 * 256) // Sınıf - narrow
                else -> sheet.setColumnWidth(index, 25 * 256) // Other columns - medium
            }
        }
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {
        val cellStyle = workbook.createCellStyle()
        cellStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.index
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        return cellStyle
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

    private fun addData(sheet: Sheet, onComplete: () -> Unit) {
        var completedStudents = 0
        var rowIndex = 1

        if (studentList.isEmpty()) {
            onComplete()
            return
        }

        // Create cell style for numbers with 2 decimal places
        val decimalStyle = workbook.createCellStyle()
        val format = workbook.createDataFormat()
        decimalStyle.dataFormat = format.getFormat("0.00")

        studentList.forEach { student ->
            db.collection("School")
                .document(kurumKodu.toString())
                .collection("Student")
                .document(student.id)
                .collection("Studies")
                .whereGreaterThan("timestamp", baslangicTarihi)
                .whereLessThan("timestamp", bitisTarihi)
                .get()
                .addOnSuccessListener { studies ->
                    val row = sheet.createRow(rowIndex++)
                    
                    // Student info
                    row.createCell(0).setCellValue(student.studentName)
                    row.createCell(1).setCellValue(student.grade.toString())
                    
                    // Calculate totals
                    var totalTime = 0.0
                    var totalQuestions = 0.0
                    val studyCount = studies.size().toDouble()
                    
                    studies.forEach { study ->
                        totalTime += study.get("toplamCalisma")?.toString()?.toDoubleOrNull() ?: 0.0
                        totalQuestions += study.get("çözülenSoru")?.toString()?.toDoubleOrNull() ?: 0.0
                    }

                    // Calculate date range in days (ensure it's at least 1 to prevent division by zero)
                    val daysBetween = maxOf(((bitisTarihi.time - baslangicTarihi.time) / (1000.0 * 60 * 60 * 24)) + 1, 1.0)
                    
                    // Calculate daily averages
                    val avgHoursPerDay = (totalTime / 60.0) / daysBetween
                    val avgQuestionsPerDay = totalQuestions / daysBetween
                    val avgStudiesPerDay = studyCount / daysBetween
                    
                    // Add totals and averages using numeric cells
                    val timeCell = row.createCell(2)
                    timeCell.setCellValue(totalTime)
                    timeCell.cellStyle = decimalStyle
                    
                    val hoursCell = row.createCell(3)
                    hoursCell.setCellValue(totalTime / 60.0)
                    hoursCell.cellStyle = decimalStyle
                    
                    val avgHoursCell = row.createCell(4)
                    avgHoursCell.setCellValue(avgHoursPerDay)
                    avgHoursCell.cellStyle = decimalStyle
                    
                    val questionsCell = row.createCell(5)
                    questionsCell.setCellValue(totalQuestions)
                    questionsCell.cellStyle = decimalStyle
                    
                    val avgQuestionsCell = row.createCell(6)
                    avgQuestionsCell.setCellValue(avgQuestionsPerDay)
                    avgQuestionsCell.cellStyle = decimalStyle
                    
                    val studyCountCell = row.createCell(7)
                    studyCountCell.setCellValue(studyCount)
                    studyCountCell.cellStyle = decimalStyle
                    
                    val avgStudiesCell = row.createCell(8)
                    avgStudiesCell.setCellValue(avgStudiesPerDay)
                    avgStudiesCell.cellStyle = decimalStyle

                    completedStudents++
                    if (completedStudents == studentList.size) {
                        // Add summary row
                        val summaryRow = sheet.createRow(rowIndex)
                        val summaryStyle = workbook.createCellStyle()
                        summaryStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
                        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                        summaryStyle.setDataFormat(format.getFormat("0.00"))

                        summaryRow.createCell(0).apply {
                            setCellValue("ORTALAMA")
                            cellStyle = summaryStyle
                        }

                        // Calculate averages for all students
                        val avgFormula = { col: String -> "AVERAGE($col${2}:$col${rowIndex})" }
                        
                        // Skip first two columns (name and grade)
                        for (i in 2..8) {
                            val cell = summaryRow.createCell(i)
                            cell.cellFormula = avgFormula(('A' + i).toString())
                            cell.cellStyle = summaryStyle
                        }

                        onComplete()
                    }
                }
                .addOnFailureListener {
                    completedStudents++
                    if (completedStudents == studentList.size) {
                        onComplete()
                    }
                }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createExcel() {
        mainActivity?.let { activity ->
            Toast.makeText(activity, "Excel dosyası hazırlanıyor...", Toast.LENGTH_SHORT).show()

            addData(workbook.getSheetAt(0)) {
                try {
                    val fileName = "Öğrenci_Çalışmaları_${
                        SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Date())
                    }.xlsx"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(
                                MediaStore.MediaColumns.MIME_TYPE,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOCUMENTS
                            )
                        }

                        activity.contentResolver?.insert(
                            MediaStore.Files.getContentUri("external"),
                            contentValues
                        )?.let { uri ->
                            activity.contentResolver?.openOutputStream(uri)?.use { outputStream ->
                                workbook.write(outputStream)
                            }
                            Toast.makeText(
                                activity,
                                "Excel dosyası başarıyla oluşturuldu",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val path =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        val file = File(path, fileName)

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        Toast.makeText(
                            activity,
                            "Excel dosyası başarıyla oluşturuldu",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        activity,
                        "Excel dosyası oluşturulurken hata oluştu: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}