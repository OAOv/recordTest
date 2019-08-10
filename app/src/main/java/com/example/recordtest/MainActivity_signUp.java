package com.example.recordtest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity_signUp extends AppCompatActivity {
    Button btnCheck;
    EditText account, password, password2, nickname;
    String str_account, str_password, str_password2, str_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign_up);

        btnCheck = (Button)findViewById(R.id.btnSignUpCheck);

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = (EditText)findViewById(R.id.textAccount);
                password = (EditText)findViewById(R.id.textPW);
                password2 = (EditText)findViewById(R.id.textPW2);
                nickname = (EditText)findViewById(R.id.textNickname);
                str_account = account.getText().toString();
                str_password = password.getText().toString();
                str_password2 = password2.getText().toString();
                str_nickname = nickname.getText().toString();

                if(str_password.equals("") || str_password2.equals("") || str_account.equals("") || str_nickname.equals("")) {
                    Toast.makeText(getApplicationContext(), "不能為空", Toast.LENGTH_SHORT).show();
                }
                else if(str_password.equals(str_password2) && !str_account.equals("") && !str_nickname.equals("")) {
                    //Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                    checkAccount();
                    //save();
                }
                else if (!str_password.equals(str_password2)){
                    Toast.makeText(getApplication(),"密碼不一致，請重新輸入",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void checkAccount() {
        BackgroundTask bt = new BackgroundTask();
        bt.execute(str_account);
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
                httpURLConnection.disconnect();
                result = stringBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            my_url = "http://192.168.43.181/recordUpdate/checkAccount.php";
            //my_url = "http://speech.cse.ttu.edu.tw/recordUpdate/signUp.php";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("00")) {
                //Toast.makeText(MainActivity_signUp.this, "帳戶創建成功", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("account", str_account);
                bundle.putString("password", str_password);
                bundle.putString("nickname", str_nickname);

                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity_signUp.this  , MainActivity_recordPage.class);
                startActivity(intent);
            }
            else if(result.equals("01"))
                Toast.makeText(getApplicationContext(), "帳號已存在, 請更換", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
