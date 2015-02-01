package com.rd.base;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;

public class CustomOverlayItem extends ItemizedOverlay<OverlayItem> {
	// private List<OverlayItem> GeoList = new ArrayList<OverlayItem>();
	private Context mContext;
	private OverlayItem overlay;
	boolean showtext;
	// private String title;
	private Drawable marker;

	/**
	 * marker ������
	 * 
	 * @param marker
	 * @param context
	 * @param p
	 * @param title
	 * @param sinppet
	 * @param showtext
	 */
	public CustomOverlayItem(Drawable marker, Context context, GeoPoint p,
			String title, String sinppet, boolean showtext) {
		super(boundCenterBottom(marker));
		this.mContext = context;
		// �ø����ľ�γ�ȹ���GeoPoint����λ��΢�� (�� * 1E6)
		// point = p;
		this.showtext = showtext;
		// this.title = title;
		this.marker = marker;
		overlay = new OverlayItem(p, title, sinppet);
		populate(); // createItem(int)��������item��һ���������ݣ��ڵ�����������ǰ�����ȵ����������
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlay;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean arg2) {
		super.draw(canvas, mapView, arg2);
		// Projection�ӿ�������Ļ��������;�γ������֮��ı任
		Projection projection = mapView.getProjection();
		String title = overlay.getTitle();
		// �Ѿ�γ�ȱ任�������MapView���Ͻǵ���Ļ��������
		Point point = projection.toPixels(overlay.getPoint(), null);
		// ���ڴ˴�������Ļ��ƴ���
		Paint paintText = new Paint();
		Paint paint = new Paint();
		paint.setAlpha(255);
		paint.setColor(Color.DKGRAY);
		paint.setStrokeWidth(5);
		paintText.setColor(Color.BLUE);
		paintText.setTextSize(15);
		// canvas.drawCircle(point.x, point.y, 100, paint);
		canvas.drawText(title, point.x - 30, point.y - 50, paintText); // �����ı�
		// ����һ��drawable�߽磬ʹ�ã�0��0�������drawable�ײ����һ�����ĵ�һ������
		boundCenterBottom(marker);
	}

	@Override
	// ��������¼�
	protected boolean onTap(int i) {
		if (showtext)
			Toast.makeText(this.mContext, overlay.getTitle(),
					Toast.LENGTH_SHORT).show();
		return true;
	}
}
