package ru.turbomolot.ntchart;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.data.PointLine;
import ru.turbomolot.ntchart.series.SeriesLine;

public class DemoActivity extends AppCompatActivity {

    private NTChart chart;
    private SeriesLine seriesLine1;
    private SeriesLine seriesLine2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initChart();
    }

    private void initChart() {
        chart = (NTChart) findViewById(R.id.chart);
        if (chart == null)
            return;
        float Tx = 10f;
        seriesLine1 = new SeriesLine("S1");
        seriesLine1.setMaxDistanceStore((float) Math.PI * Tx * 2);
        seriesLine1.setMaxDistanceX((float) Math.round(Math.PI) * Tx);


//        seriesLine2 = new SeriesLine("S2");
//        seriesLine2.setMaxDistanceStore((float) Math.PI * Tx * 2);
//        seriesLine2.setMaxDistanceX((float) Math.round(Math.PI) * Tx);
//        seriesLine2.setColor(Color.parseColor("#007200"));

        chart.addSeries(seriesLine1);
//        chart.addSeries(seriesLine2);

        Thread thData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    float time = 0;
                    float dxTime = 0.01f;
                    List<PointLine> pts1 = new DataList<>();
//                    List<PointLine> pts2 = new DataList<>();

                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(60);
                        int cnt = 20;
                        while (--cnt >= 0) {
                            float y = (float) Math.sin(time) * 2f;
//                            if((time * 100) % 2 == 0)
//                                y *= (float) Math.random();
                            pts1.add(new PointLine(time, y));
//                            y = (float) Math.cos(time) * 2f;
//                            pts2.add(new PointLine(time, y));
                            time += dxTime;
                        }
                        seriesLine1.addPoints(pts1);
//                        seriesLine2.addPoints(pts2);
                        pts1.clear();
//                        pts2.clear();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thData.setDaemon(true);
        thData.start();
    }
}
