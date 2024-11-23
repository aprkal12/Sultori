package com.example.firstandroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class ObjectDetectionMenu extends AppCompatActivity {
    private static final int REQUEST_CODE = 0;
    final static int TAKE_PICTURE = 1;
    String mCurrentPhotoPath;
    final static int REQUEST_TAKE_PHOTO = 1;
    private ActivityResultLauncher<Intent> resultLauncher;
    private ActivityResultLauncher<Intent> resultpicture;
    Button btn1;
    Button btn2;
    Button btn3;
    ImageView imageView;
    Bitmap resizebitmap;
    Bitmap bitmap;
    private DataProcess dataProcess;
    private OrtEnvironment ortEnvironment;
    private OrtSession session;
    private PreviewView previewView;
    private RectView rectView;
    private String currentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection_menu);


        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        imageView = (ImageView) findViewById(R.id.img1);
        rectView = (RectView) findViewById(R.id.rectView);


        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Uri uri = data.getData();

                Glide.with(this).load(uri).into(imageView);

                Bitmap bitmap = null;
                try {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
                    resizebitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);
                    // imageView.setImageBitmap(resizebitmap);
                    load();
                    if (resizebitmap.getConfig() == Bitmap.Config.HARDWARE) {
                        // Config#ARGB_8888로 변환
                        resizebitmap = resizebitmap.copy(Bitmap.Config.ARGB_8888, false);
                    }
                    imageProcess(resizebitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (OrtException e) {
                    throw new RuntimeException(e);
                }
            } else if (result.getResultCode() == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_SHORT).show();
            }
        });

        resultpicture = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Glide.with(this).load(currentPhotoPath).into(imageView);
                        // 찍은 사진을 파일에서 읽어와서 Bitmap으로 표시
                        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);

                        resizebitmap = Bitmap.createScaledBitmap(imageBitmap, 640, 640, true);
                        imageView.setImageBitmap(resizebitmap);
                        try {
                            load();
                        } catch (OrtException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (resizebitmap.getConfig() == Bitmap.Config.HARDWARE) {
                            // Config#ARGB_8888로 변환
                            resizebitmap = resizebitmap.copy(Bitmap.Config.ARGB_8888, false);
                        }
                        try {
                            imageProcess(resizebitmap);
                        } catch (OrtException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });



        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                resultLauncher.launch(intent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ObjectDetectionMenu.this, CameraMain.class);
                startActivity(intent);
            }
        });
        Uri photoUri;
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });


    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // 에러 처리 (파일 생성 실패 등)
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.firstandroid.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                resultpicture.launch(takePictureIntent);
            }
        }
    }


    private void imageProcess(Bitmap resizebitmap) throws OrtException {
        FloatBuffer floatBuffer = dataProcess.bitmapToFloatBuffer(resizebitmap);
        String inputName = session.getInputNames().iterator().next(); // session 이름
        // 모델의 요구 입력값 [1 3 640 640] [배치 사이즈, 픽셀(RGB), 너비, 높이], 모델마다 크기는 다를 수 있음.
        long[] shape = new long[]{
                DataProcess.BATCH_SIZE,
                DataProcess.PIXEL_SIZE,
                DataProcess.INPUT_SIZE,
                DataProcess.INPUT_SIZE
        };
        OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape);
        OrtSession.Result resultTensor = session.run(Collections.singletonMap(inputName, inputTensor));
        Object[] outputs = (Object[]) resultTensor.get(0).getValue(); // [1 84 8400]
        ArrayList<Result> results = dataProcess.outputsToNPMSPredictions(outputs);

        // 화면 표출
        rectView.transformRect(results);
        rectView.invalidate();
    }

    private void load() throws OrtException, IOException {
        dataProcess = new DataProcess(this);
        dataProcess.loadModel(); // onnx 모델 불러오기
        dataProcess.loadLabel(); // coco txt 파일 불러오기

        ortEnvironment = OrtEnvironment.getEnvironment();
        session = ortEnvironment.createSession(
                this.getFilesDir().getAbsolutePath() + "/" + DataProcess.FILE_NAME,
                new OrtSession.SessionOptions()
        );

        rectView.setClassLabel(dataProcess.classes);
    }
}