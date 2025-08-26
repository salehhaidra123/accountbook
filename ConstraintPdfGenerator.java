package com.my.myapp.reports;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.my.myapp.Constraint;
import com.my.myapp.DBConstants;
import com.my.myapp.DatabaseHelper;
import com.my.myapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream; // تأكد من وجود هذا الاستيراد
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstraintPdfGenerator {
	
	public static void createAndOpenPdf(Context context,
	String accountName,
	List<Constraint> constraintList,
	Map<Integer, String> typeIdToNameMap,
	int accountId,
	String accountType,
	String fromDateFilter,
	String toDateFilter) {
		
		String fileName = "ConstraintsList_" + System.currentTimeMillis() + ".pdf";
		OutputStream outputStream = null;
		Uri uri = null;
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		
		try {
			ContentValues values = new ContentValues();
			values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
			values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
			}
			uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
			if (uri == null) {
				Toast.makeText(context, "فشل في إنشاء ملف PDF", Toast.LENGTH_SHORT).show();
				return;
			}
			
			outputStream = context.getContentResolver().openOutputStream(uri);
			if (outputStream == null) {
				Toast.makeText(context, "فشل في فتح دفق الإخراج", Toast.LENGTH_SHORT).show();
				return;
			}
			
			// الخيار الثاني: هوامش صغيرة (موصى به)
			// Document(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom)
			Document document = new Document(PageSize.A4, 10, 10, 10, 10);
			PdfWriter.getInstance(document, outputStream);
			document.open();
			
			// === 1. إضافة الترويسة ===
			String reportTitle = "كشف حساب: " + accountName;
			PdfHeaderHelper.addHeaderToDocument(context, document, reportTitle, fromDateFilter, toDateFilter);
			// =========================
			
			// === 2. تحميل الخطوط ===
			// هذا الاستدعاء الآن صحيح لأن الدالة أصبحت public static
			File fontFile = PdfHeaderHelper.copyFontFromAssets(context, "Mirza-SemiBold.ttf");
			BaseFont baseFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fontArabicTableHeader = new Font(baseFont, 14, Font.BOLD);
			Font fontArabicTableCell = new Font(baseFont, 12, Font.NORMAL);
			Font fontArabicTotal = new Font(baseFont, 12, Font.BOLD);
			// =====================
			
			// === 3. حساب المجاميع باستخدام الدوال الجديدة من DatabaseHelper ===
			double totalDebit = dbHelper.getTotalDebitByDate(accountId, fromDateFilter, toDateFilter);
			double totalCredit = dbHelper.getTotalCreditByDate(accountId, fromDateFilter, toDateFilter);
			double generalBalance = dbHelper.getAccountBalanceByDate(accountId, accountType, fromDateFilter, toDateFilter);
			// ================================================================
			
			// === 4. إنشاء وإضافة جدول البيانات ===
			PdfPTable table = new PdfPTable(6);
			table.setWidthPercentage(100);
			table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			String[] headers = {"م", "التاريخ", "البيان", "مدين", "دائن", "الرصيد"};
			BaseColor headerBgColor = new BaseColor(230, 230, 230);
			
			for (String colHeader : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(colHeader, fontArabicTableHeader));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(headerBgColor);
				cell.setPadding(8f);
				cell.setMinimumHeight(30f);
				table.addCell(cell);
			}
			
			DecimalFormat formatter = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.getDefault()));
			for (Constraint constraint : constraintList) {
				double balance = constraint.getDebit() - constraint.getCredit();
				
				table.addCell(createCell(String.valueOf(constraint.getId()), fontArabicTableCell));
				table.addCell(createCell(constraint.getDate(), fontArabicTableCell));
				
				String typeName = typeIdToNameMap.get(constraint.getConstraintTypeId());
				if (typeName == null) typeName = "غير معروف";
				table.addCell(createCell(typeName + ": " + constraint.getDetails(), fontArabicTableCell));
				
				table.addCell(createCell(formatter.format(constraint.getDebit()), fontArabicTableCell));
				table.addCell(createCell(formatter.format(constraint.getCredit()), fontArabicTableCell));
				table.addCell(createCell(formatter.format(balance), fontArabicTableCell, balance < 0));
			}
			
			// إضافة سطر إجمالي العمليات
			PdfPCell totalLabelCell = new PdfPCell(new Phrase("اجمالي العمليات", fontArabicTotal));
			totalLabelCell.setColspan(3);
			totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			totalLabelCell.setBackgroundColor(new BaseColor(240, 240, 240));
			totalLabelCell.setMinimumHeight(30f);
			table.addCell(totalLabelCell);
			table.addCell(createCell(formatter.format(totalDebit), fontArabicTotal, new BaseColor(240, 240, 240)));
			table.addCell(createCell(formatter.format(totalCredit), fontArabicTotal, new BaseColor(240, 240, 240)));
			table.addCell(createCell("", fontArabicTotal, new BaseColor(240, 240, 240)));
			
			// إضافة سطر إجمالي الرصيد العام
			PdfPCell balanceLabelCell = new PdfPCell(new Phrase("اجمالي الرصيد العام", fontArabicTotal));
			balanceLabelCell.setColspan(3);
			balanceLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			balanceLabelCell.setBackgroundColor(new BaseColor(220, 220, 220));
			balanceLabelCell.setMinimumHeight(30f);
			table.addCell(balanceLabelCell);
			
			PdfPCell generalBalanceCell = new PdfPCell(new Phrase(formatter.format(generalBalance), fontArabicTotal));
			generalBalanceCell.setColspan(3);
			generalBalanceCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			if (generalBalance < 0) {
				generalBalanceCell.setBackgroundColor(new BaseColor(255, 180, 180));
				} else if (generalBalance > 0) {
				generalBalanceCell.setBackgroundColor(new BaseColor(180, 255, 180));
				} else {
				generalBalanceCell.setBackgroundColor(new BaseColor(220, 220, 220));
			}
			table.addCell(generalBalanceCell);
			
			document.add(table);
			document.close();
			outputStream.close();
			
			Toast.makeText(context, "تم حفظ ملف PDF بنجاح", Toast.LENGTH_LONG).show();
			openPdfFile(context, uri);
			
			} catch (DocumentException | IOException e) {
			e.printStackTrace();
			Toast.makeText(context, "حدث خطأ أثناء إنشاء PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
			} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
					} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static PdfPCell createCell(String text, Font font) {
		return createCell(text, font, false, null);
	}
	
	private static PdfPCell createCell(String text, Font font, boolean isNegative) {
		return createCell(text, font, isNegative, null);
	}
	
	private static PdfPCell createCell(String text, Font font, BaseColor backgroundColor) {
		return createCell(text, font, false, backgroundColor);
	}
	
	private static PdfPCell createCell(String text, Font font, boolean isNegative, BaseColor backgroundColor) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPadding(6f);
		cell.setMinimumHeight(25f);
		if (isNegative) {
			cell.setBackgroundColor(new BaseColor(255, 200, 200));
			} else if (backgroundColor != null) {
			cell.setBackgroundColor(backgroundColor);
		}
		return cell;
	}
	
	private static void openPdfFile(Context context, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		try {
			context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "لا يوجد تطبيق مثبت لعرض ملفات PDF", Toast.LENGTH_SHORT).show();
		}
	}
}