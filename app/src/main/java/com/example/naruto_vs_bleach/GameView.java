package com.example.naruto_vs_bleach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private boolean running = false;

    private Bitmap background;

    // Nhân vật
    private ArrayList<Bitmap> runFrames = new ArrayList<>();
    private ArrayList<Bitmap> jumpFrames = new ArrayList<>();
    private ArrayList<Bitmap> stopFrames = new ArrayList<>();
    private Bitmap currentFrame;

    private int x = 200;
    private int y = 0;
    private int groundY = 0;
    private boolean facingRight = true;

    // Nhảy
    private boolean jumping = false;
    private int velocityY = 0;
    private final int jumpVelocity = -60;

    // Animation
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private final long frameDelay = 100;

    private Paint paint = new Paint();

    // Joystick
    private Joystick joystick;
    private Paint joystickPaint = new Paint();
    private ArrayList<RectF> actionButtons = new ArrayList<>();
    private Paint actionPaint = new Paint();
    private final float buttonSize = 120f;
    private final float buttonPadding = 20f;

    // Skill
    private ArrayList<Bitmap> skillFrames = new ArrayList<>();
    private boolean isSkillActive = false;
    private int skillFrameIndex = 0;
    private long lastSkillFrameTime = 0;
    private final long skillFrameDelay = 100;
    private Bitmap currentSkillFrame;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);

        joystickPaint.setColor(Color.BLACK);
        actionPaint.setColor(Color.argb(180, 200, 0, 0));

        loadFrames("run", runFrames);
        loadFrames("jump-up", jumpFrames);
        loadFrames("stop", stopFrames);

        currentFrame = stopFrames.size() > 0 ? stopFrames.get(0) : null;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.goteijusantai);
    }

    private void loadFrames(String folder, ArrayList<Bitmap> frames) {
        try {
            String[] files = getContext().getAssets().list("ichigo/" + folder);
            if (files != null) {
                java.util.Arrays.sort(files);
                for (String file : files) {
                    InputStream is = getContext().getAssets().open("ichigo/" + folder + "/" + file);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 3, bitmap.getHeight() * 3, true);
                    frames.add(scaled);
                    is.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSkillFrames() {
        try {
            String[] files = getContext().getAssets().list("ichigo/skill");
            if (files != null) {
                java.util.Arrays.sort(files);
                for (String file : files) {
                    InputStream is = getContext().getAssets().open("ichigo/skill/" + file);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 3, bitmap.getHeight() * 3, true);
                    skillFrames.add(scaled);
                    is.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSkill() {
        if (!isSkillActive && skillFrames.size() > 0) {
            isSkillActive = true;
            skillFrameIndex = 0;
            lastSkillFrameTime = System.currentTimeMillis();
            currentSkillFrame = skillFrames.get(0);
        }
    }

    private void initActionButtons() {
        actionButtons.clear();
        float startX = getWidth() - (3 * (buttonSize + buttonPadding)) - 50f;
        float startY = getHeight() - (2 * (buttonSize + buttonPadding)) - 50f;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                float left = startX + col * (buttonSize + buttonPadding);
                float top = startY + row * (buttonSize + buttonPadding);
                actionButtons.add(new RectF(left, top, left + buttonSize, top + buttonSize));
            }
        }
    }

    private void drawActionButtons(Canvas canvas) {
        for (RectF rect : actionButtons) {
            canvas.drawRoundRect(rect, 20f, 20f, actionPaint);
        }
    }

    private void handleActionButtons(float px, float py) {
        for (int i = 0; i < actionButtons.size(); i++) {
            RectF rect = actionButtons.get(i);
            if (rect.contains(px, py)) {
                switch (i) {
                    case 0: // nhảy
                        jumping = true;
                        velocityY = jumpVelocity;
                        frameIndex = 0;
                        break;
                    default: // skill
                        startSkill();
                        break;
                }
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            update();
            drawCanvas();
            try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void update() {
        long now = System.currentTimeMillis();

        // update frame
        if (now - lastFrameTime > frameDelay) {
            lastFrameTime = now;
            frameIndex++;
            if (jumping) {
                if (frameIndex >= jumpFrames.size()) frameIndex = jumpFrames.size() - 1;
                currentFrame = jumpFrames.get(frameIndex);
            } else if (joystick != null && joystick.active) {
                if (frameIndex >= runFrames.size()) frameIndex = 0;
                currentFrame = runFrames.get(frameIndex);
            } else {
                if (frameIndex >= stopFrames.size()) frameIndex = 0;
                currentFrame = stopFrames.get(frameIndex);
            }
        }

        // di chuyển joystick
        float vx = 0;
        if (joystick != null && joystick.active) {
            float dx = joystick.knobX - joystick.baseX;
            float dy = joystick.knobY - joystick.baseY;
            double angle = Math.toDegrees(Math.atan2(dy, dx));

            float speed = 30f;

            if (angle > -45 && angle < 45) vx = speed;
            else if (angle > 135 || angle < -135) vx = -speed;

            x += vx;
            facingRight = vx >= 0;
        }

        // giới hạn biên
        if (x < 0) x = 0;
        if (currentFrame != null && x > getWidth() - currentFrame.getWidth()) x = getWidth() - currentFrame.getWidth();

        // nhảy
        if (jumping) {
            y += velocityY;
            velocityY += 7;
            if (y >= groundY) {
                y = groundY;
                jumping = false;
                velocityY = 0;
                frameIndex = 0;
            }
        }

        // skill
        if (isSkillActive) {
            now = System.currentTimeMillis();
            if (now - lastSkillFrameTime > skillFrameDelay) {
                lastSkillFrameTime = now;
                skillFrameIndex++;
                if (skillFrameIndex >= skillFrames.size()) {
                    isSkillActive = false;
                    skillFrameIndex = 0;
                    currentSkillFrame = null;
                } else {
                    currentSkillFrame = skillFrames.get(skillFrameIndex);
                }
            }
        }
    }

    private void drawCanvas() {
        if (!getHolder().getSurface().isValid()) return;
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawBitmap(background, null, new android.graphics.Rect(0,0,getWidth(),getHeight()), paint);

        if (joystick != null) joystick.draw(canvas, joystickPaint);

        // nhân vật
        if (currentFrame != null) {
            Matrix matrix = new Matrix();
            if (!facingRight) {
                matrix.postScale(-1f, 1f);
                matrix.postTranslate(x + currentFrame.getWidth(), y - currentFrame.getHeight());
            } else {
                matrix.postTranslate(x, y - currentFrame.getHeight());
            }
            canvas.drawBitmap(currentFrame, matrix, paint);
        }

        if (currentSkillFrame != null) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(x, y - currentSkillFrame.getHeight());
            canvas.drawBitmap(currentSkillFrame, matrix, paint);
        }

        drawActionButtons(canvas);

        getHolder().unlockCanvasAndPost(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float px = event.getX();
        float py = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                boolean actionPressed = false;
                for (RectF rect : actionButtons) {
                    if (rect.contains(px, py)) {
                        handleActionButtons(px, py);
                        actionPressed = true;
                        break;
                    }
                }

                if (!actionPressed && joystick != null) {
                    joystick.active = true;
                    joystick.knobX = px;
                    joystick.knobY = py;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (joystick != null) joystick.reset();
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        groundY = getHeight() - 50;
        y = groundY;

        initActionButtons();
        loadSkillFrames();

        joystick = new Joystick(200, getHeight() - 200, 100, 200, getHeight() - 200, 50);

        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try { gameThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public void pause() { running = false; }
    public void resume() {
        if (!running) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
}