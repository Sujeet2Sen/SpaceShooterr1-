package com.sujeet.spaceshooter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class GameSelectView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface OnGameSelectedListener {
        void onSelectSpaceShooter();
        void onSelectStreetFighter();
    }

    private final OnGameSelectedListener listener;

    public GameSelectView(Context context, OnGameSelectedListener listener) {
        super(context);
        this.listener = listener;
        setFocusable(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Dark Arcady Space Background
        canvas.drawColor(Color.rgb(15, 12, 30));

        paint.setTextAlign(Paint.Align.CENTER);

        // Header Title
        paint.setColor(Color.YELLOW);
        paint.setTextSize(55);
        canvas.drawText("SELECT GAME MODE", cx, cy - 180, paint);

        // Option 1: SPACE SHOOTER
        paint.setColor(Color.rgb(0, 180, 220));
        canvas.drawRoundRect(cx - 220, cy - 80, cx + 220, cy, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(38);
        canvas.drawText("🚀 SPACE SHOOTER", cx, cy - 30, paint);

        // Option 2: STREET FIGHTER
        paint.setColor(Color.rgb(220, 50, 50));
        canvas.drawRoundRect(cx - 220, cy + 40, cx + 220, cy + 120, 20, 20, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(38);
        canvas.drawText("🥊 STREET FIGHTER", cx, cy + 90, paint);

        // Footer Branding
        paint.setColor(Color.rgb(150, 180, 220));
        paint.setTextSize(26);
        canvas.drawText("SUJEET GAMES ARCADE COLLECTION", cx, cy + 220, paint);

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

            // Space Shooter Selection
            if (x >= cx - 220 && x <= cx + 220 && y >= cy - 80 && y <= cy) {
                if (listener != null) {
                    listener.onSelectSpaceShooter();
                }
                return true;
            }

            // Street Fighter Selection
            if (x >= cx - 220 && x <= cx + 220 && y >= cy + 40 && y <= cy + 120) {
                if (listener != null) {
                    listener.onSelectStreetFighter();
                }
                return true;
            }
        }
        return true;
    }
}
