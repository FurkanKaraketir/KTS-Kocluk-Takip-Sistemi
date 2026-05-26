package com.karaketir.coachingapp.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.adapter.StatisticsRecyclerAdapter
import com.karaketir.coachingapp.databinding.FragmentStatsBinding
import com.karaketir.coachingapp.models.Statistic
import com.karaketir.coachingapp.services.ExcelExportHelper
import com.karaketir.coachingapp.services.StudyQueryHelper
import kotlinx.coroutines.launch
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private var isViewCreated = false
    private lateinit var mBinding: FragmentStatsBinding
    private lateinit var toplamSureTextView: TextView
    private lateinit var toplamSoruTextView: TextView
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        this.mainActivity = activity
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var layoutManager: LinearLayoutManager
    private var secilenZamanAraligi = "Seçiniz"
    private var kurumKodu = 0
    private var secilenGrade = "Bütün Sınıflar"
    private lateinit var recyclerViewStats: RecyclerView
    private lateinit var recyclerViewStatsAdapter: StatisticsRecyclerAdapter
    private var dersSoruHash = hashMapOf<String, Float>()
    private var dersSureHash = hashMapOf<String, Float>()
    private var statsList = ArrayList<Statistic>()
    private var ogrenciSayisi = 0
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0", "13")

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

    private var hasShownInitialDialog = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        auth = Firebase.auth
        db = Firebase.firestore
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it["kurumKodu"].toString().toInt()
        }

        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewCreated = false
    }

    // Add a custom method to check if the binding is available
    private fun isBindingAvailable(): Boolean {
        return isViewCreated && _binding != null
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        if (isBindingAvailable()) {
            mBinding = binding
            toplamSureTextView = mBinding.toplamSure
            toplamSoruTextView = mBinding.toplamSoru
            val fileSaveButton = mBinding.fileSaveExcelButton

            layoutManager = LinearLayoutManager(mainActivity)
            val statsZamanSpinner = mBinding.statsZamanAraligiSpinner
            val statsGradeSpinner = mBinding.statsGradeSpinner

            setupSpinnerAdapters(statsZamanSpinner, statsGradeSpinner)

            if (!hasShownInitialDialog) {
                showInitialDialog(statsZamanSpinner, statsGradeSpinner)
                hasShownInitialDialog = true
            }

            fileSaveButton.setOnClickListener {
                exportStatsExcel()
            }
        }
    }

    private fun setupSpinnerAdapters(statsZamanSpinner: Spinner, statsGradeSpinner: Spinner) {
        val statsAdapter = mainActivity?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, zamanAraliklari)
        }
        val gradeAdapter = mainActivity?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, gradeList)
        }

        statsAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        statsZamanSpinner.adapter = statsAdapter
        statsGradeSpinner.adapter = gradeAdapter

        setupSpinnerListeners(statsZamanSpinner, statsGradeSpinner)
    }

    private fun setupSpinnerListeners(statsZamanSpinner: Spinner, statsGradeSpinner: Spinner) {
        statsZamanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val customDateLayout = mBinding.customDateLayout
                customDateLayout.visibility = View.VISIBLE

                secilenZamanAraligi = zamanAraliklari[position]
                handleTimeSelection(secilenZamanAraligi)
                getData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        statsGradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18s")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                secilenGrade = gradeList[position]
                getData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    @SuppressLint("SetTextI18s", "SimpleDateFormat")
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
            "Son 30 Gün" -> {
                bitisTarihi = cal.time
                cal.add(Calendar.DAY_OF_YEAR, -30)
                baslangicTarihi = cal.time
            }
            "Bu Ay" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                baslangicTarihi = cal.time
                cal.add(Calendar.MONTH, 1)
                bitisTarihi = cal.time
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
            "Özel" -> {
                setupCustomDatePickers()
            }
        }

        if (selectedTime != "Özel") {
            updateDateTextViews()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupCustomDatePickers() {
        val cal = Calendar.getInstance()
        val baslangicTarihiTextView = mBinding.baslangicTarihiTextView
        val bitisTarihiTextView = mBinding.bitisTarihiTextView

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            baslangicTarihi = cal.time
            updateDateTextViews()
            getData()
        }

        val dateSetListener2 = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            bitisTarihi = cal.time
            updateDateTextViews()
            getData()
        }

        baslangicTarihiTextView.setOnClickListener {
            mainActivity?.let { activity ->
                DatePickerDialog(
                    activity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        bitisTarihiTextView.setOnClickListener {
            mainActivity?.let { activity ->
                DatePickerDialog(
                    activity,
                    dateSetListener2,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun updateDateTextViews() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        mBinding.baslangicTarihiTextView.text = "Başlangıç Tarihi: ${dateFormat.format(baslangicTarihi)}"
        mBinding.bitisTarihiTextView.text = "Bitiş Tarihi: ${dateFormat.format(bitisTarihi)}"
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun showSum() {
        var toplamSure = 0f
        var toplamSoru = 0f
        if (statsList.isNotEmpty()) {
            for (i in statsList) {
                toplamSure += i.toplamCalisma.replace(",", ".").toFloat()
                toplamSoru += i.cozulenSoru.replace(",", ".").toFloat()
            }
        }

        val toplamSureSaat = toplamSure / 60
        toplamSureTextView.text = "${toplamSure.format(2)}dk (${toplamSureSaat.format(2)} Saat)"
        toplamSoruTextView.text = "${toplamSoru.format(2)} Soru"
    }


    private fun Float.format(digits: Int): String {
        return String.format(java.util.Locale.US, "%.${digits}f", this)
    }


    @SuppressLint("SetTextI18n")
    private fun exportStatsExcel() {
        val ctx = mainActivity ?: return
        if (secilenZamanAraligi == "Seçiniz") {
            Toast.makeText(ctx, "Lütfen bir zaman aralığı seçiniz", Toast.LENGTH_SHORT).show()
            return
        }
        if (statsList.isEmpty()) {
            Toast.makeText(ctx, "Dışa aktarılacak veri yok", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(ctx, getString(R.string.excel_lutfen_bekleyin), Toast.LENGTH_SHORT).show()

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

        val sortedStats = statsList.sortedBy { it.dersAdi }
        sortedStats.forEach { stat ->
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
            ctx,
            workbook,
            fileName,
            ExcelExportHelper.FOLDER_STATS
        )
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        if (secilenZamanAraligi == "Seçiniz" || kurumKodu == 0) return

        statsList.clear()
        dersSureHash.clear()
        dersSoruHash.clear()

        recyclerViewStats = mBinding.statsRecyclerView
        recyclerViewStats.layoutManager = layoutManager
        recyclerViewStatsAdapter = StatisticsRecyclerAdapter(statsList)
        recyclerViewStats.adapter = recyclerViewStatsAdapter

        val teacherId = auth.uid ?: return
        val gradeFilter = if (secilenGrade == "Bütün Sınıflar") null else secilenGrade.toIntOrNull()

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
                    if (!isBindingAvailable()) return@launch
                    updateStatsList()
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

                if (!isBindingAvailable()) return@launch
                updateStatsList()
            } catch (e: Exception) {
                println(e.localizedMessage)
                if (!isBindingAvailable()) return@launch
                Toast.makeText(mainActivity, "İstatistikler yüklenemedi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateStatsList() {
        if (ogrenciSayisi > 0) {
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
    }

    private fun showInitialDialog(statsZamanSpinner: Spinner, statsGradeSpinner: Spinner) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_initial_stats, null)
        val dialogGradeSpinner = dialogView.findViewById<Spinner>(R.id.dialogGradeSpinner)
        val dialogTimeSpinner = dialogView.findViewById<Spinner>(R.id.dialogTimeSpinner)

        // Set up dialog spinners with same adapters
        dialogGradeSpinner.adapter = statsGradeSpinner.adapter
        dialogTimeSpinner.adapter = statsZamanSpinner.adapter

        AlertDialog.Builder(requireContext())
            .setTitle("İstatistik Filtreleri")
            .setView(dialogView)
            .setPositiveButton("Tamam") { dialog, _ ->
                // Set the main spinners to match dialog selections
                statsGradeSpinner.setSelection(dialogGradeSpinner.selectedItemPosition)
                statsZamanSpinner.setSelection(dialogTimeSpinner.selectedItemPosition)
                dialog.dismiss()
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}