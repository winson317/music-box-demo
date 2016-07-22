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

/*总结：MediaPlayer播放不同来源的音频文件有如下几种。
 * 一、播放应用的资源文件
 *   1、调用MediaPlayer的create(Context context, int resid)方法加载指定资源文件。
 *   2、调用MediaPlayer的start()、pause()、stop()等方法控制播放即可。
 * 二、播放应用的原始资源文件
 *   1、调用Context的getAssets()方法获取应用的AssetManager。
 *   2、调用AssetManager对象的openFd(String name)方法打开指定的原生资源，该方法返回一个AssetFileDescriptor对象。
 *   3、调用AssetFileDescriptor的getFileDescriptor()、getStartOffset和getLength()方法来获取音频文件的FileDescriptor、开始位置、长度等。
 *   4、创建MediaPlayer对象，并调用MediaPlayer对象的setDataSource(FileDescriptor fd, long offset, long length)方法来装载音频资源。
 *   5、调用MediaPlayer对象的prepare()方法准备音频。
 *   6、调用MediaPlayer的start()、pause()、stop()等方法控制播放即可。
 *三、播放外部存储器上音频文件
 *   1、创建MediaPlayer对象，并调用MediaPlayer对象的setDataSource(String path)方法装载指定的音频文件。
 *   2、调用MediaPlayer对象的prepare()方法准备音频。
 *   3、调用MediaPlayer的start()、pause()、stop()等方法控制播放即可。
 *四、播放来自网络的音频文件
 *  播放来自网络的音频文件有两种方式：
 *  第一，直接使用MediaPlayer的静态create(Context context, Uri uri)方法；
 *  第二，调用MediaPlayer的setDateSource(Context context, Uri uri)装载指定Uri对应的音频文件。
 *     以第二种方式播放来自网络的音频文件的步骤如下：
 *   1、根据网络上的音频文件所在的位置创建Uri对象。
 *   2、创建MediaPlayer对象，并调用MediaPlayer对象的setDataSource(Context context, Uri uri)方法装载Uri对应的音频文件。
 *   3、调用MediaPlayer对象的prepare()方法准备音频。
 *   4、调用MediaPlayer的start()、pause()、stop()等方法控制播放即可。
 */

public class MusicService extends Service {
	
	MyReceiver serviceReceiver;
	AssetManager assetManager;
	String[] musics = new String[] {"wish.mp3", "dadi.mp3", "suiyue.mp3"};
	MediaPlayer mediaPlayer;
	int status = 0x11; //当前的状态，0x11代表没有播放，0x12代表正在播放，0x13代表暂停
	int current = 0;   //记录当前正在播放的音乐
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		assetManager = getAssets();
		serviceReceiver = new MyReceiver();  //创建BroadcastReceiver
		IntentFilter filter = new IntentFilter(); //创建IntentFilter
		filter.addAction(MusicBox.CONTROL_ACTION);
		registerReceiver(serviceReceiver, filter);
		mediaPlayer = new MediaPlayer();  //创建MediaPlayer
		//为MediaPlayer播放完成事件绑定监听器
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				// TODO Auto-generated method stub
				current++;
				if (current >= 3)
				{
					current = 0;
				}
				
				Intent sendIntent = new Intent(MusicBox.UPDATE_ACTION); //发送广播通知Activity更改文本框
				sendIntent.putExtra("current", current);
				sendBroadcast(sendIntent); //发送广播，将被Activity组件中的BroadcastReceiver接收到
				prepareAndPlay(musics[current]); //准备并播放音乐
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
			//播放/暂停
			case 1:
				//原来处于没有播放状态
				if (status == 0x11)
				{
					prepareAndPlay(musics[current]);  //准备并播放音乐
					status = 0x12;
				}
				else if (status == 0x12)  //原来处于播放状态
				{
					mediaPlayer.pause(); //暂停
					status = 0x13; //改变为暂停状态
				}
				else if (status == 0x13)  //原来处于暂停状态
				{
					mediaPlayer.start(); //播放
					status = 0x12;  //改变状态
				}
				break;

		    //停止声音
			case 2:
				//如果原来正在播放或暂停
				if (status == 0x12 || status == 0x13)
				{
					mediaPlayer.stop();  //停止播放
					status = 0x11; 
				}
			}
			
			Intent sendIntent = new Intent(MusicBox.UPDATE_ACTION); //发送广播通知Activity更改图标、文本框
			sendIntent.putExtra("update", status);
			sendIntent.putExtra("current", current);
			sendBroadcast(sendIntent); //发送广播，将被Activity组件中的BroadcastReceiver接收到
		}
	}
	
	private void prepareAndPlay(String music)
	{
		try 
		{
			AssetFileDescriptor assetFileDescriptor = assetManager.openFd(music); //打开指定音乐文件
			mediaPlayer.reset();
			mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), 
					assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());//使用MediaPlayer加载指定的声音文件
			mediaPlayer.prepare(); //准备声音
			mediaPlayer.start();  //播放
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
