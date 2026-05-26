package com.karaketir.coachingapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.adapter.StatisticsRecyclerAdapter
import com.karaketir.coachingapp.databinding.FragmentStatsBinding
import com.karaketir.coachingapp.models.Statistic
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

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
    private val workbook = XSSFWorkbook()
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
            val sheet: Sheet = workbook.createSheet("Sayfa 1")

            //Create Header Cell Style
            val cellStyle = getHeaderStyle(workbook)

            //Creating sheet header row
            createSheetHeader(cellStyle, sheet)

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
                addData(sheet)
                askForPermissions()
                createExcel()
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


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createExcel()
            } else {
                // Permission not granted, handle accordingly
            }
        }

    private fun askForPermissions() {
        if (mainActivity?.let {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            createExcel()
        }
    }

    @SuppressLint("Recycle", "Range", "SimpleDateFormat")
    private fun createExcel() {

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val current = formatter.format(time)

        val contentUri = MediaStore.Files.getContentUri("external")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"

            val selectionArgs =
                arrayOf(Environment.DIRECTORY_DOCUMENTS + "/Koçluk İstatistikleri/") //must include "/" in front and end


            val cursor: Cursor? = mainActivity?.contentResolver?.query(
                contentUri, null, selection, selectionArgs, null
            )

            var uri: Uri? = null

            if (cursor != null) {
                if (cursor.count == 0) {

                    try {
                        val values = ContentValues()
                        values.put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "$secilenGrade - $secilenZamanAraligi - $current"
                        ) //file name
                        values.put(
                            MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel"
                        ) //file extension, will automatically add to file
                        values.put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOCUMENTS + "/Koçluk İstatistikleri/"
                        ) //end "/" is not mandatory
                        uri = mainActivity?.contentResolver?.insert(
                            MediaStore.Files.getContentUri("external"), values
                        ) //important!
                        val outputStream = mainActivity?.contentResolver?.openOutputStream(uri!!)
                        workbook.write(outputStream)
                        outputStream!!.flush()
                        //outputStream!!.write("This is menu category data.".toByteArray())
                        outputStream.close()
                        Toast.makeText(
                            mainActivity, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(mainActivity, "İşlem Başarısız!", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    while (cursor.moveToNext()) {
                        val fileName: String =
                            cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                        if (fileName == "$secilenGrade - $secilenZamanAraligi - $current.xls") {                          //must include extension
                            val id: Long =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                            uri = ContentUris.withAppendedId(contentUri, id)
                            break
                        }
                    }
                    if (uri == null) {

                        try {
                            val values = ContentValues()
                            values.put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                "$secilenGrade - $secilenZamanAraligi - $current"
                            ) //file name
                            values.put(
                                MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel"
                            ) //file extension, will automatically add to file
                            values.put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOCUMENTS + "/Koçluk İstatistikleri/"
                            ) //end "/" is not mandatory
                            uri = mainActivity?.contentResolver?.insert(
                                MediaStore.Files.getContentUri("external"), values
                            ) //important!
                            val outputStream =
                                mainActivity?.contentResolver?.openOutputStream(uri!!)
                            workbook.write(outputStream)
                            outputStream!!.flush()
                            //outputStream!!.write("This is menu category data.".toByteArray())
                            outputStream.close()
                            Toast.makeText(
                                mainActivity, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            Toast.makeText(mainActivity, "İşlem Başarısız!", Toast.LENGTH_SHORT)
                                .show()
                        }


                    } else {
                        try {
                            val outputStream: OutputStream? =
                                mainActivity?.contentResolver?.openOutputStream(
                                    uri, "rwt"
                                ) //overwrite mode, see below
                            workbook.write(outputStream)
                            outputStream!!.flush()
                            //outputStream!!.write("This is menu category data.".toByteArray())
                            outputStream.close()
                            Toast.makeText(
                                mainActivity, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            Toast.makeText(mainActivity, "İşlem Başarısız!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }


        } else {
            val filePath = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/$secilenGrade - $secilenZamanAraligi - $current.xlsx"
            )
            try {
                if (!filePath.exists()) {
                    filePath.createNewFile()
                }
                val fileOutputStream = FileOutputStream(filePath)
                workbook.write(fileOutputStream)
                Toast.makeText(
                    mainActivity, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                ).show()
                fileOutputStream.flush()

                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println(e)
            }
        }
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        //setHeaderStyle is a custom function written below to add header style

        //Create sheet first row
        val row = sheet.createRow(0)

        //Header list
        val headerList = listOf("column_1", "column_2", "column_3")

        //Loop to populate each column of header row
        for ((index, value) in headerList.withIndex()) {

            val columnWidth = (15 * 500)

            sheet.setColumnWidth(index, columnWidth)

            val cell = row.createCell(index)

            cell?.setCellValue(value)

            cell.cellStyle = cellStyle
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun addData(sheet: Sheet) {
        try {
            // Add information row
            val infoRow = sheet.createRow(0)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            CellUtil.createCell(
                infoRow, 
                0, 
                "${secilenGrade.ifEmpty { "Tüm Sınıflar" }} - ${secilenZamanAraligi} " +
                "(${dateFormat.format(baslangicTarihi)} - ${dateFormat.format(bitisTarihi)})"
            )
            
            // Merge cells for the info row
            sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 2))

            // Create header row (now in row 2)
            val headerRow = sheet.createRow(2)
            
            // Set fixed column widths
            sheet.setColumnWidth(0, 30 * 256) // Ders column - wider for lesson names
            sheet.setColumnWidth(1, 25 * 256) // Ortalama Süre (dk) column
            sheet.setColumnWidth(2, 25 * 256) // Ortalama Süre (saat) column
            sheet.setColumnWidth(3, 25 * 256) // Ortalama Soru column

            // Create header cells
            CellUtil.createCell(headerRow, 0, "Ders")
            CellUtil.createCell(headerRow, 1, "Ortalama Süre (dk)")
            CellUtil.createCell(headerRow, 2, "Ortalama Süre (saat)")
            CellUtil.createCell(headerRow, 3, "Ortalama Soru")

            // Add statistics rows starting from row 3
            statsList.forEachIndexed { index, stat ->
                val row = sheet.createRow(index + 3)
                CellUtil.createCell(row, 0, stat.dersAdi)
                CellUtil.createCell(row, 1, stat.toplamCalisma)
                CellUtil.createCell(row, 2, String.format("%.2f", stat.toplamCalisma.toFloat() / 60))
                CellUtil.createCell(row, 3, stat.cozulenSoru)
            }

            // Add summary row with a blank row above it
            val summaryRow = sheet.createRow(statsList.size + 5)
            CellUtil.createCell(summaryRow, 0, "Toplam")
            val totalTime = calculateTotalTime()
            CellUtil.createCell(summaryRow, 1, totalTime)
            CellUtil.createCell(summaryRow, 2, String.format("%.2f", totalTime.toFloat() / 60))
            CellUtil.createCell(summaryRow, 3, calculateTotalQuestions())

            // Add student count information
            val studentCountRow = sheet.createRow(statsList.size + 6)
            CellUtil.createCell(studentCountRow, 0, "Öğrenci Sayısı: $ogrenciSayisi")
            sheet.addMergedRegion(CellRangeAddress(
                statsList.size + 6, 
                statsList.size + 6, 
                0, 
                3
            ))

        } catch (e: Exception) {
            mainActivity?.let {
                Toast.makeText(it, "Excel oluşturma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateTotalTime(): String {
        val total = statsList.sumOf { it.toplamCalisma.toDouble() }.toFloat()
        return total.format(2)
    }

    private fun calculateTotalQuestions(): String {
        val total = statsList.sumOf { it.cozulenSoru.toDouble() }.toFloat()
        return total.format(2)
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {

        //Cell style for header row
        val cellStyle: CellStyle = workbook.createCellStyle()

        //Apply cell color
        val colorMap: IndexedColorMap = (workbook as XSSFWorkbook).stylesSource.indexedColors
        var color = XSSFColor(IndexedColors.RED, colorMap).indexed
        cellStyle.fillForegroundColor = color
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        //Apply font style on cell text
        val whiteFont = workbook.createFont()
        color = XSSFColor(IndexedColors.WHITE, colorMap).indexed
        whiteFont.color = color
        whiteFont.bold = true
        cellStyle.setFont(whiteFont)


        return cellStyle
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        // Clear lists at the start
        statsList.clear()
        dersSureHash.clear()
        dersSoruHash.clear()
        
        recyclerViewStats = mBinding.statsRecyclerView
        recyclerViewStats.layoutManager = layoutManager
        recyclerViewStatsAdapter = StatisticsRecyclerAdapter(statsList)
        recyclerViewStats.adapter = recyclerViewStatsAdapter

        // Move dersListesi outside the listener to prevent multiple initializations
        val dersListesi = ArrayList<String>()
        
        db.collection("Lessons")
            .orderBy("dersAdi", Query.Direction.ASCENDING)
            .get() // Use get() instead of addSnapshotListener for one-time data fetch
            .addOnSuccessListener { dersler ->
                dersler?.forEach { ders ->
                    dersListesi.add(ders.id)
                }
                
                // Process each ders after getting the full list
                processStudentData(dersListesi)
            }
    }

    private fun processStudentData(dersListesi: List<String>) {
        val studentQuery = if (secilenGrade == "Bütün Sınıflar") {
            db.collection("School")
                .document(kurumKodu.toString())
                .collection("Student")
                .whereEqualTo("teacher", auth.uid.toString())
        } else {
            db.collection("School")
                .document(kurumKodu.toString())
                .collection("Student")
                .whereEqualTo("teacher", auth.uid.toString())
                .whereEqualTo("grade", secilenGrade.toInt())
        }

        studentQuery.get().addOnSuccessListener { students ->
            ogrenciSayisi = students.size()
            
            // Process each student's data for each ders
            students.forEach { student ->
                dersListesi.forEach { ders ->
                    processStudentDersData(student.id, ders)
                }
            }
        }
    }

    private fun processStudentDersData(studentId: String, dersAdi: String) {
        db.collection("School")
            .document(kurumKodu.toString())
            .collection("Student")
            .document(studentId)
            .collection("Studies")
            .whereEqualTo("dersAdi", dersAdi)
            .whereGreaterThan("timestamp", baslangicTarihi)
            .whereLessThan("timestamp", bitisTarihi)
            .get()
            .addOnSuccessListener { studies ->
                var toplamCalisma = 0
                var cozulenSoru = 0
                
                studies.forEach { study ->
                    toplamCalisma += study.get("toplamCalisma").toString().toInt()
                    cozulenSoru += study.get("çözülenSoru").toString().toInt()
                }

                // Update the hash maps atomically
                synchronized(dersSureHash) {
                    dersSureHash[dersAdi] = (dersSureHash[dersAdi] ?: 0f) + toplamCalisma.toFloat()
                }
                synchronized(dersSoruHash) {
                    dersSoruHash[dersAdi] = (dersSoruHash[dersAdi] ?: 0f) + cozulenSoru.toFloat()
                }

                // Update the UI
                updateStatsList()
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