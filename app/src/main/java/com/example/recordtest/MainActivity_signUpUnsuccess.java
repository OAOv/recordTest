package com.example.recordtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity_signUpUnsuccess extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign_up_unsuccess);

        Button btnToRecordPage = (Button)findViewById(R.id.btnToRecordPage);

        btnToRecordPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity_signUpUnsuccess.this.finish();
            }
        });

    }
}
