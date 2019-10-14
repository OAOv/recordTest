package com.example.recordtest;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class MainActivity_Login extends AppCompatActivity {

    private final String TAG = "RecorderActivity";
    final int REQUEST_PERMISSION_CODE = 1000;
    String pathSave = "", pathSaveTmp = "", result = "";
    EditText userAccount;
    String str_account = "";
    MediaRecorder mediaRecorder;
    Button btnLogin;

    private  AudioRecord mAudioRecord;
    private boolean isRecording = false;
    private static final int audioBPP = 16;
    private static final int audioSource = MediaRecorder.AudioSource.MIC;
    private static final int audioRate = 44100;
    private static final int audioChannel = AudioFormat.CHANNEL_IN_MONO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = 2 * AudioRecord.getMinBufferSize(audioRate,audioChannel,audioFormat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__login);

        String testStr = "\n" + randomSentence() + "\n";
        TextView textView = (TextView)findViewById(R.id.randomString);
        textView.setText(testStr);


        userAccount = (EditText)findViewById(R.id.userAccount);
        SharedPreferences setting = getSharedPreferences("userRecord", MODE_PRIVATE);
        userAccount.setText(setting.getString("userAccount", ""));

        btnLogin = (Button)findViewById(R.id.btnLogin);

        if(!checkPermissionFromDevice())
            requestPermission();

        btnLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnLogin.getBackground().setColorFilter(new LightingColorFilter(0x7d7d7d, 0x000000));
                        if (startRecord() == false) {
                            Toast.makeText(MainActivity_Login.this, "請輸入帳戶", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnLogin.getBackground().setColorFilter(new LightingColorFilter(0xd4d4d4, 0x000000));
                        if (!str_account.equals("")) {
                            stopRecord();
                            fileUpload();
                            Toast.makeText(MainActivity_Login.this, "wait a moment...", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private Boolean startRecord() {
        str_account = userAccount.getText().toString();
        if(str_account.equals("")) {
            return false;
        }

        isRecording = true;
        pathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str_account + ".wav";
        pathSaveTmp = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str_account + "_tmp.wav";
        mAudioRecord = new AudioRecord(audioSource, audioRate, audioChannel, audioFormat, bufferSize);

        final byte data[] = new byte[bufferSize];
        final File fileAudio = new File(pathSaveTmp);

        mAudioRecord.startRecording();

        new Thread(new Runnable() {
            @Override
            public void run() {

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(fileAudio);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != fos) {
                    while (isRecording) {
                        int read = mAudioRecord.read(data, 0, bufferSize);
                        //返回正确时才读取数据
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                fos.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        /*原文链接：https://blog.csdn.net/u010126792/article/details/86309592*/
        return true;
    }

    private void stopRecord() {
        isRecording = false;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            //调用release之后必须置为null
            mAudioRecord = null;

            copyWaveFile(pathSaveTmp, pathSave);
        }
    }

    /////////////////////////////
    //to .wav file
    //http://selvaline.blogspot.com/2016/04/record-audio-wav-format-android-how-to.html
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = audioRate;
        int channels = ((audioChannel == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2);
        long byteRate = audioBPP * audioRate * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((audioChannel == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = audioBPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
    /////////////////////////////

    private void fileUpload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String end = "\r\n";
                    String hyphens = "--";
                    String boundary = "*****";
                    File file = new File(pathSave);
                    URL url = new URL("http://140.129.25.230/SecretNotes/loginUpload.php");   //school's server
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

                        Log.e(TAG, conn.getResponseCode() + "");
                        Log.e(TAG, conn.getResponseMessage());
                        ds.close();

                        if(conn.getResponseCode() == 200) {
                            InputStream inputStream = conn.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = null;
                            Boolean isFirst = true;
                            while ((line = bufferedReader.readLine()) != null) {
                                if (isFirst) {
                                    isFirst = false;
                                } else {
                                    stringBuilder.append("\n");
                                }
                                stringBuilder.append(line);
                            }
                            inputStream.close();
                            conn.disconnect();
                            result = stringBuilder.toString();

                            Log.e(TAG, result);
                            //如果結果跟輸入名稱相同就登入, 不同跳出訊息重新錄音
                            /*Intent intent = new Intent();
                            intent.setClass(MainActivity_Login.this, MainActivity_signUpSuccess.class);
                            startActivity(intent);*/
                        }

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
                file = new File(pathSaveTmp);
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
