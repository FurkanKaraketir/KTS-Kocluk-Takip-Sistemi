package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
    private lateinit var layoutManager: GridLayoutManager

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        recyclerViewStudies = binding.recyclerViewStudies

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
            "Tüm Zamanlar" -> {
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
                                val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()
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
                                                iCozulen += study.get("çözülenSoru").toString()
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

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Çalışma Durumu")
            alertDialog.setMessage("Çalışma Durumunu 5 Yıldız Olarak Değerlendirmek İstiyor musunuz?")
            alertDialog.setPositiveButton("5 Yıldız") { _, _ ->
                val notificationsSender = FcmNotificationsSenderService(
                    "/topics/$studentID",
                    "Çalışmanızın Durumu",
                    "Çalışmanızın Durumu 5 Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                    this
                )
                notificationsSender.sendNotifications()
                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()

        }
        fourStarButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Çalışma Durumu")
            alertDialog.setMessage("Çalışma Durumunu 4 Yıldız Olarak Değerlendirmek İstiyor musunuz?")
            alertDialog.setPositiveButton("4 Yıldız") { _, _ ->
                val notificationsSender = FcmNotificationsSenderService(
                    "/topics/$studentID",
                    "Çalışmanızın Durumu",
                    "Çalışmanızın Durumu 4 Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                    this
                )
                notificationsSender.sendNotifications()
                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()
        }

        treeStarButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Çalışma Durumu")
            alertDialog.setMessage("Çalışma Durumunu 3 Yıldız Olarak Değerlendirmek İstiyor musunuz?")
            alertDialog.setPositiveButton("3 Yıldız") { _, _ ->
                val notificationsSender = FcmNotificationsSenderService(
                    "/topics/$studentID",
                    "Çalışmanızın Durumu",
                    "Çalışmanızın Durumu 3 Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                    this
                )
                notificationsSender.sendNotifications()
                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()

            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()
        }

        twoStarButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Çalışma Durumu")
            alertDialog.setMessage("Çalışma Durumunu 2 Yıldız Olarak Değerlendirmek İstiyor musunuz?")
            alertDialog.setPositiveButton("2 Yıldız") { _, _ ->
                val notificationsSender = FcmNotificationsSenderService(
                    "/topics/$studentID",
                    "Çalışmanızın Durumu",
                    "Çalışmanızın Durumu 2 Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                    this
                )
                notificationsSender.sendNotifications()
                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()

            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()
        }

        oneStarButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Çalışma Durumu")
            alertDialog.setMessage("Çalışma Durumunu 1 Yıldız Olarak Değerlendirmek İstiyor musunuz?")
            alertDialog.setPositiveButton("1 Yıldız") { _, _ ->
                val notificationsSender = FcmNotificationsSenderService(
                    "/topics/$studentID",
                    "Çalışmanızın Durumu",
                    "Çalışmanızın Durumu 1 Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                    this
                )
                notificationsSender.sendNotifications()
                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()

            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()
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

}