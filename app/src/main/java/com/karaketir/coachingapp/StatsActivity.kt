@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.adapter.StatisticsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityStatsBinding
import com.karaketir.coachingapp.models.Statistic
import com.karaketir.coachingapp.services.ExcelExportHelper
import com.karaketir.coachingapp.services.StudyQueryHelper
import kotlinx.coroutines.launch
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityStatsBinding
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var layoutManager: GridLayoutManager
    private var secilenZamanAraligi = ""
    private var kurumKodu = 0
    private var secilenGrade = ""
    private lateinit var recyclerViewStats: RecyclerView
    private lateinit var recyclerViewStatsAdapter: StatisticsRecyclerAdapter
    private var dersSoruHash = hashMapOf<String, Float>()
    private var dersSureHash = hashMapOf<String, Float>()
    private var statsList = ArrayList<Statistic>()
    private var ogrenciSayisi = 0
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0", "13")

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

    @SuppressLint("SetTextI18n")
    private fun exportStatsExcel() {
        if (statsList.isEmpty()) {
            Toast.makeText(this, "Dışa aktarılacak veri yok", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, getString(R.string.excel_lutfen_bekleyin), Toast.LENGTH_SHORT).show()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("İstatistik")
        val headers = arrayOf(
            "Ders",
            "Ortalama Süre (dk)",
            "Ortalama Süre (saat)",
            "Ortalama Soru"
        )
        val headerStyle = ExcelExportHelper.createHeaderStyle(workbook)
        val columnWidths = intArrayOf(30 * 256, 25 * 256, 25 * 256, 25 * 256)

        val infoEnd = ExcelExportHelper.writeReportInfoBlock(
            sheet,
            ExcelExportHelper.ReportContext(
                title = getString(R.string.excel_rapor_istatistik),
                gradeLabel = secilenGrade,
                timeRangeLabel = secilenZamanAraligi,
                startDate = baslangicTarihi,
                endDate = bitisTarihi,
                extraLines = listOf("Öğrenci sayısı: $ogrenciSayisi")
            ),
            headers.size
        )
        var rowIndex = ExcelExportHelper.writeStyledHeaderRow(
            sheet, headers, headerStyle, infoEnd, columnWidths
        )

        statsList.sortedBy { it.dersAdi }.forEach { stat ->
            val row = sheet.createRow(rowIndex++)
            CellUtil.createCell(row, 0, stat.dersAdi)
            CellUtil.createCell(row, 1, stat.toplamCalisma)
            CellUtil.createCell(
                row,
                2,
                String.format(Locale.US, "%.2f", stat.toplamCalisma.toFloat() / 60f)
            )
            CellUtil.createCell(row, 3, stat.cozulenSoru)
        }

        rowIndex += 1
        val summaryRow = sheet.createRow(rowIndex)
        val totalTime = statsList.sumOf { it.toplamCalisma.toDouble() }.toFloat()
        val totalSoru = statsList.sumOf { it.cozulenSoru.toDouble() }.toFloat()
        CellUtil.createCell(summaryRow, 0, "Toplam")
        CellUtil.createCell(summaryRow, 1, totalTime.format(2))
        CellUtil.createCell(
            summaryRow,
            2,
            String.format(Locale.US, "%.2f", totalTime / 60f)
        )
        CellUtil.createCell(summaryRow, 3, totalSoru.format(2))

        val fileName = ExcelExportHelper.buildFileName(
            "Istatistik",
            secilenGrade,
            secilenZamanAraligi
        )
        ExcelExportHelper.saveWorkbook(
            this,
            workbook,
            fileName,
            ExcelExportHelper.FOLDER_STATS
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        val fileSaveButton = binding.fileSaveExcelButton

        layoutManager = GridLayoutManager(applicationContext, 2)
        val statsZamanSpinner = binding.statsZamanAraligiSpinner

        val statsGradeSpinner = binding.statsGradeSpinner

        val statsAdapter = ArrayAdapter(
            this@StatsActivity, android.R.layout.simple_spinner_item, zamanAraliklari
        )

        val gradeAdapter = ArrayAdapter(
            this@StatsActivity, android.R.layout.simple_spinner_item, gradeList
        )

        statsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statsZamanSpinner.adapter = statsAdapter


        fileSaveButton.setOnClickListener {
            if (ExcelExportHelper.needsLegacyStoragePermission() &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                exportStatsExcel()
            }
        }




        statsZamanSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                statsGradeSpinner.adapter = gradeAdapter
                statsGradeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemSelected(
                        p0: AdapterView<*>?, p1: View?, position2: Int, p3: Long
                    ) {
                        secilenGrade = gradeList[position2]
                        secilenZamanAraligi = zamanAraliklari[position]
                        statsList.clear()
                        dersSureHash.clear()
                        dersSoruHash.clear()

                        recyclerViewStats = binding.statsRecyclerView
                        recyclerViewStats.layoutManager = layoutManager

                        recyclerViewStatsAdapter = StatisticsRecyclerAdapter(statsList)

                        recyclerViewStats.adapter = recyclerViewStatsAdapter
                        recyclerViewStatsAdapter.notifyDataSetChanged()


                        var cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

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

                            "Son 2 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -2)
                                baslangicTarihi = cal.time
                            }

                            "Son 3 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -3)
                                baslangicTarihi = cal.time
                            }

                            "Son 4 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -4)
                                baslangicTarihi = cal.time
                            }

                            "Son 5 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -5)
                                baslangicTarihi = cal.time
                            }

                            "Son 6 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

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


                        loadStatsData()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        val weakHandler = Handler(WeakReferenceHandlerCallback(this))

        weakHandler.post(object : Runnable {
            override fun run() {
                // Keep the postDelayed before the updateTime(), so when the event ends, the handler will stop too.
                weakHandler.postDelayed(this, 2000)
                showSum()
            }
        })


    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun loadStatsData() {
        val teacherId = auth.uid ?: return
        val gradeFilter = if (secilenGrade == "Bütün Sınıflar") {
            null
        } else {
            secilenGrade.toIntOrNull()
        }

        statsList.clear()
        dersSureHash.clear()
        dersSoruHash.clear()

        recyclerViewStats = binding.statsRecyclerView
        recyclerViewStats.layoutManager = layoutManager
        recyclerViewStatsAdapter = StatisticsRecyclerAdapter(statsList)
        recyclerViewStats.adapter = recyclerViewStatsAdapter

        lifecycleScope.launch {
            try {
                val studentIds = StudyQueryHelper.fetchStudentIdsForTeacher(
                    db,
                    kurumKodu.toString(),
                    teacherId,
                    gradeFilter,
                )
                ogrenciSayisi = studentIds.size
                if (studentIds.isEmpty()) {
                    updateStatsListFromHashes()
                    return@launch
                }

                val merged = StudyQueryHelper.fetchClassStatsAggregates(
                    db,
                    kurumKodu.toString(),
                    studentIds,
                    baslangicTarihi,
                    bitisTarihi,
                )

                dersSureHash.clear()
                dersSoruHash.clear()
                merged.forEach { (ders, totals) ->
                    dersSureHash[ders] = totals.minutes.toFloat()
                    dersSoruHash[ders] = totals.questions.toFloat()
                }
                updateStatsListFromHashes()
            } catch (e: Exception) {
                println(e.localizedMessage)
                Toast.makeText(
                    this@StatsActivity,
                    "İstatistikler yüklenemedi",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateStatsListFromHashes() {
        if (ogrenciSayisi <= 0) {
            statsList.clear()
            recyclerViewStatsAdapter.notifyDataSetChanged()
            showSum()
            return
        }
        statsList.clear()
        dersSureHash.forEach { (dersAdi, toplamSure) ->
            val ortalamaSure = (toplamSure / ogrenciSayisi).format(2)
            val ortalamaSoru = ((dersSoruHash[dersAdi] ?: 0f) / ogrenciSayisi).format(2)
            statsList.add(Statistic(dersAdi, ortalamaSure, ortalamaSoru))
        }
        statsList.sortBy { it.dersAdi }
        recyclerViewStatsAdapter.notifyDataSetChanged()
        showSum()
    }

    @SuppressLint("SetTextI18n")
    private fun showSum() {
        var toplamSure = 0f
        var toplamSoru = 0f
        if (statsList.isNotEmpty()) {
            for (i in statsList) {
                toplamSure += i.toplamCalisma.toFloat()
                toplamSoru += i.cozulenSoru.toFloat()
            }
        }


        val toplamSureSaat = toplamSure / 60
        binding.toplamSure.text = toplamSure.format(2) + "dk " + "(${
            toplamSureSaat.format(2)
        } Saat)"
        binding.toplamSoru.text = "${toplamSoru.format(2)} Soru"
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                exportStatsExcel()
            }
        }

    // Define a WeakReferenceHandlerCallback class
    class WeakReferenceHandlerCallback(activity: StatsActivity) : Handler.Callback {
        private val weakReference = WeakReference(activity)

        override fun handleMessage(msg: Message): Boolean {
            weakReference.get()

            return true
        }
    }
}