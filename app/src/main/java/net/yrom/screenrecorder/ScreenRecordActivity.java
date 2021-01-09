package net.yrom.screenrecorder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.flyco.tablayout.SegmentTabLayout;
import com.king.view.waveview.WaveView;

public class ScreenRecordActivity extends AppCompatActivity {
    WaveView waveView;
    SegmentTabLayout segmentTabLayout;
    private String[] mTitles_2 = {"标准480P", "高清720P", "超清1080P"};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recorder);
        waveView=findViewById(R.id.waveView);
        segmentTabLayout = findViewById(R.id.tl_2);
        segmentTabLayout.setTabData(mTitles_2);
        waveView.start();
    }
}
