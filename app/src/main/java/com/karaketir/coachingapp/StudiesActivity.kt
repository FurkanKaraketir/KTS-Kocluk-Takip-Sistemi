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
import kotlin.collections.ArrayList


class StudiesActivity : AppCompatActivity() {

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
    private lateinit var recyclerViewStudiesAdapter: ClassesAdapter
    private val workbook = XSSFWorkbook()

    private lateinit var recyclerViewStudies: RecyclerView
    private var studyList = ArrayList<com.karaketir.coachingapp.models.Class>()
    private var classList = ArrayList<String>()
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
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
        }
        layoutManager = GridLayoutManager(applicationContext, 2)
        val intent = intent
        studentID = intent.getStringExtra("studentID").toString()
        secilenZamanAraligi = intent.getStringExtra("secilenZaman").toString()

        val sheet: Sheet = workbook.createSheet("Sayfa 1")

        //Create Header Cell Style
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

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
        val zamanAraligiTextView = binding.zamanAraligiTextView
        val excelCreateButton = binding.excelStudentButton

        setupStudyRecyclerView(studyList)
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !
        db.collection("User").document(studentID).get().addOnSuccessListener {
            name = it.get("nameAndSurname").toString()
            nameTextView.text = name

        }
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        zamanAraligiTextView.text = secilenZamanAraligi

        excelCreateButton.setOnClickListener {
            addData(sheet)

            askForPermissions()
        }

        when (secilenZamanAraligi) {

            "Bugün" -> {
                baslangicTarihi = cal.time
                binding.starScroll.visibility = View.VISIBLE

                cal.add(Calendar.DAY_OF_YEAR, 1)
                bitisTarihi = cal.time
            }
            "Dün" -> {
                binding.starScroll.visibility = View.VISIBLE
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
                                kurumKodu = it.get("kurumKodu")?.toString()?.toInt()!!
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

    @SuppressLint("Range", "Recycle", "SimpleDateFormat")
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
                            "$name - $secilenZamanAraligi - $current"
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
                        if (fileName == "$name - $secilenZamanAraligi - $current.xls") {                          //must include extension
                            val id: Long =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                            uri = ContentUris.withAppendedId(contentUri, id)
                            break
                        }
                    }
                    if (uri == null) {
                        Toast.makeText(
                            this,
                            "\"$name - $secilenZamanAraligi - $current.xls\" Bulunamadı",
                            Toast.LENGTH_SHORT
                        ).show()

                        try {
                            val values = ContentValues()
                            values.put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                "$name - $secilenZamanAraligi - $current"
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
                    .toString() + "/$name - $secilenZamanAraligi - $current.xlsx"
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

    private fun addData(sheet: Sheet) {
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

        var indexNum = 0
        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("Studies")
            .whereGreaterThan("timestamp", baslangicTarihi).whereLessThan("timestamp", bitisTarihi)
            .addSnapshotListener { value, _ ->

                if (value != null) {
                    for (i in value) {
                        val row = sheet.createRow(indexNum)

                        CellUtil.createCell(row, 0, i.get("dersAdi").toString())
                        CellUtil.createCell(row, 1, i.get("tür").toString())
                        CellUtil.createCell(row, 2, i.get("konuAdi").toString())
                        CellUtil.createCell(row, 3, (i.get("toplamCalisma").toString() + " dk"))
                        CellUtil.createCell(row, 4, (i.get("çözülenSoru").toString()) + " Soru")
                        indexNum += 1
                    }
                    createExcel()
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

}