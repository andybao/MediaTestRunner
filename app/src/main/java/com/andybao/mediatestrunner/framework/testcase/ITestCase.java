package com.andybao.mediatestrunner.framework.testcase;

import android.content.Context;

/**
 * Created by andybao on 2017-01-30.
 */

public interface ITestCase {

    String SET_DATA_SOURCE_LOG_FLAG = "Play file: ";
    String TEST_CASE_LOG = "test_case_log.txt";
    String SIMPLE_LOG = "simple_log.txt";
    String MEMORY_LOG = "memory_log.txt";
    String CPU_LOG = "cpu_usage_log.txt";

    void setContext(Context cxt);
    void setDataSource(String data);
    void setCurrentLoopCount(int loopCount);
    void setTotalLoopCount(int loopCount);

    void setUp() throws Exception;
    void runTestCase() throws Exception;
    void tearDown() throws Exception;
}
