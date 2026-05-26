@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.adapter.ClassesAdapter
import com.karaketir.coachingapp.curriculum.StudyLabels
import com.karaketir.coachingapp.databinding.ActivityStudiesBinding
import com.karaketir.coachingapp.services.ExcelExportHelper
import com.karaketir.coachingapp.services.FcmNotificationsSenderService
import com.karaketir.coachingapp.services.StudyQueryHelper
import com.karaketir.coachingapp.services.StudyTotals
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.Calendar
import java.util.Date


class StudiesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewStudiesAdapter: ClassesAdapter
    private lateinit var recyclerViewStudies: RecyclerView
    private var studyList = mutableListOf<com.karaketir.coachingapp.models.Class>()
    private var classList = mutableListOf<String>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var binding: ActivityStudiesBinding
    private var secilenZamanAraligi = ""

    private var studentID = ""
    var name = ""
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

        kurumKodu = intent.getStringExtra("kurumKodu")?.toIntOrNull() ?: 0


        layoutManager = GridLayoutManager(applicationContext, 2)
        val intent = intent
        studentID = intent.getStringExtra("studentID").toString()
        secilenZamanAraligi = intent.getStringExtra("secilenZaman").toString()
        baslangicTarihi = intent.getSerializableExtra("baslangicTarihi") as Date
        bitisTarihi = intent.getSerializableExtra("bitisTarihi") as Date
        when (secilenZamanAraligi) {

            "Bugün" -> {
                binding.starScroll.visibility = View.VISIBLE
            }

            "Dün" -> {
                binding.starScroll.visibility = View.VISIBLE

            }

            else -> {
                binding.starScroll.visibility = View.GONE
            }
        }


        val dersProgramiTeacherButton = binding.dersProgramiTeacherButton
        val previousRatingsButton = binding.previousRatingsButton
        val gorevlerButton = binding.gorevTeacherButton
        val hedefTeacherButton = binding.hedefTeacherButton
        val toplamSureText = binding.toplamSureText
        val toplamSoruText = binding.toplamSoruText
        val nameTextView = binding.studentNameForTeacher
        val fiveStarButton = binding.fiveStarButton
        val fourStarButton = binding.fourStarButton
        val treeStarButton = binding.threeStarButton
        val twoStarButton = binding.twoStarButton
        val oneStarButton = binding.oneStarButton
        val zamanAraligiTextView = binding.zamanAraligiTextView
        val excelCreateButton = binding.excelStudentButton

        setupStudyRecyclerView(studyList)

        previousRatingsButton.setOnClickListener {
            val newIntent = Intent(this, PreviousRatingsActivity::class.java)
            newIntent.putExtra("personType", "Teacher")
            newIntent.putExtra("studentID", studentID)
            newIntent.putExtra("kurumKodu", kurumKodu.toString())
            this.startActivity(newIntent)
        }

        println(studentID)

        db.collection("User").document(studentID).get().addOnSuccessListener {
            name = it.get("nameAndSurname").toString()
            nameTextView.text = name
        }
        zamanAraligiTextView.text = secilenZamanAraligi

        excelCreateButton.setOnClickListener {
            exportStudentStudiesExcel()
        }

        loadStudiesForDateRange(toplamSureText, toplamSoruText)




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
            intent2.putExtra("personType", "Teacher")
            intent2.putExtra("kurumKodu", kurumKodu.toString())

            this.startActivity(intent2)
        }




        gorevlerButton.setOnClickListener {
            val intent2 = Intent(this, DutiesActivity::class.java)
            intent2.putExtra("studentID", studentID)
            intent2.putExtra("personType", "Teacher")
            intent2.putExtra("kurumKodu", kurumKodu.toString())
            this.startActivity(intent2)
        }

        dersProgramiTeacherButton.setOnClickListener {
            val newIntent = Intent(this, ProgramActivity::class.java)
            newIntent.putExtra("studentID", studentID)
            newIntent.putExtra("personType", "Teacher")
            newIntent.putExtra("kurumKodu", kurumKodu.toString())
            this.startActivity(newIntent)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setupStudyRecyclerView(list: List<com.karaketir.coachingapp.models.Class>) {
        val layoutManager = GridLayoutManager(applicationContext, 2)

        recyclerViewStudies.layoutManager = layoutManager

        recyclerViewStudiesAdapter = ClassesAdapter(list, kurumKodu)

        recyclerViewStudies.adapter = recyclerViewStudiesAdapter
        recyclerViewStudiesAdapter.notifyDataSetChanged()

    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun loadStudiesForDateRange(
        toplamSureText: android.widget.TextView,
        toplamSoruText: android.widget.TextView,
    ) {
        lifecycleScope.launch {
            try {
                val lessonsSnap = db.collection("Lessons")
                    .orderBy("dersAdi", Query.Direction.ASCENDING)
                    .get()
                    .await()
                classList.clear()
                lessonsSnap.documents.forEach { classList.add(it.id) }

                val byDers = StudyQueryHelper.fetchAggregatedByDers(
                    db,
                    kurumKodu.toString(),
                    studentID,
                    baslangicTarihi,
                    bitisTarihi,
                )

                studyList.clear()
                var toplamSure = 0
                var toplamSoru = 0
                for (dersAdi in classList) {
                    val totals = byDers[dersAdi] ?: StudyTotals()
                    toplamSure += totals.minutes
                    toplamSoru += totals.questions
                    studyList.add(
                        com.karaketir.coachingapp.models.Class(
                            dersAdi,
                            studentID,
                            baslangicTarihi,
                            bitisTarihi,
                            secilenZamanAraligi,
                            totals.questions,
                            totals.minutes,
                        )
                    )
                }

                val toplamSureSaat = toplamSure.toFloat() / 60
                toplamSureText.text = "${toplamSure}dk (${toplamSureSaat.format(2)} Saat)"
                toplamSoruText.text = "$toplamSoru Soru"
                recyclerViewStudiesAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                println(e.localizedMessage)
                Toast.makeText(
                    this@StudiesActivity,
                    "Çalışmalar yüklenemedi",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun starFun(yildisSayisi: Int) {

        val now = Calendar.getInstance()


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Çalışma Durumu")
        alertDialog.setMessage("Çalışma Durumunu $yildisSayisi Yıldız Olarak Değerlendirmek İstiyor musunuz?")
        alertDialog.setPositiveButton("$yildisSayisi Yıldız") { _, _ ->

            if (secilenZamanAraligi == "Bugün") {
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
                            "Çalışmanızın Durumu",
                            "Çalışmanızın Durumu $yildisSayisi Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                            this
                        )
                        notificationsSender.sendNotifications()
                        Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()

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
                            "Çalışmanızın Durumu",
                            "Çalışmanızın Durumu $yildisSayisi Yıldız Olarak Değerlendirildi. \nÇalışma Tarihi: $secilenZamanAraligi",
                            this
                        )
                        notificationsSender.sendNotifications()
                        Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()

                    }
            }

        }
        alertDialog.setNegativeButton("İptal") { _, _ ->

        }
        alertDialog.show()


    }

    private fun exportStudentStudiesExcel() {
        if (ExcelExportHelper.needsLegacyStoragePermission() &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return
        }

        Toast.makeText(this, getString(R.string.excel_lutfen_bekleyin), Toast.LENGTH_SHORT).show()

        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Çalışmalar")
        val headers = arrayOf(
            "Ders Adı",
            "Program",
            "Çalışma Türü",
            "Tema",
            "Konu Adı",
            "Toplam Çalışma (dk)",
            "Toplam Çalışma (saat)",
            "Çözülen Soru"
        )
        val headerStyle = ExcelExportHelper.createHeaderStyle(workbook)
        val columnWidths = intArrayOf(
            30 * 256, 18 * 256, 22 * 256, 22 * 256, 28 * 256, 20 * 256, 20 * 256, 18 * 256
        )

        val infoEnd = ExcelExportHelper.writeReportInfoBlock(
            sheet,
            ExcelExportHelper.ReportContext(
                title = getString(R.string.excel_rapor_ogrenci_calisma),
                gradeLabel = name.ifBlank { "Öğrenci" },
                timeRangeLabel = secilenZamanAraligi,
                startDate = baslangicTarihi,
                endDate = bitisTarihi
            ),
            headers.size
        )
        var rowIndex = ExcelExportHelper.writeStyledHeaderRow(
            sheet, headers, headerStyle, infoEnd, columnWidths
        )
        val decimalStyle = ExcelExportHelper.createDecimalStyle(workbook)

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("Studies")
            .whereGreaterThan("timestamp", baslangicTarihi)
            .whereLessThan("timestamp", bitisTarihi)
            .get()
            .addOnSuccessListener { studies ->
                if (studies.isEmpty) {
                    Toast.makeText(this, "Dışa aktarılacak çalışma yok", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                for (study in studies) {
                    val row = sheet.createRow(rowIndex++)
                    CellUtil.createCell(row, 0, study.getString("dersAdi").orEmpty())
                    CellUtil.createCell(row, 1, StudyLabels.programLabel(study.getString("program")))
                    CellUtil.createCell(row, 2, StudyLabels.studyTypeLabel(study))
                    CellUtil.createCell(row, 3, study.getString("temaAdi").orEmpty())
                    CellUtil.createCell(row, 4, StudyLabels.studyTrackingLabel(study))

                    val minutes = study.get("toplamCalisma")?.toString()?.toDoubleOrNull() ?: 0.0
                    val questions = study.get("çözülenSoru")?.toString()?.toDoubleOrNull() ?: 0.0

                    val minutesCell = row.createCell(5)
                    minutesCell.setCellValue(minutes)
                    minutesCell.cellStyle = decimalStyle

                    val hoursCell = row.createCell(6)
                    hoursCell.setCellValue(minutes / 60.0)
                    hoursCell.cellStyle = decimalStyle

                    val questionsCell = row.createCell(7)
                    questionsCell.setCellValue(questions)
                    questionsCell.cellStyle = decimalStyle
                }

                val safeName = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(40)
                val fileName = ExcelExportHelper.buildFileName(
                    "Ogrenci_Calisma",
                    safeName,
                    secilenZamanAraligi
                )
                ExcelExportHelper.saveWorkbook(
                    this,
                    workbook,
                    fileName,
                    ExcelExportHelper.FOLDER_STATS
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    getString(R.string.excel_dosya_hata, e.localizedMessage ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            exportStudentStudiesExcel()
        }
    }

}