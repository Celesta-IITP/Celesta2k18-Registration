package com.anweshainfo.anwesha_registration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class reg_result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_result);
    }

    @Override
    public void onBackPressed() {
        Intent i  = new Intent(this,qrscannerActivity.class );
        startActivity(i);
        this.finish();
    }
}
