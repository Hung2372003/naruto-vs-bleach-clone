package com.example.naruto_vs_bleach;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.List;

public class Player extends GameObject {
    private Animation runAnim, jumpAnim, idleAnim, skillAnim;
    private boolean jumping = false;
    private int velocityY = 0;
    private final int jumpVelocity = -60;
    private int groundY;

    private boolean isSkillActive = false;

    // ✅ Tốc độ di chuyển
    private float speed = 20f;

    // ✅ Lưu kích thước màn hình để giới hạn di chuyển
    private final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    public Player(Context context, int groundY) {
        this.groundY = groundY;

        List<Bitmap> run = Utils.loadFrames(context, "ichigo-convert/move");
        List<Bitmap> jump = Utils.loadFrames(context, "ichigo-convert/jump");
        List<Bitmap> idle = Utils.loadFrames(context, "ichigo-convert/stance");
        List<Bitmap> skill = Utils.loadFrames(context, "ichigo-convert/attack");

        runAnim = new Animation(run, 100);
        jumpAnim = new Animation(jump, 100);
        idleAnim = new Animation(idle, 100);
        skillAnim = new Animation(skill, 100);

        currentAnim = idleAnim;

        x = 200;
        y = groundY;
    }

    @Override
    public void update() {
        if (jumping) {
            y += velocityY;
            velocityY += 7;
            if (y >= groundY) {
                y = groundY;
                jumping = false;
                velocityY = 0;
                currentAnim = idleAnim;
            }
            jumpAnim.update();
        } else if (isSkillActive) {
            skillAnim.update();
        } else {
            currentAnim.update();
        }
    }

    // ✅ Di chuyển theo joystick (có giới hạn màn hình)
    public void updateWithJoystick(Joystick joystick) {
        if (joystick.active) {
            float angle = joystick.getAngle();
            float moveX = (float) Math.cos(angle) * speed;
            float moveY = (float) Math.sin(angle) * speed;

            // === Di chuyển theo X ===
            x += moveX;

            // ✅ Giới hạn không ra khỏi màn hình
            int playerWidth = currentAnim.getCurrentFrame().getWidth();
            if (x < playerWidth / 2f) x = (int) (playerWidth / 2f);
            if (x > screenWidth - playerWidth / 2f) x = (int) (screenWidth - playerWidth / 2f);

            // === Nếu joystick hướng lên (nhảy) ===
            if (moveY < -5 && !jumping) {
                jump();
                return;
            }

            // === Cập nhật hướng nhìn ===
            facingRight = moveX >= 0;

            // === Đặt hoạt ảnh ===
            if (!jumping && !isSkillActive && Math.abs(moveX) > 1f) {
                currentAnim = runAnim;
            }
        } else {
            // Không di chuyển => idle
            if (!jumping && !isSkillActive) {
                currentAnim = idleAnim;
            }
        }
    }
    public Bitmap getCurrentFrame() {
        return currentAnim.getCurrentFrame();
    }

    public void drawAt(Canvas canvas, Paint paint, float screenX, float screenY) {
        Bitmap frame = currentAnim.getCurrentFrame();
        if (facingRight) {
            canvas.drawBitmap(frame, screenX - frame.getWidth() / 2f, screenY - frame.getHeight(), paint);
        } else {
            Matrix m = new Matrix();
            m.preScale(-1, 1);
            Bitmap flipped = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), m, true);
            canvas.drawBitmap(flipped, screenX - frame.getWidth() / 2f, screenY - frame.getHeight(), paint);
        }
    }
    public void move(float vx) {
        x += vx;
        facingRight = vx >= 0;
        if (!jumping && !isSkillActive) {
            currentAnim = runAnim;
        }
    }

    public void idle() {
        if (!jumping && !isSkillActive) {
            currentAnim = idleAnim;
        }
    }

    public void jump() {
        if (!jumping) {
            jumping = true;
            velocityY = jumpVelocity;
            currentAnim = jumpAnim;
            jumpAnim.reset();
        }
    }

    public void skill() {
        if (!isSkillActive) {
            isSkillActive = true;
            currentAnim = skillAnim;
            skillAnim.reset();
        }
    }
}
