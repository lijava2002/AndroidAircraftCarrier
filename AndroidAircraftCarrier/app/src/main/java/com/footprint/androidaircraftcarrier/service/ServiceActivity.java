package com.footprint.androidaircraftcarrier.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.footprint.androidaircraftcarrier.IMyAidlInterface;
import com.footprint.androidaircraftcarrier.ITaskCallback;
import com.footprint.androidaircraftcarrier.R;

import java.io.IOException;

/**
 * Created by liquanmin on 15/4/15.
 * Service学习三大点：1)Bind/Start Service;  2)Messenger;  3)AIDL
 */
public class ServiceActivity extends Activity {
    protected Button bindButton, msgButton;
    protected TextView serviceText;

    LocalService bindService;
    boolean bindBound = false;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        bindButton = (Button) findViewById(R.id.bindButton);
        bindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bindBound) {
                    int num = bindService.getRandomNumber();
                    Toast.makeText(ServiceActivity.this, "number: " + num, Toast.LENGTH_SHORT).show();
                }
            }
        });

        msgButton = (Button) findViewById(R.id.messagerButton);
        msgButton.setText("第二页面");
        msgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServiceActivity.this, ServiceNextActivity.class);
                startActivity(intent);
            }
        });

        serviceText = (TextView) findViewById(R.id.serviceText);

        startService(new Intent(ServiceActivity.this, LocalService.class));

        /**
         * Bind操作是异步的，并不直接返回，所以需要Connection对象，这其中是两个回调
         * Only activities, services, and content providers can bind to a service,
         * you cannot bind to a service from a broadcast receiver.
         * */

        // Bind to LocalService
        Intent intent = new Intent(this, RemoteAIDLService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        /**
         * 测试播放音乐
         * 【验证】切到下一个页面以及页面退到后台，音乐都会继续播放。界面杀死之后，不论是否在哦你Destroy中停止
         * 播放文件，音乐都会停止。
         * 建议使用Service播放音乐，Context居于后台，从而音乐的播放停止由程序控制
         * */
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("http://yinyueshiting.baidu.com/data2/music/134370203/14945107241200128.mp3?xcode=48b564f00c231cb037b30130eee42a5a9747f83dd337b2e9");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 通过异步的方式装载媒体资源
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 装载完毕 开始播放流媒体
                mediaPlayer.start();
                Toast.makeText(ServiceActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private IMyAidlInterface iMyAidlInterface;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                iMyAidlInterface.registerCallback(mCallback);
                Toast.makeText(ServiceActivity.this, iMyAidlInterface.getMsg(), Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("FP", "Service has unexpectedly disconnected");
            iMyAidlInterface = null;
        }
    };

    private ITaskCallback mCallback = new ITaskCallback.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void actionCallback(int actionId) throws RemoteException {
            Log.e("FP", "I am callbacked!");
        }
    };

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection bindConnection = new ServiceConnection() {

        /**
         * The Android system calls this when the connection to the service is unexpectedly lost,
         * such as when the service has crashed or has been killed.
         * This is not called when the client unbinds.
         * 【实验】全部unbind之后，Service的onDestroy()方法被调用，但是该方法没有被调用。
         * */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("FP", "Service Disconnected");
            bindBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
            bindService = binder.getService();
            bindBound = true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind from the service
        /**
         * When your client is destroyed, it will unbind from the service
         * 但是会报错：has leaked ServiceConnection
         * */
        if (bindBound) {
            unbindService(bindConnection);
            bindBound = false;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
