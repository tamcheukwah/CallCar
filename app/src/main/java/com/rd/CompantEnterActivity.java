package com.rd;

import com.rd.Util.ExitApplication;
import com.rd.entity.FeedMsg;
import com.rd.json.getJson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CompantEnterActivity extends Activity {

	private EditText feedmessage, contact;
	private Button back, clear, submit_feed;

	private ProgressDialog pro = null;

	private final int feedkbackSuccess = 0;
	final int fedkbackFail = 1;
	App app = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_layout);
		ExitApplication.getInstance().addActivity(this);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		app = (App) getApplication();

		// ��ʼ�����ضԻ���
		pro = new ProgressDialog(this);
		pro.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pro.setTitle("��ʾ");
		pro.setIcon(android.R.drawable.ic_dialog_info);
		pro.setMessage("�����ύ���������Ժ򡭡�");
		pro.setIndeterminate(false);
		pro.setCancelable(true);

		feedmessage = (EditText) findViewById(R.id.feedmessage);
		contact = (EditText) findViewById(R.id.contact);

		back = (Button) findViewById(R.id.back);
		clear = (Button) findViewById(R.id.clear);
		submit_feed = (Button) findViewById(R.id.submit_feed);

		back.setOnClickListener(new OnClk());
		clear.setOnClickListener(new OnClk());
		submit_feed.setOnClickListener(new OnClk());
	}

	class OnClk implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.clear:
				if (feedmessage.isFocused())
					feedmessage.setText("");
				if (contact.isFocused())
					contact.setText("");
				break;
			case R.id.submit_feed:
				SubMit();
				break;
			case R.id.back:
				CompantEnterActivity.this.finish();
				break;
			}
		}
	}

	private void Clear() {
		feedmessage.setText("");
		contact.setText("");
	}

	private void SubMit() {
		String msg = feedmessage.getText().toString().trim();
		String add = contact.getText().toString().trim();

		if (msg.equals("")) {
			ShowToast("�����뷴�����ݣ�");
			return;
		}

		if (msg.length() > 500) {
			ShowToast("�������ݹ��࣬��ɾ���������ݣ�");
			return;
		}

		if (add.length() > 200) {
			ShowToast("��ϵ��ʽ������������޸ĳ��ȣ�");
			return;
		}

		final FeedMsg fedmsg = new FeedMsg();
		fedmsg.setUserid(app.getUSerid());
		fedmsg.setContent(msg);
		fedmsg.setAddress(add);

		pro.show();
		new Thread(new Runnable() {

			public void run() {
				try {
					boolean isSub = getJson.SubFeedBack(fedmsg);
					if (isSub) {
						Message message = new Message();
						message.what = feedkbackSuccess;
						handler.sendMessage(message);
					} else {
						Message message = new Message();
						message.what = fedkbackFail;
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					Message message = new Message();
					message.what = fedkbackFail;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pro.dismiss();
			switch (msg.what) {
			case feedkbackSuccess:
				ShowToast("�����ɹ�����л���ķ�����");
				Clear();
				break;
			case fedkbackFail:
				ShowToast("����ʧ�ܣ�����������������ύ����л����ʹ�ã�");
				break;
			}
		}
	};

	private void ShowToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
