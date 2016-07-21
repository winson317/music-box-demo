package com.example.musicbox;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

public class MusicService extends Service {
	
	MyReceiver serviceReceiver;
	AssetManager assetManager;
	String[] musics = new String[] {"wish.mp3", "dadi.mp3", "suiyue.mp3"};
	MediaPlayer mediaPlayer;
	int status = 0x11; //��ǰ��״̬��0x11����û�в��ţ�0x12�������ڲ��ţ�0x13������ͣ
	int current = 0;   //��¼��ǰ���ڲ��ŵ�����
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		assetManager = getAssets();
		serviceReceiver = new MyReceiver();  //����BroadcastReceiver
		IntentFilter filter = new IntentFilter(); //����IntentFilter
		filter.addAction(MusicBox.CONTROL_ACTION);
		registerReceiver(serviceReceiver, filter);
		mediaPlayer = new MediaPlayer();  //����MediaPlayer
		//ΪMediaPlayer��������¼��󶨼�����
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				// TODO Auto-generated method stub
				current++;
				if (current >= 3)
				{
					current = 0;
				}
				
				Intent sendIntent = new Intent(MusicBox.UPDATE_ACTION); //���͹㲥֪ͨActivity�����ı���
				sendIntent.putExtra("current", current);
				sendBroadcast(sendIntent); //���͹㲥������Activity����е�BroadcastReceiver���յ�
				prepareAndPlay(musics[current]); //׼������������
			}
		});
		super.onCreate();
	}
	
	public class MyReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int control = intent.getIntExtra("control", -1);
			switch (control) 
			{
			//����/��ͣ
			case 1:
				//ԭ������û�в���״̬
				if (status == 0x11)
				{
					prepareAndPlay(musics[current]);  //׼������������
					status = 0x12;
				}
				else if (status == 0x12)  //ԭ�����ڲ���״̬
				{
					mediaPlayer.pause(); //��ͣ
					status = 0x13; //�ı�Ϊ��ͣ״̬
				}
				else if (status == 0x13)  //ԭ��������ͣ״̬
				{
					mediaPlayer.start(); //����
					status = 0x12;  //�ı�״̬
				}
				break;

		    //ֹͣ����
			case 2:
				//���ԭ�����ڲ��Ż���ͣ
				if (status == 0x12 || status == 0x13)
				{
					mediaPlayer.stop();  //ֹͣ����
					status = 0x11; 
				}
			}
			
			Intent sendIntent = new Intent(MusicBox.UPDATE_ACTION); //���͹㲥֪ͨActivity����ͼ�ꡢ�ı���
			sendIntent.putExtra("update", status);
			sendIntent.putExtra("current", current);
			sendBroadcast(sendIntent); //���͹㲥������Activity����е�BroadcastReceiver���յ�
		}
	}
	
	private void prepareAndPlay(String music)
	{
		try 
		{
			AssetFileDescriptor assetFileDescriptor = assetManager.openFd(music); //��ָ�������ļ�
			mediaPlayer.reset();
			mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), 
					assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());//ʹ��MediaPlayer����ָ���������ļ�
			mediaPlayer.prepare(); //׼������
			mediaPlayer.start();  //����
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}