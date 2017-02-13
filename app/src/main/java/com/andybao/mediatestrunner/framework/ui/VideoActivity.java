package com.andybao.mediatestrunner.framework.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.andybao.mediatestrunner.R;
import com.andybao.mediatestrunner.framework.service.TestRunnerService;
import com.andybao.mediatestrunner.framework.utils.SQLiteHelper;
import com.andybao.mediatestrunner.framework.utils.TLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andybao on 2017-02-03.
 */

public class VideoActivity extends AppCompatActivity {

    private HashMap<String, Boolean> selectItem = new HashMap<String, Boolean>();
    private Button startButton;
    private Button stopButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_main_view);

        startButton = (Button) findViewById(R.id.audio_start_button);
        stopButton = (Button) findViewById(R.id.audio_stop_button);

        //Initialize case list view.
        ListView listView = (ListView) findViewById(R.id.case_ListView);

        List<Map<String, Object>> listItem = new ArrayList<Map<String,Object>>();
        String[] caseList = this.getResources().getStringArray(R.array.VideoTestCase);

        for(String caseName:caseList){
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("checkBox", false);
            map.put("itemName", caseName.substring(TestRunnerService.TEST_CASE_NAME_FLAG.length()));
            listItem.add(map);
            selectItem.put(caseName, false);
        }

        SimpleAdapter listAdapter = new SimpleAdapter(this, listItem, R.layout.list_item, new String[] {"checkBox", "itemName"},new int[] {R.id.item_checkbox, R.id.item_name });
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> listAdapter, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.item_checkbox);
                TextView textView = (TextView) view.findViewById(R.id.item_name);
                if(checkBox.isChecked()){
                    checkBox.setChecked(false);
                    selectItem.put(TestRunnerService.TEST_CASE_NAME_FLAG + textView.getText().toString(), false);
                }else{
                    checkBox.setChecked(true);
                    selectItem.put(TestRunnerService.TEST_CASE_NAME_FLAG + textView.getText().toString(), true);
                }

                TLog.debug("item: " + textView.getText() + ", " + selectItem.get(TestRunnerService.TEST_CASE_NAME_FLAG + textView.getText().toString()));
            }
        });

        startButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        boolean isStart = InitializeTestDataInSQL();
                        if(isStart){
                            Intent intent = new Intent(VideoActivity.this, TestRunnerService.class);
                            startService(intent);
                            startButton.post(new Runnable(){
                                @Override
                                public void run() {
                                    startButton.setEnabled(false);
                                    Toast.makeText(VideoActivity.this, "Start testing!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        stopButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Toast.makeText(VideoActivity.this, "Testing will stop after few seconds...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TestRunnerService.TEST_RUNNER_BROADCAST_ACTION);
                intent.putExtra(TestRunnerService.TEST_RUNNER_BROADCAST_KEY_NAME, TestRunnerService.STOP_TEST_SERVICE);
                sendBroadcast(intent);
                startButton.setEnabled(true);
            }
        });
    }

    private boolean InitializeTestDataInSQL(){
        SQLiteHelper mSQLHelper = new SQLiteHelper(this);
        mSQLHelper.delete();
        ArrayList<String> selectedCaseList = new  ArrayList<String>();
        String[] caseList = this.getResources().getStringArray(R.array.VideoTestCase);

        for(String selected:caseList){
            TLog.debug("selectItem.get(selected): " + selectItem.get(selected));
            if(selectItem.get(selected)){
                TLog.debug("Selected:  " + selected);
                selectedCaseList.add(selected);
            }
        }
        TLog.debug("Selected test case number is: " + selectedCaseList.size());

        if(selectedCaseList.size() == 0){
            return false;
        }
        mSQLHelper.insert(0, -1, 1, selectedCaseList);
        return true;
    }
}
