package com.maiji.magkareble40;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blakequ.bluetooth_manager_lib.BleManager;
import com.blakequ.bluetooth_manager_lib.BleParamsOptions;
import com.blakequ.bluetooth_manager_lib.connect.BluetoothSubScribeData;
import com.blakequ.bluetooth_manager_lib.connect.ConnectConfig;
import com.blakequ.bluetooth_manager_lib.connect.ConnectState;
import com.blakequ.bluetooth_manager_lib.connect.ConnectStateListener;
import com.blakequ.bluetooth_manager_lib.connect.multiple.MultiConnectManager;

import org.achartengine.GraphicalView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import de.greenrobot.event.EventBus;

//import com.maiji.magkareble40.Dlib.BleManager;
//import com.maiji.magkareble40.Dlib.BleParamsOptions;
//import com.maiji.magkareble40.Dlib.connect.BluetoothSubScribeData;
//import com.maiji.magkareble40.Dlib.connect.ConnectConfig;
//import com.maiji.magkareble40.Dlib.connect.ConnectState;
//import com.maiji.magkareble40.Dlib.connect.ConnectStateListener;
//import com.maiji.magkareble40.Dlib.connect.multiple.MultiConnectManager;

/**
 * @author xqx
 * @email djlxqx@163.com
 * blog:http://www.cnblogs.com/xqxacm/
 * createAt 2017/9/6
 * description:  ble 4.0 多设备连接
 */

