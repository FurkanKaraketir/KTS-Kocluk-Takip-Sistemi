package com.karaketir.coachingapp.services

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ortak Excel dışa aktarma: dosya kaydetme, başlık stilleri ve rapor üst bilgisi.
 */
object ExcelExportHelper {

    const val FOLDER_KTS = "Koçluk Takip Sistemi"
    const val FOLDER_STATS = "Koçluk İstatistikleri"
    private const val MIME_XLSX =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    data class ReportContext(
        val title: String,
        val gradeLabel: String? = null,
        val timeRangeLabel: String? = null,
        val startDate: Date? = null,
        val endDate: Date? = null,
        val extraLines: List<String> = emptyList()
    )

    fun needsLegacyStoragePermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun daysBetween(start: Date, end: Date): Double =
        maxOf(((end.time - start.time) / (1000.0 * 60 * 60 * 24)) + 1, 1.0)

    fun buildFileName(prefix: String, vararg parts: String?): String {
        val suffix = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault()).format(Date())
        val middle = parts.filterNot { it.isNullOrBlank() }.joinToString("_")
        return if (middle.isEmpty()) "${prefix}_$suffix.xlsx" else "${prefix}_${middle}_$suffix.xlsx"
    }

    fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        style.fillForegroundColor = IndexedColors.LIGHT_BLUE.index
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        val font = workbook.createFont()
        font.bold = true
        style.setFont(font)
        return style
    }

    fun createDecimalStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        style.dataFormat = workbook.createDataFormat().getFormat("0.00")
        return style
    }

    /**
     * Üst bilgi satırları yazar. Dönen değer: veri/başlık satırının başlayacağı satır indeksi.
     */
    fun writeReportInfoBlock(sheet: Sheet, context: ReportContext, columnSpan: Int): Int {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var rowIndex = 0

        fun addLine(text: String) {
            val row = sheet.createRow(rowIndex++)
            CellUtil.createCell(row, 0, text)
            if (columnSpan > 1) {
                sheet.addMergedRegion(
                    CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, columnSpan - 1)
                )
            }
        }

        addLine(context.title)

        val grade = context.gradeLabel?.takeIf { it.isNotBlank() }
        val time = context.timeRangeLabel?.takeIf { it.isNotBlank() }
        if (grade != null || time != null) {
            val parts = listOfNotNull(grade, time)
            addLine(parts.joinToString(" · "))
        }

        if (context.startDate != null && context.endDate != null) {
            addLine(
                "Tarih: ${dateFormat.format(context.startDate)} – ${dateFormat.format(context.endDate)}"
            )
        }

        context.extraLines.forEach { addLine(it) }

        return rowIndex + 1 // boş satır
    }

    fun writeStyledHeaderRow(
        sheet: Sheet,
        headers: Array<String>,
        style: CellStyle,
        rowIndex: Int,
        columnWidths: IntArray? = null
    ): Int {
        val row = sheet.createRow(rowIndex)
        headers.forEachIndexed { index, header ->
            val cell = row.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = style
            val width = columnWidths?.getOrNull(index) ?: when (index) {
                0 -> 30 * 256
                1 -> 12 * 256
                else -> 22 * 256
            }
            sheet.setColumnWidth(index, width)
        }
        return rowIndex + 1
    }

    fun saveWorkbook(
        context: Context,
        workbook: Workbook,
        fileName: String,
        subfolder: String = FOLDER_KTS
    ): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, MIME_XLSX)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOCUMENTS}/$subfolder/"
                    )
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    contentValues
                ) ?: throw IOException("Dosya oluşturulamadı")
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                } ?: throw IOException("Dosya yazılamadı")
            } else {
                @Suppress("DEPRECATION")
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    subfolder
                )
                if (!dir.exists()) dir.mkdirs()
                FileOutputStream(File(dir, fileName)).use { outputStream ->
                    workbook.write(outputStream)
                }
            }
            Toast.makeText(
                context,
                context.getString(com.karaketir.coachingapp.R.string.excel_dosya_olusturuldu),
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(
                    com.karaketir.coachingapp.R.string.excel_dosya_hata,
                    e.localizedMessage ?: e.message ?: ""
                ),
                Toast.LENGTH_LONG
            ).show()
            false
        } finally {
            if (workbook is XSSFWorkbook) {
                try {
                    workbook.close()
                } catch (_: IOException) {
                }
            }
        }
    }
}
