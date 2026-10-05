package com.sujeet.spaceshooter.wrestling;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class Fighter {

    protected float x;
    protected float y;
    protected String name;
    protected int health;
    protected int maxHealth;
    protected float width = 100f;
    protected float height = 200f;
    protected float speed = 8f;
    protected boolean facingRight = true;

    protected int primaryColor = Color.rgb(0, 180, 240);
    protected int secondaryColor = Color.rgb(255, 215, 0);

    protected final RectF bounds = new RectF();

    public Fighter(String name, float x, float y, boolean facingRight, int primaryColor, int secondaryColor) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.maxHealth = 100;
        this.health = maxHealth;
    }

    public void update() {
        // Base update loop
    }

    public void moveLeft() {
        x -= speed;
        facingRight = false;
    }

    public void moveRight() {
        x += speed;
        facingRight = true;
    }

    public RectF getBounds() {
        bounds.set(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
        return bounds;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Ground Shadow
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawOval(x - 50, y + height / 2 - 10, x + 50, y + height / 2 + 10, paint);

        // Vector Wrestler Body Rendering (Extensible for sprite sheets)
        // Head
        paint.setColor(Color.rgb(245, 195, 155));
        canvas.drawCircle(x, y - height / 2 + 25, 24, paint);

        // Mask / Headgear Accent
        paint.setColor(secondaryColor);
        canvas.drawRect(x - 24, y - height / 2 + 15, x + 24, y - height / 2 + 25, paint);

        // Torso / Vest
        paint.setColor(primaryColor);
        canvas.drawRect(x - 30, y - height / 2 + 48, x + 30, y + 20, paint);

        // Belt
        paint.setColor(secondaryColor);
        canvas.drawRect(x - 32, y + 10, x + 32, y + 25, paint);

        // Arms
        paint.setColor(Color.rgb(245, 195, 155));
        float armOffset = facingRight ? 28 : -28;
        canvas.drawCircle(x - armOffset, y, 14, paint);
        canvas.drawCircle(x + armOffset, y, 14, paint);

        // Legs
        paint.setColor(primaryColor);
        canvas.drawRect(x - 26, y + 25, x - 6, y + height / 2 - 15, paint);
        canvas.drawRect(x + 6, y + 25, x + 26, y + height / 2 - 15, paint);

        // Boots
        paint.setColor(secondaryColor);
        canvas.drawRect(x - 28, y + height / 2 - 15, x - 4, y + height / 2, paint);
        canvas.drawRect(x + 4, y + height / 2 - 15, x + 28, y + height / 2, paint);
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isFacingRight() { return facingRight; }
}
