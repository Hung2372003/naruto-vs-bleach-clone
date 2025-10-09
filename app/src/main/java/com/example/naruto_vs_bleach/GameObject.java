package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Lớp cơ sở cho mọi đối tượng có hoạt ảnh, vị trí và vật lý cơ bản.
 * Dùng chung cho Player, Boss, v.v.
 */
public abstract class GameObject {
    // ----------- Thuộc tính cơ bản -----------
    protected float x, y;              // vị trí
    protected float width = 80;        // 🔧 giảm nhẹ kích thước hitbox cho tự nhiên hơn
    protected float height = 160;
    protected float velocityX = 0;
    protected float velocityY = 0;
    protected float speed = 15f;
    protected boolean facingRight = true;
    protected boolean alive = true;

    protected Animation currentAnim;

    // ----------- Các phương thức chính -----------

    /** Cập nhật logic riêng cho từng đối tượng (Player, Boss, v.v.) */
    public abstract void update();

    /** Cập nhật vị trí dựa vào vận tốc (vật lý cơ bản) */
    public void physicsUpdate() {
        x += velocityX;
        y += velocityY;
    }

    /** Giới hạn không vượt khỏi biên map */
    public void clampPosition(float minX, float maxX) {
        if (x < minX) x = minX;
        if (x + width > maxX) x = maxX - width;
    }

    /** Kiểm tra va chạm AABB (Axis-Aligned Bounding Box) */
    public boolean isColliding(GameObject other) {
        return alive && other.alive &&
                x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    /** Khi va chạm → đẩy 2 bên ra xa nhau */
    public void resolveCollision(GameObject other) {
        if (!isColliding(other)) return;

        float dx = (x + width / 2f) - (other.x + other.width / 2f);
        float dy = (y + height / 2f) - (other.y + other.height / 2f);
        float overlapX = (width + other.width) / 2f - Math.abs(dx);
        float overlapY = (height + other.height) / 2f - Math.abs(dy);

        // Ưu tiên tách theo trục X (chủ yếu là va chạm ngang)
        if (overlapX < overlapY) {
            float push = overlapX / 2f;
            if (dx > 0) {
                x += push;
                other.x -= push;
            } else {
                x -= push;
                other.x += push;
            }
            velocityX = 0;
            other.velocityX = 0;
        } else {
            float push = overlapY / 2f;
            if (dy > 0) {
                y += push;
                other.y -= push;
            } else {
                y -= push;
                other.y += push;
            }
            velocityY = 0;
            other.velocityY = 0;
        }
    }

    // ----------- Vẽ hoạt ảnh -----------
    public void draw(Canvas canvas, Paint paint) {
        if (currentAnim == null) return;

        Bitmap frame = currentAnim.getCurrentFrame();
        if (frame == null) return;

        Matrix matrix = new Matrix();

        if (facingRight) {
            matrix.postTranslate(x, y - frame.getHeight());
        } else {
            // Lật ảnh quanh tâm
            matrix.postScale(-1f, 1f, frame.getWidth() / 2f, frame.getHeight() / 2f);
            matrix.postTranslate(x, y - frame.getHeight());
        }

        canvas.drawBitmap(frame, matrix, paint);
    }

    // ----------- Getter / Setter -----------
    public Bitmap getCurrentFrame() {
        return (currentAnim != null) ? currentAnim.getCurrentFrame() : null;
    }
    public Rect getBounds() {
        // default hitbox, có thể override ở Player/Boss
        return new Rect((int)x, (int)y, (int)x + (int)width, (int)y + (int)height);
    }

    public boolean isAttacking() { return false; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
