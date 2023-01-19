@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp


import android.Manifest
//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.adapter.StudiesRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityMainBinding
import com.karaketir.coachingapp.models.Student
import com.karaketir.coachingapp.models.Study
import com.karaketir.coachingapp.services.glide
import com.karaketir.coachingapp.services.placeHolderYap
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

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
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerViewPreviousStudies: RecyclerView
    private lateinit var recyclerViewMyStudents: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mp: MediaPlayer
    private val workbook = XSSFWorkbook()
    private var studyList = ArrayList<Study>()
    private var studentCount = 0
    private var counter = 0
    private var currentKurumKodu = 0
    private var secilenGrade = "Bütün Sınıflar"
    private var secilenZaman = "Bugün"
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")
    private val zamanAraliklari =
        arrayOf("Bugün", "Dün", "Bu Hafta", "Geçen Hafta", "Bu Ay", "Geçen Ay", "Tüm Zamanlar")
    private lateinit var filteredList: ArrayList<Student>
    private lateinit var filteredStudyList: ArrayList<Study>

    private var studentList = ArrayList<Student>()

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic(auth.uid.toString())
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
                            "$secilenGrade - $secilenZaman - $current"
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
                        if (fileName == "$secilenGrade - $secilenZaman - $current.xls") {                          //must include extension
                            val id: Long =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                            uri = ContentUris.withAppendedId(contentUri, id)
                            break
                        }
                    }
                    if (uri == null) {
                        Toast.makeText(
                            this,
                            "\"$secilenGrade - $secilenZaman - $current.xls\" Bulunamadı",
                            Toast.LENGTH_SHORT
                        ).show()

                        try {
                            val values = ContentValues()
                            values.put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                "$secilenGrade - $secilenZaman - $current"
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
                    .toString() + "/$secilenGrade - $secilenZaman - $current.xlsx"
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
        val headerList = listOf(
            "column_1",
            "column_2",
            "column_3",
            "column_4",
            "column_5",
            "column_6",
            "column_7",
            "column_8",
            "column_9",
            "column_10",
            "column_11",
            "column_12",
            "column_13",
            "column_14",
            "column_15",
            "column_16",
            "column_17",
            "column_18",
            "column_19",
            "column_20",
            "column_21",
            "column_22",
            "column_23",
            "column_24",
            "column_25",
            "column_26",
            "column_27",
            "column_28"
        )

        //Loop to populate each column of header row
        for ((index, value) in headerList.withIndex()) {

            val columnWidth = (15 * 500)

            sheet.setColumnWidth(index, columnWidth)

            val cell = row.createCell(index)

            cell?.setCellValue(value)

            cell.cellStyle = cellStyle
        }
    }

    private fun addData(sheet: Sheet) {

        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

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
        val rowFake = sheet.createRow(0)

        CellUtil.createCell(rowFake, 0, "Ad Soyad")
        CellUtil.createCell(rowFake, 1, "Toplam Süre")
        CellUtil.createCell(rowFake, 2, "Toplam Soru")

        CellUtil.createCell(rowFake, 3, "Matematik Süre")
        CellUtil.createCell(rowFake, 4, "Matematik Soru")

        CellUtil.createCell(rowFake, 5, "Problem Süre")
        CellUtil.createCell(rowFake, 6, "Problem Soru")

        CellUtil.createCell(rowFake, 7, "Geometri Süre")
        CellUtil.createCell(rowFake, 8, "Geometri Soru")

        CellUtil.createCell(rowFake, 9, "Paragraf Süre")
        CellUtil.createCell(rowFake, 10, "Paragraf Soru")

        CellUtil.createCell(rowFake, 11, "Türkçe Süre")
        CellUtil.createCell(rowFake, 12, "Türkçe Soru")

        CellUtil.createCell(rowFake, 13, "Fizik Süre")
        CellUtil.createCell(rowFake, 14, "Fizik Soru")

        CellUtil.createCell(rowFake, 15, "Kimya Süre")
        CellUtil.createCell(rowFake, 16, "Kimya Soru")

        CellUtil.createCell(rowFake, 17, "Biyoloji Süre")
        CellUtil.createCell(rowFake, 18, "Biyoloji Soru")

        CellUtil.createCell(rowFake, 19, "Tarih Süre")
        CellUtil.createCell(rowFake, 20, "Tarih Soru")

        CellUtil.createCell(rowFake, 21, "Coğrafya Süre")
        CellUtil.createCell(rowFake, 22, "Coğrafya Soru")

        CellUtil.createCell(rowFake, 23, "Felsefe Süre")
        CellUtil.createCell(rowFake, 24, "Felsefe Soru")

        CellUtil.createCell(rowFake, 25, "Din Süre")
        CellUtil.createCell(rowFake, 26, "Din Soru")

        CellUtil.createCell(rowFake, 27, "Diğer Süre")
        CellUtil.createCell(rowFake, 28, "Diğer Soru")

        if (secilenGrade != "Bütün Sınıflar") {
            db.collection("School").document(currentKurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString())
                .whereEqualTo("grade", secilenGrade.toInt()).orderBy("nameAndSurname")
                .addSnapshotListener { students, _ ->
                    if (students != null) {
                        var index = 1
                        counter = 0
                        studentCount = students.size()
                        for (i in students) {
                            var biyoSure = 0
                            var biyoSoru = 0

                            var cografyaSure = 0
                            var cografyaSoru = 0

                            var dinSure = 0
                            var dinSoru = 0

                            var digerSure = 0
                            var digerSoru = 0

                            var felsefeSure = 0
                            var felsefeSoru = 0

                            var fizikSure = 0
                            var fizikSoru = 0

                            var geometriSure = 0
                            var geometriSoru = 0

                            var kimyaSure = 0
                            var kimyaSoru = 0

                            var matSure = 0
                            var matSoru = 0

                            var paragrafSure = 0
                            var paragrafSoru = 0

                            var problemSure = 0
                            var problemSoru = 0

                            var tarihSure = 0
                            var tarihSoru = 0

                            var turkceSure = 0
                            var turkceSoru = 0


                            var ogrenciToplamSure = 0
                            var ogrenciToplamSoru = 0
                            val row = sheet.createRow(index)
                            CellUtil.createCell(row, 0, i.get("nameAndSurname").toString())

                            db.collection("School").document(currentKurumKodu.toString())
                                .collection("Student").document(i.id).collection("Studies")
                                .whereGreaterThan("timestamp", baslangicTarihi)
                                .whereLessThan("timestamp", bitisTarihi)
                                .addSnapshotListener { studies, errorStudies ->

                                    if (errorStudies != null) {
                                        println(errorStudies.localizedMessage)
                                    }

                                    if (studies != null) {
                                        for (study in studies) {

                                            when (study.get("dersAdi")) {

                                                "Biyoloji" -> {

                                                    biyoSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    biyoSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Coğrafya" -> {

                                                    cografyaSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    cografyaSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Din" -> {

                                                    dinSure += study.get("toplamCalisma").toString()
                                                        .toInt()
                                                    dinSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Diğer" -> {

                                                    digerSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    digerSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Felsefe" -> {

                                                    felsefeSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    felsefeSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Fizik" -> {

                                                    fizikSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    fizikSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Geometri" -> {

                                                    geometriSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    geometriSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Kimya" -> {

                                                    kimyaSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    kimyaSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Matematik" -> {

                                                    matSure += study.get("toplamCalisma").toString()
                                                        .toInt()
                                                    matSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Paragraf" -> {

                                                    paragrafSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    paragrafSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Problem" -> {

                                                    problemSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    problemSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Tarih" -> {

                                                    tarihSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    tarihSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Türkçe-Edebiyat" -> {

                                                    turkceSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    turkceSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                            }


                                            ogrenciToplamSure += study.get("toplamCalisma")
                                                .toString().toInt()
                                            ogrenciToplamSoru += study.get("çözülenSoru").toString()
                                                .toInt()
                                        }

                                        val toplamSureSaat = ogrenciToplamSure / 60f

                                        CellUtil.createCell(
                                            row, 1, "$ogrenciToplamSure dk " + "(${
                                                toplamSureSaat.format(2)
                                            } Saat)"
                                        )
                                        CellUtil.createCell(row, 2, ogrenciToplamSoru.toString())

                                        CellUtil.createCell(row, 3, matSure.toString() + "dk")
                                        CellUtil.createCell(row, 4, matSoru.toString())

                                        CellUtil.createCell(row, 5, problemSure.toString() + "dk")
                                        CellUtil.createCell(row, 6, problemSoru.toString())

                                        CellUtil.createCell(row, 7, geometriSure.toString() + "dk")
                                        CellUtil.createCell(row, 8, geometriSoru.toString())

                                        CellUtil.createCell(row, 9, paragrafSure.toString() + "dk")
                                        CellUtil.createCell(row, 10, paragrafSoru.toString())

                                        CellUtil.createCell(row, 11, turkceSure.toString() + "dk")
                                        CellUtil.createCell(row, 12, turkceSoru.toString())

                                        CellUtil.createCell(row, 13, fizikSure.toString() + "dk")
                                        CellUtil.createCell(row, 14, fizikSoru.toString())

                                        CellUtil.createCell(row, 15, kimyaSure.toString() + "dk")
                                        CellUtil.createCell(row, 16, kimyaSoru.toString())

                                        CellUtil.createCell(row, 17, biyoSure.toString() + "dk")
                                        CellUtil.createCell(row, 18, biyoSoru.toString())

                                        CellUtil.createCell(row, 19, tarihSure.toString() + "dk")
                                        CellUtil.createCell(row, 20, tarihSoru.toString())

                                        CellUtil.createCell(row, 21, cografyaSure.toString() + "dk")
                                        CellUtil.createCell(row, 22, cografyaSoru.toString())

                                        CellUtil.createCell(row, 23, felsefeSure.toString() + "dk")
                                        CellUtil.createCell(row, 24, felsefeSoru.toString())

                                        CellUtil.createCell(row, 25, dinSure.toString() + "dk")
                                        CellUtil.createCell(row, 26, dinSoru.toString())

                                        CellUtil.createCell(row, 27, digerSure.toString() + "dk")
                                        CellUtil.createCell(row, 28, digerSoru.toString())

                                    }
                                    counter += 1
                                    if (counter == studentCount) {
                                        createExcel()
                                    }


                                }

                            index += 1
                        }
                    }
                }


        } else {
            db.collection("School").document(currentKurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString()).orderBy("nameAndSurname")
                .addSnapshotListener { students, _ ->
                    if (students != null) {
                        var index = 1
                        counter = 0
                        studentCount = students.size()
                        for (i in students) {
                            var biyoSure = 0
                            var biyoSoru = 0

                            var cografyaSure = 0
                            var cografyaSoru = 0

                            var dinSure = 0
                            var dinSoru = 0

                            var digerSure = 0
                            var digerSoru = 0

                            var felsefeSure = 0
                            var felsefeSoru = 0

                            var fizikSure = 0
                            var fizikSoru = 0

                            var geometriSure = 0
                            var geometriSoru = 0

                            var kimyaSure = 0
                            var kimyaSoru = 0

                            var matSure = 0
                            var matSoru = 0

                            var paragrafSure = 0
                            var paragrafSoru = 0

                            var problemSure = 0
                            var problemSoru = 0

                            var tarihSure = 0
                            var tarihSoru = 0

                            var turkceSure = 0
                            var turkceSoru = 0


                            var ogrenciToplamSure = 0
                            var ogrenciToplamSoru = 0
                            val row = sheet.createRow(index)
                            CellUtil.createCell(row, 0, i.get("nameAndSurname").toString())

                            db.collection("School").document(currentKurumKodu.toString())
                                .collection("Student").document(i.id).collection("Studies")
                                .whereGreaterThan("timestamp", baslangicTarihi)
                                .whereLessThan("timestamp", bitisTarihi)
                                .addSnapshotListener { studies, errorStudies ->

                                    if (errorStudies != null) {
                                        println(errorStudies.localizedMessage)
                                    }

                                    if (studies != null) {
                                        for (study in studies) {

                                            when (study.get("dersAdi")) {

                                                "Biyoloji" -> {

                                                    biyoSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    biyoSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Coğrafya" -> {

                                                    cografyaSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    cografyaSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Din" -> {

                                                    dinSure += study.get("toplamCalisma").toString()
                                                        .toInt()
                                                    dinSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Diğer" -> {

                                                    digerSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    digerSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Felsefe" -> {

                                                    felsefeSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    felsefeSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Fizik" -> {

                                                    fizikSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    fizikSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Geometri" -> {

                                                    geometriSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    geometriSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Kimya" -> {

                                                    kimyaSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    kimyaSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Matematik" -> {

                                                    matSure += study.get("toplamCalisma").toString()
                                                        .toInt()
                                                    matSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Paragraf" -> {

                                                    paragrafSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    paragrafSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Problem" -> {

                                                    problemSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    problemSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                                "Tarih" -> {

                                                    tarihSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    tarihSoru += study.get("çözülenSoru").toString()
                                                        .toInt()


                                                }
                                                "Türkçe-Edebiyat" -> {

                                                    turkceSure += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    turkceSoru += study.get("çözülenSoru")
                                                        .toString().toInt()


                                                }
                                            }

                                            ogrenciToplamSure += study.get("toplamCalisma")
                                                .toString().toInt()
                                            ogrenciToplamSoru += study.get("çözülenSoru").toString()
                                                .toInt()
                                        }

                                        val toplamSureSaat = ogrenciToplamSure / 60f

                                        CellUtil.createCell(
                                            row, 1, "$ogrenciToplamSure dk " + "(${
                                                toplamSureSaat.format(2)
                                            } Saat)"
                                        )
                                        CellUtil.createCell(row, 2, ogrenciToplamSoru.toString())

                                        CellUtil.createCell(row, 2, ogrenciToplamSoru.toString())

                                        CellUtil.createCell(row, 3, matSure.toString() + "dk")
                                        CellUtil.createCell(row, 4, matSoru.toString())

                                        CellUtil.createCell(row, 5, problemSure.toString() + "dk")
                                        CellUtil.createCell(row, 6, problemSoru.toString())

                                        CellUtil.createCell(row, 7, geometriSure.toString() + "dk")
                                        CellUtil.createCell(row, 8, geometriSoru.toString())

                                        CellUtil.createCell(row, 9, paragrafSure.toString() + "dk")
                                        CellUtil.createCell(row, 10, paragrafSoru.toString())

                                        CellUtil.createCell(row, 11, turkceSure.toString() + "dk")
                                        CellUtil.createCell(row, 12, turkceSoru.toString())

                                        CellUtil.createCell(row, 13, fizikSure.toString() + "dk")
                                        CellUtil.createCell(row, 14, fizikSoru.toString())

                                        CellUtil.createCell(row, 15, kimyaSure.toString() + "dk")
                                        CellUtil.createCell(row, 16, kimyaSoru.toString())

                                        CellUtil.createCell(row, 17, biyoSure.toString() + "dk")
                                        CellUtil.createCell(row, 18, biyoSoru.toString())

                                        CellUtil.createCell(row, 19, tarihSure.toString() + "dk")
                                        CellUtil.createCell(row, 20, tarihSoru.toString())

                                        CellUtil.createCell(row, 21, cografyaSure.toString() + "dk")
                                        CellUtil.createCell(row, 22, cografyaSoru.toString())

                                        CellUtil.createCell(row, 23, felsefeSure.toString() + "dk")
                                        CellUtil.createCell(row, 24, felsefeSoru.toString())

                                        CellUtil.createCell(row, 25, dinSure.toString() + "dk")
                                        CellUtil.createCell(row, 26, dinSoru.toString())

                                        CellUtil.createCell(row, 27, digerSure.toString() + "dk")
                                        CellUtil.createCell(row, 28, digerSoru.toString())


                                    }
                                    counter += 1
                                    if (counter == studentCount) {
                                        createExcel()
                                    }

                                }

                            index += 1
                        }
                    }
                }


        }


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

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val sheet: Sheet = workbook.createSheet("Sayfa 1")

        //Create Header Cell Style
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

        val nameAndSurnameTextView = binding.nameAndSurnameTextView
        val transitionsContainer = binding.transitionsContainer
        val sayacContainer = binding.sayacContainer

        var sayacVisible = false

        val contentTextView = binding.contentTextView
        val addStudyButton = binding.addStudyButton
        val signOutButton = binding.signOutButton
        val gorevButton = binding.gorevButton
        val previousRatingsButton = binding.previousRatingsButton
        val allStudentsBtn = binding.allStudentsBtn
        val searchEditText = binding.searchStudentMainActivityEditText
        val studySearchEditText = binding.searchStudyEditText
        val studentDenemeButton = binding.studentDenemeButton
        val teacherDenemeButton = binding.teacherDenemeButton
        val istatistikButton = binding.koclukStatsBtn
        val hedeflerStudentButton = binding.hedefStudentButton
        val gradeSpinner = binding.gradeSpinner
        val gradeSpinnerLayout = binding.gradeSpinnerLayout
        val kocOgretmenTextView = binding.kocOgretmenTextView
        val teacherSpinnerLayout = binding.teacherSpinnerLayout
        val teacherSpinner = binding.studyZamanAraligiSpinner
        val searchBarTeacher = binding.searchBarTeacher
        val okulLogo = binding.logoLayout
        val updateLayout = binding.updateLayout
        val updateButton = binding.updateButton
        val imageHalit = binding.imageHalit
        val excelButton = binding.excelButton

        db.collection("UserPhotos").document(auth.uid.toString()).get().addOnSuccessListener {
            imageHalit.glide(it.get("photoURL").toString(), placeHolderYap(applicationContext))

        }

        val dogumGunu = Calendar.getInstance()

        updateButton.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.karaketir.coachingapp")
            )
            startActivity(browserIntent)
        }

        excelButton.setOnClickListener {
            addData(sheet)

            askForPermissions()
        }



        db.collection("VersionCode").document("60qzy2yuxMwCCau44HdF").get().addOnSuccessListener {
            val myVersion = BuildConfig.VERSION_CODE
            val latestVersion = it.get("latestVersion").toString().toInt()
            if (myVersion < latestVersion) {
                addStudyButton.visibility = View.GONE
                signOutButton.visibility = View.GONE
                updateLayout.visibility = View.VISIBLE

            } else {
                addStudyButton.visibility = View.VISIBLE
                signOutButton.visibility = View.VISIBLE
                updateLayout.visibility = View.GONE

            }
        }

        var grade = 0
        okulLogo.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/rteprojeihl/"))
            startActivity(browserIntent)
        }

        nameAndSurnameTextView.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        recyclerViewPreviousStudies = binding.previousStudies
        recyclerViewMyStudents = binding.myStudents

        hedeflerStudentButton.setOnClickListener {
            val intent2 = Intent(this, GoalsActivity::class.java)
            intent2.putExtra("studentID", auth.currentUser?.uid.toString())
            this.startActivity(intent2)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredList = ArrayList()
                if (p0.toString() != "") {
                    for (item in studentList) {
                        if (item.studentName.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(item)
                        }
                    }
                    setupStudentRecyclerView(filteredList)
                } else {
                    setupStudentRecyclerView(studentList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        studySearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredStudyList = ArrayList()
                if (p0.toString() != "") {
                    for (item in studyList) {
                        if (item.studyName.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredStudyList.add(item)
                        }
                    }
                    setupStudyRecyclerView(filteredStudyList)
                } else {
                    setupStudyRecyclerView(studyList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })


        var visible = false
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            nameAndSurnameTextView.text = "Merhaba: " + it.get("nameAndSurname").toString()
            val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

            if (kurumKodu != null) {
                currentKurumKodu = kurumKodu
            }


            TransitionManager.beginDelayedTransition(transitionsContainer)
            visible = !visible
            nameAndSurnameTextView.visibility = if (visible) View.VISIBLE else View.GONE


            if (it.get("personType").toString() == "Student") {

                val cal = Calendar.getInstance()
                grade = it.get("grade").toString().toInt()
                addStudyButton.visibility = View.VISIBLE
                kocOgretmenTextView.visibility = View.VISIBLE
                excelButton.visibility = View.GONE

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(auth.uid.toString()).get().addOnSuccessListener { student ->
                        val kocID = student.get("teacher").toString()
                        if (kocID.isEmpty()) {
                            kocOgretmenTextView.text = "Koç Öğretmenin Bulunmuyor"
                        } else {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Teacher").document(kocID).get()
                                .addOnSuccessListener { teacher ->
                                    kocOgretmenTextView.text =
                                        "Koç Öğretmenin: " + teacher.get("nameAndSurname")
                                            .toString()
                                }

                        }
                    }
                searchBarTeacher.visibility = View.GONE
                gradeSpinnerLayout.visibility = View.GONE
                teacherSpinnerLayout.visibility = View.GONE
                studySearchEditText.visibility = View.VISIBLE
                searchEditText.visibility = View.GONE
                previousRatingsButton.visibility = View.VISIBLE
                studentDenemeButton.visibility = View.VISIBLE
                teacherDenemeButton.visibility = View.GONE
                hedeflerStudentButton.visibility = View.VISIBLE
                recyclerViewPreviousStudies.visibility = View.VISIBLE
                istatistikButton.visibility = View.GONE
                allStudentsBtn.visibility = View.GONE
                gorevButton.visibility = View.VISIBLE

                contentTextView.text = "Bu Haftaki Çalışmalarım"

                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                val baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                val bitisTarihi = cal.time

                val email = it.get("email").toString()
                db.collection("VersionCode").document("1sraIWibAFqiyzJHzxV5").get()
                    .addOnSuccessListener { dogumGunuIt ->
                        if (email == dogumGunuIt.get("email").toString()) {
                            val dogumGunuBas = dogumGunuIt.get("dogumGunuBas") as Timestamp
                            val dogumGunuSon = dogumGunuIt.get("dogumGunuSon") as Timestamp
                            val photoURL = dogumGunuIt.get("photoURL").toString()



                            if (dogumGunuBas.toDate() <= dogumGunu.time && dogumGunu.time <= dogumGunuSon.toDate()) {
                                nameAndSurnameTextView.text =
                                    "İyi ki Doğdun: " + it.get("nameAndSurname").toString()
                                binding.YKSsayac.visibility = View.INVISIBLE
                                okulLogo.visibility = View.INVISIBLE
                                kocOgretmenTextView.visibility = View.INVISIBLE
                                imageHalit.glide(photoURL, placeHolderYap(this))
                                mp = MediaPlayer.create(this, com.karaketir.coachingapp.R.raw.halay)
                                mp.isLooping = true
                                mp.start()
                            }

                        }
                    }

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(auth.uid.toString()).collection("Studies")
                    .whereGreaterThan("timestamp", baslangicTarihi)
                    .whereLessThan("timestamp", bitisTarihi)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { it1, _ ->
                        studyList.clear()
                        val documents = it1!!.documents
                        for (document in documents) {
                            val subjectTheme = document.get("konuAdi").toString()
                            val subjectCount = document.get("toplamCalisma").toString()
                            val studyDersAdi = document.get("dersAdi").toString()
                            val studyTur = document.get("tür").toString()
                            val soruSayisi = document.get("çözülenSoru").toString()
                            val timestamp = document.get("timestamp") as Timestamp
                            val currentStudy = Study(
                                subjectTheme,
                                subjectCount,
                                auth.uid.toString(),
                                studyDersAdi,
                                studyTur,
                                soruSayisi,
                                timestamp,
                                document.id
                            )

                            studyList.add(currentStudy)

                        }

                        setupStudyRecyclerView(studyList)

                        recyclerViewPreviousStudiesAdapter.notifyDataSetChanged()

                    }


            } else if (it.get("personType").toString() == "Teacher") {
                studySearchEditText.visibility = View.GONE
                hedeflerStudentButton.visibility = View.GONE
                excelButton.visibility = View.VISIBLE
                searchEditText.visibility = View.VISIBLE
                teacherSpinnerLayout.visibility = View.VISIBLE
                kocOgretmenTextView.visibility = View.GONE
                recyclerViewMyStudents.visibility = View.VISIBLE
                searchBarTeacher.visibility = View.VISIBLE
                studentDenemeButton.visibility = View.GONE
                previousRatingsButton.visibility = View.GONE
                teacherDenemeButton.visibility = View.VISIBLE
                allStudentsBtn.visibility = View.VISIBLE
                addStudyButton.visibility = View.GONE
                istatistikButton.visibility = View.VISIBLE
                gorevButton.visibility = View.GONE
                gradeSpinnerLayout.visibility = View.VISIBLE
                contentTextView.text = "Öğrencilerim"

                val gradeAdapter = ArrayAdapter(
                    this@MainActivity, R.layout.simple_spinner_item, gradeList
                )
                gradeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                gradeSpinner.adapter = gradeAdapter
                gradeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        secilenGrade = gradeList[p2]
                        if (secilenGrade == "Bütün Sınıflar") {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").whereEqualTo("teacher", auth.uid.toString())
                                .addSnapshotListener { documents, _ ->

                                    studentList.clear()
                                    if (documents != null) {
                                        for (document in documents) {
                                            val studentGrade =
                                                document.get("grade").toString().toInt()
                                            val studentName =
                                                document.get("nameAndSurname").toString()
                                            val teacher = document.get("teacher").toString()
                                            val id = document.get("id").toString()
                                            val currentStudent =
                                                Student(studentName, teacher, id, studentGrade)
                                            studentList.add(currentStudent)

                                        }
                                        binding.studentCountTextView.text =
                                            "Öğrenci Sayısı: " + studentList.size
                                    }
                                    studentList.sortBy { a ->
                                        a.studentName
                                    }
                                    setupStudentRecyclerView(studentList)

                                    recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

                                }
                        } else {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").whereEqualTo("teacher", auth.uid.toString())
                                .whereEqualTo("grade", secilenGrade.toInt())
                                .addSnapshotListener { documents, _ ->

                                    studentList.clear()
                                    if (documents != null) {
                                        for (document in documents) {
                                            val studentGrade =
                                                document.get("grade").toString().toInt()
                                            val studentName =
                                                document.get("nameAndSurname").toString()
                                            val teacher = document.get("teacher").toString()
                                            val id = document.get("id").toString()
                                            val currentStudent =
                                                Student(studentName, teacher, id, studentGrade)
                                            studentList.add(currentStudent)

                                        }
                                        binding.studentCountTextView.text =
                                            "Öğrenci Sayısı: " + studentList.size
                                    }
                                    studentList.sortBy { a ->
                                        a.studentName
                                    }
                                    setupStudentRecyclerView(studentList)

                                    recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

                                }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }


                val tarihAdapter = ArrayAdapter(
                    this@MainActivity, R.layout.simple_spinner_item, zamanAraliklari
                )
                tarihAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                teacherSpinner.adapter = tarihAdapter
                teacherSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        secilenZaman = zamanAraliklari[p2]
                        setupStudentRecyclerView(studentList)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                }


            }



            TransitionManager.beginDelayedTransition(sayacContainer)
            sayacVisible = !sayacVisible
            contentTextView.visibility = if (sayacVisible) View.VISIBLE else View.GONE

        }


        previousRatingsButton.setOnClickListener {
            val intent = Intent(this, PreviousRatingsActivity::class.java)
            this.startActivity(intent)
        }

        studentDenemeButton.setOnClickListener {
            val intent = Intent(this, DenemelerActivity::class.java)
            intent.putExtra("studentID", auth.uid.toString())
            this.startActivity(intent)
        }
        teacherDenemeButton.setOnClickListener {
            val intent = Intent(this, DenemelerTeacherActivity::class.java)
            this.startActivity(intent)
        }


        istatistikButton.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            this.startActivity(intent)
        }

        allStudentsBtn.setOnClickListener {
            val intent = Intent(this, AllStudentsActivity::class.java)
            this.startActivity(intent)
        }

        addStudyButton.setOnClickListener {
            val intent = Intent(this, ClassesActivity::class.java)
            this.startActivity(intent)
        }
        signOutButton.setOnClickListener {

            val signOutAlertDialog = AlertDialog.Builder(this)
            signOutAlertDialog.setTitle("Çıkış Yap")
            signOutAlertDialog.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
            signOutAlertDialog.setPositiveButton("Çıkış") { _, _ ->
                signOut()
                finish()
            }
            signOutAlertDialog.setNegativeButton("İptal") { _, _ ->

            }
            signOutAlertDialog.show()

        }

        gorevButton.setOnClickListener {
            val intent = Intent(this, DutiesActivity::class.java)
            intent.putExtra("studentID", auth.uid)
            this.startActivity(intent)
        }

        handler.post(object : Runnable {
            override fun run() {
                // Keep the postDelayed before the updateTime(), so when the event ends, the handler will stop too.
                handler.postDelayed(this, 1000)
                updateTime(grade)
            }
        })


    }

    private fun setupStudentRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter = StudentsRecyclerAdapter(list, secilenZaman)

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter

    }

    private fun setupStudyRecyclerView(list: ArrayList<Study>) {
        val layoutManager = GridLayoutManager(applicationContext, 2)

        recyclerViewPreviousStudies.layoutManager = layoutManager
        recyclerViewPreviousStudiesAdapter = StudiesRecyclerAdapter(list, "Bu Hafta")
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter

    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }


    @SuppressLint("SetTextI18n")
    fun updateTime(grade: Int) {

        val currentDate = Calendar.getInstance()

        val eventDate = Calendar.getInstance()
        when (grade) {
            12, 0 -> {
                eventDate[Calendar.YEAR] = 2023
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 17
            }
            11 -> {
                eventDate[Calendar.YEAR] = 2024
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 15
            }
            10 -> {
                eventDate[Calendar.YEAR] = 2025
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 14
            }
            9 -> {
                eventDate[Calendar.YEAR] = 2026
                eventDate[Calendar.MONTH] = 5
                eventDate[Calendar.DAY_OF_MONTH] = 13
            }
        }

        eventDate[Calendar.HOUR_OF_DAY] = 10
        eventDate[Calendar.MINUTE] = 15
        eventDate[Calendar.SECOND] = 0

        val diff = eventDate.timeInMillis - currentDate.timeInMillis

        val days = diff / (24 * 60 * 60 * 1000)
        val hours = diff / (1000 * 60 * 60) % 24
        val minutes = diff / (1000 * 60) % 60
        val seconds = (diff / 1000) % 60

        binding.YKSsayac.text = "YKS'ye Son:\n $days Gün $hours Saat $minutes Dk $seconds Sn"


    }

    override fun onResume() {

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        }

        super.onResume()
    }

    private fun askForPermissions() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //İzin Verilmedi, iste
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )


        } else {
            createExcel()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                createExcel()
            }
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

}