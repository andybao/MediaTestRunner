package com.andybao.mediatestrunner.framework.testcase;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.andybao.mediatestrunner.framework.service.TestRunnerService;
import com.andybao.mediatestrunner.framework.ui.PlayerActivity;
import com.andybao.mediatestrunner.framework.utils.SystemVideoView;
import com.andybao.mediatestrunner.framework.utils.TLog;

/**
 * Created by andybao on 2017-02-02.
 */

public abstract class SystemMediaPlayerTestCase implements ITestCase {

    private static final String TAG = "SystemMediaPlayerTestCase";

    protected Context cxt;
    protected String data;
    protected SystemVideoView videoView;
    protected MediaPlayer mp;
    protected int currentLoopNumber = 0;
    protected int totalLoopNumber = 0;

    protected boolean isSeekComplete = false;
    protected boolean isOnComplete = false;
    protected boolean isPrepared = false;
    protected long onSeekCompletePosition = -1;
    private boolean isStartTest = false;

    /**
     * Start Video player activity.
     */
    protected void startPlayActivity(){
        Intent intent = new Intent(cxt, PlayerActivity.class);
        intent.putExtra(PlayerActivity.VIDEO_PATH, data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cxt.startActivity(intent);
    }

    /**
     *
     * @param time (milliseconds).
     * @throws Exception
     */
    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setVideoView(SystemVideoView vv){
        videoView = vv;
        mp = videoView.getMediaPlayer();
    }

    /**
     * Assert the result is false, if true, it will throw an exception and current running TestCase will be interrupted.
     * @param result
     * @param comments -> The message will be showed in log file if assert failed.
     * @throws Exception
     */
    protected void assertFalse(boolean result, String comments) throws Exception{
        if(result){
            throw new Exception(comments);
        }
    }

    /**
     * Close Video player activity.
     */
    protected void finishPlayerActivity(){
        Intent intent = new Intent(PlayerActivity.PLAYER_VIDEO_RECEIVE_ACTION);
        intent.putExtra(PlayerActivity.PLAYER_RECEIVER_KEY, TestRunnerService.FINISH_PLAYER_ACTIVITY);
        cxt.sendBroadcast(intent);
    }

    /**
     * Initialize MediaPlayer listener.
     */
    protected void initListener(){
        /**Only need add prepare and error listener for Audio.
         * Video prepare listener has been added in PlayerActivity.
         * Video error listen has been added in HelixVideoView.
         */
        if(videoView != null){

            mp.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mp.setOnCompletionListener(mOnCompletionListener);

        }else{

            mp.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mp.setOnCompletionListener(mOnCompletionListener);
            mp.setOnPreparedListener(mOnPreparedListener);
            mp.setOnErrorListener(mOnErrorListener);

        }
    }

    protected MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener(){
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            isSeekComplete = true;
            onSeekCompletePosition = mp.getCurrentPosition();
            TLog.debug("SeekComplete.");
        }
    };

    protected MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener(){
        @Override
        public void onCompletion(MediaPlayer mp) {
            isOnComplete = true;
            TLog.debug("OnCompletion done.");
        }
    };

    protected MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener(){
        @Override
        public void onPrepared(MediaPlayer mp) {
            isPrepared = true;
            TLog.debug("Prepared done. Start playing.");
            mp.start();
        }
    };

    protected MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            String errorCode = Integer.toHexString(extra).toUpperCase();
            TLog.test("Error was found on MediaPlayer: " + what + ", errorCode:" + errorCode);

            //Send MediaPlayer error code to TestRunnerService by Broadcast.
            Intent intent = new Intent(TestRunnerService.TEST_RUNNER_BROADCAST_ACTION);
            intent.putExtra(TestRunnerService.TEST_RUNNER_BROADCAST_KEY_NAME, TestRunnerService.TEST_ERROR);
            intent.putExtra(TestRunnerService.MEDIA_PLAYER_ERROR_CODE, errorCode);
            cxt.sendBroadcast(intent);

