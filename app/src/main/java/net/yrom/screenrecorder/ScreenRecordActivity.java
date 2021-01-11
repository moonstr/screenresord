package net.yrom.screenrecorder;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodecInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.tablayout.SegmentTabLayout;
import com.king.view.waveview.WaveView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;
import static net.yrom.screenrecorder.ScreenRecorder.AUDIO_AAC;
import static net.yrom.screenrecorder.ScreenRecorder.VIDEO_AVC;

public class ScreenRecordActivity extends AppCompatActivity {
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    WaveView waveView;
    SegmentTabLayout segmentTabLayout;
    TextView shu;
    CircleImageView shuCir;
    TextView heng;
    CircleImageView hengCir;
    ImageView start;
    RelativeLayout status;
    TextView pause;
    TextView stop;
    LinearLayout count_down;
    TextView countText;
    int current = 0;//0未开始，1进行中，2暂停中
    private String[] mTitles_2 = {"标准480P", "高清720P", "超清1080P"};
    Drawable pauseDrawable;
    Drawable startDrawable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recorder);
        pauseDrawable= getResources().getDrawable(R.mipmap.pause);
        startDrawable= getResources().getDrawable(R.mipmap.start);
        pauseDrawable.setBounds(0, 0, pauseDrawable.getMinimumWidth(), pauseDrawable.getMinimumHeight());
        startDrawable.setBounds(0, 0, startDrawable.getMinimumWidth(), startDrawable.getMinimumHeight());

        waveView = findViewById(R.id.waveView);
        segmentTabLayout = findViewById(R.id.tl_2);
        shu = findViewById(R.id.shu);
        shuCir = findViewById(R.id.shu_cir);
        heng = findViewById(R.id.heng);
        hengCir = findViewById(R.id.heng_cir);
        start = findViewById(R.id.start);
        status=findViewById(R.id.status);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);
        count_down = findViewById(R.id.count_down);
        countText = findViewById(R.id.count_time);
        segmentTabLayout.setTabData(mTitles_2);
        waveView.start();
        shu.setOnClickListener(v -> {
            setCheck(0);
        });
        heng.setOnClickListener(v -> {
            setCheck(1);
        });
        start.setOnClickListener(v -> {
            start.setVisibility(View.GONE);
            status.setVisibility(View.VISIBLE);
            count_down.setVisibility(View.VISIBLE);
            current = 1;
            countText.setText("00:02:00");
            initTimer();
        });
        pause.setOnClickListener(v -> {
            if (current==1){
                current=2;
                timer.cancel();
                pause.setText("开始");
                pause.setCompoundDrawables(null,startDrawable,null,null);
            }else if (current==2){
                current=1;
                pause.setText("暂停");
                pause.setCompoundDrawables(null,pauseDrawable,null,null);
                initTimer();
            }

        });
        setCheck(0);
        mMediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);

    }
    @TargetApi(M)
    private void requestPermissions() {
        String[] permissions = false
                ? new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}
                : new String[]{WRITE_EXTERNAL_STORAGE};
        boolean showRationale = false;
        for (String perm : permissions) {
            showRationale |= shouldShowRequestPermissionRationale(perm);
        }
        if (!showRationale) {
            requestPermissions(permissions, REQUEST_PERMISSIONS);
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.using_your_mic_to_record_audio))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        requestPermissions(permissions, REQUEST_PERMISSIONS))
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ScreenRecorder mRecorder;
    private VirtualDisplay mVirtualDisplay;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            // NOTE: Should pass this result data into a Service to run ScreenRecorder.
            // The following codes are merely exemplary.

            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Log.e("@@", "media projection is null");
                return;
            }

            mMediaProjection = mediaProjection;
            mMediaProjection.registerCallback(mProjectionCallback, new Handler());
            startCapturing(mediaProjection);
        }
    }
    private MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            if (mRecorder != null) {
                stopRecorder();
            }
        }
    };
    private void startRecorder() {
        if (mRecorder == null) return;
        mRecorder.start();
        registerReceiver(mStopActionReceiver, new IntentFilter(ACTION_STOP));
        moveTaskToBack(true);
    }
    private void startCapturing(MediaProjection mediaProjection) {
        VideoEncodeConfig video = createVideoConfig();
        AudioEncodeConfig audio = createAudioConfig(); // audio can be null
        if (video == null) {
            toast(getString(R.string.create_screenRecorder_failure));
            return;
        }

        File dir = getSavingDir();
        if (!dir.exists() && !dir.mkdirs()) {
            cancelRecorder();
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        final File file = new File(dir, "Screenshots-" + format.format(new Date())
                + "-" + video.width + "x" + video.height + ".mp4");
        Log.d("@@", "Create recorder with :" + video + " \n " + audio + "\n " + file);
        mRecorder = newRecorder(mediaProjection, video, audio, file);
        if (hasPermissions()) {
            startRecorder();
        } else {
            cancelRecorder();
        }
    }
    String  width480="480x360";
    String  width720="720x480";
    String  width1080="1280x720";

    private boolean hasPermissions() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        int granted = (false ? pm.checkPermission(RECORD_AUDIO, packageName) : PackageManager.PERMISSION_GRANTED)
                | pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName);
        return granted == PackageManager.PERMISSION_GRANTED;
    }
    private VideoEncodeConfig createVideoConfig() {
        final String codec = getSelectedVideoCodec();
        if (codec == null) {
            // no selected codec ??
            return null;
        }
        // video size
        int[] selectedWithHeight = getSelectedWithHeight();
        boolean isLandscape = currentCheck==1;
        int width = selectedWithHeight[isLandscape ? 0 : 1];
        int height = selectedWithHeight[isLandscape ? 1 : 0];
        int framerate = 15;
        int iframe = 1;
        int bitrate = 800;
        MediaCodecInfo.CodecProfileLevel profileLevel = Utils.toProfileLevel("Default");
        return new VideoEncodeConfig(width, height, bitrate,
                framerate, iframe, codec, VIDEO_AVC, profileLevel);
    }
    private String getSelectedVideoCodec() {
        return "OMX.qcom.video.encoder.avc";
    }
    private int[] getSelectedWithHeight() {
        String[] xes=new String[]{};
        if (segmentTabLayout.getCurrentTab()==0){
            xes=width480.split("x");
        }else if (segmentTabLayout.getCurrentTab()==1){
            xes=width720.split("x");
        }else if (segmentTabLayout.getCurrentTab()==2){
            xes=width1080.split("x");
        };
        if (xes.length != 2) throw new IllegalArgumentException();
        return new int[]{Integer.parseInt(xes[0]), Integer.parseInt(xes[1])};

    }
    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        final VirtualDisplay display = getOrCreateVirtualDisplay(mediaProjection, video);
        ScreenRecorder r = new ScreenRecorder(video, audio, display, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {
            long startTime = 0;

            @Override
            public void onStop(Throwable error) {
                runOnUiThread(() -> stopRecorder());
                if (error != null) {
                    toast("Recorder error ! See logcat for more details");
                    error.printStackTrace();
                    output.delete();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.fromFile(output));
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onStart() {
                mNotifications.recording(0);
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                if (startTime <= 0) {
                    startTime = presentationTimeUs;
                }
                long time = (presentationTimeUs - startTime) / 1000;
                mNotifications.recording(time);
            }
        });
        return r;
    }
    private VirtualDisplay getOrCreateVirtualDisplay(MediaProjection mediaProjection, VideoEncodeConfig config) {
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenRecorder-display0",
                    config.width, config.height, 1 /*dpi*/,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    null /*surface*/, null, null);
        } else {
            // resize if size not matched
            Point size = new Point();
            mVirtualDisplay.getDisplay().getSize(size);
            if (size.x != config.width || size.y != config.height) {
                mVirtualDisplay.resize(config.width, config.height, 1);
            }
        }
        return mVirtualDisplay;
    }
    private void cancelRecorder() {
        if (mRecorder == null) return;
        Toast.makeText(this, getString(R.string.permission_denied_screen_recorder_cancel), Toast.LENGTH_SHORT).show();
        stopRecorder();
    }
    private void stopRecorder() {
        mNotifications.clear();
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
        mButton.setText(getString(R.string.restart_recorder));
        try {
            unregisterReceiver(mStopActionReceiver);
        } catch (Exception e) {
            //ignored
        }
    }
    private AudioEncodeConfig createAudioConfig() {
        return null;
//        String codec = getSelectedAudioCodec();
//        if (codec == null) {
//            return null;
//        }
//        int bitrate = getSelectedAudioBitrate();
//        int samplerate = getSelectedAudioSampleRate();
//        int channelCount = getSelectedAudioChannelCount();
//        int profile = getSelectedAudioProfile();
//
//        return new AudioEncodeConfig(codec, AUDIO_AAC, bitrate, samplerate, channelCount, profile);
    }
    int currentCheck=0;//0竖1横
    private void setCheck(int position) {
        shu.setAlpha(position == 0 ? 1 : 0.5f);
        shu.setTextSize(DensityUtil.px2dp(position == 0 ? 48 : 44));
        shuCir.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
        heng.setAlpha(position == 1 ? 1 : 0.5f);
        heng.setTextSize(DensityUtil.px2dp(position == 1 ? 48 : 44));
        hengCir.setVisibility(position == 1 ? View.VISIBLE : View.INVISIBLE);
        currentCheck=position;

    }
    static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP";

    private BroadcastReceiver mStopActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_STOP.equals(intent.getAction())) {
//                stopRecordingAndOpenFile(context);
            }
        }
    };
    private final static int COUNT = 1;
    private final static int TOTAL_TIME_24 = 120;
    Timer timer;

    public void initTimer() {
        //countDown = (TextView) findViewById(R.id.textViewTime24);
        timer = new Timer();
        /**
         * 每一秒发送一次消息给handler更新UI
         * schedule(TimerTask task, long delay, long period)
         */
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(COUNT);
            }
        }, 0, 1000);
    }
    private void toast(String message, Object... args) {

        int length_toast = Locale.getDefault().getCountry().equals("BR") ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        // In Brazilian Portuguese this may take longer to read

        Toast toast = Toast.makeText(this,
                (args.length == 0) ? message : String.format(Locale.US, message, args),
                length_toast);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(toast::show);
        } else {
            toast.show();
        }
    }
    private static File getSavingDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "Screenshots");
    }
    private Handler handler = new Handler() {
        int num = TOTAL_TIME_24;

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case COUNT:
                    int minute = 0;
                    int second = 0;
                    if (num >= 60 && num <= 120) {
                        minute = (int) (num / 60f);
                        second = num % 60;
                    } else {
                        minute = 0;
                        second = num % 60;
                    }
                    StringBuilder stringBuffer = new StringBuilder();
                    stringBuffer.append("00:0").append(minute + ":").append(second>=10?(second + ""):("0"+second));
                    Log.i("设置时间","stringBuffer:"+stringBuffer.toString());

                    countText.setText(stringBuffer);
                    if (num == 0)
                        timer.cancel();//0秒结束
                    num--;
                    break;
                default:
                    break;
            }
        }

        ;
    };

}
