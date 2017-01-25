package com.flair.blurb.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.flair.blurb.R;

/**
 * Created by sivaram-3911 on 23/01/17.
 */

public class DisplayStats {

    private static String TAG = DisplayStats.class.getSimpleName();
    Context context;
    View statsLayout;
    ChartView chart;
    TextView summary;
    Cursor data;
    ProgressBar progressBar;
    int barColor;
    int totalcount = 0, steps;
    View back;

    public DisplayStats(final Context context, View statsLayout) {
        this.context = context;
        this.statsLayout = statsLayout;
        this.chart = ((ChartView) statsLayout.findViewById(R.id.chart));
        this.progressBar = ((ProgressBar) statsLayout.findViewById(R.id.progress_bar));
        this.barColor = ContextCompat.getColor(context, R.color.green);
        this.summary = ((TextView) statsLayout.findViewById(R.id.summary));
        back = statsLayout.findViewById(R.id.back_notifcaion_stats);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BlurbOn) context).behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        statsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void loadStats(Cursor data) {
        Log.d(TAG, "loadStats: " + data.getCount());
        this.data = data;
    }

    public void showStats() {
        if (data != null) {
            new ProcessData(chart, progressBar, data).execute();
        }
    }

    private class ProcessData extends AsyncTask<String, String, String> {

        BarChartView chart;
        ProgressBar progressBar;
        Cursor data;
        BarSet dataset;

        public ProcessData(ChartView chart, ProgressBar progressBar, Cursor data) {
            this.chart = ((BarChartView) chart);
            this.progressBar = progressBar;
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            summary.setVisibility(View.INVISIBLE);
            chart.setVisibility(View.INVISIBLE);
            chart.setRoundCorners(context.getResources().getDimension(R.dimen.chart_bar_round_radius));
            chart.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {
            dataset = new BarSet();
            totalcount = steps = 0;
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                Log.d(TAG, "doInBackground: " + data.getString(0) + " " + data.getInt(1));
                Bar bar = new Bar(data.getString(0), data.getInt(1));
                totalcount += data.getInt(1);
                dataset.addBar(bar);
            }
            steps = totalcount / 4;
            dataset.setColor(barColor);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (totalcount == 0) {
                ((ViewGroup) chart.getParent()).setVisibility(View.GONE);
            } else {
                ((ViewGroup) chart.getParent()).setVisibility(View.VISIBLE);
                chart.setVisibility(View.VISIBLE);
                chart.addData(dataset);
                chart.setStep(steps);
                chart.show();
            }
            summary.setVisibility(View.VISIBLE);
            summary.setText(context.getString(R.string.summary_text) + " " + totalcount);
            progressBar.setVisibility(View.GONE);
        }
    }
}
