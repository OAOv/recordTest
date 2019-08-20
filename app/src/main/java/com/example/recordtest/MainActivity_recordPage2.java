package com.example.recordtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity_recordPage2 extends AppCompatActivity {

    private final String TAG = "RecorderActivity";
    final int REQUEST_PERMISSION_CODE = 1000;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Button btnRecord, btnStopRecord, btnPlay, btnStop, btnUpload, btnNextStep;
    Bundle bundle;
    String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_record_page2);

        bundle = getIntent().getExtras();

        String testStr = randomSentence();
        TextView textView = (TextView)findViewById(R.id.testString);
        textView.setText(testStr);

        if(!checkPermissionFromDevice())
            requestPermission();

        btnRecord = (Button)findViewById(R.id.btnRecord);
        btnStopRecord = (Button)findViewById(R.id.btnStopRecord);
        btnPlay = (Button)findViewById(R.id.btnPlay);
        btnStop = (Button)findViewById(R.id.btnStop);
        btnUpload = (Button)findViewById(R.id.btnUpload);
        btnNextStep = (Button)findViewById(R.id.btnNextStep);

        btnRecord.setEnabled(true);
        btnStopRecord.setEnabled(false);
        btnPlay.setEnabled(false);
        btnStop.setEnabled(false);
        btnUpload.setEnabled(false);
        btnNextStep.setEnabled(false);

        account = bundle.getString("account");

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermissionFromDevice()) {
                    pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/" + account + "_audio_record2.mp3";
                    setupMediaRecorder();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    btnRecord.setEnabled(false);
                    btnStopRecord.setEnabled(true);
                    btnPlay.setEnabled(false);
                    btnStop.setEnabled(false);
                    btnUpload.setEnabled(false);
                    btnNextStep.setEnabled(false);

                    Toast.makeText(MainActivity_recordPage2.this, "錄製中....", Toast.LENGTH_SHORT).show();
                }
                else {
                    requestPermission();
                }
            }
        });

        btnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecorder.stop();
                btnRecord.setEnabled(true);
                btnStopRecord.setEnabled(false);
                btnPlay.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                btnNextStep.setEnabled(false);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecord.setEnabled(false);
                btnStopRecord.setEnabled(false);
                btnPlay.setEnabled(false);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(true);
                btnNextStep.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(pathSave);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity_recordPage2.this, "播放中...", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecord.setEnabled(true);
                btnStopRecord.setEnabled(true);
                btnPlay.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                btnNextStep.setEnabled(false);


                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    setupMediaRecorder();
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnNextStep.setEnabled(true);

                if(pathSave != null && pathSave != "") {
                    Toast.makeText(MainActivity_recordPage2.this, "上傳中...", Toast.LENGTH_SHORT).show();
                    fileUpload();
                }
            }
        });

        btnNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity_recordPage2.this  , MainActivity_recordPage3.class);
                startActivity(intent);
            }
        });
    }

    private void fileUpload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String end = "\r\n";
                    String hyphens = "--";
                    String boundary = "*****";
                    File file = new File(pathSave);
                    // url = new URL("http://192.168.43.181/recordUpdate/update.php");
                    URL url = new URL("http://speech.cse.ttu.edu.tw/recordUpdate/update.php");   //school's server
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                    Log.e(TAG, file.toString());
                    if(file != null) {
                        DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
                        ds.writeBytes(hyphens + boundary + end);
                        ds.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""+pathSave.substring(pathSave.lastIndexOf("/"), pathSave.length())+"\""+end);
                        ds.writeBytes(end);

                        InputStream input = new FileInputStream(file);
                        int size = 1024;
                        byte[] buffer = new byte[size];
                        int length = -1;
                        while((length = input.read(buffer)) != -1) {
                            ds.write(buffer, 0, length);
                        }
                        input.close();
                        ds.writeBytes(end);
                        ds.writeBytes(hyphens + boundary + hyphens + end);
                        ds.flush();

                        Log.e(TAG, conn.getResponseCode() + "=======");
                        ds.close();
                        conn.disconnect();
                    }
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                File file = new File(pathSave);
                file.delete();
            }
        }).start();
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    /////////////////////////////////
    ////////////////////////////////

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
    public static String randomSentence(){


        String[] nounsPosition={"森林", "銀河", "城市" ,"教室", "學校", "公司", "遊樂園", "草地", "公園", "星空", "音樂廳"};
        String[] nonuTime = {"黑夜", "早晨", "正午", "凌晨", "清晨", "半夜", "午後"};
        String[] nounsN={"小鳥", "斑馬", "貓咪", "小狗", "音樂", "小提琴", "鋼琴", "長笛", "單簧管", "雲彩", "金魚"};
        String[] articles={"這些", "有著", "一些", "一片", "任何", "任意", "存在", "那些", "沒有", "失去", "一群", "這裡有", "那裡有"};
        String[] articlesIng = {"身在", "待在", "漫步在", "睡在", "醒來在"};
        String[] verbs={ "跑", "走", "跳", "飛" ,"越", "游", "發射", "穿", "吸引", "排斥"};
        String[] prepositions={ "向", "來", "過", "上" ,"下", "到", "去"};

        int  rNounPosition=(int)(Math.random() * 11);
        int  rNounTime=(int)(Math.random() * 7);
        int  rNounN=(int)(Math.random() * 11);

        int  rArticles=(int)(Math.random() * 13);
        int  rArticleIng=(int)(Math.random() * 4);

        int  rVerb=(int)(Math.random() * 10);
        int  rPrepostion=(int)(Math.random() * 7);


        String randomSentence = articlesIng[rArticleIng] + nonuTime[rNounTime] + articles[rArticles] + nounsN[rNounN] +
                verbs[rVerb] + prepositions[rPrepostion] + nounsPosition[rNounPosition] + "。";

        return randomSentence;
    }
}
