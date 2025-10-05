package com.example.naruto_vs_bleach;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class Joystick {

    public float baseX, baseY, knobX, knobY;
    public float baseRadius, knobRadius;
    public boolean active = false;

    public Joystick(float baseX, float baseY, float baseRadius, float knobX, float knobY, float knobRadius) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseRadius = baseRadius;
        this.knobX = knobX;
        this.knobY = knobY;
        this.knobRadius = knobRadius;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.BLACK);
        paint.setAlpha(100);
        canvas.drawCircle(baseX, baseY, baseRadius, paint);

        paint.setAlpha(200);
        float dx = knobX - baseX;
        float dy = knobY - baseY;
        float dist = (float)Math.hypot(dx, dy);
        float maxDist = baseRadius;
        float drawX = knobX, drawY = knobY;
        if (dist > maxDist) {
            drawX = baseX + dx / dist * maxDist;
            drawY = baseY + dy / dist * maxDist;
        }
        canvas.drawCircle(drawX, drawY, knobRadius, paint);
    }

    public void reset() {
        knobX = baseX;
        knobY = baseY;
        active = false;
    }
}