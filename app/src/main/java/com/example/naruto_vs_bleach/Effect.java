package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Effect {
    public enum Layer { FRONT, BACK }

    public float x, y;
    private Animation anim;
    private boolean finished = false;
    private Layer layer;

    public Effect(float x, float y, Animation anim, Layer layer) {
        this.x = x;
        this.y = y;
        this.anim = anim;
        this.layer = layer;
    }

    public Layer getLayer() { return layer; }

    public void update() {
        if (finished) return;
        anim.update();
        if (anim.isLastFrame()) finished = true;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (finished) return;
        Bitmap frame = anim.getCurrentFrame();
        if (frame != null) {
            canvas.drawBitmap(frame, x - frame.getWidth()/2f, y - frame.getHeight()/2f, paint);
        }
    }

    public boolean isFinished() { return finished; }
}
