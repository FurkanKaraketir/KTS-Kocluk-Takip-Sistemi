package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.ClassesAdapter
import com.karaketir.coachingapp.databinding.ActivityStudiesBinding
import com.karaketir.coachingapp.services.FcmNotificationsSenderService
import java.util.*
import kotlin.collections.ArrayList


class StudiesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewStudiesAdapter: ClassesAdapter

    private lateinit var recyclerViewStudies: RecyclerView
    private var studyList = ArrayList<com.karaketir.coachingapp.models.Class>()
    private var classList = ArrayList<String>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var binding: ActivityStudiesBinding
    private var secilenZamanAraligi = ""
    private var studentID = ""
    private var kurumKodu = 0
    private lateinit var layoutManager: GridLayoutManager

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        recyclerViewStudies = binding.recyclerViewStudies
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
        }
        layoutManager = GridLayoutManager(applicationContext, 2)
        val intent = intent
        studentID = intent.getStringExtra("studentID").toString()
        secilenZamanAraligi = intent.getStringExtra("secilenZaman").toString()
        val gorevlerButton = binding.gorevTeacherButton
        val denemelerButton = binding.denemeTeacherButton
        val hedefTeacherButton = binding.hedefTeacherButton
        val toplamSureText = binding.toplamSureText
        val toplamSoruText = binding.toplamSoruText
        val nameTextView = binding.studentNameForTeacher
        val fiveStarButton = binding.fiveStarButton
        val fourStarButton = binding.fourStarButton
        val treeStarButton = binding.threeStarButton
        val twoStarButton = binding.twoStarButton
        val oneStarButton = binding.oneStarButton

        setupStudyRecyclerView(studyList)
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !
        db.collection("User").document(studentID).get().addOnSuccessListener {
            val name = it.get("nameAndSurname").toString()
            nameTextView.text = name

        }
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        when (secilenZamanAraligi) {

            "Bug??n" -> {
                baslangicTarihi = cal.time
                binding.starScroll.visibility = View.VISIBLE

                cal.add(Calendar.DAY_OF_YEAR, 1)
                bitisTarihi = cal.time
            }
            "D??n" -> {
                binding.starScroll.visibility = View.VISIBLE
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
            "Ge??en Hafta" -> {
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
            "Ge??en Ay" -> {
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
            "T??m Zamanlar" -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time


                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time

            }

        }
        var toplamSure = 0
        var toplamSoru = 0

        db.collection("Lessons").orderBy("dersAdi", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    studyList.clear()
                    classList.clear()
                    for (i in value) {
                        classList.add(i.id)
                    }

                    for (i in classList) {
                        var iCalisma = 0
                        var iCozulen = 0
                        db.collection("User").document(auth.uid.toString()).get()
                            .addOnSuccessListener {
                                kurumKodu = it.get("kurumKodu")?.toString()?.toInt()!!
                                db.collection("School").document(kurumKodu.toString())
                                    .collection("Student").document(studentID).collection("Studies")
                                    .whereEqualTo("dersAdi", i)
                                    .whereGreaterThan("timestamp", baslangicTarihi)
                                    .whereLessThan("timestamp", bitisTarihi)
                                    .addSnapshotListener { value, error ->
                                        if (error != null) {
                                            println(error.localizedMessage)
                                        }

                                        if (value != null) {
                                            for (study in value) {
                                                iCalisma += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                iCozulen += study.get("????z??lenSoru").toString()
                                                    .toInt()
                                            }
                                        }
                                        toplamSoru += iCozulen
                                        toplamSure += iCalisma
                                        val currentClass = com.karaketir.coachingapp.models.Class(
                                            i,
                                            studentID,
                                            baslangicTarihi,
                                            bitisTarihi,
                                            secilenZamanAraligi,
                                            iCozulen,
                                            iCalisma
                                        )
                                        val toplamSureSaat = toplamSure.toFloat() / 60
                                        toplamSureText.text = toplamSure.toString() + "dk " + "(${
                                            toplamSureSaat.format(2)
                                        } Saat)"
                                        toplamSoruText.text = "$toplamSoru Soru"

                                        studyList.add(currentClass)

                                        recyclerViewStudiesAdapter.notifyDataSetChanged()

                                    }

                            }
                    }


                }
            }




        fiveStarButton.setOnClickListener {
            starFun(5)
        }
        fourStarButton.setOnClickListener {
            starFun(4)
        }

        treeStarButton.setOnClickListener {
            starFun(3)
        }

        twoStarButton.setOnClickListener {
            starFun(2)
        }

        oneStarButton.setOnClickListener {
            starFun(1)
        }

        hedefTeacherButton.setOnClickListener {
            val intent2 = Intent(this, GoalsActivity::class.java)
            intent2.putExtra("studentID", studentID)
            this.startActivity(intent2)
        }




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
    private fun setupStudyRecyclerView(list: ArrayList<com.karaketir.coachingapp.models.Class>) {
        val layoutManager = GridLayoutManager(applicationContext, 2)

        recyclerViewStudies.layoutManager = layoutManager

        recyclerViewStudiesAdapter = ClassesAdapter(list)

        recyclerViewStudies.adapter = recyclerViewStudiesAdapter
        recyclerViewStudiesAdapter.notifyDataSetChanged()

    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

    private fun starFun(yildisSayisi: Int) {

        val now = Calendar.getInstance()


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("??al????ma Durumu")
        alertDialog.setMessage("??al????ma Durumunu $yildisSayisi Y??ld??z Olarak De??erlendirmek ??stiyor musunuz?")
        alertDialog.setPositiveButton("$yildisSayisi Y??ld??z") { _, _ ->

            if (secilenZamanAraligi == "Bug??n") {
                val degerlendirmeHash = hashMapOf(
                    "yildizSayisi" to yildisSayisi,
                    "time" to now.time,
                    "degerlendirmeDate" to now.time
                )

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(studentID).collection("Degerlendirme").document()
                    .set(degerlendirmeHash).addOnSuccessListener {
                        val notificationsSender = FcmNotificationsSenderService(
                            "/topics/$studentID",
                            "??al????man??z??n Durumu",
                            "??al????man??z??n Durumu $yildisSayisi Y??ld??z Olarak De??erlendirildi. \n??al????ma Tarihi: $secilenZamanAraligi",
                            this
                        )
                        notificationsSender.sendNotifications()
                        Toast.makeText(this, "????lem Ba??ar??l??!", Toast.LENGTH_SHORT).show()

                    }
            } else {
                now.add(Calendar.DAY_OF_YEAR, -1)
                val degerlendirmeHash = hashMapOf(
                    "yildizSayisi" to yildisSayisi,
                    "time" to Calendar.getInstance().time,
                    "degerlendirmeDate" to now.time
                )

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(studentID).collection("Degerlendirme").document()
                    .set(degerlendirmeHash).addOnSuccessListener {
                        val notificationsSender = FcmNotificationsSenderService(
                            "/topics/$studentID",
                            "??al????man??z??n Durumu",
                            "??al????man??z??n Durumu $yildisSayisi Y??ld??z Olarak De??erlendirildi. \n??al????ma Tarihi: $secilenZamanAraligi",
                            this
                        )
                        notificationsSender.sendNotifications()
                        Toast.makeText(this, "????lem Ba??ar??l??!", Toast.LENGTH_SHORT).show()

                    }
            }

        }
        alertDialog.setNegativeButton("??ptal") { _, _ ->

        }
        alertDialog.show()


    }

}