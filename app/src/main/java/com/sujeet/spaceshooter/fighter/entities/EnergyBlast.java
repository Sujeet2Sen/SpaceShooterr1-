package com.sujeet.spaceshooter.fighter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class EnergyBlast {

    private float x;
    private final float y;
    private final float speed;
    private final boolean isPlayer;
    private final float radius = 25;
    private final RectF bounds = new RectF();

    public EnergyBlast(float x, float y, boolean isPlayer) {
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
        this.speed = isPlayer ? 18f : -18f;
    }

    public void update() {
        x += speed;
    }

    public float getX() {
        return x;
    }

    public boolean isOutOfBounds(int screenWidth) {
        return x < -50 || x > screenWidth + 50;
    }

    public RectF getBounds() {
        bounds.set(x - radius, y - radius, x + radius, y + radius);
        return bounds;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Outer energy aura
        paint.setColor(isPlayer ? Color.argb(150, 0, 255, 255) : Color.argb(150, 255, 50, 0));
        canvas.drawCircle(x, y, radius + 8, paint);

        // Core fireball
        paint.setColor(isPlayer ? Color.CYAN : Color.YELLOW);
        canvas.drawCircle(x, y, radius, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius * 0.5f, paint);
    }
}
