package com.example.lee.maze;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnInitListener{

    private Button button;
    long time =0;
    private TextToSpeech textToSpeech;
    private Boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().addActivity(this);
        textToSpeech = new TextToSpeech(this, this);


        button = findViewById(R.id.screen_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long secondTime= System.currentTimeMillis();
                if(secondTime - time > 1000){
                    time = secondTime;
                    textToSpeech.speak(getResources().getString(R.string.operation), TextToSpeech.QUEUE_ADD, null);
                }else{
                    if (!flag ) {
                        flag = true;    //防止开启多个界面
                        Intent intent = new Intent(MainActivity.this, Game.class);
                        MainActivity.this.startActivity(intent);//跳转界面
                    }
                }

            }
        });

    }
    @Override
    public void onResume(){
        super.onResume();
    }

    public void onInit(int status){
        // 判断是否转化成功
        if (status == TextToSpeech.SUCCESS){
            //默认设定语言为中文
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            textToSpeech.setPitch(0.9f);
            textToSpeech.setSpeechRate(1.1f);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(MainActivity.this, "语音设置失败", Toast.LENGTH_SHORT).show();
            }else{
                //不支持中文就将语言设置为英文
                textToSpeech.setLanguage(Locale.US);
            }
        }
        Toast.makeText(MainActivity.this, "为获得最佳游戏体验，请带上耳机", Toast.LENGTH_LONG).show();
        textToSpeech.speak(this.getString(R.string.help), TextToSpeech.QUEUE_ADD, null);

    }
    protected void onStop() {
        super.onStop();
        textToSpeech.stop(); // 不管是否正在朗读TTS都被打断
        textToSpeech.shutdown(); // 关闭，释放资源
    }

}
