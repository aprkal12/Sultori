package com.example.firstandroid;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class subpage1 extends Activity {

    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subpage1);

        setTitle("술토리 서브 페이지");

        btn = (Button) findViewById(R.id.btnt);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("name", "김재엽 바보");

                setResult(RESULT_OK, intent);

                finish();
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