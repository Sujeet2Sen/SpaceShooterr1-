package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class EnemyBullet {

    private float x;
    private float y;

    private final float speed = 8f;
    private final float width = 12;
    private final float height = 30;

    public EnemyBullet(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += speed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isOutOfBounds(int screenHeight) {
        return y > screenHeight + 50;
    }

    public RectF getBounds() {
        return new RectF(
                x - width / 2,
                y - height / 2,
                x + width / 2,
                y + height / 2
        );
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        canvas.drawRect(getBounds(), paint);
    }
}
