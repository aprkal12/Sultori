package com.example.firstandroid;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class subpage1 extends Activity {

    double soju = 0.25;
    double beer = 0.045;
    double C=0; double A=0;
    Button btn, sol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subpage1);

        setTitle("술토리 서브 페이지");
        btn = (Button) findViewById(R.id.btnt);
        sol = (Button) findViewById(R.id.solution);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                setResult(RESULT_OK, intent);

                finish();
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
        weightTextView.setText(weight + "kg");

        TextView alcholTextView = findViewById(R.id.alchol);
        alcholTextView.setText(alchol);

        TextView countTextView = findViewById(R.id.janORbyung);
        countTextView.setText(count + amount);

        TextView genderTextView = findViewById(R.id.maleORfemale);
        genderTextView.setText(gender);

        TextView c = findViewById(R.id.C);
        if(alchol.equals("맥주")) {
            if (amount.equals("잔")) {
                A = 200 * Integer.parseInt(count) * beer * 0.7894;
            } else if (amount.equals("병")) {
                A = 330 * Integer.parseInt(count) * beer * 0.7894;
            }
        }else if(alchol.equals("소주")){
            if (amount.equals("잔")){
                A = 50 * Integer.parseInt(count) * soju * 0.7894;
            }else if(amount.equals("병")){
                A = 360 * Integer.parseInt(count) * soju * 0.7894;
            }
        }
        double result;
        double R=0;
        if(gender.equals("남성")){
            R = 0.86;
        }else if(gender.equals("여성")){
            R = 0.64;
        }
        result = A*0.7/(Integer.parseInt(weight)*R);

        c.setText(Double.toString(result));

        sol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 정보를 SolutionPage로 전달하는 Intent 생성
                Intent intent = new Intent(subpage1.this, SolutionPage.class);
                intent.putExtra("c_value", result);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            String name = data.getStringExtra("name");
            Toast.makeText(getApplicationContext(), "메뉴화면에서의 응답 : " + name, Toast.LENGTH_SHORT).show();
        }
    }
}