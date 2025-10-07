package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;
import java.util.List;

public class Animation {
    private List<Bitmap> frames;
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private long frameDelay;
    private boolean loop = true; // ✅ Thêm biến kiểm soát lặp

    public Animation(List<Bitmap> frames, long frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
    }

    public Animation(List<Bitmap> frames, long frameDelay, boolean loop) {
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.loop = loop;
    }

    public Bitmap getCurrentFrame() {
        if (frames == null || frames.isEmpty()) return null;
        return frames.get(frameIndex);
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > frameDelay) {
            lastFrameTime = now;
            frameIndex++;

            if (frameIndex >= frames.size()) {
                if (loop) {
                    frameIndex = 0;
                } else {
                    frameIndex = frames.size() - 1; // Dừng ở frame cuối nếu không lặp
                }
            }
        }
    }

    public void reset() {
        frameIndex = 0;
        lastFrameTime = 0;
    }

    // ✅ Kiểm tra đã tới frame cuối chưa
    public boolean isLastFrame() {
        return frameIndex == frames.size() - 1;
    }

    // ✅ Cho phép thay đổi loop nếu cần
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
}
