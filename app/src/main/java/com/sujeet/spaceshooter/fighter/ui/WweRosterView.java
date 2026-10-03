package com.sujeet.spaceshooter.fighter.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sujeet.spaceshooter.R;
import com.sujeet.spaceshooter.fighter.data.WweRosterStorage;
import com.sujeet.spaceshooter.fighter.entities.WweSuperstar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WweRosterView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, Bitmap> photoCache = new HashMap<>();

    public interface OnRosterActionListener {
        void onStartFight();
        void onBackToSelect();
    }

    private final OnRosterActionListener listener;
    private List<WweSuperstar> roster;
    private int selectedIndex = 0;
    private int coins = 0;

    public WweRosterView(Context context, OnRosterActionListener listener) {
        super(context);
        this.listener = listener;
        setFocusable(true);
        refreshRoster();
    }

    private Bitmap getCachedPhoto(String heroId) {
        if (photoCache.containsKey(heroId)) {
            return photoCache.get(heroId);
        }
        int resId = 0;
        if (heroId.equals("roman")) resId = R.drawable.roman;
        else if (heroId.equals("cena")) resId = R.drawable.cena;
        else if (heroId.equals("rock")) resId = R.drawable.rock;
        else if (heroId.equals("undertaker")) resId = R.drawable.undertaker;

        if (resId != 0) {
            try {
                Bitmap b = BitmapFactory.decodeResource(getResources(), resId);
                photoCache.put(heroId, b);
                return b;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public void refreshRoster() {
        coins = WweRosterStorage.getCoins(getContext());
        roster = WweRosterStorage.loadRoster(getContext());

        String currentId = WweRosterStorage.getSelectedHeroId(getContext());
        for (int i = 0; i < roster.size(); i++) {
            if (roster.get(i).getId().equals(currentId)) {
                selectedIndex = i;
                break;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Dark WWE Arena Background
        canvas.drawColor(Color.rgb(18, 12, 28));

        paint.setTextAlign(Paint.Align.CENTER);

        // Header Title
        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        paint.setTextSize(52);
        canvas.drawText("WWE SUPERSTARS ROSTER", cx, 70, paint);

        // Coins Display
        paint.setColor(Color.YELLOW);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("🪙 COINS: " + coins, getWidth() - 30, 70, paint);
        paint.setTextAlign(Paint.Align.CENTER);

        if (roster == null || roster.isEmpty()) return;

        WweSuperstar current = roster.get(selectedIndex);

        // Hero Card Background Frame
        paint.setColor(Color.rgb(35, 25, 50));
        canvas.drawRoundRect(cx - 240, 105, cx + 240, cy + 95, 24, 24, paint);

        paint.setColor(current.getPrimaryColor());
        canvas.drawRoundRect(cx - 230, 115, cx + 230, cy + 85, 20, 20, paint);

        // Character Name
        paint.setColor(Color.WHITE);
        paint.setTextSize(44);
        canvas.drawText(current.getName(), cx, 160, paint);

        // Draw Real Character Photo Portrait Badge
        drawCharacterPortrait(canvas, current, cx, cy - 80);

        // Navigation Arrows ◄ and ►
        paint.setColor(Color.YELLOW);
        paint.setTextSize(60);
        canvas.drawText("◄", cx - 195, cy - 60, paint);
        canvas.drawText("►", cx + 195, cy - 60, paint);

        // Stats Display
        paint.setTextSize(32);
        paint.setColor(Color.WHITE);
        canvas.drawText("POWER LEVEL: " + current.getPowerLevel() + " / 10", cx, cy + 10, paint);
        canvas.drawText("HEALTH: " + current.getMaxHealth() + " HP", cx, cy + 45, paint);
        canvas.drawText("ATTACK DAMAGE: " + current.getBaseDamage(), cx, cy + 80, paint);

        // Action Buttons at Bottom
        if (!current.isUnlocked()) {
            // UNLOCK BUTTON
            paint.setColor(coins >= current.getUnlockCost() ? Color.rgb(0, 200, 100) : Color.GRAY);
            canvas.drawRoundRect(cx - 200, cy + 125, cx + 200, cy + 190, 20, 20, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(36);
            canvas.drawText("UNLOCK (" + current.getUnlockCost() + " COINS)", cx, cy + 168, paint);
        } else {
            // UPGRADE POWER BUTTON
            int upCost = current.getUpgradeCost();
            paint.setColor(coins >= upCost && current.getPowerLevel() < 10 ? Color.rgb(220, 140, 0) : Color.GRAY);
            canvas.drawRoundRect(cx - 220, cy + 115, cx + 220, cy + 175, 18, 18, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            String upText = current.getPowerLevel() < 10 ? "UPGRADE POWER (" + upCost + " COINS)" : "MAX POWER!";
            canvas.drawText(upText, cx, cy + 153, paint);

            // FIGHT / SELECT BUTTON
            boolean isSelected = WweRosterStorage.getSelectedHeroId(getContext()).equals(current.getId());
            paint.setColor(isSelected ? Color.rgb(0, 180, 250) : Color.rgb(0, 220, 120));
            canvas.drawRoundRect(cx - 220, cy + 195, cx + 220, cy + 255, 18, 18, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(34);
            canvas.drawText(isSelected ? "SELECTED - FIGHT NOW!" : "SELECT FOR BATTLE", cx, cy + 235, paint);
        }

        // BACK TO MENU BUTTON
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawRoundRect(cx - 160, cy + 275, cx + 160, cy + 330, 15, 15, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        canvas.drawText("BACK", cx, cy + 312, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawCharacterPortrait(Canvas canvas, WweSuperstar superstar, float x, float y) {
        // Portrait Circular Badge Base
        paint.setColor(Color.rgb(20, 20, 35));
        canvas.drawCircle(x, y, 75, paint);

        Bitmap photo = getCachedPhoto(superstar.getId());
        if (photo != null) {
            canvas.save();
            Path clipPath = new Path();
            clipPath.addCircle(x, y, 70, Path.Direction.CW);
            canvas.clipPath(clipPath);

            Rect src = new Rect(0, 0, photo.getWidth(), photo.getHeight());
            RectF dst = new RectF(x - 70, y - 70, x + 70, y + 70);
            canvas.drawBitmap(photo, src, dst, paint);
            canvas.restore();
        }

        // Gold / Team Primary Color Border Ring
        paint.setColor(superstar.getPrimaryColor());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawCircle(x, y, 72, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();

            float x = event.getX();
            float y = event.getY();
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;

            if (roster == null || roster.isEmpty()) return true;

            WweSuperstar current = roster.get(selectedIndex);

            // Left Arrow ◄
            if (x >= cx - 240 && x <= cx - 160 && y >= cy - 110 && y <= cy + 20) {
                selectedIndex = (selectedIndex - 1 + roster.size()) % roster.size();
                invalidate();
                return true;
            }

            // Right Arrow ►
            if (x >= cx + 160 && x <= cx + 240 && y >= cy - 110 && y <= cy + 20) {
                selectedIndex = (selectedIndex + 1) % roster.size();
                invalidate();
                return true;
            }

            // UNLOCK Button
            if (!current.isUnlocked()) {
                if (x >= cx - 200 && x <= cx + 200 && y >= cy + 125 && y <= cy + 190) {
                    if (coins >= current.getUnlockCost()) {
                        WweRosterStorage.addCoins(getContext(), -current.getUnlockCost());
                        WweRosterStorage.saveSuperstarState(getContext(), current.getId(), true, current.getPowerLevel());
                        WweRosterStorage.setSelectedHeroId(getContext(), current.getId());
                        refreshRoster();
                    }
                    return true;
                }
            } else {
                // UPGRADE POWER Button
                if (x >= cx - 220 && x <= cx + 220 && y >= cy + 115 && y <= cy + 175) {
                    int upCost = current.getUpgradeCost();
                    if (coins >= upCost && current.getPowerLevel() < 10) {
                        WweRosterStorage.addCoins(getContext(), -upCost);
                        WweRosterStorage.saveSuperstarState(getContext(), current.getId(), true, current.getPowerLevel() + 1);
                        refreshRoster();
                    }
                    return true;
                }

                // FIGHT / SELECT Button
                if (x >= cx - 220 && x <= cx + 220 && y >= cy + 195 && y <= cy + 255) {
                    WweRosterStorage.setSelectedHeroId(getContext(), current.getId());
                    if (listener != null) {
                        listener.onStartFight();
                    }
                    return true;
                }
            }

            // BACK Button
            if (x >= cx - 160 && x <= cx + 160 && y >= cy + 275 && y <= cy + 330) {
                if (listener != null) {
                    listener.onBackToSelect();
                }
                return true;
            }
        }
        return true;
    }
}
