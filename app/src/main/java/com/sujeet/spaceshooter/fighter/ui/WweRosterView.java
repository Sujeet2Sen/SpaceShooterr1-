package com.sujeet.spaceshooter.fighter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sujeet.spaceshooter.fighter.data.WweRosterStorage;
import com.sujeet.spaceshooter.fighter.entities.WweSuperstar;

import java.util.List;

public class WweRosterView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

        // Hero Card Background
        paint.setColor(Color.rgb(35, 25, 50));
        canvas.drawRoundRect(cx - 240, 110, cx + 240, cy + 90, 24, 24, paint);

        paint.setColor(current.getPrimaryColor());
        canvas.drawRoundRect(cx - 230, 120, cx + 230, cy + 80, 20, 20, paint);

        // Character Name
        paint.setColor(Color.WHITE);
        paint.setTextSize(44);
        canvas.drawText(current.getName(), cx, 175, paint);

        // Navigation Arrows ◄ and ►
        paint.setColor(Color.YELLOW);
        paint.setTextSize(55);
        canvas.drawText("◄", cx - 200, cy - 20, paint);
        canvas.drawText("►", cx + 200, cy - 20, paint);

        // Stats Display
        paint.setTextSize(32);
        paint.setColor(Color.WHITE);
        canvas.drawText("POWER LEVEL: " + current.getPowerLevel() + " / 10", cx, cy - 40, paint);
        canvas.drawText("HEALTH: " + current.getMaxHealth() + " HP", cx, cy, paint);
        canvas.drawText("ATTACK DAMAGE: " + current.getBaseDamage(), cx, cy + 40, paint);

        // Action Buttons at Bottom
        if (!current.isUnlocked()) {
            // UNLOCK BUTTON
            paint.setColor(coins >= current.getUnlockCost() ? Color.rgb(0, 200, 100) : Color.GRAY);
            canvas.drawRoundRect(cx - 200, cy + 120, cx + 200, cy + 190, 20, 20, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(36);
            canvas.drawText("UNLOCK (" + current.getUnlockCost() + " COINS)", cx, cy + 165, paint);
        } else {
            // UPGRADE POWER BUTTON
            int upCost = current.getUpgradeCost();
            paint.setColor(coins >= upCost && current.getPowerLevel() < 10 ? Color.rgb(220, 140, 0) : Color.GRAY);
            canvas.drawRoundRect(cx - 220, cy + 110, cx + 220, cy + 170, 18, 18, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            String upText = current.getPowerLevel() < 10 ? "UPGRADE POWER (" + upCost + " COINS)" : "MAX POWER!";
            canvas.drawText(upText, cx, cy + 148, paint);

            // FIGHT / SELECT BUTTON
            boolean isSelected = WweRosterStorage.getSelectedHeroId(getContext()).equals(current.getId());
            paint.setColor(isSelected ? Color.rgb(0, 180, 250) : Color.rgb(0, 220, 120));
            canvas.drawRoundRect(cx - 220, cy + 190, cx + 220, cy + 250, 18, 18, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(34);
            canvas.drawText(isSelected ? "SELECTED - FIGHT NOW!" : "SELECT FOR BATTLE", cx, cy + 230, paint);
        }

        // BACK TO MENU BUTTON
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawRoundRect(cx - 160, cy + 270, cx + 160, cy + 325, 15, 15, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        canvas.drawText("BACK", cx, cy + 307, paint);

        paint.setTextAlign(Paint.Align.LEFT);
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
            if (x >= cx - 240 && x <= cx - 160 && y >= cy - 80 && y <= cy + 40) {
                selectedIndex = (selectedIndex - 1 + roster.size()) % roster.size();
                invalidate();
                return true;
            }

            // Right Arrow ►
            if (x >= cx + 160 && x <= cx + 240 && y >= cy - 80 && y <= cy + 40) {
                selectedIndex = (selectedIndex + 1) % roster.size();
                invalidate();
                return true;
            }

            // UNLOCK Button
            if (!current.isUnlocked()) {
                if (x >= cx - 200 && x <= cx + 200 && y >= cy + 120 && y <= cy + 190) {
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
                if (x >= cx - 220 && x <= cx + 220 && y >= cy + 110 && y <= cy + 170) {
                    int upCost = current.getUpgradeCost();
                    if (coins >= upCost && current.getPowerLevel() < 10) {
                        WweRosterStorage.addCoins(getContext(), -upCost);
                        WweRosterStorage.saveSuperstarState(getContext(), current.getId(), true, current.getPowerLevel() + 1);
                        refreshRoster();
                    }
                    return true;
                }

                // FIGHT / SELECT Button
                if (x >= cx - 220 && x <= cx + 220 && y >= cy + 190 && y <= cy + 250) {
                    WweRosterStorage.setSelectedHeroId(getContext(), current.getId());
                    if (listener != null) {
                        listener.onStartFight();
                    }
                    return true;
                }
            }

            // BACK Button
            if (x >= cx - 160 && x <= cx + 160 && y >= cy + 270 && y <= cy + 325) {
                if (listener != null) {
                    listener.onBackToSelect();
                }
                return true;
            }
        }
        return true;
    }
}
