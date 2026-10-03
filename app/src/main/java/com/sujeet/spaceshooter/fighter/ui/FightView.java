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

    private boolean movingLeft = false;
    private boolean movingRight = false;

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
            float floorY = getHeight() > 0 ? getHeight() * 0.58f : 900f;
            float p1X = getWidth() > 0 ? getWidth() * 0.25f : 300;
            float p2X = getWidth() > 0 ? getWidth() * 0.75f : 800;

            player = new Fighter(p1X, floorY, true);
            enemyAi = new Fighter(p2X, floorY, false);

            blasts.clear();
            roundTimer = 60;
            frameCounter = 0;
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

        float floorY = getHeight() > 0 ? getHeight() * 0.58f : 900f;
        if (player.getY() != floorY && floorY > 0) {
            player.setPosition(getWidth() * 0.25f, floorY);
            enemyAi.setPosition(getWidth() * 0.75f, floorY);
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

        // Player Movement
        if (movingLeft) player.move(-8, getWidth());
        if (movingRight) player.move(8, getWidth());

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
                int dmg = player.getState() == FighterState.KICK ? 12 : 8;
                SoundManager.playHit();
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
        if (aiDecisionTimer >= 20) {
            aiDecisionTimer = 0;
            float dist = Math.abs(enemyAi.getX() - player.getX());

            if (dist > 220) {
                enemyAi.move(-8, getWidth());
            } else if (dist < 130) {
                int action = random.nextInt(100);
                if (action < 40) {
                    enemyAi.punch();
                } else if (action < 75) {
                    enemyAi.kick();
                } else if (action < 90) {
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
            float floorY = getHeight() > 0 ? getHeight() * 0.58f : 900f;

            // Sunset Arena Background
            canvas.drawColor(Color.rgb(25, 15, 40));

            // Background Sunset Glow Gradient Effect
            paint.setColor(Color.rgb(180, 50, 80));
            canvas.drawRect(0, floorY - 350, getWidth(), floorY + 20, paint);

            paint.setColor(Color.rgb(230, 100, 50));
            canvas.drawRect(0, floorY - 200, getWidth(), floorY + 20, paint);

            // Arena Pillars / Torches
            drawPillars(canvas, floorY);

            // Arena Floor Mat with Neon Boundary
            paint.setColor(Color.rgb(40, 25, 60));
            canvas.drawRect(0, floorY + 20, getWidth(), getHeight(), paint);

            // Neon Ring Line
            paint.setColor(Color.rgb(0, 220, 255));
            canvas.drawRect(0, floorY + 12, getWidth(), floorY + 20, paint);

            // Draw Energy Blasts
            for (EnergyBlast blast : blasts) {
                blast.draw(canvas, paint);
            }

            // Draw Fighters
            player.draw(canvas, paint);
            enemyAi.draw(canvas, paint);

            // Draw HUD (Health Bars & Round Timer)
            drawHUD(canvas);

            // Draw Glossy Arcade Action Buttons
            drawControls(canvas);

            // Match Result Overlay
            if (fightOver) {
                drawResult(canvas);
            }
        }
    }

    private void drawPillars(Canvas canvas, float floorY) {
        // Left Torch Pillar
        paint.setColor(Color.rgb(80, 80, 95));
        canvas.drawRect(50, floorY - 300, 90, floorY + 20, paint);
        paint.setColor(Color.rgb(255, 140, 0));
        canvas.drawCircle(70, floorY - 315, 18, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(70, floorY - 315, 10, paint);

        // Right Torch Pillar
        paint.setColor(Color.rgb(80, 80, 95));
        canvas.drawRect(getWidth() - 90, floorY - 300, getWidth() - 50, floorY + 20, paint);
        paint.setColor(Color.rgb(255, 140, 0));
        canvas.drawCircle(getWidth() - 70, floorY - 315, 18, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(getWidth() - 70, floorY - 315, 10, paint);
    }

    private void drawHUD(Canvas canvas) {
        float barWidth = getWidth() * 0.35f;

        // Player 1 Health Bar (Top-Left)
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(26, 36, 34 + barWidth, 79, 12, 12, paint);

        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(30, 40, 30 + barWidth, 75, 10, 10, paint);

        float p1Pct = (float) player.getHealth() / player.getMaxHealth();
        paint.setColor(Color.rgb(0, 230, 120)); // Cyan-Green
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
        paint.setColor(Color.rgb(240, 40, 40)); // Red
        canvas.drawRoundRect(getWidth() - 30 - barWidth * p2Pct, 40, getWidth() - 30, 75, 10, 10, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.rgb(255, 100, 100));
        canvas.drawText("P2 ENEMY", getWidth() - 30, 30, paint);

        // VS Emblem & Round Timer (Top-Center)
        float cx = getWidth() / 2f;
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setColor(Color.rgb(255, 215, 0)); // Gold VS
        paint.setTextSize(30);
        canvas.drawText("VS", cx, 35, paint);

        paint.setColor(Color.YELLOW);
        paint.setTextSize(44);
        canvas.drawText(String.valueOf(roundTimer), cx, 75, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawControls(Canvas canvas) {
        float cy = getHeight() - 110;

        // Left / Right Movement D-Pad Buttons
        drawArcadeButton(canvas, 100, cy, 52, Color.rgb(0, 180, 220), "◄");
        drawArcadeButton(canvas, 230, cy, 52, Color.rgb(0, 180, 220), "►");

        // Action Buttons: P (Punch), K (Kick), SP (Special)
        float rx = getWidth() - 320;
        float rx2 = getWidth() - 190;
        float rx3 = getWidth() - 70;

        drawArcadeButton(canvas, rx, cy, 48, Color.rgb(230, 40, 40), "P");
        drawArcadeButton(canvas, rx2, cy, 48, Color.rgb(240, 150, 0), "K");
        drawArcadeButton(canvas, rx3, cy, 54, Color.rgb(0, 220, 255), "SP");

        // Exit / Back to Mode Select Button
        paint.setColor(Color.argb(180, 50, 60, 80));
        canvas.drawRoundRect(20, 90, 140, 138, 12, 12, paint);

        paint.setColor(Color.CYAN);
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("EXIT", 80, 120, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawArcadeButton(Canvas canvas, float x, float y, float r, int mainColor, String label) {
        // Outer Glow Ring
        paint.setColor(Color.argb(100, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor)));
        canvas.drawCircle(x, y, r + 6, paint);

        // Bevel Rim
        paint.setColor(Color.rgb(30, 35, 50));
        canvas.drawCircle(x, y, r, paint);

        // Inner Core
        paint.setColor(mainColor);
        canvas.drawCircle(x, y, r - 6, paint);

        // Label Text
        paint.setColor(Color.WHITE);
        paint.setTextSize(r * 0.7f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, x, y + (r * 0.25f), paint);
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
        float cy = getHeight() - 110;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
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

                // Left Movement Button
                if (Math.hypot(x - 100, y - cy) <= 60) {
                    movingLeft = true;
                    return true;
                }

                // Right Movement Button
                if (Math.hypot(x - 230, y - cy) <= 60) {
                    movingRight = true;
                    return true;
                }

                // Punch (P) Button
                float rx = getWidth() - 320;
                if (Math.hypot(x - rx, y - cy) <= 55) {
                    player.punch();
                    return true;
                }

                // Kick (K) Button
                float rx2 = getWidth() - 190;
                if (Math.hypot(x - rx2, y - cy) <= 55) {
                    player.kick();
                    return true;
                }

                // Special Attack (SP) Button
                float rx3 = getWidth() - 70;
                if (Math.hypot(x - rx3, y - cy) <= 60) {
                    player.special();
                    blasts.add(new EnergyBlast(player.getX() + 50, player.getY() - 20, true));
                    SoundManager.playShoot();
                    return true;
                }

                // Tap anywhere on Right Screen Area to Attack!
                if (x >= getWidth() * 0.45f && y < getHeight() - 180f) {
                    if (y < getHeight() * 0.35f) {
                        player.special();
                        blasts.add(new EnergyBlast(player.getX() + 50, player.getY() - 20, true));
                        SoundManager.playShoot();
                    } else if (y < getHeight() * 0.48f) {
                        player.punch();
                    } else {
                        player.kick();
                    }
                    return true;
                }

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                movingLeft = false;
                movingRight = false;
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
