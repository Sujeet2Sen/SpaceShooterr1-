package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Player {

    private float x;
    private float y;

    private final float width = 80;
    private final float height = 100;

    public Player(float x, float y) {

        this.x = x;
        this.y = y;
    }

    public void setX(float x) {

        this.x = x;
    }

    public void setY(float y) {

        this.y = y;
    }

    public float getX() {

        return x;
    }

    public float getY() {

        return y;
    }

    public float getWidth() {

        return width;
    }

    public float getHeight() {

        return height;
    }

    public RectF getBounds() {

        return new RectF(
                x - width / 2,
                y - height / 2,
                x + width / 2,
                y + height / 2
        );
    }

    public void draw(
            Canvas canvas,
            Paint paint) {

        // Outer glow
        paint.setColor(Color.argb(100, 0, 255, 255));
        Path glowPath = new Path();
        glowPath.moveTo(x, y - height / 2 - 6);
        glowPath.lineTo(x - width / 2 - 6, y + height / 2 + 6);
        glowPath.lineTo(x, y + height / 4);
        glowPath.lineTo(x + width / 2 + 6, y + height / 2 + 6);
        glowPath.close();
        canvas.drawPath(glowPath, paint);

        // Main Ship Hull
        Path ship = new Path();

        ship.moveTo(
                x,
                y - height / 2
        );

        ship.lineTo(
                x - width / 2,
                y + height / 2
        );

        ship.lineTo(
                x - width / 4,
                y + height / 3
        );

        ship.lineTo(
                x,
                y + height / 4
        );

        ship.lineTo(
                x + width / 4,
                y + height / 3
        );

        ship.lineTo(
                x + width / 2,
                y + height / 2
        );

        ship.close();

        paint.setColor(Color.rgb(0, 220, 255));

        canvas.drawPath(
                ship,
                paint
        );

        // Cockpit Glass
        paint.setColor(Color.rgb(20, 50, 100));

        canvas.drawCircle(
                x,
                y - 10,
                12,
                paint
        );

        paint.setColor(Color.rgb(150, 220, 255));

        canvas.drawCircle(
                x,
                y - 12,
                6,
                paint
        );

        // Engine Thruster Flames
        paint.setColor(Color.rgb(255, 100, 0));

        canvas.drawRect(
                x - 12,
                y + height / 2,
                x - 4,
                y + height / 2 + 25,
                paint
        );

        canvas.drawRect(
                x + 4,
                y + height / 2,
                x + 12,
                y + height / 2 + 25,
                paint
        );

        paint.setColor(Color.rgb(255, 255, 0));

        canvas.drawRect(
                x - 8,
                y + height / 2,
                x - 6,
                y + height / 2 + 15,
                paint
        );

        canvas.drawRect(
                x + 6,
                y + height / 2,
                x + 8,
                y + height / 2 + 15,
                paint
        );
    }
}
