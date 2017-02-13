package com.andybao.mediatestrunner.framework.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.StatFs;
import android.support.annotation.Nullable;

import com.andybao.mediatestrunner.R;
import com.andybao.mediatestrunner.framework.testcase.ITestCase;
import com.andybao.mediatestrunner.framework.ui.MainActivity;
import com.andybao.mediatestrunner.framework.utils.FileUtil;
import com.andybao.mediatestrunner.framework.utils.SQLiteHelper;
import com.andybao.mediatestrunner.framework.utils.TLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by andybao on 2017-01-30.
 */

public class TestRunnerService extends Service {

    public static final String TEST_RUNNER_TAG = "TestRunner";

    public static final int START_TEST_SERVICE = 1;
    public static final int STOP_TEST_SERVICE = 2;
    public static final int TEST_ERROR = 3;
    public static final int FINISH_PLAYER_ACTIVITY = 4;

    public static final String TEST_RUNNER_BROADCAST_KEY_NAME = "control";
    public static final String MEDIA_PLAYER_ERROR_CODE = "mp_error_code";
    public static final String TEST_RUNNER_BROADCAST_ACTION = "com.andybao.mediatestrunner.control";
    public static final String PLAYLIST_FOLDER = TLog.ROOT_PATH + "playlist" + File.separator;
    public static final String TEST_CASE_NAME_FLAG = "Test";
    public static final String TEST_CASE_PACKAGE = "com.andybao.mediatestrunner.testcases.";
    public static final String ADB_AM_START_EXTRA_KEY = "TestCaseList";
    public static final String TEST_CASE_SPLIT_IN_COMMAND = "#";
    public static final String PLAY_FILE_LOOP_COUNT_FLAG = "LOOP:";
    public static final String CONTINUE_RUN_COMMAND = "continue";
    public static final String PLAYLIST_FILE_FLAG = "_playlist.txt";
    public static final String START_TESTING_FLAG = "Start Testing...";
    public static final String TEST_CASE_PASSED_LOG_FLAG = "---> TestCase Passed!";
    public static final String TEST_CASE_FAILED_LOG_FLAG = "---> TestCase Failed: ";
    public static final String TEST_CASE_COMPLETE_LOG_FLAG = "Test Complete!";
    public static final String START_RUN_TEST_CASE_LOG_FLAG = "[Run Test Case]: ";

