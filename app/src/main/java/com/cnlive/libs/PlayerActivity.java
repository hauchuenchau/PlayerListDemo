package com.cnlive.libs;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.cnlive.libs.player.MyMediaPlayer;

/**
 * 播放视频
 */
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    private MyMediaPlayer cnVideoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);
        //初始化
        initPlayer();
    }

    private void initPlayer() {
        cnVideoPlayer = (MyMediaPlayer) findViewById(R.id.mn_videoplayer);
        //播放视连通视频资源
        cnVideoPlayer.setDataSource(PlayerActivity.this, Uri.parse(getIntent().getStringExtra("path")),getIntent().getStringExtra("title"));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PlayerActivity.this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cnVideoPlayer.pauseVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cnVideoPlayer.startVideo();
    }

    @Override
    protected void onDestroy() {
        //一定要记得销毁View
        if (cnVideoPlayer != null) {
            cnVideoPlayer.destroyVideo();
            cnVideoPlayer = null;
        }
        super.onDestroy();
    }

}
