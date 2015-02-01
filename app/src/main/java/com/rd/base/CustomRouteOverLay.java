package com.rd.base;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.baidu.mapapi.RouteOverlay;
import com.rd.R;

/**
 * custom route overlay
 * 
 * @author neverever415
 * 
 */
public class CustomRouteOverLay extends RouteOverlay {

	public Activity ac;
	private MapView mapView;

	static ArrayList<View> overlayviews = new ArrayList<View>();

	public CustomRouteOverLay(Activity arg0, MapView arg1) {
		super(arg0, arg1);
		ac = arg0;
		mapView = arg1;
	}

	@Override
	protected boolean onTap(int arg0) {
		// return super.onTap(arg0);
		return true;
	}

	@Override
	public void setData(MKRoute arg0) {
		super.setData(arg0);
		addHint(arg0);
	}

	public void addHints(MKRoute routes) {
		for (int i = 0; i < routes.getNumSteps(); i++) {
			Drawable marker = ac.getResources().getDrawable(R.drawable.pop); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight()); // Ϊmaker����λ�úͱ߽�
			OverItemT overitem = new OverItemT(marker, ac, routes.getStep(i)
					.getContent(), routes.getStep(i).getPoint());
			// OverlayItem over=new OverlayItem(routes.GET, null, null);
			mapView.getOverlays().add(overitem); // ���ItemizedOverlayʵ����mMapView
		}
		mapView.invalidate();
	}

	/**
	 * ���� ָʾ·��
	 * 
	 * @param routes
	 */
	public void addHint(MKRoute routes) {
		mapView.getOverlays().clear();// �����
		// mapView.removeAllViewsInLayout();
		View mPopView = ac.getLayoutInflater().inflate(R.layout.popview, null);
		for (int i = 0; i < overlayviews.size(); i++) {
			System.out.println("remove &" + i);
			mapView.removeViewInLayout(overlayviews.get(i));
			overlayviews.remove(i);
		}

		mapView.invalidate();
		// ���ItemizedOverlay
		for (int i = 0; i < routes.getNumSteps(); i++) {

			Drawable marker = ac.getResources().getDrawable(R.drawable.pop); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight()); // Ϊmaker����λ�úͱ߽�
			GeoPoint pt = routes.getStep(i).getPoint();// =
														// routes.get(i).getPoint();
			if (i != 0 && i != routes.getNumSteps() - 1) {
				mPopView = ac.getLayoutInflater().inflate(R.layout.popview,
						null);
				mapView.addView(mPopView, new MapView.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						null, MapView.LayoutParams.TOP_LEFT));
				mPopView.setVisibility(View.GONE);
				mapView.updateViewLayout(mPopView, new MapView.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						pt, MapView.LayoutParams.BOTTOM_CENTER));
				mPopView.setVisibility(View.VISIBLE);
				Button button = (Button) mPopView
						.findViewById(R.id.overlay_pop);
				button.setText(routes.getStep(i).getContent());
				overlayviews.add(mPopView);
				overlayviews.add(button);
			} else {
				// �޸���ʼ����յ���ʽ-�Զ���
				mPopView = ac.getLayoutInflater().inflate(R.layout.popview,
						null);
				mapView.addView(mPopView, new MapView.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						null, MapView.LayoutParams.TOP_LEFT));
				mPopView.setVisibility(View.GONE);
				mapView.updateViewLayout(mPopView, new MapView.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						pt, MapView.LayoutParams.BOTTOM_CENTER));
				mPopView.setVisibility(View.VISIBLE);
				Button button = (Button) mPopView
						.findViewById(R.id.overlay_pop);
				button.offsetTopAndBottom(100);
				button.setTextColor(Color.BLUE);
				button.setBackgroundColor(Color.TRANSPARENT);
				button.setText(routes.getStep(i).getContent());
				overlayviews.add(mPopView);
				overlayviews.add(button);
			}
		}
	}

	class OverItemT extends ItemizedOverlay<OverlayItem> {

		private Drawable marker;
		private OverlayItem o;

		public OverItemT(Drawable marker, Context context, String title,
				GeoPoint p) {
			super(boundCenterBottom(marker));
			this.marker = marker;
			// ����OverlayItem��������������Ϊ��item��λ�ã������ı�������Ƭ��
			o = new OverlayItem(p, title, title);
			populate(); // createItem(int)��������item��һ���������ݣ��ڵ�����������ǰ�����ȵ����������
		}

		public void updateOverlay() {
			populate();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			// Projection�ӿ�������Ļ��������;�γ������֮��ı任
			Projection projection = mapView.getProjection();
			for (int index = size() - 1; index >= 0; index--) { // ����mGeoList
				OverlayItem overLayItem = getItem(index); // �õ�����������item
				String title = overLayItem.getTitle();
				// �Ѿ�γ�ȱ任�������MapView���Ͻǵ���Ļ��������
				Point point = projection.toPixels(overLayItem.getPoint(), null);
				// ���ڴ˴�������Ļ��ƴ���
				Paint paintText = new Paint();
				paintText.setColor(Color.BLUE);
				paintText.setTextSize(15);
				canvas.drawText(title, point.x - 30, point.y, paintText); // �����ı�
			}
			super.draw(canvas, mapView, shadow);
			// ����һ��drawable�߽磬ʹ�ã�0��0�������drawable�ײ����һ�����ĵ�һ������
			boundCenterBottom(marker);
		}

		@Override
		protected OverlayItem createItem(int i) {
			return o;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		// ��������¼�
		protected boolean onTap(int i) {
			// ��������λ��,��ʹ֮��ʾ
			return true;
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// ��ȥ����������
			// ItemizedOverlayDemo.mPopView.setVisibility(View.GONE);
			return super.onTap(arg0, arg1);
		}
	}

}
