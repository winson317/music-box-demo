package com.example.musicbox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/*
 * 下面程序的音乐将会由后台运行的Service组件负责播放，当后台的播放状态发生改变时，程序将会通过发送广播通知前台Activity更新界面；
 * 当用户单击前台Activity的界面按钮时，系统将通过发送广播通知后台Service来改变播放状态。
 */

public class MusicBox extends Activity implements OnClickListener{
	
	TextView title, author; //获取界面中显示歌曲标题、作者文本框
	ImageButton play, stop;  //播放/暂停、停止按钮
	ActivityReceiver activityReceiver;
	public static final String CONTROL_ACTION = "com.winson.action.CONTROL_ACTION";
	public static final String UPDATE_ACTION = "com.winson.action.UPDATE_ACTION";
	int status = 0x11; //定义音乐的播放状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
	String[] titleStrs = new String[] {"心愿", "大地", "光辉岁月"};
	String[] authorStrs = new String[] {"未知艺术家", "赵传", "beyond"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        play = (ImageButton)findViewById(R.id.play);
        stop = (ImageButton)findViewById(R.id.stop);
        title = (TextView)findViewById(R.id.title);
        author = (TextView)findViewById(R.id.author);
        
        //为两个按钮的单击事件添加监听器
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        
        activityReceiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter(); //创建IntentFilter
        filter.addAction(UPDATE_ACTION);  //指定BroadcastReceiver监听的Action
        registerReceiver(activityReceiver, filter); //注册BroadcastReceiver
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);  //启动后台Service
    }
    
    //自定义的BroadcastReceiver，负责监听从Service传回来的广播
    public class ActivityReceiver extends BroadcastReceiver
    {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int update = intent.getIntExtra("update", -1); //获取Intent中的update消息，update代表播放状态
			int current = intent.getIntExtra("current", -1); //获取Intent中的current消息，current代表当前正在播放的歌曲
			
			if (current >= 0)
			{
				title.setText(titleStrs[current]);
				author.setText(authorStrs[current]);
			}
			switch (update) 
			{
			case 0x11:
				play.setImageResource(R.drawable.play);
				status = 0x11;
				break;

			//控制系统进入播放状态
			case 0x12:
				play.setImageResource(R.drawable.pause); //播放状态下设置使用暂停图标
				status = 0x12; //设置当前状态
				break;
				
			case 0x13:
				//暂停状态下设置使用播放图标
				play.setImageResource(R.drawable.play);
				status = 0x13;  //设置当前状态
				break;
			}
		}
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent("com.winson.action.CONTROL_ACTION");  //创建Intent
		switch (v.getId()) 
		{
		//按下播放暂停/按钮
		case R.id.play:
			intent.putExtra("control", 1);
			break;
		//按下停止按钮
		case R.id.stop:
			intent.putExtra("control", 2);
			break;
		}
		sendBroadcast(intent);  //发送广播，将被组件中的BroadcastReceiver接收到
	}
}
