package com.my.myapp.reports;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.my.myapp.utils.UserPreferencesManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfHeaderHelper {

	// مقاس الشعار المقترح (عرض 80، ارتفاع 80)
	private static final float LOGO_WIDTH = 80f;
	private static final float LOGO_HEIGHT = 80f;

	/**
	* تنشئ هذه الدالة ترويسة موحدة لملفات PDF وتضيفها للمستند.
	*
	* @param context      السياق (Context) للوصول للموارد مثل الخطوط و SharedPreferences.
	* @param document     مستند الـ PDF الذي تريد إضافة الترويسة إليه.
	* @param reportTitle  عنوان التقرير الذي سيظهر تحت الترويسة.
	* @param fromDate    تاريخ بداية الفلترة (يمكن أن يكون null).
	* @param toDate      تاريخ نهاية الفلترة (يمكن أن يكون null).
	* @throws IOException        في حال حدوث خطأ أثناء قراءة ملف الخط.
	* @throws DocumentException  في حال حدوث خطأ أثناء التعامل مع مستند الـ PDF.
	*/
	public static void addHeaderToDocument(Context context, Document document, String reportTitle, String fromDate,
			String toDate) throws IOException, DocumentException {

		// 1. جلب البيانات من SharedPreferences
		UserPreferencesManager prefs = UserPreferencesManager.getInstance(context);
		String companyName = prefs.getCompany();
		String phoneNumber = prefs.getPhone();
		Bitmap logoBitmap = prefs.getLogo();

		// 2. تحميل الخط العربي
		File fontFile = copyFontFromAssets(context, "Mirza-SemiBold.ttf");
		BaseFont baseFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		Font fontArabicHeader = new Font(baseFont, 14, Font.BOLD);
		Font fontArabicStatement = new Font(baseFont, 16, Font.BOLD);

		// 3. الحصول على التاريخ الحالي
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
		String currentDate = sdf.format(new Date());

		// 4. إنشاء جدول الترويسة (3 أعمدة)
		PdfPTable headerTable = new PdfPTable(3);
		headerTable.setWidthPercentage(100);
		headerTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
		headerTable.setWidths(new float[] { 3f, 4f, 3f });

		// --- العمود الأول (textColumn) ---
		PdfPCell textColumn = new PdfPCell();
		textColumn.setBorder(Rectangle.NO_BORDER);
		textColumn.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
		textColumn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		textColumn.setVerticalAlignment(Element.ALIGN_TOP);
		textColumn.setPadding(5);

		textColumn.addElement(new Paragraph("الجمهورية اليمنية", fontArabicHeader));
		textColumn
				.addElement(new Paragraph(companyName.isEmpty() ? "________________" : companyName, fontArabicHeader));
		textColumn.addElement(new Paragraph("دفتر الحسابات", fontArabicHeader));

		headerTable.addCell(textColumn);

		// --- العمود الثاني (logoColumn) ---
		// --- العمود الثاني (logoColumn) ---
		PdfPCell logoColumn = new PdfPCell();
		logoColumn.setBorder(Rectangle.NO_BORDER);
		logoColumn.setHorizontalAlignment(Element.ALIGN_CENTER); // التوسيط الأفقي للخلية
		logoColumn.setVerticalAlignment(Element.ALIGN_TOP); // التوسيط الرأسي للخلية
		logoColumn.setPadding(0); // إزالة الحشو مؤقتاً

		if (logoBitmap != null) {
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				Image logoImage = Image.getInstance(stream.toByteArray());

				// ضبط حجم الشعار
				logoImage.scaleToFit(LOGO_WIDTH, LOGO_HEIGHT);

				// === الحل الجديد: تغليف الصورة داخل Paragraph ===
				Paragraph logoParagraph = new Paragraph();
				logoParagraph.setAlignment(Element.ALIGN_CENTER); // توسيط الصورة داخل الفقرة
				logoParagraph.setLeading(0, 1); // تعيين الـ leading (المسافة بين الأسطر) ليكون بنفس ارتفاع الصورة
				logoParagraph.add(new Chunk(logoImage, 0, 0, true)); // إضافة الصورة كـ Chunk

				logoColumn.addElement(logoParagraph);
				// =====================================================

			} catch (Exception e) {
				Log.e("PdfHeaderHelper", "Error adding logo to PDF", e);
			}
		}
		headerTable.addCell(logoColumn);

		// --- العمود الثالث (dateColumn) ---
		// --- العمود الثالث (dateColumn) ---
		PdfPCell dateColumn = new PdfPCell();
		dateColumn.setBorder(Rectangle.NO_BORDER);
		dateColumn.setHorizontalAlignment(Element.ALIGN_RIGHT); // محاولة سابقة (قد لا تكون ضرورية الآن)
		dateColumn.setRunDirection(PdfWriter.RUN_DIRECTION_LTR); // <-- الحل: إضافة هذا السطر
		dateColumn.setVerticalAlignment(Element.ALIGN_TOP);
		dateColumn.setPadding(5);
		
		// لا حاجة لتغيير شيء هنا، الفقرات ستتبع اتجاه الخلية تلقائياً
		dateColumn.addElement(new Paragraph(currentDate, fontArabicHeader));
		dateColumn.addElement(new Paragraph(phoneNumber.isEmpty() ? "________________" : phoneNumber, fontArabicHeader));
		dateColumn.addElement(new Paragraph(" ", fontArabicHeader));
		
		headerTable.addCell(dateColumn);

		// ... (كود إضافة headerTable للمستند)
		// ... (كود إضافة headerTable للمستند)
		document.add(headerTable);
		
		// === الحل الأفضل والأوضح: استخدام setLeading للتحكم في المسافة ===
		LineSeparator line = new LineSeparator();
		line.setLineColor(BaseColor.BLACK);
		line.setLineWidth(2f);
		
		// وضع الخط الفاصل داخل فقرة للتحكم في المسافة الرأسية
		Paragraph lineParagraph = new Paragraph();
		lineParagraph.add(line); // إضافة الخط للفقرة
		
		// === التحكم في المسافات ===
		// setLeading(leading, multipleOfLeading)
		// - leading: المسافة الأساسية بين الأسطر.
		// - multipleOfLeading: مضاعف للمسافة. عادة ما يكون 1.0.
		// لزيادة المسافة *فوق* الخط، نزيد من قيمة leading.
		lineParagraph.setLeading(5, 1); // جرب قيماً أكبر مثل 40 أو 50 لرفع الخط أكثر
		// ==================================================
		
		document.add(lineParagraph);
		// =============================================================================
		
		// ... (باقي الكود لإنشاء جدول العنوان statementTable)
		// =============================================================================
		
		// ... (باقي الكود لإنشاء جدول العنوان statementTable)
		// 6. إنشاء جدول عنوان التقرير
		PdfPTable statementTable = new PdfPTable(1);
		statementTable.setWidthPercentage(100);
		statementTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

		PdfPCell statementCell = new PdfPCell();
		statementCell.setBorder(Rectangle.NO_BORDER);
		statementCell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
		statementCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		statementCell.setPadding(10);

		String dateRangeText;
		if (fromDate != null && toDate != null) {
			dateRangeText = "من تاريخ: " + fromDate + " إلى تاريخ: " + toDate;
		} else {
			dateRangeText = "من تاريخ: البداية" + " إلى تاريخ: " + currentDate;
		}

		String fullTitle = reportTitle + "\n" + dateRangeText;

		Paragraph statementPara = new Paragraph(fullTitle, fontArabicStatement);
		statementPara.setAlignment(Element.ALIGN_CENTER);
		statementCell.addElement(statementPara);

		statementTable.addCell(statementCell);
		document.add(statementTable);
	}

	/**
	* نسخ الخط من مجلد assets إلى ذاكرة التخزين المؤقتة للتطبيق.
	*/
	public static File copyFontFromAssets(Context context, String fontFileName) throws IOException {
		File outFile = new File(context.getCacheDir(), fontFileName);
		if (!outFile.exists()) {
			try (InputStream is = context.getAssets().open("fonts/" + fontFileName);
					OutputStream os = new FileOutputStream(outFile)) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}
		}
		return outFile;
	}
}