            return true;
        }
    };

    /**
     * Assert mediaplayer is playing.
     * @param timeout (seconds)
     * @throws Exception
     */
    protected void assertIsPlaying(int timeout) throws Exception{
        int times = 10 * timeout;
        int pos = mp.getCurrentPosition();

        for(int i=0; i<times; i++){
            if(mp.isPlaying() && (mp.getCurrentPosition() > pos)){
                return;
            }else{
                pos = mp.getCurrentPosition();
                sleep(100);
            }
        }

        String msg = "";
        if(isOnComplete){
            msg = "Playing has been completed. ";
        }
        throw new Exception("Error: MediaPlayer was not playing. " + msg + "getCurrentPosition:" + mp.getCurrentPosition() + ", getDuration:" + mp.getDuration());
    }

    /**
     * wait for seek complete.
     * @param timeout (seconds)
     * @throws Exception if seek not complete in timeout, it will throw an exception.
     */
    protected void waitForSeekComplete(int timeout) throws Exception{
        sleep(100);
        int times = 10 * timeout;

        for(int i=0; i<times; i++){
            if(isSeekComplete){
                break;
            }else{
                sleep(100);
            }
        }
        if(!isSeekComplete){
            throw new Exception("Seek failed. isSeekComplete = false.");
        }
        assertIsPlaying(3);
    }

    /**
     * Assert two number is equal,
     * if not, it will throw an exception and current running TestCase will be interrupted.
     * @param first -> first compared number;
     * @param second -> second compared number;
     * @param tolerance -> the tolerance for the compare result.
     * @param comments -> The message will be showed in log file if assert failed.
     * @throws Exception
     */
    protected void assertEquals(int first, int second, int tolerance, String comments) throws Exception{
        int result = 0;
        result = Math.abs(first - second);
        if(result > tolerance){
            throw new Exception(comments);
        }
    }

    /**
     * wait for playing complete.
     * @param timeout (seconds)
     * @throws Exception if playing not complete in timeout, it will throw an exception.
     */
    protected void waitForPlayingComplete(int timeout) throws Exception{
        sleep(100);
        int times = 10 * timeout;

        for(int i=0; i<times; i++){
            if(isOnComplete){
                break;
            }else{
                sleep(100);
            }
        }
        if(!isOnComplete){
            throw new Exception("Cannot play to the end. isOnComplete = false. " + " getCurrentPosition(): " + mp.getCurrentPosition() + ", getDuration():" + mp.getDuration());
        }
        assertIsNotPlaying(3);
    }

    /**
     * Assert mediaplayer is not playing.
     * @param timeout (seconds)
     * @throws Exception
     */
    protected void assertIsNotPlaying(int timeout) throws Exception{
        sleep(100);
        int pos = mp.getCurrentPosition();
        int times = 10 * timeout;

        for(int i=0; i<times; i++){
            if(!mp.isPlaying() && (mp.getCurrentPosition() == pos)){
                return;
            }else{
                sleep(100);
                pos = mp.getCurrentPosition();
            }
        }
        throw new Exception("Error: MediaPlayer was still playing. getCurrentPosition:" + mp.getCurrentPosition());
    }

    @Override
    public void setContext(Context cxt) {
        this.cxt = cxt;
    }

    @Override
    public void setDataSource(String data) {
        this.data = data;
    }

    @Override
    public void setCurrentLoopCount(int loopCount) {
        this.currentLoopNumber = loopCount;
    }

    @Override
    public void setTotalLoopCount(int loopCount) {
        this.totalLoopNumber = loopCount;
    }

    @Override
    public void setUp() throws Exception {
        isStartTest = true;
    }

    @Override
    public abstract void runTestCase() throws Exception;

    @Override
    public void tearDown() throws Exception {
        isStartTest = false;
    }
}
