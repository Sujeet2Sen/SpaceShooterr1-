package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class PowerUp {

    private float x;
    private float y;

    private final PowerUpType type;

    private final float speed = 4;

    public PowerUp(
            float x,
            float y,
            PowerUpType type) {

        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {

        y += speed;
    }

    public float getY() {
        return y;
    }

    public PowerUpType getType() {
        return type;
    }

    public RectF getBounds() {

        return new RectF(
                x - 25,
                y - 25,
                x + 25,
                y + 25
        );
    }

    public void draw(
            Canvas canvas,
            Paint paint) {

        switch (type) {

            case RAPID_FIRE:
                paint.setColor(Color.RED);
                break;

            case DOUBLE_BULLET:
                paint.setColor(Color.YELLOW);
                break;

            case SHIELD:
                paint.setColor(Color.CYAN);
                break;

            case EXTRA_LIFE:
                paint.setColor(Color.GREEN);
                break;

            case DUAL_SHIP:
                paint.setColor(Color.rgb(0, 255, 200));
                break;
        }

        canvas.drawCircle(
                x,
                y,
                25,
                paint
        );

        paint.setColor(Color.BLACK);

        paint.setTextSize(22);

        String text = "";

        switch (type) {

            case RAPID_FIRE:
                text = "R";
                break;

            case DOUBLE_BULLET:
                text = "D";
                break;

            case SHIELD:
                text = "S";
                break;

            case EXTRA_LIFE:
                text = "+";
                break;

            case DUAL_SHIP:
                text = "2x";
                break;
        }

        canvas.drawText(
                text,
                x - 10,
                y + 8,
                paint
        );
    }
}
