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
 * �����������ֽ����ɺ�̨���е�Service������𲥷ţ�����̨�Ĳ���״̬�����ı�ʱ�����򽫻�ͨ�����͹㲥֪ͨǰ̨Activity���½��棻
 * ���û�����ǰ̨Activity�Ľ��水ťʱ��ϵͳ��ͨ�����͹㲥֪ͨ��̨Service���ı䲥��״̬��
 */

public class MusicBox extends Activity implements OnClickListener{
	
	TextView title, author; //��ȡ��������ʾ�������⡢�����ı���
	ImageButton play, stop;  //����/��ͣ��ֹͣ��ť
	ActivityReceiver activityReceiver;
	public static final String CONTROL_ACTION = "com.winson.action.CONTROL_ACTION";
	public static final String UPDATE_ACTION = "com.winson.action.UPDATE_ACTION";
	int status = 0x11; //�������ֵĲ���״̬��0x11����û�в��ţ�0x12�������ڲ��ţ�0x13������ͣ
	String[] titleStrs = new String[] {"��Ը", "���", "�������"};
	String[] authorStrs = new String[] {"δ֪������", "�Դ�", "beyond"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        play = (ImageButton)findViewById(R.id.play);
        stop = (ImageButton)findViewById(R.id.stop);
        title = (TextView)findViewById(R.id.title);
        author = (TextView)findViewById(R.id.author);
        
        //Ϊ������ť�ĵ����¼���Ӽ�����
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        
        activityReceiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter(); //����IntentFilter
        filter.addAction(UPDATE_ACTION);  //ָ��BroadcastReceiver������Action
        registerReceiver(activityReceiver, filter); //ע��BroadcastReceiver
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);  //������̨Service
    }
    
    //�Զ����BroadcastReceiver�����������Service�������Ĺ㲥
    public class ActivityReceiver extends BroadcastReceiver
    {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int update = intent.getIntExtra("update", -1); //��ȡIntent�е�update��Ϣ��update������״̬
			int current = intent.getIntExtra("current", -1); //��ȡIntent�е�current��Ϣ��current����ǰ���ڲ��ŵĸ���
			
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

			//����ϵͳ���벥��״̬
			case 0x12:
				play.setImageResource(R.drawable.pause); //����״̬������ʹ����ͣͼ��
				status = 0x12; //���õ�ǰ״̬
				break;
				
			case 0x13:
				//��ͣ״̬������ʹ�ò���ͼ��
				play.setImageResource(R.drawable.play);
				status = 0x13;  //���õ�ǰ״̬
				break;
			}
		}
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent("com.winson.action.CONTROL_ACTION");  //����Intent
		switch (v.getId()) 
		{
		//���²�����ͣ/��ť
		case R.id.play:
			intent.putExtra("control", 1);
			break;
		//����ֹͣ��ť
		case R.id.stop:
			intent.putExtra("control", 2);
			break;
		}
		sendBroadcast(intent);  //���͹㲥����������е�BroadcastReceiver���յ�
	}
}