public class XBleActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    public static int value;
    public static final int IMA_ADPCM_4BIT_BLOCK = 256;
    public static final int IMA_ADPCM_PCM_RAW_LEN = 1010;
    short samp0 = 0, presamp = 0;
    int dataLen = 0;
    int index, iLen;
    int diff = 0;
    char code = 0;
    short sampx = 0;
    int testLength = 0;
    boolean startFlag = false;
    boolean odd = true, jieduan = false;
    byte[] jieduanArray;
    private Button btnSelectDevice;  //选择需要绑定的设备
    private Button btnStartConnect;  //开始连接按钮
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 3;
    private TextView txtContentMac, txtContentMac1, txtContentMac2; //获取到的数据解析结果显示
    private ArrayList<String> gattList = new ArrayList<String>();
    private final int REQUEST_CODE_PERMISSION = 1; // 权限请求码  用于回调
    File filetest1 = new File(Environment.getExternalStorageDirectory(), "test0.txt");
    File filetest2 = new File(Environment.getExternalStorageDirectory(), "test1.txt");
    MultiConnectManager multiConnectManager ;  //多设备连接
    private BluetoothAdapter bluetoothAdapter;   //蓝牙适配器
    private ChartService mService1, mService2, mService3;
    private LinearLayout mAudioCurveLayout1, mAudioCurveLayout2, mAudioCurveLayout3;//存放右图表的布局容器
    private GraphicalView mView1, mView2, mView3;//左右图表
    private Spinner spinner;
    private ArrayList<String> connectDeviceMacList; //需要连接的mac设备集合
    ArrayList<BluetoothGatt> gattArrayList; //设备gatt集合
    private StringBuffer temptext = new StringBuffer();
    private SeekBar seekBar;
    private Button openMusic, clearMusic, saveAudio, disfinish;
    private ImageButton bt_play, bt_pre, bt_next;
    private TextView currentTimeTxt, totalTimeTxt, musicinfo;
    private RadioGroup rg, position_rg;
    private String xingbie = "", nianling = "", additondisease = "", position = "";
    private EditText otherdis;
    private CheckBox checkButton1, checkButton2, checkButton3, checkButton4, checkButton5, checkButton6;
    private MusicServiceDemo.CallBack callBack;
    private boolean mFlag = true;
    private int mProgress;
    private boolean binderFlag = false;
    private ArrayList<MusicBean> musicBeanList = new ArrayList<MusicBean>();
    private MyHandler mHandler = new MyHandler(this);
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    private String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChangYin";
    File file = new File(Environment.getExternalStorageDirectory(), "test.txt");
    //StringBuilder stringBuffer1;
    ArrayList<String> disease = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();
    private FileOutputStream fos, fos1, fos2;
    private BufferedOutputStream bufos, bufos1, bufos2;
    private double ceshi [] = {0,1,0,1,0,1,0,1,0,1,0,1};
    private static int[] steptab = {7, 8, 9, 10, 11, 12, 13, 14,
            16, 17, 19, 21, 23, 25, 28, 31,
            34, 37, 41, 45, 50, 55, 60, 66,
            73, 80, 88, 97, 107, 118, 130, 143,
            157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658,
            724, 796, 876, 963, 1060, 1166, 1282, 1411,
            1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024,
            3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484,
            7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };
    private static int[] indextab = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
    int bufsize = AudioTrack.getMinBufferSize(7880, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);//一个采样点16比特-2个字节
    AudioTrack trackplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 7880, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufsize * 10, AudioTrack.MODE_STREAM);//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xble);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initVariables();
        initView();
        initEvent();
        //requestWritePermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //showToast("自Android 6.0开始需要打开位置权限才可以搜索到Ble设备");
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
//        if(!isLocationEnable(XBleActivity.this))
//            setLocationService();
        initConfig();  // 蓝牙初始设置
        //EventBus.getDefault().register(this);
        if (isLocationEnable(XBleActivity.this)) {
            // 扫描并选择需要连接的设备
            Intent intent = new Intent();
            intent.setClass(this, SelectDeviceActivity.class);
            startActivityForResult(intent, 1);
        } else {
            setLocationService();
        }
    }

    private void initVariables() {
        connectDeviceMacList = new ArrayList<>();
        gattArrayList = new ArrayList<>();
    }

    //播放时间的更新
    private class MyHandler extends Handler {
        // 弱引用
        private WeakReference<XBleActivity> reference;

        public MyHandler(XBleActivity activity) {
            reference = new WeakReference<XBleActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //播放时间的更新
            XBleActivity activity = reference.get();
            if (activity != null && activity.callBack != null) {
                int currentTime = activity.callBack.callCurrentTime();
                int totalTime = activity.callBack.callTotalDate();
                activity.seekBar.setMax(totalTime);
                activity.seekBar.setProgress(currentTime);
                String current = activity.format.format(new Date(currentTime));
                String total = activity.format.format(new Date(totalTime));
                activity.currentTimeTxt.setText(current);
                activity.totalTimeTxt.setText(total);
            }
        }
    }
    //通知时间的播放的更新过程，主线程的更新UI
    private void seekTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlag) {
                    if (callBack != null) {
                        mHandler.sendMessage(Message.obtain());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }).start();
    }

    //seekBar的时间的更新，调取主线程的更新
    private void forSeekBar() {
        mProgress = 0;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (callBack != null) {
                    mProgress = progress;
                    //Toast.makeText(MainActivity.this, ""+mProgress, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (callBack != null) {
                    //音乐服务
                    callBack.iSeekTo(mProgress);
                }
            }
        });
    }

    //播放服务的绑定
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            callBack = (MusicServiceDemo.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            callBack = null;
        }
    };

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        File dfile = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (dfile.exists() && dfile.isFile()) {
            if (dfile.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 未压缩的数据流pcm的数据头格式
     * @param out
     * @param totalAudioLen
     * @param totalDataLen
     * @param longSampleRate
     * @param channels
     * @param byteRate
     * @throws IOException
     */
    public void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        //
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    /**
     * 压缩数据流的数据格式头
     * @param out   最后需要生成的数据流文件流
     * @param totalAudioLen 原始压缩数据的长度
     * @param totalDataLen  加上数据头的长度
     * @param longSampleRate 原始压缩数据流的采样率
     * @param channels 音频的声道
     * @param byteRate 数据传输的字节率
     * @throws IOException 数据书写异常
     */
    //totalDataLen = totalAudioLen+52;
    //byteRate = (longSampleRate*channels*256)/505
    //s = totalAudioLen/(256*channels)*505
    public void ADWriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[48];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //
        header[16] = 20; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //
        header[20] = 17; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        //
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = 0;
        header[33] = 1;
        header[34] = 4;
        header[35] = 0;
        header[36] = 2;
        header[37] = 0;
        header[38] = (byte) 249;
        header[39] = 1;
//      header[40] = 'f';header[41]='a';header[42] = 'c';header[43] = 't';
//      header[44] = 4;
//      header[45] = 0;
//      header[46] = 0;
//      header[47] = 0;
//      header[48] = (byte) (s & 0xff);
//      header[49] = (byte) ((s >> 8) & 0xff);
//      header[50] = (byte) ((s >> 16) & 0xff);
//      header[51] = (byte) ((s >> 24) & 0xff) ;
        header[40] = 'd';
        header[41] = 'a';
        header[42] = 't';
        header[43] = 'a';
        header[44] = (byte) (totalAudioLen & 0xff);
        header[45] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[46] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[47] = (byte) ((totalAudioLen >> 24) & 0xff);
//      header[33] = 0;
//      header[34] = 16; // bits per sample
//      header[35] = 0;
//      header[36] = 'd';
//      header[37] = 'a';
//      header[38] = 't';
//      header[39] = 'a';
//      header[40] = (byte) (totalAudioLen & 0xff);
//      header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
//      header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
//      header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 48);
    }

    private void initEvent() {
        //btnSelectDevice.setOnClickListener(this);
        //btnStartConnect.setOnClickListener(this);
        mService1 = new ChartService(this, 1);
        mService1.setXYMultipleSeriesDataset("曲线图1");
        mService1.setXYMultipleSeriesRenderer(40000, 1, "实时曲线图", "采样点", "相对幅度", Color.RED, Color.BLACK, Color.BLACK, Color.WHITE);
        mView1 = mService1.getGraphicalView();
        //将图表添加到布局容器中
        mAudioCurveLayout1.addView(mView1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mService1.updateCharts(8000);
        //2
        mService2 = new ChartService(this, 2);
        mService2.setXYMultipleSeriesDataset("曲线图2");
        mService2.setXYMultipleSeriesRenderer(40000, 1, "", "采样点", "相对幅度", Color.RED, Color.BLACK, Color.BLACK, Color.WHITE);
        mView2 = mService2.getGraphicalView();
        //将图表添加到布局容器中
        mAudioCurveLayout2.addView(mView2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mService2.updateCharts(8000);
        //3
        mService3 = new ChartService(this, 3);
        mService3.setXYMultipleSeriesDataset("曲线图3");
        mService3.setXYMultipleSeriesRenderer(40000, 1, "", "采样点", "相对幅度", Color.RED, Color.BLACK, Color.BLACK, Color.WHITE);
        mView3 = mService3.getGraphicalView();
        //将图表添加到布局容器中
        mAudioCurveLayout3.addView(mView3, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mService3.updateCharts(8000);

        final List<String> datas = new ArrayList<String>();
        for (int i = 1; i < 80; i++) datas.add("" + i);
        MyAdapter adapter = new MyAdapter(this);
        spinner.setAdapter(adapter);
        adapter.setDatas(datas);
        //adapter.setDropDownViewResource(R.layout.spinner_style);
        spinner.setSelection(0, false);
        /**选项选择监听*/
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nianling = datas.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initView() {
        txtContentMac = (TextView) findViewById(R.id.connectState1);
        txtContentMac1 = (TextView) findViewById(R.id.connectState2);
        txtContentMac2 = (TextView) findViewById(R.id.connectState3);
        mAudioCurveLayout1 = (LinearLayout) findViewById(R.id.audio_curve1);
        mAudioCurveLayout2 = (LinearLayout) findViewById(R.id.audio_curve2);
        mAudioCurveLayout3 = (LinearLayout) findViewById(R.id.audio_curve3);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        bt_play = (ImageButton) findViewById(R.id.bt_play);
        bt_pre = (ImageButton) findViewById(R.id.bt_pre);
        bt_next = (ImageButton) findViewById(R.id.bt_next);
        currentTimeTxt = (TextView) findViewById(R.id.current_time_txt);
        totalTimeTxt = (TextView) findViewById(R.id.total_time_txt);
        musicinfo = (TextView) findViewById(R.id.musicinfo);
        openMusic = (Button) findViewById(R.id.openMusic);
        clearMusic = (Button) findViewById(R.id.clearMusic);
        disfinish = (Button) findViewById(R.id.disfinish);
        spinner = (Spinner) findViewById(R.id.spinner);
        position_rg = (RadioGroup) findViewById(R.id.position_rg);
        rg = (RadioGroup) findViewById(R.id.rg_sex);
        rg.setOnCheckedChangeListener(this);
        position_rg.setOnCheckedChangeListener(this);
        saveAudio = (Button) findViewById(R.id.saveAudio);
        saveAudio.setOnClickListener(this);
        saveAudio.setEnabled(false);
        otherdis = (EditText) findViewById(R.id.otherdis);
        otherdis.setEnabled(false);
        checkButton1 = (CheckBox) findViewById(R.id.checkButton1);
        checkButton2 = (CheckBox) findViewById(R.id.checkButton2);
        checkButton3 = (CheckBox) findViewById(R.id.checkButton3);
        checkButton4 = (CheckBox) findViewById(R.id.checkButton4);
        checkButton5 = (CheckBox) findViewById(R.id.checkButton5);
        checkButton6 = (CheckBox) findViewById(R.id.checkButton6);
        checkButton1.setOnCheckedChangeListener(this);
        checkButton2.setOnCheckedChangeListener(this);
        checkButton3.setOnCheckedChangeListener(this);
        checkButton4.setOnCheckedChangeListener(this);
        checkButton5.setOnCheckedChangeListener(this);
        checkButton6.setOnCheckedChangeListener(this);
        disfinish.setOnClickListener(this);
        bt_play.setOnClickListener(this);
        bt_pre.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        openMusic.setOnClickListener(this);
        clearMusic.setOnClickListener(this);
    }

    /**
     * 播放音乐通过Binder接口实现
     */
    public void playerMusicByIBinder() {
        boolean playerState = callBack.isPlayerMusic();
        if (playerState) {
            bt_play.setImageResource(R.drawable.pause);
        } else {
            bt_play.setImageResource(R.drawable.play);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 播放或者暂停
            case R.id.bt_play:
                if (binderFlag) {
                    playerMusicByIBinder();
                    musicinfo.setText(musicBeanList.get(value).getTitle());
                } else
                    Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_pre:
                if (binderFlag) {
                    callBack.isPlayPre();
                    musicinfo.setText(musicBeanList.get(value).getTitle());
                } else
                    Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_next:
                if (binderFlag) {
                    callBack.isPlayNext();
                    musicinfo.setText(musicBeanList.get(value).getTitle());
                } else
                    Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
                break;
            case R.id.openMusic:
                XBleActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(new File(sdPath))));
                Intent musicIntent = new Intent(this, MusicListActivity.class);
                startActivityForResult(musicIntent, 4);
//                Intent intent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                intent2.setData(Uri.fromFile(new File(sdPath)));
//                XBleActivity.this.sendBroadcast(intent2);
                break;
            case R.id.clearMusic:
                deleteFile(sdPath + "/FinalAudio.wav");
                deleteFile(sdPath + "/FinalAudio1.wav");
                deleteFile(sdPath + "/FinalAudio2.wav");
                //mService2.updateCharts(8000);
//                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                intent.setData(Uri.fromFile(new File(sdPath)));
//                XBleActivity.this.sendBroadcast(intent);
                XBleActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(new File(sdPath))));
                break;
            case R.id.otherdis:
                additondisease = otherdis.getText().toString();
                break;
            case R.id.disfinish:
                sb.delete(0, sb.length());
                for (int i = 0; i < disease.size(); i++) {
                    //把选择的爱好添加到string尾部
                    if (i == (disease.size() - 1)) {
                        sb.append(disease.get(i));
                    } else {
                        sb.append(disease.get(i) + ",");
                    }
                }
                saveAudio.setEnabled(true);
                Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.saveAudio:
                //Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
                if (position == null || position.length() == 0) {
                    Toast.makeText(XBleActivity.this, "请选择录制位置选项", Toast.LENGTH_SHORT).show();
                } else if (xingbie == null || xingbie.length() == 0) {
                    Toast.makeText(XBleActivity.this, "请选择性别选项", Toast.LENGTH_SHORT).show();
                } else if (nianling == null || nianling.length() == 0) {
                    Toast.makeText(XBleActivity.this, "请选择年龄选项", Toast.LENGTH_SHORT).show();
                } else {
                    //mService2.updateCharts(8000);
                    String finalname = "";
                    switch (Integer.parseInt(position)) {
                        case 1:
                            finalname = "FinalAudio.wav";
                            break;
                        case 2:
                            finalname = "FinalAudio1.wav";
                            break;
                        case 3:
                            finalname = "FinalAudio2.wav";
                            break;
                        case 4:
                            finalname = "待开发";
                    }
                    if (!finalname.equals("待开发")) {
                        File file = new File(sdPath + "/" + finalname);
                        file.renameTo(new File(sdPath + "/" + sDateFormat.format(new Date()) + "_" + position + "_" + sb + additondisease + "_" + xingbie + "_" + nianling + ".wav"));
                        XBleActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(new File(sdPath))));
                        Toast.makeText(this, sDateFormat.format(new Date()) + "_" + position + "_" + sb + additondisease + "_" + xingbie + "_" + nianling, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "目前支持3个传感器", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 选中状态改变时被触发
        switch (checkedId) {
            case R.id.rb_FeMale:
                // 当用户选择女性时
                xingbie = "女";
                break;
            case R.id.rb_Male:
                // 当用户选择男性时
                xingbie = "男";
                break;
            case R.id.position_rb1:
                // 当用户选择女性时
                position = "1";
                break;
            case R.id.position_rb2:
                // 当用户选择男性时
                position = "2";
                break;
            case R.id.position_rb3:
                // 当用户选择男性时
                position = "3";
                break;
            case R.id.position_rb4:
                // 当用户选择男性时
                position = "4";
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            //添加到爱好数组
            if (buttonView.getText().toString().trim().equals("其它"))
                otherdis.setEnabled(true);
            else
                disease.add(buttonView.getText().toString().trim());
        } else {
            //从数组中移除
            if (buttonView.getText().toString().trim().equals("其它"))
                otherdis.setEnabled(false);
            else
                disease.remove(buttonView.getText().toString().trim());
            //Adpcm2PcmInterface.Adpcm2Pcm();
        }
    }

    /**
     * 连接需要连接的传感器
     *
     */
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i])+" ");
        }
        return hexString.toString().toUpperCase();
    }
    private void connentBluetooth() {
        final String[] objects = connectDeviceMacList.toArray(new String[connectDeviceMacList.size()]);
        final List<String> list1 = Arrays.asList(objects);
        multiConnectManager.addDeviceToQueue(objects);
        multiConnectManager.addConnectStateListener(new ConnectStateListener() {
            @Override
            public void onConnectStateChanged(String address, ConnectState state) {
                //地址选择进行一个switch
                switch (list1.indexOf(address)) {
                    case 0:
                        switch (state) {
                            //连接中状态
                            case CONNECTING:
                                txtContentMac.setText("连接中(1)");
                                Toast.makeText(XBleActivity.this, "已连接", Toast.LENGTH_SHORT).show();
                                break;
                            //已连接状态
                            case CONNECTED:
                                txtContentMac.setText("已连接(1)");

                                break;
                            //断开连接状态
                            case NORMAL:
                                txtContentMac.setText("已断开(1)");
                                break;
                        }
                        break;
                    case 1:
                        switch (state) {
                            //连接中状态
                            case CONNECTING:
                                txtContentMac1.setText("连接中(2)");
                                break;
                            //已连接状态
                            case CONNECTED:
                                txtContentMac1.setText("已连接(2)");
                                break;
                            //断开连接状态
                            case NORMAL:
                                txtContentMac1.setText("已断开(2)");
                                break;
                        }
                        break;
                    case 2:
                        switch (state) {
                            //连接中状态
                            case CONNECTING:
                                txtContentMac2.setText("连接中(3)");
                                break;
                            //已连接状态
                            case CONNECTED:
                                txtContentMac2.setText("已连接(3)");
                                break;
                            //断开连接状态
                            case NORMAL:
                                txtContentMac2.setText("已断开(3)");
                                break;
                        }
                        break;

                }
            }
        });
        /**
         * 数据回调
         */
        multiConnectManager.setServiceUUID("00001000-0000-1000-8000-00805f9b34fb");
        multiConnectManager.addBluetoothSubscribeData(new BluetoothSubScribeData.Builder().setCharacteristicNotify(UUID.fromString("00001002-0000-1000-8000-00805f9b34fb")).build());
        //multiConnectManager.addBluetoothSubscribeData(new BluetoothSubScribeData.Builder().setCharacteristicNotify(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")).build());
       //multiConnectManager.setServiceUUID("0003a150-0000-1000-8000-00805f9b0131");
       //multiConnectManager.addBluetoothSubscribeData(new BluetoothSubScribeData.Builder().setCharacteristicNotify(UUID.fromString("0003a151-0000-1000-8000-00805f9b0131")).build());
        multiConnectManager.setBluetoothGattCallback(new BluetoothGattCallback() {
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                super.onCharacteristicChanged(gatt, characteristic);
                //Log.e("XBleActivity", " "+gattList.size());
                if (!gattList.contains(gatt.toString())) {
                   gattList.add(gatt.toString());
                    Toast.makeText(XBleActivity.this, "Service +1", Toast.LENGTH_SHORT).show();
                }
                //Log.e("XBleActivity", ""+gatt.toString()+" "+gattList.get(0));
                if (gattList.indexOf(gatt.toString()) == 0) {
                    //mService1.updateCharts(ceshi,8000);

                    byte[] data = jieduanIs(characteristic);
                    byte[] datatemp = characteristic.getValue();
//                    if(!startFlag){
//                        Log.e("XBleActivity", "开始数据:"+toHexString(datatemp));
//                        startFlag = true;
//                    }
                    testLength = testLength + datatemp.length;
                    Log.e("XBleActivity", "数据长度:"+testLength);
                    //开始
                    if (data.length == 2 && data[0] == -86 && data[1] == -86) {
                        Log.e("XBleActivity", ""+data.length);
                        Toast.makeText(XBleActivity.this, "Start", Toast.LENGTH_SHORT).show();
                        //开启播放流
                        //trackplayer.play();
                        //建立文件夹和文件
                        File file1 = new File(sdPath);
                        if (!file1.exists()) {
                            file1.mkdirs();
                        }
                        try {
                            File filepos1 = new File(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
                            fos = new FileOutputStream(filepos1, true);
                            bufos = new BufferedOutputStream(fos);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    //结束录制data.length == 20 &&  data1.length == 20 && data1[0] == -1 && data1[1] == -1 && data1[2] == -1 && data1[3] == -1
                    else if (data.length == 20 && data[0] == -1 && data[1] == -1 && data[2] == -1 && data[3] == -1) {
                        Log.e("XBleActivity", ""+data.length);
//                        Set set = new HashSet(Arrays.asList(data));
//                        if (set.size() == 1) {
                            //Toast.makeText(XBleActivity.this, "finished", Toast.LENGTH_SHORT).show();
                            //Log.e("XBleActivity", "finished ");
                            //Log.e();
                            trackplayer.stop();
                            //trackplayer.flush();
                            byte[] databuffer = new byte[1024];
                            try {
                                FileInputStream in = new FileInputStream(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
                                FileOutputStream out = new FileOutputStream(sdPath + "/FinalAudio.wav");
                                long totalAudioLen = in.getChannel().size();
                                //long totalDataLen = totalAudioLen + 36;
                                long totalDataLen = totalAudioLen + 40;
                                int srate = 8000;
                                //WriteWaveFileHeader(out, totalAudioLen, totalDataLen, 8000, 1, (8000 * 256 * 4) / 505);
                                ADWriteWaveFileHeader(out, totalAudioLen, totalDataLen, srate, 1, srate * 2);
                                while (in.read(databuffer) != -1) out.write(databuffer);
                                in.close();
                                out.close();
                                //删除蓝牙缓存数据文件
                                deleteFile(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                fos.close();
                                bufos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        //}
                    }
                        else {
                        //Log.e("XBleActivity", ""+data.length);
//                            try {
//                                bufos.write(characteristic.getValue(), 0, characteristic.getValue().length);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                            //解码数据显示

                            decodeByte(data);
                        }
                    }
                }
        });
        multiConnectManager.startConnect();
    }
    //追加文件：使用FileWriter
    public static void method2(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param characteristic
     * @return
     */
    private byte[] jieduanIs(BluetoothGattCharacteristic characteristic) {
        byte[] data;
        if (jieduan) {
            //将本次的byte数据和上次截留的byte数据拼接
            byte[] data1 = characteristic.getValue();
            data = new byte[data1.length + jieduanArray.length];
            // 合并两个数组
            System.arraycopy(jieduanArray, 0, data, 0, jieduanArray.length);
            System.arraycopy(data1, 0, data, jieduanArray.length, data1.length);
            jieduan = false;
        } else
            data = characteristic.getValue();
        return data;
    }

    /**
     * @param data
     */
    private void decodeByte(byte[] data) {
        //解析完成后实时显示
        if (dataLen == 0 && (dataLen + data.length) > 2) {
            dataLen = dataLen + data.length;
            mService1.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(data, data.length, true), 32000);
        } else if ((dataLen + data.length) < 256 && (dataLen + data.length) > 2) {
            dataLen = dataLen + data.length;
            mService1.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(data, data.length, false), 32000);
        } else if ((dataLen + data.length) >= 256) {
            if ((dataLen + data.length) == 256) {
                dataLen = dataLen + data.length;
                mService1.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(data, data.length, false), 32000);
                dataLen = 0;
            } else {
                dataLen = dataLen + data.length;
                //说明两块有重叠
                //step 1
                //将前一块的数据截取出来
                int tempLength = data.length + 256 - dataLen;
                byte[] temp = new byte[tempLength];
                for (int i = 0; i < tempLength; i++)
                    temp[i] = data[i];
                mService1.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(temp, tempLength, false), 32000);
                //step 2
                jieduanArray = new byte[dataLen - 256];
                for (int i = tempLength, j = 0; i < dataLen - 256; j++, i++) {
                    jieduanArray[j] = data[i];
                }
                jieduan = true;
                dataLen = 0;
            }
        }
    }

    /**
     * 对蓝牙的初始化操作
     */
    private void initConfig() {
        multiConnectManager = BleManager.getMultiConnectManager(this);
        // 获取蓝牙适配器
        try {
            // 获取蓝牙适配器
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
                return;
            }
            // 蓝牙没打开的时候打开蓝牙
            if (!bluetoothAdapter.isEnabled())
                bluetoothAdapter.enable();
        } catch (Exception err) {
        }
        ;
        //设置
        BleManager.setBleParamsOptions(new BleParamsOptions.Builder()
                .setBackgroundBetweenScanPeriod(5 * 60 * 1000)
                .setBackgroundScanPeriod(10000)
                .setForegroundBetweenScanPeriod(2000)
                .setForegroundScanPeriod(10000)
                .setDebugMode(BuildConfig.DEBUG)
                .setMaxConnectDeviceNum(7)            //最大可以连接的蓝牙设备个数
                .setReconnectBaseSpaceTime(1000)
                .setReconnectMaxTimes(Integer.MAX_VALUE)
                .setReconnectStrategy(ConnectConfig.RECONNECT_LINE_EXPONENT)
                .setReconnectedLineToExponentTimes(5)
                .setConnectTimeOutTimes(20000)
                .build());
    }

    /**
     * @author xqx
     * @email djlxqx@163.com
     * blog:http://www.cnblogs.com/xqxacm/
     * createAt 2017/8/30
     * description:  权限申请相关，适配6.0+机型 ，蓝牙，文件，位置 权限
     */
    private String[] allPermissionList = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 遍历出需要获取的权限
     */
    private void requestWritePermission() {
        ArrayList<String> permissionList = new ArrayList<>();
        // 将需要获取的权限加入到集合中  ，根据集合数量判断 需不需要添加
        for (int i = 0; i < allPermissionList.length; i++) {
            if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, allPermissionList[i])) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        String permissionArray[] = new String[permissionList.size()];
        for (int i = 0; i < permissionList.size(); i++) {
            permissionArray[i] = permissionList.get(i);
        }
        if (permissionList.size() > 0)
            ActivityCompat.requestPermissions(this, permissionArray, REQUEST_CODE_PERMISSION);
    }

    /**
     * 权限申请的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意使用write
            } else {
                //用户不同意，自行处理即可
                Toast.makeText(XBleActivity.this, "您取消了权限申请,可能会影响软件的使用,如有问题请退出重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @param context
     * @return
     */
    public static final boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
    }

    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (data!=null){
        switch (requestCode) {
            case 1:
                if (data != null) {
                    connectDeviceMacList = data.getStringArrayListExtra("data");
                    //Log.i("xqxinfo","需要连接的mac"+connectDeviceMacList.toString());
//                    temptext.append("待连接设备:"+connectDeviceMacList.toString()+"\n");
//                    txtContentMac.setText(temptext);
                    //获取设备gatt对象
                    for (int i = 0; i < connectDeviceMacList.size(); i++) {
                        BluetoothGatt gatt = bluetoothAdapter.getRemoteDevice(connectDeviceMacList.get(i)).connectGatt(this, false, new BluetoothGattCallback() {
                        });
                        gattArrayList.add(gatt);
                    }
                    connentBluetooth();
                }
                break;
            case 2:
                // 扫描并选择需要连接的设备
                Intent intent = new Intent();
                intent.setClass(this, SelectDeviceActivity.class);
                startActivityForResult(intent, 1);
                break;
            case 4:
                /** 接收音乐列表资源 */
                if (data.getIntExtra("isTouch", 0) == 11) {
                    if (binderFlag) {
                        unbindService(conn);
                        callBack = null;
                    }
                    musicBeanList = data.getParcelableArrayListExtra("MUSIC_LIST");
                    int currentPosition = data.getIntExtra("CURRENT_POSITION", 0);
                    Intent intentMusic = new Intent(this, MusicServiceDemo.class);
                    intentMusic.putParcelableArrayListExtra("MUSIC_LIST", musicBeanList);
                    intentMusic.putExtra("CURRENT_POSITION", currentPosition);
                    //startService(intent);
                    bindService(intentMusic, conn, Service.BIND_AUTO_CREATE);
                    binderFlag = true;
                    musicinfo.setText(musicBeanList.get(currentPosition).getTitle());
                    //打开数据显示波形
//                        try {
//                            byte[] databuffer1 = new byte[60000];
//                            FileInputStream in = new FileInputStream(musicBeanList.get(currentPosition).getMusicPath());
//                            while (in.read(databuffer1) != -1) {
//                            }
//                            StringBuilder stringBuffer = new StringBuilder(getRealLength(databuffer1));
//                            for (byte byteChar : databuffer1)
//                                stringBuffer.append(String.format("%02X", byteChar));
//                           // mService2.updateCharts(8000);
//                            //mService2.updateCharts(array(stringBuffer.toString()),8000);
//                            in.close();
//                            stringBuffer1 = stringBuffer;
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    seekTime();
                    forSeekBar();
                }
                break;
        }
//        }
    }

    /**
     * @param res 将字符串类型的数据转化成可以显示的归一化的数据流
     * @return
     */
    //补码字节处理
    public double[] array(String res) {
        double[] c = new double[res.length() / 4];
        int j = 0;
        for (int i = 0; i < res.length(); ) {
            if ((i + 4) > res.length())
                break;
            String a = res.substring(i + 2, i + 4) + res.substring(i, i + 2);
            int bm = Integer.parseInt(a, 16);
            if (bm < 32768)
                c[j] = bm / 32768.0;
            else {
                //c[j] = (-65536-(~bm+1))/32768.0;
                c[j] = (-65536 + bm) / 32768.0;
            }
            i = i + 4;
            j = j + 1;
        }
        return c;
    }

    /**
     * @param str
     * @return
     */
    //16进制字符转byte类型
    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    /**
     * @param imaData 压缩的数据字节流
     * @param iDataLen 压缩的数据字节流的数据长度
     * @param start_Flag 数据块的传输是否完毕
     * @return
     */
    //adpcm解码测试程序
    public double[] Decode_IMA_ADPCM_4BIT_MONO(byte[] imaData, int iDataLen, boolean start_Flag) {
        int tl = start_Flag ? ((iDataLen - 4) * 2 + 1) : iDataLen * 2;
        double[] rdata = new double[tl];
        byte[] pcmData = new byte[tl * 2];
        iLen = 0;
        int iLen1 = 0;
        int i = 0;
        odd = true;
        //数据的长度不够4字节说明数据块存在一定的问题
        if (start_Flag) {
            int startValue = Integer.parseInt(String.format("%02X", imaData[1]) + String.format("%02X", imaData[0]), 16);
            if (startValue < 32768)
                samp0 = (short) startValue;
            else
                samp0 = (short) (-65536 - (~startValue + 1));
            //提取索引位置
            index = imaData[2] & 0xFF;
            sampx = samp0;
            odd = true;
            rdata[iLen++] = sampx / 32768.0;
            pcmData[iLen1++] = imaData[0];
            pcmData[iLen1++] = imaData[1];
            i = i + 4;
        }
        while (i < iDataLen) {
            //周期性取一个字节高低位,先去一个字节的低4位，下次循环再取一个字节的高四位
            char a = ((char) (imaData[i] & 0xFF));
            if (odd)
                code = (char) (a & 0x0F);
            else
                code = (char) (a >> 4);
            diff = 0;
            if ((code & 4) != 0) diff = diff + steptab[index];
            if ((code & 2) != 0) diff = diff + (steptab[index] >> 1);
            if ((code & 1) != 0) diff = diff + (steptab[index] >> 2);
            diff = diff + (steptab[index] >> 3);

            if ((code & 8) != 0) diff = -diff;
            if ((sampx + diff) < -32768)
                sampx = -32768;
            else if ((sampx + diff) > 32767)
                sampx = 32767;
            else sampx = (short) (sampx + diff);

            if (sampx >= 0) {
                pcmData[iLen1++] = (byte) (sampx % 256);
                pcmData[iLen1++] = (byte) (sampx / 256);
            } else {
                pcmData[iLen1++] = (byte) ((sampx + 32768) % 256);
                pcmData[iLen1++] = (byte) ((sampx + 32768) / 256 + 128);
            }
            rdata[iLen++] = sampx / 32768.0;
            index = index + indextab[code - 0];
            //防止数据索引越界
            if (index < 0) index = 0;
            if (index > 88) index = 88;
            odd = !odd;
            //偶数变奇数时，说明取到下一个字节数据
            if (odd)
                i++;
        }
        //trackplayer.write(pcmData, 0, pcmData.length);
//        //将byte数据写入到Audio.raw文件中
//        try {
//            //trackplayer.write(pcmData, 0, pcmData.length);
//
//            //trackplayer.flush();
//            fos.write(pcmData, 0, pcmData.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //trackplayer.write(pcmData, 0, pcmData.length);
        Log.e("XBleActivity", "解码数据"+rdata.length);
        return rdata;
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binderFlag) {
            unbindService(conn);
            callBack = null;
        }
        if (bluetoothAdapter.isEnabled())
            bluetoothAdapter.disable();
        EventBus.getDefault().unregister(this);
    }
}