    private BroadcastReceiver receiver;
    private PowerManager.WakeLock wl;
    private ArrayList<String> playList = new ArrayList<String>();
    private HashMap<String,Integer> dataMap = new HashMap<String,Integer>();
    private SQLiteHelper mSQLHelper;
    private Thread runThread;
    private int errorType = 0;
    private String errorCode = "";
    private int classIndex = 0;
    private boolean isStop = false;
    private int passedLoop = 0;
    private int failedLoop = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        TLog.debug("TestRunnerService onCreate()");
        receiver = new TestReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TEST_RUNNER_BROADCAST_ACTION);
        registerReceiver(receiver, filter);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TEST_RUNNER_TAG);
        wl.acquire();

        FileUtil.createDefaultFolderOnDevice();
        checkDeviceEnvironment();

        mSQLHelper = new SQLiteHelper(this);
        if(mSQLHelper.query(1) != -1){
            TLog.debug("Test Service Reset!!!");
            TLog.test("---> TestCase Failed: ANR detected during the testing.");
        }

        //Start testing.
        Intent intent = new Intent(TEST_RUNNER_BROADCAST_ACTION);
        intent.putExtra(TEST_RUNNER_BROADCAST_KEY_NAME, START_TEST_SERVICE);
        sendBroadcast(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        /**
         * Start testing by adb shell am command.
         * adb shell am startservice -n com.andybao.mediatestrunner/.framework.service.TestRunnerService -e $ADB_AM_START_EXTRA_KEY testCaseList
         * if there are several test cases, please use '#' to separate them;
         * if you want to continue run the testing, please use 'continue' to replace of testCaseList.
         * */
        String testCaseList = intent.getStringExtra(ADB_AM_START_EXTRA_KEY);
        if(testCaseList != null){
            boolean isStart = true;
            TLog.debug("TestCaseList: " + testCaseList);
            testCaseList = testCaseList.trim();
            if(!testCaseList.equals(CONTINUE_RUN_COMMAND)){
                isStart = InitializeTestByCommand(testCaseList);
            }
            if(isStart){
                Intent i = new Intent(TEST_RUNNER_BROADCAST_ACTION);
                intent.putExtra(TEST_RUNNER_BROADCAST_KEY_NAME, START_TEST_SERVICE);
                sendBroadcast(i);
            }else{
                TLog.info("Invalid input test case nam list!");
            }
        }
        /** If this Service is killed by system, it will start automatically again. */
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        TLog.debug("TestRunnerService onDestroy()");
        unregisterReceiver(receiver);
        //notificationManager.cancel(0);
        wl.release();
        mSQLHelper.delete();
        super.onDestroy();
    }

    /**
     * TestRunner Broadcast receiver. Receive and execute all test commands.
     */
    public class TestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int action = intent.getIntExtra(TEST_RUNNER_BROADCAST_KEY_NAME, -1);
            TLog.debug("TestRunner BroadcastReceiver get Action: " + action);

            switch (action) {
                case START_TEST_SERVICE:
                    runThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startTest();
                        }
                    });
                    runThread.start();
                    break;
                case STOP_TEST_SERVICE:
                    isStop = true;
                    break;
                case TEST_ERROR:
                    errorType = 1;
                    errorCode = intent.getStringExtra(MEDIA_PLAYER_ERROR_CODE);
                    break;
            }
        }
    }

    private void startTest(){
        TLog.debug("startTest()");
        String testCaseName = "";
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        TLog.test("[" + date + "] " + START_TESTING_FLAG);

        //Get all test info from SQL.
        ArrayList<String> testCaseArray = mSQLHelper.querySelectedTestCaseClass();
        classIndex = mSQLHelper.query(0);

        int testCaseNum = testCaseArray.size();
        for(; classIndex < testCaseNum; classIndex++){
            testCaseName = testCaseArray.get(classIndex);
            TLog.test(START_RUN_TEST_CASE_LOG_FLAG + testCaseName);
            setPlayList(testCaseName);
            mSQLHelper.update(0, -1, 1, null);	/** Must set the run status in SQL to default.*/
            testDriver(testCaseName);
            mSQLHelper.removeTestCase(testCaseName); /**Should remove executed test case from SQL. */
        }

        date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        TLog.test("[" + date + "] " + TEST_CASE_COMPLETE_LOG_FLAG);
        stopSelf();
    }

    /**
     * Set playlist array.
     * If there isn't a matched playlist in PLAYLIST_FOLDER, the default playlist will be used.
     * @param className -> Test case class name.
     */
    private void setPlayList(String className){
        String linkName = "";
        int loopCountValue = 1;
        ArrayList<String> allList = new ArrayList<String>();
        playList.clear();
        dataMap.clear();

        ArrayList<String> playlistFile = FileUtil.getAllPlaylistFileList(PLAYLIST_FOLDER);

        for(String fileName:playlistFile){
            TLog.debug("fileName: " + fileName);
            if(fileName.equals(className + PLAYLIST_FILE_FLAG)){
                allList = FileUtil.getPlayList(fileName);
                TLog.debug("Playlist: " + fileName + ", size: " + allList.size());
            }
        }

        if(allList.size() == 0){
            TLog.debug("Default playlist will be used.");
            if(className.contains("Audio")){
                allList = FileUtil.getPlayList("audio_default_playlist.txt");
            }else if(className.contains("Video")){
                allList = FileUtil.getPlayList("video_default_playlist.txt");
            }else{
                TLog.debug("Invalid test case class name!!!");
                return;
            }
            TLog.debug("playlist size is: " + allList.size());
        }

        for(String list:allList){
            if(list.contains(PLAY_FILE_LOOP_COUNT_FLAG)){
                linkName = list.substring(0, list.indexOf(PLAY_FILE_LOOP_COUNT_FLAG));
                linkName = linkName.trim();
                loopCountValue = Integer.parseInt(list.substring(list.indexOf(PLAY_FILE_LOOP_COUNT_FLAG) + PLAY_FILE_LOOP_COUNT_FLAG.length()).trim());
                playList.add(linkName);
                dataMap.put(linkName, loopCountValue);
            }else{
                playList.add(list.trim());
                dataMap.put(list.trim(), 1);
            }
        }
    }

    private void testDriver(String testCaseName){
        TLog.debug("testDriver(), " + testCaseName);

        int dataIndex = -1;
        int loopCount = 1;
        int currentCount = 1;

        String nText = null;
        String nTitle = TestRunnerService.this.getResources().getString(R.string.app_name);

        Intent intent = new Intent(TestRunnerService.this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(TestRunnerService.this, 0, intent, 0);

        dataIndex = mSQLHelper.query(1) + 1;
        currentCount = mSQLHelper.query(2);
        TLog.debug("testDriver().dataIndex: " + dataIndex + ", testDriver().currentCount:" + currentCount);

        for(; dataIndex < playList.size(); dataIndex++){
            String data = playList.get(dataIndex);
            loopCount = dataMap.get(data);
            passedLoop = 0;
            failedLoop = 0;
            TLog.test("[Index: " + (dataIndex+1) + "] Test File: " + data);
            for(int i=currentCount; i<=loopCount; i++){
                if(isStop){
                    return;	//stop testing.
                }

                if(loopCount > 1){
                    TLog.info("LoopCount: " + i);
                    nText = "Running: " + (dataIndex+1) + "/" + playList.size() + ", Loop:" + i + "/" + loopCount;
                }else{
                    nText = "Running: " + (dataIndex+1) + "/" + playList.size();
                }

                mSQLHelper.update(classIndex, dataIndex, i, null);
                runTest(testCaseName, data, i, loopCount);
                sleep(3000);
            }
        }
    }

    private void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runTest(String testCaseName, String data, int currentLoop, int loopCount){
        TLog.debug("runTest(), " + testCaseName + ", " + data);
        errorType = 0;
        Class<?> testClass = null;
        ITestCase testCase = null;
        try {
            testClass = Class.forName(TEST_CASE_PACKAGE + testCaseName);
        } catch (ClassNotFoundException e){
            TLog.debug("ClassNotFoundException: " + e.getMessage());
            return;
        }
        try {
            if(!data.startsWith("http")){
                File localMediaFile = new File(data);
                if(!localMediaFile.exists()){
                    throw new Exception("File not exist! " + data);
                }
            }
            testCase = (ITestCase)testClass.newInstance();
            testCase.setContext(TestRunnerService.this);
            testCase.setDataSource(data);
            testCase.setCurrentLoopCount(currentLoop);
            testCase.setTotalLoopCount(loopCount);
            testCase.setUp();
            sleep(1000);
            testCase.runTestCase();
            sleep(1000);
            testCase.tearDown();
            if(errorType == 0){
                if(loopCount == 1){
                    TLog.test(TEST_CASE_PASSED_LOG_FLAG);
                }else{
                    passedLoop++;
                    TLog.test("Loop " + currentLoop + " Passed!");
                }
            }else if(errorType == 2){
                throw new Exception("Testing Timeout!");
            }else{
                TLog.debug("errorType: " + errorType);
            }
        } catch(Exception e){
            String errorMsg = e.getMessage();
            if(errorType == 1){
                String errorName = "";
                errorMsg = errorMsg + " ErrorCode: " + errorCode + errorName;
            }
            sleep(2000);
            if(loopCount == 1){
                TLog.test(TEST_CASE_FAILED_LOG_FLAG + errorMsg);
            }else{
                failedLoop++;
                TLog.test("Loop " + currentLoop + " Failed:" + errorMsg);
            }
            e.printStackTrace();
            if(testCase != null){
                try {
                    testCase.tearDown();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        if(currentLoop == loopCount && loopCount > 1){
            if(failedLoop > 0){
                TLog.test(TEST_CASE_FAILED_LOG_FLAG + " Failed LoopCount: " + failedLoop + ", Passed LoopCount: " + passedLoop + ", Total Executed LoopCount: " + (passedLoop + failedLoop));
            }else{
                TLog.test(TEST_CASE_PASSED_LOG_FLAG + " Total Executed LoopCount: " + passedLoop);
            }
        }
    }

    private void checkDeviceEnvironment(){
        TLog.test("Test environment as below:");
        //1. Device info
        TLog.test("Device Info: " + "CPU: " + Build.CPU_ABI + ", " + Build.CPU_ABI2 + ", Hardware: " + Build.HARDWARE + ", Product Name:" + Build.MODEL
                + ", Android Version:" + Build.VERSION.RELEASE + ", SDK Version:" + Build.VERSION.SDK_INT);

        //2. check wifi connection.
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifi.getConnectionInfo();
        if(!wifi.isWifiEnabled()){
            wifi.setWifiEnabled(true);
            TLog.test("Open Wifi...");
        }
        TLog.test("WIFI: Enable->" + wifi.isWifiEnabled() + ", AP->" + wInfo.getSSID() + ", Info->" + wInfo.toString());

        //3. check sdcard info.
        StatFs stat = new StatFs(FileUtil.getEnvPath());
        long availableBlocks = stat.getAvailableBlocks();
        long blockSize = stat.getBlockSize();
        long freeSize = (availableBlocks * blockSize)/1024/1024;
        TLog.test("SD card free size: " + freeSize + "MIB");
    }

    /**
     * Initialize testing which started by adb shell am command.
     * @param caseList
     * @return
     */
    private boolean InitializeTestByCommand(String caseList){
        SQLiteHelper mSQLHelper = new SQLiteHelper(this);
        mSQLHelper.delete();
        ArrayList<String> selectedCaseList = new  ArrayList<String>();

        String[] list = caseList.split(TEST_CASE_SPLIT_IN_COMMAND);
        for(String caseName:list){
            caseName = caseName.trim();
            if(caseName.length() > 3){
                selectedCaseList.add(caseName);
            }
        }
        TLog.debug("Input test case number is: " + selectedCaseList.size());

        if(selectedCaseList.size() == 0){
            return false;
        }
        mSQLHelper.insert(0, -1, 1, selectedCaseList);
        return true;
    }

}
