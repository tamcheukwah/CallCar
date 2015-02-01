package com.rd.data;

import java.util.ArrayList;
import java.util.List;

public class staticData {
	private static String[] chooseText = { "��ƴ��", "ƴ����1��", "ƴ����2��", "ƴ����3��" };

	public static String numByChoose(String ss) {
		for (int i = 0; i < chooseText.length; i++) {
			if (ss.equals(chooseText[0])) {
				return "���4��";
			} else if (chooseText[i].equals(ss)) {
				return String.valueOf(i) + "��";
			}
		}

		return String.valueOf("0");
	}

	public static List<String> getList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < chooseText.length; i++) {
			list.add(chooseText[i]);
		}
		return list;
	}

	private static String[] Setting = { "��½", "ע��", "��ʷ��¼", "����", "�л��˻�", "�˳�" };

	public static List<String> getSettingList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < Setting.length; i++) {
			list.add(Setting[i]);
		}
		return list;
	}
}
