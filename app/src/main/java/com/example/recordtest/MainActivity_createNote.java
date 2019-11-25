package com.example.recordtest;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity_createNote extends AppCompatActivity {

    private final String TAG = "RecorderActivity";
    private Button btnCreate, btnDelete;
    private EditText etTitle, etDescription;
    private Bundle bundle;
    private String account, title, description;
    private int note_index;
    private boolean isExist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_create_note);

        btnCreate = (Button)findViewById(R.id.btnCreate);
        btnDelete = (Button)findViewById(R.id.btnDelete);
        etTitle = (EditText)findViewById(R.id.noteTitle);
        etDescription = (EditText)findViewById(R.id.noteDescription);
        bundle = getIntent().getExtras();

        account = bundle.getString("account");
        note_index = bundle.getInt("Note_Index");
        if(note_index < 0) {
            isExist = false;
        }
        else {
            isExist = true;
            title = bundle.getString("title");
            description = bundle.getString("description");
            etTitle.setText(title);
            etDescription.setText(description);
            btnCreate.setText("修改");
        }


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreate.setEnabled(false);

                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                if(!title.equals("")) {
                    createNote(account, title, description, isExist, note_index);

                    try{
                        Thread.sleep(1000);
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    MainActivity_createNote.this.finish();
                }
                else {
                    Toast.makeText(MainActivity_createNote.this, "請輸入標題", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(note_index < 0) {
                    MainActivity_createNote.this.finish();
                }
                else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity_createNote.this);
                    dialog.setTitle("刪除");
                    dialog.setMessage("確定要刪除此筆記嗎?");
                    dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteNote(note_index);
                            Toast.makeText(MainActivity_createNote.this, "刪除完成", Toast.LENGTH_SHORT).show();
                            MainActivity_createNote.this.finish();
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    ///////////////////////
    private void createNote(String account, String title, String description, Boolean isExist, int index_note) {
        createbackgroundTask bt = new createbackgroundTask();
        bt.execute(account, title, description, isExist.toString(), Integer.toString(index_note));
    }

    class createbackgroundTask extends AsyncTask<String, Void, String> {
        String my_url;

        @Override
        protected String doInBackground(String... params) {
            String result = "error!";
            final String account = params[0];
            final String title = params[1];
            final String description = params[2];
            final String isExist = params[3];
            final String note_index = params[4];

            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String newData = URLEncoder.encode("account", "UTF-8") + "=" + URLEncoder.encode(account, "UTF-8") + "&";
                newData += URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(title, "UTF-8") + "&";
                newData += URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(description, "UTF-8") + "&";
                newData += URLEncoder.encode("isExist", "UTF-8") + "=" + URLEncoder.encode(isExist, "UTF-8") + "&";
                newData += URLEncoder.encode("note_index", "UTF-8") + "=" + URLEncoder.encode(note_index, "UTF-8");

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

                Log.e(TAG, "account: " + account + " title: " + title + " description: " + description + " isExist: " + isExist + " note_index: " + note_index);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            my_url = "http://140.129.25.230/SecretNotes/createNote.php";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
    ///////////////////////

    /////////////////////
    //deleteNote
    private void deleteNote(int index_note) {
        deleteBackgroundTask bt = new deleteBackgroundTask();
        bt.execute(Integer.toString(index_note));
    }

    class deleteBackgroundTask extends AsyncTask<String, Void, String> {
        String my_url;

        @Override
        protected String doInBackground(String... params) {
            String result = "error!";
            final String note_index = params[0];

            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String newData = URLEncoder.encode("note_index", "UTF-8") + "=" + URLEncoder.encode(note_index, "UTF-8");

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
                Log.e(TAG, result);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            my_url = "http://140.129.25.230/SecretNotes/deleteNote.php";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
    /////////////////////
}
