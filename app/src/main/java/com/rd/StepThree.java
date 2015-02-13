package com.rd;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKLine;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.RouteOverlay;
import com.baidu.mapapi.TransitOverlay;
import com.rd.Util.ExitApplication;
import com.rd.data.staticData;
import com.rd.entity.PointInfo;
import com.rd.json.getJson;
import com.rd.view.MarqueeText;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StepThree extends MapActivity {

	MapView mMapView = null; // 地图View
	MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	MapController mMapController;

	private Button refresh, local, back, detail;

	private ProgressDialog mpDialog;
	Dialog dialog = null;

	final int LOGINSUCCESS_MSG = 0;
	final int LOGINFAIL_MSG = 1;
	final int getAddSuccess = 2;
	final int getAddFail = 3;
	App app;
	TextView loadText;
	MarqueeText line;
	List<String> step;
	int numSteps = 0;

	LocationListener mLocationListener = null;// onResume时注册此listener，onPause时需要Remove
	MyLocationOverlay mLocationOverlay = null; // 定位图层

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threestep_layout);
		ExitApplication.getInstance().addActivity(this);

		app = (App) this.getApplication();
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new App.MyGeneralListener());
		}
		app.mBMapMan.start();
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(app.mBMapMan);

		line = (MarqueeText) findViewById(R.id.line);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.setDrawOverlayWhenZooming(true);
		mMapView.setBuiltInZoomControls(true);
		// 初始化搜索模块，注册事件监听
		mMapController = mMapView.getController(); // 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
				(int) (116.404 * 1E6)); // 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(point); // 设置地图中心点
		// mMapController.setZoom(12); // 设置地图zoom级别
		//
		// // 初始化搜索模块，注册事件监听
		mSearch = new MKSearch();
		mSearch.setTransitPolicy(MKSearch.EBUS_TRANSFER_FIRST);
		mSearch.init(app.mBMapMan, new MKSearchListener() {

			@Override
			public void onGetPoiDetailSearchResult(int type, int error) {
			}

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
				// 错误号可参考MKEvent中的定义
				if (error != 0 || res == null) {
					loadText.setText("定位失败！");
					// refresh.setEnabled(false);
					Toast.makeText(StepThree.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}
				loadText.setVisibility(View.GONE);
				mMapView.setVisibility(View.VISIBLE);
				line.setVisibility(View.VISIBLE);

				RouteOverlay routeOverlay = new RouteOverlay(StepThree.this,
						mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(routeOverlay);
				mMapView.invalidate();

				mMapView.getController().animateTo(res.getStart().pt);

				mkStep(res, null, null);
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
				if (error != 0 || res == null) {
					Toast.makeText(StepThree.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}
				TransitOverlay routeOverlay = new TransitOverlay(
						StepThree.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(routeOverlay);
				mMapView.invalidate();

				mMapView.getController().animateTo(res.getStart().pt);
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
				if (error != 0 || res == null) {
					Toast.makeText(StepThree.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}
				RouteOverlay routeOverlay = new RouteOverlay(StepThree.this,
						mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(routeOverlay);
				mMapView.invalidate();

				mMapView.getController().animateTo(res.getStart().pt);

			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
			}

			public void onGetPoiResult(MKPoiResult res, int arg1, int arg2) {
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int iError) {

				if (iError != 0 || res == null) {

					Toast.makeText(StepThree.this, "抱歉，未找到结果",
							Toast.LENGTH_LONG).show();

					return;

				}

				int nSize = res.getSuggestionNum();

				String[] mStrSuggestions = new String[nSize];

				for (int i = 0; i < nSize; i++) {

					mStrSuggestions[i] = res.getSuggestion(i).city
							+ res.getSuggestion(i).key;
				}

				ListView list = new ListView(StepThree.this);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						StepThree.this,
						android.R.layout.simple_selectable_list_item,
						mStrSuggestions);
				list.setAdapter(adapter);
				list.setCacheColorHint(0);
				dialog = new AlertDialog.Builder(StepThree.this).setTitle("路线")
						.setPositiveButton("确定", null).setView(list).create();
				//
				// ArrayAdapter<String> suggestionString = new
				// ArrayAdapter<String>(
				// StepThree.this, android.R.layout.simple_list_item_1,
				// mStrSuggestions);
				//
				// mSuggestionList.setAdapter(suggestionString);

			}

			@Override
			public void onGetRGCShareUrlResult(String arg0, int arg1) {

			}
		});

		mMapController.setZoom(18); // 设置地图zoom级别

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);

		// 注册定位事件
		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					GeoPoint pt = new GeoPoint(
							(int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(pt);

					final double lang = location.getLatitude();
					final double lant = location.getLongitude();

					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								String add = getJson.getAddress(lang, lant,
										StepThree.this);
								if (add != "") {
									Message message = new Message();
									message.what = getAddSuccess;
									message.obj = add;
									handler.sendMessage(message);
								} else {
									Message message = new Message();
									message.what = getAddFail;
									handler.sendMessage(message);
								}
							} catch (Exception e) {
								Message message = new Message();
								message.what = getAddFail;
								handler.sendMessage(message);
							}
						}
					}).start();
				}
			}
		};

		// 初始化加载对话框
		mpDialog = new ProgressDialog(this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setTitle(R.string.loading_data);
		mpDialog.setIcon(android.R.drawable.ic_dialog_info);
		mpDialog.setMessage(getString(R.string.sending));
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(true);

		refresh = (Button) findViewById(R.id.refresh);
		refresh.setText("取消叫车");
		refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ShowSureDialog();
				Cancel();
			}
		});

		local = (Button) findViewById(R.id.local);
		local.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchButtonProcess();
			}
		});

		back = (Button) findViewById(R.id.back);
		back.setVisibility(View.GONE);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StepThree.this.finish();
			}
		});

		detail = (Button) findViewById(R.id.detail);
		detail.setVisibility(View.GONE);
		detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});

		loadText = (TextView) findViewById(R.id.loadText);

		// SearchButtonProcess();
	}

	private void Cancel() {
		mpDialog.show();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					boolean isCancel =true; //getJson.cancelCall(app.getUSerid(),
//							getSave("backId"));
					if (isCancel) {
						Message message = new Message();
						message.what = LOGINSUCCESS_MSG;
						handler.sendMessage(message);
					} else {
						Message message = new Message();
						message.what = LOGINFAIL_MSG;
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					Message message = new Message();
					message.what = LOGINFAIL_MSG;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	// 线程处理
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mpDialog.dismiss();
			switch (msg.what) {
			case LOGINSUCCESS_MSG:
				ShowToast("取消打车成功！");
				startActivity(new Intent(StepThree.this, StepOne.class));
				StepThree.this.finish();
				break;
			case LOGINFAIL_MSG:
				ShowToast("取消打车失败！");
				break;
			case getAddFail:
				loadText.setText("定位失败，请稍后重试！");
				break;
			case getAddSuccess:
				mMapView.setVisibility(View.VISIBLE);
				loadText.setVisibility(View.GONE);
				break;
			}
		}
	};

	@SuppressWarnings("unused")
	private void ShowSureDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(StepThree.this)
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
								startActivity(new Intent(StepThree.this,
										StepOne.class));
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
					int isLogin = getJson.SendInfo(info);
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

	// 线程处理
	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mpDialog.dismiss();
			switch (msg.what) {
			case LOGINSUCCESS_MSG:
				if (((Integer) msg.obj).equals(-1)) {
					ShowToast("您已叫车，请勿重复叫车！");
				} else {
					SaveEnd("backId", String.valueOf(msg.obj));
					startActivity(new Intent(StepThree.this, CallSuccess.class));
				}
				break;
			case LOGINFAIL_MSG:
				ShowToast("叫车失败！");
				break;
			}
		}
	};

	private void ShowToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	void SearchButtonProcess() {
		// 对起点终点的name进行赋值，也可以直接对坐标赋值，赋值坐标则将根据坐标进行搜索
		MKPlanNode stNode = new MKPlanNode();
		stNode.pt = getFromSave();
		MKPlanNode enNode = new MKPlanNode();
		enNode.name = getSave("key");

		// 实际使用中请对起点终点城市进行正确的设定
		mSearch.drivingSearch("", stNode, getSave("city"), enNode);
		// mSearch.suggestionSearch(getSave("city"));
		loadText.setVisibility(View.VISIBLE);
	}

	public void mkStep(MKDrivingRouteResult res1, MKTransitRouteResult res2,
			MKWalkingRouteResult res3) {
		step = new ArrayList<String>();
		if (res1 != null) {
			numSteps = res1.getPlan(0).getRoute(0).getNumSteps() - 1;
			if (numSteps > 0) {
				for (int i = 0; i < numSteps; i++) {
					step.add(res1.getPlan(0).getRoute(0).getStep(i)
							.getContent());
				}
			}

			DecimalFormat decimalFormat = new DecimalFormat(".#");
			double c = Double.parseDouble(decimalFormat.format(res1.getPlan(0)
					.getRoute(0).getDistance() * 1.0 / 1000));

			line.setText("总路程：" + res1.getPlan(0).getRoute(0).getDistance()
					+ "米 约:" + c + "公里");
		} else if (res2 != null) {
			numSteps = res2.getPlan(0).getNumLines();
			int i;
			int j1 = res2.getPlan(0).getNumRoute();
			if (numSteps > 0) {
				for (i = 0; i < numSteps; i++) {
					MKLine line = res2.getPlan(0).getLine(i);
					if (line.getType() == MKLine.LINE_TYPE_BUS) {
						step.add("乘坐公交" + line.getTitle() + "路");
						step.add("自" + line.getGetOnStop().name + "经过"
								+ line.getNumViaStops() + "站到"
								+ line.getGetOffStop().name + "---"
								+ line.getDistance() + "m");

					} else if (line.getType() == MKLine.LINE_TYPE_SUBWAY) {
						step.add("乘坐" + line.getTitle());
						step.add("自" + line.getGetOnStop().name + "经过"
								+ line.getNumViaStops() + "站到"
								+ line.getGetOffStop().name + "---"
								+ line.getDistance() + "m");

					}
					if (i <= j1)
						step.add("在" + line.getGetOffStop().name + "下车，步行"
								+ res2.getPlan(0).getRoute(i + 1).getDistance()
								+ "m");
				}
			}
			step.add("到达终点");
		} else {
			numSteps = res3.getPlan(0).getRoute(0).getNumSteps() - 1;
			if (numSteps > 0) {
				for (int i = 0; i < numSteps; i++) {
					step.add(res3.getPlan(0).getRoute(0).getStep(i)
							.getContent());
				}
			}
		}
		if (numSteps > 0) {
			ListView list = new ListView(this);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_selectable_list_item, step);
			list.setAdapter(adapter);
			list.setCacheColorHint(0);
			dialog = new AlertDialog.Builder(StepThree.this).setTitle("路线")
					.setPositiveButton("确定", null)
					// .setNegativeButton("分享到人人",
					// new DialogInterface.OnClickListener() {
					//
					// @Override
					// public void onClick(DialogInterface dialog,
					// int which) {
					// String text = "";
					// for (int i = 0; i < step.size(); i++) {
					// if (i != step.size() - 1)
					// text = text + step.get(i) + "，";
					// else
					// text = text + step.get(i) + "。";
					// }
					// }
					// })
					.setView(list).create();
		}
	}

	private GeoPoint getFromSave() {
		double lang = Double.parseDouble(getSave("lang"));
		double lant = Double.parseDouble(getSave("lant"));
		GeoPoint pt = new GeoPoint((int) (lang * 1e6), (int) (lant * 1e6));
		return pt;
	}

	private String getSave(String key) {
		return PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString(key, "");
	}

	private void SaveEnd(String type, String begin) {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.edit().putString(type, begin).commit();
	}

	@Override
	protected void onPause() {
		App app = (App) this.getApplication();
		app.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		// mLocationOverlay.disableCompass(); // 关闭指南针
		app.mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		App app = (App) this.getApplication();
		// 注册定位事件，定位后将地图移动到定位点
		app.mBMapMan.getLocationManager().requestLocationUpdates(
				mLocationListener);
		mLocationOverlay.enableMyLocation();
		// mLocationOverlay.enableCompass(); // 打开指南针
		app.mBMapMan.start();

		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			final AlertDialog alertDialog = new AlertDialog.Builder(
					StepThree.this)
					.setTitle(R.string.cacelCall)
					.setMessage(R.string.cancelCallHint)
					.setPositiveButton(R.string.sure,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Cancel();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
			alertDialog.show();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
