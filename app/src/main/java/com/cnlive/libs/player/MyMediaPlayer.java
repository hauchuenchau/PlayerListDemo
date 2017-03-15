package com.cnlive.libs.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cnlive.libs.R;
import com.cnlive.libs.utils.LightnessControl;
import com.cnlive.libs.utils.PlayerUtils;
import com.cnlive.libs.video.video.MediaPlayer;
import com.cnlive.libs.video.video.VideoView;
import com.cnlive.libs.video.video.base.IDataSource;
import com.cnlive.libs.video.video.base.IMediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mr.hou on 12/15/2016.
 */

public class MyMediaPlayer extends FrameLayout implements View.OnClickListener, GestureDetector.OnGestureListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "MyMediaPlayer";
    private Context context;
    private Activity activity;
    static final Handler myHandler = new Handler(Looper.getMainLooper()) {
    };


    //地址
    private String videoPath;//播放地址
    private String videoTitle;//title
    private int video_position = 0;

    //控件的位置信息
    private float mediaPlayerX;
    private float mediaPlayerY;

    // 计时器
    private Timer timer_video_time;
    private TimerTask task_video_timer;
    private Timer timer_controller;
    private TimerTask task_controller;

    //是否是横屏
    private boolean isPrepare = false;
    private boolean isFirstPlay = false;

    //控件
    private RelativeLayout mn_rl_bottom_menu;//底部控制栏
    private ImageView mn_iv_play_pause;//暂停/播放
    private TextView mn_tv_time;//播放事件/总时长
    private SeekBar mn_seekBar;//seekbar
    private ImageView mn_iv_back;//返回
    private TextView mn_tv_title;//标题
    private RelativeLayout mn_rl_top_menu;//顶部控制栏
    private RelativeLayout mn_player_rl_progress;//progressbar布局

    private ProgressBar mn_player_progressBar;
    private VideoView videoView;
    /**
     * 手势相关
     */
    private RelativeLayout gesture_volume_layout;// 音量控制布局
    private TextView geture_tv_volume_percentage;// 音量百分比
    private ImageView gesture_iv_player_volume;// 音量图标
    private RelativeLayout gesture_light_layout;// 亮度布局
    private TextView geture_tv_light_percentage;// 亮度百分比
    private RelativeLayout gesture_progress_layout;// 进度图标
    private TextView geture_tv_progress_time;// 播放时间进度
    private ImageView gesture_iv_progress;// 快进或快退标志
    private GestureDetector gestureDetector;
    private AudioManager audiomanager;
    private int maxVolume, currentVolume;
    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_LIGHT = 2f;// 协调亮度滑动时的步长，避免每次滑动都改变，导致改变过快
    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final int GESTURE_MODIFY_BRIGHTNESS = 3;


    public MyMediaPlayer(Context context) {
        this(context, null);
    }

    public MyMediaPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMediaPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        activity = (Activity) this.context;
        //自定义属性相关
        initAttrs(context, attrs);
        //其他
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MNViderPlayer);
        //遍历拿到自定义属性
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.MNViderPlayer_mnFirstNeedPlay) {
                isFirstPlay = typedArray.getBoolean(R.styleable.MNViderPlayer_mnFirstNeedPlay, false);
            }
        }
        //销毁
        typedArray.recycle();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int screenWidth = PlayerUtils.getScreenWidth(activity);
        int screenHeight = PlayerUtils.getScreenHeight(activity);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        //newConfig.orientation获得当前屏幕状态是横向或者竖向
        //Configuration.ORIENTATION_PORTRAIT 表示竖向
        //Configuration.ORIENTATION_LANDSCAPE 表示横屏
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //计算视频的大小16：9
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 9 / 16;
            setX(mediaPlayerX);
            setY(mediaPlayerY);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            layoutParams.width = screenWidth;
            layoutParams.height = screenHeight;

            setX(0);
            setY(0);
        }
        setLayoutParams(layoutParams);
    }

    /**
     * 初始化播放相关
     */
    private void init() {
        View inflate = View.inflate(context, R.layout.cn_player_view, this);
        mn_rl_bottom_menu = (RelativeLayout) inflate.findViewById(R.id.mn_rl_bottom_menu);
        mn_iv_play_pause = (ImageView) inflate.findViewById(R.id.mn_iv_play_pause);
        mn_tv_time = (TextView) inflate.findViewById(R.id.mn_tv_time);
        mn_seekBar = (SeekBar) inflate.findViewById(R.id.mn_seekBar);
        mn_iv_back = (ImageView) inflate.findViewById(R.id.mn_iv_back);
        mn_tv_title = (TextView) inflate.findViewById(R.id.mn_tv_title);
        mn_rl_top_menu = (RelativeLayout) inflate.findViewById(R.id.mn_rl_top_menu);
        mn_player_rl_progress = (RelativeLayout) inflate.findViewById(R.id.mn_player_rl_progress);
        mn_player_progressBar = (ProgressBar) inflate.findViewById(R.id.mn_player_progressBar);
        videoView = (VideoView) inflate.findViewById(R.id.videoView);

        mn_seekBar.setOnSeekBarChangeListener(this);
        mn_iv_play_pause.setOnClickListener(this);
        mn_iv_back.setOnClickListener(this);
        videoView.setOnPreparedListener(mOnPreparedListener);
        videoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        videoView.setOnInfoListener(mOnInfoListener);
        videoView.setOnErrorListener(mOnErrorListener);
        videoView.setOnSeekCompleteListener(mOnSeekCompletedListener);
        videoView.setOnCompletionListener(mOnCompletionListener);
        videoView.setOnPlayStateListener(mOnPlayStateListener);


        if (!isFirstPlay) {
            mn_player_progressBar.setVisibility(View.GONE);
        }

        //初始化手势
        initGesture();

        //存储控件的位置信息
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayerX = getX();
                mediaPlayerY = getY();
                Log.i(TAG, "控件的位置---X：" + mediaPlayerX + "，Y：" + mediaPlayerY);
            }
        }, 1000);
    }

    private void initViews() {
        mn_rl_bottom_menu.setVisibility(View.GONE);
        mn_rl_top_menu.setVisibility(View.GONE);
        mn_player_rl_progress.setVisibility(View.VISIBLE);
        mn_player_progressBar.setVisibility(View.VISIBLE);
        initTopMenu();
    }

    private void initTopMenu() {
        mn_tv_title.setText(videoTitle);
    }

    /**
     * onclik点击事件监听
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mn_iv_play_pause://播放/暂停
                if (videoView != null) {
                    if (videoView.isPlaying()) {
                        videoView.pause();
                        mn_iv_play_pause.setImageResource(R.drawable.mn_player_play);
                    } else {
                        videoView.start();
                        mn_iv_play_pause.setImageResource(R.drawable.mn_player_pause);
                    }
                }
                break;
            case R.id.mn_iv_back://退出
                activity.finish();
                break;
        }

    }


    //下面菜单的显示和隐藏
    private void initBottomMenuState() {
        if (mn_rl_bottom_menu.getVisibility() == View.GONE) {
            initControllerTask();
            mn_rl_bottom_menu.setVisibility(View.VISIBLE);
            mn_rl_top_menu.setVisibility(VISIBLE);
        } else {
            destroyControllerTask(true);
        }
    }

    private void dismissControllerMenu() {
        mn_rl_top_menu.setVisibility(View.GONE);
        mn_rl_bottom_menu.setVisibility(View.GONE);
    }


    /**
     * 计时器相关操作
     */
    private void initTimeTask() {
        timer_video_time = new Timer();
        task_video_timer = new TimerTask() {
            @Override
            public void run() {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView == null) {
                            return;
                        }
                        //设置时间
                        mn_tv_time.setText(String.valueOf(PlayerUtils.converLongTimeToStr(videoView.getCurrentPosition()) + " / " + PlayerUtils.converLongTimeToStr(videoView.getDuration())));
                        //进度条
                        int progress = (int) videoView.getCurrentPosition();
                        mn_seekBar.setProgress(progress);
                    }
                });
            }
        };
        timer_video_time.schedule(task_video_timer, 0, 1000);
    }

    private void destroyTimeTask() {
        if (timer_video_time != null && task_video_timer != null) {
            timer_video_time.cancel();
            task_video_timer.cancel();
            timer_video_time = null;
            task_video_timer = null;
        }
    }

    private void initControllerTask() {
        // 设置计时器,控制器的影藏和显示
        timer_controller = new Timer();
        task_controller = new TimerTask() {
            @Override
            public void run() {
                destroyControllerTask(false);
            }
        };
        timer_controller.schedule(task_controller, 5000);
        initTimeTask();
    }

    private void destroyControllerTask(boolean isMainThread) {
        if (isMainThread) {
            dismissControllerMenu();
        } else {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    dismissControllerMenu();
                }
            });
        }
        if (timer_controller != null && task_controller != null) {
            timer_controller.cancel();
            task_controller.cancel();
            timer_controller = null;
            task_controller = null;
        }
        destroyTimeTask();
    }

    /**
     * CNLiveSDK播放器接口方法实现
     */
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mediaplayer) {
            mediaplayer.start(); // 开始播放
            isPrepare = true;
            if (video_position > 0) {
                Log.i(TAG, "onPrepared---video_position:" + video_position);
                mediaplayer.seekTo(video_position);
                video_position = 0;
            }
            // 把得到的总长度和进度条的匹配
            mn_seekBar.setMax((int) mediaplayer.getDuration());
            mn_iv_play_pause.setImageResource(R.drawable.mn_player_pause);
            mn_tv_time.setText(String.valueOf(PlayerUtils.converLongTimeToStr(mediaplayer.getCurrentPosition()) + "/" + PlayerUtils.converLongTimeToStr(mediaplayer.getDuration())));
            //延时：避免出现上一个视频的画面闪屏
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initBottomMenuState();
                    mn_player_rl_progress.setVisibility(View.GONE);
                }
            }, 500);
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mn_iv_play_pause.setImageResource(R.drawable.mn_player_play);
            destroyControllerTask(true);
            video_position = 0;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            if (percent >= 0 && percent <= 100) {
                int secondProgress = (int) (mp.getDuration() * percent / 100);
                mn_seekBar.setSecondaryProgress(secondProgress);
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangeListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {

        }
    };

    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompletedListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            Log.e(TAG, "onSeekComplete...............");
            mn_seekBar.setProgress((int) mp.getCurrentPosition());
            audiomanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                    break;
                default:
                    Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
            }

            return false;
        }
    };

    public IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            switch (i) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.d(TAG, "Buffering Start.");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.d(TAG, "Buffering End.");
                    break;
                case MediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    Toast.makeText(activity, "Audio Rendering Start", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Toast.makeText(activity, "Video Rendering Start", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.MEDIA_INFO_SUGGEST_RELOAD:
                    // Player find a new stream(video or audio), and we could reload the video.
                    if (videoView != null)
//                    videoView.reload(videoPath, false);
                        break;
                case MediaPlayer.MEDIA_INFO_RELOADED:
                    Toast.makeText(activity, "Succeed to reload video.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Succeed to reload video.");
                    return false;
            }
            return false;
        }
    };

    public IMediaPlayer.OnPlayStateListener mOnPlayStateListener = new IMediaPlayer.OnPlayStateListener() {
        @Override
        public void onPlayState(boolean played) {
        }
    };


    /**
     * initGesture初始化
     */
    private void initGesture() {
        gesture_volume_layout = (RelativeLayout) findViewById(R.id.mn_gesture_volume_layout);
        geture_tv_volume_percentage = (TextView) findViewById(R.id.mn_gesture_tv_volume_percentage);
        gesture_iv_player_volume = (ImageView) findViewById(R.id.mn_gesture_iv_player_volume);

        gesture_progress_layout = (RelativeLayout) findViewById(R.id.mn_gesture_progress_layout);
        geture_tv_progress_time = (TextView) findViewById(R.id.mn_gesture_tv_progress_time);
        gesture_iv_progress = (ImageView) findViewById(R.id.mn_gesture_iv_progress);

        //亮度的布局
        gesture_light_layout = (RelativeLayout) findViewById(R.id.mn_gesture_light_layout);
        geture_tv_light_percentage = (TextView) findViewById(R.id.mn_geture_tv_light_percentage);

        gesture_volume_layout.setVisibility(View.GONE);
        gesture_progress_layout.setVisibility(View.GONE);
        gesture_light_layout.setVisibility(View.GONE);

        gestureDetector = new GestureDetector(getContext(), this);
        setLongClickable(true);
        gestureDetector.setIsLongpressEnabled(true);
        audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
    }

    /**
     * OnGestureListener监听
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!isPrepare) {
            return false;
        }
        initBottomMenuState();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        if (!isPrepare) {
            return false;
        }

        int FLAG = 0;

        // 横向的距离变化大则调整进度，纵向的变化大则调整音量
        if (Math.abs(distanceX) >= Math.abs(distanceY)) {
            if (videoView != null && videoView.isPlaying()) {
                FLAG = GESTURE_MODIFY_PROGRESS;
            }
        } else {
            int intX = (int) e1.getX();
            int screenWidth = PlayerUtils.getScreenWidth((Activity) context);
            if (intX > screenWidth / 2) {
                FLAG = GESTURE_MODIFY_VOLUME;
            } else {
                //左边是亮度
                FLAG = GESTURE_MODIFY_BRIGHTNESS;
            }
        }

        if (GESTURE_FLAG != 0 && GESTURE_FLAG != FLAG) {
            return false;
        }

        GESTURE_FLAG = FLAG;

        if (FLAG == GESTURE_MODIFY_PROGRESS) {
            //表示是横向滑动,可以添加快进
            // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_light_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.VISIBLE);
            try {
                if (videoView != null && videoView.isPlaying()) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                        if (distanceX >= PlayerUtils.dip2px(context, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
                            gesture_iv_progress.setImageResource(R.drawable.mn_player_backward);
                            if (videoView.getCurrentPosition() > 3 * 1000) {// 避免为负
                                int cpos = mn_seekBar.getProgress();
                                videoView.seekTo(cpos - videoView.getDuration() / 70);
                                mn_seekBar.setProgress(cpos);

                            } else {
                                //什么都不做
                                videoView.seekTo(3000);
                            }
                        } else if (distanceX <= -PlayerUtils.dip2px(context, STEP_PROGRESS)) {// 快进
                            gesture_iv_progress.setImageResource(R.drawable.mn_player_forward);
                            if (videoView.getCurrentPosition() < videoView.getDuration() - 5 * 1000) {// 避免超过总时长
                                int cpos = mn_seekBar.getProgress();
                                videoView.seekTo(cpos + videoView.getDuration() / 70);
                                mn_seekBar.setProgress(cpos);
                            }
                        }
                    }
                    String timeStr = PlayerUtils.converLongTimeToStr(videoView.getCurrentPosition()) + " / "
                            + PlayerUtils.converLongTimeToStr(videoView.getDuration());
                    geture_tv_progress_time.setText(timeStr);

                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
        else if (FLAG == GESTURE_MODIFY_VOLUME)

        {
            //右边是音量
            gesture_volume_layout.setVisibility(View.VISIBLE);
            gesture_light_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.GONE);
            currentVolume = audiomanager
                    .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                if (currentVolume == 0) {// 静音，设定静音独有的图片
                    gesture_iv_player_volume.setImageResource(R.drawable.mn_player_volume_close);
                }
                if (distanceY >= PlayerUtils.dip2px(context, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                        currentVolume++;
                    }
                    gesture_iv_player_volume.setImageResource(R.drawable.mn_player_volume_open);
                } else if (distanceY <= -PlayerUtils.dip2px(context, STEP_VOLUME)) {// 音量调小
                    if (currentVolume > 0) {
                        currentVolume--;
                        if (currentVolume == 0) {// 静音，设定静音独有的图片
                            gesture_iv_player_volume.setImageResource(R.drawable.mn_player_volume_close);
                        }
                    }
                }
                int percentage = (currentVolume * 100) / maxVolume;
                geture_tv_volume_percentage.setText(String.valueOf(percentage + "%"));
                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }
        }
        //调节亮度
        else if (FLAG == GESTURE_MODIFY_BRIGHTNESS)

        {
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_light_layout.setVisibility(View.VISIBLE);
            gesture_progress_layout.setVisibility(View.GONE);
            currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值


            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                // 亮度调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                int mLight = LightnessControl.GetLightness((Activity) context);
                if (mLight >= 0 && mLight <= 255) {
                    if (distanceY >= PlayerUtils.dip2px(context, STEP_LIGHT)) {
                        if (mLight > 245) {
                            LightnessControl.SetLightness((Activity) context, 255);
                        } else {
                            LightnessControl.SetLightness((Activity) context, mLight + 10);
                        }
                    } else if (distanceY <= -PlayerUtils.dip2px(context, STEP_LIGHT)) {// 亮度调小
                        if (mLight < 10) {
                            LightnessControl.SetLightness((Activity) context, 0);
                        } else {
                            LightnessControl.SetLightness((Activity) context, mLight - 10);
                        }
                    }
                } else if (mLight < 0) {
                    LightnessControl.SetLightness((Activity) context, 0);
                } else {
                    LightnessControl.SetLightness((Activity) context, 255);
                }
                //获取当前亮度
                int currentLight = LightnessControl.GetLightness((Activity) context);
                int percentage = (currentLight * 100) / 255;
                geture_tv_light_percentage.setText(String.valueOf(percentage + "%"));
            }
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 手势里除了singleTapUp，没有其他检测up的方法
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_progress_layout.setVisibility(View.GONE);
            gesture_light_layout.setVisibility(View.GONE);
        }
        return gestureDetector.onTouchEvent(event);
    }


    /**
     * 设置视频信息
     */
    public void setDataSource(IDataSource iDataSource, String title) {
        videoView.setDataSource(iDataSource);
        videoTitle = title;
        initViews();
    }

    public void setDataSource(String path, String title) {
        videoView.setDataSource(path);
        videoTitle = title;
        initViews();
    }
    public void setDataSource(Context context,Uri uri, String title) {
        videoView.setDataSource(context,uri);
        videoTitle = title;
        initViews();
    }

    private void resetMediaPlayer() {
        try {
            if (videoView != null) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    videoView.stop();
                }
                //重置mediaPlayer
                videoView.reset();
                //添加播放路径
                videoView.setDataSource(videoPath);
                // 准备开始,异步准备，自动在子线程中
                videoView.prepareAsync();
            } else {
                Toast.makeText(context, "播放器初始化失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频
     */
    public void startVideo() {
        if (videoView != null) {
            videoView.start();
            mn_iv_play_pause.setImageResource(R.drawable.mn_player_pause);
        }
    }

    /**
     * 暂停视频
     */
    public void pauseVideo() {
        if (videoView != null) {
            videoView.pause();
            mn_iv_play_pause.setImageResource(R.drawable.mn_player_play);
            video_position = (int) videoView.getCurrentPosition();
        }
    }

    /**
     * 获取当前播放的位置
     */
    public int getVideoCurrentPosition() {
        int position = 0;
        if (videoView != null) {
            position = (int) videoView.getCurrentPosition();
        }
        return position;
    }

    /**
     * 获取视频总长度
     */
    public int getVideoTotalDuration() {
        int position = 0;
        if (videoView != null) {
            position = (int) videoView.getDuration();
        }
        return position;
    }

    /**
     * 销毁资源
     */
    public void destroyVideo() {
        if (videoView != null) {
            videoView.stop();
            videoView.release();// 释放资源
            videoView = null;
        }
        video_position = 0;
        destroyTimeTask();
        myHandler.removeCallbacksAndMessages(null);
    }


    /**
     * OnSeekBarChangeListener监听
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //在拖动时设置音量为静音
        audiomanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        if (videoView != null && videoView.isPlaying()) {
            int maxCanSeekTo = seekBar.getMax() - 5 * 1000;
            if (seekBar.getProgress() < maxCanSeekTo) {
                videoView.seekTo(seekBar.getProgress());
            } else {
                //不能拖到最后
                videoView.seekTo(maxCanSeekTo);
            }
        }
    }
}
