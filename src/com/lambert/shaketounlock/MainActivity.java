package com.lambert.shaketounlock;

import cn.waps.AppConnect;
import cn.waps.extend.SlideWall;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	private Button btnEnable = null;
	private Button btnDisable = null;
	private SensorManager sensorManager = null;
	private Vibrator vibrator = null;
	private static final String TAG = "MainActivity";
	private static final int SENSOR_SHAKE = 10;
	private static boolean bEnabled = false;
	private View slidingDrawerView;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SENSOR_SHAKE:
				unlockScreen();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AppConnect.getInstance(this);
		// 预加载自定义广告内容（仅在使用了自定义广告、抽屉广告或迷你广告的情况，才需要添加）
		AppConnect.getInstance(this).initAdInfo();
		// 预加载插屏广告内容（仅在使用到插屏广告的情况，才需要添加）
		AppConnect.getInstance(this).initPopAd(this);
		// 预加载功能广告内容（仅在使用到功能广告的情况，才需要添加）
    	AppConnect.getInstance(this).initFunAd(this);
		
		// 迷你广告
		LinearLayout miniLayout = (LinearLayout) findViewById(R.id.miniAdLinearLayout);
		AppConnect.getInstance(this).showMiniAd(this, miniLayout, 10);// 10秒刷新一次

		btnEnable = (Button) findViewById(R.id.btnEnable);
		btnDisable = (Button) findViewById(R.id.btnDisable);

		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		checkBtnStatus();

		btnEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
				sensorManager.registerListener(mSensorEventListener,
						sensorManager
								.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						sensorManager.SENSOR_DELAY_NORMAL);

				bEnabled = true;
				checkBtnStatus();
			}
		});

		btnDisable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sensorManager != null) {
					sensorManager.unregisterListener(mSensorEventListener);
					sensorManager = null;
				}

				bEnabled = false;
				checkBtnStatus();
			}
		});
		
		// 互动广告调用方式
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.AdLinearLayout);
		AppConnect.getInstance(this).showBannerAd(this, layout);
		
		// 抽屉式应用墙
		// 1,将drawable-hdpi文件夹中的图片全部拷贝到新工程的drawable-hdpi文件夹中
		// 2,将layout文件夹中的detail.xml和slidewall.xml两个文件，拷贝到新工程的layout文件夹中
		// 获取抽屉样式的自定义广告
    	slidingDrawerView = SlideWall.getInstance().getView(this);  	
    	if(slidingDrawerView != null){
    		addContentView(slidingDrawerView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	}
	}
	
	@Override
	protected void onDestroy() {
		AppConnect.getInstance(this).close();
		super.onDestroy();
	}

	private SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values = event.values;
			float x = values[0];
			float y = values[1];
			float z = values[2];

			int medumValue = 19;
			if (x > medumValue || x < -medumValue || y > medumValue
					|| y < -medumValue || z > medumValue || z < -medumValue) {
				Message msg = new Message();
				msg.what = SENSOR_SHAKE;
				handler.sendMessage(msg);
				Log.i(TAG, "" + msg);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	private void checkBtnStatus() {
		btnEnable.setEnabled(!bEnabled);
		btnDisable.setEnabled(bEnabled);
	}

	private void unlockScreen() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		wl.acquire();
		wl.release();

		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock kl = km.newKeyguardLock("unLock");
		kl.disableKeyguard();
	}
}
