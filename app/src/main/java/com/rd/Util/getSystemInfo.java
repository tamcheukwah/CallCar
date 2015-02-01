package com.rd.Util;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class getSystemInfo {
	public static String getVersionName(Activity activity) {
		// ��ȡpackagemanager��ʵ��
		PackageManager packageManager = activity.getPackageManager();
		// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packInfo.versionName;
	}

	public static int getVersionCode(Activity activity) {
		// ��ȡpackagemanager��ʵ��
		PackageManager packageManager = activity.getPackageManager();
		// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packInfo.versionCode;
	}
}
