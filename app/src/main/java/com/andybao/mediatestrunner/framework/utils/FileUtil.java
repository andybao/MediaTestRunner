package com.andybao.mediatestrunner.framework.utils;

import android.os.Environment;
import android.util.Log;

import com.andybao.mediatestrunner.framework.testcase.ITestCase;
import com.andybao.mediatestrunner.framework.service.TestRunnerService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by andybao on 2017-01-30.
 */

public class FileUtil {

    private static String ENV_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static void setEnvPath(String path){
        ENV_PATH = path;
    }

    public static String getEnvPath(){
        return ENV_PATH;
    }

    /**
     * Create default folders on device.
     */
    public static synchronized void createDefaultFolderOnDevice(){
        File rootFolder = new File(ENV_PATH + File.separator + TLog.ROOT_PATH);
        File tempFolder = new File(ENV_PATH + File.separator + TLog.TEMP_FOLDER);
        File logFolder = new File(ENV_PATH + File.separator + TLog.LOG_FOLDER);
        File playListFolder = new File(ENV_PATH + File.separator + TLog.PLAYLIST_FOLDER);

        if(!rootFolder.exists()){
            rootFolder.mkdir();
        }

        if(!tempFolder.exists()){
            tempFolder.mkdir();
        }

        if(!logFolder.exists()){
            logFolder.mkdir();
        }

        if(!playListFolder.exists()){
            playListFolder.mkdir();
        }
    }

    /**
     * Write log to log file. Log will saved on LOG_PATH folder.
     * @param fileName
     * @param msg
     */
    public static synchronized void writeLog(String fileName,String msg){

        File log = new File(ENV_PATH + File.separator + fileName);

        BufferedWriter writer = null;
        FileWriter fw = null;
        if(!log.exists()){
            try {
                TLog.debug("Create new log file.");
                log.createNewFile();
            } catch (IOException e) {
                TLog.debug("Write log to sdcard failed. " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        try {
            //write msg to the end.
            fw = new FileWriter(log,true);
            writer = new BufferedWriter(fw);
            writer.write(msg + "\n");
            writer.flush();
            writer.close();
            fw.close();
        }catch (IOException e) {
            TLog.debug("Write log to sdcard failed. " + e.getMessage());
            e.printStackTrace();
        }finally {
            if(fw != null && writer != null)
                try {
                    writer.close();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Get file name list from device sdcard folder.
     */
    public static ArrayList<String> getAllPlaylistFileList(String folderPath){
        ArrayList<String> fileName = new ArrayList<String>();
        File file = new File(ENV_PATH + File.separator + folderPath);

        if(file != null && file.isDirectory()){
            String[] fileListName = file.list();
            for(String name:fileListName){
                if(name.contains(TestRunnerService.PLAYLIST_FILE_FLAG)){
                    fileName.add(name);
                }
            }
        }

        if(fileName.size() == 0){
            TLog.debug("This is no playlist file.");
        }
        return fileName;
    }

    /**
     * Get play list for device.
     * @param fileName
     * @return
     */
    public static ArrayList<String> getPlayList(String fileName){
        File myFile = new File(ENV_PATH + File.separator + TLog.PLAYLIST_FOLDER + fileName);
        BufferedReader reader = null;
        FileReader fr = null;
        String line = "";
        ArrayList<String> playList = new ArrayList<String>();

        try {
            fr = new FileReader(myFile);
            reader = new BufferedReader(fr);
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.length() > 5){
                    playList.add(line);
                }
            }
            reader.close();
            fr.close();
        }catch (IOException e) {
            TLog.debug("Get playlist from sdcard failed." + e.getMessage());
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                    fr.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return playList;
    }

    /**
     * Get cpu log info from cpu log file.
     * @param fileName
     * @return
     */
    public static synchronized ArrayList<String> getCPULog(String fileName){
        File myFile = new File(ENV_PATH + File.separator + TLog.TEMP_FOLDER + fileName);
        BufferedReader reader = null;
        FileReader fr = null;
        String line = "";
        ArrayList<String> cpuUsage = new ArrayList<String>();

        try {
            fr = new FileReader(myFile);
            reader = new BufferedReader(fr);
            int index = 0;
            String usage = "";
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.contains("mediaserver")){
                    index = line.indexOf("%");
                    usage = line.substring(index-3, index);
                    usage = usage.trim();
                    cpuUsage.add(usage);
                }
            }
            reader.close();
            fr.close();
        }catch (IOException e) {
            TLog.debug("Get CPU log from sdcard failed." + e.getMessage());
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                    fr.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return cpuUsage;
    }


    public static synchronized boolean createSimpleLog(){
        File testCaseLog = new File(ENV_PATH + File.separator + TLog.LOG_FOLDER + ITestCase.TEST_CASE_LOG);
        File simpleLog = new File(ENV_PATH + File.separator + TLog.LOG_FOLDER + ITestCase.SIMPLE_LOG);
        BufferedReader reader = null;
        FileReader fr = null;
        BufferedWriter writer = null;
        FileWriter fw = null;
        String line = "";

        if(!testCaseLog.exists()){
            TLog.debug("log file not exist!");
            return false;
        }
        if(simpleLog.exists()){
            simpleLog.delete();
        }

        try {
            simpleLog.createNewFile();
            fr = new FileReader(testCaseLog);
            reader = new BufferedReader(fr);
            fw = new FileWriter(simpleLog,true);
            writer = new BufferedWriter(fw);

            int index = 1;
            int passCount = 0;
            int failCount = 0;
            boolean isOneResult = false;
            String content = "";
            String result = "";
            String comment = "";
            String flag = "\t";
            writer.write("Index" + flag + "TestContent" + flag + "Result" + flag + "Comments" + "\n");
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.contains(ITestCase.SET_DATA_SOURCE_LOG_FLAG)){
                    content = line.substring(ITestCase.SET_DATA_SOURCE_LOG_FLAG.length());
                    content = content.trim();
                    isOneResult = true;
                }else if(line.contains("---> TestCase") && isOneResult){
                    isOneResult = false;
                    if(line.contains("Passed")){
                        result = "Passed";
                        passCount++;
                        comment = "\n";
                    }else{
                        result = "Failed";
                        failCount++;
                        comment = line.substring(line.indexOf("TestCase") + 9);
                        comment = comment.trim();
                        comment = comment + "\n";
                    }
                    writer.write(index + flag + content + flag + result + flag + comment);
                    writer.flush();
                    index++;
                }
            }
            writer.write("\n" + "Summary:" + "\n" + "Total:" + flag + (index-1) + "\n" + "Passed:" + flag + passCount + "\n"
                    + "Failed:" + flag + failCount + "\n" + "PassRate:" + flag + (passCount*100/(index-1)) + "%");
            writer.flush();
            reader.close();
            writer.close();
            fr.close();
            fw.close();
        }catch (IOException e) {
            TLog.debug("Get log from sdcard failed." + e.getMessage());
            e.printStackTrace();
            return false;
        }finally {
            if(reader != null){
                try {
                    reader.close();
                    fr.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(writer != null){
                try {
                    writer.close();
                    fw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return true;
    }


}
