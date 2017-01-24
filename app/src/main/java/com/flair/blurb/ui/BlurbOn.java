package com.flair.blurb.ui;

import android.animation.Animator;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

import com.flair.blurb.R;
import com.flair.blurb.Util;
import com.flair.blurb.db.StatsContract;

public class BlurbOn extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BLURB_LOADER_ID = 2695;
    private static final String TAG = BlurbOn.class.getSimpleName();
    Toolbar toolbar;
    FloatingActionButton statsFab;
    BottomSheetBehavior behavior;
    View upcomingFeaturesLayout, statsLayout, contentMain;
    long animTime;
    Animator reveal, hide;
    ScaleAnimation bubble, shrink;
    View upcomingIcon, backIcon;
    DisplayUpcomingFeatures displayUpcomingFeatures;
    DisplayStats displayStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurb_on);
        toolbar = ((Toolbar) findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        animTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        statsFab = ((FloatingActionButton) findViewById(R.id.fab_stats));
        upcomingFeaturesLayout = findViewById(R.id.upcoming_features_layout);
        statsLayout = findViewById(R.id.stats_layout);
        behavior = BottomSheetBehavior.from(statsLayout);
        contentMain = findViewById(R.id.content_main);
        upcomingIcon = findViewById(R.id.upcoming_features);
        backIcon = findViewById(R.id.back_upcoming_features);

        upcomingIcon.setOnClickListener(this);
        findViewById(R.id.share_container).setOnClickListener(this);

        statsFab.setOnClickListener(this);
        behavior.setBottomSheetCallback(callback);

        bubble = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        loadAnim(bubble);
        loadAnim(shrink);

        backIcon.setOnClickListener(this);
        displayStats = new DisplayStats(this, statsLayout);
        getLoaderManager().initLoader(BLURB_LOADER_ID, null, this);
    }

    public void loadAnim(ScaleAnimation anim) {
        anim.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        anim.setInterpolator(new OvershootInterpolator());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_container:
                break;
            case R.id.upcoming_features:
                if (upcomingFeaturesLayout.getVisibility() == View.INVISIBLE) {
                    int[] coords = Util.getCenterCoordsOfView(v);
                    showUpcoming(Math.max(upcomingFeaturesLayout.getWidth(), upcomingFeaturesLayout.getHeight()), coords[0], coords[1]);
                }
                break;
            case R.id.fab_stats:
                behavior.setState(behavior.getState() == BottomSheetBehavior.STATE_EXPANDED ? BottomSheetBehavior.STATE_COLLAPSED : BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.back_upcoming_features:
                int[] coords = Util.getCenterCoordsOfView(upcomingIcon);
                hideUpcoming(Math.max(upcomingFeaturesLayout.getWidth(), upcomingFeaturesLayout.getHeight()), coords[0], coords[1]);
                break;
        }
    }

    BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void onBackPressed() {
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (upcomingFeaturesLayout.getVisibility() == View.VISIBLE) {
            int[] coords = Util.getCenterCoordsOfView(upcomingIcon);
            hideUpcoming(Math.max(upcomingFeaturesLayout.getWidth(), upcomingFeaturesLayout.getHeight()), coords[0], coords[1]);
        } else {
            super.onBackPressed();
        }
    }

    private void showUpcoming(float radius, int centerX, int centerY) {
        upcomingFeaturesLayout.setVisibility(View.VISIBLE);
        reveal = ViewAnimationUtils.createCircularReveal(upcomingFeaturesLayout, centerX, centerY, 0, radius);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.setDuration(animTime);
        reveal.start();
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                statsFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        statsFab.startAnimation(shrink);
    }

    private void hideUpcoming(float radius, int centerX, int centerY) {
        hide = ViewAnimationUtils.createCircularReveal(upcomingFeaturesLayout, centerX, centerY, radius, 0);
        hide.setInterpolator(new AccelerateDecelerateInterpolator());
        hide.setDuration(animTime);
        hide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                upcomingFeaturesLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        hide.start();
        statsFab.setVisibility(View.VISIBLE);
        statsFab.startAnimation(bubble);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{StatsContract.StatsEntry.COLUMN_KEY, StatsContract.StatsEntry.COLUMN_CATEGORY, StatsContract.StatsEntry.COLUMN_TIMESTAMP};
        return new CursorLoader(this, StatsContract.StatsEntry.CONTENT_URI, projection, null, null, StatsContract.StatsEntry.COLUMN_TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        displayStats.loadStats(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
