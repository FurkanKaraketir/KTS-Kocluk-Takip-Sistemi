package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.kodgem.coachingapp.adapter.StudentsRecyclerAdapter
import com.kodgem.coachingapp.adapter.StudiesRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityMainBinding
import com.kodgem.coachingapp.models.Student
import com.kodgem.coachingapp.models.Study
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerViewPreviousStudies: RecyclerView
    private lateinit var recyclerViewMyStudents: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private var studyList = ArrayList<Study>()
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
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val nameAndSurnameTextView = binding.nameAndSurnameTextView
        val transitionsContainer = binding.transitionsContainer
        val sayacContainer = binding.sayacContainer

        var sayacVisible = false

        val contentTextView = binding.contentTextView
        val addStudyButton = binding.addStudyButton
        val signOutButton = binding.signOutButton
        val sayacButton = binding.sayacButton
        val gorevButton = binding.gorevButton
        val allStudentsBtn = binding.allStudentsBtn
        val searchEditText = binding.searchStudentMainActivityEditText
        val studySearchEditText = binding.searchStudyEditText
        val studentDenemeButton = binding.studentDenemeButton
        val istatistikButton = binding.koclukStatsBtn

        auth = Firebase.auth
        db = Firebase.firestore

        recyclerViewPreviousStudies = binding.previousStudies
        recyclerViewMyStudents = binding.myStudents



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
            nameAndSurnameTextView.text = it.get("nameAndSurname").toString()
            val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

            TransitionManager.beginDelayedTransition(transitionsContainer)
            visible = !visible
            nameAndSurnameTextView.visibility = if (visible) View.VISIBLE else View.GONE


            if (it.get("personType").toString() == "Student") {
                studySearchEditText.visibility = View.VISIBLE
                searchEditText.visibility = View.GONE
                studentDenemeButton.visibility = View.VISIBLE
                recyclerViewPreviousStudies.visibility = View.VISIBLE
                istatistikButton.visibility = View.GONE
                allStudentsBtn.visibility = View.GONE
                addStudyButton.visibility = View.VISIBLE

                sayacButton.visibility = View.VISIBLE
                gorevButton.visibility = View.VISIBLE

                contentTextView.text = "Bu Haftaki Çalışmalarım"
                val cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                val baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                val bitisTarihi = cal.time

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
                                timestamp
                            )

                            studyList.add(currentStudy)

                        }

                        setupStudyRecyclerView(studyList)

                        recyclerViewPreviousStudiesAdapter.notifyDataSetChanged()

                    }


            } else if (it.get("personType").toString() == "Teacher") {
                studySearchEditText.visibility = View.GONE
                searchEditText.visibility = View.VISIBLE
                recyclerViewMyStudents.visibility = View.VISIBLE
                studentDenemeButton.visibility = View.GONE
                allStudentsBtn.visibility = View.VISIBLE
                addStudyButton.visibility = View.GONE
                istatistikButton.visibility = View.VISIBLE
                gorevButton.visibility = View.GONE
                contentTextView.text = "Öğrencilerim"




                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .whereEqualTo("teacher", auth.uid.toString()).addSnapshotListener { it2, _ ->

                        studentList.clear()
                        val documents = it2!!.documents
                        for (document in documents) {
                            val studentName = document.get("nameAndSurname").toString()
                            val teacher = document.get("teacher").toString()
                            val id = document.get("id").toString()
                            val currentStudent = Student(studentName, teacher, id)
                            studentList.add(currentStudent)

                        }
                        studentList.sortBy { a ->
                            a.studentName
                        }
                        setupStudentRecyclerView(studentList)

                        recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

                    }

            }



            TransitionManager.beginDelayedTransition(sayacContainer)
            sayacVisible = !sayacVisible
            contentTextView.visibility = if (sayacVisible) View.VISIBLE else View.GONE

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
            signOutAlertDialog.setTitle("Çıkış Yap")
            signOutAlertDialog.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
            signOutAlertDialog.setPositiveButton("Çıkış") { _, _ ->
                signOut()
                finish()
            }
            signOutAlertDialog.setNegativeButton("İptal") { _, _ ->

            }
            signOutAlertDialog.show()

        }

        sayacButton.setOnClickListener {
            val intent = Intent(this, ChronometerActivity::class.java)
            this.startActivity(intent)
        }

        gorevButton.setOnClickListener {
            val intent = Intent(this, DutiesActivity::class.java)
            intent.putExtra("studentID", auth.uid)
            this.startActivity(intent)
        }


    }

    private fun setupStudentRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter = StudentsRecyclerAdapter(list)

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
}