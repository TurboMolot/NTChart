package ru.turbomolot.ntchart.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.Point2D;
import ru.turbomolot.ntchart.series.ISeries;
import ru.turbomolot.ntchart.series.SeriesLineM;
import ru.turbomolot.ntchart.utils.ConverterUtil;
import ru.turbomolot.ntchart.utils.FPSHelper;
import ru.turbomolot.ntchart.utils.generator.EcgCalc;
import ru.turbomolot.ntchart.utils.generator.EcgParameters;

public class MainActivity extends AppCompatActivity {

    private NTChart chart;
    private ISeries<Point2D> seriesLine1;
//    private SeriesLine seriesLine2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart = findViewById(R.id.NTChart);
        init();
    }

    private void init() {
        if (chart == null)
            return;
        seriesLine1 = new SeriesLineM();
        ((SeriesLineM) seriesLine1).setMaxDistanceStore((float)(4d * Math.PI * 40d));
        ((SeriesLineM) seriesLine1).setMaxDistanceX((float)(Math.PI * 20d));
//        seriesLine2 = new SeriesLine();
        chart.addSeries(seriesLine1);

//        seriesLine1.setMinY(-12f);
//        seriesLine1.setMaxY(12f);

//        chart.addSeries(seriesLine2);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                processSin();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void processSin() {
        float time = 0;
        float dx = 0.02f;
//        long curTime = System.currentTimeMillis();
//        long lastTime = curTime;
        float y;
        try {
//            EcgParameters ecgParam = new EcgParameters();
//            EcgCalc ecgCalc = new EcgCalc(ecgParam);
//            EcgCalc.Generator generator = ecgCalc.calculateEcg();

            FPSHelper fpsHelper = new FPSHelper();
            fpsHelper.setNormalFPS(24);
            List<Point2D> pts = new ArrayList<>();
            seriesLine1.addPoints(pts);

            while (!Thread.interrupted()) {
                fpsHelper.sleepNormal();
                int cnt = 10;
                while (--cnt >= 0) {
//                    EcgCalc.EcgMeasurement pt = generator.next();
//                    pts.add(new Point2D((float) pt.getTime(), (float) pt.getVoltage()));
                    y = 10 * (float) Math.sin(time);
                    if(y > 0) {
                        y = y + (float)(2d * Math.random());
                    }
                    pts.add(new Point2D(time, y));
                    time += dx;
                }
                seriesLine1.addPoints(pts);
                pts.clear();
////                seriesLine2.addPoints(pts);
////                curTime = System.currentTimeMillis();
////                time += ((double) (curTime - lastTime)) / 1000f;
////                lastTime = curTime;
//
////                List<Point2D> ptsRes = seriesLine1.getPoints();
////                if (ptsRes != null) {
////
////                }
//                pts.clear();
            }
//            seriesLine1.addPoints(pts);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.gc();
        System.exit(0);
    }

    /*-------------------------------------------------------------------------------------------*/

//    @Override
//    protected void onStart() {
//        super.onStart();
//        NTChartSV.nOnStart();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        NTChartSV.nOnResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        NTChartSV.nOnPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        NTChartSV.nOnStop();
//    }
}
