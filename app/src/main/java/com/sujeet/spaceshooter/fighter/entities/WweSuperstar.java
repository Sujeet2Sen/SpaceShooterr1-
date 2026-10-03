package com.sujeet.spaceshooter.fighter.entities;

public class WweSuperstar {

    private final String id;
    private final String name;
    private final int unlockCost;
    private boolean unlocked;
    private int powerLevel; // 1 to 10
    private final int primaryColor;
    private final int secondaryColor;

    public WweSuperstar(String id, String name, int unlockCost, boolean unlocked, int powerLevel, int primaryColor, int secondaryColor) {
        this.id = id;
        this.name = name;
        this.unlockCost = unlockCost;
        this.unlocked = unlocked;
        this.powerLevel = powerLevel;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getUnlockCost() {
        return unlockCost;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public int getMaxHealth() {
        return 100 + (powerLevel - 1) * 20; // +20 HP per power upgrade
    }

    public int getBaseDamage() {
        return 10 + (powerLevel - 1) * 3; // +3 Damage per power upgrade
    }

    public int getUpgradeCost() {
        return powerLevel * 200; // Upgrade cost
    }
}
