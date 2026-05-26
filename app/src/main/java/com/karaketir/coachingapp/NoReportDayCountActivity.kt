@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
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
import com.karaketir.coachingapp.adapter.NoReportDayCounterAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportDayCountBinding
import com.karaketir.coachingapp.models.Student
import com.karaketir.coachingapp.services.ExcelExportHelper
import com.karaketir.coachingapp.services.StudyQueryHelper
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.Calendar
import java.util.Date
import java.util.TreeMap

class NoReportDayCountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoReportDayCountBinding
    private var secilenZaman = "Bugün"
    private var secilenGrade = "Bütün Sınıflar"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 763455
    private var studentList = ArrayList<Student>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var sortedMap: MutableMap<String, Int>
    private lateinit var dayCounterAdapter: NoReportDayCounterAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoReportDayCountBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        studentList = intent.getSerializableExtra("studentList") as ArrayList<Student>
        secilenZaman = intent.getStringExtra("secilenZaman").toString()
        secilenGrade = intent.getStringExtra("grade").toString()
        val titleNoReport = binding.titleNoReport
        val noReportExcelButton = binding.noReportExcelButton

        studentList.sortBy { it.studentName }

        titleNoReport.text = "Zaman Aralığı: $secilenZaman \nSeçilen Sınıf: $secilenGrade"
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

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

            "Son 30 Gün" -> {
                cal = Calendar.getInstance()

                bitisTarihi = cal.time

                cal.add(Calendar.DAY_OF_YEAR, -30)

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


        val layoutManager = LinearLayoutManager(applicationContext)
        binding.noReportDayCounterRecycler.layoutManager = layoutManager
        dayCounterAdapter = NoReportDayCounterAdapter(studentList, kurumKodu)
        binding.noReportDayCounterRecycler.adapter = dayCounterAdapter

        loadNoDayReportCounts()

        noReportExcelButton.setOnClickListener {
            exportDayCountExcel()
        }
    }

    private fun loadNoDayReportCounts() {
        lifecycleScope.launch {
            try {
                val counts = StudyQueryHelper.fetchNoDayReportCounts(
                    db,
                    kurumKodu.toString(),
                    studentList.map { it.id },
                    baslangicTarihi,
                    bitisTarihi,
                )
                sortedMap = TreeMap()
                studentList.forEach { student ->
                    sortedMap[student.studentName] = counts[student.id] ?: 0
                }
                dayCounterAdapter.updateDayCounts(counts)
            } catch (e: Exception) {
                println(e.localizedMessage)
                Toast.makeText(
                    this@NoReportDayCountActivity,
                    "Gün sayıları yüklenemedi",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                exportDayCountExcel()
            }
        }

    private fun exportDayCountExcel() {
        if (!::sortedMap.isInitialized || sortedMap.isEmpty()) {
            Toast.makeText(this, "Veri henüz yüklenmedi, lütfen bekleyin", Toast.LENGTH_SHORT).show()
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
        val sheet = workbook.createSheet("Gün Sayıları")
        val headers = arrayOf("Ad Soyad", "Rapor Atılmayan Gün Sayısı")
        val headerStyle = ExcelExportHelper.createHeaderStyle(workbook)
        val columnWidths = intArrayOf(35 * 256, 28 * 256)

        val infoEnd = ExcelExportHelper.writeReportInfoBlock(
            sheet,
            ExcelExportHelper.ReportContext(
                title = getString(R.string.excel_rapor_gun_sayisi),
                gradeLabel = secilenGrade,
                timeRangeLabel = secilenZaman,
                startDate = baslangicTarihi,
                endDate = bitisTarihi,
                extraLines = listOf("Kayıt sayısı: ${sortedMap.size}")
            ),
            headers.size
        )
        var rowIndex = ExcelExportHelper.writeStyledHeaderRow(
            sheet, headers, headerStyle, infoEnd, columnWidths
        )

        sortedMap.forEach { (studentName, dayCount) ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(studentName)
            row.createCell(1).setCellValue(dayCount.toDouble())
        }

        val fileName = ExcelExportHelper.buildFileName(
            "Rapor_Gun_Sayisi",
            secilenGrade,
            secilenZaman
        )
        ExcelExportHelper.saveWorkbook(this, workbook, fileName)
    }

}