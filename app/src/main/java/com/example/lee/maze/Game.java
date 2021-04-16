package com.example.lee.maze;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import static com.example.lee.maze.Lattice.findShortPath;

/**
    1 白色 　 #FFFFFFFF
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
**/

public class Game extends AppCompatActivity implements OnInitListener {
    public float width;
    public Button btn_top, btn_down, btn_left, btn_right, btn_hint, btn_pause;
    private long time = 0;
    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    private MediaPlayer effectMediaPlayer;
    private MediaPlayer hintMediaPlayer1;
    private MediaPlayer hintMediaPlayer2;
    private MediaPlayer hintMediaPlayer3;
    private MediaPlayer hintMediaPlayer4;
    private MediaPlayer wordMediaPlayer;
    private int stageId = 0;
    private int count = 1;
    public double bias = 0.1;
    public Boolean isHard = true;
    private float downX ;    //手指按下时X坐标
    private float downY ;    //手指按下时Y坐标
    private Boolean mediaCanPlaying =false;
    private Boolean isPause = false;
    private Boolean abilityOn = false;
    private Boolean isFinish = false;
    MyView myView;

    //注册并产生窗口
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        MyApplication.getInstance().addActivity(this);

        //获取屏幕信息
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels;

        // TextToSpeech插件支持
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setPitch(0.9f);
        textToSpeech.setSpeechRate(1.1f);

        // 背景音乐播放器
        mediaPlayer =MediaPlayer.create(Game.this, R.raw.the_call);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        if (mediaPlayer.isPlaying())
            mediaCanPlaying =true;

        // 音效播放器
        effectMediaPlayer = MediaPlayer.create(Game.this, R.raw.piano1);
        effectMediaPlayer.setLooping(false);

        // 提示播放器
        hintMediaPlayer1 = MediaPlayer.create(Game.this, R.raw.rain_moderate);
        hintMediaPlayer1.setLooping(true);
        hintMediaPlayer2 = MediaPlayer.create(Game.this,R.raw.surf_brilliant);
        hintMediaPlayer2.setLooping(true);
        hintMediaPlayer3 = MediaPlayer.create(Game.this,R.raw.thunder_resonant);
        hintMediaPlayer3.setLooping(true);
        hintMediaPlayer4 = MediaPlayer.create(Game.this,R.raw.deciduous_forest_whippoorwill);
        hintMediaPlayer4.setLooping(true);

        // 组件id查找
        btn_top = findViewById(R.id.top);
        btn_down = findViewById(R.id.down);
        btn_left = findViewById(R.id.left);
        btn_right = findViewById(R.id.right);
        btn_hint = findViewById(R.id.btn_hint);
        btn_pause = findViewById(R.id.btn_pause);

        // 设置VIEW
        final FrameLayout frameLayout = findViewById(R.id.frameLayout);
        myView = new MyView(this);//不能再指向其他对象
        frameLayout.addView(myView);



