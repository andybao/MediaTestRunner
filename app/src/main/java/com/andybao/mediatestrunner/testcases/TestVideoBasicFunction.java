package com.andybao.mediatestrunner.testcases;

import android.app.Activity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.andybao.mediatestrunner.framework.testcase.SystemMediaPlayerTestCase;
import com.andybao.mediatestrunner.framework.ui.PlayerActivity;
import com.andybao.mediatestrunner.framework.utils.TLog;

import java.lang.ref.WeakReference;
import java.util.zip.Inflater;

import static android.R.attr.duration;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by andybao on 2017-02-02.
 */

public class TestVideoBasicFunction extends SystemMediaPlayerTestCase {


    @Override
    public void setUp() throws Exception{
        TLog.debug("setUp()");
        startPlayActivity();
        sleep(2000);
        assertFalse(PlayerActivity.mSysVideoView == null, "Initialize failed. videoView is null.");
        setVideoView(PlayerActivity.mSysVideoView);

    }

    @Override
    public void tearDown() throws Exception{
        TLog.debug("tearDown()");
        finishPlayerActivity();
    }

    @Override
    public void runTestCase() throws Exception{
        //1. start.
        TLog.test("1. Start playing.");
        initListener();
        assertIsPlaying(30);
        int duration = videoView.getDuration();

        TLog.info("videoView.getDuration(): " + duration);
        assertFalse(duration == 0, "Error: video duration is 0.");

        //Set seek tolerance.
        int seekTolerance = videoView.getDuration()/100;
        if(seekTolerance < 5000){
            seekTolerance = 5000;
        }else if(seekTolerance > 20000){
            seekTolerance = 20000;
        }

        sleep(3000);

        //2. pause.
        TLog.test("2. Pause.");
        videoView.pause();
        sleep(3000);
        assertFalse(videoView.isPlaying(), "Error: video still playing after pause.");

        //3. start after pausing.
        TLog.test("3. Start after pause.");
        videoView.start();
        assertIsPlaying(30);
        sleep(3000);
        assertIsPlaying(30);

        //4. seek to Forward.
        TLog.test("4. Seek forward.");
        int seekDuration = videoView.getCurrentPosition() + (videoView.getDuration() - videoView.getCurrentPosition())/2;
        isSeekComplete = false;
        videoView.seekTo(seekDuration);
        sleep(500);
        waitForSeekComplete(10);
        assertEquals(seekDuration, videoView.getCurrentPosition(), seekTolerance, "Seek inaccuracy. seekTo: " + seekDuration + ", getCurrentPosition:" + videoView.getCurrentPosition());

        sleep(5000);
        assertIsPlaying(30);

        //5. seek to Backward.
        TLog.test("5. Seek backward.");
        seekDuration = videoView.getCurrentPosition()/2;
        isSeekComplete = false;
        videoView.seekTo(seekDuration);
        sleep(100);
        waitForSeekComplete(10);

        sleep(5000);
        assertIsPlaying(30);

        //6. seek to begining.
        TLog.test("6. Seek to begining.");
        isSeekComplete = false;
        videoView.seekTo(0);
        sleep(500);
        waitForSeekComplete(10);

        sleep(5000);
        assertIsPlaying(30);

        //7. seek to the end (keep last 5 seconds).
        TLog.test("7. Seek to the end. (keep last 5 seconds)");
        seekDuration = videoView.getDuration() - 5000;
        if(seekDuration < 0){
            seekDuration = 3000;
        }
        isSeekComplete = false;
        isOnComplete = false;
        videoView.seekTo(seekDuration);
        sleep(500);
        waitForSeekComplete(10);

        //8. waiting for playing finish.
        sleep(10000);
        waitForPlayingComplete(20);
    }

}