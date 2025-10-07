package com.example.naruto_vs_bleach;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.List;

public class Player extends GameObject {
    private Animation runAnim, jumpAnim, idleAnim;
    private Animation attackAnim1, attackAnim2;
    private Animation currentAnim;

    private boolean jumping = false;
    private int velocityY = 0;
    private final int jumpVelocity = -60;
    private int groundY;
    private final int gravity = 12;

    private boolean isAttacking = false;
    private int comboStep = 0;
    private long lastAttackTime = 0;
    private final long comboMaxDelay = 300; // ms
    private boolean comboQueued = false;
    private float speed = 35f;
    private final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    public Player(Context context, int groundY) {
        this.groundY = groundY;

        List<Bitmap> run = Utils.loadFrames(context, "ichigo-convert/move");
        List<Bitmap> jump = Utils.loadFrames(context, "ichigo-convert/jump");
        List<Bitmap> idle = Utils.loadFrames(context, "ichigo-convert/stance");
        List<Bitmap> normalAttack1 = Utils.loadFrames(context, "ichigo-convert/attack");
        List<Bitmap> normalAttack2 = Utils.loadFrames(context, "ichigo-convert/b-forward-attack");

        runAnim = new Animation(run, 30);
        jumpAnim = new Animation(jump, 100);
        idleAnim = new Animation(idle, 100);
        attackAnim1 = new Animation(normalAttack1, 80);
        attackAnim2 = new Animation(normalAttack2, 80);

        currentAnim = idleAnim;

        x = 200;
        y = groundY;
    }

    @Override
    public void update() {
        if (jumping) {
            y += velocityY;
            velocityY += gravity;

            if (y >= groundY) {
                y = groundY;
                jumping = false;
                velocityY = 0;
                currentAnim = idleAnim;
            }
            jumpAnim.update();
            return;
        }

        if (isAttacking) {
            currentAnim.update();

            if (currentAnim.isLastFrame()) {
                isAttacking = false;

                // Nếu có combo queued → trigger attack 2
                if (comboQueued) {
                    comboStep = 2;
                    isAttacking = true;
                    x += facingRight?speed*3:-speed*3;
                    currentAnim = attackAnim2;
                    attackAnim2.reset();
                    comboQueued = false;
                    lastAttackTime = System.currentTimeMillis();
                } else {
                    comboStep = 0;
                    currentAnim = idleAnim;
                }
            }
            return;
        }


        currentAnim.update();
    }

    public void updateWithJoystick(Joystick joystick) {
        if (isAttacking) return; // không di chuyển khi đang đánh

        if (joystick.active) {
            float angle = joystick.getAngle();
            float moveX = (float) Math.cos(angle) * speed;
            float moveY = (float) Math.sin(angle) * speed;

            x += moveX;

            // === Nếu joystick hướng lên (nhảy) ===
            if (moveY < -5 && !jumping) {
                jump();
                return;
            }

            facingRight = moveX >= 0;

            if (!jumping && Math.abs(moveX) > 1f) {
                currentAnim = runAnim;
            }
        } else if (!jumping && !isAttacking) {
            currentAnim = idleAnim;
        }
    }

    public Bitmap getCurrentFrame() {
        return currentAnim.getCurrentFrame();
    }

    public void drawAt(Canvas canvas, Paint paint, float screenX, float screenY) {
        Bitmap frame = currentAnim.getCurrentFrame();
        if (frame == null) return;

        if (facingRight) {
            canvas.drawBitmap(frame, screenX - frame.getWidth() / 2f, screenY - frame.getHeight(), paint);
        } else {
            Matrix m = new Matrix();
            m.preScale(-1, 1);
            Bitmap flipped = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), m, true);
            canvas.drawBitmap(flipped, screenX - frame.getWidth() / 2f, screenY - frame.getHeight(), paint);
        }
    }

    public void idle() {
        if (!jumping && !isAttacking) {
            currentAnim = idleAnim;
        }
    }

    public void jump() {
        if (!jumping && !isAttacking) {
            jumping = true;
            velocityY = jumpVelocity;
            currentAnim = jumpAnim;
            jumpAnim.reset();
        }
    }

    // ✅ Combo attack an toàn
    public void attack() {
        long now = System.currentTimeMillis();

        // Nếu đang đánh nhưng animation chưa xong → bỏ qua nhấn tiếp
        if (isAttacking) {
            // Nếu đang attack 1, nhấn trong thời gian combo → queue combo 2
            if (comboStep == 1 && now - lastAttackTime <= comboMaxDelay) {
                comboQueued = true;
            }
            return; // đang attack, không start animation mới ngay
        }

        comboStep = 1;
        isAttacking = true;
        currentAnim = attackAnim1;
        attackAnim1.reset();
        lastAttackTime = now;
    }
}
