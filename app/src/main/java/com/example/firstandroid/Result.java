package com.example.firstandroid;

import android.graphics.RectF;

public class Result {
    private final int classIndex;
    private final float score;
    private final RectF rectF;

    public Result(int classIndex, float score, RectF rectF) {
        this.classIndex = classIndex;
        this.score = score;
        this.rectF = rectF;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public float getScore() {
        return score;
    }

    public RectF getRectF() {
        return rectF;
    }
}

