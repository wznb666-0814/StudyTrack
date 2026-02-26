package com.repea.studytrack.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Subject
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExcelHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportToUri(uri: Uri, subjects: List<Subject>, records: List<ExamWithSubject>): Boolean {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Grades")

        // Header
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("科目", "考试名称", "时间", "分数", "分类", "班排", "年排", "区排", "反思")
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        // Data
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(record.subject.name)
            row.createCell(1).setCellValue(record.exam.examName)
            row.createCell(2).setCellValue(dateFormat.format(Date(record.exam.examDate)))
            row.createCell(3).setCellValue(record.exam.score)
            row.createCell(4).setCellValue(record.exam.examType)
            row.createCell(5).setCellValue(record.exam.classRank?.toString() ?: "")
            row.createCell(6).setCellValue(record.exam.gradeRank?.toString() ?: "")
            row.createCell(7).setCellValue(record.exam.districtRank?.toString() ?: "")
            row.createCell(8).setCellValue(record.exam.reflection ?: "")
        }

        return try {
            context.contentResolver.openOutputStream(uri)?.use { 
                workbook.write(it) 
            }
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportToExcel(subjects: List<Subject>, records: List<ExamWithSubject>): Uri? {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Grades")

        // Header
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("科目", "考试名称", "时间", "分数", "分类", "班排", "年排", "区排", "反思")
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        // Data
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(record.subject.name)
            row.createCell(1).setCellValue(record.exam.examName)
            row.createCell(2).setCellValue(dateFormat.format(Date(record.exam.examDate)))
            row.createCell(3).setCellValue(record.exam.score)
            row.createCell(4).setCellValue(record.exam.examType)
            row.createCell(5).setCellValue(record.exam.classRank?.toString() ?: "")
            row.createCell(6).setCellValue(record.exam.gradeRank?.toString() ?: "")
            row.createCell(7).setCellValue(record.exam.districtRank?.toString() ?: "")
            row.createCell(8).setCellValue(record.exam.reflection ?: "")
        }

        // Save
        val filename = "StudyTrack_Backup_${System.currentTimeMillis()}.xlsx"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StudyTrack")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { 
                        workbook.write(it) 
                    }
                    workbook.close()
                    return uri
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StudyTrack")
            if (!dir.exists()) dir.mkdirs()
            
            val file = File(dir, filename)
            
            return try {
                val fos = FileOutputStream(file)
                workbook.write(fos)
                fos.close()
                workbook.close()
                Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return null
    }

    // Import needs more complex logic to match subjects, but for now basic reading
    fun readExcel(uri: Uri): List<ParsedRecord> {
        val records = mutableListOf<ParsedRecord>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (row in sheet) {
                if (row.rowNum == 0) continue // Skip header

                val subjectName = row.getCell(0)?.stringCellValue ?: continue
                val examName = row.getCell(1)?.stringCellValue ?: ""
                val dateStr = row.getCell(2)?.stringCellValue ?: ""
                val score = row.getCell(3)?.numericCellValue ?: 0.0
                val typeStr = row.getCell(4)?.stringCellValue ?: "其他"
                val classRank = row.getCell(5)?.stringCellValue?.toIntOrNull()
                val gradeRank = row.getCell(6)?.stringCellValue?.toIntOrNull()
                val districtRank = row.getCell(7)?.stringCellValue?.toIntOrNull()
                val reflection = row.getCell(8)?.stringCellValue

                val date = try {
                    dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                records.add(ParsedRecord(
                    subjectName, examName, date, score, typeStr, classRank, gradeRank, districtRank, reflection
                ))
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return records
    }
}

data class ParsedRecord(
    val subjectName: String,
    val examName: String,
    val date: Long,
    val score: Double,
    val type: String,
    val classRank: Int?,
    val gradeRank: Int?,
    val districtRank: Int?,
    val reflection: String?
)
