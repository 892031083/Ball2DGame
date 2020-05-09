package com.example.ball2dgame;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ball2dView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    public static final int GAME_PRESTART=101;//游戏预加载
    public static final int GAME_READY=100;//游戏准备
    public static final int GAME_START=200;//游戏运行中
    public static final int GAME_STOP=500;//游戏停止
    public static final int GAME_PAUSE=400;//游戏暂停
    public static final int GAME_SUCCESS=666;//游戏通过
    public static final int MAX_SPEED=20;//最大速度20 每帧
    private MediaPlayer mediaPlayer;
    int checkpoint;//当前关卡
    public static final int SUCCESS_SCORE=5;//通关分数
    private SurfaceHolder surfaceHolder;
    private Paint mPaint;
    private Paint imgPaint;
    private Canvas mCanvas;//画布
    private int radius=40;//球半径
    private Bitmap img1,img2,img3;//兔子的bitmap资源
    public int gameTime;//游戏时间秒

    int status;//游戏状态
    DrawObj ball;//小球
    DrawObj board;//滑板
    List<DrawObj> obstacles;//障碍物集合
    DrawObj hare;//小兔子
    float gameW,gameH;//游戏区域的宽高
    float botW,botH;//滑板底部的坐标
    float botHeight=350;//地步的高度
    float v=15,vx,vy;//速度 每帧
    protected int score=0;//分数
    private boolean IsDraw=true;//游戏线程开关
    private Context context;
    private GameListener gameListener;
    public Ball2dView(Context context) {
        super(context);
        init(context);
    }

    public Ball2dView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Ball2dView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context=context;
      //  status=GAME_READY;
        surfaceHolder=getHolder();
        surfaceHolder.addCallback(this);
        mPaint=new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);//画笔属性是实心圆
        mPaint.setStrokeWidth(8);//设置画笔粗细
        mPaint.setTextSize(50);
        //vx=v;vy=v;//初始速度是5
        img1= BitmapFactory.decodeResource(context.getResources(),R.drawable.img1);
        img2= BitmapFactory.decodeResource(context.getResources(),R.drawable.img2);
        img3= BitmapFactory.decodeResource(context.getResources(),R.drawable.img3);
        ball=new DrawObj();//初始化球

    }

    public void loadCheckpoint(int checkpoint){//加载关卡
        this.checkpoint=checkpoint;
        board=new DrawObj();
        score=0;
        v=15;
        gameTime=30;
        botW=gameW;
        botH=gameH-botHeight;//滑板底部高400度
        isdrawText=false;
        board.w=200;//宽度100
        board.h=20;//高度20
        board.x=gameW/2-board.w/2;
        board.y=botH;//y坐标
        hare=new DrawObj();
        setBall();
        setObstacles();
        mediaPlayer=mediaPlayer.create(context,R.raw.up);

//        img1=getScalBitmap(img1,(float)200/img1.getWidth(),(float)300/img1.getHeight());
//        img2=getScalBitmap(img2,(float)200/img2.getWidth(),(float)300/img2.getHeight());
//        img3=getScalBitmap(img3,(float)200/img3.getWidth(),(float)300/img3.getHeight());
        createHare();//生成兔子
        if (checkpoint==2) status=GAME_READY;
    }


    public Bitmap getScalBitmap(Bitmap bitmap,float dx,float dy){//获取 dx dy缩放比例后的bitmap
        return Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*dx),(int)(bitmap.getHeight()*dy),true);

    }
    private void setObstacles() {
        if (checkpoint==1){
            obstacles=new ArrayList<>();
            DrawObj obstacleRect=new DrawObj();
            obstacleRect.x=300;
            obstacleRect.y=400;
            obstacleRect.w=150;
            obstacleRect.h=150;
            obstacles.add(obstacleRect);
            obstacles.add(new DrawObj(600,800,80));
        }else {//第二关卡
            obstacles=new ArrayList<>();
            DrawObj obstacleRect=new DrawObj();
            obstacleRect.x=500;
            obstacleRect.y=400;
            obstacleRect.w=200;
            obstacleRect.h=200;
            obstacles.add(obstacleRect);
            obstacles.add(new DrawObj(400,800,80));
        }


    }

    private void setBall() {
        ball.radius=radius;
        ball.x=board.x+board.w/2;//在板子的中间
        ball.y=board.y-ball.radius;
    }
    int imgData=1;//图片切换下表
    private void createHare() {

        switch (imgData){
            case 1:
                hare.bitmap=img1;
                break;
            case 2:
                hare.bitmap=img3;
                break;
            case 3:
                hare.bitmap=img2;
                break;
        }
        imgData=imgData==3?0:imgData+1;
        hare.w=(float) hare.bitmap.getWidth();
        hare.h=(float) hare.bitmap.getHeight();
        hare.x= (float) (1+ Math.random()*(gameW-hare.w-1+1));//随机宽度
        hare.y= (float) (1+Math.random()*(board.y-hare.h-1+1));//随机高度
        Log.i("LLLLLLLLL",hare.x+":"+hare.y);

    }

    public void setStatus(int status) {//用于activity设置失败
        this.status = status;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameW=getMeasuredWidth();
        gameH=getMeasuredHeight();
        loadCheckpoint(1);
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        IsDraw=false;
    }


    @Override
    public void run() {
        while (IsDraw){
            mCanvas=surfaceHolder.lockCanvas();
            if (mCanvas==null) return;
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
            drawMain();

            surfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void drawMain() {
      //  drawBottom();//绘制底部区域
        if (status==GAME_READY || status==GAME_START ||status==GAME_PAUSE){
            drawBall();//画个球
            drawObstacles();//画障碍物
            drawBoard();//画滑板
            moveBall();//移动球
            drawHare();//画小兔子
            drawtoText();//绘制文字
        }
        if (gameTime<=0 && status==GAME_START){
            status=GAME_STOP;
            if (gameListener!=null) gameListener.checkStatus(status);
        }
    }

    private void drawBottom() {
        mPaint.setColor(Color.parseColor("#757575"));
        mCanvas.drawRect(0,botH,botW,botH+botHeight,mPaint);
    }

    boolean isdrawText=false;
    int isTextDataTime=0;
    float istextX,istextY;//文字显示位置
    String drawtext="";
    private void drawtoText() {
        if (isdrawText && isTextDataTime<=60){//存在60帧
            isTextDataTime++;
            mPaint.setColor(Color.GRAY);
            mCanvas.drawText(drawtext,istextX,istextY,mPaint);
        }else if (isTextDataTime>60){
            isdrawText=false;
            isTextDataTime=0;
        }
    }

    private void drawHare() {
        Matrix matrix=new Matrix();
        matrix.setTranslate(hare.x,hare.y);
        mCanvas.drawBitmap(hare.bitmap,matrix,null);
        if (CollstionByHare()){//碰撞兔子 分数+1
            istextX=hare.x+hare.w/2;
            istextY=hare.y+hare.h/2;//记录原来兔子的坐标
            createHare();//重置兔子位置
            score+=1;
            drawtext="分数+1";
            startUp();
            if (gameListener!=null){
                gameListener.addScore(score);
            }
            if (score>=5){
                status=GAME_SUCCESS;
                if (gameListener!=null){
                    gameListener.checkStatus(status);
                }
            }
            isTextDataTime=0;
            isdrawText=true;//显示文字分数
        }
    }

    private void drawObstacles() {
        mPaint.setColor(Color.GREEN);
        for (DrawObj obj:obstacles){
            if (obj.radius!=0){
                mCanvas.drawCircle(obj.x,obj.y,obj.radius,mPaint);//画圆形
                CollstionByCircle(obj);
            }
            else {
                mCanvas.drawRect(obj.x,obj.y,obj.x+obj.w,obj.y+obj.h,mPaint);//画矩形
                CollstionByRect(obj);
            }
        }
    }

    private void drawBall() {//画球
        mPaint.setColor(Color.BLUE);
        mCanvas.drawCircle(ball.x,ball.y,ball.radius,mPaint);
    }

    private void drawBoard() {
        mPaint.setColor(Color.RED);
        mCanvas.drawRect(board.x,board.y,board.x+board.w,board.y+board.h,mPaint);

    }
    private void moveBoard(float x){
        if (status==GAME_START){
            //根据手势移动滑板
            board.x=(x-board.w/2)<=0?0:x-board.w/2;
            board.x=board.x+board.w>=gameW?gameW-board.w:board.x;
//            if (status==GAME_READY){//如果是游戏准备状态
//                setBall();
//            }
        }
    }
   private void moveBall(){
        if (status==GAME_START){
            ball.x-=vx;
            ball.y-=vy;
            CollstionBall(ball);
            CollstionByBoard();
        }

   }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
             //   if (status==GAME_PAUSE)
                moveBoard(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                moveBoard(event.getX());

                break;
            case MotionEvent.ACTION_UP:
                if (status==GAME_READY) {
                    status=GAME_START;
                    setBallVector(new PointF(event.getX(),event.getY()));//设置球运动方向
                }
                break;

        }
       return true;
    }

    private void setBallVector(PointF pointF) {
        float spac=getPointSpac(ball.x,ball.y,pointF.x,pointF.y);
        float time=spac/v;
        vx=Math.abs(pointF.x-ball.x)/time;
        vy=Math.abs(pointF.y-ball.y)/time;
        if (pointF.x>ball.x) vx=-vx;
        //float dx=Math.abs(pointF.x-ball.x);

    }

    private void CollstionByBoard(){
        if (ball.x-ball.radius<=board.x+board.w&& ball.x+ball.radius>=board.x){
            if (ball.y+ball.radius >= board.y && ball.y<board.y){//上册碰撞
                if (vy<0)vy=-vy;
            }else if (ball.y-ball.radius <= board.y+board.h && ball.y>board.y){
                if (vy>0){
                    vy=-vy;
                }
            }
        }
    }


    private boolean CollstionByRect(DrawObj obj){//矩形与原的碰撞

        if (ball.x+ball.radius>=obj.x && ball.x-ball.radius<=obj.x+obj.w){//上下

            if (ball.y+ball.radius>=obj.y && ball.y<obj.y+vx){//上测碰撞
                 if (vy<0)vy=-vy;
                 ball.y=obj.y-ball.radius;
                return true;
            }else if (ball.y-ball.radius<=obj.y +obj.h&& ball.y>obj.y+obj.h){//下侧
                 if (vy>0)vy=-vy;
                 ball.y=obj.y+ball.radius+obj.h;
                return true;
            }

        }  if (ball.y>=obj.y && ball.y<=obj.y+obj.h) {//左右

            if (ball.x+ball.radius>=obj.x && ball.x<obj.x){
                if (vx<0)vx=-vx;
                ball.x=obj.x-ball.radius;
                return true;
            }else if (ball.x-ball.radius<=obj.x+obj.w && ball.x>obj.x+obj.w){
                if (vx>0)vx=-vx;
                ball.x=obj.x+obj.w+ball.radius;
                return true;
            }

        }
//        //判断矩形四个顶点碰撞
//        if (getPointSpac(ball.x,ball.y,obj.x,obj.y)<=ball.radius ){ //点与圆心距离小于半径代表碰撞 与左上
//           // if ()
//
//            float dy=vy;
//            vy=vx;
//            vx=-dy;
//            return true;
//        }
//        if (getPointSpac(ball.x,ball.y,obj.x+obj.w,obj.y)<=ball.radius){//右上
//            if (ball.x<=obj.x+obj.w||ball.y>=obj.y)  return true;
//            float dy=vy;
//            vy=vx;
//            vx=-dy;
//            return true;
//        }
//        if (getPointSpac(ball.x,ball.y,obj.x,obj.y+obj.h)<=ball.radius){//左下
//            if (ball.y<=obj.y+obj.h||ball.x>=obj.x)  return true;
//            float dx=vx;
//            vx=vy;
//            vy=-dx;
//            return true;
//        }
//        if (getPointSpac(ball.x,ball.y,obj.x+obj.w,obj.y+obj.h)<=ball.radius){//右下
//            if (ball.y<=obj.y+obj.h || ball.x<=obj.x+obj.w)  return true;
//            float dx=vx;
//            vx=vy;
//            vy=-dx;
//            return true;
//        }
        return false;
    }

    private void moveByPoint(float x,float y) {

            if (Math.abs(y-ball.y)> Math.abs(x - ball.x)) {
                vy=-vy;
            }else {
                vx=-vx;
            }

    }

    private boolean CollstionByCircle(DrawObj obj){//圆 与 圆的碰撞
        float A=(obj.y-ball.y)*(obj.y-ball.y);
        float B=(obj.x-ball.x)*(obj.x-ball.x);
        float C= (float) Math.sqrt(A+B);//开方根

        if (C<=ball.radius+obj.radius){//
            //代表碰撞
            //碰撞后的动作让他 从碰撞点到圆心O 方向运动 因为两球的圆心与碰撞点三点一线 所以就是圆心1 - 圆心2的方向
            float spac=getPointSpac(ball.x,ball.y,obj.x,obj.y);
            float time=spac/v;
            vx=-(ball.x-obj.x)/time;//我们将改距离运行时间设为4帧 1: v  x:?
            vy=-(ball.y-obj.y)/time;
           return true;
        }
        return false;
    }
    //与兔子的碰撞检测:
    public boolean CollstionByHare(){
        if (ball.x<=hare.x && ball.x+ball.radius<=hare.x){//左侧不碰撞
                return false;
        }else if (ball.x>=hare.x+hare.w && ball.x-ball.radius>=hare.x+hare.w){
            return false;
        }else if (ball.y<=hare.y && ball.y+ball.radius<=hare.y){//上册不碰撞
            return false;
        }else if (ball.y>=hare.y  && ball.y-ball.radius>=hare.y+hare.h){//下侧不碰撞
            return false;
        }
        return true;//所有不碰撞要素都不满足时 那就是碰撞了
    }


    public float getPointSpac(float x,float y,float x1 ,float y1){//获取两点距离 勾股定理
        return (float) Math.sqrt((x1-x)*(x1-x)+(y1-y)*(y1-y));
    }
    private void CollstionBall(DrawObj ball){//验证碰撞边框 fuy
        if (ball.x<=ball.radius){//说明碰到边框的左侧 中心点的x坐标 <= 半径
          //  ball.Vx=-ball.Vx;
            vx=-vx;
        }
        if (ball.x+ball.radius>=getMeasuredWidth())//我们将view充满屏幕 所以getMW代表屏幕宽高, 圆心Ox > MW-半径
        {
          //  ball.Vx=-ball.Vx;
            vx=-vx;
        }
        //y上下碰撞同理 判断y
        if (ball.y<=ball.radius){//说明碰到边框的左侧 中心点的x坐标 <= 半径
          //  ball.Vy=-ball.Vy;
            vy=-vy;
        }
        if (ball.y+ball.radius>=getMeasuredHeight())//我们将view充满屏幕 所以getMW代表屏幕宽高, 圆心Ox > MW-半径
        {
            //下侧碰撞
           // ball.Vy=-ball.Vy;
            vy=-vy;
            v++;
            vx=vx>0?vx+1:vx-1;
            vy=vy>0?vy+1:vy-1;
            if (v>=MAX_SPEED){
                status=GAME_STOP;
                if (gameListener!=null){
                    gameListener.checkStatus(status);
                }
            }else {
                istextX=ball.x;
                istextY=ball.y;
                drawtext="速度+1";
                if (gameListener!=null){
                    gameListener.addSpeed(v);//通知速度
                }
                isdrawText=true;
            }

        }
    }

    public void startUp(){//播放铃音
        mediaPlayer.seekTo(200);
        mediaPlayer.start();
    }
    class DrawObj{
        float x,y;//xy坐标 如果是球 则为圆心xy坐标
        float radius=0;//半径 矩形为0
        float w,h;//如果是矩形 兔子等 为宽高
        Bitmap bitmap=null;//图片

        public DrawObj() {

        }

        public DrawObj(float x, float y, float radius) {//球的初始化
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public DrawObj(float x, float y, float w, float h) {//矩形初始化
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public DrawObj(float x, float y, float w, float h, Bitmap bitmap) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.bitmap = bitmap;
        }
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    interface GameListener{
        void addScore(int score);
        void addSpeed(float speed);
        void checkStatus(int status);
    }
}
