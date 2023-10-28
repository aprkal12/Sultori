package com.example.firstandroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class subpage2 extends AppCompatActivity {
    Button btn, btn2;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        String str = intent.getStringExtra("name");
                        Toast.makeText(subpage2.this, str, Toast.LENGTH_SHORT).show();
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
                intent.putExtra("name", "김재엽 바보ㅋㅋ");

                setResult(RESULT_OK, intent);

                finish();
            }
        });

        btn2 = (Button) findViewById(R.id.rebtn);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), subpage1.class);
                resultLauncher.launch(intent);

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