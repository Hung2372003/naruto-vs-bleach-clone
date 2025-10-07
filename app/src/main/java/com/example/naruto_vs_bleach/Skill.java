package com.example.naruto_vs_bleach;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Skill {
    private Animation anim;
    private boolean active = false;
    private int damage;
    private int x, y;

    public Skill(Animation anim, int damage) {
        this.anim = anim;
        this.damage = damage;
    }

    public void start(int startX, int startY) {
        active = true;
        x = startX;
        y = startY;
        anim.reset();
    }

    public void update() {
        if (active) anim.update();
        // khi anim xong thì active = false
    }

    public void draw(Canvas canvas, Paint paint) {
        if (active) canvas.drawBitmap(anim.getCurrentFrame(), x, y, paint);
    }

    public boolean isActive() { return active; }
}
