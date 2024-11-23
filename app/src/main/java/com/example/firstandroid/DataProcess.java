package com.example.firstandroid;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import androidx.camera.core.ImageProxy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import kotlin.math.MathKt;

public class DataProcess {
    private Context context;
    String[] classes;

    public static final int BATCH_SIZE = 1;
    public static final int INPUT_SIZE = 640;
    public static final int PIXEL_SIZE = 3;
    public static final String FILE_NAME = "best.onnx";
    public static final String LABEL_NAME = "sultori.txt";

    public DataProcess(Context context) {
        this.context = context;
    }

    public Bitmap imageToBitmap(ImageProxy imageProxy) {
        Bitmap bitmap = imageProxy.toBitmap();
        Bitmap bitmap640 = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        Matrix matrix = new Matrix();
        matrix.postRotate(90f);
        return Bitmap.createBitmap(bitmap640, 0, 0, INPUT_SIZE, INPUT_SIZE, matrix, true);
    }

    public static FloatBuffer bitmapToFloatBuffer(Bitmap bitmap) {
        float imageSTD = 255.0f;
        FloatBuffer buffer = FloatBuffer.allocate(BATCH_SIZE * PIXEL_SIZE * INPUT_SIZE * INPUT_SIZE);
        buffer.rewind();
        int area = INPUT_SIZE * INPUT_SIZE;
        int[] bitmapData = new int[area];
        bitmap.getPixels(bitmapData, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < INPUT_SIZE - 1; i++) {
            for (int j = 0; j < INPUT_SIZE - 1; j++) {
                int idx = INPUT_SIZE * i + j;
                int pixelValue = bitmapData[idx];
                buffer.put(idx, ((pixelValue >> 16 & 0xff) / imageSTD));
                buffer.put(idx + area, ((pixelValue >> 8 & 0xff) / imageSTD));
                buffer.put(idx + area * 2, ((pixelValue & 0xff) / imageSTD));
            }
        }
        buffer.rewind();
        return buffer;
    }
    public void loadModel() throws IOException {
        // onnx 파일 불러오기
        AssetManager assetManager = context.getAssets();
        File outputFile = new File(context.getFilesDir().toString() + "/" + FILE_NAME);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(FILE_NAME);
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public void loadLabel() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(LABEL_NAME)));
            String line;
            ArrayList<String> classList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                classList.add(line);
            }
            classes = classList.toArray(new String[0]);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Result> outputsToNPMSPredictions(Object[] outputs) {
        float confidenceThreshold = 0.45f;
        ArrayList<Result> results = new ArrayList<>();
        int rows;
        int cols;

        Object[] firstOutput = (Object[]) outputs[0];
        rows = firstOutput.length;
        cols = ((float[]) firstOutput[0]).length;

        float[][] output = new float[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                output[j][i] = ((float[]) ((Object[]) outputs[0])[i])[j];
            }
        }

        for (int i = 0; i < cols; i++) {
            int detectionClass = -1;
            float maxScore = 0f;
            float[] classArray = new float[classes.length];

            System.arraycopy(output[i], 4, classArray, 0, classes.length);

            for (int j = 0; j < classes.length; j++) {
                if (classArray[j] > maxScore) {
                    detectionClass = j;
                    maxScore = classArray[j];
                }
            }

            if (maxScore > confidenceThreshold) {
                float xPos = output[i][0];
                float yPos = output[i][1];
                float width = output[i][2];
                float height = output[i][3];

                RectF rectF = new RectF(
                        Math.max(0f, xPos - width / 2f),
                        Math.max(0f, yPos - height / 2f),
                        Math.min(INPUT_SIZE - 1f, xPos + width / 2f),
                        Math.min(INPUT_SIZE - 1f, yPos + height / 2f)
                );

                Result result = new Result(detectionClass, maxScore, rectF);
                results.add(result);
            }
        }

        return nms(results);
    }

    private ArrayList<Result> nms(ArrayList<Result> results) {
        ArrayList<Result> list = new ArrayList<>();

        for (int i = 0; i < classes.length; i++) {
            // 1. 클래스 (라벨들) 중에서 가장 높은 확률값을 가졌던 클래스 찾기
            PriorityQueue<Result> pq = new PriorityQueue<>(50, Comparator.comparing(Result::getScore));
            int finalI = i;
            List<Result> classResults = results.stream().filter(result -> result.getClassIndex() == finalI).collect(Collectors.toList());
            pq.addAll(classResults);

            // NMS 처리
            while (!pq.isEmpty()) {
                // 큐 안에 속한 최대 확률값을 가진 class 저장
                Result[] detections = pq.toArray(new Result[0]);
                Result max = detections[0];
                list.add(max);
                pq.clear();

                // 교집합 비율 확인하고 50% 넘기면 제거
                for (int k = 1; k < detections.length; k++) {
                    Result detection = detections[k];
                    RectF rectF = detection.getRectF();
                    float iouThresh = 0.5f;
                    if (boxIOU(max.getRectF(), rectF) < iouThresh) {
                        pq.add(detection);
                    }
                }
            }
        }
        return list;
    }

    // 겹치는 비율 (교집합/합집합)
    private float boxIOU(RectF a, RectF b) {
        return boxIntersection(a, b) / boxUnion(a, b);
    }

    // 교집합
    private float boxIntersection(RectF a, RectF b) {
        // x1, x2 == 각 rect 객체의 중심 x or y값, w1, w2 == 각 rect 객체의 넓이 or 높이
        float w = overlap(
                (a.left + a.right) / 2f, a.right - a.left,
                (b.left + b.right) / 2f, b.right - b.left
        );
        float h = overlap(
                (a.top + a.bottom) / 2f, a.bottom - a.top,
                (b.top + b.bottom) / 2f, b.bottom - b.top
        );

        return (w < 0 || h < 0) ? 0f : w * h;
    }

    // 합집합
    private float boxUnion(RectF a, RectF b) {
        float i = boxIntersection(a, b);
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
    }

    // 서로 겹치는 부분의 길이
    private float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = Math.max(l1, l2);
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = Math.min(r1, r2);
        return right - left;
    }



}

