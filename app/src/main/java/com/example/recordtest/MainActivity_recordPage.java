package com.example.recordtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity_recordPage extends AppCompatActivity {

    private final String TAG = "RecorderActivity";
    final int REQUEST_PERMISSION_CODE = 1000;
    String pathSave = "", path1 = "", path2 = "", path3 = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Button btnRecord1, btnPlay1, btnRecord2, btnPlay2, btnRecord3, btnPlay3, btnNextStep;
    Bundle bundle;
    String account, password, nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_record_page);

        bundle = getIntent().getExtras();

        String testStr = randomSentence();
        TextView textView = (TextView)findViewById(R.id.testString1);
        textView.setText(testStr);

        testStr = randomSentence();
        textView = (TextView)findViewById(R.id.testString2);
        textView.setText(testStr);

        testStr = randomSentence();
        textView = (TextView)findViewById(R.id.testString3);
        textView.setText(testStr);

        if(!checkPermissionFromDevice())
            requestPermission();

        btnRecord1 = (Button)findViewById(R.id.btnRecord1);
        btnPlay1 = (Button)findViewById(R.id.btnPlay1);
        btnRecord2 = (Button)findViewById(R.id.btnRecord2);
        btnPlay2 = (Button)findViewById(R.id.btnPlay2);
        btnRecord3 = (Button)findViewById(R.id.btnRecord3);
        btnPlay3 = (Button)findViewById(R.id.btnPlay3);
        btnNextStep = (Button)findViewById(R.id.btnNextStep);

        account = bundle.getString("account");
        password = bundle.getString("password");
        nickname = bundle.getString("nickname");

        btnPlay1.setEnabled(false);
        btnPlay2.setEnabled(false);
        btnPlay3.setEnabled(false);
        btnNextStep.setEnabled(false);

        btnRecord1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        path1 = pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/" + account + "_audio_record1.mp3";
                        btnRecord1.getBackground().setColorFilter(new LightingColorFilter(0x7d7d7d, 0x000000));
                        startRecord();
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnRecord1.getBackground().setColorFilter(new LightingColorFilter(0xd4d4d4, 0x000000));
                        try {
                            stopRecord();
                        } catch (RuntimeException e) {
                            Toast.makeText(MainActivity_recordPage.this, "請長按進行錄音", Toast.LENGTH_SHORT).show();
                        }
                        btnPlay1.setEnabled(true);
                        fileUpload();
                        if(!path1.equals("") && !path2.equals("") && !path3.equals(""))
                            btnNextStep.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        btnPlay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path1);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity_recordPage.this, "播放中...", Toast.LENGTH_SHORT).show();
            }
        });

        btnRecord2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        path2 = pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/" + account + "_audio_record2.mp3";
                        btnRecord2.getBackground().setColorFilter(new LightingColorFilter(0x7d7d7d, 0x000000));
                        startRecord();
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnRecord2.getBackground().setColorFilter(new LightingColorFilter(0xd4d4d4, 0x000000));
                        try {
                            stopRecord();
                        } catch (RuntimeException e) {
                            Toast.makeText(MainActivity_recordPage.this, "請長按進行錄音", Toast.LENGTH_SHORT).show();
                        }
                        btnPlay2.setEnabled(true);
                        fileUpload();
                        if(!path1.equals("") && !path2.equals("") && !path3.equals(""))
                            btnNextStep.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        btnPlay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path2);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity_recordPage.this, "播放中...", Toast.LENGTH_SHORT).show();
            }
        });

        btnRecord3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        path3 = pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/" + account + "_audio_record3.mp3";
                        btnRecord3.getBackground().setColorFilter(new LightingColorFilter(0x7d7d7d, 0x000000));
                        startRecord();
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnRecord3.getBackground().setColorFilter(new LightingColorFilter(0xd4d4d4, 0x000000));
                        try {
                            stopRecord();
                        } catch (RuntimeException e) {
                            Toast.makeText(MainActivity_recordPage.this, "請長按進行錄音", Toast.LENGTH_SHORT).show();
                        }
                        btnPlay3.setEnabled(true);
                        fileUpload();
                        if(!path1.equals("") && !path2.equals("") && !path3.equals(""))
                            btnNextStep.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        btnPlay3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path3);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity_recordPage.this, "播放中...", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity_recordPage.this  , MainActivity_signUpSuccess.class);
                startActivity(intent);
            }
        });
    }

    private void startRecord() {
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
                    //URL url = new URL("http://192.168.43.181/recordUpdate/update.php");
                    URL url = new URL("http://140.129.25.230/recordUpdate/update.php");   //school's server
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
                        ds.close();

                        Log.e(TAG, conn.getResponseCode() + "=======");
                        conn.disconnect();

                    /*
                                    作者：安卓小农民
                                    来源：CSDN
                                    原文：https://blog.csdn.net/Tomtwo/article/details/51721811
                                    版权声明：本文为博主原创文章，转载请附上博文链接！*/
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            deleteAccount();
        }
        MainActivity_recordPage.this.finish();
        return true;
    }

    public void deleteAccount() {
        BackgroundTask bt = new BackgroundTask();
        bt.execute(account);
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {
        String my_url;

        @Override
        protected String doInBackground(String... params) {
            String result = "error!";
            final String ac = params[0];

            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String newData = URLEncoder.encode("account", "UTF-8") + "=" + URLEncoder.encode(ac, "UTF-8");

                bw.write(newData);
                bw.flush();
                bw.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                Boolean isFirst = true;
                while((line = bufferedReader.readLine()) != null) {
                    if(isFirst) {
                        isFirst = false;
                    }
                    else {
                        stringBuilder.append("\n");
                    }
                    stringBuilder.append(line);
                }
                inputStream.close();
                result = stringBuilder.toString();

                httpURLConnection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            my_url = "http://140.129.25.230/recordUpdate/deleteAccount.php";
        }

        @Override
        protected void onPostExecute(String result) {}

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
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
