package com.rd.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.rd.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

public class UpdateService extends Service {
	private NotificationManager nm;
	private Notification notification;
	private File tempFile = null;
	private boolean cancelUpdate = false;
	private MyHandler myHandler;
	private int download_precent = 0;
	private RemoteViews views;
	private int notificationId = 1234;

	@Override
	//IBinder�ӿ��ǶԿ���̵Ķ���ĳ�����ͨ�����ڵ�ǰ���̿��Է��ʣ�
	//���ϣ�������ܱ��������̷��ʣ��Ǿͱ���ʵ��IBinder�ӿڡ�IBinder�ӿڿ���ָ�򱾵ض���
	//Ҳ����ָ��Զ�̶��󣬵����߲���Ҫ����ָ��Ķ����Ǳ��صĻ���Զ�̡�
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@SuppressWarnings("deprecation")
	@Override
	//����service��ʱ��onCreate����ֻ�е�һ�λ���ã�onStartCommand��onStartÿ�ζ������á�
	//onStartCommand�����ϵͳ��������������ж��Ƿ��쳣��ֹ�������������ں���������쳣��ֹ  
	public int onStartCommand(Intent intent, int flags, int startId) {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;//����֪ͨ��״̬����ʾ��ͼ��
		// notification.icon=android.R.drawable.stat_sys_download_done;
		notification.tickerText = getString(R.string.app_name) + "����";//�����ǵ��֪ͨʱ��ʾ������
		notification.when = System.currentTimeMillis();
		notification.defaults = Notification.DEFAULT_LIGHTS;//֪ͨʱ������Ĭ������
		// ���������������ؽ�����ʾ��views
		//RemoteViews��������һ��View�����ܹ���ʾ�����������У�
		//�����ںϴ�һ�� layout��Դ�ļ�ʵ�ֲ��֡�
		views = new RemoteViews(getPackageName(), R.layout.update);
		notification.contentView = views;
		//Pendingintent��һ������ Notification�ϣ��������Ϊ�ӳ�ִ�е�intent
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
		notification.setLatestEventInfo(this, "", "", contentIntent);
		// ������������ӵ���������
		nm.notify(notificationId, notification);

		myHandler = new MyHandler(Looper.myLooper(), this);
		// ��ʼ��������������views
		Message message = myHandler.obtainMessage(3, 0);
		myHandler.sendMessage(message);
		// �����߳̿�ʼִ����������
		downFile(intent.getStringExtra("url"));
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// ���ظ����ļ�
	private void downFile(final String url) {
		new Thread() {
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
					// params[0]�������ӵ�url
					HttpGet get = new HttpGet(url);
					HttpResponse response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					InputStream is = entity.getContent();

					File rootFile;
					if (is != null) {
						rootFile = new File("/sdcard/callCar");
						if (!rootFile.exists() && !rootFile.isDirectory())
							rootFile.mkdir();

						tempFile = new File("/sdcard/callCar/" + url.substring(url.lastIndexOf("/") + 1));
						if (tempFile.exists())
							tempFile.delete();
						tempFile.createNewFile();

						// �Ѷ�������Ϊ��������һ�����л���������
						BufferedInputStream bis = new BufferedInputStream(is);

						// ����һ���µ�д����������ȡ����ͼ������д�뵽�ļ���
						FileOutputStream fos = new FileOutputStream(tempFile);
						// ��д������Ϊ��������һ�����л����д����
						BufferedOutputStream bos = new BufferedOutputStream(fos);

						int read;
						long count = 0;
						int precent = 0;
						byte[] buffer = new byte[1024];
						while ((read = bis.read(buffer)) != -1 && !cancelUpdate) {
							bos.write(buffer, 0, read);
							count += read;
							precent = (int) (((double) count / length) * 100);
							// ÿ�������5%��֪ͨ�����������޸����ؽ���
							if (precent - download_precent >= 5) {
								download_precent = precent;
								Message message = myHandler.obtainMessage(3, precent);
								myHandler.sendMessage(message);
							}
						}
						bos.flush();
						bos.close();
						fos.flush();
						fos.close();
						is.close();
						bis.close();
					}

					if (!cancelUpdate) {
						Message message = myHandler.obtainMessage(2, tempFile);
						myHandler.sendMessage(message);
					} else {
						tempFile.delete();
					}
				} catch (ClientProtocolException e) {
					Message message = myHandler.obtainMessage(4, "���ظ����ļ�ʧ��");
					myHandler.sendMessage(message);
				} catch (IOException e) {
					Message message = myHandler.obtainMessage(4, "���ظ����ļ�ʧ��");
					myHandler.sendMessage(message);
				} catch (Exception e) {
					Message message = myHandler.obtainMessage(4, "���ظ����ļ�ʧ��");
					myHandler.sendMessage(message);
				}
			}
		}.start();
	}

	// ��װ���غ��apk�ļ�
	private void Instanll(File file, Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/* �¼������� */
	class MyHandler extends Handler {
		private Context context;

		public MyHandler(Looper looper, Context c) {
			super(looper);
			this.context = c;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg != null) {
				switch (msg.what) {
				case 0:
					Toast.makeText(context, msg.obj.toString(),
							Toast.LENGTH_SHORT).show();
					break;
				case 1:
					break;
				case 2:
					// ������ɺ��������������Ϣ��ִ�а�װ��ʾ
					download_precent = 0;
					nm.cancel(notificationId);
					Instanll((File) msg.obj, context);
					// ֹͣ����ǰ�ķ���
					stopSelf();
					break;
				case 3:
					// ����״̬���ϵ����ؽ�����Ϣ
					views.setTextViewText(R.update_id.tvProcess, "������"
							+ download_precent + "%");
					views.setProgressBar(R.update_id.pbDownload, 100,
							download_precent, false);
					notification.contentView = views;
					nm.notify(notificationId, notification);
					break;
				case 4:
					nm.cancel(notificationId);
					break;
				}
			}
		}
	}
}
