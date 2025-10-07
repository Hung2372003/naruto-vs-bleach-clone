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

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private boolean running = false;
    private Paint paint = new Paint();

    private Player player;
    private Joystick joystick;

    private int groundY;
    private android.graphics.Bitmap background;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.goteijusantai);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        groundY = getHeight() - 50;
        player = new Player(getContext(), groundY);
        joystick = new Joystick(200, getHeight() - 200, 100, 200, getHeight() - 200, 50);

        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerFrame = 1000000000.0 / 60.0; // 60 FPS

        while (running) {
            long now = System.nanoTime();
            if (now - lastTime >= nsPerFrame) {
                update();
                drawCanvas();
                lastTime = now;
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

            // === 1️⃣ Scale nền để cao bằng màn hình ===
            float baseScale = (float) getHeight() / background.getHeight();
            mapScale = baseScale;
            mapWidth = Math.round(background.getWidth() * mapScale * 2.0f); // map gấp đôi màn hình

            // === 2️⃣ Đặt vị trí nhân vật ban đầu ở 1/3 chiều rộng map ===
            if (player.x == 200) { // chỉ gán một lần
                player.x = (int) (mapWidth / 3f);
            }

            // === 3️⃣ Camera theo nhân vật, bắt đầu soi từ 0 ===
            float halfScreen = getWidth() / 2f;
            cameraX = player.x - halfScreen;

            // === 4️⃣ Giới hạn camera trong map ===
            if (cameraX < 0) cameraX = 0;
            if (cameraX > mapWidth - getWidth()) cameraX = mapWidth - getWidth();

            // === 5️⃣ Giới hạn nhân vật không ra ngoài map ===
            float playerWidth = player.getCurrentFrame().getWidth();
            if (player.x < playerWidth / 2f)
                player.x = (int) (playerWidth / 2f);
            if (player.x > mapWidth - playerWidth / 2f)
                player.x = (int) (mapWidth - playerWidth / 2f);

            // === 6️⃣ Vẽ phần nền tương ứng với camera ===
            int srcLeft = Math.max(0, (int) (cameraX / mapScale));
            int srcRight = Math.min(background.getWidth(), (int) ((cameraX + getWidth()) / mapScale));
            Rect srcRect = new Rect(srcLeft, 0, srcRight, background.getHeight());
            Rect dstRect = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(background, srcRect, dstRect, paint);

            // === 7️⃣ Vẽ joystick ===
            joystick.draw(canvas, paint);

            // === 8️⃣ Vẽ nhân vật theo vị trí camera ===
            float playerScreenX = player.x - cameraX;
            player.drawAt(canvas, paint, playerScreenX, player.y);

        } finally {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }







    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Nếu chạm trong vùng joystick thì kích hoạt joystick
                float dx = x - joystick.baseX;
                float dy = y - joystick.baseY;
                if (Math.hypot(dx, dy) < joystick.baseRadius * 2) {
                    joystick.active = true;
                    joystick.knobX = x;
                    joystick.knobY = y;
                } else {
                    // Nếu chạm ngoài joystick -> nhân vật nhảy
                    player.jump();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (joystick.active) {
                    joystick.knobX = x;
                    joystick.knobY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (joystick.active) {
                    joystick.reset(); // nhả tay -> joystick về giữa
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