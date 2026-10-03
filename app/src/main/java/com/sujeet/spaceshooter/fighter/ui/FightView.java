package com.sujeet.spaceshooter.fighter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sujeet.spaceshooter.fighter.entities.EnergyBlast;
import com.sujeet.spaceshooter.fighter.entities.Fighter;
import com.sujeet.spaceshooter.fighter.entities.FighterState;
import com.sujeet.spaceshooter.utils.SoundManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FightView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private Fighter player;
    private Fighter enemyAi;
    private final List<EnergyBlast> blasts = new ArrayList<>();

    private int roundTimer = 60;
    private int frameCounter = 0;
    private int aiDecisionTimer = 0;
    private boolean fightOver = false;
    private String matchResult = "";

    private int specialMeter = 0; // 0 to 100 Finisher Meter

    private float touchStartX = 0;
    private boolean isSwiping = false;

    private boolean running = true;
    private Thread gameThread;
    private final Object lock = new Object();

    public Runnable onExitListener;

    public FightView(Context context) {
        super(context);
        setFocusable(true);
        initFighters();
        startGameLoop();
    }

    private void initFighters() {
        synchronized (lock) {
            float floorY = getHeight() > 0 ? getHeight() * 0.65f : 950f;
            float p1X = getWidth() > 0 ? getWidth() * 0.28f : 300;
            float p2X = getWidth() > 0 ? getWidth() * 0.72f : 800;

            player = new Fighter(p1X, floorY, true);
            enemyAi = new Fighter(p2X, floorY, false);

            blasts.clear();
            roundTimer = 60;
            frameCounter = 0;
            specialMeter = 0;
            fightOver = false;
            matchResult = "";
        }
    }

    @SuppressWarnings("BusyWait")
    private void startGameLoop() {
        gameThread = new Thread(() -> {
            while (running) {
                synchronized (lock) {
                    updateFight();
                }
                postInvalidate();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameThread.start();
    }

    private void updateFight() {
        if (fightOver) return;

        float floorY = getHeight() > 0 ? getHeight() * 0.65f : 950f;
        if (player.getY() != floorY && floorY > 0) {
            player.setPosition(getWidth() * 0.28f, floorY);
            enemyAi.setPosition(getWidth() * 0.72f, floorY);
        }

        // Round timer
        frameCounter++;
        if (frameCounter >= 60) {
            frameCounter = 0;
            roundTimer--;
            if (roundTimer <= 0) {
                roundTimer = 0;
                fightOver = true;
                if (player.getHealth() > enemyAi.getHealth()) {
                    matchResult = "TIME UP! YOU WIN!";
                } else if (enemyAi.getHealth() > player.getHealth()) {
                    matchResult = "TIME UP! YOU LOSE!";
                } else {
                    matchResult = "TIME UP! DRAW!";
                }
            }
        }

        // Update Fighters
        player.update(enemyAi.getX());
        enemyAi.update(player.getX());

        // AI Control for Fighter 2
        updateEnemyAi();

        // Update Energy Blasts
        Iterator<EnergyBlast> iterator = blasts.iterator();
        while (iterator.hasNext()) {
            EnergyBlast blast = iterator.next();
            blast.update();

            // Hit check
            if (blast.getBounds().intersects(player.getBounds().left, player.getBounds().top, player.getBounds().right, player.getBounds().bottom)) {
                iterator.remove();
                SoundManager.playHit();
                if (player.takeDamage(15)) {
                    fightOver = true;
                    matchResult = "K.O.! YOU LOSE!";
                }
                continue;
            }

            if (blast.getBounds().intersects(enemyAi.getBounds().left, enemyAi.getBounds().top, enemyAi.getBounds().right, enemyAi.getBounds().bottom)) {
                iterator.remove();
                SoundManager.playHit();
                specialMeter = Math.min(100, specialMeter + 25);
                if (enemyAi.takeDamage(15)) {
                    fightOver = true;
                    matchResult = "K.O.! YOU WIN!";
                }
                continue;
            }

            if (blast.isOutOfBounds(getWidth())) {
                iterator.remove();
            }
        }

        // Melee Hits check (Punch & Kick)
        checkMeleeHits();
    }

    private void checkMeleeHits() {
        // Player attacks Enemy AI
        if (player.getState() == FighterState.PUNCH || player.getState() == FighterState.KICK) {
            if (RectF.intersects(player.getHitBox(), enemyAi.getBounds())) {
                int dmg = player.getState() == FighterState.KICK ? 14 : 9;
                SoundManager.playHit();
                specialMeter = Math.min(100, specialMeter + 10);
                if (enemyAi.takeDamage(dmg)) {
                    fightOver = true;
                    matchResult = "K.O.! YOU WIN!";
                }
            }
        }

        // Enemy AI attacks Player
        if (enemyAi.getState() == FighterState.PUNCH || enemyAi.getState() == FighterState.KICK) {
            if (RectF.intersects(enemyAi.getHitBox(), player.getBounds())) {
                int dmg = enemyAi.getState() == FighterState.KICK ? 12 : 8;
                SoundManager.playHit();
                if (player.takeDamage(dmg)) {
                    fightOver = true;
                    matchResult = "K.O.! YOU LOSE!";
                }
            }
        }
    }

    private void updateEnemyAi() {
        if (fightOver) return;

        aiDecisionTimer++;
        if (aiDecisionTimer >= 18) {
            aiDecisionTimer = 0;
            float dist = Math.abs(enemyAi.getX() - player.getX());

            if (dist > 200) {
                enemyAi.move(-12, getWidth());
            } else if (dist < 130) {
                int action = random.nextInt(100);
                if (action < 40) {
                    enemyAi.punch();
                } else if (action < 70) {
                    enemyAi.kick();
                } else if (action < 88) {
                    enemyAi.block();
                } else {
                    enemyAi.special();
                    blasts.add(new EnergyBlast(enemyAi.getX() - 50, enemyAi.getY() - 20, false));
                    SoundManager.playShoot();
                }
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            float floorY = getHeight() > 0 ? getHeight() * 0.65f : 950f;

            // Full-Screen Sunset Arena Background
            canvas.drawColor(Color.rgb(20, 12, 35));

            paint.setColor(Color.rgb(180, 50, 80));
            canvas.drawRect(0, floorY - 380, getWidth(), floorY + 20, paint);

            paint.setColor(Color.rgb(230, 100, 50));
            canvas.drawRect(0, floorY - 220, getWidth(), floorY + 20, paint);

            // Torch Pillars
            drawPillars(canvas, floorY);

            // Arena Floor Mat with Neon Boundary
            paint.setColor(Color.rgb(35, 20, 55));
            canvas.drawRect(0, floorY + 20, getWidth(), getHeight(), paint);

            paint.setColor(Color.rgb(0, 220, 255));
            canvas.drawRect(0, floorY + 12, getWidth(), floorY + 20, paint);

            // Energy Blasts
            for (EnergyBlast blast : blasts) {
                blast.draw(canvas, paint);
            }

            // Fighters
            player.draw(canvas, paint);
            enemyAi.draw(canvas, paint);

            // HUD
            drawHUD(canvas);

            // Immortals Style Special Skill Bar & Controls Guide
            drawImmortalsControls(canvas);

            // Result Overlay
            if (fightOver) {
                drawResult(canvas);
            }
        }
    }

    private void drawPillars(Canvas canvas, float floorY) {
        paint.setColor(Color.rgb(80, 80, 95));
        canvas.drawRect(50, floorY - 320, 90, floorY + 20, paint);
        canvas.drawRect(getWidth() - 90, floorY - 320, getWidth() - 50, floorY + 20, paint);

        paint.setColor(Color.rgb(255, 140, 0));
        canvas.drawCircle(70, floorY - 335, 18, paint);
        canvas.drawCircle(getWidth() - 70, floorY - 335, 18, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(70, floorY - 335, 10, paint);
        canvas.drawCircle(getWidth() - 70, floorY - 335, 10, paint);
    }

    private void drawHUD(Canvas canvas) {
        float barWidth = getWidth() * 0.35f;

        // Player 1 Health Bar (Top-Left)
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(26, 36, 34 + barWidth, 79, 12, 12, paint);

        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(30, 40, 30 + barWidth, 75, 10, 10, paint);

        float p1Pct = (float) player.getHealth() / player.getMaxHealth();
        paint.setColor(Color.rgb(0, 230, 120));
        canvas.drawRoundRect(30, 40, 30 + barWidth * p1Pct, 75, 10, 10, paint);

        paint.setColor(Color.CYAN);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("P1 HERO", 30, 30, paint);

        // Player 2 Health Bar (Top-Right)
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(getWidth() - 34 - barWidth, 36, getWidth() - 26, 79, 12, 12, paint);

        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(getWidth() - 30 - barWidth, 40, getWidth() - 30, 75, 10, 10, paint);

        float p2Pct = (float) enemyAi.getHealth() / enemyAi.getMaxHealth();
        paint.setColor(Color.rgb(240, 40, 40));
        canvas.drawRoundRect(getWidth() - 30 - barWidth * p2Pct, 40, getWidth() - 30, 75, 10, 10, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.rgb(255, 100, 100));
        canvas.drawText("P2 ENEMY", getWidth() - 30, 30, paint);

        // VS Emblem & Timer
        float cx = getWidth() / 2f;
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(30);
        canvas.drawText("VS", cx, 35, paint);

        paint.setColor(Color.YELLOW);
        paint.setTextSize(44);
        canvas.drawText(String.valueOf(roundTimer), cx, 75, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawImmortalsControls(Canvas canvas) {
        float bottomY = getHeight() - 70;

        // Special Power Finisher Gauge (Bottom-Left)
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(26, bottomY - 35, 244, bottomY + 25, 15, 15, paint);

        paint.setColor(specialMeter >= 100 ? Color.rgb(0, 255, 200) : Color.rgb(0, 150, 220));
        canvas.drawRoundRect(30, bottomY - 30, 30 + (210 * (specialMeter / 100f)), bottomY + 20, 12, 12, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        String specLabel = specialMeter >= 100 ? "FINISHER READY! TAP" : "SPECIAL POWER (" + specialMeter + "%)";
        canvas.drawText(specLabel, 135, bottomY + 2, paint);

        // Exit Button (Top-Left)
        paint.setColor(Color.argb(180, 50, 60, 80));
        canvas.drawRoundRect(20, 90, 140, 138, 12, 12, paint);

        paint.setColor(Color.CYAN);
        paint.setTextSize(22);
        canvas.drawText("EXIT", 80, 120, paint);

        // On-Screen Gesture Guide Hints
        paint.setColor(Color.argb(150, 200, 220, 255));
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("TAP: Strike | SWIPE ➔: Heavy Kick | HOLD LEFT: Block", getWidth() - 20, getHeight() - 25, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawResult(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        paint.setColor(Color.argb(220, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(matchResult.contains("WIN") ? Color.YELLOW : Color.RED);
        paint.setTextSize(65);
        canvas.drawText(matchResult, cx, cy - 40, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        canvas.drawText("Tap Screen to Play Again", cx, cy + 40, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                performClick();

                if (fightOver) {
                    initFighters();
                    return true;
                }

                // EXIT Button
                if (x >= 20 && x <= 140 && y >= 90 && y <= 140) {
                    if (onExitListener != null) {
                        onExitListener.run();
                    }
                    return true;
                }

                // SPECIAL FINISHER GAUGE TAP (Bottom-Left)
                float bottomY = getHeight() - 70;
                if (x >= 26 && x <= 244 && y >= bottomY - 35 && y <= bottomY + 25) {
                    if (specialMeter >= 100) {
                        specialMeter = 0;
                        player.special();
                        blasts.add(new EnergyBlast(player.getX() + 50, player.getY() - 20, true));
                        SoundManager.playShoot();
                        return true;
                    }
                }

                touchStartX = x;
                isSwiping = false;

                // Left Side Hold = Block
                if (x < getWidth() * 0.3f) {
                    player.block();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = x - touchStartX;

                if (Math.abs(dx) > 50 && !isSwiping) {
                    isSwiping = true;
                    if (dx > 0) {
                        // Swipe Right = Flying Heavy Dash Kick Attack!
                        player.move(45, getWidth());
                        player.kick();
                        SoundManager.playHit();
                    } else {
                        // Swipe Left = Dash Back Dodge!
                        player.move(-55, getWidth());
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (!isSwiping) {
                    // Tap = Light Combo Strike!
                    if (x > getWidth() * 0.3f) {
                        player.punch();
                        specialMeter = Math.min(100, specialMeter + 8);
                    }
                }
                return true;
        }

        return true;
    }

    public boolean handleBackPressed() {
        if (onExitListener != null) {
            onExitListener.run();
            return true;
        }
        return false;
    }

    public void stopGame() {
        running = false;
    }
}
