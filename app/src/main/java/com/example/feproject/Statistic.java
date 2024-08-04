package com.example.feproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Statistic extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    List<String> xValues = Arrays.asList("Correct", "Forward", "Backward", "Bending");
    SQLiteDatabase database;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = openOrCreateDatabase("statistics", MODE_PRIVATE, null);
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS statistics (day VARCHAR ,correct REAL, forward REAL, backward REAL, bending REAL)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor c = database.query("statistics", null, null, null, null, null, null);
        c.moveToNext();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        float correct = 0;
        float forward = 0;
        float backward = 0;
        float bending = 0;
        while (c.moveToNext()) {
            String day = c.getString(0);
            if (day.equals(currentDate)) {
                correct += c.getFloat(1);
                forward += c.getFloat(2);
                backward += c.getFloat(3);
                bending += c.getFloat(4);
            }
        }
        BarChart barChart = findViewById(R.id.bar_chart);
        barChart.setTouchEnabled(true);
        barChart.getAxisRight().setDrawLabels(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, correct));
        entries.add(new BarEntry(1, forward));
        entries.add(new BarEntry(2, backward));
        entries.add(new BarEntry(3, bending));

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(200);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        BarDataSet barDataSet = new BarDataSet(entries, "Cells");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        barChart.setData(new BarData(barDataSet));
        barChart.invalidate();

        barChart.getDescription().setEnabled(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
    }


}