package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public abstract class GameObject {
    protected int x, y;
    protected boolean facingRight = true;
    protected Animation currentAnim;

    public abstract void update();

    public void draw(Canvas canvas, Paint paint) {
        if (currentAnim == null) return;

        Bitmap frame = currentAnim.getCurrentFrame();
        Matrix matrix = new Matrix();

        if (facingRight) {
            matrix.postTranslate(x, y - frame.getHeight());
        } else {
            // Flip quanh tâm frame
            matrix.postScale(-1f, 1f, frame.getWidth() / 2f, frame.getHeight() / 2f);
            matrix.postTranslate(x, y - frame.getHeight());
        }

        canvas.drawBitmap(frame, matrix, paint);
    }
}