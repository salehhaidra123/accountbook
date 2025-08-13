package com.my.myapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.itextpdf.text.pdf.BaseFont;
import com.my.myapp.DeleteAccountDialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AllAccountsActivity extends AppCompatActivity implements
		DeleteAccountDialogFragment.OnDeleteConfirmedListener, EditAccountDialogFragment.OnAccountEditedListener {
	TextView tvId, tvName, tvBalance;
	ListView listView;
	Toolbar toolbar;
	Button btnExportToPdf;
	AllAccountsAdapter adapter;
	DatabaseHelper dbHelper;
	ArrayList<Account> accountList;
	String selectedAccType;
	private ActionMode actionMode;
	private int selectedItemPosition = -1;

	private static final int REQUEST_PERMISSION = 123;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_accounts);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("قائمة الحسابات");

		btnExportToPdf = findViewById(R.id.btn_export_pdf);
		listView = findViewById(R.id.list_view_all_accounts);

		dbHelper = new DatabaseHelper(getBaseContext());
		accountList = dbHelper.getAllAccounts();
		adapter = new AllAccountsAdapter(getBaseContext(), accountList);
		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			if (actionMode == null) {
				Account selectedAccount = accountList.get(position);
				selectedItemPosition = position;
				actionMode = startSupportActionMode(actionModeCallback);
			}
			return true;
		});

		btnExportToPdf.setOnClickListener(v -> {
			if (checkPermissions()) {
				createPdfAndOpen();
			}
		});
	}

	// باقي الكود كما هو (loadAccountList, onCreateContextMenu, onContextItemSelected, showDeleteAccountDialog, showEditAccountDialog)

	public void loadAccountList() {
		accountList = dbHelper.getAllAccounts();
		adapter = new AllAccountsAdapter(this, accountList);
		listView.setAdapter(adapter);
	}

	// bring delete account dialog
	private void showDeleteAccountDialog(int accountId) {
		DeleteAccountDialogFragment dialog = DeleteAccountDialogFragment.newInstance(accountId);
		dialog.show(getSupportFragmentManager(), "DeleteDialog");
	}

	// bring edit account dialog
	public void showEditAccountDialog(Account account) {
		EditAccountDialogFragment dialog = EditAccountDialogFragment.newInstance(account);
		dialog.show(getSupportFragmentManager(), "EditAccountDialog");
	}

	// edit account listener
	@Override
	public void onAccountEdited() {
		loadAccountList(); // ✅ يتم تحديث القائمة عند التعديل
	}

	//delete account listener
	@Override
	public void onAccountDeleteConfirmed(int accountId) {
		dbHelper.deleteAccountById(accountId);
		loadAccountList(); // تحديث القائمة
	}

	private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_edit:
				Account accToEdit = accountList.get(selectedItemPosition);
				showEditAccountDialog(accToEdit);
				mode.finish();
				return true;

			case R.id.menu_delete:
				Account accToDelete = accountList.get(selectedItemPosition);
				showDeleteAccountDialog(accToDelete.getAccountId());
				mode.finish();
				return true;

			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			selectedItemPosition = -1;
		}
	};

	private File copyFontFromAssets(String fontFileName) throws IOException {
		File outFile = new File(getCacheDir(), fontFileName);
		if (!outFile.exists()) {
			try (InputStream is = getAssets().open("fonts/" + fontFileName);
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

	private void createPdfAndOpen() {
		Document document = new Document();

		String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				+ "/AccountsList.pdf";

		File pdfFile = new File(filePath);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
			document.open();

			// تحميل الخط العربي من assets/fonts/Amiri-Regular.ttf
			File fontFile = copyFontFromAssets("Amiri-Regular.ttf"); // يجب أن تنشئ هذه الدالة لنسخ الخط من assets إلى مسار يمكن قراءته
			BaseFont baseFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			Font fontArabicHeader = new Font(baseFont, 16, Font.BOLD);
			Font fontArabicTableHeader = new Font(baseFont, 14, Font.BOLD);
			Font fontArabicTableCell = new Font(baseFont, 12, Font.NORMAL);

			// إنشاء جدول الترويسة 3 أعمدة
			PdfPTable headerTable = new PdfPTable(3);
			headerTable.setWidthPercentage(100);
			headerTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			headerTable.setWidths(new float[] { 1f, 1f, 1f });

			// خلايا العمود الأول والثاني فارغة بدون حدود
			PdfPCell emptyCell1 = new PdfPCell(new Phrase(""));
			emptyCell1.setBorder(Rectangle.NO_BORDER);
			PdfPCell emptyCell2 = new PdfPCell(new Phrase(""));
			emptyCell2.setBorder(Rectangle.NO_BORDER);

			// سطر 1 في العمود الأيمن
			PdfPCell cell1 = new PdfPCell(new Phrase("الجمهورية اليمنية", fontArabicHeader));
			cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell1.setBorder(Rectangle.NO_BORDER);

			// سطر 2 في العمود الأيمن
			PdfPCell cell2 = new PdfPCell(new Phrase("تطبيق دفتر الحسابات", fontArabicHeader));
			cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell2.setBorder(Rectangle.NO_BORDER);

			// سطر 3 في العمود الأيمن
			PdfPCell cell3 = new PdfPCell(new Phrase("تقرير الحسابات", fontArabicHeader));
			cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell3.setBorder(Rectangle.NO_BORDER);

			// إضافة الصف الأول (خلايا عمود 1 و2 فارغة، وعمود 3 النص)
			headerTable.addCell(cell1);
			headerTable.addCell(emptyCell2);
			headerTable.addCell(emptyCell1);

			// إضافة الصف الثاني
			headerTable.addCell(cell2);
			headerTable.addCell(emptyCell2);
			headerTable.addCell(emptyCell1);

			// إضافة الصف الثالث
			headerTable.addCell(cell3);
			headerTable.addCell(emptyCell2);
			headerTable.addCell(emptyCell1);
		
			headerTable.setSpacingAfter(20f);
			document.add(headerTable);
			//	document.add(headerTable);
				
			// إضافة خط فاصل
			LineSeparator line = new LineSeparator();
			line.setLineColor(BaseColor.BLACK);
			line.setLineWidth(1f);
			document.add(new Chunk(line));
			document.add(Chunk.NEWLINE);


			// إعداد جدول البيانات
			PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

			String[] headers = { "المعرف", "تاريخ الإنشاء", "الاسم", "الهاتف" };
			BaseColor headerBgColor = new BaseColor(230, 230, 230);

			for (String colHeader : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(colHeader, fontArabicTableHeader));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(headerBgColor);
				cell.setPadding(8f);
				table.addCell(cell);
			}

			// افترض أن accountList موجودة ومليئة بالبيانات
			for (Account acc : accountList) {
				PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(acc.getAccountId()), fontArabicTableCell));
				idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				idCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				idCell.setPadding(6f);
				table.addCell(idCell);

				PdfPCell dateCell = new PdfPCell(new Phrase(acc.getCreatedDate(), fontArabicTableCell));
				dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				dateCell.setPadding(6f);
				table.addCell(dateCell);

				PdfPCell nameCell = new PdfPCell(new Phrase(acc.getAccountName(), fontArabicTableCell));
				nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				nameCell.setPadding(6f);
				table.addCell(nameCell);

				PdfPCell phoneCell = new PdfPCell(new Phrase(acc.getAccountPhone(), fontArabicTableCell));
				phoneCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				phoneCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				phoneCell.setPadding(6f);
				table.addCell(phoneCell);
			}

			document.add(table);
			document.close();

			Toast.makeText(this, "تم إنشاء ملف PDF في: " + filePath, Toast.LENGTH_LONG).show();
			openPdfFile(pdfFile);

		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "حدث خطأ أثناء إنشاء PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void openPdfFile(File file) {
		Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			startActivity(intent);
		} catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(this, "لا يوجد تطبيق مثبت لعرض ملفات PDF", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean checkPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION);
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				createPdfAndOpen();
			} else {
				Toast.makeText(this, "الصلاحيات مطلوبة لحفظ الملف", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
