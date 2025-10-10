package com.example.naruto_vs_bleach;

import android.content.Intent;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private boolean running = false;
    private boolean gameOverLaunched = false;

    private final Paint paint = new Paint();

    private Player player;
    private Boss boss;
    private Joystick joystick;
    private List<SkillButton> skillButtons;
    private int groundY;
    private final List<Integer> skillPointerIds = new ArrayList<>();

    private Bitmap background;
    private float cameraX = 0;
    private float mapScale;
    private int mapWidth;
    private float dpUnit = 1f;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.goteijusantai);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        float screenWidth = getWidth();
        float screenHeight = getHeight();
        float baseY = screenHeight - 250;

        // ---- Skill buttons ----
        dpUnit = Math.min(screenWidth, screenHeight) / 411f;
        float margin = 16f * dpUnit;
        float gap    = 14f * dpUnit;
        float safeBottom = 24f * dpUnit;

        float rA = 48f * dpUnit;   // nút A
        float rS = 36f * dpUnit;   // nút skill

// ĐẨY TÂM A VÀO TRONG = chừa chỗ cho nút bên phải & dưới
        float aCx = screenWidth  - margin - rA - (rS + gap);
        float aCy = screenHeight - margin - safeBottom - rA - (rS + gap);

// Khoảng cách tâm→tâm đảm bảo không chạm nhau
        float D = rA + rS + gap;

