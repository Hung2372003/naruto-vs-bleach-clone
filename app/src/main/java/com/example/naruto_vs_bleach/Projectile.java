package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class Projectile {
    public float x, y;
    public float speedX, speedY;
    public boolean alive = true;
    private Bitmap sprite;
    private boolean facingRight;

    public Projectile(float x, float y, float speedX, float speedY, Bitmap sprite, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.sprite = sprite;
        this.facingRight = facingRight;
    }

    public void update() {
        x += speedX;
        y += speedY;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (sprite == null) return;
        if (facingRight) {
            canvas.drawBitmap(sprite, x - sprite.getWidth() / 2f, y - sprite.getHeight(), paint);
        } else {
            Matrix m = new Matrix();
            m.preScale(-1, 1);
            Bitmap flipped = Bitmap.createBitmap(sprite, 0, 0, sprite.getWidth(), sprite.getHeight(), m, true);
            canvas.drawBitmap(flipped, x - sprite.getWidth() / 2f, y - sprite.getHeight(), paint);
        }
    }

    public Rect getBounds() {
        if (sprite == null) return new Rect((int)x, (int)y, (int)x, (int)y);
        return new Rect((int)(x - sprite.getWidth()/2f), (int)(y - sprite.getHeight()),
                (int)(x + sprite.getWidth()/2f), (int)y);
    }
}
