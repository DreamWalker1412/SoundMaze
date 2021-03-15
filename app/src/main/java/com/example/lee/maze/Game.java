package com.example.lee.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;
import java.util.Stack;
/*
* canvas.drawArc ()//（扇形）
canvas.drawCircle()//（圆）
canvas.drawOval()//（椭圆）
canvas.drawLine()//（线）
canvas.drawPoint()//（点）
canvas.drawRect()//（矩形）
canvas.drawRoundRect()//（圆角矩形）
canvas.drawVertices()//（顶点）(不了解)
canvas.drawPath()//（路径）
canvas.drawBitmap()// （位图）
canvas.drawText()// （文字）
*
* 1 白色 　 #FFFFFFFF
　　2 红色 　　 #FFFF0000
　　3 绿色 　　 #FF00FF00
　　4 蓝色 　　 #FF0000FF
　　5 牡丹红 　　　#FFFF00FF
　　6 青色 　　 #FF00FFFF
　　7 黄色 　　　　#FFFFFF00
　　8 黑色 　　　　　#FF000000
　　9 海蓝 　　　　#FF70DB93
　　10 巧克力色 　　　#FF5C3317
　　11 蓝紫色 　　　 #FF9F5F9F
　　12 黄铜色 　　　　 #FFB5A642
　　13 亮金色 　　　　 #FFD9D919
　　14 棕色 　　　　　　#FFA67D3D
* */

public class Game extends AppCompatActivity implements OnInitListener {
    public int widths;
    public Button btn_top, btn_down, btn_left, btn_right, btn_play, btn_pause;//声明按钮
    private long firstTime1 = 0;
    private long firstTime2 = 0;
    private int count=0;
    private TextToSpeech tts;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//注册并产生窗口
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        tts = new TextToSpeech(this, this);
        MyApplication.getInstance().addActivity(this);
        DisplayMetrics metric = new DisplayMetrics();//获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(metric);//获取屏幕信息
        widths = metric.widthPixels;// 屏幕宽度（像素）
        final FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        mMediaPlayer=MediaPlayer.create(Game.this, R.raw.background_music);
        mMediaPlayer.setLooping(true);  //循环播放
        btn_top = (Button)findViewById(R.id.top);//得到控件，并类型转换
        btn_down = (Button)findViewById(R.id.down);
        btn_left = (Button)findViewById(R.id.left);
        btn_right = (Button)findViewById(R.id.right);
        btn_play = (Button)findViewById(R.id.btn_play);
        btn_pause = (Button)findViewById(R.id.btn_pause);
        final MyView myView = new MyView(this);//不能再指向其他对象
        frameLayout.addView(myView);
        btn_left.setOnClickListener(new View.OnClickListener() {//单击左按钮
            @Override
            public void onClick(View v) {
                myView.move(1);
                myView.postInvalidate();
                myView.checkIsWin();
            }
        });
        btn_right.setOnClickListener(new View.OnClickListener() {//单击右按钮
            @Override
            public void onClick(View v) {
                myView.move(2);
                myView.postInvalidate();
                myView.checkIsWin();
            }
        });
        btn_top.setOnClickListener(new View.OnClickListener() {//单击上按钮
            @Override
            public void onClick(View v) {
                myView.move(3);
                myView.postInvalidate();
                myView.checkIsWin();
            }
        });
        btn_down.setOnClickListener(new View.OnClickListener() {//单击下按钮
            @Override
            public void onClick(View v) {
                myView.move(4);
                myView.postInvalidate();
                myView.checkIsWin();
            }
        });

