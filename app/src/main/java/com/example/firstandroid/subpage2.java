package com.example.firstandroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class subpage2 extends AppCompatActivity {
    Button btn, btn2, btnForSolution;
    String jujong, gender,jan,kg,amount;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        //String str = intent.getStringExtra("name");
                        //Toast.makeText(subpage2.this, str, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subpage2);

        btn = (Button) findViewById(R.id.rebtn2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                setResult(RESULT_OK, intent);

                finish();
            }
        });

        // 결과분석 레츠고 버튼
        btn2 = (Button) findViewById(R.id.rebtn);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), subpage1.class);
//                resultLauncher.launch(intent);
                EditText weightEditText = findViewById(R.id.weight);
                RadioButton alchol = findViewById(R.id.alchol);
                RadioButton alchol2 = findViewById(R.id.alchol2);
                EditText countEditText = findViewById(R.id.count);
                RadioButton maleRadioButton = findViewById(R.id.radioButton);
                RadioButton femaleRadioButton = findViewById(R.id.radioButton2);
                RadioButton janRadioButton = findViewById(R.id.radioButton3);
                RadioButton byungRadioButton = findViewById(R.id.radioButton4);

                // 정보를 SolutionPage로 전달하는 Intent 생성
                Intent intent = new Intent(subpage2.this, subpage1.class);

                // 몸무게, 주종, 주량 정보 추가
                intent.putExtra("weight", weightEditText.getText().toString());
                kg = weightEditText.getText().toString();

                if(alchol.isChecked()) {
                    intent.putExtra("alchol", "맥주");
                    jujong = alchol.getText().toString();
                } else if(alchol2.isChecked()){
                    intent.putExtra("alchol", "소주");
                    jujong = alchol2.getText().toString();
                }
                if (maleRadioButton.isChecked()) {
                    intent.putExtra("gender", "남성");
                    gender = maleRadioButton.getText().toString();
                } else if (femaleRadioButton.isChecked()) {
                    intent.putExtra("gender", "여성");
                    gender = femaleRadioButton.getText().toString();
                }
                intent.putExtra("count", countEditText.getText().toString());
                jan = countEditText.getText().toString();

                // 양 정보 추가
                if (janRadioButton.isChecked()) {
                    intent.putExtra("amount", janRadioButton.getText().toString());
                    amount = janRadioButton.getText().toString();
                } else if (byungRadioButton.isChecked()) {
                    intent.putExtra("amount", byungRadioButton.getText().toString());
                    amount = janRadioButton.getText().toString();
                }
                //내장메모리에 저장하는 방식
                String TimeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String sibun = new SimpleDateFormat("HHmm").format(new Date());
                String myText = TimeStamp+"\n"+jujong + "\n"+jan+amount+ "\n"+kg+ "\n"+gender;
                String FileName = "sultory"+ TimeStamp + sibun;

                try {
                    FileOutputStream os = openFileOutput(FileName, MODE_PRIVATE);
                    os.write(myText.getBytes());
                    os.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            String name = data.getStringExtra("name");
            Toast.makeText(getApplicationContext(), "dialog화면에서의 응답 : " + name, Toast.LENGTH_SHORT).show();
        }
    }
}