// Tính toạ độ vệ tinh quanh A (kim cương)
        float[] pS1 = polar(aCx, aCy, D,  45);   // trên
        float[] pS2 = polar(aCx, aCy, D, 135);   // trên-trái (dịch trái hơn chút so với 150)
        float[] pS3 = polar(aCx, aCy, D, 225);   // dưới-trái
        float[] pS4 = polar(aCx, aCy, D, 315);   // dưới-phải (không chạm mép nữa)

        skillButtons = new ArrayList<>();
        skillButtons.add(new SkillButton(aCx,      aCy,      rA, "A"));
        skillButtons.add(new SkillButton(pS1[0],   pS1[1],   rS, "S1"));
        skillButtons.add(new SkillButton(pS2[0],   pS2[1],   rS, "S2"));
        skillButtons.add(new SkillButton(pS3[0],   pS3[1],   rS, "S3"));
        skillButtons.add(new SkillButton(pS4[0],   pS4[1],   rS, "S4"));



        groundY = getHeight() - 50;

        // ---- Player & Boss ----
        player = new Player(getContext(), groundY);
        boss = new Boss(getContext(), groundY, player);

        // ---- Joystick ----
        joystick = new Joystick(200, getHeight() - 200, 100, 200, getHeight() - 200, 50);

        // ---- Start game thread ----
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    private float[] polar(float cx, float cy, float dist, float deg) {
        double rad = Math.toRadians(deg);
        float x = cx + (float) (Math.cos(rad) * dist);
        float y = cy - (float) (Math.sin(rad) * dist);

        // nẹp nhẹ tránh tràn quá mức, nhưng KHÔNG rút ngắn khoảng cách D
        float pad = 8f * dpUnit;
        x = Math.max(pad, Math.min(getWidth()  - pad, x));
        y = Math.max(pad, Math.min(getHeight() - pad, y));
        return new float[]{x, y};


    }

    @Override
    public void run() {
        final int targetFPS = 60;
        final long frameTime = 1000 / targetFPS;

        while (running) {
            long start = System.currentTimeMillis();
            update();
            drawCanvas();
            long sleep = frameTime - (System.currentTimeMillis() - start);
            if (sleep > 0) try { Thread.sleep(sleep); } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        // ---- Player update ----
        player.updateWithJoystick(joystick);
        player.update();

        // ---- Boss AI update ----
        boss.update();
        for (int i = player.projectiles.size() - 1; i >= 0; i--) {
            Projectile p = player.projectiles.get(i);
            p.update();

            // kiểm tra va chạm với boss
            if (Rect.intersects(p.getBounds(), boss.getBounds())) {
                boss.takeDamage(500); // damage chiêu S4
                player.projectiles.remove(i); // loại bỏ projectile
                continue;
            }

            // nếu projectile ra ngoài màn hình
            if (p.x < 0 || p.x > mapWidth) {
                player.projectiles.remove(i);
            }
        }
        // ---- Check collision ----
        checkCollisions();

        if (!gameOverLaunched) {
            if (boss != null && (!boss.alive || boss.hp <= 0)) {
                launchGameOver(true);      // Bạn thắng
                return;
            }
            if (player != null && (!player.alive || player.hp <= 0)) {
                launchGameOver(false);     // Bạn thua
                return;
            }
        }

    }
    private void launchGameOver(boolean win) {
        if (gameOverLaunched) return;
        gameOverLaunched = true;
        Context ctx = getContext();
        Intent it = new Intent(ctx, GameOverActivity.class);
        it.putExtra(GameOverActivity.EXTRA_RESULT, win ? "win" : "lose");
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(it);

        // dừng loop hiện tại cho sạch
        running = false;
    }


    private void checkCollisions() {
        Rect playerHitbox = player.getBounds();
        Rect bossHitbox = boss.getBounds();

        // Player attack hits Boss
        if ((player.isAttacking || player.usingS1Skill || player.usingS2Skill || player.usingS3Skill)
                && Rect.intersects(playerHitbox, bossHitbox)) {
            boss.takeDamage(10);
        }

        // Boss attack hits Player
        if ((boss.isAttacking() || boss.usingS1Skill || boss.usingS2Skill || boss.usingS3Skill)
                && Rect.intersects(playerHitbox, bossHitbox)) {
            player.takeDamage(10);
        }
    }


    private void drawCanvas() {
        if (!getHolder().getSurface().isValid()) return;
        Canvas canvas = getHolder().lockCanvas();
        try {
            canvas.drawColor(Color.BLACK);
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);

            // ---- Scale background ----
            float scaleY = (float) getHeight() / background.getHeight();
            mapScale = scaleY;
            float scaledBgWidth = background.getWidth() * scaleY;
            mapWidth = Math.round(scaledBgWidth * 2f);

            // ---- Camera follow player ----
            Bitmap playerFrame = player.getCurrentFrame();
            if (playerFrame != null) {
                float playerW = playerFrame.getWidth();
                if (player.x < playerW / 2f) player.x = playerW / 2f;
                if (player.x > mapWidth - playerW / 2f) player.x = mapWidth - playerW / 2f;
            }

            float visibleWidth = getWidth();
            cameraX = player.x - visibleWidth / 2f;
            if (cameraX < 0) cameraX = 0;
            if (cameraX > mapWidth - visibleWidth) cameraX = mapWidth - visibleWidth;

            // ---- Draw background twice ----
            for (int i = 0; i < 2; i++) {
                float bgX = i * scaledBgWidth - cameraX;
                Rect src = new Rect(0, 0, background.getWidth(), background.getHeight());
                Rect dst = new Rect((int) bgX, 0, (int) (bgX + scaledBgWidth), getHeight());
                canvas.drawBitmap(background, src, dst, paint);
            }

            // ---- Draw foot glows ----
            float playerScreenX = player.x - cameraX;
            float bossScreenX = boss.x - cameraX;

//            drawFootGlow(canvas, playerScreenX, player.y, player.getCurrentFrame().getWidth(),
//                    player.getCurrentFrame().getHeight(), Color.GREEN);
            drawFootGlow(canvas, bossScreenX, boss.y, boss.getCurrentFrame().getWidth(),
                    boss.getCurrentFrame().getHeight(), Color.RED);

            // ---- Draw characters ----
            player.drawAt(canvas, paint, playerScreenX, player.y);
            boss.drawAt(canvas, paint, bossScreenX, boss.y);

            // ---- Health bars ----
            drawHealthBars(canvas);

            // ---- UI ----
            joystick.draw(canvas, paint);
            for (SkillButton btn : skillButtons) btn.draw(canvas);

        } finally {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawHealthBars(Canvas canvas) {
        Paint p = new Paint();
        float barWidth = 400, barHeight = 40, pad = 20;

        // Player HP
        float left = pad, top = pad;
        p.setColor(Color.BLACK);
        canvas.drawRect(left, top, left + barWidth, top + barHeight, p);
        p.setColor(Color.RED);
        canvas.drawRect(left, top, left + barWidth * ((float) player.hp / player.maxHp), top + barHeight, p);

        // Boss HP
        float right = getWidth() - barWidth - pad;
        p.setColor(Color.BLACK);
        canvas.drawRect(right, top, right + barWidth, top + barHeight, p);
        p.setColor(Color.RED);
        canvas.drawRect(right, top, right + barWidth * ((float) boss.hp / boss.maxHp), top + barHeight, p);
    }

    private void drawFootGlow(Canvas canvas, float x, float y, float w, float h, int color) {
        Paint g = new Paint();
        g.setColor(color);
        g.setAlpha(100);
        g.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(x - w * 0.6f, y - h * 0.1f, x + w * 0.6f, y + h * 0.1f, g);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int idx = e.getActionIndex();
        int pid = e.getPointerId(idx);
        float x = e.getX(idx), y = e.getY(idx);

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_POINTER_DOWN:
                player.handleTouchDown(x, y, pid, joystick, skillButtons, skillPointerIds);
                break;
            case MotionEvent.ACTION_MOVE:
                player.handleTouchMove(e, joystick);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                player.handleTouchUp(pid, joystick, skillButtons, skillPointerIds);
                break;
        }
        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder h, int f, int w, int he) {}
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try { gameThread.join(); } catch (Exception ignored) {}
    }
}