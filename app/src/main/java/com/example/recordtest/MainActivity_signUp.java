package com.example.recordtest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    Button btnNextStep;
    EditText account, password, password2, nickname;
    String str_account, str_password, str_password2, str_nickname;
    String[] testStr = new String[3];
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign_up);

        randomSentenceFromGoogleSheet();

        btnNextStep = (Button)findViewById(R.id.btnNextStep);

        btnNextStep.setOnClickListener(new View.OnClickListener() {
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
                    save();
                }
                else if (!str_password.equals(str_password2)){
                    Toast.makeText(getApplication(),"密碼不一致，請重新輸入",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void save() {
        BackgroundTask bt = new BackgroundTask();
        bt.execute(str_account, str_password, str_nickname);
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {
        String my_url;

        @Override
        protected String doInBackground(String... params) {
            String result = "error!";
            final String ac = params[0];
            final String pw = params[1];
            final String nn = params[2];

            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String newData = URLEncoder.encode("account", "UTF-8") + "=" + URLEncoder.encode(ac, "UTF-8") + "&";
                newData += URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pw, "UTF-8") + "&";
                newData += URLEncoder.encode("nickname", "UTF-8") + "=" + URLEncoder.encode(nn, "UTF-8");

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
            my_url = "http://140.129.25.230/SecretNotes/signUp.php";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("00")) {
                bundle.putString("account", str_account);
                bundle.putString("password", str_password);
                bundle.putString("nickname", str_nickname);

                randomSentenceFromGoogleSheet();

                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity_signUp.this  , MainActivity_recordPage.class);
                startActivity(intent);
            }
           else if(result.equals("01"))
                Toast.makeText(getApplicationContext(), "帳號已存在, 請更換", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
    /////////////////////////////
    interface AsyncResult
    {
        void onResult(JSONObject object);
    }

    public void randomSentenceFromGoogleSheet() {
        new DownloadWebpageTask(new AsyncResult() {
            @Override
            public void onResult(JSONObject object) {
                processJson(object);
            }
        }).execute("https://spreadsheets.google.com/tq?key=1PcTQZVkZZMwnD2k6lLxGTLSZ_N9TSzolH1-nGx8mCGc");
    }

    private void processJson(JSONObject object) {
        try {
            JSONArray rows = object.getJSONArray("rows");

            for(int i = 1; i < 4; i++) {
                double testNum = Math.random() * 271;
                Log.e("testNum", "" + testNum);
                JSONObject row = rows.getJSONObject((int)testNum);
                JSONArray columns = row.getJSONArray("c");
                testStr[i-1] = columns.getJSONObject(0).getString("v");
                bundle.putString("testStr" + i, testStr[i-1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        AsyncResult callback;
        public DownloadWebpageTask(AsyncResult callback) {
            this.callback = callback;
        }
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to download the requested page.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // remove the unnecessary parts from the response and construct a JSON
            int start = result.indexOf("{", result.indexOf("{") + 1);
            int end = result.lastIndexOf("}");
            String jsonResponse = result.substring(start, end);
            try {
                JSONObject table = new JSONObject(jsonResponse);
                callback.onResult(table);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private String downloadUrl(String urlString) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int responseCode = conn.getResponseCode();
                is = conn.getInputStream();
                String contentAsString = convertStreamToString(is);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }
}
