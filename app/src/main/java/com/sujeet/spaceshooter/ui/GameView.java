package com.sujeet.spaceshooter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sujeet.spaceshooter.entities.*;
import com.sujeet.spaceshooter.utils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Random random =
            new Random();

    // ==============================
    // OBJECTS
    // ==============================

    private Player player;

    private final Player wingmanPlayer = new Player(0, 0);

    private Boss boss;

    private final List<Bullet> bullets =
            new ArrayList<>();

    private final List<Enemy> enemies =
            new ArrayList<>();

    private final List<Star> stars =
            new ArrayList<>();

    private final List<Explosion> explosions =
            new ArrayList<>();

    private final List<PowerUp> powerUps =
            new ArrayList<>();

    private final List<EnemyBullet> enemyBullets =
            new ArrayList<>();

    // ==============================
    // GAME
    // ==============================

    private GameState gameState =
            GameState.SPLASH;

    private int splashTimer = 0;

    private int score = 0;

    private int highScore = 0;

    private int lives = 3;

    private int level = 1;

    private int enemyTimer = 0;

    private int lastBossLevel = 0;

    // ==============================
    // POWER
    // ==============================

    private boolean shieldActive = false;

    private boolean rapidFire = false;

    private boolean doubleBullet = false;

    private boolean dualShipActive = false;

    private int powerTimer = 0;

    private int shootTimer = 0;

    // ==============================
    // TOUCH
    // ==============================

    private boolean touching = false;

    private float touchX = 0;

    // ==============================
    // LOCK
    // ==============================

    private final Object lock = new Object();

    // ==============================
    // LOOP
    // ==============================

    private boolean running = true;

    private Thread gameThread;

    public GameView(Context context) {

        super(context);

        setFocusable(true);

        createStars();

        highScore =
                ScoreStorage.load(context);

        startGameLoop();
    }

    // ==============================
    // STARS
    // ==============================

    private void createStars() {

        for (int i = 0; i < 120; i++) {

            stars.add(
                    new Star()
            );
        }
    }

    // ==============================
    // GAME LOOP
    // ==============================

    @SuppressWarnings("BusyWait")
    private void startGameLoop() {

        gameThread =
                new Thread(() -> {

                    while (running) {

                        if (gameState == GameState.SPLASH) {

                            synchronized (lock) {
                                updateStars();
                                splashTimer++;
                                if (splashTimer >= 140) {
                                    gameState = GameState.MENU;
                                }
                            }

                        } else if (gameState == GameState.PLAYING) {

                            synchronized (lock) {
                                updateGame();
                            }
                        }

                        postInvalidate();

                        try {

                            Thread.sleep(16);

                        } catch (
                                InterruptedException e) {

                            Thread.currentThread()
                                    .interrupt();

                            break;
                        }
                    }
                });

        gameThread.start();
    }

    // ==============================
    // UPDATE
    // ==============================

    private void updateGame() {

        updateStars();

        updatePlayer();

        updateBullets();

        updateEnemies();

        if (boss != null) {

            boss.update(getWidth(), enemyBullets);
        }

        updatePowerUps();

        updateExplosions();

        updateEnemyBullets();

        spawnEnemies();

        checkBulletEnemyCollision();

        checkPlayerEnemyCollision();

        checkPowerUpCollision();

        updatePower();

        updateLevel();

        updateBoss();
    }

    // ==============================
    // PLAYER
    // ==============================

    private void updatePlayer() {

        if (touching) {

            player.setX(touchX);
        }

        float halfWidth =
                player.getWidth() / 2;

        if (player.getX() <
                halfWidth) {

            player.setX(
                    halfWidth
            );
        }

        if (player.getX() >
                getWidth() - halfWidth) {

            player.setX(
                    getWidth() - halfWidth
            );
        }

        player.setY(
                getHeight() - 180
        );

        // Auto shooting

        shootTimer++;

        int delay =
                rapidFire ? 6 : 14;

        if (shootTimer >= delay) {

            shoot();

            shootTimer = 0;
        }
    }

    // ==============================
    // SHOOT
    // ==============================

    private void shoot() {

        if (player == null) {
            return;
        }

        SoundManager.playShoot();

        // Main Ship
        if (doubleBullet) {

            bullets.add(
                    new Bullet(
                            player.getX() - 20,
                            player.getY() - 50
                    )
            );

            bullets.add(
                    new Bullet(
                            player.getX() + 20,
                            player.getY() - 50
                    )
            );

        } else {

            bullets.add(
                    new Bullet(
                            player.getX(),
                            player.getY() - 50
                    )
            );
        }

        // Dual Wingman Drone Ship
        if (dualShipActive) {

            float wingmanX = player.getX() + 85;

            if (wingmanX > getWidth() - 40) {

                wingmanX = player.getX() - 85;
            }

            if (doubleBullet) {

                bullets.add(
                        new Bullet(
                                wingmanX - 15,
                                player.getY() - 50
                        )
                );

                bullets.add(
                        new Bullet(
                                wingmanX + 15,
                                player.getY() - 50
                        )
                );

            } else {

                bullets.add(
                        new Bullet(
                                wingmanX,
                                player.getY() - 50
                        )
                );
            }
        }
    }

    // ==============================
    // STARS
    // ==============================

    private void updateStars() {

        for (Star star : stars) {

            star.update(
                    getHeight(),
                    getWidth()
            );
        }
    }

    // ==============================
    // BULLETS
    // ==============================

    private void updateBullets() {

        Iterator<Bullet> iterator =
                bullets.iterator();

        while (iterator.hasNext()) {

            Bullet bullet =
                    iterator.next();

            bullet.update();

            if (bullet.getY() < -50) {

                iterator.remove();
            }
        }
    }

    // ==============================
    // ENEMIES
    // ==============================

    private void updateEnemies() {

        int screenWidth = getWidth();
        int screenHeight = getHeight();

        Iterator<Enemy> iterator =
                enemies.iterator();

        while (iterator.hasNext()) {

            Enemy enemy =
                    iterator.next();

            enemy.update(screenWidth);

            if (enemy.getY() >
                    screenHeight + 100) {

                iterator.remove();
            }
        }
    }

    // ==============================
    // POWER UPS
    // ==============================

    private void updatePowerUps() {

        Iterator<PowerUp> iterator =
                powerUps.iterator();

        while (iterator.hasNext()) {

            PowerUp powerUp =
                    iterator.next();

            powerUp.update();

            if (powerUp.getY() >
                    getHeight() + 50) {

                iterator.remove();
            }
        }
    }

    // ==============================
    // EXPLOSIONS
    // ==============================

    private void updateExplosions() {

        Iterator<Explosion> iterator =
                explosions.iterator();

        while (iterator.hasNext()) {

            Explosion explosion =
                    iterator.next();

            explosion.update();

            if (explosion.isFinished()) {

                iterator.remove();
            }
        }
    }

    private void updateEnemyBullets() {

        Iterator<EnemyBullet> iterator =
                enemyBullets.iterator();

        while (iterator.hasNext()) {

            EnemyBullet bullet =
                    iterator.next();

            bullet.update();

            if (bullet.isOutOfBounds(getHeight())) {

                iterator.remove();
            }
        }

        checkEnemyBulletPlayerCollision();
    }

    private void checkEnemyBulletPlayerCollision() {

        Iterator<EnemyBullet> iterator =
                enemyBullets.iterator();

        while (iterator.hasNext()) {

            EnemyBullet bullet =
                    iterator.next();

            if (player != null &&
                    RectF.intersects(
                            bullet.getBounds(),
                            player.getBounds()
                    )) {

                iterator.remove();

                explosions.add(
                        new Explosion(
                                player.getX(),
                                player.getY()
                        )
                );

                if (shieldActive) {

                    shieldActive = false;

                } else {

                    SoundManager.playHit();

                    lives--;

                    if (lives <= 0) {

                        lives = 0;

                        gameState =
                                GameState.GAME_OVER;

                        updateHighScore();
                    }
                }

                break;
            }
        }
    }

    // ==============================
    // SPAWN
    // ==============================

    private void spawnEnemies() {

        enemyTimer++;

        int delay =
                Math.max(
                        20,
                        65 - level * 4
                );

        if (enemyTimer >= delay) {

            float x =
                    60 + random.nextInt(
                            Math.max(
                                    1,
                                    getWidth() - 120
                            )
                    );

            EnemyType type =
                    EnemyType.values()[
                            random.nextInt(
                                    EnemyType.values().length
                            )
                    ];

            enemies.add(
                    new Enemy(
                            x,
                            80,
                            getEnemySpeed(),
                            type
                    )
            );

            enemyTimer = 0;
        }
    }

    private float getEnemySpeed() {

        return 4 +
                level * 0.5f;
    }

    // ==============================
    // BULLET COLLISION
    // ==============================

    private void checkBulletEnemyCollision() {

        Iterator<Bullet> bulletIterator =
                bullets.iterator();

        while (bulletIterator.hasNext()) {

            Bullet bullet =
                    bulletIterator.next();

            if (boss != null &&
                    RectF.intersects(
                            bullet.getBounds(),
                            boss.getBounds()
                    )) {

                float x = boss.getX();
                float y = boss.getY();

                bulletIterator.remove();

                boss.damage();

                if (boss.isDestroyed()) {

                    SoundManager.playExplosion();

                    explosions.add(
                            new Explosion(
                                    x,
                                    y
                            )
                    );

                    score += 250;

                    powerUps.add(
                            new PowerUp(
                                    x,
                                    y,
                                    PowerUpType.EXTRA_LIFE
                            )
                    );

                    boss = null;

                    updateHighScore();
                }

                break;
            }

            Iterator<Enemy> enemyIterator =
                    enemies.iterator();

            while (enemyIterator.hasNext()) {

                Enemy enemy =
                        enemyIterator.next();

                if (RectF.intersects(
                        bullet.getBounds(),
                        enemy.getBounds()
                )) {

                    float x = enemy.getX();
                    float y = enemy.getY();

                    bulletIterator.remove();

                    enemy.damage();

                    if (enemy.isDestroyed()) {

                        enemyIterator.remove();

                        SoundManager.playExplosion();

                        explosions.add(
                                new Explosion(
                                        x,
                                        y
                                )
                        );

                        score += 10;

                        if (random.nextInt(100) < 12) {

                            PowerUpType type =
                                    PowerUpType.values()[
                                            random.nextInt(
                                                    PowerUpType.values().length
                                            )
                                    ];

                            powerUps.add(
                                    new PowerUp(
                                            x,
                                            y,
                                            type
                                    )
                            );
                        }

                        updateHighScore();
                    }

                    break;
                }
            }
        }
    }

    // ==============================
    // PLAYER COLLISION
    // ==============================

    private void checkPlayerEnemyCollision() {

        if (boss != null &&
                RectF.intersects(
                        player.getBounds(),
                        boss.getBounds()
                )) {

            explosions.add(
                    new Explosion(
                            player.getX(),
                            player.getY()
                    )
            );

            if (shieldActive) {

                shieldActive = false;

            } else {

                SoundManager.playHit();

                lives--;

                if (lives <= 0) {

                    lives = 0;

                    gameState =
                            GameState.GAME_OVER;

                    updateHighScore();
                }
            }
        }

        Iterator<Enemy> iterator =
                enemies.iterator();

        while (iterator.hasNext()) {

            Enemy enemy =
                    iterator.next();

            if (RectF.intersects(
                    player.getBounds(),
                    enemy.getBounds()
            )) {

                iterator.remove();

                explosions.add(
                        new Explosion(
                                player.getX(),
                                player.getY()
                        )
                );

                if (shieldActive) {

                    shieldActive = false;

                    continue;
                }

                SoundManager.playHit();

                lives--;

                if (lives <= 0) {

                    lives = 0;

                    gameState =
                            GameState.GAME_OVER;

                    updateHighScore();

                    break;
                }
            }
        }
    }

    // ==============================
    // POWER COLLISION
    // ==============================

    private void checkPowerUpCollision() {

        Iterator<PowerUp> iterator =
                powerUps.iterator();

        while (iterator.hasNext()) {

            PowerUp powerUp =
                    iterator.next();

            if (RectF.intersects(
                    player.getBounds(),
                    powerUp.getBounds()
            )) {

                SoundManager.playPowerUp();

                activatePowerUp(
                        powerUp.getType()
                );

                iterator.remove();
            }
        }
    }

    // ==============================
    // POWER
    // ==============================

    private void activatePowerUp(
            PowerUpType type) {

        switch (type) {

            case RAPID_FIRE:

                rapidFire = true;

                powerTimer = 600;

                break;

            case DOUBLE_BULLET:

                doubleBullet = true;

                powerTimer = 600;

                break;

            case DUAL_SHIP:

                dualShipActive = true;

                powerTimer = 600;

                break;

            case SHIELD:

                shieldActive = true;

                break;

            case EXTRA_LIFE:

                if (lives < 5) {

                    lives++;
                }

                break;
        }
    }

    private void updatePower() {

        if (powerTimer > 0) {

            powerTimer--;

            if (powerTimer == 0) {

                rapidFire = false;

                doubleBullet = false;

                dualShipActive = false;
            }
        }
    }

    // ==============================
    // LEVEL
    // ==============================

    private void updateLevel() {

        level =
                (score / 500) + 1;
    }

    // ==============================
    // BOSS
    // ==============================

    private void updateBoss() {

        if (level > 0 &&
                level % 5 == 0 &&
                level != lastBossLevel) {

            if (boss == null) {

                lastBossLevel = level;

                float x =
                        getWidth() > 0 ?
                                getWidth() / 2f :
                                500f;

                boss = new Boss(
                        x,
                        -80,
                        level
                );
            }
        }
    }

    // ==============================
    // HIGH SCORE
    // ==============================

    private void updateHighScore() {

        if (score > highScore) {

            highScore = score;

            ScoreStorage.save(
                    getContext(),
                    highScore
            );
        }
    }

    // ==============================
    // DRAW
    // ==============================

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);

        synchronized (lock) {

            canvas.drawColor(
                    Color.rgb(
                            2,
                            5,
                            20
                    )
            );

            // Stars

            for (Star star : stars) {

                star.draw(
                        canvas,
                        paint
                );
            }

            if (gameState == GameState.SPLASH) {

                drawSplash(canvas);

                return;
            }

            if (gameState ==
                    GameState.MENU) {

                drawMenu(canvas);

                return;
            }

            if (gameState ==
                    GameState.SETTINGS) {

                drawSettings(canvas);

                return;
            }

            // Bullets

            for (Bullet bullet :
                    bullets) {

                bullet.draw(
                        canvas,
                        paint
                );
            }

            // Enemy Bullets

            for (EnemyBullet bullet :
                    enemyBullets) {

                bullet.draw(
                        canvas,
                        paint
                );
            }

            // Enemies

            for (Enemy enemy :
                    enemies) {

                enemy.draw(
                        canvas,
                        paint
                );
            }

            // Boss

            if (boss != null) {

                boss.draw(
                        canvas,
                        paint
                );
            }

            // PowerUps

            for (PowerUp powerUp :
                    powerUps) {

                powerUp.draw(
                        canvas,
                        paint
                );
            }

            // Player

            if (player != null) {

                player.draw(
                        canvas,
                        paint
                );

                if (dualShipActive) {

                    float wingmanX = player.getX() + 85;

                    if (wingmanX > getWidth() - 40) {

                        wingmanX = player.getX() - 85;
                    }

                    wingmanPlayer.setX(wingmanX);
                    wingmanPlayer.setY(player.getY());

                    wingmanPlayer.draw(
                            canvas,
                            paint
                    );
                }

                if (shieldActive) {

                    paint.setStyle(
                            Paint.Style.STROKE
                    );

                    paint.setStrokeWidth(6);

                    paint.setColor(
                            Color.CYAN
                    );

                    canvas.drawCircle(
                            player.getX(),
                            player.getY(),
                            65,
                            paint
                    );

                    paint.setStyle(
                            Paint.Style.FILL
                    );
                }
            }

            // Explosions

            for (Explosion explosion :
                    explosions) {

                explosion.draw(
                        canvas,
                        paint
                );
            }

            drawHUD(canvas);

            if (gameState ==
                    GameState.PAUSED) {

                drawPause(canvas);
            }

            if (gameState ==
                    GameState.GAME_OVER) {

                drawGameOver(canvas);
            }
        }
    }

    // ==============================
    // SPLASH
    // ==============================

    private void drawSplash(Canvas canvas) {

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        int alpha = 255;
        if (splashTimer < 30) {
            alpha = (int) (255 * (splashTimer / 30f));
        } else if (splashTimer > 110) {
            alpha = (int) (255 * ((140 - splashTimer) / 30f));
        }
        alpha = Math.max(0, Math.min(255, alpha));

        paint.setTextAlign(Paint.Align.CENTER);

        // Logo Outer Glowing Ring
        paint.setColor(Color.argb(alpha / 3, 0, 255, 255));
        canvas.drawCircle(cx, cy - 40, 110, paint);

        paint.setColor(Color.argb(alpha, 0, 200, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawCircle(cx, cy - 40, 95, paint);
        paint.setStyle(Paint.Style.FILL);

        // Subtitle Top
        paint.setColor(Color.argb(alpha, 180, 220, 255));
        paint.setTextSize(26);
        canvas.drawText("DESIGNED & DEVELOPED BY", cx, cy - 160, paint);

        // Main Brand Logo: SUJEET
        paint.setColor(Color.argb(alpha, 255, 220, 0));
        paint.setTextSize(85);
        canvas.drawText("SUJEET", cx, cy - 15, paint);

        // Subtitle Bottom
        paint.setColor(Color.argb(alpha, 0, 255, 255));
        paint.setTextSize(32);
        canvas.drawText("G A M E S", cx, cy + 90, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    // ==============================
    // MENU
    // ==============================

    private void drawMenu(Canvas canvas) {

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        paint.setTextAlign(Paint.Align.CENTER);

        // Title
        paint.setColor(Color.CYAN);
        paint.setTextSize(70);
        canvas.drawText("SPACE SHOOTER", cx, cy - 140, paint);

        // START GAME Button
        paint.setColor(Color.rgb(0, 180, 220));
        canvas.drawRoundRect(cx - 160, cy - 20, cx + 160, cy + 50, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(38);
        canvas.drawText("START GAME", cx, cy + 25, paint);

        // SETTINGS Button
        paint.setColor(Color.rgb(70, 80, 110));
        canvas.drawRoundRect(cx - 160, cy + 80, cx + 160, cy + 150, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(38);
        canvas.drawText("SETTINGS", cx, cy + 125, paint);

        // High Score
        paint.setTextSize(30);
        paint.setColor(Color.YELLOW);
        canvas.drawText("HIGH SCORE: " + highScore, cx, cy + 220, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    // ==============================
    // SETTINGS
    // ==============================

    private void drawSettings(Canvas canvas) {

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        paint.setTextAlign(Paint.Align.CENTER);

        // Title
        paint.setColor(Color.CYAN);
        paint.setTextSize(65);
        canvas.drawText("SETTINGS", cx, cy - 140, paint);

        // SOUND TOGGLE Button
        if (SoundManager.soundEnabled) {
            paint.setColor(Color.rgb(0, 160, 80));
        } else {
            paint.setColor(Color.rgb(180, 50, 50));
        }
        canvas.drawRoundRect(cx - 180, cy - 30, cx + 180, cy + 40, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(35);
        String soundText = SoundManager.soundEnabled ? "SOUND: ON" : "SOUND: OFF";
        canvas.drawText(soundText, cx, cy + 15, paint);

        // RESET HIGH SCORE Button
        paint.setColor(Color.rgb(180, 90, 0));
        canvas.drawRoundRect(cx - 180, cy + 70, cx + 180, cy + 140, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        canvas.drawText("RESET HIGH SCORE", cx, cy + 113, paint);

        // BACK Button
        paint.setColor(Color.rgb(70, 80, 110));
        canvas.drawRoundRect(cx - 180, cy + 170, cx + 180, cy + 240, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(35);
        canvas.drawText("BACK TO MENU", cx, cy + 215, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    // ==============================
    // HUD
    // ==============================

    private void drawHUD(Canvas canvas) {

        paint.setColor(
                Color.WHITE
        );

        paint.setTextSize(35);

        canvas.drawText(
                "Score: " + score,
                25,
                45,
                paint
        );

        paint.setTextAlign(
                Paint.Align.RIGHT
        );

        canvas.drawText(
                "Level: " + level,
                getWidth() - 30,
                45,
                paint
        );

        paint.setTextAlign(
                Paint.Align.LEFT
        );

        canvas.drawText(
                "Lives: " + lives,
                25,
                90,
                paint
        );

        if (rapidFire) {

            paint.setColor(
                    Color.RED
            );

            canvas.drawText(
                    "RAPID FIRE",
                    getWidth() / 2f - 90,
                    45,
                    paint
            );
        }

        if (doubleBullet) {

            paint.setColor(
                    Color.YELLOW
            );

            canvas.drawText(
                    "DOUBLE",
                    getWidth() / 2f - 65,
                    85,
                    paint
            );
        }

        if (dualShipActive) {

            paint.setColor(
                    Color.rgb(0, 255, 200)
            );

            canvas.drawText(
                    "DUAL SHIP 2X",
                    getWidth() / 2f - 110,
                    125,
                    paint
            );
        }
    }

    // ==============================
    // PAUSE
    // ==============================

    private void drawPause(Canvas canvas) {

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        paint.setColor(
                Color.argb(
                        220,
                        0,
                        0,
                        0
                )
        );

        canvas.drawRect(
                0,
                0,
                getWidth(),
                getHeight(),
                paint
        );

        paint.setTextAlign(Paint.Align.CENTER);

        // Title
        paint.setColor(Color.YELLOW);
        paint.setTextSize(65);
        canvas.drawText("PAUSED", cx, cy - 100, paint);

        // RESUME Button
        paint.setColor(Color.rgb(0, 180, 220));
        canvas.drawRoundRect(cx - 160, cy - 20, cx + 160, cy + 50, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        canvas.drawText("RESUME", cx, cy + 25, paint);

        // MAIN MENU Button
        paint.setColor(Color.rgb(180, 50, 50));
        canvas.drawRoundRect(cx - 160, cy + 80, cx + 160, cy + 150, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        canvas.drawText("MAIN MENU", cx, cy + 125, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    // ==============================
    // GAME OVER
    // ==============================

    private void drawGameOver(Canvas canvas) {

        paint.setColor(
                Color.argb(
                        210,
                        0,
                        0,
                        0
                )
        );

        canvas.drawRect(
                0,
                0,
                getWidth(),
                getHeight(),
                paint
        );

        paint.setColor(
                Color.RED
        );

        paint.setTextSize(70);

        canvas.drawText(
                "GAME OVER",
                getWidth() / 2f - 190,
                getHeight() / 2f - 100,
                paint
        );

        paint.setColor(
                Color.WHITE
        );

        paint.setTextSize(40);

        canvas.drawText(
                "Score: " + score,
                getWidth() / 2f - 80,
                getHeight() / 2f - 20,
                paint
        );

        canvas.drawText(
                "High Score: " +
                        highScore,
                getWidth() / 2f - 120,
                getHeight() / 2f + 35,
                paint
        );

        paint.setTextSize(35);

        canvas.drawText(
                "Tap to Restart",
                getWidth() / 2f - 115,
                getHeight() / 2f + 110,
                paint
        );
    }

    // ==============================
    // TOUCH
    // ==============================

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event) {

        float x =
                event.getX();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                performClick();

                float touchY = event.getY();
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;

                if (gameState == GameState.SPLASH) {

                    gameState = GameState.MENU;

                    return true;
                }

                if (gameState == GameState.MENU) {

                    // START GAME button
                    if (x >= cx - 160 && x <= cx + 160 && touchY >= cy - 20 && touchY <= cy + 50) {
                        startGame();
                        return true;
                    }

                    // SETTINGS button
                    if (x >= cx - 160 && x <= cx + 160 && touchY >= cy + 80 && touchY <= cy + 150) {
                        gameState = GameState.SETTINGS;
                        return true;
                    }

                    return true;
                }

                if (gameState == GameState.SETTINGS) {

                    // SOUND TOGGLE button
                    if (x >= cx - 180 && x <= cx + 180 && touchY >= cy - 30 && touchY <= cy + 40) {
                        SoundManager.soundEnabled = !SoundManager.soundEnabled;
                        return true;
                    }

                    // RESET HIGH SCORE button
                    if (x >= cx - 180 && x <= cx + 180 && touchY >= cy + 70 && touchY <= cy + 140) {
                        highScore = 0;
                        ScoreStorage.save(getContext(), 0);
                        return true;
                    }

                    // BACK TO MENU button
                    if (x >= cx - 180 && x <= cx + 180 && touchY >= cy + 170 && touchY <= cy + 240) {
                        gameState = GameState.MENU;
                        return true;
                    }

                    return true;
                }

                if (gameState ==
                        GameState.GAME_OVER) {

                    startGame();

                    return true;
                }

                if (gameState == GameState.PAUSED) {

                    // RESUME button
                    if (x >= cx - 160 && x <= cx + 160 && touchY >= cy - 20 && touchY <= cy + 50) {
                        gameState = GameState.PLAYING;
                        return true;
                    }

                    // MAIN MENU button
                    if (x >= cx - 160 && x <= cx + 160 && touchY >= cy + 80 && touchY <= cy + 150) {
                        gameState = GameState.MENU;
                        return true;
                    }

                    return true;
                }

                if (gameState == GameState.PLAYING) {

                    touching = true;
                    touchX = x;
                    return true;
                }

            case MotionEvent.ACTION_MOVE:

                if (gameState ==
                        GameState.PLAYING) {

                    touching = true;

                    touchX = x;
                }

                return true;

            case MotionEvent.ACTION_UP:

                touching = false;

                return true;
        }

        return true;
    }

    // ==============================
    // START
    // ==============================

    private void startGame() {
        synchronized (lock) {
            score = 0;

            lives = 3;

            level = 1;

            enemyTimer = 0;

            lastBossLevel = 0;

            shootTimer = 0;

            shieldActive = false;

            rapidFire = false;

            doubleBullet = false;

            dualShipActive = false;

            powerTimer = 0;

            bullets.clear();

            enemies.clear();

            explosions.clear();

            powerUps.clear();

            enemyBullets.clear();

            boss = null;

            lastBossLevel = 0;

            player = new Player(
                    getWidth() / 2f,
                    getHeight() - 180
            );

            gameState =
                    GameState.PLAYING;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGame();
    }

    // ==============================
    // STOP
    // ==============================

    public void stopGame() {

        running = false;

        try {

            if (gameThread != null) {

                gameThread.join();
            }

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }

    // ==============================
    // BACK & PAUSE
    // ==============================

    public boolean handleBackPressed() {
        synchronized (lock) {
            if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
                touching = false;
                return true;
            } else if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
                return true;
            } else if (gameState == GameState.SETTINGS || gameState == GameState.GAME_OVER) {
                gameState = GameState.MENU;
                return true;
            }
        }
        return false;
    }

    public void pauseGame() {
        synchronized (lock) {
            if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
                touching = false;
            }
        }
    }
}
