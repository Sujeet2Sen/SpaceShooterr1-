package com.sujeet.wrestling;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class GameUI {

    private static final RectF leftBtnRect = new RectF();
    private static final RectF rightBtnRect = new RectF();
    private static final RectF punchBtnRect = new RectF();
    private static final RectF kickBtnRect = new RectF();
    private static final RectF blockBtnRect = new RectF();
    private static final RectF specialBtnRect = new RectF();
    private static final RectF restartBtnRect = new RectF();

    public static void drawHUD(Canvas canvas, Paint paint, Fighter player, Fighter enemy, int width, int height) {
        if (player == null || enemy == null || width <= 0 || height <= 0) return;

        float barWidth = width * 0.36f;
        float barHeight = 28f;
        float topY = 45f;

        // Player Health Bar (Top-Left)
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(25, topY, 25 + barWidth + 6, topY + barHeight + 6, 8, 8, paint);

        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(28, topY + 3, 28 + barWidth, topY + barHeight, 6, 6, paint);

        float p1HpPct = (float) player.getHealth() / player.getMaxHealth();
        paint.setColor(Color.rgb(0, 230, 120));
        canvas.drawRoundRect(28, topY + 3, 28 + barWidth * p1HpPct, topY + barHeight, 6, 6, paint);

        paint.setColor(Color.CYAN);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("PLAYER (" + player.getHealth() + "/" + player.getMaxHealth() + ")", 28, topY - 10, paint);

        // Player Power Meter (Below Health Bar)
        float p1PowerPct = (float) player.getPower() / player.getMaxPower();
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(28, topY + barHeight + 8, 28 + barWidth, topY + barHeight + 20, 4, 4, paint);

        paint.setColor(player.getPower() >= player.getMaxPower() ? Color.rgb(0, 255, 200) : Color.rgb(0, 180, 240));
        canvas.drawRoundRect(28, topY + barHeight + 8, 28 + barWidth * p1PowerPct, topY + barHeight + 20, 4, 4, paint);

        // Enemy Health Bar (Top-Right)
        float enemyRight = width - 28f;
        float enemyLeft = enemyRight - barWidth;

        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(enemyLeft - 3, topY, enemyRight + 3, topY + barHeight + 6, 8, 8, paint);

        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(enemyLeft, topY + 3, enemyRight, topY + barHeight, 6, 6, paint);

        float p2HpPct = (float) enemy.getHealth() / enemy.getMaxHealth();
        paint.setColor(Color.rgb(240, 40, 50));
        canvas.drawRoundRect(enemyRight - barWidth * p2HpPct, topY + 3, enemyRight, topY + barHeight, 6, 6, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.rgb(255, 120, 120));
        canvas.drawText("RIVAL (" + enemy.getHealth() + "/" + enemy.getMaxHealth() + ")", enemyRight, topY - 10, paint);

        // Enemy Power Meter (Below Health Bar)
        float p2PowerPct = (float) enemy.getPower() / enemy.getMaxPower();
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(enemyLeft, topY + barHeight + 8, enemyRight, topY + barHeight + 20, 4, 4, paint);

        paint.setColor(enemy.getPower() >= enemy.getMaxPower() ? Color.YELLOW : Color.rgb(220, 100, 0));
        canvas.drawRoundRect(enemyRight - barWidth * p2PowerPct, topY + barHeight + 8, enemyRight, topY + barHeight + 20, 4, 4, paint);

        // Center VS Emblem
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(32);
        canvas.drawText("VS", width / 2f, topY + barHeight, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    public static void drawControls(Canvas canvas, Paint paint, Fighter player, int width, int height) {
        if (width <= 0 || height <= 0) return;

        float bottomMargin = 120f;
        float btnY = height - bottomMargin;
        float btnHeight = 70f;

        // D-Pad Left & Right Buttons
        leftBtnRect.set(25, btnY - btnHeight, 135, btnY);
        rightBtnRect.set(150, btnY - btnHeight, 260, btnY);

        paint.setColor(Color.argb(180, 0, 180, 240));
        canvas.drawRoundRect(leftBtnRect, 14, 14, paint);
        canvas.drawRoundRect(rightBtnRect, 14, 14, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("LEFT", leftBtnRect.centerX(), leftBtnRect.centerY() + 9, paint);
        canvas.drawText("RIGHT", rightBtnRect.centerX(), rightBtnRect.centerY() + 9, paint);

        // Action Buttons: PUNCH, KICK, BLOCK, SPECIAL
        float rightMargin = width - 25f;
        float btnW = 95f;

        specialBtnRect.set(rightMargin - btnW, btnY - btnHeight, rightMargin, btnY);
        blockBtnRect.set(rightMargin - 2 * btnW - 10, btnY - btnHeight, rightMargin - btnW - 10, btnY);
        kickBtnRect.set(rightMargin - 3 * btnW - 20, btnY - btnHeight, rightMargin - 2 * btnW - 20, btnY);
        punchBtnRect.set(rightMargin - 4 * btnW - 30, btnY - btnHeight, rightMargin - 3 * btnW - 30, btnY);

        // Punch
        paint.setColor(Color.argb(200, 230, 40, 50));
        canvas.drawRoundRect(punchBtnRect, 14, 14, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("PUNCH", punchBtnRect.centerX(), punchBtnRect.centerY() + 9, paint);

        // Kick
        paint.setColor(Color.argb(200, 240, 150, 0));
        canvas.drawRoundRect(kickBtnRect, 14, 14, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("KICK", kickBtnRect.centerX(), kickBtnRect.centerY() + 9, paint);

        // Block
        paint.setColor(Color.argb(200, 120, 140, 160));
        canvas.drawRoundRect(blockBtnRect, 14, 14, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("BLOCK", blockBtnRect.centerX(), blockBtnRect.centerY() + 9, paint);

        // Special (Glows when 100% full)
        boolean isSpecialReady = player != null && player.getPower() >= player.getMaxPower();
        paint.setColor(isSpecialReady ? Color.argb(230, 0, 255, 200) : Color.argb(120, 0, 180, 240));
        canvas.drawRoundRect(specialBtnRect, 14, 14, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("SPECIAL", specialBtnRect.centerX(), specialBtnRect.centerY() + 9, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    public static void drawOverlayResult(Canvas canvas, Paint paint, String resultText, int width, int height) {
        if (width <= 0 || height <= 0) return;

        float cx = width / 2f;
        float cy = height / 2f;

        paint.setColor(Color.argb(220, 0, 0, 0));
        canvas.drawRect(0, 0, width, height, paint);

        paint.setTextAlign(Paint.Align.CENTER);

        boolean isVictory = resultText.contains("VICTORY");
        paint.setColor(isVictory ? Color.YELLOW : Color.rgb(255, 50, 50));
        paint.setTextSize(65);
        canvas.drawText(resultText, cx, cy - 50, paint);

        // RESTART Button
        restartBtnRect.set(cx - 160, cy + 30, cx + 160, cy + 100);
        paint.setColor(Color.rgb(0, 180, 240));
        canvas.drawRoundRect(restartBtnRect, 16, 16, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        canvas.drawText("RESTART", cx, cy + 76, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    public static boolean isLeftPressed(float x, float y) { return leftBtnRect.contains(x, y); }
    public static boolean isRightPressed(float x, float y) { return rightBtnRect.contains(x, y); }
    public static boolean isPunchPressed(float x, float y) { return punchBtnRect.contains(x, y); }
    public static boolean isKickPressed(float x, float y) { return kickBtnRect.contains(x, y); }
    public static boolean isBlockPressed(float x, float y) { return blockBtnRect.contains(x, y); }
    public static boolean isSpecialPressed(float x, float y) { return specialBtnRect.contains(x, y); }
    public static boolean isRestartPressed(float x, float y) { return restartBtnRect.contains(x, y); }
}
