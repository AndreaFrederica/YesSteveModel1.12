package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.util.EnumHand;

public class ConditionManager {
    private final ConditionalSwing SWING = new ConditionalSwing(EnumHand.MAIN_HAND);
    private final ConditionalSwing SWING_OFFHAND = new ConditionalSwing(EnumHand.OFF_HAND);
    private final ConditionalUse USE_MAINHAND = new ConditionalUse(EnumHand.MAIN_HAND);
    private final ConditionalUse USE_OFFHAND = new ConditionalUse(EnumHand.OFF_HAND);
    private final ConditionalHold HOLD_MAINHAND = new ConditionalHold(EnumHand.MAIN_HAND);
    private final ConditionalHold HOLD_OFFHAND = new ConditionalHold(EnumHand.OFF_HAND);
    private final ConditionArmor ARMOR = new ConditionArmor();
    private final ConditionalVehicle VEHICLE = new ConditionalVehicle();
    private final ConditionalPassenger PASSENGER = new ConditionalPassenger();

    public void addTest(String name) {
        this.SWING.addTest(name);
        this.SWING_OFFHAND.addTest(name);
        this.USE_MAINHAND.addTest(name);
        this.USE_OFFHAND.addTest(name);
        this.HOLD_MAINHAND.addTest(name);
        this.HOLD_OFFHAND.addTest(name);
        this.ARMOR.addTest(name);
        this.VEHICLE.addTest(name);
        this.PASSENGER.addTest(name);
    }

    public ConditionalSwing getSwingMainhand() {
        return this.SWING;
    }

    public ConditionalSwing getSwingOffhand() {
        return this.SWING_OFFHAND;
    }

    public ConditionalUse getUseMainhand() {
        return this.USE_MAINHAND;
    }

    public ConditionalUse getUseOffhand() {
        return this.USE_OFFHAND;
    }

    public ConditionalHold getHoldMainhand() {
        return this.HOLD_MAINHAND;
    }

    public ConditionalHold getHoldOffhand() {
        return this.HOLD_OFFHAND;
    }

    public ConditionArmor getArmor() {
        return this.ARMOR;
    }

    public ConditionalVehicle getVehicle() {
        return this.VEHICLE;
    }

    public ConditionalPassenger getPassenger() {
        return this.PASSENGER;
    }
}
