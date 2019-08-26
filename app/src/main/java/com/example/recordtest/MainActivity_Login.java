package com.example.recordtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.UUID;

public class MainActivity_Login extends AppCompatActivity {

    private final String TAG = "RecorderActivity";
    final int REQUEST_PERMISSION_CODE = 1000;
    String pathSave = "";
    EditText userAccount;
    String str_account = "";
    MediaRecorder mediaRecorder;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__login);

        String testStr = "\n" + randomSentence() + "\n";
        TextView textView = (TextView)findViewById(R.id.randomString);
        textView.setText(testStr);

        btnLogin = (Button)findViewById(R.id.btnLogin);

        if(!checkPermissionFromDevice())
            requestPermission();

        btnLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnLogin.getBackground().setColorFilter(new LightingColorFilter(0x7d7d7d, 0x000000));
                        if (startRecord() == false)
                            Toast.makeText(MainActivity_Login.this, "請輸入帳戶", Toast.LENGTH_SHORT).show();
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnLogin.getBackground().setColorFilter(new LightingColorFilter(0xd4d4d4, 0x000000));
                        if (!str_account.equals("")) {
                            stopRecord();
                            fileUpload();
                        }
                        break;
                }
                return false;
            }
        });


    }

    private Boolean startRecord() {
        userAccount = (EditText)findViewById(R.id.userAccount);
        str_account = userAccount.getText().toString();

        if(str_account.equals("")) {
            return false;
        }

        pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + str_account + ".mp3";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);

        try{
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void stopRecord() {
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();

            mediaRecorder = null;
        }
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
                    //URL url = new URL("http://192.168.43.181/recordUpdate/loginUpload.php");
                    URL url = new URL("http://140.129.25.230/recordUpdate/loginUpload.php");   //school's server
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

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }


    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    ///////////////////////////

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
                verbs[rVerb] + prepositions[rPrepostion] + nounsPosition[rNounPosition];

        return randomSentence;
    }
}
