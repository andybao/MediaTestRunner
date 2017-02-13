package com.andybao.mediatestrunner.framework.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by andybao on 2017-01-31.
 */

public class SQLiteHelper {

    private MyOpenHelper mySQLHelper;
    private final String caseSeparator = "&";

    public SQLiteHelper(Context context){
        mySQLHelper = new MyOpenHelper(context, MyOpenHelper.DB_NAME, null, 1);
    }

    /**
     * Insert testrunner data to SQL. This method only will be invoked for the first time to start a testing.
     * @param classIndex
     * @param dataIndex
     * @param crtCount
     * @param caseClassList
     */
    public void insert(int classIndex, int dataIndex, int crtCount, ArrayList<String> caseClassList){
        SQLiteDatabase db = mySQLHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String caseList = "";

        values.put(MyOpenHelper.CLASS_INDEX, classIndex);
        values.put(MyOpenHelper.DATA_INDEX, dataIndex);
        values.put(MyOpenHelper.CURRENT_LOOP_COUNT, crtCount);

        int num = caseClassList.size();
        caseList = caseClassList.get(0);
        for(int i=1; i<num; i++){
            caseList = caseList + caseSeparator + caseClassList.get(i);
        }
        values.put(MyOpenHelper.TEST_CASS_LIST, caseList);

        long count = db.insert(MyOpenHelper.TABLE_NAME, null, values);
        if(count == -1){
            TLog.debug("Insert to SQLiter failed.");
        }else{
            TLog.debug("Insert to SQLiter passed.");
        }
        db.close();
    }

    /**
     * Update testrunner data to SQL.
     * @param classIndex
     * @param dataIndex
     * @param crtCount
     * @param caseClassList
     */
    public void update(int classIndex, int dataIndex, int crtCount, ArrayList<String> caseClassList){
        SQLiteDatabase db = mySQLHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyOpenHelper.CLASS_INDEX, classIndex);
        values.put(MyOpenHelper.DATA_INDEX, dataIndex);
        values.put(MyOpenHelper.CURRENT_LOOP_COUNT, crtCount);
        if(caseClassList != null){
            String caseList = "";
            int num = caseClassList.size();
            caseList = caseClassList.get(0);
            for(int i=1; i<num; i++){
                caseList = caseList + caseSeparator + caseClassList.get(i);
            }
            values.put(MyOpenHelper.TEST_CASS_LIST, caseList);
        }
        int count = db.update(MyOpenHelper.TABLE_NAME, values, "id=?", new String[]{"1"});
        if(count != 1){
            TLog.debug("Update SQLiter failed.");
        }else{
            TLog.debug("Update SQLiter passed.");
        }
        db.close();
    }

    /**
     * Query test status.
     * @param flag 0->class index; 1->data index; 2->current loop count
     * @return value
     */
    public int query(int flag){
        int result = -1;
        SQLiteDatabase db = mySQLHelper.getReadableDatabase();
        Cursor c = db.query(
                MyOpenHelper.TABLE_NAME,
                new String[]{MyOpenHelper.CLASS_INDEX,MyOpenHelper.DATA_INDEX,MyOpenHelper.CURRENT_LOOP_COUNT},
                "id=?",
                new String[] { "1" }, null, null, null);

        if(c.getCount() == 0){
            TLog.debug("No data in SQLiter.");
        }else{
            c.moveToLast();
            result = Integer.parseInt(c.getString(flag).trim());
        }
        c.close();
        db.close();

        return result;
    }

    /**
     * Query user selected test case list.
     * @return Test case list.
     */
    public ArrayList<String> querySelectedTestCaseClass(){
        ArrayList<String> testCaseList = new ArrayList<String>();
        String result = "";
        SQLiteDatabase db = mySQLHelper.getReadableDatabase();
        Cursor c = db.query(
                MyOpenHelper.TABLE_NAME,
                new String[]{MyOpenHelper.TEST_CASS_LIST},
                "id=?",
                new String[] { "1" }, null, null, null);

        if(c.getCount() == 0){
            TLog.debug("No data in SQLiter.");
        }else{
            c.moveToLast();
            result = c.getString(0).trim();
        }
        TLog.debug("Selected TestCase class list is: " + result);

        String[] caseList = result.split(caseSeparator);
        for(String caseName:caseList){
            if(caseName.length() > 3){
                testCaseList.add(caseName.trim());
            }
        }
        c.close();
        db.close();
        TLog.debug("Get test case number from SQL is: " + testCaseList.size());

        return testCaseList;
    }

    /**
     * Remove executed test case list from SQL.
     */
    public void removeTestCase(String caseName){
        String result = "";
        SQLiteDatabase db = mySQLHelper.getWritableDatabase();
        Cursor c = db.query(MyOpenHelper.TABLE_NAME, new String[]{MyOpenHelper.TEST_CASS_LIST}, "id=?", new String[] { "1" }, null, null, null);
        if(c.getCount() == 0){
            TLog.debug("No data in SQLiter.");
        }else{
            c.moveToLast();
            result = c.getString(0).trim();
        }

        String[] caseList = result.split(caseSeparator);
        String list = "";
        for(String name:caseList){
            name = name.trim();
            if(name.equals(caseName)){
                list = list + caseSeparator + name;
            }
        }

        ContentValues values = new ContentValues();
        values.put(MyOpenHelper.TEST_CASS_LIST, list);
        int count = db.update(MyOpenHelper.TABLE_NAME, values, "id=?", new String[]{"1"});
        if(count != 1){
            TLog.debug("Remove TestCase in SQLiter failed. " + caseName);
        }else{
            TLog.debug("Remove TestCase in SQLiter passed. " + caseName);
        }
        c.close();
        db.close();
    }

    /**
     * Delete all data.
     */
    public void delete(){
        SQLiteDatabase db = mySQLHelper.getWritableDatabase();
        db.delete(MyOpenHelper.TABLE_NAME, null, null);
        db.close();
    }

    /**
     *
     * Saved run status in SQL.
     */
    public class MyOpenHelper extends SQLiteOpenHelper {
        public static final String DB_NAME = "media_test_runner";
        public static final String TABLE_NAME = "test_data";
        public static final String ID = "id";
        public static final String CLASS_INDEX = "class_index";
        public static final String DATA_INDEX = "data_index";
        public static final String CURRENT_LOOP_COUNT = "current_count";
        public static final String TEST_CASS_LIST = "case_list";

        public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME + " ("
                    + ID + " INTEGER PRIMARY KEY,"
                    + TEST_CASS_LIST + " VARCHAR,"
                    + CLASS_INDEX + " VARCHAR,"
                    + DATA_INDEX + " VARCHAR,"
                    + CURRENT_LOOP_COUNT + " VARCHAR)" );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}
