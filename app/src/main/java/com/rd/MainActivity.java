package com.rd;

import com.rd.Util.DialogManager;
import com.rd.Util.ExitApplication;
import com.rd.Util.UpdateCustomer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;//滑动菜单相关包

public class MainActivity extends Activity {
	Button button_set, button_record, button_next;
	App app = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);//主视图
		ExitApplication.getInstance().addActivity(this);
        final SlidingMenu menu = new SlidingMenu(this);//初始化滑动组件
		app = (App) getApplication();

		new UpdateCustomer(this, false).Logging();
        //设置按钮
		button_set = (Button) findViewById(R.id.button_set);
        button_set.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getLogin()) {
                    menu.toggle();//滑动的方法
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
            }
        });
        //叫车记录按钮
		button_record = (Button) findViewById(R.id.button_record);
        button_record.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getLogin()) {
                    startActivity(new Intent(MainActivity.this, CallRecord.class));
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
            }
        });
        //下一步按钮
		button_next = (Button) findViewById(R.id.button_next);
        button_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getLogin()) {
                    startActivity(new Intent(MainActivity.this, StepOne.class));
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
            }
        });

        menu.setMode(SlidingMenu.LEFT);//设置菜单的位置
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//设置菜单滑动的样式
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);//菜单滑动时阴影部分
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setBehindWidth(200);//菜单宽带
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.setting_layout);//添加菜单
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
            new DialogManager().showExitHit(this);
            return false;
        }
		return super.onKeyDown(keyCode, event);
	}
}
