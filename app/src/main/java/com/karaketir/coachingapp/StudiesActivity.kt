package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.StudiesRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityStudiesBinding
import com.karaketir.coachingapp.models.Study
import java.util.*


class StudiesActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewStudiesAdapter: StudiesRecyclerAdapter

    private lateinit var recyclerViewStudies: RecyclerView
    private var studyList = ArrayList<Study>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var binding: ActivityStudiesBinding
    private var secilenZamanAraligi = ""
    private var studentID = ""
    var filteredList = ArrayList<Study>()
    private val zamanAraliklari =
        arrayOf("Bu Hafta", "Geçen Hafta", "Bu Ay", "Geçen Ay", "Tüm Zamanlar")
    private lateinit var layoutManager: GridLayoutManager

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        recyclerViewStudies = binding.recyclerViewStudies

        layoutManager = GridLayoutManager(applicationContext, 2)
        val intent = intent
        val studyZamanSpinner = binding.studyZamanAraligiSpinner
        val gorevlerButton = binding.gorevTeacherButton
        val denemelerButton = binding.denemeTeacherButton
        val hedefTeacherButton = binding.hedefTeacherButton
        val calismaAra = binding.searchStudy
        val studyAdapter = ArrayAdapter(
            this@StudiesActivity, R.layout.simple_spinner_item, zamanAraliklari
        )


        calismaAra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredList = ArrayList()
                if (p0.toString() != "") {
                    for (item in studyList) {
                        if (item.studyDersAdi.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(item)
                        }
                    }
                    setupStudyRecyclerView(filteredList)
                } else {
                    setupStudyRecyclerView(studyList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        hedefTeacherButton.setOnClickListener {
            val intent2 = Intent(this, GoalsActivity::class.java)
            intent2.putExtra("studentID", studentID)
            this.startActivity(intent2)
        }

        studentID = intent.getStringExtra("studentID").toString()

        studyAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        studyZamanSpinner.adapter = studyAdapter
        studyZamanSpinner.onItemSelectedListener = this


        denemelerButton.setOnClickListener {
            val intent2 = Intent(this, DenemelerActivity::class.java)
            intent2.putExtra("studentID", studentID)
            this.startActivity(intent2)
        }

        gorevlerButton.setOnClickListener {
            val intent2 = Intent(this, DutiesActivity::class.java)
            intent2.putExtra("studentID", studentID)
            this.startActivity(intent2)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        secilenZamanAraligi = zamanAraliklari[position]

        setupStudyRecyclerView(studyList)
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        when (position) {
            0 -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                bitisTarihi = cal.time

            }
            1 -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                bitisTarihi = cal.time


                cal.add(Calendar.DAY_OF_YEAR, -7)
                baslangicTarihi = cal.time


            }
            2 -> {

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
            3 -> {
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
            4 -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time


                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time

            }
        }


        var kurumKodu: Int
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(studentID).collection("Studies")
                .whereGreaterThan("timestamp", baslangicTarihi)
                .whereLessThan("timestamp", bitisTarihi)
                .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->
                    if (value != null) {
                        studyList.clear()
                        if (!value.isEmpty) {
                            for (document in value) {
                                val studyName = document.get("konuAdi").toString()
                                val sure = document.get("toplamCalisma").toString()
                                val studyDersAdi = document.get("dersAdi").toString()
                                val studyTur = document.get("tür").toString()
                                val soruSayisi = document.get("çözülenSoru").toString()
                                val timestamp = document.get("timestamp") as Timestamp

                                val currentStudy = Study(
                                    studyName,
                                    sure,
                                    studentID,
                                    studyDersAdi,
                                    studyTur,
                                    soruSayisi,
                                    timestamp
                                )
                                studyList.add(currentStudy)
                            }

                            recyclerViewStudiesAdapter.notifyDataSetChanged()

                        } else {
                            studyList.clear()
                            recyclerViewStudiesAdapter.notifyDataSetChanged()

                        }


                    } else {
                        studyList.clear()
                        recyclerViewStudiesAdapter.notifyDataSetChanged()

                    }

                }

        }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setupStudyRecyclerView(list: ArrayList<Study>) {
        val layoutManager = GridLayoutManager(applicationContext, 2)

        recyclerViewStudies.layoutManager = layoutManager

        recyclerViewStudiesAdapter = StudiesRecyclerAdapter(list, secilenZamanAraligi)

        recyclerViewStudies.adapter = recyclerViewStudiesAdapter
        recyclerViewStudiesAdapter.notifyDataSetChanged()

    }
}