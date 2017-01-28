package com.flair.blurb.ui;

import android.animation.Animator;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.flair.blurb.Constants;
import com.flair.blurb.R;
import com.flair.blurb.Util;
import com.flair.blurb.db.StatsContract;
import com.flair.blurb.service.BlurbNotificationService;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class BlurbOn extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = BlurbOn.class.getSimpleName();
    private static final String PROJECTION_COUNT_BY_CATEGORY = "COUNT(" + StatsContract.StatsEntry.COLUMN_CATEGORY + ")";
    Toolbar toolbar;
    FloatingActionButton statsFab;
    BottomSheetBehavior behavior;
    View upcomingFeaturesLayout, statsLayout, contentMain;
    long animTime;
    Animator reveal, hide;
    ScaleAnimation bubble, shrink;
    View upcomingIcon, backIcon;
    ImageView big_b;
    DisplayUpcomingFeatures displayUpcomingFeatures;
    DisplayStats displayStats;
    boolean notificationAccessGranted = false;
    static String preference_notificaion_access_granted_key, pref_service_running_key, pref_blurb_notification_enabled_key;
    private FirebaseAnalytics firebaseAnalytics;
    boolean isServiceRunning, isBlurbNotificationShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurb_on);
        toolbar = ((Toolbar) findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        animTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        preference_notificaion_access_granted_key = getString(R.string.pref_notification_access_granted_key);
        pref_service_running_key = getString(R.string.pref_service_running_key);
        pref_blurb_notification_enabled_key = getString(R.string.pref_blurb_notification_enabled);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        statsFab = ((FloatingActionButton) findViewById(R.id.fab_stats));
        upcomingFeaturesLayout = findViewById(R.id.upcoming_features_layout);
        statsLayout = findViewById(R.id.stats_layout);
        behavior = BottomSheetBehavior.from(statsLayout);
        contentMain = findViewById(R.id.content_main);
        upcomingIcon = findViewById(R.id.upcoming_features);
        backIcon = findViewById(R.id.back_upcoming_features);
        big_b = ((ImageView) findViewById(R.id.big_b));
        findViewById(R.id.blurb_activator).setOnClickListener(this);

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

        getLoaderManager().initLoader(Constants.BLURB_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        notificationAccessGranted = prefs.getBoolean(preference_notificaion_access_granted_key, false);
        isServiceRunning = prefs.getBoolean(pref_service_running_key, false);
        isBlurbNotificationShowing = prefs.getBoolean(pref_blurb_notification_enabled_key, false);
        changeBigBColor(isBlurbNotificationShowing);
    }

    public void loadAnim(ScaleAnimation anim) {
        anim.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        anim.setInterpolator(new OvershootInterpolator());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.blurb_activator:
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "R.id.blurb_activator");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "blurb_activator");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                blurbActivation();
                break;
            case R.id.share_container:
                shareThisApp();
                break;
            case R.id.upcoming_features:
                Log.d(TAG, "onClick: upcoming");
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
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                displayStats.showStats();
                statsLayout.findViewById(R.id.back_notifcaion_stats).setVisibility(View.VISIBLE);
            } else {
                statsLayout.findViewById(R.id.back_notifcaion_stats).setVisibility(View.GONE);
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
        String[] projection = new String[]{StatsContract.StatsEntry.COLUMN_CATEGORY, PROJECTION_COUNT_BY_CATEGORY};
        return new CursorLoader(this, StatsContract.StatsEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        displayStats.loadStats(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void shareThisApp() {
        String text = getString(R.string.share_app_text);
        text = text.concat(" https://play.google.com/store/apps/details?id=" + getPackageName());
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");
        startActivity(shareIntent);
    }


    public void blurbActivation() {
        notificationAccessGranted = getDefaultSharedPreferences(this).getBoolean(preference_notificaion_access_granted_key, false);
        if (isBlurbNotificationShowing) {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Constants.BLURB_NOTIFICATION_ID);
            Toast.makeText(this, getString(R.string.blurb_stopped), Toast.LENGTH_SHORT).show();
        } else {
            if(!notificationAccessGranted) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                Toast.makeText(this, getString(R.string.blurb_notification_access_request), Toast.LENGTH_LONG).show();
            } else if(isServiceRunning) {
                startService(new Intent(this, BlurbNotificationService.class).putExtra(getString(R.string.intent_request_key), Constants.REQUEST_START_BLURB));
            } else {
                Toast.makeText(this, "There was a problem starting the service", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(preference_notificaion_access_granted_key)) {
            Log.d(TAG, "onSharedPreferenceChanged: ");
            notificationAccessGranted = sharedPreferences.getBoolean(key, false);
        }
        else if(key.equals(pref_service_running_key)) {
            Log.d(TAG, "onSharedPreferenceChanged: ");
            isServiceRunning = sharedPreferences.getBoolean(key, false);
            changeBigBColor(isServiceRunning);
        }
        else if(key.equals(pref_blurb_notification_enabled_key)) {
            isBlurbNotificationShowing = sharedPreferences.getBoolean(pref_blurb_notification_enabled_key, false);
            changeBigBColor(isBlurbNotificationShowing);
        }
    }

    private void changeBigBColor(boolean serviceActive) {
        VectorDrawable drawable = ((VectorDrawable) big_b.getDrawable());
        drawable.setTint(ContextCompat.getColor(this, serviceActive ? R.color.blurb_enabled : R.color.blurb_disabled));
        big_b.setImageDrawable(drawable);
    }
}
