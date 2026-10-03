package com.sujeet.spaceshooter.fighter.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Fighter {

    private float x;
    private float y;
    private String name = "HERO";
    private int health = 100;
    private int maxHealth = 100;
    private int baseDamage = 10;
    private int color = Color.rgb(0, 180, 255);
    private final boolean isPlayer;

    private FighterState state = FighterState.IDLE;
    private int actionTimer = 0;
    private final float width = 90;
    private final float height = 180;
    private final RectF bounds = new RectF();

    public Fighter(float x, float y, boolean isPlayer) {
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
    }

    public void applySuperstar(WweSuperstar superstar) {
        if (superstar != null) {
            this.name = superstar.getName();
            this.maxHealth = superstar.getMaxHealth();
            this.health = this.maxHealth;
            this.baseDamage = superstar.getBaseDamage();
            this.color = superstar.getPrimaryColor();
        }
    }

    public void update(float opponentX) {
        if (state == FighterState.KNOCKOUT) return;

        if (actionTimer > 0) {
            actionTimer--;
            if (actionTimer == 0 && state != FighterState.KNOCKOUT) {
                state = FighterState.IDLE;
            }
        }

        // Keep facing & spacing opponent
        if (isPlayer && x > opponentX - 60) x = opponentX - 60;
        if (!isPlayer && x < opponentX + 60) x = opponentX + 60;
    }

    public void move(float dx, int screenWidth) {
        if (state == FighterState.KNOCKOUT || state == FighterState.PUNCH || state == FighterState.KICK) return;
        x += dx;
        if (x < 60) x = 60;
        if (x > screenWidth - 60) x = screenWidth - 60;
        if (dx > 0) state = FighterState.WALK_FORWARD;
        else if (dx < 0) state = FighterState.WALK_BACKWARD;
    }

    public void punch() {
        if (actionTimer == 0 && state != FighterState.KNOCKOUT) {
            state = FighterState.PUNCH;
            actionTimer = 12;
        }
    }

    public void kick() {
        if (actionTimer == 0 && state != FighterState.KNOCKOUT) {
            state = FighterState.KICK;
            actionTimer = 16;
        }
    }

    public void block() {
        if (actionTimer == 0 && state != FighterState.KNOCKOUT) {
            state = FighterState.BLOCK;
            actionTimer = 20;
        }
    }

    public void special() {
        if (actionTimer == 0 && state != FighterState.KNOCKOUT) {
            state = FighterState.SPECIAL;
            actionTimer = 18;
        }
    }

    public boolean takeDamage(int damage) {
        if (state == FighterState.BLOCK) {
            damage /= 4;
        }
        health -= damage;
        if (health <= 0) {
            health = 0;
            state = FighterState.KNOCKOUT;
            return true;
        } else if (state != FighterState.BLOCK) {
            state = FighterState.HIT;
            actionTimer = 10;
        }
        return false;
    }

    public int getAttackDamage(boolean isKick) {
        return isKick ? (baseDamage + 4) : baseDamage;
    }

    public RectF getHitBox() {
        float reach = 0;
        if (state == FighterState.PUNCH) reach = 55;
        if (state == FighterState.KICK) reach = 70;

        float attackX = isPlayer ? (x + width / 2 + reach) : (x - width / 2 - reach);
        bounds.set(attackX - 35, y - height / 2, attackX + 35, y + height / 2);
        return bounds;
    }

    public RectF getBounds() {
        bounds.set(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
        return bounds;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Shadow
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawOval(x - 45, y + height / 2 - 10, x + 45, y + height / 2 + 10, paint);

        int bodyColor = color;
        if (state == FighterState.HIT) bodyColor = Color.WHITE;

        // Head
        paint.setColor(Color.rgb(255, 205, 170));
        canvas.drawCircle(x, y - height / 2 + 20, 22, paint);

        // Headband / Visor
        paint.setColor(isPlayer ? Color.RED : Color.YELLOW);
        canvas.drawRect(x - 22, y - height / 2 + 12, x + 22, y - height / 2 + 20, paint);

        // Torso / Gi
        paint.setColor(bodyColor);
        canvas.drawRect(x - 25, y - height / 2 + 42, x + 25, y + 20, paint);

        // Name on Chest
        paint.setColor(Color.WHITE);
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(name.split(" ")[0], x, y - 5, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        // Arms & Punching Pose
        paint.setColor(Color.rgb(255, 205, 170));
        if (state == FighterState.PUNCH) {
            float fistX = isPlayer ? (x + 65) : (x - 65);
            paint.setColor(bodyColor);
            canvas.drawRect(Math.min(x, fistX), y - 10, Math.max(x, fistX), y + 10, paint);
            paint.setColor(isPlayer ? Color.RED : Color.BLACK);
            canvas.drawCircle(fistX, y, 16, paint);
        } else if (state == FighterState.BLOCK) {
            paint.setColor(Color.YELLOW);
            canvas.drawRect(x - 20, y - 20, x + 20, y + 20, paint);
        } else {
            canvas.drawCircle(x - 22, y, 12, paint);
            canvas.drawCircle(x + 22, y, 12, paint);
        }

        // Legs & Kicking Pose
        paint.setColor(bodyColor);
        if (state == FighterState.KICK) {
            float footX = isPlayer ? (x + 75) : (x - 75);
            canvas.drawRect(Math.min(x, footX), y + 20, Math.max(x, footX), y + 45, paint);
        } else {
            canvas.drawRect(x - 22, y + 20, x - 5, y + height / 2, paint);
            canvas.drawRect(x + 5, y + 20, x + 22, y + height / 2, paint);
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

    public int getMaxHealth() {
        return maxHealth;
    }

    public String getName() {
        return name;
    }

    public FighterState getState() {
        return state;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.health = maxHealth;
        this.state = FighterState.IDLE;
    }
}
