package com.frost.mqtttutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import helpers.ChartHelper;
import helpers.MqttHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Pappi MQTT";

    MqttHelper mqttHelper;
    ChartHelper mChart;
    LineChart chart;

    TextView dataReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataReceived = (TextView) findViewById(R.id.dataReceived);
        chart = (LineChart) findViewById(R.id.chart);
        mChart = new ChartHelper(chart);

        startMqtt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mqttHelper.stopPlayer();
    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
    }
}
