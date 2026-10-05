package com.sujeet.spaceshooter.wrestling;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class GameView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PlayerFighter player;
    private EnemyFighter enemy;

    private GameState gameState = GameState.PLAYING;

    private boolean movingLeft = false;
    private boolean movingRight = false;

    private boolean running = true;
    private Thread gameThread;
    private final Object lock = new Object();

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        initGame();
        startGameLoop();
    }

    private void initGame() {
        synchronized (lock) {
            float floorY = getHeight() > 0 ? getHeight() * 0.60f : 900f;
            float p1X = getWidth() > 0 ? getWidth() * 0.30f : 300f;
            float p2X = getWidth() > 0 ? getWidth() * 0.70f : 800f;

            player = new PlayerFighter(p1X, floorY);
            enemy = new EnemyFighter(p2X, floorY);

            gameState = GameState.PLAYING;
        }
    }

    @SuppressWarnings("BusyWait")
    private void startGameLoop() {
        gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            double nsPerTick = 1000000000.0 / 60.0; // Target 60 FPS

            while (running) {
                long now = System.nanoTime();
                double delta = (now - lastTime) / nsPerTick;
                if (delta >= 1) {
                    lastTime = now;
                    synchronized (lock) {
                        if (gameState == GameState.PLAYING) {
                            updateGame();
                        }
                    }
                    postInvalidate();
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameThread.start();
    }

    private void updateGame() {
        float floorY = getHeight() > 0 ? getHeight() * 0.60f : 900f;
        if (player.getY() != floorY && floorY > 0) {
            player.setY(floorY);
            enemy.setY(floorY);
        }

        // Handle Player Movement
        if (movingLeft) {
            player.moveLeft();
        } else if (movingRight) {
            player.moveRight();
        }

        player.update();
        enemy.update();

        // Keep inside ring & prevent overlap
        CollisionManager.keepInsideRing(player, getWidth());
        CollisionManager.keepInsideRing(enemy, getWidth());
        CollisionManager.preventOverlap(player, enemy);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            float floorY = getHeight() > 0 ? getHeight() * 0.60f : 900f;

            // Arena Background
            canvas.drawColor(Color.rgb(18, 12, 32));

            // Stadium Spotlights
            drawSpotlights(canvas);

            // Ring Mat Floor
            paint.setColor(Color.rgb(220, 220, 235));
            canvas.drawRect(0, floorY + 20, getWidth(), getHeight(), paint);

            // Red Ring Apron Mat
            paint.setColor(Color.rgb(190, 20, 30));
            canvas.drawRect(0, floorY + 20, getWidth(), floorY + 40, paint);

            // Center Ring Text / Logo
            paint.setColor(Color.argb(40, 190, 20, 30));
            paint.setTextSize(46);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("O R I G I N A L   R I N G", getWidth() / 2f, floorY + 120, paint);

            // Ring Posts & Ropes
            drawRingPostsAndRopes(canvas, floorY);

            // Draw Fighters
            player.draw(canvas, paint);
            enemy.draw(canvas, paint);

            // Draw On-Screen Movement Controls
            drawControls(canvas);
        }
    }

    private void drawSpotlights(Canvas canvas) {
        paint.setColor(Color.argb(25, 255, 255, 220));
        Path light1 = new Path();
        light1.moveTo(getWidth() * 0.2f, 0);
        light1.lineTo(0, getHeight() * 0.60f);
        light1.lineTo(getWidth() * 0.5f, getHeight() * 0.60f);
        light1.close();
        canvas.drawPath(light1, paint);

        Path light2 = new Path();
        light2.moveTo(getWidth() * 0.8f, 0);
        light2.lineTo(getWidth() * 0.5f, getHeight() * 0.60f);
        light2.lineTo(getWidth(), getHeight() * 0.60f);
        light2.close();
        canvas.drawPath(light2, paint);
    }

    private void drawRingPostsAndRopes(Canvas canvas, float floorY) {
        // Steel Posts
        paint.setColor(Color.rgb(160, 20, 20));
        canvas.drawRect(20, floorY - 260, 48, floorY + 20, paint);
        canvas.drawRect(getWidth() - 48, floorY - 260, getWidth() - 20, floorY + 20, paint);

        // Turnbuckles
        paint.setColor(Color.rgb(220, 30, 30));
        canvas.drawRoundRect(15, floorY - 210, 52, floorY - 180, 8, 8, paint);
        canvas.drawRoundRect(15, floorY - 120, 52, floorY - 90, 8, 8, paint);
        canvas.drawRoundRect(getWidth() - 52, floorY - 210, getWidth() - 15, floorY - 180, 8, 8, paint);
        canvas.drawRoundRect(getWidth() - 52, floorY - 120, getWidth() - 15, floorY - 90, 8, 8, paint);

        // Ropes
        paint.setColor(Color.rgb(0, 180, 240));
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(34, floorY - 195, getWidth() - 34, floorY - 195, paint);
        canvas.drawLine(34, floorY - 105, getWidth() - 34, floorY - 105, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawControls(Canvas canvas) {
        float cy = getHeight() - 100f;

        // Left D-Pad Button
        paint.setColor(Color.argb(160, 0, 180, 240));
        canvas.drawRoundRect(60, cy - 40, 180, cy + 40, 15, 15, paint);

        // Right D-Pad Button
        canvas.drawRoundRect(210, cy - 40, 330, cy + 40, 15, 15, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("LEFT", 120, cy + 12, paint);
        canvas.drawText("RIGHT", 270, cy + 12, paint);

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
        float cy = getHeight() - 100f;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                performClick();

                // Left Button
                if (x >= 60 && x <= 180 && y >= cy - 40 && y <= cy + 40) {
                    movingLeft = true;
                    return true;
                }

                // Right Button
                if (x >= 210 && x <= 330 && y >= cy - 40 && y <= cy + 40) {
                    movingRight = true;
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

    public void pauseGame() {
        synchronized (lock) {
            gameState = GameState.PAUSED;
            movingLeft = false;
            movingRight = false;
        }
    }

    public void resumeGame() {
        synchronized (lock) {
            gameState = GameState.PLAYING;
        }
    }

    public boolean handleBackPressed() {
        if (gameState == GameState.PLAYING) {
            pauseGame();
            return true;
        } else if (gameState == GameState.PAUSED) {
            resumeGame();
            return true;
        }
        return false;
    }

    public void stopGame() {
        running = false;
    }
}
