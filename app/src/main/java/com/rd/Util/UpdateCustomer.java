package com.rd.Util;

import com.rd.R;
import com.rd.entity.UpdataInfo;
import com.rd.json.getJson;
import com.rd.service.UpdateService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class UpdateCustomer {
	private Activity act;
	private final int UPDATA_CLIENT = 5;
	private final int GET_UNDATAINFO_ERROR = 6;
	private boolean isShow = false;

	public UpdateCustomer(Activity activity, boolean isShowHint) {
		this.act = activity;
		isShow = isShowHint;
	}

	public void Lgoining() {

		new Thread(new Runnable() {

			public void run() {
				try {
					int versionName = getSystemInfo.getVersionCode(act);
					UpdataInfo info = getJson.getNewsVersion();
					if (info.getVersion() > versionName) {
						Message msg = new Message();
						msg.what = UPDATA_CLIENT;
						msg.obj = info;
						handler.sendMessage(msg);
					} else {
						Message msg = new Message();
						msg.what = GET_UNDATAINFO_ERROR;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = GET_UNDATAINFO_ERROR;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATA_CLIENT:
				// �Ի���֪ͨ�û���������
				showUpdataDialog((UpdataInfo) msg.obj);
				break;
			case GET_UNDATAINFO_ERROR:
				if (isShow) {
					Toast.makeText(act, R.string.NotNeedUpt, Toast.LENGTH_LONG)
							.show();
				}
				break;
			}
		}
	};

	private void showUpdataDialog(final UpdataInfo info) {
		try {
			AlertDialog.Builder builer = new Builder(act);
			builer.setTitle(R.string.versionUpdate);
			builer.setMessage(info.getDescription());
			builer.setPositiveButton(R.string.sure,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							act.startService(new Intent(act,
									UpdateService.class).putExtra("url",
									info.getUrl()));
						}
					});
			// ����ȡ����ťʱ���е�¼
			builer.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			AlertDialog dialog = builer.create();
			dialog.show();
		} catch (Exception e) {

		}
	}
}
