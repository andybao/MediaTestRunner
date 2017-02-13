package com.andybao.mediatestrunner.framework.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andybao.mediatestrunner.R;

public class MainActivity extends AppCompatActivity {

    private ImageView audioButton;
    private ImageView videoButton;
    private Button helpButton;
    private Button settingButton;
    private TextView nameText;
    private CheckBox mCheckBox;
    private EditText mEditPath;
    private AlertDialog mSettingsDialog;
    private AlertDialog.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        nameText = (TextView)findViewById(R.id.media_name);
        audioButton = (ImageView) findViewById(R.id.audio_button);
        videoButton = (ImageView) findViewById(R.id.video_button);
        helpButton = (Button) findViewById(R.id.help_button);
        settingButton = (Button) findViewById(R.id.setting_button);

        TextPaint tp = nameText.getPaint();
        tp.setFakeBoldText(true);

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,
                        "This feature is not supported in this demo", Toast.LENGTH_SHORT).show();
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(intent);
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHelpDialog().show();
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });

    }

    private AlertDialog getHelpDialog(){
        AlertDialog helpDialog;
        String helpDoc = "MediaTestRunner: v1.0 \n" +
                "This is Media automation test tool demo. \n" +
                "SimpleUserGuide: \n" +
                "1. Edit and push the playlist file on /sdcard/testrunner/playlist/ folder." +
                "(Note: playlist file name must end with '_playlist.txt'. Audio default playlist is 'audio_default_playlist.txt' and " +
                "Video default playlist is 'video_default_playlist.txt'. You can add the loop count for every file in playlist," +
                "format is 'LOOP:xxx, e.g. LOOP:10. Default loop count is 1). \n" +
                "2. Launch the HelixTestRunner and select the TestCase. Click 'Start' button to start testing. \n" +
                "3. The log will be saved on /sdcard/testrunner/log/ folder. \n" +
                "4. You can check the running state by 'adb logcat -s TestCase'. \n" +
                "5. You can check Tool log by 'adb logcat -s TestRunner'. \n";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.help_title);
        TextView textView = new TextView(this);
        textView.setScrollContainer(true);
        textView.setText(helpDoc);
        builder.setView(textView);
        builder.setPositiveButton("OK", null);

        helpDialog = builder.create();

        return helpDialog;
    }

    private void showSettingsDialog(){
        if(mSettingsDialog == null){

            mBuilder = new AlertDialog.Builder(this);
            mBuilder.setTitle(R.string.settings);

            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.settings_dialog, null, false);


            mCheckBox = (CheckBox)v.findViewById(R.id.enable_checkbox);

            mEditPath = (EditText)v.findViewById(R.id.path);
            mEditPath.setEnabled(false);
            mEditPath.setFocusableInTouchMode(false);
            mEditPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath());

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        mEditPath.setEnabled(true);
                        mEditPath.setFocusableInTouchMode(true);
                    }else{
                        mEditPath.setEnabled(false);
                        mEditPath.setFocusableInTouchMode(false);
                    }
                }

            });

            mBuilder.setView(v);
            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "You didn't change anything", Toast.LENGTH_SHORT).show();
                }

            });

            mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mCheckBox.isChecked()){

                    }

                }
            });

            mSettingsDialog = mBuilder.create();
        }

        mSettingsDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
