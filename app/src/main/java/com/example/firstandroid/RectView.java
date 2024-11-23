package com.example.firstandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class RectView extends View {

    private ArrayList<Result> results;
    private String[] classes;

    private final Paint textPaint = new Paint();

    public RectView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        textPaint.setTextSize(60f);
        textPaint.setColor(Color.WHITE);
    }

    public void transformRect(ArrayList<Result> results) {
//        float scaleX = getWidth() / (float) DataProcess.INPUT_SIZE;
//        float scaleY = scaleX * 9f / 16f;
//
//        float realY = getWidth() * 9f / 16f;
//        float diffY = realY - getHeight();
        float scaleY = getHeight() / (float) DataProcess.INPUT_SIZE;
        float scaleX = scaleY * 9f / 16f;
        float realX = getHeight() * 9f / 16f;
        float diffX = realX - getWidth();


        for (Result result : results) {
            result.getRectF().left = result.getRectF().left * scaleX - (diffX / 2f);
            result.getRectF().right = result.getRectF().right * scaleX - (diffX / 2f);
            result.getRectF().top *= scaleY;
            result.getRectF().bottom *= scaleY;
        }

        this.results = results;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (results != null) {
            for (Result result : results) {
                canvas.drawRect(result.getRectF(), findPaint(result.getClassIndex()));
                canvas.drawText(
                        classes[result.getClassIndex()] + ", " + Math.round(result.getScore() * 100) + "%",
                        result.getRectF().left + 10,
                        result.getRectF().top + 60,
                        textPaint
                );
            }
        }
        super.onDraw(canvas);
    }

    public void setClassLabel(String[] classes) {
        this.classes = classes;
    }

    private Paint findPaint(int classIndex) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(100f);

        switch (classIndex) {
            case 0:
            case 45:
            case 18:
            case 19:
            case 22:
            case 30:
            case 42:
            case 43:
            case 44:
            case 61:
            case 71:
            case 72:
                paint.setColor(Color.WHITE);
                break;
            case 1:
            case 3:
            case 14:
            case 25:
            case 37:
            case 38:
            case 79:
                paint.setColor(Color.BLUE);
                break;
            case 2:
            case 9:
            case 10:
            case 11:
            case 32:
            case 47:
            case 49:
            case 51:
            case 52:
                paint.setColor(Color.RED);
                break;
            case 5:
            case 23:
            case 46:
            case 48:
                paint.setColor(Color.YELLOW);
                break;
            case 6:
            case 13:
            case 34:
            case 35:
            case 36:
            case 54:
            case 59:
            case 60:
            case 73:
            case 77:
            case 78:
                paint.setColor(Color.GRAY);
                break;
            case 7:
            case 24:
            case 26:
            case 27:
            case 28:
            case 62:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 74:
            case 75:
                paint.setColor(Color.BLACK);
                break;
            case 12:
            case 29:
            case 33:
            case 39:
            case 41:
            case 58:
            case 50:
                paint.setColor(Color.GREEN);
                break;
            case 15:
            case 16:
            case 17:
            case 20:
            case 21:
            case 31:
            case 40:
            case 55:
            case 57:
            case 63:
                paint.setColor(Color.DKGRAY);
                break;
            case 70:
            case 76:
                paint.setColor(Color.LTGRAY);
                break;
            default:
                paint.setColor(Color.DKGRAY);
                break;
        }
        return paint;
    }
}

