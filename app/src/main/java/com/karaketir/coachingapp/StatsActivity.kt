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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.StatisticsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityStatsBinding
import com.karaketir.coachingapp.models.Statistic
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil.createCell
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {
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
    private lateinit var binding: ActivityStatsBinding
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var layoutManager: GridLayoutManager
    private var secilenZamanAraligi = ""
    private val handler = Handler(Looper.getMainLooper())
    private var kurumKodu = 0
    private val workbook = XSSFWorkbook()
    private var secilenGrade = ""
    private lateinit var recyclerViewStats: RecyclerView
    private lateinit var recyclerViewStatsAdapter: StatisticsRecyclerAdapter
    private var dersSoruHash = hashMapOf<String, Float>()
    private var dersSureHash = hashMapOf<String, Float>()
    private var statsList = ArrayList<Statistic>()
    private var ogrenciSayisi = 0
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")

    private val zamanAraliklari =
        arrayOf("Bugün", "Dün", "Bu Hafta", "Geçen Hafta", "Bu Ay", "Geçen Ay", "Tüm Zamanlar")

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


            val cursor: Cursor? =
                contentResolver.query(contentUri, null, selection, selectionArgs, null)

            var uri: Uri? = null

            if (cursor != null) {
                if (cursor.count == 0) {
                    Toast.makeText(
                        this,
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
                        uri = contentResolver.insert(
                            MediaStore.Files.getContentUri("external"), values
                        ) //important!
                        val outputStream = contentResolver.openOutputStream(uri!!)
                        workbook.write(outputStream)
                        outputStream!!.flush()
                        //outputStream!!.write("This is menu category data.".toByteArray())
                        outputStream.close()
                        Toast.makeText(
                            this, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show()
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
                            this,
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
                            uri = contentResolver.insert(
                                MediaStore.Files.getContentUri("external"), values
                            ) //important!
                            val outputStream = contentResolver.openOutputStream(uri!!)
                            workbook.write(outputStream)
                            outputStream!!.flush()
                            //outputStream!!.write("This is menu category data.".toByteArray())
                            outputStream.close()
                            Toast.makeText(
                                this, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show()
                        }


                    } else {
                        try {
                            val outputStream: OutputStream? = contentResolver.openOutputStream(
                                uri, "rwt"
                            ) //overwrite mode, see below
                            workbook.write(outputStream)
                            outputStream!!.flush()
                            //outputStream!!.write("This is menu category data.".toByteArray())
                            outputStream.close()
                            Toast.makeText(
                                this, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show()
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
                    this, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
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


        val toplamSureSaat = toplamSure / 60
        createCell(row, 0, "Toplam")
        createCell(
            row, 1, toplamSure.format(2) + "dk " + "(${
                toplamSureSaat.format(2)
            } Saat)"
        ) //Column 1


        createCell(row, 2, "${toplamSoru.format(2)} Soru")
        var indexNum = 0
        if (ogrenciSayisi != 0) {

            for (i in dersSureHash.keys) {
                val row2 = sheet.createRow(indexNum + 1)

                val dersSure = dersSureHash[i]
                if (dersSure != null) {
                    val sureSaat = (dersSure) / (60 * ogrenciSayisi)

                    createCell(row2, 0, i)
                    createCell(
                        row2,
                        1,
                        (dersSure / (ogrenciSayisi)).format(2) + "dk " + "(${sureSaat.format(2)} Saat)"
                    )
                }
                val dersSoru = dersSoruHash[i]
                if (dersSoru != null) {
                    createCell(
                        row2, 2, (dersSoru / ogrenciSayisi).format(2) + " Soru"
                    )
                }
                indexNum += 1

            }
        }
        Toast.makeText(this, "Excel Dosyası Oluşturuldu", Toast.LENGTH_SHORT).show()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        val sheet: Sheet = workbook.createSheet("Sayfa 1")

        //Create Header Cell Style
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

        //Adding data to the sheet

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
            addData(sheet)

            askForPermissions()
            createExcel()
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

                        when (position) {
                            0 -> {
                                baslangicTarihi = cal.time


                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                bitisTarihi = cal.time
                            }

                            1 -> {
                                bitisTarihi = cal.time

                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                baslangicTarihi = cal.time
                            }

                            2 -> {
                                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                                baslangicTarihi = cal.time


                                cal.add(Calendar.WEEK_OF_YEAR, 1)
                                bitisTarihi = cal.time

                            }

                            3 -> {
                                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                                bitisTarihi = cal.time


                                cal.add(Calendar.DAY_OF_YEAR, -7)
                                baslangicTarihi = cal.time


                            }

                            4 -> {

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

                            5 -> {
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

                            6 -> {
                                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                baslangicTarihi = cal.time


                                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                bitisTarihi = cal.time

                            }
                        }


                        val dersListesi = kotlin.collections.ArrayList<String>()



                        db.collection("Lessons").orderBy("dersAdi", Query.Direction.ASCENDING)
                            .addSnapshotListener { dersler, _ ->
                                if (dersler != null) {
                                    for (i in dersler) {
                                        dersListesi.add(i.id)
                                    }
                                }
                                for (dersIndex in dersListesi) {

                                    if (secilenGrade == "Bütün Sınıflar") {
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student")
                                            .whereEqualTo("teacher", auth.uid.toString())
                                            .addSnapshotListener { ogrencliler, _ ->
                                                statsList.clear()

                                                if (ogrencliler != null) {
                                                    ogrenciSayisi = ogrencliler.size()

                                                    for (ogrenci in ogrencliler) {
                                                        var toplamCalisma = 0
                                                        var cozulenSoru = 0
                                                        db.collection("School")
                                                            .document(kurumKodu.toString())
                                                            .collection("Student")
                                                            .document(ogrenci.id)
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

                                                                        val currentValue =
                                                                            dersSoruHash[dersIndex]

                                                                        if (currentValue != null) {
                                                                            dersSoruHash[dersIndex] =
                                                                                currentValue + cozulenSoru
                                                                        }


                                                                    } else {
                                                                        dersSoruHash[dersIndex] =
                                                                            cozulenSoru.toFloat()
                                                                    }

                                                                    if (dersIndex in dersSureHash.keys) {

                                                                        val currentValue =
                                                                            dersSureHash[dersIndex]

                                                                        if (currentValue != null) {
                                                                            dersSureHash[dersIndex] =
                                                                                currentValue + toplamCalisma
                                                                        }


                                                                    } else {
                                                                        dersSureHash[dersIndex] =
                                                                            toplamCalisma.toFloat()
                                                                    }




                                                                    statsList.clear()
                                                                    for (i in dersSureHash.keys) {
                                                                        val currentStatistic =
                                                                            Statistic(
                                                                                i,
                                                                                (dersSureHash[i]?.div(
                                                                                    ogrenciSayisi
                                                                                )).toString(),
                                                                                (dersSoruHash[i]?.div(
                                                                                    ogrenciSayisi
                                                                                )).toString()
                                                                            )

                                                                        statsList.add(
                                                                            currentStatistic
                                                                        )
                                                                        recyclerViewStatsAdapter.notifyDataSetChanged()
                                                                    }
                                                                }


                                                            }


                                                    }

                                                }


                                            }
                                    } else {
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student")
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
                                                        db.collection("School")
                                                            .document(kurumKodu.toString())
                                                            .collection("Student")
                                                            .document(ogrenci.id)
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

                                                                        val currentValue =
                                                                            dersSoruHash[dersIndex]

                                                                        if (currentValue != null) {
                                                                            dersSoruHash[dersIndex] =
                                                                                currentValue + cozulenSoru
                                                                        }


                                                                    } else {
                                                                        dersSoruHash[dersIndex] =
                                                                            cozulenSoru.toFloat()
                                                                    }

                                                                    if (dersIndex in dersSureHash.keys) {

                                                                        val currentValue =
                                                                            dersSureHash[dersIndex]

                                                                        if (currentValue != null) {
                                                                            dersSureHash[dersIndex] =
                                                                                currentValue + toplamCalisma
                                                                        }


                                                                    } else {
                                                                        dersSureHash[dersIndex] =
                                                                            toplamCalisma.toFloat()
                                                                    }



                                                                    statsList.clear()
                                                                    for (i in dersSureHash.keys) {
                                                                        val currentStatistic =
                                                                            Statistic(
                                                                                i,
                                                                                (dersSureHash[i]?.div(
                                                                                    ogrenciSayisi
                                                                                )).toString(),
                                                                                (dersSoruHash[i]?.div(
                                                                                    ogrenciSayisi
                                                                                )).toString()
                                                                            )
                                                                        statsList.add(
                                                                            currentStatistic
                                                                        )
                                                                        recyclerViewStatsAdapter.notifyDataSetChanged()
                                                                    }


                                                                }


                                                            }


                                                    }

                                                }


                                            }
                                    }
                                }

                            }


                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        handler.post(object : Runnable {
            override fun run() {
                // Keep the postDelayed before the updateTime(), so when the event ends, the handler will stop too.
                handler.postDelayed(this, 2000)
                showSum()
            }
        })


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
                createExcel()
            } else {
                // Permission not granted, handle accordingly
            }
        }

    private fun askForPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            createExcel()
        }
    }

}