package com.karaketir.coachingapp

//noinspection SuspiciousImport

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.adapter.StudiesRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityMainBinding
import com.karaketir.coachingapp.models.Student
import com.karaketir.coachingapp.models.Study
import com.karaketir.coachingapp.services.glide
import com.karaketir.coachingapp.services.placeHolderYap
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerViewPreviousStudies: RecyclerView
    private lateinit var recyclerViewMyStudents: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mp: MediaPlayer

    private var studyList = ArrayList<Study>()
    private var secilenZaman = "Bug??n"
    private var gradeList = arrayOf("B??t??n S??n??flar", "12", "11", "10", "9", "0")
    private val zamanAraliklari =
        arrayOf("Bug??n", "D??n", "Bu Hafta", "Ge??en Hafta", "Bu Ay", "Ge??en Ay", "T??m Zamanlar")
    private lateinit var filteredList: ArrayList<Student>
    private lateinit var filteredStudyList: ArrayList<Study>

    private var studentList = ArrayList<Student>()

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic(auth.uid.toString())
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val nameAndSurnameTextView = binding.nameAndSurnameTextView
        val transitionsContainer = binding.transitionsContainer
        val sayacContainer = binding.sayacContainer

        var sayacVisible = false

        val contentTextView = binding.contentTextView
        val addStudyButton = binding.addStudyButton
        val signOutButton = binding.signOutButton
        val gorevButton = binding.gorevButton
        val previousRatingsButton = binding.previousRatingsButton
        val allStudentsBtn = binding.allStudentsBtn
        val searchEditText = binding.searchStudentMainActivityEditText
        val studySearchEditText = binding.searchStudyEditText
        val studentDenemeButton = binding.studentDenemeButton
        val teacherDenemeButton = binding.teacherDenemeButton
        val istatistikButton = binding.koclukStatsBtn
        val hedeflerStudentButton = binding.hedefStudentButton
        val gradeSpinner = binding.gradeSpinner
        val gradeSpinnerLayout = binding.gradeSpinnerLayout
        val kocOgretmenTextView = binding.kocOgretmenTextView
        val teacherSpinnerLayout = binding.teacherSpinnerLayout
        val teacherSpinner = binding.studyZamanAraligiSpinner
        val searchBarTeacher = binding.searchBarTeacher
        val okulLogo = binding.logoLayout
        val updateLayout = binding.updateLayout
        val updateButton = binding.updateButton
        val imageHalit = binding.imageHalit

        db.collection("UserPhotos").document(auth.uid.toString()).get().addOnSuccessListener {
            imageHalit.glide(it.get("photoURL").toString(), placeHolderYap(applicationContext))

        }

        val dogumGunu = Calendar.getInstance()

        updateButton.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.karaketir.coachingapp")
            )
            startActivity(browserIntent)
        }



        db.collection("VersionCode").document("60qzy2yuxMwCCau44HdF").get().addOnSuccessListener {
            val myVersion = BuildConfig.VERSION_CODE
            val latestVersion = it.get("latestVersion").toString().toInt()
            if (myVersion < latestVersion) {
                addStudyButton.visibility = View.GONE
                signOutButton.visibility = View.GONE
                updateLayout.visibility = View.VISIBLE

            } else {
                addStudyButton.visibility = View.VISIBLE
                signOutButton.visibility = View.VISIBLE
                updateLayout.visibility = View.GONE

            }
        }

        var grade = 0
        okulLogo.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/rteprojeihl/"))
            startActivity(browserIntent)
        }

        nameAndSurnameTextView.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        recyclerViewPreviousStudies = binding.previousStudies
        recyclerViewMyStudents = binding.myStudents

        hedeflerStudentButton.setOnClickListener {
            val intent2 = Intent(this, GoalsActivity::class.java)
            intent2.putExtra("studentID", auth.currentUser?.uid.toString())
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

        studySearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredStudyList = ArrayList()
                if (p0.toString() != "") {
                    for (item in studyList) {
                        if (item.studyName.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredStudyList.add(item)
                        }
                    }
                    setupStudyRecyclerView(filteredStudyList)
                } else {
                    setupStudyRecyclerView(studyList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })


        var visible = false
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            nameAndSurnameTextView.text = "Merhaba: " + it.get("nameAndSurname").toString()
            val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()


            TransitionManager.beginDelayedTransition(transitionsContainer)
            visible = !visible
            nameAndSurnameTextView.visibility = if (visible) View.VISIBLE else View.GONE


            if (it.get("personType").toString() == "Student") {

                val cal = Calendar.getInstance()
                grade = it.get("grade").toString().toInt()
                addStudyButton.visibility = View.VISIBLE
                kocOgretmenTextView.visibility = View.VISIBLE

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(auth.uid.toString()).get().addOnSuccessListener { student ->
                        val kocID = student.get("teacher").toString()
                        if (kocID.isEmpty()) {
                            kocOgretmenTextView.text = "Ko?? ????retmenin Bulunmuyor"
                        } else {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Teacher").document(kocID).get()
                                .addOnSuccessListener { teacher ->
                                    kocOgretmenTextView.text =
                                        "Ko?? ????retmenin: " + teacher.get("nameAndSurname")
                                            .toString()
                                }

                        }
                    }
                searchBarTeacher.visibility = View.GONE
                gradeSpinnerLayout.visibility = View.GONE
                teacherSpinnerLayout.visibility = View.GONE
                studySearchEditText.visibility = View.VISIBLE
                searchEditText.visibility = View.GONE
                previousRatingsButton.visibility = View.VISIBLE
                studentDenemeButton.visibility = View.VISIBLE
                teacherDenemeButton.visibility = View.GONE
                hedeflerStudentButton.visibility = View.VISIBLE
                recyclerViewPreviousStudies.visibility = View.VISIBLE
                istatistikButton.visibility = View.GONE
                allStudentsBtn.visibility = View.GONE
                gorevButton.visibility = View.VISIBLE

                contentTextView.text = "Bu Haftaki ??al????malar??m"

                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                val baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                val bitisTarihi = cal.time

                val email = it.get("email").toString()
                db.collection("VersionCode").document("1sraIWibAFqiyzJHzxV5").get()
                    .addOnSuccessListener { dogumGunuIt ->
                        if (email == dogumGunuIt.get("email").toString()) {
                            val dogumGunuBas = dogumGunuIt.get("dogumGunuBas") as Timestamp
                            val dogumGunuSon = dogumGunuIt.get("dogumGunuSon") as Timestamp
                            val photoURL = dogumGunuIt.get("photoURL").toString()



                            if (dogumGunuBas.toDate() <= dogumGunu.time && dogumGunu.time <= dogumGunuSon.toDate()) {
                                nameAndSurnameTextView.text =
                                    "??yi ki Do??dun: " + it.get("nameAndSurname").toString()
                                binding.YKSsayac.visibility = View.INVISIBLE
                                okulLogo.visibility = View.INVISIBLE
                                kocOgretmenTextView.visibility = View.INVISIBLE
                                imageHalit.glide(photoURL, placeHolderYap(this))
                                mp = MediaPlayer.create(this, com.karaketir.coachingapp.R.raw.halay)
                                mp.isLooping = true
                                mp.start()
                            }

                        }
                    }

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
                            val studyTur = document.get("t??r").toString()
                            val soruSayisi = document.get("????z??lenSoru").toString()
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
                studySearchEditText.visibility = View.GONE
                hedeflerStudentButton.visibility = View.GONE
                searchEditText.visibility = View.VISIBLE
                teacherSpinnerLayout.visibility = View.VISIBLE
                kocOgretmenTextView.visibility = View.GONE
                recyclerViewMyStudents.visibility = View.VISIBLE
                searchBarTeacher.visibility = View.VISIBLE
                studentDenemeButton.visibility = View.GONE
                previousRatingsButton.visibility = View.GONE
                teacherDenemeButton.visibility = View.VISIBLE
                allStudentsBtn.visibility = View.VISIBLE
                addStudyButton.visibility = View.GONE
                istatistikButton.visibility = View.VISIBLE
                gorevButton.visibility = View.GONE
                gradeSpinnerLayout.visibility = View.VISIBLE
                contentTextView.text = "????rencilerim"

                val gradeAdapter = ArrayAdapter(
                    this@MainActivity, R.layout.simple_spinner_item, gradeList
                )
                gradeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                gradeSpinner.adapter = gradeAdapter
                gradeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val secilenGrade = gradeList[p2]
                        if (secilenGrade == "B??t??n S??n??flar") {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").whereEqualTo("teacher", auth.uid.toString())
                                .addSnapshotListener { documents, _ ->

                                    studentList.clear()
                                    if (documents != null) {
                                        for (document in documents) {
                                            val studentGrade =
                                                document.get("grade").toString().toInt()
                                            val studentName =
                                                document.get("nameAndSurname").toString()
                                            val teacher = document.get("teacher").toString()
                                            val id = document.get("id").toString()
                                            val currentStudent =
                                                Student(studentName, teacher, id, studentGrade)
                                            studentList.add(currentStudent)

                                        }
                                        binding.studentCountTextView.text =
                                            "????renci Say??s??: " + studentList.size
                                    }
                                    studentList.sortBy { a ->
                                        a.studentName
                                    }
                                    setupStudentRecyclerView(studentList)

                                    recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

                                }
                        } else {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").whereEqualTo("teacher", auth.uid.toString())
                                .whereEqualTo("grade", secilenGrade.toInt())
                                .addSnapshotListener { documents, _ ->

                                    studentList.clear()
                                    if (documents != null) {
                                        for (document in documents) {
                                            val studentGrade =
                                                document.get("grade").toString().toInt()
                                            val studentName =
                                                document.get("nameAndSurname").toString()
                                            val teacher = document.get("teacher").toString()
                                            val id = document.get("id").toString()
                                            val currentStudent =
                                                Student(studentName, teacher, id, studentGrade)
                                            studentList.add(currentStudent)

                                        }
                                        binding.studentCountTextView.text =
                                            "????renci Say??s??: " + studentList.size
                                    }
                                    studentList.sortBy { a ->
                                        a.studentName
                                    }
                                    setupStudentRecyclerView(studentList)

                                    recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

                                }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }


                val tarihAdapter = ArrayAdapter(
                    this@MainActivity, R.layout.simple_spinner_item, zamanAraliklari
                )
                tarihAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                teacherSpinner.adapter = tarihAdapter
                teacherSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        secilenZaman = zamanAraliklari[p2]
                        setupStudentRecyclerView(studentList)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                }


            }



            TransitionManager.beginDelayedTransition(sayacContainer)
            sayacVisible = !sayacVisible
            contentTextView.visibility = if (sayacVisible) View.VISIBLE else View.GONE

        }


        previousRatingsButton.setOnClickListener {
            val intent = Intent(this, PreviousRatingsActivity::class.java)
            this.startActivity(intent)
        }

        studentDenemeButton.setOnClickListener {
            val intent = Intent(this, DenemelerActivity::class.java)
            intent.putExtra("studentID", auth.uid.toString())
            this.startActivity(intent)
        }
        teacherDenemeButton.setOnClickListener {
            val intent = Intent(this, DenemelerTeacherActivity::class.java)
            this.startActivity(intent)
        }


        istatistikButton.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            this.startActivity(intent)
        }

        allStudentsBtn.setOnClickListener {
            val intent = Intent(this, AllStudentsActivity::class.java)
            this.startActivity(intent)
        }

        addStudyButton.setOnClickListener {
            val intent = Intent(this, ClassesActivity::class.java)
            this.startActivity(intent)
        }
        signOutButton.setOnClickListener {

            val signOutAlertDialog = AlertDialog.Builder(this)
            signOutAlertDialog.setTitle("????k???? Yap")
            signOutAlertDialog.setMessage("Hesab??n??zdan ????k???? Yapmak ??stedi??inize Emin misiniz?")
            signOutAlertDialog.setPositiveButton("????k????") { _, _ ->
                signOut()
                finish()
            }
            signOutAlertDialog.setNegativeButton("??ptal") { _, _ ->

            }
            signOutAlertDialog.show()

        }

        gorevButton.setOnClickListener {
            val intent = Intent(this, DutiesActivity::class.java)
            intent.putExtra("studentID", auth.uid)
            this.startActivity(intent)
        }

        handler.post(object : Runnable {
            override fun run() {
                // Keep the postDelayed before the updateTime(), so when the event ends, the handler will stop too.
                handler.postDelayed(this, 1000)
                updateTime(grade)
            }
        })


    }

    private fun setupStudentRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter = StudentsRecyclerAdapter(list, secilenZaman)

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter

    }

    private fun setupStudyRecyclerView(list: ArrayList<Study>) {
        val layoutManager = GridLayoutManager(applicationContext, 2)

        recyclerViewPreviousStudies.layoutManager = layoutManager
        recyclerViewPreviousStudiesAdapter = StudiesRecyclerAdapter(list, "Bu Hafta")
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter

    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    fun updateTime(grade: Int) {

        val currentDate = Calendar.getInstance()

        val eventDate = Calendar.getInstance()
        when (grade) {
            12, 0 -> {
                eventDate[Calendar.YEAR] = 2023
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 17
            }
            11 -> {
                eventDate[Calendar.YEAR] = 2024
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 15
            }
            10 -> {
                eventDate[Calendar.YEAR] = 2025
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 14
            }
            9 -> {
                eventDate[Calendar.YEAR] = 2026
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 13
            }
        }

        eventDate[Calendar.HOUR_OF_DAY] = 10
        eventDate[Calendar.MINUTE] = 15
        eventDate[Calendar.SECOND] = 0

        val diff = eventDate.timeInMillis - currentDate.timeInMillis

        val days = diff / (24 * 60 * 60 * 1000)
        val hours = diff / (1000 * 60 * 60) % 24
        val minutes = diff / (1000 * 60) % 60
        val seconds = (diff / 1000) % 60

        binding.YKSsayac.text = "YKS'ye Son:\n $days G??n $hours Saat $minutes Dk $seconds Sn"


    }

    override fun onResume() {

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        }

        super.onResume()
    }

}