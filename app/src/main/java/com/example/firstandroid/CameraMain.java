package com.example.firstandroid;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraMain extends AppCompatActivity {

    private PreviewView previewView;
    private RectView rectView;
    private OrtEnvironment ortEnvironment;
    private OrtSession session;
    private DataProcess dataProcess;

    private static final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_main);
        previewView = findViewById(R.id.previewView);
        rectView = findViewById(R.id.rectView);

        // 자동 꺼짐 해제
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 권한 허용
        setPermissions();

        // onnx 파일 && txt 파일 불러오기
        try {
            load();
        } catch (OrtException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 카메라 켜기
        try {
            setCamera();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setCamera() throws ExecutionException, InterruptedException {
        // 카메라 제공 객체
        ProcessCameraProvider processCameraProvider = ProcessCameraProvider.getInstance(this).get();

        // 전체 화면
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        // 전면 카메라
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // 16:9 화면으로 받아옴
        Preview preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();

        // preview 에서 받아와서 previewView 에 보여준다.
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // 분석 중이면 그 다음 화면이 대기중인 것이 아니라 계속 받아오는 화면으로 새로고침 함. 분석이 끝나면 그 최신 사진을 다시 분석
        ImageAnalysis analysis = new ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        analysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
            try {
                imageProcess(imageProxy);
            } catch (OrtException e) {
                e.printStackTrace();
                // Handle exception as needed
            } finally {
                imageProxy.close();
            }
        });

        // 카메라의 수명 주기를 메인 액티비티에 귀속
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
    }

    private void imageProcess(ImageProxy imageProxy) throws OrtException {
        Bitmap bitmap = dataProcess.imageToBitmap(imageProxy);
        FloatBuffer floatBuffer = dataProcess.bitmapToFloatBuffer(bitmap);
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
        imageProxy.close();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한을 허용하지 않으면 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(android.Manifest.permission.CAMERA);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION);
            }
        }
    }
}
