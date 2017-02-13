package com.andybao.mediatestrunner.framework.utils;

import android.util.Log;

import com.andybao.mediatestrunner.framework.testcase.ITestCase;

import java.io.File;

/**
 * Created by andybao on 2017-01-30.
 */

public class TLog {

    public static final String TEST_CASE_TAG = "TestCase";
    public static final String TEST_RUNNER_TAG = "TestRunner";
    public static final String ROOT_PATH = "testrunner" + File.separator;
    public static final String LOG_FOLDER = ROOT_PATH + "log" + File.separator;
    public static final String TEMP_FOLDER = ROOT_PATH + "temp" + File.separator;
    public static final String PLAYLIST_FOLDER = ROOT_PATH + "playlist" + File.separator;

    /**
     * Print debug info.
     * @param comments
     */
    public static void debug(String comments){
        Log.d(TEST_RUNNER_TAG, comments);
    }

    /**
     * Print debug info.
     * @param tag
     * @param comments
     */
    public static void debug(String tag, String comments){
        Log.d(tag, comments);
    }

    /**
     * Print and write test case log info to ITestCase.TEST_CASE_LOG.
     * @param comments
     */
    public static void test(String comments){
        Log.v(TEST_CASE_TAG, comments);
        FileUtil.writeLog(LOG_FOLDER + ITestCase.TEST_CASE_LOG, comments);

    }

    /**
     * Print test case log info.
     * @param comments
     */
    public static void info(String comments){
        Log.v(TEST_CASE_TAG, comments);

    }

    /**
     * Print and write log to a special log file.
     * @param logFile->Log file name.
     * @param comments
     */
    public static void info(String logFile, String comments){
        Log.v(TEST_CASE_TAG, comments);
        FileUtil.writeLog(LOG_FOLDER + logFile, comments);
    }

    /**
     * Write test log to a special log file.
     * @param logFile
     * @param comments
     */
    public static void write(String logFile, String comments){
        FileUtil.writeLog(LOG_FOLDER + logFile, comments);
    }
}
