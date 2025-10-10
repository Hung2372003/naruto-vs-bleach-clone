package com.example.naruto_vs_bleach;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class Player extends GameObject {
    public int maxHp;
    public int hp;
    public boolean alive = true;
    public Animation runAnim, jumpAnim, idleAnim;
    private Animation attackAnim1, attackAnim2, attackAnim3;
    private Animation s1SkillAnim, s2SkillAnim, s3SkillAnim, s4SkillAnim;
    private Animation attack3EffectAnim;
    public Animation currentAnim;

    public boolean jumping = false;
    private int velocityY = 0;
    private final int jumpVelocity = -50;
    private int groundY;
    private final int gravity = 5;

    public boolean isAttacking = false;
    public boolean usingS1Skill = false;
    public boolean usingS2Skill = false;
    public boolean usingS3Skill = false;
    public boolean usingS4Skill = false;

    private int comboStep = 0;
    private long lastAttackTime = 0;
    private final long comboMaxDelay = 300;
    private boolean comboQueued = false;

    public float speed;
    private final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    private final List<Effect> activeEffects = new ArrayList<>();
    private boolean s1SkillMoving = false;
    private float s1SkillTargetX = 0;
    private float s1SkillMoveSpeed = 100f;

    // Dùng cho input
    private int joystickPointerId = -1;
    protected float hitboxScaleX = 0.5f; // mặc định 50% chiều rộng
    protected float hitboxScaleY = 0.8f; // mặc định 80% chiều cao
    // thêm vào lớp Player
//    public List<Projecttile> getProjectiles() {
//        return Projecttile;
//    }

    public Player(Context context, int groundY) {
        this.groundY = groundY;

        // ✅ Đọc cài đặt từ SettingsStore
        SettingsStore settings = new SettingsStore(context);
        int hpMul = settings.getHpMultiplier();
        int difficulty = settings.getDifficulty();

        maxHp = 50 * hpMul;
        hp = maxHp;

        switch (difficulty) {
            case SettingsStore.DIFF_EASY:
                speed = 40f;
                break;
            case SettingsStore.DIFF_NORMAL:
                speed = 35f;
                break;
            case SettingsStore.DIFF_HARD:
                speed = 30f;
                break;
            default:
                speed = 35f;
                break;
        }

        // === Load animation ===
        List<Bitmap> run = Utils.loadFrames(context, "ichigo-convert/move");
        List<Bitmap> jump = Utils.loadFrames(context, "ichigo-convert/jump");
        List<Bitmap> idle = Utils.loadFrames(context, "ichigo-convert/stance");
        List<Bitmap> normalAttack1 = Utils.loadFrames(context, "ichigo-convert/attack");
        List<Bitmap> normalAttack2 = Utils.loadFrames(context, "ichigo-convert/b-forward-attack");
        List<Bitmap> normalAttack3 = Utils.loadFrames(context, "ichigo-convert/b-down-attack");
        List<Bitmap> s1Skill = Utils.loadFrames(context, "ichigo-convert/y-attack");
        List<Bitmap> s2Skill = Utils.loadFrames(context, "ichigo-convert/y-forword-attack");
        List<Bitmap> s3Skill = Utils.loadFrames(context, "ichigo-convert/dash");
        List<Bitmap> s4Skill = Utils.loadFrames(context, "ichigo-convert/until");

        attack3EffectAnim = new Animation(Utils.loadFrames(context, "effects/attack3"), 50);

        runAnim = new Animation(run, 30);
        jumpAnim = new Animation(jump, 90);
        idleAnim = new Animation(idle, 100);
        attackAnim1 = new Animation(normalAttack1, 60);
        attackAnim2 = new Animation(normalAttack2, 60);
        attackAnim3 = new Animation(normalAttack3, 90);
        s1SkillAnim = new Animation(s1Skill, 120);
        s2SkillAnim = new Animation(s2Skill, 70);
        s3SkillAnim = new Animation(s3Skill, 150);
        s4SkillAnim = new Animation(s4Skill, 150);

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
            } else x += deltaX;
        }

        if (isAttacking) {
            currentAnim.update();
            if (currentAnim.isLastFrame()) {
                isAttacking = false;
                usingS1Skill = false;
                usingS2Skill = false;
                usingS3Skill = false;
                usingS4Skill = false;

                if (comboQueued) {
                    comboStep++;
                    comboQueued = false;
                    if (comboStep == 2) startAttack2();
                    else if (comboStep == 3) startAttack3();
                } else {
                    comboStep = 0;
                    currentAnim = idleAnim;
                }
            }
            return;
        }

        currentAnim.update();
    }

    private void startAttack2() {
        x += facingRight ? speed * 2.5 : -speed * 2.5;
        isAttacking = true;
        currentAnim = attackAnim2;
        attackAnim2.reset();
        lastAttackTime = System.currentTimeMillis();
    }

    private void startAttack3() {
        x += facingRight ? speed * 2.5 : -speed * 2.5;
        isAttacking = true;
        currentAnim = attackAnim3;
        attackAnim3.reset();
        lastAttackTime = System.currentTimeMillis();
    }

    public void updateWithJoystick(Joystick joystick) {
        if (isAttacking) return;

        if (joystick.active) {
            float angle = joystick.getAngle();
            float moveX = (float) Math.cos(angle) * speed;
            float moveY = (float) Math.sin(angle) * speed;

            x += moveX;
            if (moveY < -5 && !jumping) {
                jump();
                return;
            }

            facingRight = moveX >= 0;
            if (!jumping && Math.abs(moveX) > 1f) currentAnim = runAnim;
        } else if (!jumping) {
            currentAnim = idleAnim;
        }

    }

    public Bitmap getCurrentFrame() { return currentAnim.getCurrentFrame(); }

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

    public void jump() {
        if (!jumping && !isAttacking) {
            jumping = true;
            velocityY = jumpVelocity;
            currentAnim = jumpAnim;
            jumpAnim.reset();
        }
    }

    public void attack() {
        long now = System.currentTimeMillis();
        if (isAttacking) {
            if ((comboStep == 1 || comboStep == 2) && now - lastAttackTime <= comboMaxDelay)
                comboQueued = true;
            return;
        }
        comboStep = 1;
        isAttacking = true;
        currentAnim = attackAnim1;
        attackAnim1.reset();
        lastAttackTime = now;
    }

    public void useS1Skill() {
        if (isAttacking || usingS1Skill) return;
        usingS1Skill = true;
        isAttacking = true;
        currentAnim = s1SkillAnim;
        s1SkillAnim.reset();
        s1SkillTargetX = x + (facingRight ? speed * 20 : -speed * 20);
        s1SkillMoving = true;
    }

    public void useS2Skill() {
        if (isAttacking || usingS2Skill) return;
        usingS2Skill = true;
        isAttacking = true;
        currentAnim = s2SkillAnim;
        s2SkillAnim.reset();
        s1SkillMoveSpeed = speed * 3;
        x = x + (facingRight ? speed * 30 : -speed * 30);
    }

    public void useS3Skill() {
        if (isAttacking || usingS3Skill) return; // fix: check đúng biến
        usingS3Skill = true;
        isAttacking = true;
        currentAnim = s3SkillAnim;
        s3SkillAnim.reset();
        x = x + (facingRight ? speed * 30 : -speed * 30);
    }
    public void useS4Skill() {
        if (isAttacking || usingS4Skill) return; // fix: check đúng biến
        usingS4Skill = true;
        isAttacking = true;
        currentAnim = s4SkillAnim;
        s4SkillAnim.reset();
        //x = x + (facingRight ? speed * 30 : -speed * 30);

    }

    public void takeDamage(int dmg) {
        if (!alive) return;
        hp -= dmg;
        if (hp <= 0) {
            alive = false;
            hp = 0;
            currentAnim = null;
        }
    }

    @Override
    public boolean isAttacking() { return isAttacking; }


    @Override
    public Rect getBounds() {
        Bitmap frame = getCurrentFrame();
        if (frame == null) return new Rect((int)x, (int)y, (int)x, (int)y);

        int width = frame.getWidth();
        int height = frame.getHeight();

        int hitWidth = (int)(width * 0.6f);  // 60% width
        int hitHeight = (int)(height * 0.8f); // 80% height

        int left = (int)(x - hitWidth/2f);
        int top = (int)(y - hitHeight);
        int right = left + hitWidth;
        int bottom = top + hitHeight;

        return new Rect(left, top, right, bottom);
    }


    // ==========================
    // 📱 Xử lý touch input ở đây
    // ==========================
    public void handleTouchDown(float x, float y, int pointerId,
                                Joystick joystick, List<SkillButton> skillButtons, List<Integer> skillPointerIds) {
        boolean skillTouched = false;

        for (SkillButton button : skillButtons) {
            if (button.isTouched(x, y)) {
                button.isPressed = true;
                skillPointerIds.add(pointerId);
                skillTouched = true;
                switch (button.label) {
                    case "A": attack(); break;
                    case "S1": useS1Skill(); break;
                    case "S2": useS2Skill(); break;
                    case "S3": useS3Skill(); break;
                    case "S4": useS4Skill(); break;
                }
                break;
            }
        }

        if (!skillTouched && joystickPointerId == -1 &&
                Math.hypot(x - joystick.baseX, y - joystick.baseY) <= joystick.baseRadius * 2) {
            joystick.active = true;
            joystick.knobX = x;
            joystick.knobY = y;
            joystickPointerId = pointerId;
        }
    }

    public void handleTouchMove(MotionEvent event, Joystick joystick) {
        if (joystickPointerId != -1) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                if (event.getPointerId(i) == joystickPointerId) {
                    joystick.knobX = event.getX(i);
                    joystick.knobY = event.getY(i);
                    break;
                }
            }
        }
    }

    public void handleTouchUp(int pointerId, Joystick joystick,
                              List<SkillButton> skillButtons, List<Integer> skillPointerIds) {
        if (pointerId == joystickPointerId) {
            joystick.reset();
            joystickPointerId = -1;
        }

        if (skillPointerIds.contains(pointerId)) {
            skillPointerIds.remove((Integer) pointerId);
            for (SkillButton button : skillButtons) {
                button.isPressed = false;
            }
        }
    }
    public Rect getPhysicsBounds() {
        Bitmap frame = getCurrentFrame();
        if (frame == null) return new Rect((int)x, (int)y, (int)x, (int)y);

        int w = (int)(frame.getWidth() * hitboxScaleX);
        int h = (int)(frame.getHeight() * hitboxScaleY);

        int left = (int)(x - w/2);
        int top = (int)(y - h);
        return new Rect(left, top, left + w, top + h);
    }


}
