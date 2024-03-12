package com.karaketir.coachingapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.adapter.StatisticsRecyclerAdapter
import com.karaketir.coachingapp.databinding.FragmentStatsBinding
import com.karaketir.coachingapp.models.Statistic
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
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

class StatsFragment(private var mainActivity: MainActivity) : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private var isViewCreated = false
    private lateinit var mBinding: FragmentStatsBinding
    private lateinit var toplamSureTextView: TextView
    private lateinit var toplamSoruTextView: TextView
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
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
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")

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

            //Adding data to the sheet

            val fileSaveButton = mBinding.fileSaveExcelButton
            val customDateLayout = mBinding.customDateLayout
            val baslangicTarihiTextView = mBinding.baslangicTarihiTextView
            val bitisTarihiTextView = mBinding.bitisTarihiTextView

            layoutManager = LinearLayoutManager(mainActivity)
            val statsZamanSpinner = mBinding.statsZamanAraligiSpinner

            val statsGradeSpinner = mBinding.statsGradeSpinner

            val statsAdapter = ArrayAdapter(
                mainActivity, android.R.layout.simple_spinner_item, zamanAraliklari
            )

            val gradeAdapter = ArrayAdapter(
                mainActivity, android.R.layout.simple_spinner_item, gradeList
            )

            statsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statsZamanSpinner.adapter = statsAdapter


            fileSaveButton.setOnClickListener {
                addData(sheet)

                askForPermissions()
                createExcel()
            }



            statsZamanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, position: Int, p3: Long
                ) {
                    customDateLayout.visibility = View.VISIBLE

                    secilenZamanAraligi = zamanAraliklari[position]
                    if (secilenZamanAraligi == "Özel") {
                        val cal = Calendar.getInstance()
                        val dateSetListener =
                            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                cal.set(Calendar.YEAR, year)
                                cal.set(Calendar.MONTH, month)
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                baslangicTarihi = cal.time
                                baslangicTarihiTextView.text =
                                    "Başlangıç Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                        baslangicTarihi
                                    )
                                getData()
                            }
                        val dateSetListener2 =
                            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                cal.set(Calendar.YEAR, year)
                                cal.set(Calendar.MONTH, month)
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                bitisTarihi = cal.time
                                bitisTarihiTextView.text =
                                    "Bitiş Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                        bitisTarihi
                                    )
                                getData()
                            }
                        baslangicTarihiTextView.setOnClickListener {
                            DatePickerDialog(
                                mainActivity,
                                dateSetListener,
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        bitisTarihiTextView.setOnClickListener {
                            DatePickerDialog(
                                mainActivity,
                                dateSetListener2,
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }

                    } else {

                        var cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        when (secilenZamanAraligi) {
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

                            "Tüm Zamanlar" -> {
                                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                baslangicTarihi = cal.time


                                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                bitisTarihi = cal.time

                            }
                        }
                        baslangicTarihiTextView.text =
                            "Başlangıç Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                baslangicTarihi
                            )

                        bitisTarihiTextView.text =
                            "Bitiş Tarihi: " + SimpleDateFormat("dd/MM/yyyy").format(
                                bitisTarihi
                            )

                        getData()

                    }


                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }


            gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statsGradeSpinner.adapter = gradeAdapter
            statsGradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, position2: Int, p3: Long
                ) {
                    secilenGrade = gradeList[position2]
                    getData()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }


        }


    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
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
        toplamSureTextView.text = toplamSure.format(2) + "dk " + "(${
            toplamSureSaat.format(2)
        } Saat)"
        toplamSoruTextView.text = "${toplamSoru.format(2)} Soru"
    }


    private fun Float.format(digits: Int) = "%.${digits}f".format(this)


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createExcel()
            } else {
                // Permission not granted, handle accordingly
            }
        }

    private fun askForPermissions() {
        if (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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


            val cursor: Cursor? = mainActivity.contentResolver.query(
                contentUri, null, selection, selectionArgs, null
            )

            var uri: Uri? = null

            if (cursor != null) {
                if (cursor.count == 0) {
                    Toast.makeText(
                        mainActivity,
                        "Dosya Bulunamadı \"" + Environment.DIRECTORY_DOCUMENTS + "/Koçluk İstatistikleri/\"",
                        Toast.LENGTH_LONG
                    ).show()

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
                        uri = mainActivity.contentResolver.insert(
                            MediaStore.Files.getContentUri("external"), values
                        ) //important!
                        val outputStream = mainActivity.contentResolver.openOutputStream(uri!!)
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
                        Toast.makeText(
                            mainActivity,
                            "\"$secilenGrade - $secilenZamanAraligi - $current.xls\" Bulunamadı",
                            Toast.LENGTH_SHORT
                        ).show()

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
                            uri = mainActivity.contentResolver.insert(
                                MediaStore.Files.getContentUri("external"), values
                            ) //important!
                            val outputStream = mainActivity.contentResolver.openOutputStream(uri!!)
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
                                mainActivity.contentResolver.openOutputStream(
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

    @SuppressLint("SetTextI18n")
    private fun addData(sheet: Sheet) {

        //Create row based on row index
        val row = sheet.createRow(0)

        var toplamSure = 0f
        var toplamSoru = 0f
        if (statsList.isNotEmpty()) {
            for (i in statsList) {
                toplamSure += i.toplamCalisma.toFloat()
                toplamSoru += i.cozulenSoru.toFloat()
            }
        }


        CellUtil.createCell(row, 0, "Toplam")
        CellUtil.createCell(
            row, 1, toplamSure.format(2) + "dk "
        ) //Column 1


        CellUtil.createCell(row, 2, toplamSoru.format(2) + " Soru")
        var indexNum = 0
        if (ogrenciSayisi != 0) {

            for (i in dersSureHash.keys) {
                val row2 = sheet.createRow(indexNum + 1)

                val dersSure = dersSureHash[i]
                if (dersSure != null) {

                    CellUtil.createCell(row2, 0, i)
                    CellUtil.createCell(
                        row2, 1, (dersSure / (ogrenciSayisi)).format(2)
                    )
                }
                val dersSoru = dersSoruHash[i]
                if (dersSoru != null) {
                    CellUtil.createCell(
                        row2, 2, (dersSoru / ogrenciSayisi).format(2)
                    )
                }
                indexNum += 1

            }
        }
        Toast.makeText(mainActivity, "Excel Dosyası Oluşturuldu", Toast.LENGTH_SHORT).show()
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

        statsList.clear()
        dersSureHash.clear()
        dersSoruHash.clear()

        recyclerViewStats = mBinding.statsRecyclerView
        recyclerViewStats.layoutManager = layoutManager

        recyclerViewStatsAdapter = StatisticsRecyclerAdapter(statsList)

        recyclerViewStats.adapter = recyclerViewStatsAdapter
        recyclerViewStatsAdapter.notifyDataSetChanged()


        val dersListesi = ArrayList<String>()



        db.collection("Lessons").orderBy("dersAdi", Query.Direction.ASCENDING)
            .addSnapshotListener { dersler, _ ->
                if (dersler != null) {
                    for (i in dersler) {
                        dersListesi.add(i.id)
                    }
                }
                for (dersIndex in dersListesi) {

                    if (secilenGrade == "Bütün Sınıflar") {
                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .whereEqualTo("teacher", auth.uid.toString())
                            .addSnapshotListener { ogrencliler, _ ->
                                statsList.clear()

                                if (ogrencliler != null) {
                                    ogrenciSayisi = ogrencliler.size()

                                    for (ogrenci in ogrencliler) {
                                        var toplamCalisma = 0
                                        var cozulenSoru = 0
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student").document(ogrenci.id)
                                            .collection("Studies").whereEqualTo(
                                                "dersAdi", dersIndex
                                            ).whereGreaterThan(
                                                "timestamp", baslangicTarihi
                                            ).whereLessThan(
                                                "timestamp", bitisTarihi
                                            ).addSnapshotListener { studies, _ ->


                                                if (studies != null && ogrencliler.size() != 0) {

                                                    for (study in studies) {
                                                        toplamCalisma += study.get(
                                                            "toplamCalisma"
                                                        ).toString().toInt()
                                                        cozulenSoru += study.get(
                                                            "çözülenSoru"
                                                        ).toString().toInt()


                                                    }
                                                    if (dersIndex in dersSoruHash.keys) {

                                                        val currentValue = dersSoruHash[dersIndex]

                                                        if (currentValue != null) {
                                                            dersSoruHash[dersIndex] =
                                                                currentValue + cozulenSoru.toFloat()
                                                        }


                                                    } else {
                                                        dersSoruHash[dersIndex] =
                                                            cozulenSoru.toFloat()
                                                    }

                                                    if (dersIndex in dersSureHash.keys) {

                                                        val currentValue = dersSureHash[dersIndex]

                                                        if (currentValue != null) {
                                                            dersSureHash[dersIndex] =
                                                                currentValue + toplamCalisma.toFloat()
                                                        }


                                                    } else {
                                                        dersSureHash[dersIndex] =
                                                            toplamCalisma.toFloat()
                                                    }




                                                    statsList.clear()
                                                    for (i in dersSureHash.keys) {
                                                        val currentStatistic = Statistic(
                                                            i, (dersSureHash[i]?.div(
                                                                ogrenciSayisi
                                                            )).toString(), (dersSoruHash[i]?.div(
                                                                ogrenciSayisi
                                                            )).toString()
                                                        )

                                                        statsList.add(
                                                            currentStatistic
                                                        )
                                                        statsList.sortBy { it.dersAdi }
                                                        recyclerViewStatsAdapter.notifyDataSetChanged()
                                                    }

                                                }

                                                showSum()

                                            }


                                    }

                                }


                            }
                    } else {
                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .whereEqualTo("teacher", auth.uid.toString())
                            .whereEqualTo("grade", secilenGrade.toInt())
                            .addSnapshotListener { ogrencliler, error ->
                                statsList.clear()
                                if (error != null) {
                                    println(error.localizedMessage)
                                }

                                if (ogrencliler != null) {
                                    ogrenciSayisi = ogrencliler.size()

                                    for (ogrenci in ogrencliler) {
                                        var toplamCalisma = 0
                                        var cozulenSoru = 0
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student").document(ogrenci.id)
                                            .collection("Studies").whereEqualTo(
                                                "dersAdi", dersIndex
                                            ).whereGreaterThan(
                                                "timestamp", baslangicTarihi
                                            ).whereLessThan(
                                                "timestamp", bitisTarihi
                                            ).addSnapshotListener { studies, _ ->


                                                if (studies != null && ogrencliler.size() != 0) {

                                                    for (study in studies) {
                                                        toplamCalisma += study.get(
                                                            "toplamCalisma"
                                                        ).toString().toInt()
                                                        cozulenSoru += study.get(
                                                            "çözülenSoru"
                                                        ).toString().toInt()


                                                    }
                                                    if (dersIndex in dersSoruHash.keys) {

                                                        val currentValue = dersSoruHash[dersIndex]

                                                        if (currentValue != null) {
                                                            dersSoruHash[dersIndex] =
                                                                currentValue + cozulenSoru.toFloat()
                                                        }


                                                    } else {
                                                        dersSoruHash[dersIndex] =
                                                            cozulenSoru.toFloat()
                                                    }

                                                    if (dersIndex in dersSureHash.keys) {

                                                        val currentValue = dersSureHash[dersIndex]

                                                        if (currentValue != null) {
                                                            dersSureHash[dersIndex] =
                                                                currentValue + toplamCalisma.toFloat()
                                                        }


                                                    } else {
                                                        dersSureHash[dersIndex] =
                                                            toplamCalisma.toFloat()
                                                    }



                                                    statsList.clear()
                                                    for (i in dersSureHash.keys) {
                                                        val currentStatistic = Statistic(
                                                            i, (dersSureHash[i]?.div(
                                                                ogrenciSayisi
                                                            )).toString(), (dersSoruHash[i]?.div(
                                                                ogrenciSayisi
                                                            )).toString()
                                                        )
                                                        statsList.add(
                                                            currentStatistic
                                                        )
                                                        statsList.sortBy { it.dersAdi }

                                                        recyclerViewStatsAdapter.notifyDataSetChanged()
                                                    }


                                                }
                                                showSum()


                                            }


                                    }

                                }


                            }
                    }
                }

            }


    }
}