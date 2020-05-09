package com.example.ball2dgame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
     Ball2dView ball2dView;
     LinearLayout gamesoard;
     Button startBtn,outBtn,btn;
     TextView text;//游戏提示文本
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        text=findViewById(R.id.text);
        ball2dView=findViewById(R.id.gameView);
        ball2dView.setStatus(Ball2dView.GAME_PRESTART);//设置游戏状态为预开始
        ball2dView.setGameListener(new Ball2dView.GameListener() {//游戏状态监听
            @Override
            public void addScore(final int score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.score)).setText("分数: "+score+"/"+Ball2dView.SUCCESS_SCORE);
                    }
                });
            }

            @Override
            public void addSpeed(final float speed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.speed)).setText("速度: "+speed+"/帧");
                    }
                });
            }

            @Override
            public void checkStatus(final int status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status==Ball2dView.GAME_STOP){
                            text.setTextColor(Color.RED);
                            text.setText("游戏失败");
                            startBtn.setText("重新开始");
                        }else if (status==Ball2dView.GAME_SUCCESS){
                            text.setTextColor(Color.GREEN);
                            text.setText("恭喜");
                            startBtn.setText("进入下一关");
                        }
                       showSoard();
                    }
                });
            }
        });
        initGamesoard();
        initTimer();
    }



    private void initGamesoard() {
         gamesoard=findViewById(R.id.gameboard);
        gamesoard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;//占用
            }
        });
        startBtn=findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        outBtn=findViewById(R.id.out);
        outBtn.setOnClickListener(this);
        btn=findViewById(R.id.btn);
        btn.setOnClickListener(this);
   //     findViewById(R.id.start).setOnClickListener(this);
    }
    private void showSoard(){
        gamesoard.setVisibility(View.VISIBLE);
        Animation animation= AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.anim_start_activity);
        gamesoard.setAnimation(animation);
        animation.start();
    }
    private void hideSoard(){
        ((TextView)findViewById(R.id.scenes)).setText("关卡: "+ball2dView.checkpoint+"/2");
        ((TextView)findViewById(R.id.score)).setText("分数: "+ball2dView.score+"/5");
        ((TextView)findViewById(R.id.speed)).setText("速度: "+ball2dView.v+"/帧");
        ((TextView)findViewById(R.id.time)).setText("时间: "+ball2dView.gameTime);

        gamesoard.setVisibility(View.GONE);
        Animation animation= AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.anim_out_activity);
        gamesoard.setAnimation(animation);
        animation.start();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                if (ball2dView.status==Ball2dView.GAME_PRESTART){
                    //开始
                    ball2dView.setStatus(Ball2dView.GAME_READY);

                }else if (ball2dView.status==Ball2dView.GAME_SUCCESS){//进入下一关
                    ball2dView.loadCheckpoint(2);
                    ball2dView.setStatus(Ball2dView.GAME_READY);
                }else if (ball2dView.status==Ball2dView.GAME_STOP){
                    ball2dView.loadCheckpoint(1);
                    ball2dView.setStatus(Ball2dView.GAME_READY);
                }else if (ball2dView.status==Ball2dView.GAME_PAUSE){
                    ball2dView.status=Ball2dView.GAME_START;//返回游戏
                }
                hideSoard();
                break;
            case R.id.out:
                finish();
                break;
            case R.id.btn:
                showSoard();
                startBtn.setText("返回游戏");
                text.setTextColor(Color.WHITE);
                text.setText("暂停!");
                ball2dView.status=Ball2dView.GAME_PAUSE;
                break;
        }
    }
    private void initTimer() {//定时器
        final Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ball2dView.gameTime>0&&ball2dView.status==Ball2dView.GAME_START){
                    ball2dView.gameTime--;
                    ((TextView)findViewById(R.id.time)).setText("时间: "+ball2dView.gameTime);
                }
                handler.postDelayed(this,1000);
            }
        },0);
    }

}
