package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

public class Boss {

    private float x;
    private float y;

    private int health;
    private final int maxHealth;

    private final float width = 160;
    private final float height = 130;

    private float moveOffset = 0;
    private int shootTimer = 0;

    private final RectF cachedBounds = new RectF();

    public Boss(float x, float y, int level) {
        this.x = x;
        this.y = y;
        this.health = 20 + (level * 5);
        this.maxHealth = this.health;
    }

    public void update(int screenWidth, List<EnemyBullet> enemyBullets) {
        if (y < 150) {
            y += 2;
        } else {
            moveOffset += 0.05f;
            x += Math.sin(moveOffset) * 6;
        }

        if (screenWidth > 0) {
            float halfW = width / 2;
            if (x < halfW) {
                x = halfW;
            } else if (x > screenWidth - halfW) {
                x = screenWidth - halfW;
            }
        }

        // Shoot bullets at player
        shootTimer++;
        if (shootTimer >= 40) {
            shootTimer = 0;
            if (enemyBullets != null) {
                enemyBullets.add(new EnemyBullet(x - 40, y + height / 2));
                enemyBullets.add(new EnemyBullet(x + 40, y + height / 2));
            }
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
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

    public void draw(Canvas canvas, Paint paint) {
        // Outer Dreadnought Glow
        paint.setColor(Color.argb(90, 180, 0, 255));
        canvas.drawRect(
                x - width / 2 - 8,
                y - height / 2 - 8,
                x + width / 2 + 8,
                y + height / 2 + 8,
                paint
        );

        // Dark Purple Armored Hull
        paint.setColor(Color.rgb(100, 15, 150));
        canvas.drawRect(
                x - width / 2,
                y - height / 2,
                x + width / 2,
                y + height / 2,
                paint
        );

        // Armor Plating Detail
        paint.setColor(Color.rgb(150, 40, 210));
        canvas.drawRect(
                x - width / 2 + 15,
                y - height / 2 + 15,
                x + width / 2 - 15,
                y + height / 2 - 15,
                paint
        );

        // Glowing Pulsing Reactor Core
        paint.setColor(Color.rgb(255, 30, 30));
        canvas.drawCircle(x, y, 32, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x, y, 16, paint);

        // Heavy Cannon Wings
        paint.setColor(Color.rgb(255, 180, 0));
        canvas.drawRect(x - width / 2 - 25, y - 20, x - width / 2, y + 20, paint);
        canvas.drawRect(x + width / 2, y - 20, x + width / 2 + 25, y + 20, paint);

        // Health Bar
        drawHealthBar(canvas, paint);
    }

    private void drawHealthBar(Canvas canvas, Paint paint) {
        float barWidth = 150;
        float healthPercent = (float) health / maxHealth;

        // Health bar background with border
        paint.setColor(Color.BLACK);
        canvas.drawRect(
                x - barWidth / 2 - 3,
                y - height / 2 - 30,
                x + barWidth / 2 + 3,
                y - height / 2 - 15,
                paint
        );

        paint.setColor(Color.DKGRAY);
        canvas.drawRect(
                x - barWidth / 2,
                y - height / 2 - 28,
                x + barWidth / 2,
                y - height / 2 - 17,
                paint
        );

        paint.setColor(Color.GREEN);
        canvas.drawRect(
                x - barWidth / 2,
                y - height / 2 - 28,
                x - barWidth / 2 + barWidth * healthPercent,
                y - height / 2 - 17,
                paint
        );
    }
}
