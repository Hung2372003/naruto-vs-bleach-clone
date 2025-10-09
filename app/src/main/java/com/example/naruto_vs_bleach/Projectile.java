package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class Projectile {
    public float x, y;
    public float speed = 20f;   // tốc độ bay
    public boolean facingRight;
    public boolean alive = true;
    public int damage = 20;

    private Animation anim;

    public Projectile(float x, float y, boolean facingRight, Animation anim) {
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.anim = anim;
    }

    public void update(int mapWidth) {
        // di chuyển projectile
        x += facingRight ? speed : -speed;

        // update animation
        anim.update();

        // nếu bay ra ngoài map => chết
        if (x < 0 || x > mapWidth) alive = false;
    }

    public void draw(Canvas canvas, Paint paint) {
        Bitmap frame = anim.getCurrentFrame();
        if (frame == null) return;

        if (facingRight) {
            canvas.drawBitmap(frame, x - frame.getWidth()/2f, y - frame.getHeight()/2f, paint);
        } else {
            Matrix m = new Matrix();
            m.preScale(-1, 1);
            Bitmap flipped = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), m, true);
            canvas.drawBitmap(flipped, x - frame.getWidth()/2f, y - frame.getHeight()/2f, paint);
        }
    }

    public Rect getBounds() {
        Bitmap frame = anim.getCurrentFrame();
        if (frame == null) return new Rect((int)x, (int)y, (int)x, (int)y);

        int w = frame.getWidth();
        int h = frame.getHeight();
        return new Rect((int)(x - w/2f), (int)(y - h/2f), (int)(x + w/2f), (int)(y + h/2f));
    }
}
