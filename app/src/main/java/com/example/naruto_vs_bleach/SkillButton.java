package com.example.naruto_vs_bleach;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class SkillButton {
    public float x, y, radius;
    public String label;
    public boolean isPressed = false;

    public SkillButton(float x, float y, float radius, String label) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.label = label;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(isPressed ? Color.YELLOW : Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius, paint);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(radius / 1.5f);
        canvas.drawText(label, x, y + radius / 3f, paint);
    }

    public boolean isTouched(float touchX, float touchY) {
        float dx = touchX - x;
        float dy = touchY - y;
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }
}
