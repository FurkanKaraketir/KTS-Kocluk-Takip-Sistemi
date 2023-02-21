package com.karaketir.coachingapp.services

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.R
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

fun ImageView.glide(url: String?, placeholder: CircularProgressDrawable) {
    val options = RequestOptions().placeholder(placeholder).error(R.drawable.blank)

    Glide.with(context.applicationContext).setDefaultRequestOptions(options).load(url).into(this)
}

fun placeHolderYap(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 40f
        start()
    }
}

fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
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

fun getHeaderStyle(workbook: Workbook): CellStyle {

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
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val current = formatter.format(time)

    val contentUri = MediaStore.Files.getContentUri("external")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"

        val selectionArgs =
            arrayOf(Environment.DIRECTORY_DOCUMENTS + "/Koçluk İstatistikleri/") //must include "/" in front and end


        val cursor: Cursor? =
            context.contentResolver.query(contentUri, null, selection, selectionArgs, null)

        var uri: Uri? = null

        if (cursor != null) {
            if (cursor.count == 0) {
                Toast.makeText(
                    context.applicationContext,
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
                            Environment.DIRECTORY_DOCUMENTS + "/Koçluk İstatistikleri/"
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
                        val outputStream: OutputStream? = context.contentResolver.openOutputStream(
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

fun addData(
    sheet: Sheet,
    secilenZaman: String,
    secilenGrade: String,
    currentKurumKodu: String,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    context: Context,
    workbook: XSSFWorkbook
) {

    var cal = Calendar.getInstance()
    cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

    var bitisTarihi = cal.time
    var baslangicTarihi = cal.time
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
        db.collection("School").document(currentKurumKodu).collection("Student")
            .whereEqualTo("teacher", auth.uid.toString())
            .whereEqualTo("grade", secilenGrade.toInt()).orderBy("nameAndSurname")
            .addSnapshotListener { students, _ ->
                if (students != null) {
                    var index = 1
                    var counter = 0
                    val studentCount = students.size()
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

                        db.collection("School").document(currentKurumKodu).collection("Student")
                            .document(i.id).collection("Studies")
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

                                                biyoSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                biyoSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Coğrafya" -> {

                                                cografyaSure += study.get("toplamCalisma")
                                                    .toString().toInt()
                                                cografyaSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Din" -> {

                                                dinSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                dinSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Diğer" -> {

                                                digerSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                digerSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Felsefe" -> {

                                                felsefeSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                felsefeSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Fizik" -> {

                                                fizikSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                fizikSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Geometri" -> {

                                                geometriSure += study.get("toplamCalisma")
                                                    .toString().toInt()
                                                geometriSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Kimya" -> {

                                                kimyaSure += study.get("toplamCalisma").toString()
                                                    .toInt()
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
                                                paragrafSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Problem" -> {

                                                problemSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                problemSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Tarih" -> {

                                                tarihSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                tarihSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Türkçe-Edebiyat" -> {

                                                turkceSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                turkceSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                        }


                                        ogrenciToplamSure += study.get("toplamCalisma").toString()
                                            .toInt()
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
                                    createExcel(context, secilenGrade, secilenZaman, workbook)
                                }


                            }

                        index += 1
                    }
                }
            }


    } else {
        db.collection("School").document(currentKurumKodu).collection("Student")
            .whereEqualTo("teacher", auth.uid.toString()).orderBy("nameAndSurname")
            .addSnapshotListener { students, _ ->
                if (students != null) {
                    var index = 1
                    var counter = 0
                    val studentCount = students.size()
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

                        db.collection("School").document(currentKurumKodu).collection("Student")
                            .document(i.id).collection("Studies")
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

                                                biyoSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                biyoSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Coğrafya" -> {

                                                cografyaSure += study.get("toplamCalisma")
                                                    .toString().toInt()
                                                cografyaSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Din" -> {

                                                dinSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                dinSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Diğer" -> {

                                                digerSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                digerSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Felsefe" -> {

                                                felsefeSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                felsefeSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Fizik" -> {

                                                fizikSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                fizikSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Geometri" -> {

                                                geometriSure += study.get("toplamCalisma")
                                                    .toString().toInt()
                                                geometriSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Kimya" -> {

                                                kimyaSure += study.get("toplamCalisma").toString()
                                                    .toInt()
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
                                                paragrafSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Problem" -> {

                                                problemSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                problemSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Tarih" -> {

                                                tarihSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                tarihSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                            "Türkçe-Edebiyat" -> {

                                                turkceSure += study.get("toplamCalisma").toString()
                                                    .toInt()
                                                turkceSoru += study.get("çözülenSoru").toString()
                                                    .toInt()


                                            }
                                        }

                                        ogrenciToplamSure += study.get("toplamCalisma").toString()
                                            .toInt()
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
                                    createExcel(context, secilenGrade, secilenZaman, workbook)
                                }

                            }

                        index += 1
                    }
                }
            }


    }


}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)

