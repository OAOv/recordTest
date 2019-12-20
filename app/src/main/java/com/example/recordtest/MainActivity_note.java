package com.example.recordtest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
import java.util.ArrayList;

public class MainActivity_note extends AppCompatActivity {
    private final String TAG = "RecorderActivity";
    private RecyclerView noteLists;
    private ArrayList<JsonDataList> arrayList;
    private ListAdapter listAdapter;
    private OnClickInterface onClickInterface;
    FloatingActionButton btnCreateNote;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_note);

        noteLists = (RecyclerView)findViewById(R.id.note);
        arrayList = new ArrayList<JsonDataList>();
        btnCreateNote = (FloatingActionButton)findViewById(R.id.btnCreateNote);
        bundle = getIntent().getExtras();

        noteLists.setHasFixedSize(true);
        noteLists.setLayoutManager(new LinearLayoutManager(this));

        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                bundle.putInt("Note_Index", -1);
                intent.putExtras(bundle);
                intent.setClass(MainActivity_note.this  , MainActivity_createNote.class);
                startActivity(intent);
            }
        });

        onClickInterface = new OnClickInterface() {
            @Override
            public void setClick(int val) {
                bundle.putInt("Note_Index", arrayList.get(val).index);
                bundle.putString("title", arrayList.get(val).title);
                bundle.putString("description", arrayList.get(val).description);
                Intent intent = new Intent(MainActivity_note.this, MainActivity_createNote.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };
    }

    public void getNote(String account) {
        BackgroundTask bt = new BackgroundTask();
        bt.execute(account);
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {
        String my_url;

        @Override
        protected String doInBackground(String... params) {
            String account = params[0];
            String result = "error!";
            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String newData = URLEncoder.encode("account", "UTF-8") + "=" + URLEncoder.encode(account, "UTF-8");

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

                Log.e(TAG, "newData: " + newData);
                Log.e(TAG, "result: " + result);
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
                for(int i = array.length() - 1; i >= 0; i--) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    int index = jsonObject.getInt("Note_Index");
                    String account = jsonObject.getString("Account");
                    String title = jsonObject.getString("Title");
                    String description = jsonObject.getString("Description");
                    String time = jsonObject.getString("Note_Time");
                    time = time.substring(0, 16); ///yyyy-mm-dd hh-mm
                    Log.e(TAG, "account: " + account + " title: " + title + " description: " + description);
                    arrayList.add(new JsonDataList(index, account, title, description, time));
                }
                listAdapter = new ListAdapter(arrayList, getApplicationContext(), onClickInterface);
                noteLists.setAdapter(listAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
    //////////////

    @Override
    protected void onStart() {
        super.onStart();
        if(arrayList.size() > 0) {
            arrayList.removeAll(arrayList);
            listAdapter.notifyDataSetChanged();
        }
        String account = bundle.getString("account");
        getNote(account);
    }
}
