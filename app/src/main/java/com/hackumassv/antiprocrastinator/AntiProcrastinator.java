package com.hackumassv.antiprocrastinator;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class AntiProcrastinator extends AppCompatActivity {

    //The manager of stats
    public UsageStatsManager statsManager;
    public HorizontalBarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_procrastinator);

        if(!checkForPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        //Start test code!
        ProfileList testList = new ProfileList(this);
        testList.processApps();
        testList.sort();

        System.out.println(testList.toString());

        //End test code!

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        chart = findViewById(R.id.chart);
        List<BarEntry> entries = new ArrayList<>();
        for(int i=0; i<testList.size(); i++)
        {
            if(testList.getProfile(i).timeInMinutes()>10)
            { entries.add(new BarEntry(i+1, testList.getProfile(i).timeInMinutes(), testList.getProfile(i).returnAppName()));}
        }
        BarDataSet dataSet = new BarDataSet(entries, "Minutes Spent");
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return (String)entry.getData();
            }
        });
        chart.setData(barData);

        XAxis xaxis = chart.getXAxis();
        xaxis.setDrawGridLines(false);
        xaxis.setDrawLabels(false);
        xaxis.setDrawAxisLine(false);

        YAxis left = chart.getAxisLeft();
        left.setDrawGridLines(false);
        left.setDrawLabels(false);
        left.setDrawAxisLine(false);

        YAxis right = chart.getAxisRight();
        right.setDrawGridLines(false);
        right.setTextSize(12);

        barData.setValueTextSize(12);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextSize(12);
        chart.invalidate();
        chart.setDrawValueAboveBar(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_anti_procrastinator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }

    private void printTimeInForeground(Context context){

        long time = System.currentTimeMillis();
        long deltaDay = 7*24*60*1000;
        long deltaWeek = 4*7*24*60*1000;
        long deltaMonth = 6*4*7*24*60*1000;
        long deltaYear = 2*365*24*60*1000;
        UsageStatsManager statsManager = context.getSystemService(UsageStatsManager.class);
        UsageEvents usageEventsWeek = statsManager.queryEvents(time - deltaMonth, time);
        List<UsageStats> usageStatsListWeek =  statsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY,time - deltaDay,time);
        List<UsageStats> usageStatsListDay =  statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,time - deltaWeek,time);
        for(int i = 0; i < usageStatsListDay.size(); i++){

            long timeInForegroundWeek = usageStatsListWeek.get(i).getTotalTimeInForeground();
            long timeInForegroundDay = usageStatsListDay.get(i).getTotalTimeInForeground();

            if (timeInForegroundWeek > 0) {
                System.out.println("------------------------------------------------------------------------");
                System.out.println(usageStatsListDay.get(i).getPackageName());
                System.out.print("Daily: ");
                System.out.println(timeInForegroundDay);
                System.out.println(usageStatsListWeek.get(i).getPackageName());
                System.out.print("Weekly: ");
                System.out.println(timeInForegroundWeek);
                System.out.println();
                System.out.println(usageStatsListDay.get(i).getLastTimeUsed());


            }//End if
        }//End for
        UsageEvents.Event event = new UsageEvents.Event();
        while(usageEventsWeek.hasNextEvent()){
            System.out.println("__________________________________________________");
            usageEventsWeek.getNextEvent(event);
            System.out.println(event.getPackageName());
            System.out.println(event.getClassName());
            System.out.print("Event type: ");
            System.out.println(event.getEventType());
            System.out.print("Timestamp: ");
            System.out.println(event.getTimeStamp());


        }
    }
}