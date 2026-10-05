package com.sujeet.spaceshooter.wrestling;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class Fighter {

    protected float x;
    protected float y;
    protected String name;
    protected int health = 100;
    protected int maxHealth = 100;
    protected int power = 0; // 0 to 100 Power Meter
    protected final int maxPower = 100;

    protected float width = 100f;
    protected float height = 200f;
    protected float speed = 8f;
    protected boolean facingRight = true;

    protected FighterState state = FighterState.IDLE;
    protected int actionTimer = 0;

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
        this.power = 0;
    }

    public void update() {
        if (state == FighterState.KO) return;

        if (actionTimer > 0) {
            actionTimer--;
            if (actionTimer == 0) {
                state = FighterState.IDLE;
            }
        }
    }

    public void moveLeft() {
        if (state == FighterState.KO || state == FighterState.PUNCH || state == FighterState.KICK || state == FighterState.SPECIAL) return;
        x -= speed;
        facingRight = false;
        if (actionTimer == 0) state = FighterState.WALK;
    }

    public void moveRight() {
        if (state == FighterState.KO || state == FighterState.PUNCH || state == FighterState.KICK || state == FighterState.SPECIAL) return;
        x += speed;
        facingRight = true;
        if (actionTimer == 0) state = FighterState.WALK;
    }

    public void stopMove() {
        if (state == FighterState.WALK) {
            state = FighterState.IDLE;
        }
    }

    public boolean punch() {
        if (actionTimer == 0 && state != FighterState.KO) {
            state = FighterState.PUNCH;
            actionTimer = 12;
            return true;
        }
        return false;
    }

    public boolean kick() {
        if (actionTimer == 0 && state != FighterState.KO) {
            state = FighterState.KICK;
            actionTimer = 16;
            return true;
        }
        return false;
    }

    public boolean block() {
        if (actionTimer == 0 && state != FighterState.KO) {
            state = FighterState.BLOCK;
            actionTimer = 18;
            return true;
        }
        return false;
    }

    public boolean special() {
        if (actionTimer == 0 && state != FighterState.KO && power >= maxPower) {
            state = FighterState.SPECIAL;
            actionTimer = 22;
            power = 0; // Consume power meter
            return true;
        }
        return false;
    }

    public boolean takeDamage(int damage) {
        if (state == FighterState.BLOCK) {
            damage = Math.max(1, damage / 4); // Block reduces 75% damage
        }
        health -= damage;
        if (health <= 0) {
            health = 0;
            state = FighterState.KO;
            return true; // KO!
        } else if (state != FighterState.BLOCK) {
            state = FighterState.HIT;
            actionTimer = 10;
        }
        return false;
    }

    public void addPower(int amount) {
        power = Math.min(maxPower, power + amount);
    }

    public float getAttackRange() {
        if (state == FighterState.PUNCH) return 110f;
        if (state == FighterState.KICK) return 130f;
        if (state == FighterState.SPECIAL) return 220f;
        return 0f;
    }

    public RectF getBounds() {
        bounds.set(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
        return bounds;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Ground Shadow
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawOval(x - 50, y + height / 2 - 10, x + 50, y + height / 2 + 10, paint);

        if (state == FighterState.KO) {
            drawKnockedOut(canvas, paint);
            return;
        }

        int bodyColor = primaryColor;
        if (state == FighterState.HIT) bodyColor = Color.WHITE;

        // Head
        paint.setColor(Color.rgb(245, 195, 155));
        canvas.drawCircle(x, y - height / 2 + 25, 24, paint);

        // Mask / Headgear Accent
        paint.setColor(secondaryColor);
        canvas.drawRect(x - 24, y - height / 2 + 15, x + 24, y - height / 2 + 25, paint);

        // Torso / Vest
        paint.setColor(bodyColor);
        canvas.drawRect(x - 30, y - height / 2 + 48, x + 30, y + 20, paint);

        // Belt
        paint.setColor(secondaryColor);
        canvas.drawRect(x - 32, y + 10, x + 32, y + 25, paint);

        // Arms & Poses
        paint.setColor(Color.rgb(245, 195, 155));
        if (state == FighterState.PUNCH) {
            float fistX = facingRight ? (x + 70) : (x - 70);
            paint.setColor(bodyColor);
            canvas.drawRect(Math.min(x, fistX), y - 10, Math.max(x, fistX), y + 10, paint);
            paint.setColor(secondaryColor);
            canvas.drawCircle(fistX, y, 16, paint);
        } else if (state == FighterState.BLOCK) {
            paint.setColor(secondaryColor);
            canvas.drawRect(x - 20, y - 20, x + 20, y + 20, paint);
        } else if (state == FighterState.SPECIAL) {
            float auraX = facingRight ? (x + 80) : (x - 80);
            paint.setColor(Color.argb(180, 0, 255, 255));
            canvas.drawCircle(auraX, y, 35, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(auraX, y, 20, paint);
        } else {
            float armOffset = facingRight ? 28 : -28;
            canvas.drawCircle(x - armOffset, y, 14, paint);
            canvas.drawCircle(x + armOffset, y, 14, paint);
        }

        // Legs & Kicking Pose
        paint.setColor(bodyColor);
        if (state == FighterState.KICK) {
            float footX = facingRight ? (x + 80) : (x - 80);
            canvas.drawRect(Math.min(x, footX), y + 20, Math.max(x, footX), y + 45, paint);
        } else {
            canvas.drawRect(x - 26, y + 25, x - 6, y + height / 2 - 15, paint);
            canvas.drawRect(x + 6, y + 25, x + 26, y + height / 2 - 15, paint);
        }

        // Boots
        paint.setColor(secondaryColor);
        canvas.drawRect(x - 28, y + height / 2 - 15, x - 4, y + height / 2, paint);
        canvas.drawRect(x + 4, y + height / 2 - 15, x + 28, y + height / 2, paint);
    }

    private void drawKnockedOut(Canvas canvas, Paint paint) {
        paint.setColor(primaryColor);
        canvas.drawRect(x - height / 2, y + height / 2 - 40, x + height / 2, y + height / 2, paint);
        paint.setColor(Color.rgb(245, 195, 155));
        canvas.drawCircle(facingRight ? (x - height / 2) : (x + height / 2), y + height / 2 - 20, 20, paint);
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
    public int getPower() { return power; }
    public int getMaxPower() { return maxPower; }
    public FighterState getState() { return state; }
    public boolean isFacingRight() { return facingRight; }

    public void reset(float x, float y) {
        this.x = x;
        this.y = y;
        this.health = maxHealth;
        this.power = 0;
        this.state = FighterState.IDLE;
        this.actionTimer = 0;
    }
}
