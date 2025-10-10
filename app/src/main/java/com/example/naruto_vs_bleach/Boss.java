package com.example.naruto_vs_bleach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import java.util.Random;

public class Boss extends Player {
    private final Player target;
    private final Random random = new Random();
    private long lastActionTime = 0;
    private final long actionInterval = 50; // update liên tục
    private final float ATTACK_RANGE = 120f;

    public Boss(Context context, int groundY, Player target) {
        super(context, groundY);
        this.target = target;
        maxHp = 1500; hp = maxHp; speed = 30f;
        x = target.x + 600;
        facingRight = false;
        hitboxScaleX = 0.5f; hitboxScaleY = 0.8f;
    }
    public boolean alive = true; // nếu chưa có
    public void takeDamage(int dmg) {
        if (!alive) return;
        hp -= Math.max(0, dmg);
        if (hp <= 0) {
            hp = 0;
            alive = false;   // 🔴 đánh dấu chết tại đây
        }
    }

    @Override
    public void update() {
        super.update();
        if (!alive) return;

        // hướng về player
        facingRight = target.x > x;

        long now = System.currentTimeMillis();
        if (now - lastActionTime > actionInterval) {
            lastActionTime = now;
            aiMove();
        }
        float dx = target.getX() - x;
        moveBossTowardPlayer(dx);
        // --- đẩy nhau nếu chạm ---
        handleCollisionWithPlayer();
    }

    private void aiMove() {
        float dx = target.x - x;
        float distance = Math.abs(dx);

        if (distance > ATTACK_RANGE) moveTowardPlayer(dx);
        else if (!isAttacking) {
            int action = random.nextInt(4);
            switch (action) {
                case 0: attack(); break;
                case 1: useS1Skill(); break;
                case 2: useS2Skill(); break;
                case 3: useS3Skill(); break;
            }
        }
    }

    private void moveTowardPlayer(float dx) {
        // tính vị trí sắp đi tới
        float nextX = x + Math.signum(dx) * speed * 0.6f;

        // hitbox vật lý sắp tới
        Rect nextBounds = new Rect(getPhysicsBounds());
        nextBounds.offset((int)(nextX - x), 0);

        // nếu không va chạm với Player
        if (!Rect.intersects(nextBounds, target.getPhysicsBounds())) {
            x = nextX;  // di chuyển
            if (!isAttacking && !jumping) currentAnim = runAnim;
        } else {
            currentAnim = idleAnim; // dừng lại nếu va chạm
        }

        // luôn hướng về Player
        facingRight = dx > 0;
    }


    private void handleCollisionWithPlayer() {
        Rect bossBox = getBounds();
        Rect playerBox = target.getBounds();
        if (!Rect.intersects(bossBox, playerBox)) return;

        float overlapX = Math.min(bossBox.right, playerBox.right) - Math.max(bossBox.left, playerBox.left);
        if (overlapX <= 0) return;

        // đẩy 2 bên đều nhau
        float dx = target.x - x;
        float push = overlapX / 2f +7;
        if (dx > 0) { target.x += push; x -= push; }
        else { target.x -= push; x += push; }
    }
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
    private void idle() {
        if (!isAttacking && !jumping) {
            currentAnim = idleAnim;
        }
    }
    private void moveBossTowardPlayer(float dx) {
        float nextX = x + Math.signum(dx) * speed * 0.6f;

        // Tạo hitbox giả sắp di chuyển
        Rect nextBounds = new Rect(getBounds());
        nextBounds.offset((int)(nextX - x), 0);

        // Nếu không chạm player thì di chuyển
        if (!Rect.intersects(nextBounds, target.getBounds())) {
            x = nextX;
            if (!isAttacking && !jumping) currentAnim = runAnim;
        } else {
            idle(); // chạm thì đứng yên
        }
    }
}

