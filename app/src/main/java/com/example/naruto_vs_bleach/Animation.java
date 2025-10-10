package com.example.naruto_vs_bleach;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Animation implements Cloneable{
    private List<Bitmap> frames;
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private long frameDelay;
    private boolean loop = true;

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
                if (loop) frameIndex = 0;
                else frameIndex = frames.size() - 1;
            }
        }
    }

    public void reset() {
        frameIndex = 0;
        lastFrameTime = 0;
    }

    public boolean isLastFrame() {
        return frameIndex == frames.size() - 1;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    // ✅ Thêm method này để boss hoặc skill check frame hiện tại
    public int getCurrentFrameIndex() {
        return frameIndex;
    }
    @Override
    public Animation clone() {
        try {
            Animation copy = (Animation) super.clone(); // shallow copy
            copy.frames = new ArrayList<>(this.frames); // deep copy nếu cần
            copy.frameIndex = 0; // reset animation khi clone
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // sẽ không xảy ra
        }
    }
}

