@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportBinding
import com.karaketir.coachingapp.models.Student
import com.karaketir.coachingapp.services.ExcelExportHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.Calendar
import java.util.Date


class NoReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoReportBinding
    private var secilenZaman = "Bugün"
    private var secilenGrade = "Bütün Sınıflar"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 763455
    private var studentList = ArrayList<Student>()
    private var raporGondermeyenList = ArrayList<Student>()


    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date

    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNoReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val recyclerViewMyStudents = binding.noReportRecycler
        val noReportDayCounterButton = binding.noReportDayCounterButton
        val noReportExcelButton = binding.noReportExcelButton
        raporGondermeyenList = intent.getSerializableExtra("list") as ArrayList<Student>
        raporGondermeyenList.sortBy { it.studentName }
        secilenZaman = intent.getStringExtra("secilenZaman").toString()
        baslangicTarihi = intent.getSerializableExtra("baslangicTarihi") as Date
        bitisTarihi = intent.getSerializableExtra("bitisTarihi") as Date

        secilenGrade = intent.getStringExtra("grade").toString()

        val titleNoReport = binding.titleNoReport

        if (secilenZaman == "Bugün") {
            secilenZaman = "Dün"
        }
        kurumKodu = intent.getStringExtra("kurumKodu")!!.toInt()
        titleNoReport.text = "Zaman Aralığı: $secilenZaman \nSeçilen Sınıf: $secilenGrade"


        loadTeacherStudentList()


        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter = StudentsRecyclerAdapter(
            raporGondermeyenList,
            kurumKodu,
            baslangicTarihi,
            bitisTarihi,
            secilenZaman,
            useLiveListeners = false,
        )

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter

        noReportDayCounterButton.setOnClickListener {
            val newIntent = Intent(this, NoReportDayCountActivity::class.java)
            newIntent.putExtra("kurumKodu", kurumKodu.toString())
            newIntent.putExtra("studentList", studentList)
            newIntent.putExtra("grade", secilenGrade)
            newIntent.putExtra("secilenZaman", secilenZaman)
            this.startActivity(newIntent)
        }

        for (i in raporGondermeyenList) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            val day = cal[Calendar.DAY_OF_MONTH]
            val month = cal[Calendar.MONTH]
            val year = cal[Calendar.YEAR]
            val newDocID = "${day}-${month}-${year}"
            val newData = hashMapOf("id" to newDocID, "timestamp" to cal.time)

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(i.id).collection("NoDayReport").document(newDocID)
                .set(newData as Map<String, Any>)


        }

        noReportExcelButton.setOnClickListener {
            exportNoReportExcel()
        }
    }

    private fun loadTeacherStudentList() {
        val teacherId = auth.uid ?: return
        lifecycleScope.launch {
            try {
                var query = db.collection("School").document(kurumKodu.toString())
                    .collection("Student")
                    .whereEqualTo("teacher", teacherId)
                if (secilenGrade != "Bütün Sınıflar") {
                    query = query.whereEqualTo("grade", secilenGrade.toInt())
                }
                val snap = query.orderBy("nameAndSurname").get().await()
                studentList.clear()
                for (doc in snap.documents) {
                    studentList.add(
                        Student(
                            doc.get("nameAndSurname").toString(),
                            doc.get("teacher").toString(),
                            doc.id,
                            doc.get("grade").toString().toInt(),
                        )
                    )
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                exportNoReportExcel()
            }
        }

    private fun exportNoReportExcel() {
        if (raporGondermeyenList.isEmpty()) {
            Toast.makeText(this, "Dışa aktarılacak öğrenci yok", Toast.LENGTH_SHORT).show()
            return
        }
        if (ExcelExportHelper.needsLegacyStoragePermission() &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        Toast.makeText(this, getString(R.string.excel_lutfen_bekleyin), Toast.LENGTH_SHORT).show()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Rapor Göndermeyenler")
        val headers = arrayOf("Ad Soyad", "Sınıf")
        val headerStyle = ExcelExportHelper.createHeaderStyle(workbook)
        val columnWidths = intArrayOf(35 * 256, 12 * 256)

        val infoEnd = ExcelExportHelper.writeReportInfoBlock(
            sheet,
            ExcelExportHelper.ReportContext(
                title = getString(R.string.excel_rapor_gondermeyen),
                gradeLabel = secilenGrade,
                timeRangeLabel = secilenZaman,
                startDate = baslangicTarihi,
                endDate = bitisTarihi,
                extraLines = listOf("Öğrenci sayısı: ${raporGondermeyenList.size}")
            ),
            headers.size
        )
        var rowIndex = ExcelExportHelper.writeStyledHeaderRow(
            sheet, headers, headerStyle, infoEnd, columnWidths
        )

        raporGondermeyenList.forEach { student ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(student.studentName)
            row.createCell(1).setCellValue(student.grade.toString())
        }

        val fileName = ExcelExportHelper.buildFileName(
            "Rapor_Gondermeyen",
            secilenGrade,
            secilenZaman
        )
        ExcelExportHelper.saveWorkbook(this, workbook, fileName)
    }
}
