package com.anweshainfo.anwesha_registration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class reg_result extends AppCompatActivity {

    TextView tv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_result);
        tv = findViewById(R.id.tv_reg);
        String msg = getIntent().getStringExtra("msg");
        tv.setText(msg);
    }

    @Override
    public void onBackPressed() {
        Intent i  = new Intent(this,qrscannerActivity.class );
        startActivity(i);
        this.finish();
    }
}
