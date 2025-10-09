package com.example.naruto_vs_bleach;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class SkillButton {
    public float x, y;          // tâm
    public float radius;        // bán kính
    public String label;
    public boolean isPressed = false;

    // Style (có thể chỉnh nếu muốn)
    private int alphaNormal = 140;     // 0..255  (mờ ~45%)
    private int alphaPressed = 220;    // sáng hơn khi bấm
    private float pressScale = 1.06f;  // nở nhẹ khi bấm
    private float strokeWidth = 2f;

    // Paint riêng (không dùng Paint bên ngoài)
    private final Paint pFill   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText   = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SkillButton(float x, float y, float radius, String label) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.label = label;

        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeWidth(strokeWidth);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(Math.max(28f, radius * 0.5f)); // chữ tự scale theo bán kính
    }

    /** Nếu đổi radius động, gọi hàm này để cập nhật cỡ chữ. */
    public void setRadius(float r) {
        this.radius = r;
        pText.setTextSize(Math.max(28f, radius * 0.5f));
    }

    /** Tuỳ chỉnh độ mờ hoặc độ nở khi bấm (không bắt buộc). */
    public void setStyle(int alphaNormal, int alphaPressed, float pressScale) {
        this.alphaNormal = alphaNormal;
        this.alphaPressed = alphaPressed;
        this.pressScale = pressScale;
    }

    public void draw(Canvas c) {
        final int a = isPressed ? alphaPressed : alphaNormal;

        // Nền xám nhạt mờ + viền trắng mờ
        pFill.setStyle(Paint.Style.FILL);
        pFill.setColor(Color.argb(a, 230, 230, 230));
        pStroke.setColor(Color.argb(Math.min(255, a + 30), 255, 255, 255));
        pText.setColor(Color.argb(255, 30, 30, 30));

        float r = isPressed ? radius * pressScale : radius;

        c.drawCircle(x, y, r, pFill);
        c.drawCircle(x, y, r, pStroke);

        // Căn chữ giữa theo baseline
        Paint.FontMetrics fm = pText.getFontMetrics();
        float textY = y - (fm.ascent + fm.descent) / 2f;
        c.drawText(label, x, textY, pText);
    }

    public boolean isTouched(float touchX, float touchY) {
        float dx = touchX - x;
        float dy = touchY - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}
