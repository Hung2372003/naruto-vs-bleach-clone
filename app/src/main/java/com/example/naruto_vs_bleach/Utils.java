package com.example.naruto_vs_bleach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static List<Bitmap> loadFrames(Context context, String folder) {
        List<Bitmap> frames = new ArrayList<>();
        try {
            String[] files = context.getAssets().list(folder);
            if (files != null) {
                Arrays.sort(files);
                for (String file : files) {
                    InputStream is = context.getAssets().open(folder + "/" + file);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 3, bitmap.getHeight() * 3, true);
                    frames.add(scaled);
                    is.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return frames;
    }
}