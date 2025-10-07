package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;

import java.util.List;

public class Animation {
    private List<Bitmap> frames;
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private long frameDelay;

    public Animation(List<Bitmap> frames, long frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
    }

    public Bitmap getCurrentFrame() {
        if (frames == null || frames.size() == 0) return null;
        return frames.get(frameIndex);
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > frameDelay) {
            lastFrameTime = now;
            frameIndex++;
            if (frameIndex >= frames.size()) frameIndex = 0;
        }
    }

    public void reset() {
        frameIndex = 0;
        lastFrameTime = 0;
    }
}
