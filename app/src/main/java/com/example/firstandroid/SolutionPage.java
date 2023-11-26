package com.example.firstandroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SolutionPage extends AppCompatActivity {
    ImageView image1;
    VideoView video1;
    Button home, callinfo;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        String str = intent.getStringExtra("name");
                        Toast.makeText(SolutionPage.this, str, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solutionpage);
        home = (Button) findViewById(R.id.home);
        callinfo = (Button) findViewById(R.id.btn_callinfo);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                resultLauncher.launch(intent);
            }
        });
        callinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer buffer = new StringBuffer();
                String data = null;
                FileInputStream fis = null;
                try {
                    fis = openFileInput("internal.txt");
                    BufferedReader iReader = new BufferedReader(new InputStreamReader((fis)));

                    data = iReader.readLine();
                    while(data != null)
                    {
                        buffer.append(data);
                        data = iReader.readLine();
                    }
                    buffer.append("\n");
                    iReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        Intent intent = getIntent();
        String weight = intent.getStringExtra("weight");
        String alchol = intent.getStringExtra("alchol");
        String count = intent.getStringExtra("count");
        String gender = intent.getStringExtra("gender");
        String amount = intent.getStringExtra("amount");

        // 가져온 데이터를 UI에 표시
        TextView weightTextView = findViewById(R.id.weight);
        weightTextView.setText(weight+ "kg");

        TextView alcholTextView = findViewById(R.id.alchol);
        alcholTextView.setText(alchol);

        TextView countTextView = findViewById(R.id.janORbyung);
        countTextView.setText(amount);

        TextView genderTextView = findViewById(R.id.maleORfemale);
        genderTextView.setText(gender);




        image1 = (ImageView) findViewById(R.id.myImage);

        video1 = (VideoView) findViewById(R.id.video1);
        Uri videoUri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");

        //비디오뷰의 재생, 일시정지 등을 할 수 있는 '컨트롤바'를 붙여주는 작업
        video1.setMediaController(new MediaController(this));

        //VideoView가 보여줄 동영상의 경로 주소(Uri) 설정하기
        video1.setVideoURI(videoUri);

        //동영상을 읽어오는데 시간이 걸리므로..
        //비디오 로딩 준비가 끝났을 때 실행하도록..
        //리스너 설정
        video1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //비디오 시작
                video1.start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            String name = data.getStringExtra("name");
            Toast.makeText(getApplicationContext(), "dialog화면에서의 응답 : " + name, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 101){
////            if(resultCode == RESULT_OK){
////                TextView alchol = (TextView) findViewById(R.id.alchol);
////                String alcName = data.getStringExtra("alchol");
////                alchol.setText(alcName);
////
////                //양 정보 가져오기
////                TextView janORbyung = (TextView) findViewById(R.id.janORbyung);
////                int num = data.getIntExtra("janORbyung",0);
////                String jan = data.getStringExtra("text");
////                janORbyung.setText(num+jan);
////
////                //성별 정보 가져오기
////                TextView maORfe = (TextView) findViewById(R.id.maleORfemale);
////                String maleORfemale = data.getStringExtra("maleORfemale");
////                maORfe.setText(maleORfemale);
////
////                //몸무게 정보 가져오기
////                TextView weight = (TextView) findViewById(R.id.weight);
////                int weightNum = data.getIntExtra("weight",0);
////                weight.setText(weightNum);
////            }
//        }
    }