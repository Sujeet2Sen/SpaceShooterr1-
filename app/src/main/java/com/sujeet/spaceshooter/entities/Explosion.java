package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Explosion {

    private final float x;
    private final float y;

    private int frame = 0;

    private final int maxFrames = 20;

    public Explosion(
            float x,
            float y) {

        this.x = x;
        this.y = y;
    }

    public void update() {

        frame++;
    }

    public boolean isFinished() {

        return frame >= maxFrames;
    }

    public void draw(
            Canvas canvas,
            Paint paint) {

        if (isFinished()) {

            return;
        }

        float progress = (float) frame / maxFrames;
        float radius = frame * 5;

        // Outer red/orange ring
        paint.setColor(
                Color.argb(
                        (int) (255 * (1 - progress)),
                        255,
                        100,
                        0
                )
        );

        canvas.drawCircle(
                x,
                y,
                radius,
                paint
        );

        // Inner yellow ring
        paint.setColor(
                Color.argb(
                        (int) (255 * (1 - progress)),
                        255,
                        255,
                        0
                )
        );

        canvas.drawCircle(
                x,
                y,
                radius * 0.7f,
                paint
        );

        // Center white flash
        if (frame < 8) {

            paint.setColor(Color.WHITE);

            canvas.drawCircle(
                    x,
                    y,
                    radius * 0.3f,
                    paint
            );
        }
    }
}
