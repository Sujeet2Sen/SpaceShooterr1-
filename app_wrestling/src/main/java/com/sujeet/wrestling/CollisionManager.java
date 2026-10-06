package com.sujeet.wrestling;

public class CollisionManager {

    public static void keepInsideRing(Fighter fighter, int screenWidth) {
        if (fighter == null || screenWidth <= 0) return;

        float minX = 60f + fighter.getWidth() / 2;
        float maxX = screenWidth - 60f - fighter.getWidth() / 2;

        if (fighter.getX() < minX) {
            fighter.setX(minX);
        } else if (fighter.getX() > maxX) {
            fighter.setX(maxX);
        }
    }

    public static void preventOverlap(Fighter p1, Fighter p2) {
        if (p1 == null || p2 == null) return;

        float minDist = (p1.getWidth() + p2.getWidth()) / 2f - 20f;
        float dist = p2.getX() - p1.getX();

        if (dist > 0 && dist < minDist) {
            float overlap = (minDist - dist) / 2f;
            p1.setX(p1.getX() - overlap);
            p2.setX(p2.getX() + overlap);
        } else if (dist < 0 && Math.abs(dist) < minDist) {
            float overlap = (minDist - Math.abs(dist)) / 2f;
            p1.setX(p1.getX() + overlap);
            p2.setX(p2.getX() - overlap);
        }
    }
}
