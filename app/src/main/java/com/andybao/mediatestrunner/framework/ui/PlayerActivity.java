package com.andybao.mediatestrunner.framework.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;
import android.widget.VideoView;

import com.andybao.mediatestrunner.R;
import com.andybao.mediatestrunner.framework.service.TestRunnerService;
import com.andybao.mediatestrunner.framework.testcase.ITestCase;
import com.andybao.mediatestrunner.framework.utils.SystemVideoView;
import com.andybao.mediatestrunner.framework.utils.TLog;
import com.andybao.mediatestrunner.testcases.TestVideoBasicFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import static android.R.attr.process;

/**
 * Created by andybao on 2017-02-02.
 */

public class PlayerActivity extends AppCompatActivity {
    public static final String VIDEO_PATH = "com.andybao.testrunner.TestCase.VideoPath";
    public static final String PLAYER_VIDEO_RECEIVE_ACTION = "com.andybao.testrunner.receive";
    public static final String PLAYER_RECEIVER_KEY = "receive";

    public static SystemVideoView mSysVideoView;
    private TextView mFileNameTextView;

    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TLog.info("PlayerActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);

        getSupportActionBar().hide();

        mSysVideoView = (SystemVideoView) findViewById(R.id.system_video_view);
        mFileNameTextView = (TextView) findViewById(R.id.play_file_name);

        String videoPath = this.getIntent().getExtras().getString(VIDEO_PATH);

        TLog.test(ITestCase.SET_DATA_SOURCE_LOG_FLAG + videoPath);
        mFileNameTextView.setText("PlayContent: " + videoPath);

        initSysVideoView(videoPath);

        mReceiver = new PlayerActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAYER_VIDEO_RECEIVE_ACTION);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void initSysVideoView(String videoPath){
        mSysVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer mp) {
                TLog.debug("Prepared done, start mediaplayer.");
                mSysVideoView.start();
            }
        });

        mSysVideoView.setVideoURI(Uri.parse(videoPath));
    }

    public class PlayerActivityReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(PLAYER_RECEIVER_KEY, -1);
            switch (action){
                case TestRunnerService.FINISH_PLAYER_ACTIVITY:
                    finish();
            }
        }
    }

}
