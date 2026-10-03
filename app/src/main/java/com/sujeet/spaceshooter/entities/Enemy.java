package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Enemy {

    private float x;
    private float y;

    private float speed;

    private final EnemyType type;

    private int health;
    private final int maxHealth;

    private float zigZagOffset = 0;

    private float width = 80;
    private float height = 70;

    private final RectF cachedBounds = new RectF();
    private final Path fastShipPath = new Path();

    public Enemy(
            float x,
            float y,
            float speed,
            EnemyType type) {

        this.x = x;
        this.y = y;

        this.type = type;

        switch (type) {

            case NORMAL:
                this.speed = speed;
                this.health = 1;
                this.width = 80;
                this.height = 70;
                break;

            case FAST:
                this.speed = speed * 1.8f;
                this.health = 1;
                this.width = 70;
                this.height = 60;
                break;

            case TANK:
                this.speed = speed * 0.65f;
                this.health = 3;
                this.width = 95;
                this.height = 85;
                break;

            case ZIGZAG:
                this.speed = speed;
                this.health = 2;
                this.width = 75;
                this.height = 70;
                break;
        }

        this.maxHealth = this.health;
    }

    public void update() {
        update(0);
    }

    public void update(int screenWidth) {

        y += speed;

        if (type == EnemyType.ZIGZAG) {

            zigZagOffset += 0.12f;

            x += Math.sin(zigZagOffset) * 6;
        }

        if (screenWidth > 0) {

            float halfW = width / 2;

            if (x < halfW) {

                x = halfW;

            } else if (x > screenWidth - halfW) {

                x = screenWidth - halfW;
            }
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public EnemyType getType() {
        return type;
    }

    public int getHealth() {
        return health;
    }

    public void damage() {
        health--;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public RectF getBounds() {
        cachedBounds.set(
                x - width / 2,
                y - height / 2,
                x + width / 2,
                y + height / 2
        );
        return cachedBounds;
    }

    public void draw(
            Canvas canvas,
            Paint paint) {

        switch (type) {

            case NORMAL:
                drawNormal(canvas, paint);
                break;

            case FAST:
                drawFast(canvas, paint);
                break;

            case TANK:
                drawTank(canvas, paint);
                break;

            case ZIGZAG:
                drawZigZag(canvas, paint);
                break;
        }

        if (maxHealth > 1) {
            drawHealthBar(canvas, paint);
        }
    }

    private void drawNormal(
            Canvas canvas,
            Paint paint) {

        // Plasma Glow Aura
        paint.setColor(Color.argb(80, 255, 50, 50));
        canvas.drawOval(
                x - width / 2 - 6,
                y - height / 2 - 6,
                x + width / 2 + 6,
                y + height / 2 + 6,
                paint
        );

        // Red Hull
        paint.setColor(Color.rgb(230, 30, 30));
        canvas.drawOval(
                x - width / 2,
                y - height / 2,
                x + width / 2,
                y + height / 2,
                paint
        );

        // Core / Cockpit
        paint.setColor(Color.rgb(255, 230, 0));
        canvas.drawCircle(x, y - 5, 12, paint);

        // Eye Dots
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x - 18, y + 5, 5, paint);
        canvas.drawCircle(x + 18, y + 5, 5, paint);
    }

    private void drawFast(
            Canvas canvas,
            Paint paint) {

        // Speed Trail Glow
        paint.setColor(Color.argb(90, 255, 0, 255));
        canvas.drawCircle(x, y - 10, 32, paint);

        // Arrowhead Dagger Ship
        fastShipPath.reset();
        fastShipPath.moveTo(x, y + height / 2);
        fastShipPath.lineTo(x - width / 2, y - height / 2);
        fastShipPath.lineTo(x, y - height / 4);
        fastShipPath.lineTo(x + width / 2, y - height / 2);
        fastShipPath.close();

        paint.setColor(Color.rgb(220, 0, 220));
        canvas.drawPath(fastShipPath, paint);

        // White Energy Core
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y - 5, 8, paint);
    }

    private void drawTank(
            Canvas canvas,
            Paint paint) {

        // Outer Energy Field / Aura Glow
        paint.setColor(Color.argb(80, 255, 120, 0));
        canvas.drawRoundRect(
                x - width / 2 - 6,
                y - height / 2 - 6,
                x + width / 2 + 6,
                y + height / 2 + 6,
                16, 16,
                paint
        );

        // Armored Cruiser Hull (Rounded Beveled Rect)
        paint.setColor(Color.rgb(70, 75, 90));
        canvas.drawRoundRect(
                x - width / 2,
                y - height / 2,
                x + width / 2,
                y + height / 2,
                14, 14,
                paint
        );

        // Inner Titanium Plate
        paint.setColor(Color.rgb(120, 130, 150));
        canvas.drawRoundRect(
                x - width / 2 + 10,
                y - height / 2 + 10,
                x + width / 2 - 10,
                y + height / 2 - 10,
                10, 10,
                paint
        );

        // Heavy Side Cannons / Turrets
        paint.setColor(Color.rgb(255, 140, 0));
        canvas.drawRect(x - width / 2 - 12, y - 18, x - width / 2, y + 18, paint);
        canvas.drawRect(x + width / 2, y - 18, x + width / 2 + 12, y + 18, paint);

        // Central Energy Core
        paint.setColor(Color.rgb(255, 30, 30));
        canvas.drawCircle(x, y, 16, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x, y, 8, paint);
    }

    private void drawZigZag(
            Canvas canvas,
            Paint paint) {

        // Electric Green Aura Ring
        paint.setColor(Color.argb(100, 0, 255, 120));
        canvas.drawCircle(x, y, 42, paint);

        // Saucer Body
        paint.setColor(Color.rgb(0, 220, 100));
        canvas.drawCircle(x, y, 36, paint);

        // Dark Visor
        paint.setColor(Color.rgb(10, 40, 20));
        canvas.drawOval(x - 22, y - 10, x + 22, y + 10, paint);

        // Twin Energy Orbs
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x - 12, y, 5, paint);
        canvas.drawCircle(x + 12, y, 5, paint);
    }

    private void drawHealthBar(
            Canvas canvas,
            Paint paint) {

        float barWidth = 70;
        float healthPercent = (float) health / maxHealth;

        paint.setColor(Color.DKGRAY);
        canvas.drawRect(
                x - barWidth / 2,
                y - height / 2 - 15,
                x + barWidth / 2,
                y - height / 2 - 8,
                paint
        );

        paint.setColor(Color.GREEN);
        canvas.drawRect(
                x - barWidth / 2,
                y - height / 2 - 15,
                x - barWidth / 2 + barWidth * healthPercent,
                y - height / 2 - 8,
                paint
        );
    }
}
