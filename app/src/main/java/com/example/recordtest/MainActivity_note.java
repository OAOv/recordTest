package com.example.recordtest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity_note extends AppCompatActivity {
    private final String TAG = "RecorderActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_note);

        ListView listView;
        listView = (ListView)findViewById(R.id.note);

        save();
    }

    public void save() {
        MainActivity_note.BackgroundTask bt = new MainActivity_note.BackgroundTask();
        bt.execute();
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {
        String my_url;

        @Override
        protected String doInBackground(String... params) {
            String result = "error!";
            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty("Content-type", "text/html");
                httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
                httpURLConnection.setRequestProperty("contentType", "utf-8");

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
            my_url = "http://140.129.25.230/SecretNotes/getNotes.php";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray array = new JSONArray(result);
                for(int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    String account = jsonObject.getString("Account");
                    String title = jsonObject.getString("Title");
                    String context = jsonObject.getString("Context");
                    Log.e(TAG, "account: " + account + " title: " + title + " context: " + context);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
