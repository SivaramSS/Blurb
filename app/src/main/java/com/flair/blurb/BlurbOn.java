package com.flair.blurb;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class BlurbOn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurb_on);
        PackageManager
        FirebaseDatabase fdb = FirebaseDatabase.getInstance();

        fdb.getReference("apps").child("com-flair-blurb").setValue("Developed by flair apps");
    }
}
