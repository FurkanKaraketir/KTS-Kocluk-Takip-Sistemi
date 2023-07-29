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
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportBinding
import com.karaketir.coachingapp.models.Student
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


class NoReportActivity : AppCompatActivity() {


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

    private lateinit var binding: ActivityNoReportBinding
    private var secilenZaman = "Bugün"
    private var secilenGrade = "Bütün Sınıflar"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 763455
    private var studentList = ArrayList<String>()
    private val workbook = XSSFWorkbook()
    private var raporGondermeyenList = ArrayList<Student>()


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
        secilenZaman = intent.getStringExtra("secilenZaman").toString()
        secilenGrade = intent.getStringExtra("grade").toString()

        val titleNoReport = binding.titleNoReport

        if (secilenZaman == "Bugün") {
            secilenZaman = "Dün"
        }
        kurumKodu = intent.getStringExtra("kurumKodu")!!.toInt()
        titleNoReport.text = "Zaman Aralığı: $secilenZaman \nSeçilen Sınıf: $secilenGrade"


        if (secilenGrade == "Bütün Sınıflar") {
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString()).addSnapshotListener { value, _ ->
                    if (value != null) {
                        studentList.clear()
                        for (i in value) {
                            studentList.add(i.get("id").toString())
                        }
                    }
                }
        } else {
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString())
                .whereEqualTo("grade", secilenGrade.toInt()).addSnapshotListener { value, _ ->
                    if (value != null) {
                        studentList.clear()
                        for (i in value) {
                            studentList.add(i.get("id").toString())
                        }
                    }
                }
        }


        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(raporGondermeyenList, secilenZaman)

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

        val sheet: Sheet = workbook.createSheet("Sayfa 1")

        //Create Header Cell Style
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

        noReportExcelButton.setOnClickListener {
            addData(
                sheet, secilenZaman, secilenGrade, this, workbook
            )

            askForPermissions()

        }


    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createExcel(this, secilenGrade, secilenZaman, workbook)
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
            createExcel(this, secilenGrade, secilenZaman, workbook)
        }
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        //setHeaderStyle is a custom function written below to add header style

        //Create sheet first row
        val row = sheet.createRow(0)

        //Header list
        val headerList = listOf(
            "column_1"
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

    @SuppressLint("Recycle", "Range", "SimpleDateFormat")
    fun createExcel(
        context: Context, secilenGrade: String, secilenZaman: String, workbook: XSSFWorkbook
    ) {

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH-mm")
        val current = formatter.format(time)

        val contentUri = MediaStore.Files.getContentUri("external")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"

            val selectionArgs =
                arrayOf(Environment.DIRECTORY_DOCUMENTS + "/Koçluk Takip Sistemi/") //must include "/" in front and end


            val cursor: Cursor? =
                context.contentResolver.query(contentUri, null, selection, selectionArgs, null)

            var uri: Uri? = null

            if (cursor != null) {
                if (cursor.count == 0) {
                    Toast.makeText(
                        context.applicationContext,
                        "Dosya Bulunamadı \"" + Environment.DIRECTORY_DOCUMENTS + "/Koçluk Takip Sistemi/\"",
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
                            Environment.DIRECTORY_DOCUMENTS + "/Koçluk Takip Sistemi/"
                        ) //end "/" is not mandatory
                        uri = context.contentResolver.insert(
                            MediaStore.Files.getContentUri("external"), values
                        ) //important!
                        val outputStream = context.contentResolver.openOutputStream(uri!!)
                        workbook.write(outputStream)
                        outputStream!!.flush()
                        //outputStream!!.write("This is menu category data.".toByteArray())
                        outputStream.close()
                        Toast.makeText(
                            context.applicationContext,
                            "Dosya Başarıyla Oluşturuldu",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(
                            context.applicationContext, "İşlem Başarısız!", Toast.LENGTH_SHORT
                        ).show()
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
                            context.applicationContext,
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
                                Environment.DIRECTORY_DOCUMENTS + "/Koçluk Takip Sistemi/"
                            ) //end "/" is not mandatory
                            uri = context.contentResolver.insert(
                                MediaStore.Files.getContentUri("external"), values
                            ) //important!
                            val outputStream = context.contentResolver.openOutputStream(uri!!)
                            workbook.write(outputStream)
                            outputStream!!.flush()
                            //outputStream!!.write("This is menu category data.".toByteArray())
                            outputStream.close()
                            Toast.makeText(
                                context.applicationContext,
                                "Dosya Başarıyla Oluşturuldu",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            Toast.makeText(
                                context.applicationContext, "İşlem Başarısız!", Toast.LENGTH_SHORT
                            ).show()
                        }


                    } else {
                        try {
                            val outputStream: OutputStream? =
                                context.contentResolver.openOutputStream(
                                    uri, "rwt"
                                ) //overwrite mode, see below
                            workbook.write(outputStream)
                            outputStream!!.flush()
                            //outputStream!!.write("This is menu category data.".toByteArray())
                            outputStream.close()
                            Toast.makeText(
                                context.applicationContext,
                                "Dosya Başarıyla Oluşturuldu",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            Toast.makeText(
                                context.applicationContext, "İşlem Başarısız!", Toast.LENGTH_SHORT
                            ).show()
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
                    context.applicationContext, "Dosya Başarıyla Oluşturuldu", Toast.LENGTH_SHORT
                ).show()
                fileOutputStream.flush()

                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println(e)
            }
        }


    }

    private fun addData(
        sheet: Sheet,
        secilenZaman: String,
        secilenGrade: String,
        context: Context,
        workbook: XSSFWorkbook
    ) {

        val rowFake = sheet.createRow(0)
        CellUtil.createCell(rowFake, 0, "Ad Soyad")
        var newIndex = 1
        for (i in raporGondermeyenList) {
            val row = sheet.createRow(newIndex)

            CellUtil.createCell(row, 0, i.studentName)
            newIndex += 1
        }
        createExcel(context, secilenGrade, secilenZaman, workbook)
    }
}
