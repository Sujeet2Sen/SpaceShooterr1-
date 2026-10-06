package com.sujeet.wrestling;

import android.graphics.Color;

public class EnemyFighter extends Fighter {

    public EnemyFighter(float x, float y) {
        super("RIVAL", x, y, false, Color.rgb(230, 40, 50), Color.rgb(200, 200, 220));
    }
}