        btn_left.setOnClickListener(new View.OnClickListener() {//单击左按钮
            @Override
            public void onClick(View v) {
                if (isFinish)
                    return;
                myView.move(1);
                myView.postInvalidate();
            }
        });
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinish)
                    return;
                if (! myView.checkIsWin())
                    myView.move(2);
                myView.postInvalidate();
            }
        });
        btn_top.setOnClickListener(new View.OnClickListener() {//单击上按钮
            @Override
            public void onClick(View v) {
                if (isFinish)
                    return;
                myView.move(3);
                myView.postInvalidate();
            }
        });
        btn_down.setOnClickListener(new View.OnClickListener() {//单击下按钮
            @Override
            public void onClick(View v) {
                if (isFinish)
                    return;
                myView.move(4);
                myView.postInvalidate();
            }
        });
        btn_hint.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isFinish)
                    return true;
                myView.showHint();
                return true;
            }
        });
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinish)
                    return;
                if (myView.isLose()) {
                    showDialogLose();
                    return;
                }
                if ( mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                if ( hintMediaPlayer1.isPlaying())
                    hintMediaPlayer1.pause();
                if ( hintMediaPlayer2.isPlaying())
                    hintMediaPlayer2.pause();
                if ( hintMediaPlayer3.isPlaying())
                    hintMediaPlayer3.pause();
                if ( hintMediaPlayer4.isPlaying())
                    hintMediaPlayer4.pause();
                isPause = true;
                showDialogPause();
            }
        });
    }

    @Override
    public void onBackPressed() {
    //   super.onBackPressed();  //注释掉这行,back键不退出当前界面
    }

    // 退出时释放资源
    @Override
    public void onDestroy(){
        if ( mediaPlayer != null) {
            if ( mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    // 关闭播放器
    public void closeMedia(MediaPlayer mediaplayer) {
        if (mediaplayer != null) {
            if (mediaplayer.isPlaying()) {
                mediaplayer.stop();
            }
            mediaplayer.release();
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //当按下的是后退键并且是抬起的动作
        if (keyCode== KeyEvent.KEYCODE_BACK && event.getAction()== KeyEvent.ACTION_UP){
            //获取系统时间
            long secondTime= System.currentTimeMillis();
            //两次点击的间隔大于2s，则弹出Toast，并且把第二次点击的时间赋给第一次点击的时间变量，间隔小于2s则退出应用
            if(secondTime- time > 2000){
                Toast.makeText(Game.this, "再按一次返回键退出游戏", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("再按一次返回键退出游戏", TextToSpeech.QUEUE_ADD, null);
                time = secondTime;
                return true;
            }else{
                MyApplication.getInstance().finishActivity(this);
                System.exit(0);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        String action = "";
        float x= event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //将按下时的坐标存储
                downX = x;
                downY = y;
                Log.e("Tag","=======按下时X："+x);
                Log.e("Tag","=======按下时Y："+y);
                break;
            case MotionEvent.ACTION_UP:
                Log.e("Tag","=======抬起时X："+x);
                Log.e("Tag","=======抬起时Y："+y);

                //获取到距离差
                float dx= x-downX;
                float dy = y-downY;
                //防止是按下也判断
                if (Math.abs(dx)>3&&Math.abs(dy)>3) {
                    //通过距离差判断方向
                    int orientation = getOrientation(dx, dy);
                    switch (orientation) {
                        case 'r':
                            if (! myView.checkIsWin())
                                myView.move(2);
                            myView.postInvalidate();
                            action = "右";
                            break;
                        case 'l':
                            myView.move(1);
                            myView.postInvalidate();
                            action = "左";
                            break;
                        case 't':
                            myView.move(3);
                            myView.postInvalidate();
                            action = "上";
                            break;
                        case 'b':
                            myView.move(4);
                            myView.postInvalidate();
                            action = "下";
                            break;
                    }
                    // Toast.makeText(Game.this, "向" + action + "滑动", Toast.LENGTH_SHORT).show();
                }
                break;

        }

        return super.onTouchEvent(event);
    }

    private int getOrientation(float dx, float dy) {
        Log.e("Tag","========X轴距离差："+dx);
        Log.e("Tag","========Y轴距离差："+dy);
        if (Math.abs(dx)>Math.abs(dy)){
            //X轴移动
            return dx>0?'r':'l';
        }else{
            //Y轴移动
            return dy>0?'b':'t';
        }
    }


    // TextToSpeech 初始化
    @Override
    public void onInit(int status){
        // 判断是否转化成功
        if (status == TextToSpeech.SUCCESS){
            //默认设定语言为中文
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(Game.this, "语音读取失败", Toast.LENGTH_SHORT).show();
            }else{
                //不支持中文就将语言设置为英文
                textToSpeech.setLanguage(Locale.US);
            }
        }
    }
    protected void onStop() {
        super.onStop();
        textToSpeech.stop(); // 不管是否正在朗读textToSpeech都被打断
        textToSpeech.shutdown(); // 关闭，释放资源
    }

    // 显示提示对话框
    public void showDialogHint(String message){
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    // 显示失败时选择对话框
    public void showDialogLose(){
        new AlertDialog.Builder(this)
                .setTitle("选择")
                .setMessage("关卡时间已耗尽，要继续尝试吗？")
                .setPositiveButton("继续游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mediaPlayer.start();
                    }
                })
                .setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        MyApplication.getInstance().finishActivity(Game.this);
                        System.exit(0);
                    }
                })
                .show();
    }

    // 显示获胜对话框
    public void showDialogWin(){
        new AlertDialog.Builder(this)
                .setTitle("胜利！")
                .setMessage("感谢您的时间，希望玩的开心!")
                .setPositiveButton("退出游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        MyApplication.getInstance().finishActivity(Game.this);
                        System.exit(0);
                    }
                })
                .show();
    }

    // 显示暂停对话框
    public void showDialogPause() {
        new AlertDialog.Builder(this)
                .setTitle("游戏已暂停")
                .setMessage("准备好了吗？ 随时可以开始！")
                .setPositiveButton("继续游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        isPause = false;
                        mediaPlayer.start();
                        if (abilityOn) {
                            mediaPlayer.setVolume(0.15f, 0.15f);
                            myView.soundHint();
                        }
                    }
                })
                .show();
    }

    // 游戏界面设置
    public class MyView extends View {
        public int mazeSize = 8;//最初界面是8行
        public int gain = 1;//每次获胜后难度增益为1
        public int IsWinFlag=0;//获胜标志，用于增益
        public double padding = 10, width = (Game.this.width -2 * padding) / mazeSize;// width 每个格子的宽度和高度，padding格子距离屏幕边缘的距离
        public Lattice[][] maze;//格子的二位数组地图
        public int ballX, ballY;//玩家的位置
        public Paint paint;
        public MyView(Context context) {
            super(context);
            initialize();
        }



        // 初始化
        public void initialize() {
            paint = new Paint();
            paint.setAntiAlias(true);//设置抗锯齿
            paint.setColor(0xFF000000);//黑色
            paint.setStyle(Paint.Style.STROKE);//空心
            paint.setStrokeWidth(2);//圆环宽度
            if(IsWinFlag==1){//胜利则难度加1
                count++;
                if (mazeSize<14)
                    mazeSize += gain;
                if ( count % 4 == 0) {
                    closeMedia(mediaPlayer);
                    closeMedia(wordMediaPlayer);
                    stageId++;
                    switch (stageId) {
                        case 0:
                            mediaPlayer = MediaPlayer.create(Game.this, R.raw.the_call);
                            break;
                        case 1:
                            wordMediaPlayer = MediaPlayer.create(Game.this, R.raw.stage_word1);
                            wordMediaPlayer.setLooping(false);
                            wordMediaPlayer.start();
                            // textToSpeech.speak("似乎边界模糊起来了？.. 是我眼花了吗？", TextToSpeech.QUEUE_ADD, null);
                            mediaPlayer = MediaPlayer.create(Game.this, R.raw.crazy_moonlit_night);
                            break;
                        case 2:
                            wordMediaPlayer = MediaPlayer.create(Game.this, R.raw.stage_word2);
                            wordMediaPlayer.setLooping(false);
                            wordMediaPlayer.start();
                            // textToSpeech.speak("这点难度对于你而言不算什么.. 对吧？..", TextToSpeech.QUEUE_ADD, null);
                            mediaPlayer = MediaPlayer.create(Game.this, R.raw.beauty_under_the_moon);
                            closeMedia(hintMediaPlayer1);
                            closeMedia(hintMediaPlayer2);
                            closeMedia(hintMediaPlayer3);
                            closeMedia(hintMediaPlayer4);
                            hintMediaPlayer1 = MediaPlayer.create(Game.this, R.raw.tropical_forest_stream);
                            hintMediaPlayer1.setLooping(true);
                            hintMediaPlayer2 = MediaPlayer.create(Game.this,R.raw.wind_mod_strong);
                            hintMediaPlayer2.setLooping(true);
                            hintMediaPlayer3 = MediaPlayer.create(Game.this,R.raw.wetland_cricket_frog);
                            hintMediaPlayer3.setLooping(true);
                            hintMediaPlayer4 = MediaPlayer.create(Game.this,R.raw.prairie_night_wind_coyote);
                            hintMediaPlayer4.setLooping(true);
                            break;
                        case 3:
                            wordMediaPlayer = MediaPlayer.create(Game.this, R.raw.stage_word3);
                            wordMediaPlayer.setLooping(false);
                            wordMediaPlayer.start();
                            // textToSpeech.speak("变得有趣起来了.. 别忘了你还有音感能力..", TextToSpeech.QUEUE_ADD, null);
                            mediaPlayer = MediaPlayer.create(Game.this, R.raw.flower_under_the_moon);
                            break;
                        case 4:
                            wordMediaPlayer = MediaPlayer.create(Game.this, R.raw.stage_word4);
                            wordMediaPlayer.setLooping(false);
                            wordMediaPlayer.start();
                            // textToSpeech.speak("这是最后的舞台了.. 你会永远的迷失吗？..", TextToSpeech.QUEUE_ADD, null);
                            mediaPlayer = MediaPlayer.create(Game.this, R.raw.the_wind_cloud_earth);
                            break;
                    }
                    mediaPlayer.start();
                    if (abilityOn) {
                        mediaPlayer.setVolume(0.05f,0.05f);
                        soundHint();
                    }
                    count = 1;
                }
                if ( stageId <4 )
                    Toast.makeText(Game.this,"GOOD JOB, GO TO STAGE "+ (stageId +1) +"-"+count+"!",Toast.LENGTH_SHORT).show();
                else if ( stageId == 4)
                    Toast.makeText(Game.this,"WELCOME TO THE FINAL STAGE !",Toast.LENGTH_LONG).show();
                width = (Game.this.width -2*padding)/ mazeSize;
                IsWinFlag=0;
            }
            maze = new Lattice[mazeSize][mazeSize];//地图的大小
            //对格子初始化
            for (int i = 0; i <= mazeSize - 1; i++)
                for (int j = 0; j <= mazeSize - 1; j++)
                    maze[i][j] = new Lattice(i, j);
            for (int i = 0; i <= mazeSize - 1; i++)
                for (int j = 0; j <= mazeSize - 1; j++) {
                    maze[i][j].setFather(null);
                    maze[i][j].setFlag(Lattice.NOTINTREE);//初始时每个节点都没有父节点且数值为0
                }
            ballX = 0;
            ballY = 0;
            createMaze();
        }

        // 创建随机迷宫算法
        public void createMaze() {
            Boolean isCheck = false;
            int tryTimes = 0;       // 限制失败次数
            Lattice[][] tempMaze = maze;
            while (!isCheck && tryTimes<100) {
                Random random = new Random();//随机种子
                // int rootX = Math.abs(random.nextInt()) % mazeSize;
                // int rootY = Math.abs(random.nextInt()) % mazeSize;
                int rootX = mazeSize - 1;
                int rootY = mazeSize - 1;
                Stack<Lattice> stack = new Stack<Lattice>();//此处使用了BFS思想,此时使用的Stack
                Lattice p = tempMaze[rootX][rootY];    //确定迷宫出口为根，然后利用广度优先搜索的思想对整个图进行遍历，转换成树
                Lattice[] neighbours;    //网格
                stack.push(p);//将随机确定的结点作为树的初始根节点压入栈中，在取出栈内元素时要将
                while (!stack.isEmpty()) {//广度优先遍历进行构造迷宫
                    p = stack.pop();
                    p.setFlag(Lattice.INTREE);
                    neighbours = getNeighbours(p);//得到此节点的周围四个节点的坐标
                    ArrayList<Integer> list = randomRange(4);
                    for (int i = 0; i <= 3; i++) {
                        int randomNum = list.get(i);
                        if ( neighbours[randomNum] == null || neighbours[randomNum].getFlag() == Lattice.INTREE )    //对周边的结点遍历，存在且未访问过才将其压入栈中
                            continue;
                        stack.push(neighbours[randomNum]);
                        neighbours[randomNum].setFather(p);//记录周围的子节点
                    }
                }
                isCheck = passCheck(tempMaze);
                tryTimes++;
            }
            maze = tempMaze;
        }

        // 生成n范围内的随机数组
        public ArrayList<Integer> randomRange(int range) {
            Random random = new Random();
            ArrayList<Integer> list = new ArrayList<>();
            boolean[] isExist = new boolean[range];
            while (list.size()<range) {
                int num = random.nextInt(4);
                if (!isExist[num]) {
                    list.add(num);
                    isExist[num]=true;
                }
            }
            return list;
        }

        // 检查迷宫是否合适
        public Boolean passCheck(Lattice[][] maze) {
            Vector vector = findShortPath(maze,0,0);

            if (vector.size()> 0.3*mazeSize*mazeSize) {
                return true;
            }
            else
                return false;
        }

        // 得到此节点的周围节点的坐标
        public Lattice[] getNeighbours( Lattice p) {
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

        // 移动操作
        public void move(int c) {
            if (isPause) {
                showDialogPause();
                // textToSpeech.speak("游戏已暂停.. 准备好了吗.. 点击继续按钮随时可以开始游戏！", TextToSpeech.QUEUE_ADD, null);
                return;
            }

            if (isLose()) {
                showDialogLose();
                return;
            }

            int tx = ballX, ty = ballY;
            switch (c) {
                case 1:
                    ty--;
                    break;
                case 2:
                    ty++;
                    break;
                case 3:
                    tx--;
                    break;
                case 4:
                    tx++;
                    break;
                default:
            }

            //检查是否联通且未越界
            if ( !isOutOfBorder(tx, ty) && (maze[tx][ty].getFather() == maze[ballX][ballY]
                    || maze[ballX][ballY].getFather() == maze[tx][ty]) ) {
                ballX = tx;
                ballY = ty;
                if (abilityOn) {
                    soundHint();
                }
                // 提示音
                // closeMedia(effectMediaPlayer);
                //  effectMediaPlayer = MediaPlayer.create(Game.this, R.raw.piano34);
                // effectMediaPlayer.start();
            } else {
                Vibrator vibrator = (Vibrator) Game.this.getSystemService(VIBRATOR_SERVICE);
                if ( vibrator != null ) {
                    vibrator.vibrate(200);
                }
            }

        }

        // 判断是否出界
        public boolean isOutOfBorder( Lattice p) {
            return isOutOfBorder(p.getX(), p.getY());
        }
        public boolean isOutOfBorder(int x, int y) {
            return x > mazeSize - 1 || y > mazeSize - 1 || x < 0 || y < 0;
        }
        public double getCenterX(int x) {
            return padding + x * width + width / 2;
        }
        public double getCenterY(int y) {
            return padding + y * width + width / 2;
        }

        // 判断关卡胜利条件
        private Boolean checkIsWin() {
            if ( ballX == mazeSize -1  && ballY == mazeSize -1  ) {
                IsWinFlag = 1;
                closeMedia(effectMediaPlayer);
                effectMediaPlayer = MediaPlayer.create(Game.this,R.raw.piano1);
                effectMediaPlayer.start();
                if (bias < 1.5)
                    bias +=0.1;
                if ( stageId ==4 ) {
                    showDialogWin();
                    closeMedia(hintMediaPlayer1);
                    closeMedia(hintMediaPlayer2);
                    closeMedia(hintMediaPlayer3);
                    closeMedia(hintMediaPlayer4);
                    closeMedia(wordMediaPlayer);
                    wordMediaPlayer = MediaPlayer.create(Game.this, R.raw.end_word);
                    wordMediaPlayer.setLooping(false);
                    wordMediaPlayer.start();
                    closeMedia(mediaPlayer);
                    mediaPlayer = MediaPlayer.create(Game.this, R.raw.here_to_stay);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                    isFinish = true;
                   // textToSpeech.speak("胜利!.. 感谢您的时间，希望玩的开心！", TextToSpeech.QUEUE_ADD, null);
                }
                else
                    initialize();
                return true;
            } else
                return false;

        }

        // 画迷宫
        @Override
        protected void onDraw(Canvas canvas) {//先绘画格子，然后
            super.onDraw(canvas);
            canvas.drawColor(0xFFffffff);
            //从上到下画线
            for (int i = 0; i <= mazeSize; i++) {
                canvas.drawLine((int)(padding + i * width), (int)padding, (int)(padding + i * width),
                        (int)(padding + mazeSize * width), paint);
            }
            //从左到右画线
            for (int j = 0; j <= mazeSize; j++) {
                canvas.drawLine((int)(padding), (int)(padding + j * width), (int)(padding + mazeSize * width),
                        (int)(padding + j * width), paint);
            }

            paint.setColor(0xffffffff);//白色

            for (int i = mazeSize - 1; i >= 0; i--) {
                for (int j = mazeSize - 1; j >= 0; j--) {
                    Lattice f = maze[i][j].getFather();
                    if (f != null) {
                        int fx = f.getX(), fy = f.getY();	//获取父节点的坐标
                        clearFence(i, j, fx, fy, canvas);//将子孩子到父节点的边变为白色
                    }
                }
            }
            //入口颜色置白
            canvas.drawLine((int)padding, (int)padding + 1, (int)padding, (int)(padding + width - 1), paint);
            double last = padding + mazeSize * width;
            //出口
            canvas.drawLine((int)last, (int)last - 1, (int)last, (int)(last - width + 1), paint);

            //画玩家，及小球为红色
            paint.setColor(0xFFff0000);//red
            paint.setStyle(Paint.Style.FILL);
            double centerX = getCenterX(ballY) ;
            double centerY = getCenterY(ballX) ;
            double radius = width / 2.5;
            canvas.drawCircle((float)centerX, (float)centerY, (float)radius, paint);
        }

        // 清除迷宫边界
        public void clearFence(int i, int j, int fx, int fy, Canvas canvas) {
            double sx = padding + (((j > fy) ? j : fy) * width);
            double sy = padding + ((i > fx ? i : fx) * width);
            double dx = (i == fx ? sx : sx + width);
            double dy = (i == fx ? sy + width : sy);
            if (sx != dx) {
                sx++;
                dx--;
            } else {
                sy++;
                dy--;
            }
            if (isHard)
                canvas.drawLine((float) ((int)sx+bias), (float)((int)sy+bias), (float) ((int)dx+bias), (float) ((int)dy+bias), paint);
            else
                canvas.drawLine((int)sx, (int)sy, (int)dx, (int)dy, paint);
        }


        // 提示信息
        public void soundHint() {
            Vector vector = findShortPath(maze,ballX,ballY);
            Lattice p;
            Iterator iterator = vector.iterator();
            if (iterator.hasNext()) {
                p = (Lattice) iterator.next();
                String direction = "";
                if ( p.getX() - ballX == 1 ) {
                    direction = "down";
                    if (hintMediaPlayer2.isPlaying())
                        hintMediaPlayer2.pause();
                    if (hintMediaPlayer3.isPlaying())
                        hintMediaPlayer3.pause();
                    if (hintMediaPlayer4.isPlaying())
                        hintMediaPlayer4.pause();
                    if (!hintMediaPlayer1.isPlaying())
                        hintMediaPlayer1.start();   // rainfall
                }
                else if ( p.getX() - ballX == -1 ) {
                    direction = "up";
                    if (hintMediaPlayer2.isPlaying())
                        hintMediaPlayer2.pause();
                    if (hintMediaPlayer1.isPlaying())
                        hintMediaPlayer1.pause();
                    if (hintMediaPlayer4.isPlaying())
                        hintMediaPlayer4.pause();
                    if (!hintMediaPlayer3.isPlaying())
                        hintMediaPlayer3.start();   // thunder
                }
                else if ( p.getY() - ballY == 1 || ( ballX==mazeSize-1) && (ballY==mazeSize-1 )) {
                    direction = "right";
                    if (hintMediaPlayer4.isPlaying())
                        hintMediaPlayer4.pause();
                    if (hintMediaPlayer1.isPlaying())
                        hintMediaPlayer1.pause();
                    if (hintMediaPlayer3.isPlaying())
                        hintMediaPlayer3.pause();
                    if (!hintMediaPlayer2.isPlaying())
                        hintMediaPlayer2.start();    // surf
                }
                else if ( p.getY() - ballY == -1 ) {
                    direction = "left";
                    if (hintMediaPlayer2.isPlaying())
                        hintMediaPlayer2.pause();
                    if (hintMediaPlayer1.isPlaying())
                        hintMediaPlayer1.pause();
                    if (hintMediaPlayer3.isPlaying())
                        hintMediaPlayer3.pause();
                    if (!hintMediaPlayer4.isPlaying())
                        hintMediaPlayer4.start();  // whip
                }
            } else {
                if (hintMediaPlayer4.isPlaying())
                    hintMediaPlayer4.pause();
                if (hintMediaPlayer1.isPlaying())
                    hintMediaPlayer1.pause();
                if (hintMediaPlayer3.isPlaying())
                    hintMediaPlayer3.pause();
                if (!hintMediaPlayer2.isPlaying())
                    hintMediaPlayer2.start();
            }
        }

        // 提示信息
        public void showHint() {
            if (!abilityOn) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.05f,0.05f);
                    // mediaPlayer.pause();
                }
                abilityOn = true;
                soundHint();
            } else {
                abilityOn = false;
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                mediaPlayer.setVolume(1f, 1f);
                if (hintMediaPlayer2.isPlaying())
                    hintMediaPlayer2.pause();
                if (hintMediaPlayer1.isPlaying())
                    hintMediaPlayer1.pause();
                if (hintMediaPlayer3.isPlaying())
                    hintMediaPlayer3.pause();
                if (hintMediaPlayer4.isPlaying())
                    hintMediaPlayer4.pause();
            }
            Vector vector = findShortPath(maze,ballX,ballY);
            Lattice p;
            Iterator iterator = vector.iterator();
            if (iterator.hasNext()) {
                p = (Lattice) iterator.next();
                String nextPoint = ((p.getX()+1) + ", " + (p.getY()+1));
                String direction = "";
                if (p.getX()-ballX==1)
                    direction = "down";
                else if (p.getX()-ballX==-1)
                    direction = "up";
                else if (p.getY()-ballY==1)
                    direction = "right";
                else if (p.getY()-ballY==-1)
                    direction = "left";
                // showDialogHint("NextPoint is ("+nextPoint+")" + "\n"+ "On your "+direction);
                // Toast.makeText(Game.this, "On your "+direction, Toast.LENGTH_SHORT).show();
            } else {
                   // surf
                // showDialogHint("Exit is on your right！");
               // Toast.makeText(Game.this, "Exit is on your right！", Toast.LENGTH_SHORT).show();
            }
        }

        // 判断是否超时
        public Boolean isLose() {
            if (!isPause) {
                if ( mediaCanPlaying && !mediaPlayer.isPlaying() )
                    return true;
                else
                    return false;
            }
            return false;
        }


    }
}

