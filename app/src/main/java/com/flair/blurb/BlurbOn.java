package com.flair.blurb;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

public class BlurbOn extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = BlurbOn.class.getSimpleName();
    Toolbar toolbar;
    FloatingActionButton statsFab;
    BottomSheetBehavior behavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurb_on);
        toolbar = ((Toolbar) findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.share_container).setOnClickListener(this);
        findViewById(R.id.stats_container).setOnClickListener(this);
        statsFab = ((FloatingActionButton) findViewById(R.id.fab_stats));
        statsFab.setOnClickListener(this);

        ViewGroup bottomsheet = ((ViewGroup) findViewById(R.id.stats_layout));
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setBottomSheetCallback(callback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_container:
                break;
            case R.id.stats_container:
                break;
            case R.id.fab_stats:
                behavior.setState(behavior.getState() == BottomSheetBehavior.STATE_EXPANDED ? BottomSheetBehavior.STATE_COLLAPSED : BottomSheetBehavior.STATE_EXPANDED);
                break;
        }
    }

    BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheet.findViewById(R.id.header).setElevation(getResources().getDimension(R.dimen.bottom_sheet_header_elevation));
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    @Override
    public void onBackPressed() {
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
