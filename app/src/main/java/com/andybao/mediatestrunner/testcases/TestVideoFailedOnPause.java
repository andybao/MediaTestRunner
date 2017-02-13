package com.andybao.mediatestrunner.testcases;

import com.andybao.mediatestrunner.framework.testcase.SystemMediaPlayerTestCase;
import com.andybao.mediatestrunner.framework.ui.PlayerActivity;
import com.andybao.mediatestrunner.framework.utils.TLog;

/**
 * Created by andybao on 2017-02-06.
 */

public class TestVideoFailedOnPause extends SystemMediaPlayerTestCase {

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
    public void runTestCase() throws Exception {
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
        //videoView.pause();
        sleep(3000);
        assertFalse(videoView.isPlaying(), "Video still playing after pause.");
    }
}
