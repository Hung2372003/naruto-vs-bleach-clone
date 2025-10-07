package com.example.naruto_vs_bleach;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class Player extends GameObject {
    public int maxHp = 9999;
    public int hp = 8888;
    public boolean alive = true;
    public Animation runAnim, jumpAnim, idleAnim;
    private Animation attackAnim1, attackAnim2, attackAnim3;
    public Animation currentAnim;

    private boolean jumping = false;
    private int velocityY = 0;
    private final int jumpVelocity = -50;
    private int groundY;
    private final int gravity = 5;

    public boolean isAttacking = false;
    public boolean usingS1Skill = false;
    public boolean usingS2Skill = false;

    private int comboStep = 0;
    private long lastAttackTime = 0;
    private final long comboMaxDelay = 300; // ms
    private boolean comboQueued = false;
    private float speed = 35f;
    private final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private List<Effect> activeEffects = new ArrayList<>();
    private Animation  attack3EffectAnim;
    private Animation s1SkillAnim;
    private Animation s2SkillAnim;

    private boolean s1SkillMoving = false;
    private float s1SkillTargetX = 0;
    private float s1SkillMoveSpeed = 100f; // tốc độ di chuyển mỗi frame

    public Player(Context context, int groundY) {
        this.groundY = groundY;

        List<Bitmap> run = Utils.loadFrames(context, "ichigo-convert/move");
        List<Bitmap> jump = Utils.loadFrames(context, "ichigo-convert/jump");
        List<Bitmap> idle = Utils.loadFrames(context, "ichigo-convert/stance");
        List<Bitmap> normalAttack1 = Utils.loadFrames(context, "ichigo-convert/attack");
        List<Bitmap> normalAttack2 = Utils.loadFrames(context, "ichigo-convert/b-forward-attack");
        List<Bitmap> normalAttack3 = Utils.loadFrames(context, "ichigo-convert/b-down-attack");
        List<Bitmap> s1Skill = Utils.loadFrames(context, "ichigo-convert/y-attack");// <-- chiêu 3
        List<Bitmap> s2Skill = Utils.loadFrames(context, "ichigo-convert/y-forword-attack");

        attack3EffectAnim = new Animation(Utils.loadFrames(context, "effects/attack3"), 50);

        runAnim = new Animation(run, 30);
        jumpAnim = new Animation(jump, 90);
        idleAnim = new Animation(idle, 100);
        attackAnim1 = new Animation(normalAttack1, 60);
        attackAnim2 = new Animation(normalAttack2, 60);
        attackAnim3 = new Animation(normalAttack3, 90); // <-- chiêu 3
        s1SkillAnim = new Animation(s1Skill, 120);
        s2SkillAnim = new Animation(s2Skill, 70);

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
        if (s1SkillMoving) {
            float deltaX = facingRight ? s1SkillMoveSpeed : -s1SkillMoveSpeed;

            if (Math.abs(s1SkillTargetX - x) <= Math.abs(deltaX)) {
                x = s1SkillTargetX;
                s1SkillMoving = false;
            } else {
                x += deltaX;
            }
        }
        if (isAttacking) {
            currentAnim.update();

            if (currentAnim.isLastFrame()) {
                isAttacking = false;
                if (usingS1Skill) {
                    usingS1Skill = false;
                }
                if (usingS2Skill) {
                    usingS2Skill = false;
                }
               else if (comboQueued) {
                    comboStep++; // tăng bước combo
                    comboQueued = false;

                    if (comboStep == 2) {

                        // attack2
                        x += facingRight ? speed * 2.5 : -speed * 2.5; // lướt trước khi attack2
                        isAttacking = true;
                        currentAnim = attackAnim2;
                        attackAnim2.reset();
                        lastAttackTime = System.currentTimeMillis();
                    } else if (comboStep == 3) {
                        // attack3
                        x += facingRight ? speed * 2.5 : -speed * 2.5; // lướt trước khi attack3
                        isAttacking = true;
                        currentAnim = attackAnim3;
                        attackAnim3.reset();
                        lastAttackTime = System.currentTimeMillis();
                    }
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

        if (isAttacking) {
            // Nếu đang attack1 hoặc attack2, nhấn tiếp → queue combo
            if ((comboStep == 1 || comboStep == 2) && now - lastAttackTime <= comboMaxDelay) {
                comboQueued = true;
            }
            return;
        }

        // Nếu đang idle → start attack1
        comboStep = 1;
        isAttacking = true;
        currentAnim = attackAnim1;
        attackAnim1.reset();
        lastAttackTime = now;
    }
    public void useS1Skill() {
        if (isAttacking || usingS1Skill) return; // không dùng skill khi đang đánh hoặc đang skill khác

        usingS1Skill = true;
        isAttacking = true; // khóa movement

        currentAnim = s1SkillAnim;
        s1SkillAnim.reset();
//        s1SkillMoveSpeed = speed*3 ;
        s1SkillTargetX = x + (facingRight ? speed * 20 : -speed * 20);
        s1SkillMoving = true;

    }
    public void useS2Skill() {
        if (isAttacking || usingS2Skill) return; // không dùng skill khi đang đánh hoặc đang skill khác

        usingS2Skill = true;
        isAttacking = true; // khóa movement
        currentAnim = s2SkillAnim;
        s2SkillAnim.reset();
        s1SkillMoveSpeed = speed*3 ;
        x = x + (facingRight ? speed * 30 : -speed * 30);
    }

    public void takeDamage(int dmg) {
        if (!alive) return;

        hp -= dmg;
        if (hp <= 0) {
            alive = false;
            hp = 0;
            currentAnim = null; // hoặc animation chết
            // Có thể thêm logic chơi animation chết hoặc game over
        }
    }
    @Override
    public boolean isAttacking() {
        return isAttacking; // isAttacking là biến của Player/Boss
    }
    public Rect getBounds() {
        Bitmap frame = getCurrentFrame();
        if (frame == null) return new Rect((int)x, (int)y, (int)x, (int)y);
        int left = (int)(x - frame.getWidth()/2f);
        int top = (int)(y - frame.getHeight());
        int right = (int)(x + frame.getWidth()/2f);
        int bottom = (int)y;
        return new Rect(left, top, right, bottom);
    }


}