        //监听音频播放完的代码，实现音频的自动循环播放


        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mMediaPlayer.isLooping();
                }
            }
        });
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    //   super.onBackPressed();  //注释掉这行,back键不退出当前界面

    }
    @Override
    public void onDestroy(){
        if (mMediaPlayer!= null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
        }
        super.onDestroy();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //当按下的是后退键并且是抬起的动作
        if (keyCode== KeyEvent.KEYCODE_BACK&& event.getAction()== KeyEvent.ACTION_UP){
            //获取系统时间
            long secondTime= System.currentTimeMillis();
            //两次点击的间隔大于2s，则弹出Toast，并且把第二次点击的时间赋给第一次点击的时间变量，间隔小于2s则退出应用
            if(secondTime- firstTime1> 2000){
                Toast.makeText(Game.this, "再按一次返回键返回上一界面", Toast.LENGTH_SHORT).show();
                tts.speak("再按一次返回键返回上一界面", TextToSpeech.QUEUE_ADD, null);
                firstTime1= secondTime;
                return true;
            }else{
                this.finish();
                System.exit(0);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        //点击屏幕
        if (event.getAction()== KeyEvent.ACTION_DOWN){
            //获取系统时间
            long secondTime= System.currentTimeMillis();
        //三次点击的间隔小于1.5s，则退出应用
        if(secondTime- firstTime2 < 1500||count==0){
            //Toast.makeText(Game.this, "再点击一次屏幕退出程序", Toast.LENGTH_SHORT).show();
            //tts.speak("再点击一次屏幕退出程序", TextToSpeech.QUEUE_ADD, null);
            firstTime2= secondTime;
            count+=1;

        }else{
            count = 0;
        }

        if(count == 3){
            MyApplication.getInstance().finish_activity(this);
        }
    }
        return super.onTouchEvent(event);
    }
    @Override
    public void onInit(int status){
        // 判断是否转化成功
        if (status == TextToSpeech.SUCCESS){
            //默认设定语言为中文
            int result = tts.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(Game.this, "语音读取失败", Toast.LENGTH_SHORT).show();
            }else{
                //不支持中文就将语言设置为英文
                tts.setLanguage(Locale.US);
            }
        }
    }
    protected void onStop() {
        super.onStop();
        tts.stop(); // 不管是否正在朗读TTS都被打断
        tts.shutdown(); // 关闭，释放资源
    }



    public class MyView extends View {
        public int NUM = 14;//最初界面是20行
        public int Gain=2;//每次获胜后难度增益为2
        public int IsWinFlag=0;//获胜标志，用于增益
        public int padding = 10, width = (widths -2*padding)/NUM;// width 每个格子的宽度和高度，padding格子距离屏幕边缘的距离
        public com.example.lee.maze.Lattice[][] maze;//格子的二位数组地图
        public int ballX, ballY;//玩家的位置
        public boolean drawPath = false;

        public Paint paint;
        public MyView(Context context) {
            super(context);
            init();
        }
        public void init() {
            paint = new Paint();
            paint.setAntiAlias(true);//设置抗锯齿
            paint.setColor(0xFF000000);//黑色
            paint.setStyle(Paint.Style.STROKE);//空心
            paint.setStrokeWidth(2);//圆环宽度
            if(IsWinFlag==1){//胜利则难度加2
                NUM+=2;
                IsWinFlag=0;
            }
            maze = new com.example.lee.maze.Lattice[NUM][NUM];//地图的大小
            //对格子初始化
            for (int i = 0; i <= NUM - 1; i++)
                for (int j = 0; j <= NUM - 1; j++)
                    maze[i][j] = new com.example.lee.maze.Lattice(i, j);
            for (int i = 0; i <= NUM - 1; i++)
                for (int j = 0; j <= NUM - 1; j++) {
                    maze[i][j].setFather(null);
                    maze[i][j].setFlag(com.example.lee.maze.Lattice.NOTINTREE);//初始时每个节点都没有父节点且数值为0
                }
            ballX = 0;
            ballY = 0;
            drawPath = false;
            createMaze();
        }

        public void createMaze() {
            Random random = new Random();//随机种子
            int rx = Math.abs(random.nextInt()) % NUM;//随机取一个点作为根
            int ry = Math.abs(random.nextInt()) % NUM;
            Stack<com.example.lee.maze.Lattice> s = new Stack<com.example.lee.maze.Lattice>();//此处使用了BFS思想,此时使用的Stack
            com.example.lee.maze.Lattice p = maze[rx][ry];	//先随机确定一个根，然后利用广度优先搜索的思想对整个图进行遍历，转换成树
            com.example.lee.maze.Lattice neis[] = null;	//网格
            s.push(p);//将随机确定的结点作为树的初始根节点压入栈中，在取出栈内元素时要将
            while (!s.isEmpty()) {//广度优先遍历进行构造迷宫
                p = s.pop();
                p.setFlag(com.example.lee.maze.Lattice.INTREE);
                neis = getNeis(p);//得到此节点的周围四个节点的坐标
                int ran = Math.abs(random.nextInt()) % 4;
                for (int a = 0; a <= 3; a++) {
                    ran++;
                    ran %= 4;
                    if (neis[ran] == null || neis[ran].getFlag() == com.example.lee.maze.Lattice.INTREE)	//对周边的结点遍历，存在且未访问过才将其压入栈中
                        continue;
                    s.push(neis[ran]);
                    neis[ran].setFather(p);//记录周围的子节点
                }
            }
        }
        //得到此节点的周围四个节点的坐标
        public com.example.lee.maze.Lattice[] getNeis(com.example.lee.maze.Lattice p) {
            final int[] adds = {-1, 0, 1, 0, -1};
            if (isOutOfBorder(p)) {
                return null;
            }
            com.example.lee.maze.Lattice[] ps = new com.example.lee.maze.Lattice[4];
            int xt;
            int yt;
            for (int i = 0; i <= 3; i++) {
                xt = p.getX() + adds[i];
                yt = p.getY() + adds[i + 1];
                if (isOutOfBorder(xt, yt))
                    continue;
                ps[i] = maze[xt][yt];
            }
            return ps;
        }
        public void move(int c) {
            int tx = ballX, ty = ballY;
            switch (c) {
                case 1 :
                    ty--;
                    break;
                case 2 :
                    ty++;
                    break;
                case 3 :
                    tx--;
                    break;
                case 4 :
                    tx++;
                    break;
                case 5 :
                    if (drawPath == true) {
                        drawPath = false;
                    } else {
                        drawPath = true;
                    }
                    break;
                default :
            }
            //检查是否联通且未越界
            if (!isOutOfBorder(tx, ty) && (maze[tx][ty].getFather() == maze[ballX][ballY]
                    || maze[ballX][ballY].getFather() == maze[tx][ty])) {
                ballX = tx;
                ballY = ty;
            }
        }
        public boolean isOutOfBorder(com.example.lee.maze.Lattice p) {
            return isOutOfBorder(p.getX(), p.getY());
        }
        public boolean isOutOfBorder(int x, int y) {
            return (x > NUM - 1 || y > NUM - 1 || x < 0 || y < 0) ? true : false;
        }

        public int getCenterX(int x) {
            return padding + x * width + width / 2;
        }
        public int getCenterY(int y) {
            return padding + y * width + width / 2;
        }

        private void checkIsWin() {
            if (ballX == NUM - 1 && ballY == NUM - 1) {//如果球的位置在坐标点(29,29)时则找到出口
                Toast.makeText(Game.this,"YOU WIN",Toast.LENGTH_SHORT).show();//弹出结束信息
                IsWinFlag=1;
                init();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {//先绘画格子，然后
            super.onDraw(canvas);
            canvas.drawColor(0xFFffffff);
            //从上到下画线
            for (int i = 0; i <= NUM; i++) {
                canvas.drawLine(padding + i * width, padding, padding + i * width,
                        padding + NUM * width, paint);
            }
            //从左到右画线
            for (int j = 0; j <= NUM; j++) {
                canvas.drawLine(padding, padding + j * width, padding + NUM * width,
                        padding + j * width, paint);
            }

            paint.setColor(0xffffffff);//白色

            for (int i = NUM - 1; i >= 0; i--) {
                for (int j = NUM - 1; j >= 0; j--) {
                    com.example.lee.maze.Lattice f = maze[i][j].getFather();
                    if (f != null) {
                        int fx = f.getX(), fy = f.getY();	//获取父节点的坐标
                        clearFence(i, j, fx, fy, canvas);//将子孩子到父节点的边变为白色
                    }
                }
            }
            //入口颜色置白
            canvas.drawLine(padding, padding + 1, padding, padding + width - 1, paint);
            int last = padding + NUM * width;
            //出口
            canvas.drawLine(last, last - 1, last, last - width + 1, paint);

            //画玩家，及小球为红色
            paint.setColor(0xFFff0000);//red
            paint.setStyle(Paint.Style.FILL);
            float cx = getCenterX(ballY) - width / 3 + padding;
            float cy = getCenterY(ballX) - width / 3 + padding;
            float ra = width / 2;
            canvas.drawCircle(cx, cy, ra, paint);
        }
        //
        public void clearFence(int i, int j, int fx, int fy, Canvas canvas) {
            int sx = padding + ((j > fy ? j : fy) * width),//取出两个点中相对于右下角的点的坐标
                    sy = padding + ((i > fx ? i : fx) * width),
                    dx = (i == fx ? sx : sx + width),//如果和父结点在同一列,取右下角的x;不在，右下角x + "1"加上一个单位长度
                    dy = (i == fx ? sy + width : sy);//...                        y + "1"           y   (右移还是下移)
            if (sx != dx) {
                sx++;
                dx--;
            } else {
                sy++;
                dy--;
            }
            canvas.drawLine(sx, sy, dx, dy, paint);
        }
    }
}

