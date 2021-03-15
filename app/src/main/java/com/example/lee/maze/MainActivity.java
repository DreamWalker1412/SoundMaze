package com.example.lee.maze;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnInitListener{

    private Button easyBtn;
    private int NUM = 20;
    long firstTime=0;
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().addActivity(this);
        tts = new TextToSpeech(this, this);


        easyBtn = (Button)findViewById(R.id.easy);//点击容易按钮
        //监听
        easyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long secondTime= System.currentTimeMillis();
                //两次点击的间隔大于1s，则底部弹出文字，间隔小于1s则进入游戏

                if(secondTime- firstTime> 1000){
                    firstTime= secondTime;
                    tts.speak(getResources().getString(R.string.opration), TextToSpeech.QUEUE_ADD, null);
                }else{
                    Intent intent = new Intent(MainActivity.this, Game.class);
                    MainActivity.this.startActivity(intent);//跳转界面
                }

            }
        });

    }
    @Override
    public void onResume(){
        super.onResume();
        Toast.makeText(MainActivity.this, "主界面", Toast.LENGTH_SHORT).show();
    }

    public void onInit(int status){
        // 判断是否转化成功
        if (status == TextToSpeech.SUCCESS){
            //默认设定语言为中文
            int result = tts.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(MainActivity.this, "语音设置失败", Toast.LENGTH_SHORT).show();
            }else{
                //不支持中文就将语言设置为英文
                tts.setLanguage(Locale.US);
            }
        }
        tts.speak(this.getString(R.string.help), TextToSpeech.QUEUE_ADD, null);

    }
    protected void onStop() {
        super.onStop();
        tts.stop(); // 不管是否正在朗读TTS都被打断
        tts.shutdown(); // 关闭，释放资源
    }

}
