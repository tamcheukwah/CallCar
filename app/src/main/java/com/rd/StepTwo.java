package com.rd;

import com.rd.Util.ExitApplication;
import com.rd.adapter.ChooseAdapter;
import com.rd.data.staticData;
import com.rd.db.SQLiteHelper;
import com.rd.entity.HistoryBean;
import com.rd.entity.PointInfo;
import com.rd.json.getJson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StepTwo extends Activity {

	private AutoCompleteTextView endName;
	private String[] autoString;
	private SQLiteHelper mOpenHelper;

	private ListView customerChooseList;
	private TextView ShowDropDown;
	private Button nextStep, back;

	private ProgressDialog mpDialog;
	App app;

	final int LOGINSUCCESS_MSG = 0;
	final int LOGINFAIL_MSG = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twostep_layout);
		ExitApplication.getInstance().addActivity(this);

		app = (App) this.getApplication();
		mOpenHelper = new SQLiteHelper(this);

		// ��ʼ�����ضԻ���
		mpDialog = new ProgressDialog(this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setTitle(R.string.loading_data);
		mpDialog.setIcon(android.R.drawable.ic_dialog_info);
		mpDialog.setMessage(getString(R.string.sending));
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(true);

		getHistory();
		endName = (AutoCompleteTextView) findViewById(R.id.endName);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list_item, autoString);
		endName.setAdapter(adapter);

		ShowDropDown = (TextView) findViewById(R.id.ShowDropDown);
		customerChooseList = (ListView) findViewById(R.id.customerChooseList);

		ShowDropDown.setOnClickListener(new OnClick());
		ChooseAdapter cadapter = new ChooseAdapter(this, staticData.getList());
		customerChooseList.setAdapter(cadapter);
		customerChooseList.setOnItemClickListener(new OnItem());

		nextStep = (Button) findViewById(R.id.nextStep);
		nextStep.setOnClickListener(new OnClick());

		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new OnClick());
	}

	class OnItem implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			customerChooseList.setVisibility(View.GONE);
			ShowDropDown.setText(((TextView) arg1.findViewById(R.id.text))
					.getText());
		}
	}

	class OnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ShowDropDown:
				if (customerChooseList.getVisibility() == View.VISIBLE) {
					customerChooseList.setVisibility(View.GONE);
				} else {
					customerChooseList.setVisibility(View.VISIBLE);
				}
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				imm.hideSoftInputFromWindow(endName.getWindowToken(), 0);
				break;
			case R.id.nextStep:
				goNext();
				break;
			case R.id.back:
				StepTwo.this.finish();
				break;
			}
		}
	}

	private void goNext() {
		String teminal = endName.getText().toString().trim();
		String type = ShowDropDown.getText().toString().trim();
		if (teminal.equals("")) {
			ShowToast(R.string.enterEnd);
			return;
		}

		if (type.equals(R.string.chooseCustomer)) {
			ShowToast(R.string.chooseType);
			return;
		}

		insertTable(teminal);

		SaveEnd("key", teminal);
		SaveEnd("type", type);

		ShowSureDialog();
	}

	private void ShowSureDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(StepTwo.this)
				.setTitle(R.string.sureCall)
				.setMessage(
						String.format(getString(R.string.callMsg),
								getSave("key"),
								staticData.numByChoose(getSave("type"))))
				.setPositiveButton(R.string.sure,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								SendInfoToServer();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								// startActivity(new Intent(StepTwo.this,
								// StepOne.class));
							}
						}).create();
		alertDialog.show();
	}

	private void SendInfoToServer() {

		mpDialog.show();

		final PointInfo info = new PointInfo();
		info.setUserid(app.getUSerid());
		info.setCity(getSave("city"));
		info.setLang(getSave("lang"));
		info.setLant(getSave("lant"));
		info.setType(getSave("type"));
		info.setDestnation(getSave("key"));

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					int isLogin = 121;//getJson.SendInfo(info);
					Message message = new Message();
					message.what = LOGINSUCCESS_MSG;
					message.obj = isLogin;
					mhandler.sendMessage(message);
				} catch (Exception e) {
					Message message = new Message();
					message.what = LOGINFAIL_MSG;
					mhandler.sendMessage(message);
				}
			}
		}).start();
	}

	// �̴߳���
	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mpDialog.dismiss();
			switch (msg.what) {
			case LOGINSUCCESS_MSG:
				if (((Integer) msg.obj).equals(-1)) {
					ShowToast("���ѽг��������ظ��г���");
				} else {
					SaveEnd("backId", String.valueOf(msg.obj));
					// startActivity(new Intent(StepTwo.this,
					// CallSuccess.class));
					startActivity(new Intent(StepTwo.this, StepThree.class));
				}
				break;
			case LOGINFAIL_MSG:
				ShowToast("�г�ʧ�ܣ�");
				break;
			}
		}
	};

	private void SaveEnd(String type, String begin) {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.edit().putString(type, begin).commit();
	}

	private String getSave(String key) {
		return PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString(key, "");
	}

	private void ShowToast(int res) {
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	private void ShowToast(String res) {
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	/* �����в������� */
	private void insertTable(String title) {
		try {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			Cursor myCursor_one = db.rawQuery("SELECT * FROM "
					+ SQLiteHelper.TB_NAME + " where name=?",
					new String[] { String.valueOf(title) });
			String sql;
			String tip;
			if (myCursor_one.moveToFirst()) {
				sql = "update " + SQLiteHelper.TB_NAME + " set "
						+ HistoryBean.TIME + "=datetime('now') where "
						+ HistoryBean.NAME + "='" + title + "'";
				tip = "����";
			} else {
				sql = "insert into " + SQLiteHelper.TB_NAME + " ("
						+ HistoryBean.TIME + ", " + HistoryBean.NAME + ") "
						+ "values( datetime('now'),'" + title + "');";
				tip = "����";
			}
			try {
				myCursor_one.close();
				myCursor_one = null;
				db.execSQL(sql);
			} catch (SQLException e) {
				Toast.makeText(StepTwo.this, tip + "��¼����", Toast.LENGTH_LONG)
						.show();
				return;
			}
		} catch (Exception e) {
		}
	}

	// ȡ����ʷ��¼
	private void getHistory() {
		try {
			Cursor myCursor_one;
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			myCursor_one = db.rawQuery("SELECT * FROM " + SQLiteHelper.TB_NAME
					+ " order by " + HistoryBean.TIME + " desc", null);
			int name = myCursor_one.getColumnIndex(HistoryBean.NAME);
			autoString = new String[myCursor_one.getCount()];
			int i = 0;
			if (myCursor_one.moveToFirst()) {
				do {
					autoString[i] = myCursor_one.getString(name);
					i++;
				} while (myCursor_one.moveToNext());
			}
			myCursor_one.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
