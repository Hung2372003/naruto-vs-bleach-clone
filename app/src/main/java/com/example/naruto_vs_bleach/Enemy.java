package com.example.naruto_vs_bleach;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Enemy extends GameObject {
    private Animation idleAnim;
    public Enemy(int x, int y, Animation idleAnim) {
        this.x = x;
        this.y = y;
        this.idleAnim = idleAnim;
        this.currentAnim = idleAnim;
    }

    @Override
    public void update() {
        currentAnim.update();
        // TODO: thêm AI di chuyển, tấn công
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(currentAnim.getCurrentFrame(), x, y, paint);
    }
}