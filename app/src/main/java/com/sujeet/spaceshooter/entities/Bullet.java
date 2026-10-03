package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Bullet {

    private final float x;
    private float y;

    private final float speed = 20;

    public Bullet(
            float x,
            float y) {

        this.x = x;
        this.y = y;
    }

    public void update() {

        y -= speed;
    }

    public float getY() {

        return y;
    }

    public RectF getBounds() {

        return new RectF(
                x - 6,
                y - 20,
                x + 6,
                y + 20
        );
    }

    public void draw(
            Canvas canvas,
            Paint paint) {

        // Outer glow
        paint.setColor(Color.argb(120, 0, 255, 255));
        canvas.drawRect(x - 7, y - 22, x + 7, y + 22, paint);

        // Inner core
        paint.setColor(Color.rgb(255, 255, 100));

        canvas.drawRect(
                x - 3,
                y - 20,
                x + 3,
                y + 20,
                paint
        );
    }
}
