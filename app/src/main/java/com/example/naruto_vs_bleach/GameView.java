package com.example.naruto_vs_bleach;


import android.content.Context;
import android.graphics.BitmapFactory;
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

// Tạo các nút skill
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
            player.drawAt(canvas, paint, playerScreenX, player.y);
            joystick.draw(canvas, paint);
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
                        else { /* Xử lý S1–S4 */ }
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
}