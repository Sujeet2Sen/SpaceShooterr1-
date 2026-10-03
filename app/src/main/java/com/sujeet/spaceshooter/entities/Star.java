package com.sujeet.spaceshooter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Star {

    private float x;
    private float y;

    private float speed;

    private final float size;

    private final Random random =
            new Random();

    public Star() {

        x = random.nextInt(1000);

        y = random.nextInt(2000);

        speed =
                2 + random.nextFloat() * 5;

        size =
                1.5f + random.nextFloat() * 4.5f;
    }

    public void update(
            int height,
            int width) {

        y += speed;

        if (y > height) {

            y = 0;

            x = random.nextInt(
                    Math.max(1, width)
            );
        }
    }

    public void draw(
            Canvas canvas,
            Paint paint) {

        if (size > 3.5f) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.rgb(150, 200, 255));
        }

        canvas.drawCircle(
                x,
                y,
                size,
                paint
        );
    }
}
