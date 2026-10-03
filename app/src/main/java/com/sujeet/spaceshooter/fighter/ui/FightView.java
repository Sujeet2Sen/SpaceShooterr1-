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
            float floorY = getHeight() > 0 ? getHeight() - 250 : 1200;
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

        float floorY = getHeight() - 250;
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
            // Sunset Dojo Background
            canvas.drawColor(Color.rgb(30, 20, 45));

            float floorY = getHeight() - 250;

            // Floor Grid
            paint.setColor(Color.rgb(80, 50, 100));
            canvas.drawRect(0, floorY + 20, getWidth(), getHeight(), paint);

            paint.setColor(Color.rgb(180, 120, 220));
            canvas.drawRect(0, floorY + 15, getWidth(), floorY + 20, paint);

            // Draw Energy Blasts
            for (EnergyBlast blast : blasts) {
                blast.draw(canvas, paint);
            }

            // Draw Fighters
            player.draw(canvas, paint);
            enemyAi.draw(canvas, paint);

            // Draw HUD (Health Bars & Round Timer)
            drawHUD(canvas);

            // Draw On-Screen Touch Controls
            drawControls(canvas);

            // Match Result Overlay
            if (fightOver) {
                drawResult(canvas);
            }
        }
    }

    private void drawHUD(Canvas canvas) {
        float barWidth = getWidth() * 0.35f;

        // Player 1 Health Bar (Top-Left)
        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(30, 40, 30 + barWidth, 75, 10, 10, paint);

        float p1Pct = (float) player.getHealth() / player.getMaxHealth();
        paint.setColor(Color.GREEN);
        canvas.drawRoundRect(30, 40, 30 + barWidth * p1Pct, 75, 10, 10, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("P1 HERO", 30, 32, paint);

        // Player 2 Health Bar (Top-Right)
        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(getWidth() - 30 - barWidth, 40, getWidth() - 30, 75, 10, 10, paint);

        float p2Pct = (float) enemyAi.getHealth() / enemyAi.getMaxHealth();
        paint.setColor(Color.RED);
        canvas.drawRoundRect(getWidth() - 30 - barWidth * p2Pct, 40, getWidth() - 30, 75, 10, 10, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("P2 ENEMY", getWidth() - 30, 32, paint);

        // Round Timer (Top-Center)
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.YELLOW);
        paint.setTextSize(42);
        canvas.drawText(String.valueOf(roundTimer), getWidth() / 2f, 65, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawControls(Canvas canvas) {
        float cy = getHeight() - 110;

        // Left / Right Movement Buttons
        paint.setColor(Color.argb(140, 50, 60, 90));
        canvas.drawCircle(100, cy, 50, paint);
        canvas.drawCircle(230, cy, 50, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(35);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("◄", 100, cy + 12, paint);
        canvas.drawText("►", 230, cy + 12, paint);

        // Action Buttons: P (Punch), K (Kick), SP (Special)
        float rx = getWidth() - 320;
        float rx2 = getWidth() - 190;
        float rx3 = getWidth() - 70;

        // PUNCH (P)
        paint.setColor(Color.argb(160, 220, 50, 50));
        canvas.drawCircle(rx, cy, 45, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("P", rx, cy + 12, paint);

        // KICK (K)
        paint.setColor(Color.argb(160, 220, 150, 0));
        canvas.drawCircle(rx2, cy, 45, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("K", rx2, cy + 12, paint);

        // SPECIAL (SP!)
        paint.setColor(Color.argb(180, 0, 200, 255));
        canvas.drawCircle(rx3, cy, 50, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("SP", rx3, cy + 12, paint);

        // Exit / Back to Mode Select Button
        paint.setColor(Color.argb(180, 80, 80, 100));
        canvas.drawRoundRect(20, 90, 140, 140, 10, 10, paint);
        paint.setTextSize(24);
        canvas.drawText("EXIT", 80, 122, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawResult(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        paint.setColor(Color.argb(200, 0, 0, 0));
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

                // Left Movement
                if (Math.hypot(x - 100, y - cy) <= 60) {
                    movingLeft = true;
                    return true;
                }

                // Right Movement
                if (Math.hypot(x - 230, y - cy) <= 60) {
                    movingRight = true;
                    return true;
                }

                // Punch (P)
                float rx = getWidth() - 320;
                if (Math.hypot(x - rx, y - cy) <= 55) {
                    player.punch();
                    return true;
                }

                // Kick (K)
                float rx2 = getWidth() - 190;
                if (Math.hypot(x - rx2, y - cy) <= 55) {
                    player.kick();
                    return true;
                }

                // Special Attack (SP)
                float rx3 = getWidth() - 70;
                if (Math.hypot(x - rx3, y - cy) <= 60) {
                    player.special();
                    blasts.add(new EnergyBlast(player.getX() + 50, player.getY() - 20, true));
                    SoundManager.playShoot();
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
