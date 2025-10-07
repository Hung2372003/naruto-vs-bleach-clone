package com.example.naruto_vs_bleach;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private boolean running = false;
    private Paint paint = new Paint();

    private Player player;
    private Joystick joystick;

    private int groundY;
    private android.graphics.Bitmap background;
    private List<SkillButton> skillButtons;
    private int joystickPointerId = -1; // pointer đang điều khiển joystick
    private final List<Integer> skillPointerIds = new ArrayList<>(); // các pointer đang nhấn skill

    private Boss boss;
    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.goteijusantai);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Kích thước màn hình giả định
        float screenWidth = getWidth();
        float screenHeight = getHeight();
        float baseY = screenHeight - 250;

        float bigRadius = 120;
        float smallRadius = 90;

        skillButtons = new ArrayList<>();
        skillButtons.add(new SkillButton(screenWidth - 250, baseY, bigRadius, "A")); // Đánh thường
        skillButtons.add(new SkillButton(screenWidth - 480, baseY - 180, smallRadius, "S1"));
        skillButtons.add(new SkillButton(screenWidth - 150, baseY - 220, smallRadius, "S2"));
        skillButtons.add(new SkillButton(screenWidth - 400, baseY + 80, smallRadius, "S3"));
        skillButtons.add(new SkillButton(screenWidth - 80, baseY + 60, smallRadius, "S4"));

        groundY = getHeight() - 50;

        player = new Player(getContext(), groundY);
        boss = new Boss(getContext(), groundY);
        boss.setMapWidth(mapWidth);
        boss.x = player.x + 600;  // cách Player 600px
        boss.y = groundY;
        boss.facingRight = false;
        joystick = new Joystick(200, getHeight() - 200, 100, 200, getHeight() - 200, 50);

        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


    @Override
    public void run() {
        final int targetFPS = 60;
        final long targetTime = 1000 / targetFPS; // mili giây mỗi khung hình

        while (running) {
            long start = System.currentTimeMillis();

            update();
            drawCanvas();

            long elapsed = System.currentTimeMillis() - start;
            long sleepTime = targetTime - elapsed;
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void update() {

        player.updateWithJoystick(joystick);
        player.update();

        bossAI();
        boss.update();
        handleCollision();
        checkCollisions();

    }
    private void checkCollisions() {
        // Player attack trúng Boss
        if (player.isAttacking || player.usingS1Skill || player.usingS2Skill) {
            float dx = Math.abs(player.x - boss.x);
            if (dx < 100) { // khoảng cách trúng
                boss.takeDamage(10); // có thể tăng dmg theo attack
            }
        }

        // Boss attack trúng Player
        if (boss.isAttacking() || boss.usingS1Skill || boss.usingS2Skill) {
            float dx = Math.abs(player.x - boss.x);
            if (dx < 100) {
                player.takeDamage(10);
            }
        }
    }
    private float cameraX = 0; // vị trí camera (góc trái màn hình)
    private float mapScale;    // tỷ lệ phóng nền
    private int mapWidth;      // độ rộng map sau khi scale

    private void drawCanvas() {
        if (!getHolder().getSurface().isValid()) return;
        Canvas canvas = getHolder().lockCanvas();
        try {
            canvas.drawColor(Color.BLACK);
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);

            // === 1️⃣ Scale nền theo chiều cao màn hình ===
            float scaleY = (float) getHeight() / background.getHeight();
            mapScale = scaleY;

            // === 2️⃣ Map rộng gấp đôi nền thật sau khi scale ===
            float scaledBgWidth = background.getWidth() * scaleY;
            mapWidth = Math.round(scaledBgWidth * 2f);

            // === 3️⃣ Giới hạn di chuyển player ===
            float playerWidth = player.getCurrentFrame().getWidth();
            if (player.x < playerWidth / 2f)
                player.x = playerWidth / 2f;
            if (player.x > mapWidth - playerWidth / 2f)
                player.x = mapWidth - playerWidth / 2f;

            // === 4️⃣ Camera theo player ===
            float visibleWidth = getWidth();
            cameraX = player.x - visibleWidth / 2f;
            if (cameraX < 0) cameraX = 0;
            if (cameraX > mapWidth - visibleWidth)
                cameraX = mapWidth - visibleWidth;

            // === 5️⃣ Vẽ nền map (2 lần nối tiếp nhau) ===
            for (int i = 0; i < 2; i++) {
                float bgX = i * scaledBgWidth - cameraX;
                Rect srcRect = new Rect(0, 0, background.getWidth(), background.getHeight());
                Rect dstRect = new Rect((int) bgX, 0, (int) (bgX + scaledBgWidth), getHeight());
                canvas.drawBitmap(background, srcRect, dstRect, paint);
            }

            // === 6️⃣ Vẽ player ===
            float playerScreenX = player.x - cameraX;
            drawFootGlow(canvas, player.x - cameraX, player.y, player.getCurrentFrame().getWidth(), player.getCurrentFrame().getHeight(), Color.GREEN);
            player.drawAt(canvas, paint, playerScreenX, player.y);
            if (boss != null) {
                boss.drawAt(canvas, paint, boss.x - cameraX, boss.y);
            }
            drawFixedHealthBars(canvas, paint);
            joystick.draw(canvas, paint);


////            drawFixedHealthBars(canvas, paint);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setColor(Color.RED);
//            paint.setStrokeWidth(3);
//
//// Lấy kích thước frame hiện tại
//            Bitmap playerFrame = player.getCurrentFrame();
//            if (playerFrame != null) {
//                float left = player.x - playerFrame.getWidth() / 2f - cameraX;
//                float top = player.y - playerFrame.getHeight();
//                float right = player.x + playerFrame.getWidth() / 2f - cameraX;
//                float bottom = player.y;
//
//                canvas.drawRect(left, top, right, bottom, paint);
//            }
//
//// Vẽ hitbox Boss (debug)
//            Bitmap bossFrame = boss.getCurrentFrame();
//            if (bossFrame != null) {
//                float left = boss.x - bossFrame.getWidth() / 2f - cameraX;
//                float top = boss.y - bossFrame.getHeight();
//                float right = boss.x + bossFrame.getWidth() / 2f - cameraX;
//                float bottom = boss.y;
//
//                canvas.drawRect(left, top, right, bottom, paint);
//            }

// Reset paint
            paint.setStyle(Paint.Style.FILL);
            for (SkillButton button : skillButtons) {
                button.draw(canvas, paint);
            }
        } finally {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // sự kiện hiện tại (DOWN, UP, MOVE)
        int pointerIndex = event.getActionIndex(); // index của pointer
        int pointerId = event.getPointerId(pointerIndex); // id của pointer hiện tại
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // --- Kiểm tra skill button ---
                boolean skillTouched = false;
                for (SkillButton button : skillButtons) {
                    if (button.isTouched(x, y)) {
                        button.isPressed = true;
                        skillPointerIds.add(pointerId); // lưu pointer đang nhấn skill
                        skillTouched = true;
                        if (button.label.equals("A")) player.attack();
                        else if(button.label.equals("S1")){
                            player.useS1Skill();
                        }
                        else if(button.label.equals("S2")){
                            player.useS2Skill();
                        }
                        break;
                    }
                }

                // --- Nếu không chạm skill thì kiểm tra joystick ---
                if (!skillTouched && joystickPointerId == -1 &&
                        Math.hypot(x - joystick.baseX, y - joystick.baseY) <= joystick.baseRadius * 2) {
                    joystick.active = true;
                    joystick.knobX = x;
                    joystick.knobY = y;
                    joystickPointerId = pointerId; // lưu pointer điều khiển joystick
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // Cập nhật joystick nếu pointer đang điều khiển nó
                if (joystickPointerId != -1) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == joystickPointerId) {
                            joystick.knobX = event.getX(i);
                            joystick.knobY = event.getY(i);
                            break;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                // --- Kết thúc joystick ---
                if (pointerId == joystickPointerId) {
                    joystick.reset();
                    joystickPointerId = -1;
                }

                // --- Kết thúc các skill button ---
                if (skillPointerIds.contains(pointerId)) {
                    skillPointerIds.remove((Integer) pointerId);
                    for (SkillButton button : skillButtons) {
                        if (button.isPressed) button.isPressed = false;
                    }
                }
                break;
        }
        return true;
    }


    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int he) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try { gameThread.join(); } catch (Exception e) {}
    }
    private void drawFixedHealthBars(Canvas canvas, Paint paint) {
        float barWidth = 400, barHeight = 40, padding = 20;

        // Player góc trái
        float left = padding, top = padding;
        float right = left + barWidth * ((float) player.hp / player.maxHp);
        canvas.drawRect(left, top, left + barWidth, top + barHeight, paint); // viền đen
        paint.setColor(0xFFFF0000);
        canvas.drawRect(left, top, right, top + barHeight, paint);

        // Boss góc phải
        float bossLeft = getWidth() - barWidth - padding;
        float bossTop = padding;
        float bossRight = bossLeft + barWidth * ((float) boss.hp / boss.maxHp);
        paint.setColor(0xFF000000);
        canvas.drawRect(bossLeft, bossTop, bossLeft + barWidth, bossTop + barHeight, paint);
        paint.setColor(0xFFFF0000);
        canvas.drawRect(bossLeft, bossTop, bossRight, bossTop + barHeight, paint);
    }
    private void bossAI() {
        float distance = player.x - boss.x;

        if (Math.abs(distance) > 150) {
            boss.facingRight = distance > 0;
            boss.x += Math.signum(distance) * boss.speed * 0.5f;

            if (!boss.isAttacking) {
                boss.currentAnim = boss.runAnim; // ✅ đổi animation chạy
            }
        } else {
            if (!boss.isAttacking) {
                // Ngẫu nhiên đánh hoặc dùng skill
                if (Math.random() < 0.5) boss.attack();
                else if (Math.random() < 0.5) boss.useS1Skill();
                else boss.useS2Skill();
            }
        }

        // luôn update animation
        if (boss.currentAnim != null) boss.currentAnim.update();
    }
    private void handleCollision() {
        Rect playerRect = player.getBounds();
        Rect bossRect = boss.getBounds();

        if (Rect.intersects(playerRect, bossRect)) {
            // Tính overlap ngang
            float overlapLeft = playerRect.right - bossRect.left -50;
            float overlapRight = bossRect.right - playerRect.left -50;

            // Chọn overlap nhỏ nhất để đẩy ra
            float overlap = Math.min(overlapLeft, overlapRight);

            if (overlapLeft < overlapRight) player.x -= overlap;
            else player.x += overlap;

            // Gây sát thương nếu đang tấn công
            if (player.isAttacking || player.usingS1Skill || player.usingS2Skill)
                boss.takeDamage(10);
            if (boss.isAttacking() || boss.usingS1Skill || boss.usingS2Skill)
                player.takeDamage(10);
        }
    }
    private void drawFootGlow(Canvas canvas, float x, float y, float width, float height, int color) {
        Paint glowPaint = new Paint();
        glowPaint.setColor(color);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setAlpha(120); // độ trong suốt
        glowPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL)); // làm mềm

        float cx = x;                 // tâm ngang
        float cy = y;                 // y = chân nhân vật
        float radiusX = width * 0.6f; // rộng hơn chân một chút
        float radiusY = height * 0.2f; // mỏng dọc theo y

        canvas.drawOval(cx - radiusX, cy - radiusY, cx + radiusX, cy + radiusY, glowPaint);
    }

}