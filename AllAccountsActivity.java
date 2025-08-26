package com.my.myapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.my.myapp.DeleteAccountDialogFragment;
import com.my.myapp.EditAccountDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

public class AllAccountsActivity extends AppCompatActivity
		implements DeleteAccountDialogFragment.OnDeleteConfirmedListener,
		EditAccountDialogFragment.OnAccountEditedListener, AddNewAccountDialogFragment.OnAccountAddedListener {

	TextView tvId, tvName, tvBalance;
	ListView listView;
	Toolbar toolbar;
	Button btnExportToPdf;
	FloatingActionButton fabAddNewaAcc;
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
		adapter = new AllAccountsAdapter(getBaseContext(), accountList, listView);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			if (actionMode == null) {
				Account selectedAccount = accountList.get(position);
				listView.setItemChecked(position, true);
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

		fabAddNewaAcc = findViewById(R.id.fab_add_account);
		fabAddNewaAcc.setOnClickListener(v -> {
			AddNewAccountDialogFragment fragment = AddNewAccountDialogFragment.newInstance(true);
			fragment.show(getSupportFragmentManager(), "AddAccount");
		});
	}

	public void loadAccountList() {
		accountList = dbHelper.getAllAccounts();
		adapter = new AllAccountsAdapter(this, accountList, listView);
		listView.setAdapter(adapter);
	}

	private void showDeleteAccountDialog(int accountId) {
		DeleteAccountDialogFragment dialog = DeleteAccountDialogFragment.newInstance(accountId);
		dialog.show(getSupportFragmentManager(), "DeleteDialog");
	}

	public void showEditAccountDialog(Account account) {
		EditAccountDialogFragment dialog = EditAccountDialogFragment.newInstance(account);
		dialog.show(getSupportFragmentManager(), "EditAccountDialog");
	}

	@Override
	public void onAccountEdited() {
		loadAccountList();
		listView.clearChoices();
		//	((BaseAdapter) listView.getAdapter()).notifyDataSetChanged(); //
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onAccountDeleteConfirmed(int accountId) {
		dbHelper.deleteAccountById(accountId);
		loadAccountList();
	}
	/*@Override
	public void onBackPressed() {
		if (listView.getCheckedItemPosition() != ListView.INVALID_POSITION) {
			listView.clearChoices();
			adapter.notifyDataSetChanged();
			} else {
			super.onBackPressed();
		}
	}*/

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
			listView.clearChoices();
			adapter.notifyDataSetChanged();
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
		String fileName = "AccountsList_" + System.currentTimeMillis() + ".pdf";
		OutputStream outputStream = null;
		Uri uri = null;

		try {
			// إنشاء الملف باستخدام MediaStore API
			ContentValues values = new ContentValues();
			values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
			values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
			values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

			uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
			if (uri == null) {
				Toast.makeText(this, "فشل في إنشاء ملف PDF", Toast.LENGTH_SHORT).show();
				return;
			}

			outputStream = getContentResolver().openOutputStream(uri);
			if (outputStream == null) {
				Toast.makeText(this, "فشل في فتح دفق الإخراج", Toast.LENGTH_SHORT).show();
				return;
			}

			// إنشاء مستند PDF
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();

			// تحميل الخط العربي
			File fontFile = copyFontFromAssets("Amiri-Regular.ttf");
			BaseFont baseFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fontArabicHeader = new Font(baseFont, 16, Font.BOLD);
			Font fontArabicTableHeader = new Font(baseFont, 14, Font.BOLD);
			Font fontArabicTableCell = new Font(baseFont, 12, Font.NORMAL);

			// إنشاء جدول الترويسة
			// إنشاء جدول الترويسة 3 أعمدة
			PdfPTable headerTable = new PdfPTable(3);
			headerTable.setWidthPercentage(100);
			headerTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			headerTable.setWidths(new float[] { 3f, 1f, 1f }); // العمود الأول أعرض لاحتواء النصوص

			// العمود الأول (النصوص الرئيسية)
			PdfPCell textColumn = new PdfPCell();
			textColumn.setBorder(Rectangle.NO_BORDER);
			textColumn.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

			// إضافة النصوص في العمود الأول مع محاذاة كاملة لليمين
			Paragraph p1 = new Paragraph("الجمهورية اليمنية", fontArabicHeader);
			p1.setAlignment(Element.ALIGN_RIGHT);
			p1.setSpacingAfter(5f); // مسافة بين السطور
			textColumn.addElement(p1);

			Paragraph p2 = new Paragraph(" دفتر الحسابات", fontArabicHeader);
			p2.setAlignment(Element.ALIGN_RIGHT);
			p2.setSpacingAfter(5f);
			textColumn.addElement(p2);

			Paragraph p3 = new Paragraph("تقرير الحسابات", fontArabicHeader);
			p3.setAlignment(Element.ALIGN_RIGHT);
			textColumn.addElement(p3);

			headerTable.addCell(textColumn);

			// العمود الثاني (محجوز للشعار مستقبلاً)
			PdfPCell logoColumn = new PdfPCell();
			logoColumn.setBorder(Rectangle.NO_BORDER);
			logoColumn.setHorizontalAlignment(Element.ALIGN_CENTER);
			logoColumn.setVerticalAlignment(Element.ALIGN_MIDDLE);
			// مستقبلاً يمكنك إضافة:
			// Image logo = Image.getInstance(logoPath);
			// logo.scaleToFit(50, 50);
			// logoColumn.addElement(logo);
			headerTable.addCell(logoColumn);

			// العمود الثالث (محجوز للتاريخ أو معلومات أخرى)
			PdfPCell infoColumn = new PdfPCell();
			infoColumn.setBorder(Rectangle.NO_BORDER);
			infoColumn.setHorizontalAlignment(Element.ALIGN_LEFT);
			infoColumn.setVerticalAlignment(Element.ALIGN_TOP);
			// مستقبلاً يمكنك إضافة:
			// Paragraph datePara = new Paragraph(new SimpleDateFormat("yyyy/MM/dd").format(new Date()), fontArabicTableCell);
			// datePara.setAlignment(Element.ALIGN_LEFT);
			// infoColumn.addElement(datePara);
			headerTable.addCell(infoColumn);

			headerTable.setSpacingAfter(20f);
			document.add(headerTable);

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

			// إضافة البيانات
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
			outputStream.close();

			Toast.makeText(this, "تم حفظ ملف PDF بنجاح", Toast.LENGTH_LONG).show();

			// فتح الملف
			openPdfFile(uri);

		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "حدث خطأ أثناء إنشاء PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

	private void openPdfFile(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "لا يوجد تطبيق مثبت لعرض ملفات PDF", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean checkPermissions() {
		// في Android 10+، لا نحتاج إلى صلاحية WRITE_EXTERNAL_STORAGE
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(
						Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
					return true;
				} else {
					requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION);
					return false;
				}
			} else {
				return true;
			}
		}
		return true;
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

	@Override
	public void onAccountAdded(String name, String phone, String date) {
		loadAccountList();
	}
}