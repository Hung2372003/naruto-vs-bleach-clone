package com.example.naruto_vs_bleach;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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
        float dist = (float) Math.hypot(dx, dy);
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

    public float getActuatorX() {
        float dx = knobX - baseX;
        float dist = (float) Math.hypot(dx, knobY - baseY);
        return dist > 0 ? dx / dist : 0;
    }

    public float getActuatorY() {
        float dy = knobY - baseY;
        float dist = (float) Math.hypot(knobX - baseX, dy);
        return dist > 0 ? dy / dist : 0;
    }

    // ✅ Lấy góc theo radian
    public float getAngle() {
        float dx = knobX - baseX;
        float dy = knobY - baseY;
        return (float) Math.atan2(dy, dx);
    }

    // ✅ Lấy góc theo độ (0–360)
    public float getAngleDegrees() {
        float angle = (float) Math.toDegrees(getAngle());
        if (angle < 0) angle += 360;
        return angle;
    }
}
