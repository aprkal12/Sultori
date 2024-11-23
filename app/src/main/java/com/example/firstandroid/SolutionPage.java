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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class SolutionPage extends AppCompatActivity {
    ImageView image1;
    Button home;
    TextView beerTextView, amountTextView, weightTextView, genderTextView;
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

        //원형 이미지
        ImageView imageView = findViewById(R.id.myImage);
//        imageView.setImageResource(R.drawable.sultory);

        Intent intent = getIntent();
        double cValue = intent.getDoubleExtra("c_value", 0.0);
        TextView cTextView = findViewById(R.id.diagText);

        if(cValue>=0.365){
            cTextView.setText("술을 그렇게까지 먹다니...");
        }else{
            cTextView.setText("설마 운전할 생각은 아니죠...?");
        }

        image1 = (ImageView) findViewById(R.id.myImage);

        beerTextView = findViewById(R.id.alchol);
        amountTextView = findViewById(R.id.janORbyung);
        weightTextView = findViewById(R.id.weight);
        genderTextView = findViewById(R.id.maleORfemale);

        String recentFilePath = getRecentFilePath();if (recentFilePath != null) {
            File file = new File(recentFilePath);
            if (file.exists()) {
                // 파일이 존재하면 읽기 작업 수행
                readTextFromFile(recentFilePath);
            } else {
                Toast.makeText(this, "파일이 없어요", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "최근 파일이 없어요", Toast.LENGTH_SHORT).show();
        }
        home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                resultLauncher.launch(intent);
            }
        });

    }
    private String getRecentFilePath() {
        File directory = getFilesDir();
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // "sultory"로 시작하는 파일만 선택
                return name.toLowerCase().startsWith("sultory");
            }
        });

        if (files != null && files.length > 0) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });

            // 가장 최근에 수정된 파일을 반환
            return files[0].getPath();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            String name = data.getStringExtra("name");
            Toast.makeText(getApplicationContext(), "dialog화면에서의 응답 : " + name, Toast.LENGTH_SHORT).show();
        }
    }

    private void readTextFromFile(String path) {
        File file = new File(path);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // 각 항목을 저장할 변수 선언
            String beer = "", amount = "", weight = "", gender = "";

            // 파일에서 한 줄씩 읽어오기
            String line;
            int count = 0;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim(); // 좌우 공백 제거

                if (!line.isEmpty()) { // 비어있는 줄은 무시
                    // 각 항목을 변수에 저장
                    switch (count) {
                        case 0:
                            break;
                        case 1:
                            beer = line;
                            break;
                        case 2:
                            amount = line;
                            break;
                        case 3:
                            weight = line;
                            break;
                        case 4:
                            gender = line;
                            break;
                    }
                    count++;
                }
            }

            // 읽어온 값을 TextView에 설정
            beerTextView.setText(beer);
            amountTextView.setText(amount);
            weightTextView.setText(weight);
            genderTextView.setText(gender);
            // 파일에서 읽은 데이터를 로그로 출력
            Log.d("FileData", "Beer: " + beer);
            Log.d("FileData", "Amount: " + amount);
            Log.d("FileData", "Weight: " + weight);
            Log.d("FileData", "Gender: " + gender);
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}